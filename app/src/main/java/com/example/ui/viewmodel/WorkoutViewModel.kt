package com.example.ui.viewmodel

import android.app.Activity
import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.auth.AuthManager
import com.example.auth.AuthUserState
import com.example.data.AppDatabase
import com.example.data.entity.*
import com.example.data.repository.WorkoutRepository
import com.example.data.sync.CloudSyncManager
import com.example.data.sync.SyncStatus
import com.example.util.StreakCalculator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.time.DayOfWeek
import java.time.LocalDate

class WorkoutViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = WorkoutRepository(AppDatabase.getDatabase(application))
    private val authManager = AuthManager.getInstance(application)
    private val syncManager = CloudSyncManager.getInstance(application)

    val authState: StateFlow<AuthUserState> = authManager.authState
    val syncStatus: StateFlow<SyncStatus> = syncManager.syncStatus

    val routines: StateFlow<List<RoutineEntity>> = repository.allRoutines
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val weeklySplits: StateFlow<List<WeeklySplitEntity>> = repository.allWeeklySplits
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val workoutLogs: StateFlow<List<WorkoutLogEntity>> = repository.allWorkoutLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val completedEpochDays: StateFlow<Set<Long>> = repository.completedDates
        .map { it.toSet() }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    val exercises: StateFlow<List<ExerciseEntity>> = repository.allExercises
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val measurements: StateFlow<List<BodyMeasurementEntity>> = repository.allMeasurements
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Rest-Day Aware Active Streak (Computed strictly on background thread via Dispatchers.Default)
    val activeStreak: StateFlow<Int> = combine(completedEpochDays, weeklySplits) { epochDays, splits ->
        val logDates = epochDays.map { LocalDate.ofEpochDay(it) }.toSet()
        val splitMap = splits.associateBy { DayOfWeek.of(it.dayOfWeek) }
        StreakCalculator.calculateActiveStreak(logDates, splitMap)
    }.flowOn(Dispatchers.Default)
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Rest-Day Aware Consecutive Completed Weeks Streak (Background thread via Dispatchers.Default)
    val weeklyStreak: StateFlow<Int> = combine(completedEpochDays, weeklySplits) { epochDays, splits ->
        val logDates = epochDays.map { LocalDate.ofEpochDay(it) }.toSet()
        val splitMap = splits.associateBy { DayOfWeek.of(it.dayOfWeek) }
        StreakCalculator.calculateWeeklyStreak(logDates, splitMap)
    }.flowOn(Dispatchers.Default)
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val activeDraft: StateFlow<ActiveWorkoutDraftEntity?> = repository.activeDraft
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    init {
        // Trigger initial safe cloud sync on startup
        syncWithCloud()
    }

    /**
     * Crash-guarded network sync with strict timeout so spotty gym Wi-Fi/cellular never freezes the UI.
     */
    fun syncWithCloud() {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                val currentUid = authManager.currentUser?.uid
                if (!currentUid.isNullOrBlank()) {
                    syncManager.syncAll(currentUid)
                } else {
                    Log.d("IronLogSync", "Local guest mode: cloud sync idle.")
                }
            }.onFailure { error ->
                Log.w("IronLogSync", "Cloud sync guard safely caught error: ${error.localizedMessage}")
            }
        }
    }

    fun signInWithGoogle(activity: Activity) {
        viewModelScope.launch {
            val result = authManager.signInWithGoogle(activity)
            if (result.isSuccess) {
                syncWithCloud()
            }
        }
    }

    fun continueAsGuest() {
        authManager.continueAsGuest()
    }

    fun signOut() {
        authManager.signOut()
    }

    fun duplicateRoutine(routine: RoutineEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.duplicateRoutine(routine)
            syncWithCloud()
        }
    }

    fun deleteRoutine(routineId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteRoutine(routineId)
            syncWithCloud()
        }
    }

    fun createRoutine(title: String, desc: String, exercisesSummary: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertRoutine(title, desc, exercisesSummary)
            syncWithCloud()
        }
    }

    fun addExerciseToRoutine(routine: RoutineEntity, exerciseName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val currentSummary = routine.exercisesSummary.trim()
            val updatedSummary = if (currentSummary.isBlank()) {
                exerciseName
            } else if (!currentSummary.contains(exerciseName, ignoreCase = true)) {
                "$currentSummary, $exerciseName"
            } else {
                currentSummary
            }
            val updatedRoutine = routine.copy(exercisesSummary = updatedSummary)
            repository.updateRoutine(updatedRoutine)
            syncWithCloud()
        }
    }

    fun updateRoutinesOrder(reordered: List<RoutineEntity>) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateRoutinesOrder(reordered)
            syncWithCloud()
        }
    }

    fun saveWeeklySplit(splits: List<WeeklySplitEntity>) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.saveWeeklySplit(splits)
            syncWithCloud()
        }
    }

    fun logCompletedWorkout(
        routineId: String?,
        routineTitle: String,
        durationSeconds: Long,
        totalVolumeKg: Double,
        totalSets: Int,
        summary: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val log = WorkoutLogEntity(
                id = "log_" + System.currentTimeMillis(),
                routineId = routineId,
                routineTitle = routineTitle,
                dateEpochDay = LocalDate.now().toEpochDay(),
                durationSeconds = durationSeconds,
                totalVolumeKg = totalVolumeKg,
                totalSets = totalSets,
                completedExercisesSummary = summary
            )
            repository.saveWorkoutLog(log)
            // Clear autosaved draft upon successful workout completion
            repository.clearDraft()
            syncWithCloud()
        }
    }

    fun autoSaveDraft(
        routineId: String?,
        routineTitle: String,
        durationSeconds: Long,
        draftJson: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                val draft = ActiveWorkoutDraftEntity(
                    id = "active_draft",
                    routineId = routineId,
                    routineTitle = routineTitle,
                    durationSeconds = durationSeconds,
                    draftJson = draftJson,
                    lastSavedTimestamp = System.currentTimeMillis()
                )
                repository.saveDraft(draft)
            }.onFailure { e ->
                Log.w("IronLogAutosave", "Autosave failed safely without crash: ${e.message}")
            }
        }
    }

    fun clearActiveDraft() {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                repository.clearDraft()
            }
        }
    }

    fun addCustomExercise(name: String, muscle: String, equipment: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addCustomExercise(name, muscle, equipment)
        }
    }

    fun addWeightLog(weight: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addMeasurement("Weight", weight, "kg", LocalDate.now().toEpochDay())
        }
    }
}

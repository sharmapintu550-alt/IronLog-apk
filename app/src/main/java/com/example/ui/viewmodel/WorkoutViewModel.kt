package com.example.ui.viewmodel

import android.app.Activity
import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import android.content.Context
import android.net.Uri
import com.example.auth.AuthManager
import com.example.auth.AuthUserState
import com.example.data.AppDatabase
import com.example.data.backup.BackupManager
import com.example.data.backup.BackupSummary
import com.example.data.backup.ParsedBackupData
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
import java.text.SimpleDateFormat
import java.time.DayOfWeek
import java.time.LocalDate
import java.util.Date
import java.util.Locale
import java.util.UUID

class WorkoutViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = WorkoutRepository(AppDatabase.getDatabase(application))
    private val authManager = AuthManager.getInstance(application)
    private val syncManager = CloudSyncManager.getInstance(application)

    init {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deletePresetRoutines()
            repository.purgeSeedLogs()
        }
    }

    val authState: StateFlow<AuthUserState> = authManager.authState
    val syncStatus: StateFlow<SyncStatus> = syncManager.syncStatus
    val hasCompletedOnboarding: StateFlow<Boolean> = authState.map { it.hasCompletedOnboarding }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), authManager.hasCompletedOnboarding)

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val userProfile: StateFlow<UserProfileEntity?> = authState.flatMapLatest { auth ->
        val uid = auth.user?.uid
        if (!uid.isNullOrBlank()) {
            repository.getUserProfile(uid)
        } else {
            repository.getAnyCompletedProfile()
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val isProfileComplete: StateFlow<Boolean> = userProfile.map { profile ->
        profile?.isProfileComplete == true
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    private val _showProfileSetup = MutableStateFlow(false)
    val showProfileSetup: StateFlow<Boolean> = _showProfileSetup.asStateFlow()

    fun openProfileSetup() {
        _showProfileSetup.value = true
    }

    fun dismissProfileSetup() {
        _showProfileSetup.value = false
    }

    private fun getEffectiveUserId(): String {
        val user = authState.value.user
        val isGuest = authState.value.isGuest
        return if (!isGuest && user != null && user.uid.isNotBlank()) user.uid else "guest"
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val routines: StateFlow<List<RoutineEntity>> = authState.flatMapLatest { auth ->
        val effectiveUid = if (!auth.isGuest && auth.user != null && auth.user.uid.isNotBlank()) auth.user.uid else "guest"
        repository.getRoutinesForUser(effectiveUid)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val weeklySplits: StateFlow<List<WeeklySplitEntity>> = authState.flatMapLatest { auth ->
        val effectiveUid = if (!auth.isGuest && auth.user != null && auth.user.uid.isNotBlank()) auth.user.uid else "guest"
        repository.getWeeklySplitsForUser(effectiveUid).map { splits ->
            if (splits.isEmpty()) {
                (1..7).map { day ->
                    WeeklySplitEntity(
                        dayOfWeek = day,
                        routineId = null,
                        routineTitle = null,
                        reminderTime = "06:30 PM",
                        isRestDay = (day == 3 || day == 6 || day == 7),
                        userId = effectiveUid
                    )
                }
            } else {
                splits
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

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

    val activeWorkoutState: StateFlow<ActiveWorkoutStateEntity?> = repository.activeWorkoutState
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Active Workout Live Progress State
    private val _isWorkoutActive = MutableStateFlow(false)
    val isWorkoutActive: StateFlow<Boolean> = _isWorkoutActive.asStateFlow()

    private val _isWorkoutMinimized = MutableStateFlow(false)
    val isWorkoutMinimized: StateFlow<Boolean> = _isWorkoutMinimized.asStateFlow()

    private val _activeRoutine = MutableStateFlow<RoutineEntity?>(null)
    val activeRoutine: StateFlow<RoutineEntity?> = _activeRoutine.asStateFlow()

    private val _activeWorkoutTitle = MutableStateFlow("Quick Workout")
    val activeWorkoutTitle: StateFlow<String> = _activeWorkoutTitle.asStateFlow()

    private val _activeWorkoutDurationSeconds = MutableStateFlow(0L)
    val activeWorkoutDurationSeconds: StateFlow<Long> = _activeWorkoutDurationSeconds.asStateFlow()

    private val _activeWorkoutCompletedSets = MutableStateFlow(0)
    val activeWorkoutCompletedSets: StateFlow<Int> = _activeWorkoutCompletedSets.asStateFlow()

    private val _activeWorkoutTotalSets = MutableStateFlow(0)
    val activeWorkoutTotalSets: StateFlow<Int> = _activeWorkoutTotalSets.asStateFlow()

    fun updateActiveWorkoutMetrics(durationSeconds: Long, completedSets: Int, totalSets: Int) {
        _activeWorkoutDurationSeconds.value = durationSeconds
        _activeWorkoutCompletedSets.value = completedSets
        _activeWorkoutTotalSets.value = totalSets
    }

    fun minimizeWorkout() {
        _isWorkoutMinimized.value = true
    }

    fun restoreWorkout() {
        _isWorkoutMinimized.value = false
    }

    fun endWorkout(context: Context? = null) {
        _isWorkoutActive.value = false
        _isWorkoutMinimized.value = false
        _activeRoutine.value = null
        _activeWorkoutDurationSeconds.value = 0L
        clearActiveWorkoutState(context)
    }

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
                    val result = syncManager.syncAll(currentUid)
                    if (result.isFailure) {
                        val ex = result.exceptionOrNull()
                        Log.e("AuthSyncDebug", "WorkoutViewModel.syncWithCloud failed: ${ex?.message}", ex)
                    } else {
                        Log.i("AuthSyncDebug", "WorkoutViewModel.syncWithCloud succeeded for UID: $currentUid")
                    }
                } else {
                    Log.d("IronLogSync", "Local guest mode: cloud sync idle.")
                    Log.d("AuthSyncDebug", "WorkoutViewModel.syncWithCloud idle: local guest mode (no user ID)")
                }
            }.onFailure { error ->
                Log.w("IronLogSync", "Cloud sync guard safely caught error: ${error.localizedMessage}")
                Log.e("AuthSyncDebug", "WorkoutViewModel.syncWithCloud uncaught exception [${error.javaClass.simpleName}]: ${error.message}", error)
            }
        }
    }

    fun signInWithGoogle(activity: Activity, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            Log.i("AuthSyncDebug", "WorkoutViewModel: initiating signInWithGoogle...")
            val result = authManager.signInWithGoogle(activity)
            if (result.isSuccess) {
                val user = result.getOrNull()
                Log.i("AuthSyncDebug", "WorkoutViewModel: signInWithGoogle succeeded for ${user?.uid}, checking profile & syncing.")
                if (user != null) {
                    val syncedProfile = syncManager.syncUserProfile(user.uid)
                    if (syncedProfile == null || !syncedProfile.isProfileComplete) {
                        _showProfileSetup.value = true
                    }
                }
                syncWithCloud()
                onSuccess()
            } else {
                val ex = result.exceptionOrNull()
                if (ex !is androidx.credentials.exceptions.GetCredentialCancellationException) {
                    val errorMsg = authState.value.errorMessage ?: ex?.message ?: "Google Sign-In failed"
                    Log.e("AuthSyncDebug", "WorkoutViewModel: signInWithGoogle failed: $errorMsg", ex)
                } else {
                    Log.d("AuthSyncDebug", "WorkoutViewModel: Google Sign-In prompt dismissed cleanly by user")
                }
            }
        }
    }

    fun setHasCompletedOnboarding(completed: Boolean = true) {
        authManager.setHasCompletedOnboarding(completed)
    }

    fun saveUserProfile(
        displayName: String,
        gender: String,
        age: Int,
        height: Double,
        heightUnit: String,
        bodyweight: Double,
        bodyweightUnit: String,
        preferredWeightUnit: String,
        primaryGoal: String,
        experienceLevel: String,
        onComplete: () -> Unit = {}
    ) {
        val uid = authManager.currentUser?.uid ?: "user_default"
        val email = authManager.currentUser?.email ?: ""
        val profile = UserProfileEntity(
            userId = uid,
            email = email,
            displayName = displayName,
            gender = gender,
            age = age,
            height = height,
            heightUnit = heightUnit,
            bodyweight = bodyweight,
            bodyweightUnit = bodyweightUnit,
            preferredWeightUnit = preferredWeightUnit,
            primaryGoal = primaryGoal,
            experienceLevel = experienceLevel,
            isProfileComplete = true,
            updatedAt = System.currentTimeMillis()
        )
        viewModelScope.launch(Dispatchers.IO) {
            syncManager.saveAndSyncProfile(profile)
            _showProfileSetup.value = false
            withContext(Dispatchers.Main) {
                onComplete()
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
            val uid = getEffectiveUserId()
            repository.duplicateRoutine(routine, userId = uid)
            syncWithCloud()
        }
    }

    fun deleteRoutine(routineId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteRoutine(routineId)
            val uid = getEffectiveUserId()
            if (uid != "guest") {
                syncManager.deleteRoutineFromCloud(uid, routineId)
            }
            syncWithCloud()
        }
    }

    fun createRoutine(title: String, desc: String, exercisesSummary: String, exercisesJson: String = "") {
        viewModelScope.launch(Dispatchers.IO) {
            val uid = getEffectiveUserId()
            val routine = repository.insertRoutine(title, desc, exercisesSummary, userId = uid, exercisesJson = exercisesJson)
            if (uid != "guest") {
                syncManager.pushRoutine(uid, routine)
            }
            syncWithCloud()
        }
    }

    fun updateRoutine(
        id: String,
        title: String,
        desc: String,
        exercisesSummary: String,
        exercisesJson: String = "",
        onComplete: () -> Unit = {}
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val uid = getEffectiveUserId()
            val existing = repository.getRoutineById(id)
            val updatedRoutine = RoutineEntity(
                id = id,
                title = title,
                description = desc,
                exercisesSummary = exercisesSummary,
                exercisesJson = exercisesJson,
                sortOrder = existing?.sortOrder ?: 0,
                createdAt = existing?.createdAt ?: System.currentTimeMillis(),
                userId = uid
            )
            repository.updateRoutine(updatedRoutine)
            if (uid != "guest") {
                syncManager.pushRoutine(uid, updatedRoutine)
            }
            syncWithCloud()
            withContext(Dispatchers.Main) {
                onComplete()
            }
        }
    }

    fun saveImportedRoutine(
        routine: RoutineEntity,
        customExercises: List<ExerciseEntity> = emptyList(),
        onComplete: (Boolean) -> Unit = {}
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                customExercises.forEach { customEx ->
                    repository.insertCustomExercise(customEx)
                }
                val uid = getEffectiveUserId()
                val scopedRoutine = routine.copy(userId = uid)
                repository.insertRoutineEntity(scopedRoutine)
                if (uid != "guest") {
                    syncManager.pushRoutine(uid, scopedRoutine)
                }
                syncWithCloud()
                withContext(Dispatchers.Main) {
                    onComplete(true)
                }
            } catch (e: Exception) {
                Log.e("WorkoutViewModel", "Error saving imported routine", e)
                withContext(Dispatchers.Main) {
                    onComplete(false)
                }
            }
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
            val uid = getEffectiveUserId()
            val updatedRoutine = routine.copy(exercisesSummary = updatedSummary, userId = uid)
            repository.updateRoutine(updatedRoutine)
            if (uid != "guest") {
                syncManager.pushRoutine(uid, updatedRoutine)
            }
            syncWithCloud()
        }
    }

    fun saveRoutineOrder(orderedRoutines: List<Pair<String, Int>>) {
        viewModelScope.launch(Dispatchers.IO) {
            val uid = getEffectiveUserId()
            repository.saveRoutineOrder(orderedRoutines)
            if (uid != "guest") {
                syncManager.pushRoutineOrders(uid, orderedRoutines)
            }
            syncWithCloud()
        }
    }

    fun updateRoutinesOrder(reordered: List<RoutineEntity>) {
        val pairs = reordered.mapIndexed { index, routine -> routine.id to index }
        saveRoutineOrder(pairs)
    }

    fun saveWeeklySplit(splits: List<WeeklySplitEntity>) {
        val uid = getEffectiveUserId()
        val userScopedSplits = splits.map { it.copy(userId = uid, updatedAt = System.currentTimeMillis()) }
        viewModelScope.launch(Dispatchers.IO) {
            repository.saveWeeklySplit(userScopedSplits)
            if (uid != "guest") {
                syncManager.pushAllWeeklySchedule(uid, userScopedSplits)
            }
            syncWithCloud()
        }
    }

    fun updateDaySplit(split: WeeklySplitEntity) {
        val uid = getEffectiveUserId()
        val userScopedSplit = split.copy(userId = uid, updatedAt = System.currentTimeMillis())
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateDaySplit(userScopedSplit)
            if (uid != "guest") {
                syncManager.pushWeeklyScheduleDay(uid, userScopedSplit)
            }
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
            // Clear autosaved draft and persistent active state upon successful workout completion
            repository.clearDraft()
            repository.clearActiveWorkoutState()
            val uid = authManager.currentUser?.uid
            if (!uid.isNullOrBlank()) {
                syncManager.pushWorkoutLog(uid, log)
            }
            syncWithCloud()
        }
    }

    fun deleteWorkoutLog(log: WorkoutLogEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteWorkoutLog(log.id)
            val uid = authManager.currentUser?.uid
            if (!uid.isNullOrBlank()) {
                syncManager.deleteWorkoutLog(uid, log.id)
            }
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
                repository.clearActiveWorkoutState()
            }
        }
    }

    fun saveActiveWorkoutState(state: ActiveWorkoutStateEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                repository.saveActiveWorkoutState(state)
            }.onFailure { e ->
                Log.w("IronLogAutosave", "Failed to save active workout state: ${e.message}")
            }
        }
    }

    fun clearActiveWorkoutState(context: Context? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                repository.clearActiveWorkoutState()
                repository.clearDraft()
            }
            if (context != null) {
                runCatching {
                    com.example.service.WorkoutTrackingService.stop(context)
                }.onFailure { Log.e("ActiveWorkout", "Failed to stop service: ${it.message}", it) }
            }
        }
    }

    fun addCustomExercise(name: String, muscle: String, equipment: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val exercise = repository.addCustomExercise(name, muscle, equipment)
            val uid = authManager.currentUser?.uid
            if (!uid.isNullOrBlank()) {
                syncManager.pushCustomExercise(uid, exercise)
            }
            syncWithCloud()
        }
    }

    fun saveCustomExercise(exercise: ExerciseEntity, onComplete: () -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                val cleanName = exercise.name.trim()
                val existing = repository.getExerciseByName(cleanName)
                val uid = authManager.currentUser?.uid
                val exerciseToSave = if (existing != null) {
                    existing
                } else {
                    if (!uid.isNullOrBlank() && exercise.userId.isNullOrBlank()) {
                        exercise.copy(name = cleanName, userId = uid)
                    } else {
                        exercise.copy(name = cleanName)
                    }
                }
                repository.insertCustomExercise(exerciseToSave)
                if (!uid.isNullOrBlank() && uid != "guest") {
                    syncManager.pushCustomExercise(uid, exerciseToSave)
                }
                syncWithCloud()
                withContext(Dispatchers.Main) {
                    onComplete()
                }
            }.onFailure { err ->
                Log.e("WorkoutViewModel", "Error in saveCustomExercise: ${err.message}", err)
                withContext(Dispatchers.Main) {
                    onComplete()
                }
            }
        }
    }

    fun createAndAddCustomExercise(
        name: String,
        targetMuscle: String,
        equipment: String,
        barbellWeightKg: Double = 0.0,
        notes: String = "",
        onComplete: (ExerciseEntity) -> Unit = {}
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                val cleanName = name.trim()
                val existing = repository.getExerciseByName(cleanName)
                if (existing != null) {
                    val uid = authManager.currentUser?.uid
                    if (!uid.isNullOrBlank() && uid != "guest" && existing.isCustom) {
                        syncManager.pushCustomExercise(uid, existing)
                    }
                    withContext(Dispatchers.Main) {
                        onComplete(existing)
                    }
                    return@launch
                }

                val uid = authManager.currentUser?.uid ?: "guest"
                val newExercise = ExerciseEntity(
                    id = UUID.randomUUID().toString(),
                    name = cleanName,
                    muscleGroup = targetMuscle,
                    equipment = equipment,
                    category = when (targetMuscle.trim()) {
                        "Chest" -> "Chest"
                        "Back" -> "Back"
                        "Shoulders", "Traps" -> "Shoulders"
                        "Legs" -> "Legs"
                        "Biceps", "Triceps" -> "Arms"
                        "Abs & Core" -> "Core"
                        else -> targetMuscle
                    },
                    equipmentType = equipment,
                    notes = notes.trim(),
                    isCustom = true,
                    primaryMuscle = targetMuscle,
                    secondaryMuscles = emptyList(),
                    equipmentCategory = equipment,
                    trackingType = if (equipment.equals("Bodyweight", true)) "BODYWEIGHT_REP" else "WEIGHT_AND_REPS",
                    defaultRestSeconds = 90,
                    barbellWeightKg = barbellWeightKg,
                    customBarWeight = barbellWeightKg.toFloat(),
                    userId = uid
                )

                repository.insertCustomExercise(newExercise)
                if (uid != "guest" && uid.isNotBlank()) {
                    syncManager.pushCustomExercise(uid, newExercise)
                }
                syncWithCloud()

                withContext(Dispatchers.Main) {
                    onComplete(newExercise)
                }
            }.onFailure { err ->
                Log.e("WorkoutViewModel", "Error creating custom exercise: ${err.message}", err)
            }
        }
    }

    fun addWeightLog(weight: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addMeasurement("Weight", weight, "kg", LocalDate.now().toEpochDay())
        }
    }

    fun updateExerciseNote(exerciseId: String, exerciseName: String, note: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateExerciseNote(exerciseId, exerciseName, note)
        }
    }

    fun updateExerciseBarbellWeight(exerciseId: String, exerciseName: String, weight: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateExerciseBarbellWeight(exerciseId, exerciseName, weight)
        }
    }

    fun updateExerciseSetupMedia(exerciseId: String, exerciseName: String, mediaUri: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateExerciseSetupMedia(exerciseId, exerciseName, mediaUri)
        }
    }

    fun updateExerciseCalibration(exerciseId: String, exerciseName: String, calibration: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateExerciseCalibration(exerciseId, exerciseName, calibration)
        }
    }

    // Local Data Backup & Restore Operations
    private val _isBackupOperating = MutableStateFlow(false)
    val isBackupOperating: StateFlow<Boolean> = _isBackupOperating.asStateFlow()

    private val _pendingRestoreData = MutableStateFlow<ParsedBackupData?>(null)
    val pendingRestoreData: StateFlow<ParsedBackupData?> = _pendingRestoreData.asStateFlow()

    fun exportJsonToUri(uri: Uri, context: Context, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            _isBackupOperating.value = true
            try {
                val db = AppDatabase.getDatabase(getApplication())
                val jsonStr = BackupManager.generateJsonBackup(db)
                val writeRes = BackupManager.writeTextToUri(context, uri, jsonStr)
                if (writeRes.isSuccess) {
                    withContext(Dispatchers.Main) {
                        onResult(true, "JSON backup exported successfully!")
                    }
                } else {
                    val msg = writeRes.exceptionOrNull()?.message ?: "Export failed"
                    withContext(Dispatchers.Main) {
                        onResult(false, msg)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onResult(false, e.localizedMessage ?: "Export failed")
                }
            } finally {
                _isBackupOperating.value = false
            }
        }
    }

    fun exportCsvToUri(uri: Uri, context: Context, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            _isBackupOperating.value = true
            try {
                val db = AppDatabase.getDatabase(getApplication())
                val csvStr = BackupManager.generateCsvHistory(db)
                val writeRes = BackupManager.writeTextToUri(context, uri, csvStr)
                if (writeRes.isSuccess) {
                    withContext(Dispatchers.Main) {
                        onResult(true, "Workout history CSV exported successfully!")
                    }
                } else {
                    val msg = writeRes.exceptionOrNull()?.message ?: "Export failed"
                    withContext(Dispatchers.Main) {
                        onResult(false, msg)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onResult(false, e.localizedMessage ?: "Export failed")
                }
            } finally {
                _isBackupOperating.value = false
            }
        }
    }

    fun shareJsonBackup(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            _isBackupOperating.value = true
            try {
                val db = AppDatabase.getDatabase(getApplication())
                val jsonStr = BackupManager.generateJsonBackup(db)
                val dateStr = SimpleDateFormat("yyyyMMdd", Locale.US).format(Date())
                val filename = "IronLog_Backup_${dateStr}.json"
                withContext(Dispatchers.Main) {
                    BackupManager.shareTextFile(context, jsonStr, filename, "application/json")
                }
            } catch (e: Exception) {
                Log.e("WorkoutViewModel", "shareJsonBackup failed: ${e.message}")
            } finally {
                _isBackupOperating.value = false
            }
        }
    }

    fun shareCsvHistory(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            _isBackupOperating.value = true
            try {
                val db = AppDatabase.getDatabase(getApplication())
                val csvStr = BackupManager.generateCsvHistory(db)
                val dateStr = SimpleDateFormat("yyyyMMdd", Locale.US).format(Date())
                val filename = "IronLog_Workout_History_${dateStr}.csv"
                withContext(Dispatchers.Main) {
                    BackupManager.shareTextFile(context, csvStr, filename, "text/csv")
                }
            } catch (e: Exception) {
                Log.e("WorkoutViewModel", "shareCsvHistory failed: ${e.message}")
            } finally {
                _isBackupOperating.value = false
            }
        }
    }

    fun inspectBackupFile(uri: Uri, context: Context, onReady: (BackupSummary) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            _isBackupOperating.value = true
            try {
                val readRes = BackupManager.readTextFromUri(context, uri)
                if (readRes.isFailure) {
                    val msg = readRes.exceptionOrNull()?.message ?: "Failed to read file."
                    withContext(Dispatchers.Main) { onError(msg) }
                    return@launch
                }
                val jsonStr = readRes.getOrThrow()
                val parseRes = BackupManager.parseBackupJson(jsonStr)
                if (parseRes.isFailure) {
                    val msg = parseRes.exceptionOrNull()?.message ?: "Invalid IronLog backup format."
                    withContext(Dispatchers.Main) { onError(msg) }
                    return@launch
                }
                val parsedData = parseRes.getOrThrow()
                _pendingRestoreData.value = parsedData
                withContext(Dispatchers.Main) {
                    onReady(parsedData.summary)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onError(e.localizedMessage ?: "Failed to inspect backup file")
                }
            } finally {
                _isBackupOperating.value = false
            }
        }
    }

    fun confirmRestore(onSuccess: (BackupSummary) -> Unit, onError: (String) -> Unit) {
        val data = _pendingRestoreData.value
        if (data == null) {
            onError("No backup file selected")
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            _isBackupOperating.value = true
            try {
                val db = AppDatabase.getDatabase(getApplication())
                val restoreRes = BackupManager.restoreIntoDatabase(db, data)
                if (restoreRes.isSuccess) {
                    val summary = restoreRes.getOrThrow()
                    _pendingRestoreData.value = null
                    // Trigger cloud sync to propagate restored data
                    syncWithCloud()
                    withContext(Dispatchers.Main) {
                        onSuccess(summary)
                    }
                } else {
                    val msg = restoreRes.exceptionOrNull()?.message ?: "Restore failed"
                    withContext(Dispatchers.Main) { onError(msg) }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onError(e.localizedMessage ?: "Restore failed")
                }
            } finally {
                _isBackupOperating.value = false
            }
        }
    }

    fun dismissRestorePreview() {
        _pendingRestoreData.value = null
    }
}

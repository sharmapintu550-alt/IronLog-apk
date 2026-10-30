package com.example.data.repository

import com.example.data.AppDatabase
import com.example.data.entity.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.util.UUID

class WorkoutRepository(private val db: AppDatabase) {

    // Routines
    val allRoutines: Flow<List<RoutineEntity>> = db.routineDao().getAllRoutines().flowOn(Dispatchers.IO)

    fun getRoutinesForUser(userId: String): Flow<List<RoutineEntity>> =
        db.routineDao().getRoutinesForUser(userId).flowOn(Dispatchers.IO)

    suspend fun getRoutineById(id: String): RoutineEntity? = withContext(Dispatchers.IO) {
        db.routineDao().getRoutineById(id)
    }

    suspend fun insertRoutine(
        title: String,
        description: String,
        exercisesSummary: String,
        userId: String = "guest",
        exercisesJson: String = ""
    ): RoutineEntity = withContext(Dispatchers.IO) {
        val routine = RoutineEntity(
            id = "routine_" + UUID.randomUUID().toString().take(8),
            userId = userId,
            title = title,
            description = description,
            exercisesSummary = exercisesSummary,
            exercisesJson = exercisesJson,
            sortOrder = 99
        )
        db.routineDao().insert(routine)
        routine
    }

    suspend fun insertRoutineEntity(routine: RoutineEntity) = withContext(Dispatchers.IO) {
        db.routineDao().insert(routine)
    }

    suspend fun updateRoutine(routine: RoutineEntity) = withContext(Dispatchers.IO) {
        db.routineDao().insert(routine)
    }

    suspend fun duplicateRoutine(original: RoutineEntity, userId: String = original.userId) = withContext(Dispatchers.IO) {
        val newRoutine = RoutineEntity(
            id = "routine_" + UUID.randomUUID().toString().take(8),
            userId = userId,
            title = "${original.title} (Copy)",
            description = original.description,
            exercisesSummary = original.exercisesSummary,
            sortOrder = original.sortOrder + 1,
            createdAt = System.currentTimeMillis()
        )
        db.routineDao().insert(newRoutine)
    }

    suspend fun deleteRoutine(id: String) = withContext(Dispatchers.IO) {
        db.routineDao().deleteById(id)
    }

    suspend fun deletePresetRoutines() = withContext(Dispatchers.IO) {
        db.routineDao().deletePresetRoutines()
    }

    suspend fun saveRoutineOrder(orderedRoutines: List<Pair<String, Int>>) = withContext(Dispatchers.IO) {
        db.routineDao().updateAllRoutineOrders(orderedRoutines)
    }

    suspend fun updateRoutinesOrder(routines: List<RoutineEntity>) = withContext(Dispatchers.IO) {
        val pairs = routines.mapIndexed { index, routine -> routine.id to index }
        db.routineDao().updateAllRoutineOrders(pairs)
    }

    // Weekly Split
    val allWeeklySplits: Flow<List<WeeklySplitEntity>> = db.weeklySplitDao().getAllSplitDays().flowOn(Dispatchers.IO)

    fun getWeeklySplitsForUser(userId: String): Flow<List<WeeklySplitEntity>> =
        db.weeklySplitDao().getSplitsForUser(userId).flowOn(Dispatchers.IO)

    fun getSplitForDay(dayOfWeek: Int): Flow<WeeklySplitEntity?> =
        db.weeklySplitDao().getSplitForDay(dayOfWeek).flowOn(Dispatchers.IO)

    fun getSplitForDayAndUser(dayOfWeek: Int, userId: String): Flow<WeeklySplitEntity?> =
        db.weeklySplitDao().getSplitForDayAndUser(dayOfWeek, userId).flowOn(Dispatchers.IO)

    suspend fun saveWeeklySplit(splits: List<WeeklySplitEntity>) = withContext(Dispatchers.IO) {
        db.weeklySplitDao().insertAll(splits)
    }

    suspend fun saveWeeklySplitForUser(splits: List<WeeklySplitEntity>, userId: String = "guest") = withContext(Dispatchers.IO) {
        val userSplits = splits.map { it.copy(userId = userId) }
        db.weeklySplitDao().insertAll(userSplits)
    }

    suspend fun updateDaySplit(split: WeeklySplitEntity) = withContext(Dispatchers.IO) {
        db.weeklySplitDao().insertOrUpdate(split)
    }

    // Workout Logs
    val allWorkoutLogs: Flow<List<WorkoutLogEntity>> = db.workoutLogDao().getAllLogs().flowOn(Dispatchers.IO)
    val completedDates: Flow<List<Long>> = db.workoutLogDao().getCompletedDates().flowOn(Dispatchers.IO)

    fun getWorkoutLogsForExercise(exerciseName: String): Flow<List<WorkoutLogEntity>> =
        db.workoutLogDao().getLogsForExercise(exerciseName).flowOn(Dispatchers.IO)

    suspend fun saveWorkoutLog(log: WorkoutLogEntity) = withContext(Dispatchers.IO) {
        db.workoutLogDao().insertLog(log)
    }

    suspend fun deleteWorkoutLog(logId: String) = withContext(Dispatchers.IO) {
        db.workoutLogDao().deleteById(logId)
    }

    suspend fun purgeSeedLogs() = withContext(Dispatchers.IO) {
        db.workoutLogDao().purgeSeedLogs()
    }

    // Exercises
    val allExercises: Flow<List<ExerciseEntity>> = db.exerciseDao().getAllExercises().flowOn(Dispatchers.IO)

    suspend fun addCustomExercise(name: String, muscleGroup: String, equipment: String): ExerciseEntity = withContext(Dispatchers.IO) {
        val exercise = ExerciseEntity(
            id = "ex_custom_" + UUID.randomUUID().toString().take(8),
            name = name,
            muscleGroup = muscleGroup,
            equipment = equipment,
            isCustom = true
        )
        db.exerciseDao().insert(exercise)
        exercise
    }

    suspend fun insertCustomExercise(exercise: ExerciseEntity) = withContext(Dispatchers.IO) {
        db.exerciseDao().insertCustomExercise(exercise)
    }

    suspend fun getExerciseByName(name: String): ExerciseEntity? = withContext(Dispatchers.IO) {
        db.exerciseDao().getExerciseByName(name)
    }

    suspend fun updateExerciseNote(exerciseId: String, exerciseName: String, note: String) = withContext(Dispatchers.IO) {
        db.exerciseDao().updateExerciseNote(exerciseId, exerciseName, note)
    }

    suspend fun updateExerciseBarbellWeight(exerciseId: String, exerciseName: String, weight: Double) = withContext(Dispatchers.IO) {
        db.exerciseDao().updateExerciseBarbellWeight(exerciseId, exerciseName, weight)
    }

    suspend fun updateExerciseSetupMedia(exerciseId: String, exerciseName: String, mediaUri: String?) = withContext(Dispatchers.IO) {
        db.exerciseDao().updateExerciseSetupMedia(exerciseId, exerciseName, mediaUri)
    }

    suspend fun updateExerciseCalibration(exerciseId: String, exerciseName: String, calibration: String?) = withContext(Dispatchers.IO) {
        db.exerciseDao().updateExerciseCalibration(exerciseId, exerciseName, calibration)
    }

    // Measurements
    val allMeasurements: Flow<List<BodyMeasurementEntity>> = db.bodyMeasurementDao().getAllMeasurements().flowOn(Dispatchers.IO)

    suspend fun addMeasurement(metricType: String, value: Double, unit: String, epochDay: Long) = withContext(Dispatchers.IO) {
        val m = BodyMeasurementEntity(
            id = "m_" + UUID.randomUUID().toString().take(8),
            metricType = metricType,
            value = value,
            unit = unit,
            dateEpochDay = epochDay
        )
        db.bodyMeasurementDao().insert(m)
    }

    // Active Workout Autosave Draft
    val activeDraft: Flow<ActiveWorkoutDraftEntity?> = db.activeWorkoutDraftDao().getDraft().flowOn(Dispatchers.IO)

    suspend fun saveDraft(draft: ActiveWorkoutDraftEntity) = withContext(Dispatchers.IO) {
        runCatching {
            db.activeWorkoutDraftDao().saveDraft(draft)
        }.onFailure { android.util.Log.e("ActiveWorkout", "DB Error in saveDraft: ${it.message}", it) }
    }

    suspend fun clearDraft() = withContext(Dispatchers.IO) {
        runCatching {
            db.activeWorkoutDraftDao().clearDraft()
        }.onFailure { android.util.Log.e("ActiveWorkout", "DB Error in clearDraft: ${it.message}", it) }
    }

    // Active Workout Persistent State (Process Death / Swipe-Away Immunity)
    val activeWorkoutState: Flow<ActiveWorkoutStateEntity?> =
        db.activeWorkoutStateDao().getActiveWorkoutState().flowOn(Dispatchers.IO)

    suspend fun getActiveWorkoutStateOnce(): ActiveWorkoutStateEntity? = withContext(Dispatchers.IO) {
        runCatching {
            db.activeWorkoutStateDao().getActiveWorkoutStateOnce()
        }.onFailure { android.util.Log.e("ActiveWorkout", "DB Error in getActiveWorkoutStateOnce: ${it.message}", it) }.getOrNull()
    }

    suspend fun saveActiveWorkoutState(state: ActiveWorkoutStateEntity) = withContext(Dispatchers.IO) {
        runCatching {
            db.activeWorkoutStateDao().saveActiveWorkoutState(state)
        }.onFailure { android.util.Log.e("ActiveWorkout", "DB Error in saveActiveWorkoutState: ${it.message}", it) }
    }

    suspend fun clearActiveWorkoutState() = withContext(Dispatchers.IO) {
        runCatching {
            db.activeWorkoutStateDao().clearActiveWorkoutState()
        }.onFailure { android.util.Log.e("ActiveWorkout", "DB Error in clearActiveWorkoutState: ${it.message}", it) }
    }

    // User Profile
    fun getUserProfile(userId: String): Flow<UserProfileEntity?> =
        db.userProfileDao().getUserProfile(userId).flowOn(Dispatchers.IO)

    fun getAnyCompletedProfile(): Flow<UserProfileEntity?> =
        db.userProfileDao().getAnyCompletedProfile().flowOn(Dispatchers.IO)

    suspend fun saveUserProfile(profile: UserProfileEntity) = withContext(Dispatchers.IO) {
        db.userProfileDao().saveUserProfile(profile)
    }
}

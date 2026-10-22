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

    suspend fun insertRoutine(title: String, description: String, exercisesSummary: String) = withContext(Dispatchers.IO) {
        val routine = RoutineEntity(
            id = "routine_" + UUID.randomUUID().toString().take(8),
            title = title,
            description = description,
            exercisesSummary = exercisesSummary,
            orderIndex = 99
        )
        db.routineDao().insert(routine)
    }

    suspend fun updateRoutine(routine: RoutineEntity) = withContext(Dispatchers.IO) {
        db.routineDao().insert(routine)
    }

    suspend fun duplicateRoutine(original: RoutineEntity) = withContext(Dispatchers.IO) {
        val newRoutine = RoutineEntity(
            id = "routine_" + UUID.randomUUID().toString().take(8),
            title = "${original.title} (Copy)",
            description = original.description,
            exercisesSummary = original.exercisesSummary,
            orderIndex = original.orderIndex + 1,
            createdAt = System.currentTimeMillis()
        )
        db.routineDao().insert(newRoutine)
    }

    suspend fun deleteRoutine(id: String) = withContext(Dispatchers.IO) {
        db.routineDao().deleteById(id)
    }

    suspend fun updateRoutinesOrder(routines: List<RoutineEntity>) = withContext(Dispatchers.IO) {
        routines.forEachIndexed { index, routine ->
            db.routineDao().updateOrder(routine.id, index)
        }
    }

    // Weekly Split
    val allWeeklySplits: Flow<List<WeeklySplitEntity>> = db.weeklySplitDao().getAllSplitDays().flowOn(Dispatchers.IO)

    fun getSplitForDay(dayOfWeek: Int): Flow<WeeklySplitEntity?> =
        db.weeklySplitDao().getSplitForDay(dayOfWeek).flowOn(Dispatchers.IO)

    suspend fun saveWeeklySplit(splits: List<WeeklySplitEntity>) = withContext(Dispatchers.IO) {
        db.weeklySplitDao().insertAll(splits)
    }

    suspend fun updateDaySplit(split: WeeklySplitEntity) = withContext(Dispatchers.IO) {
        db.weeklySplitDao().insertOrUpdate(split)
    }

    // Workout Logs
    val allWorkoutLogs: Flow<List<WorkoutLogEntity>> = db.workoutLogDao().getAllLogs().flowOn(Dispatchers.IO)
    val completedDates: Flow<List<Long>> = db.workoutLogDao().getCompletedDates().flowOn(Dispatchers.IO)

    suspend fun saveWorkoutLog(log: WorkoutLogEntity) = withContext(Dispatchers.IO) {
        db.workoutLogDao().insertLog(log)
    }

    // Exercises
    val allExercises: Flow<List<ExerciseEntity>> = db.exerciseDao().getAllExercises().flowOn(Dispatchers.IO)

    suspend fun addCustomExercise(name: String, muscleGroup: String, equipment: String) = withContext(Dispatchers.IO) {
        val exercise = ExerciseEntity(
            id = "ex_custom_" + UUID.randomUUID().toString().take(8),
            name = name,
            muscleGroup = muscleGroup,
            equipment = equipment,
            isCustom = true
        )
        db.exerciseDao().insert(exercise)
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
        db.activeWorkoutDraftDao().saveDraft(draft)
    }

    suspend fun clearDraft() = withContext(Dispatchers.IO) {
        db.activeWorkoutDraftDao().clearDraft()
    }
}

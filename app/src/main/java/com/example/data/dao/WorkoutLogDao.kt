package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.entity.WorkoutLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutLogDao {
    @Query("SELECT * FROM workout_logs ORDER BY dateEpochDay DESC, timestamp DESC")
    fun getAllLogs(): Flow<List<WorkoutLogEntity>>

    @Query("SELECT * FROM workout_logs WHERE dateEpochDay = :dateEpochDay")
    fun getLogsForDate(dateEpochDay: Long): Flow<List<WorkoutLogEntity>>

    @Query("SELECT DISTINCT dateEpochDay FROM workout_logs")
    fun getCompletedDates(): Flow<List<Long>>

    @Query("SELECT * FROM workout_logs WHERE completedExercisesSummary LIKE '%' || :exerciseName || '%' ORDER BY dateEpochDay DESC, timestamp DESC")
    fun getLogsForExercise(exerciseName: String): Flow<List<WorkoutLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: WorkoutLogEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(logs: List<WorkoutLogEntity>)

    @Delete
    suspend fun delete(log: WorkoutLogEntity)

    @Query("DELETE FROM workout_logs WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM workout_logs WHERE routineTitle IN ('Leg Day Power', 'Back & Triceps', 'Chest & Biceps', 'Shoulder & Core', 'Legs & Shoulders', 'Back & Tricep', 'Chest - Biceps', 'Shoulders & Arms', 'Full Body Blitz', 'Pull Day Heavy', 'Push Day Power') OR id LIKE 'seed_%' OR id LIKE 'mock_%' OR routineId IN ('routine_chest_biceps', 'routine_back_triceps', 'routine_legs', 'routine_shoulders_core')")
    suspend fun purgeSeedLogs()
}

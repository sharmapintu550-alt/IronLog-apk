package com.example.data.dao

import androidx.room.Dao
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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: WorkoutLogEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(logs: List<WorkoutLogEntity>)

    @Query("DELETE FROM workout_logs WHERE id = :id")
    suspend fun deleteById(id: String)
}

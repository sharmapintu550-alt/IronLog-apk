package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.entity.ActiveWorkoutStateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ActiveWorkoutStateDao {

    @Query("SELECT * FROM active_workout_state WHERE workoutId = 'active_session' LIMIT 1")
    fun getActiveWorkoutState(): Flow<ActiveWorkoutStateEntity?>

    @Query("SELECT * FROM active_workout_state WHERE workoutId = 'active_session' LIMIT 1")
    suspend fun getActiveWorkoutStateOnce(): ActiveWorkoutStateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveActiveWorkoutState(state: ActiveWorkoutStateEntity)

    @Query("DELETE FROM active_workout_state WHERE workoutId = 'active_session'")
    suspend fun clearActiveWorkoutState()
}

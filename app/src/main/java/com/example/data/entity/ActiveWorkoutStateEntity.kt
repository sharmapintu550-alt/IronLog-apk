package com.example.data.entity

import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.PrimaryKey

@Immutable
@Entity(tableName = "active_workout_state")
data class ActiveWorkoutStateEntity(
    @PrimaryKey val workoutId: String = "active_session",
    val routineId: String? = null,
    val routineName: String = "Quick Workout",
    val startTimestamp: Long = System.currentTimeMillis(),
    val lastUpdatedTimestamp: Long = System.currentTimeMillis(),
    val isPaused: Boolean = false,
    val pauseTimestamp: Long? = null,
    val totalPausedSeconds: Long = 0L,
    val totalPausedTimeMs: Long = 0L,
    val userId: String = "guest",
    val exercisesJson: String = ""
)

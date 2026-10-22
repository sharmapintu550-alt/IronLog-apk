package com.example.data.entity

import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.PrimaryKey

@Immutable
@Entity(tableName = "workout_logs")
data class WorkoutLogEntity(
    @PrimaryKey val id: String,
    val routineId: String?,
    val routineTitle: String,
    val dateEpochDay: Long,
    val durationSeconds: Long,
    val totalVolumeKg: Double,
    val totalSets: Int,
    val completedExercisesSummary: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

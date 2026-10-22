package com.example.data.entity

import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.PrimaryKey

@Immutable
@Entity(tableName = "active_workout_draft")
data class ActiveWorkoutDraftEntity(
    @PrimaryKey val id: String = "active_draft",
    val routineId: String?,
    val routineTitle: String,
    val durationSeconds: Long,
    val draftJson: String,
    val lastSavedTimestamp: Long = System.currentTimeMillis()
)

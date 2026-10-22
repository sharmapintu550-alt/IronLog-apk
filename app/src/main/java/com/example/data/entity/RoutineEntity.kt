package com.example.data.entity

import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.PrimaryKey

@Immutable
@Entity(tableName = "routines")
data class RoutineEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String = "",
    val exercisesSummary: String = "",
    val exercisesJson: String = "",
    val orderIndex: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

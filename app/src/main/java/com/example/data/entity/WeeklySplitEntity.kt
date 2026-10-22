package com.example.data.entity

import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.PrimaryKey

@Immutable
@Entity(tableName = "weekly_split")
data class WeeklySplitEntity(
    @PrimaryKey val dayOfWeek: Int, // 1 = Monday ... 7 = Sunday (java.time.DayOfWeek.value)
    val routineId: String?,         // Nullable for Rest Days
    val routineTitle: String?,
    val reminderTime: String = "06:30 PM",
    val isRestDay: Boolean = false
)

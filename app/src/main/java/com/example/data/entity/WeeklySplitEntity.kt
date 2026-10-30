package com.example.data.entity

import androidx.compose.runtime.Immutable
import androidx.room.ColumnInfo
import androidx.room.Entity

@Immutable
@Entity(
    tableName = "weekly_split",
    primaryKeys = ["dayOfWeek", "userId"]
)
data class WeeklySplitEntity(
    val dayOfWeek: Int, // 1 = Monday ... 7 = Sunday (java.time.DayOfWeek.value)
    val routineId: String?,         // Nullable for Rest Days
    val routineTitle: String?,
    val reminderTime: String = "06:30 PM",
    val isRestDay: Boolean = false,
    @ColumnInfo(name = "userId", defaultValue = "guest")
    val userId: String = "guest",
    val updatedAt: Long = System.currentTimeMillis()
) {
    val routineName: String?
        get() = routineTitle
}

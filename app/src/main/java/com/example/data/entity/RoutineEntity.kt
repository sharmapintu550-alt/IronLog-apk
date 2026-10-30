package com.example.data.entity

import androidx.compose.runtime.Immutable
import androidx.room.ColumnInfo
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
    @ColumnInfo(name = "sortOrder", defaultValue = "0")
    val sortOrder: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "userId", defaultValue = "guest")
    val userId: String = "guest"
) {
    val orderIndex: Int get() = sortOrder
}

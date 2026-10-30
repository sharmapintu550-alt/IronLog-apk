package com.example.data.entity

import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.PrimaryKey

@Immutable
@Entity(tableName = "user_profiles")
data class UserProfileEntity(
    @PrimaryKey val userId: String,
    val email: String = "",
    val displayName: String = "",
    val gender: String = "Male",
    val age: Int = 25,
    val height: Double = 175.0,
    val heightUnit: String = "cm",
    val bodyweight: Double = 75.0,
    val bodyweightUnit: String = "kg",
    val preferredWeightUnit: String = "kg",
    val primaryGoal: String = "Strength & Hypertrophy",
    val experienceLevel: String = "Intermediate (1-3 yrs)",
    val isProfileComplete: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)

package com.example.data.entity

import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import java.util.UUID

@Immutable
@Entity(tableName = "exercises")
@TypeConverters(Converters::class)
data class ExerciseEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val muscleGroup: String = "Other",
    val equipment: String = "Barbell",
    val prWeight: Double = 0.0,
    val prReps: Int = 0,
    val isCustom: Boolean = false,
    val category: String = muscleGroup,
    val equipmentType: String = equipment,
    val notes: String = "",
    val imageUrl: String? = null,
    val primaryMuscle: String = muscleGroup,
    val secondaryMuscles: List<String> = emptyList(),
    val equipmentCategory: String = equipment,
    val trackingType: String = "WEIGHT_AND_REPS",
    val defaultRestSeconds: Int = 90,
    val instructions: String = "",
    val barbellWeightKg: Double = 20.0,
    val customBarWeight: Float? = null,
    val userId: String? = null,
    val isDualPulley: Boolean = false,
    val setupMediaUri: String? = null,
    val machineCalibration: String? = null
) {
    val uuid: UUID
        @Ignore get() = try {
            UUID.fromString(id)
        } catch (e: Exception) {
            UUID.nameUUIDFromBytes(id.toByteArray(Charsets.UTF_8))
        }

    val lastNote: String
        @Ignore get() = notes

    val targetMuscle: String
        @Ignore get() = primaryMuscle.ifBlank { muscleGroup }

    @Ignore
    constructor(
        id: Any,
        name: String = "",
        category: String = "Other",
        equipmentType: String = "Barbell",
        notes: String = "",
        imageUrl: String? = null,
        primaryMuscle: String = category,
        secondaryMuscles: List<String> = emptyList(),
        equipmentCategory: String = equipmentType,
        trackingType: String = "WEIGHT_AND_REPS",
        defaultRestSeconds: Int = 90,
        instructions: String = "",
        isCustom: Boolean = false,
        prWeight: Double = 0.0,
        prReps: Int = 0,
        barbellWeightKg: Double = 20.0,
        customBarWeight: Float? = null,
        userId: String? = null,
        isDualPulley: Boolean = false,
        setupMediaUri: String? = null,
        machineCalibration: String? = null
    ) : this(
        id = id.toString(),
        name = name,
        muscleGroup = category,
        equipment = equipmentType,
        prWeight = prWeight,
        prReps = prReps,
        isCustom = isCustom,
        category = category,
        equipmentType = equipmentType,
        notes = notes,
        imageUrl = imageUrl,
        primaryMuscle = primaryMuscle,
        secondaryMuscles = secondaryMuscles,
        equipmentCategory = equipmentCategory,
        trackingType = trackingType,
        defaultRestSeconds = defaultRestSeconds,
        instructions = instructions,
        barbellWeightKg = customBarWeight?.toDouble() ?: barbellWeightKg,
        customBarWeight = customBarWeight,
        userId = userId,
        isDualPulley = isDualPulley,
        setupMediaUri = setupMediaUri,
        machineCalibration = machineCalibration
    )
}

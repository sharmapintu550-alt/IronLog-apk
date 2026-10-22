package com.example.data.entity

import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import java.util.UUID

@Immutable
@Entity(tableName = "body_measurements")
data class BodyMeasurementEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val metricType: String = "Weight",
    val value: Double = 0.0,
    val unit: String = "kg",
    val dateEpochDay: Long = 0L,
    val timestamp: Long = System.currentTimeMillis(),
    val dateString: String = "",
    val weightKg: Double? = null,
    val targetWeightKg: Double? = null,
    val bodyFatPercentage: Double? = null,
    val neckCm: Double? = null,
    val shouldersCm: Double? = null,
    val chestCm: Double? = null,
    val leftArmCm: Double? = null,
    val rightArmCm: Double? = null,
    val forearmsCm: Double? = null,
    val waistCm: Double? = null,
    val hipsCm: Double? = null,
    val leftThighCm: Double? = null,
    val rightThighCm: Double? = null,
    val calvesCm: Double? = null,
    val notes: String? = null
) {
    val uuid: UUID
        @Ignore get() = try {
            UUID.fromString(id)
        } catch (e: Exception) {
            UUID.nameUUIDFromBytes(id.toByteArray(Charsets.UTF_8))
        }

    // Secondary constructor accepting Any for ID (supports UUID, String, etc.)
    @Ignore
    constructor(
        id: Any,
        timestamp: Long = System.currentTimeMillis(),
        dateString: String = "",
        weightKg: Double? = null,
        targetWeightKg: Double? = null,
        bodyFatPercentage: Double? = null,
        neckCm: Double? = null,
        shouldersCm: Double? = null,
        chestCm: Double? = null,
        leftArmCm: Double? = null,
        rightArmCm: Double? = null,
        forearmsCm: Double? = null,
        waistCm: Double? = null,
        hipsCm: Double? = null,
        leftThighCm: Double? = null,
        rightThighCm: Double? = null,
        calvesCm: Double? = null,
        notes: String? = null
    ) : this(
        id = id.toString(),
        metricType = "Weight",
        value = weightKg ?: 0.0,
        unit = "kg",
        dateEpochDay = timestamp / (24L * 60L * 60L * 1000L),
        timestamp = timestamp,
        dateString = dateString,
        weightKg = weightKg,
        targetWeightKg = targetWeightKg,
        bodyFatPercentage = bodyFatPercentage,
        neckCm = neckCm,
        shouldersCm = shouldersCm,
        chestCm = chestCm,
        leftArmCm = leftArmCm,
        rightArmCm = rightArmCm,
        forearmsCm = forearmsCm,
        waistCm = waistCm,
        hipsCm = hipsCm,
        leftThighCm = leftThighCm,
        rightThighCm = rightThighCm,
        calvesCm = calvesCm,
        notes = notes
    )

    // 5-arg constructor for AppDatabase initial data
    @Ignore
    constructor(
        id: String,
        metricType: String,
        value: Double,
        unit: String,
        dateEpochDay: Long
    ) : this(
        id = id,
        metricType = metricType,
        value = value,
        unit = unit,
        dateEpochDay = dateEpochDay,
        timestamp = dateEpochDay * 24L * 60L * 60L * 1000L,
        dateString = "",
        weightKg = if (metricType.equals("Weight", ignoreCase = true)) value else null
    )
}

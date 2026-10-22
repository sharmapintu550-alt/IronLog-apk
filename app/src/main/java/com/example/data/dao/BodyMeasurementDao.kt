package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.entity.BodyMeasurementEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BodyMeasurementDao {
    @Query("SELECT * FROM body_measurements ORDER BY dateEpochDay DESC")
    fun getAllMeasurements(): Flow<List<BodyMeasurementEntity>>

    @Query("SELECT * FROM body_measurements ORDER BY dateEpochDay DESC")
    suspend fun getAllMeasurementsOnce(): List<BodyMeasurementEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(measurement: BodyMeasurementEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(measurements: List<BodyMeasurementEntity>)

    suspend fun insertMeasurement(measurement: BodyMeasurementEntity) = insert(measurement)

    suspend fun insertMeasurements(measurements: List<BodyMeasurementEntity>) = insertAll(measurements)
}

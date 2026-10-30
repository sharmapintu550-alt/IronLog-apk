package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.entity.ExerciseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseDao {
    @Query("SELECT * FROM exercises ORDER BY name ASC")
    fun getAllExercises(): Flow<List<ExerciseEntity>>

    @Query("SELECT * FROM exercises WHERE isCustom = 0 OR userId IS NULL OR userId = '' OR userId = :userId OR :userId = 'guest' ORDER BY name ASC")
    fun getExercisesForUser(userId: String): Flow<List<ExerciseEntity>>

    @Query("SELECT * FROM exercises WHERE LOWER(TRIM(name)) = LOWER(TRIM(:name)) LIMIT 1")
    suspend fun getExerciseByName(name: String): ExerciseEntity?

    @Query("SELECT * FROM exercises ORDER BY name ASC")
    suspend fun getAllExercisesOnce(): List<ExerciseEntity>

    @Query("SELECT COUNT(*) FROM exercises")
    suspend fun getCount(): Int

    suspend fun getExerciseCount(): Int = getCount()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(exercise: ExerciseEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(exercises: List<ExerciseEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercises(exercises: List<ExerciseEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertExercisesIgnore(exercises: List<ExerciseEntity>)

    suspend fun insertExercise(exercise: ExerciseEntity) = insert(exercise)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomExercise(exercise: ExerciseEntity)

    @Update
    suspend fun updateExercise(exercise: ExerciseEntity)

    @Query("DELETE FROM exercises WHERE id = :id")
    suspend fun deleteExerciseById(id: String)

    @Query("UPDATE exercises SET prWeight = :weight, prReps = :reps WHERE id = :id")
    suspend fun updatePr(id: String, weight: Double, reps: Int)

    @Query("UPDATE exercises SET notes = :note WHERE id = :id OR name = :name")
    suspend fun updateExerciseNote(id: String, name: String, note: String)

    @Query("SELECT notes FROM exercises WHERE id = :id OR name = :name LIMIT 1")
    suspend fun getExerciseNote(id: String, name: String): String?

    @Query("UPDATE exercises SET barbellWeightKg = :weight WHERE id = :id OR name = :name")
    suspend fun updateExerciseBarbellWeight(id: String, name: String, weight: Double)

    @Query("SELECT barbellWeightKg FROM exercises WHERE id = :id OR name = :name LIMIT 1")
    suspend fun getExerciseBarbellWeight(id: String, name: String): Double?

    @Query("UPDATE exercises SET setupMediaUri = :mediaUri WHERE id = :id OR name = :name")
    suspend fun updateExerciseSetupMedia(id: String, name: String, mediaUri: String?)

    @Query("SELECT setupMediaUri FROM exercises WHERE id = :id OR name = :name LIMIT 1")
    suspend fun getExerciseSetupMedia(id: String, name: String): String?

    @Query("UPDATE exercises SET machineCalibration = :calibration WHERE id = :id OR name = :name")
    suspend fun updateExerciseCalibration(id: String, name: String, calibration: String?)

    @Query("SELECT machineCalibration FROM exercises WHERE id = :id OR name = :name LIMIT 1")
    suspend fun getExerciseCalibration(id: String, name: String): String?
}

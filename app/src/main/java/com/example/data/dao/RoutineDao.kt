package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.entity.RoutineEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RoutineDao {
    @Query("SELECT * FROM routines WHERE userId = :userId ORDER BY sortOrder ASC, createdAt DESC")
    fun getRoutinesForUser(userId: String): Flow<List<RoutineEntity>>

    @Query("SELECT * FROM routines ORDER BY sortOrder ASC, createdAt DESC")
    fun getAllRoutines(): Flow<List<RoutineEntity>>

    @Query("SELECT * FROM routines WHERE id = :id LIMIT 1")
    suspend fun getRoutineById(id: String): RoutineEntity?

    @Query("SELECT * FROM routines WHERE id = :id AND userId = :userId LIMIT 1")
    suspend fun getRoutineByIdAndUser(id: String, userId: String): RoutineEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(routine: RoutineEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(routines: List<RoutineEntity>)

    @Update
    suspend fun update(routine: RoutineEntity)

    @Query("DELETE FROM routines WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM routines WHERE id = :id AND userId = :userId")
    suspend fun deleteByIdAndUser(id: String, userId: String)

    @Query("DELETE FROM routines WHERE userId = :userId")
    suspend fun deleteByUserId(userId: String)

    @Query("DELETE FROM routines WHERE id IN ('routine_chest_biceps', 'routine_back_triceps', 'routine_legs', 'routine_shoulders_core') OR title IN ('Chest & Biceps', 'Back & Triceps', 'Leg Day Power', 'Shoulder & Core', 'Legs & Shoulders', 'Back & Tricep', 'Chest - Biceps')")
    suspend fun deletePresetRoutines()

    @Query("UPDATE routines SET sortOrder = :newOrder WHERE id = :id")
    suspend fun updateOrder(id: String, newOrder: Int)

    @Query("UPDATE routines SET sortOrder = :order WHERE id = :routineId")
    suspend fun updateRoutineOrder(routineId: String, order: Int)

    @Transaction
    suspend fun updateAllRoutineOrders(orderedRoutines: List<Pair<String, Int>>) {
        orderedRoutines.forEach { (routineId, order) ->
            updateRoutineOrder(routineId, order)
        }
    }
}

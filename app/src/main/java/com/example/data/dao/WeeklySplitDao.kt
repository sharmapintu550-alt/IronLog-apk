package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.entity.WeeklySplitEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WeeklySplitDao {
    @Query("SELECT * FROM weekly_split ORDER BY dayOfWeek ASC")
    fun getAllSplitDays(): Flow<List<WeeklySplitEntity>>

    @Query("SELECT * FROM weekly_split WHERE dayOfWeek = :dayOfWeek LIMIT 1")
    fun getSplitForDay(dayOfWeek: Int): Flow<WeeklySplitEntity?>

    @Query("SELECT * FROM weekly_split WHERE dayOfWeek = :dayOfWeek LIMIT 1")
    suspend fun getSplitForDaySync(dayOfWeek: Int): WeeklySplitEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(split: WeeklySplitEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(splits: List<WeeklySplitEntity>)

    @Query("DELETE FROM weekly_split WHERE dayOfWeek = :dayOfWeek")
    suspend fun deleteForDay(dayOfWeek: Int)
}

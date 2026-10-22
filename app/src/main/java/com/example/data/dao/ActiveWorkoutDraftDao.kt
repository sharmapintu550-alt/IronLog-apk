package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.entity.ActiveWorkoutDraftEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ActiveWorkoutDraftDao {

    @Query("SELECT * FROM active_workout_draft WHERE id = 'active_draft' LIMIT 1")
    fun getDraft(): Flow<ActiveWorkoutDraftEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveDraft(draft: ActiveWorkoutDraftEntity)

    @Query("DELETE FROM active_workout_draft WHERE id = 'active_draft'")
    suspend fun clearDraft()
}

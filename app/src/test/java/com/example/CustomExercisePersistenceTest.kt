package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.AppDatabase
import com.example.data.entity.ExerciseEntity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.UUID

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class CustomExercisePersistenceTest {

    private lateinit var database: AppDatabase

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun testCreateCustomExercise_persistsAndMatchesFilters() = runBlocking {
        val customExercise = ExerciseEntity(
            id = UUID.randomUUID().toString(),
            name = "Overhead Tricep Extension (Facing Away)",
            muscleGroup = "Triceps",
            equipment = "Cable",
            category = "Arms",
            equipmentType = "Cable",
            notes = "Facing away from cable tower",
            isCustom = true,
            primaryMuscle = "Triceps",
            secondaryMuscles = emptyList(),
            equipmentCategory = "Cable",
            trackingType = "WEIGHT_AND_REPS",
            defaultRestSeconds = 90,
            barbellWeightKg = 0.0,
            userId = "guest"
        )

        database.exerciseDao().insertCustomExercise(customExercise)

        val retrieved = database.exerciseDao().getExerciseByName("overhead tricep extension (facing away)")
        assertNotNull(retrieved)
        assertEquals("Overhead Tricep Extension (Facing Away)", retrieved?.name)
        assertTrue(retrieved?.isCustom == true)
        assertEquals("Triceps", retrieved?.targetMuscle)
        assertEquals("Cable", retrieved?.equipmentType)
        assertEquals("guest", retrieved?.userId)

        // Verify duplicate check prevents crashing and returns existing
        val duplicateCheck = database.exerciseDao().getExerciseByName("Overhead Tricep Extension (Facing Away)")
        assertNotNull(duplicateCheck)
        assertEquals(retrieved?.id, duplicateCheck?.id)
    }

    @Test
    fun testTargetMuscleProperty_fallbackToMuscleGroup() {
        val ex1 = ExerciseEntity(name = "Test 1", primaryMuscle = "Triceps", muscleGroup = "Arms")
        assertEquals("Triceps", ex1.targetMuscle)

        val ex2 = ExerciseEntity(name = "Test 2", primaryMuscle = "", muscleGroup = "Chest")
        assertEquals("Chest", ex2.targetMuscle)
    }
}

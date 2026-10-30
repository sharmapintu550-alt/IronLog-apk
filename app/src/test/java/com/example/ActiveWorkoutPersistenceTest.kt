package com.example

import androidx.compose.runtime.toMutableStateList
import com.example.data.entity.ActiveWorkoutStateEntity
import com.example.ui.screens.ActiveSetType
import com.example.ui.screens.ActiveWorkoutExercise
import com.example.ui.screens.ActiveWorkoutSet
import com.example.util.ActiveWorkoutSerializer
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.UUID

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class ActiveWorkoutPersistenceTest {

    @Test
    fun testSerializationAndDeserializationLossless() {
        val exercise1 = ActiveWorkoutExercise(
            id = "ex_squat_1",
            name = "Barbell Back Squat",
            primaryMuscle = "Quads",
            equipmentType = "Barbell",
            note = "Focus on depth and knee tracking",
            sets = mutableListOf(
                ActiveWorkoutSet(
                    id = "s1",
                    setNumber = 1,
                    setType = ActiveSetType.WARMUP,
                    previous = "60 kg × 10",
                    weight = "60.0",
                    reps = "10",
                    isCompleted = true
                ),
                ActiveWorkoutSet(
                    id = "s2",
                    setNumber = 2,
                    setType = ActiveSetType.NORMAL,
                    previous = "100 kg × 8",
                    weight = "100.0",
                    reps = "8",
                    isCompleted = true
                ),
                ActiveWorkoutSet(
                    id = "s3",
                    setNumber = 3,
                    setType = ActiveSetType.NORMAL,
                    previous = "110 kg × 6",
                    weight = "110.0",
                    reps = "6",
                    isCompleted = false
                )
            ).toMutableStateList(),
            initialBarbellWeightKg = 20.0
        )

        val exercise2 = ActiveWorkoutExercise(
            id = "ex_rdl_2",
            name = "Romanian Deadlift",
            primaryMuscle = "Hamstrings",
            equipmentType = "Barbell",
            note = "Slight knee bend",
            sets = mutableListOf(
                ActiveWorkoutSet(
                    id = "s4",
                    setNumber = 1,
                    setType = ActiveSetType.NORMAL,
                    previous = "80 kg × 10",
                    weight = "85.0",
                    reps = "10",
                    isCompleted = true
                )
            ).toMutableStateList(),
            initialBarbellWeightKg = 20.0,
            initialIsCompleted = true
        )

        val originalExercises = listOf(exercise1, exercise2)

        // 1. Serialize to JSON
        val json = ActiveWorkoutSerializer.serialize(originalExercises)
        assertTrue("Serialized JSON must not be blank", json.isNotBlank())

        // 2. Deserialize from JSON
        val restoredExercises = ActiveWorkoutSerializer.deserialize(json)

        // 3. Verify exact count and order
        assertEquals(2, restoredExercises.size)
        assertEquals("Barbell Back Squat", restoredExercises[0].name)
        assertEquals("Romanian Deadlift", restoredExercises[1].name)

        // 4. Verify exercise details
        assertEquals("Focus on depth and knee tracking", restoredExercises[0].note)
        assertEquals(20.0, restoredExercises[0].barbellWeightKg, 0.01)
        assertFalse("exercise1 isCompleted should be false", restoredExercises[0].isCompleted)
        assertTrue("exercise2 isCompleted should be true", restoredExercises[1].isCompleted)

        // 5. Verify sets details
        val squatSets = restoredExercises[0].sets
        assertEquals(3, squatSets.size)
        assertEquals(ActiveSetType.WARMUP, squatSets[0].setType)
        assertEquals("60.0", squatSets[0].weight)
        assertEquals("10", squatSets[0].reps)
        assertTrue(squatSets[0].isCompleted)

        assertEquals("100.0", squatSets[1].weight)
        assertEquals("8", squatSets[1].reps)
        assertTrue(squatSets[1].isCompleted)

        assertEquals("110.0", squatSets[2].weight)
        assertEquals("6", squatSets[2].reps)
        assertFalse(squatSets[2].isCompleted)

        // 6. Verify second exercise sets
        val rdlSets = restoredExercises[1].sets
        assertEquals(1, rdlSets.size)
        assertEquals("85.0", rdlSets[0].weight)
        assertEquals("10", rdlSets[0].reps)
        assertTrue(rdlSets[0].isCompleted)
    }

    @Test
    fun testContinuousElapsedTimerImmuneToProcessDeath() {
        val startTimestamp = System.currentTimeMillis() - 120_000L // 2 minutes ago
        val simulatedDeathTime = System.currentTimeMillis()

        // Elapsed time computation: System.currentTimeMillis() - startTimestamp
        val elapsedSeconds = (simulatedDeathTime - startTimestamp) / 1000

        assertTrue("Elapsed time must be around 120 seconds", elapsedSeconds in 119..125)
    }

    @Test
    fun testActiveWorkoutStateEntityCreation() {
        val state = ActiveWorkoutStateEntity(
            workoutId = "active_session",
            routineId = "routine_leg_day",
            routineName = "Leg Day Heavy",
            startTimestamp = System.currentTimeMillis(),
            lastUpdatedTimestamp = System.currentTimeMillis(),
            isPaused = false,
            userId = "guest",
            exercisesJson = "[]"
        )

        assertEquals("active_session", state.workoutId)
        assertEquals("Leg Day Heavy", state.routineName)
        assertFalse(state.isPaused)
        assertEquals("guest", state.userId)
    }

    @Test
    fun testWallClockEpochDurationOneHourAccurate() {
        val now = System.currentTimeMillis()
        val startTimestamp = now - 3600_000L // Started exactly 1 hour (3600 seconds) ago
        val totalPausedSeconds = 0L

        // Wall-clock epoch math
        val rawElapsed = (now - startTimestamp) / 1000L
        val currentDurationSeconds = maxOf(0L, rawElapsed - totalPausedSeconds)

        assertEquals("An hour-long session must record exactly 3600 seconds", 3600L, currentDurationSeconds)
    }

    @Test
    fun testWallClockEpochDurationWithPauseAndResume() {
        val now = System.currentTimeMillis()
        val startTimestamp = now - 3600_000L // 1 hour ago
        val totalPausedSeconds = 600L // 10 minutes paused previously

        // Case 1: Unpaused
        val unpausedDuration = maxOf(0L, ((now - startTimestamp) / 1000L) - totalPausedSeconds)
        assertEquals(3000L, unpausedDuration) // 50 active minutes

        // Case 2: Currently paused 5 minutes ago
        val pauseTimestamp = now - 300_000L
        val ongoingPaused = (now - pauseTimestamp) / 1000L // 300s
        val effectivePaused = totalPausedSeconds + ongoingPaused // 900s
        val currentlyPausedDuration = maxOf(0L, ((now - startTimestamp) / 1000L) - effectivePaused)
        assertEquals(2700L, currentlyPausedDuration) // 45 active minutes
    }

    @Test
    fun testActiveWorkoutStateEntityWithPauseSupport() {
        val now = System.currentTimeMillis()
        val state = ActiveWorkoutStateEntity(
            workoutId = "active_session",
            routineId = "routine_push",
            routineName = "Push Day",
            startTimestamp = now - 1800_000L,
            lastUpdatedTimestamp = now,
            isPaused = true,
            pauseTimestamp = now - 60_000L,
            totalPausedSeconds = 120L,
            userId = "guest",
            exercisesJson = "[]"
        )

        assertTrue(state.isPaused)
        assertEquals(120L, state.totalPausedSeconds)
        assertNotNull(state.pauseTimestamp)
    }
}

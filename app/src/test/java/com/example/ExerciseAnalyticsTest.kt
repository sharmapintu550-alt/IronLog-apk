package com.example

import com.example.data.entity.WorkoutLogEntity
import com.example.data.repository.ExerciseAnalyticsRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExerciseAnalyticsTest {

    @Test
    fun testEstimated1RMEpleyFormula() {
        // 0 weight should return 0.0
        assertEquals(0.0, ExerciseAnalyticsRepository.calculateEstimated1RM(0.0, 10), 0.01)

        // 1 rep should return exactly the weight
        assertEquals(100.0, ExerciseAnalyticsRepository.calculateEstimated1RM(100.0, 1), 0.01)

        // 100 kg for 10 reps: 100 * (1 + 10/30) = 133.33 kg
        val e1rm10 = ExerciseAnalyticsRepository.calculateEstimated1RM(100.0, 10)
        assertEquals(133.33, e1rm10, 0.05)

        // 80 kg for 6 reps: 80 * (1 + 6/30) = 80 * 1.2 = 96.0 kg
        val e1rm6 = ExerciseAnalyticsRepository.calculateEstimated1RM(80.0, 6)
        assertEquals(96.0, e1rm6, 0.01)
    }

    @Test
    fun testExerciseMatching() {
        // Exact match
        assertTrue(ExerciseAnalyticsRepository.matchesExercise("Barbell Bench Press", "Barbell Bench Press"))

        // Numbered list prefix in logs
        assertTrue(ExerciseAnalyticsRepository.matchesExercise("1. Barbell Bench Press (3 sets)", "Barbell Bench Press"))

        // Case insensitivity
        assertTrue(ExerciseAnalyticsRepository.matchesExercise("barbell bench press", "Barbell Bench Press"))

        // Abbreviation DB -> Dumbbell
        assertTrue(ExerciseAnalyticsRepository.matchesExercise("DB Incline Curl", "Dumbbell Incline Curl"))

        // Abbreviation BB -> Barbell
        assertTrue(ExerciseAnalyticsRepository.matchesExercise("BB Squat", "Barbell Squat"))

        // Unrelated exercises should not match
        assertFalse(ExerciseAnalyticsRepository.matchesExercise("Triceps Pushdown", "Barbell Squat"))
    }

    @Test
    fun testAnalyticsComputationWithProgression() {
        val log1 = WorkoutLogEntity(
            id = "log_1",
            routineId = "routine_push",
            routineTitle = "Push Day A",
            dateEpochDay = 19000L,
            durationSeconds = 3600L,
            totalVolumeKg = 3000.0,
            totalSets = 6,
            completedExercisesSummary = "1. Barbell Bench Press (3 sets), 2. Incline DB Press (3 sets)",
            timestamp = 1000000L
        )

        val log2 = WorkoutLogEntity(
            id = "log_2",
            routineId = "routine_push",
            routineTitle = "Push Day B",
            dateEpochDay = 19007L,
            durationSeconds = 3800L,
            totalVolumeKg = 3500.0,
            totalSets = 6,
            completedExercisesSummary = "1. Barbell Bench Press (3 sets), 2. Incline DB Press (3 sets)",
            timestamp = 2000000L
        )

        val summary = ExerciseAnalyticsRepository.computeAnalytics(
            exerciseName = "Barbell Bench Press",
            targetMuscle = "Chest",
            targetEquipment = "Barbell",
            logs = listOf(log1, log2)
        )

        assertEquals("Barbell Bench Press", summary.exerciseName)
        assertEquals(2, summary.totalSessions)
        assertTrue(summary.allTimeMaxWeight > 0.0)
        assertTrue(summary.bestEstimated1RM > 0.0)
        assertTrue(summary.lifetimeVolume > 0.0)
        assertEquals(2, summary.sessionRecords.size)

        // Newest session should be first in sessionRecords
        assertEquals("log_2", summary.sessionRecords[0].logId)
        assertEquals("log_1", summary.sessionRecords[1].logId)
    }

    @Test
    fun testBaselinePrFallbackWhenNoLogs() {
        val summary = ExerciseAnalyticsRepository.computeAnalytics(
            exerciseName = "Overhead Press",
            targetMuscle = "Shoulders",
            targetEquipment = "Barbell",
            logs = emptyList(),
            baselinePrWeight = 60.0,
            baselinePrReps = 5
        )

        assertEquals("Overhead Press", summary.exerciseName)
        assertEquals(1, summary.totalSessions)
        assertEquals(60.0, summary.allTimeMaxWeight, 0.01)
        val expected1RM = 60.0 * (1.0 + 5.0 / 30.0)
        assertEquals(expected1RM, summary.bestEstimated1RM, 0.01)
        assertEquals(1, summary.sessionRecords.size)
    }
}

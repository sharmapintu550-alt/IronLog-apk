package com.example

import com.example.data.entity.WeeklySplitEntity
import com.example.util.StreakCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate

class ExampleUnitTest {

    @Test
    fun testWeeklySplitEntityProperties() {
        val split = WeeklySplitEntity(
            dayOfWeek = 1,
            routineId = "routine_back_triceps",
            routineTitle = "Back & Triceps",
            reminderTime = "06:30 PM",
            isRestDay = false
        )
        assertEquals(1, split.dayOfWeek)
        assertEquals("routine_back_triceps", split.routineId)
        assertEquals("Back & Triceps", split.routineTitle)
        assertEquals("06:30 PM", split.reminderTime)
        assertFalse(split.isRestDay)

        val restSplit = WeeklySplitEntity(
            dayOfWeek = 3,
            routineId = null,
            routineTitle = null,
            isRestDay = true
        )
        assertEquals(3, restSplit.dayOfWeek)
        assertTrue(restSplit.isRestDay)
    }

    @Test
    fun testRestDaySafeStreakCalculation() {
        // Monday (1) = Workout
        // Tuesday (2) = Workout
        // Wednesday (3) = Rest Day
        // Thursday (4) = Workout
        // Friday (5) = Workout
        // Saturday (6) = Rest Day
        // Sunday (7) = Rest Day
        val splitMap = mapOf(
            DayOfWeek.MONDAY to WeeklySplitEntity(1, "r1", "Chest", isRestDay = false),
            DayOfWeek.TUESDAY to WeeklySplitEntity(2, "r2", "Back", isRestDay = false),
            DayOfWeek.WEDNESDAY to WeeklySplitEntity(3, null, null, isRestDay = true),
            DayOfWeek.THURSDAY to WeeklySplitEntity(4, "r3", "Legs", isRestDay = false),
            DayOfWeek.FRIDAY to WeeklySplitEntity(5, "r4", "Shoulders", isRestDay = false),
            DayOfWeek.SATURDAY to WeeklySplitEntity(6, null, null, isRestDay = true),
            DayOfWeek.SUNDAY to WeeklySplitEntity(7, null, null, isRestDay = true)
        )

        // Reference date: Thursday 2026-10-22
        // Monday completed, Tuesday completed, Wednesday rest, Thursday completed -> streak should be 3
        val thursday = LocalDate.of(2026, 10, 22) // Thursday
        val monday = thursday.minusDays(3)
        val tuesday = thursday.minusDays(2)
        val completedLogs = setOf(monday, tuesday, thursday)

        val streak = StreakCalculator.calculateActiveStreak(
            completedLogDates = completedLogs,
            weeklySplit = splitMap,
            today = thursday
        )
        // 3 completed workouts, rest day Wednesday did NOT penalize or break streak
        assertEquals(3, streak)
    }

    @Test
    fun testMissedWorkoutBreaksStreak() {
        val splitMap = mapOf(
            DayOfWeek.MONDAY to WeeklySplitEntity(1, "r1", "Chest", isRestDay = false),
            DayOfWeek.TUESDAY to WeeklySplitEntity(2, "r2", "Back", isRestDay = false),
            DayOfWeek.WEDNESDAY to WeeklySplitEntity(3, null, null, isRestDay = true),
            DayOfWeek.THURSDAY to WeeklySplitEntity(4, "r3", "Legs", isRestDay = false)
        )

        val thursday = LocalDate.of(2026, 10, 22) // Thursday
        val monday = thursday.minusDays(3)
        // Tuesday was scheduled but MISSED (not in completedLogs)
        val completedLogs = setOf(monday, thursday)

        val streak = StreakCalculator.calculateActiveStreak(
            completedLogDates = completedLogs,
            weeklySplit = splitMap,
            today = thursday
        )
        // Streak only counts Thursday (1) because Tuesday was a missed scheduled workout
        assertEquals(1, streak)
    }

    @Test
    fun testTodayPendingWorkoutPreservesPreviousStreak() {
        val splitMap = mapOf(
            DayOfWeek.MONDAY to WeeklySplitEntity(1, "r1", "Chest", isRestDay = false),
            DayOfWeek.TUESDAY to WeeklySplitEntity(2, "r2", "Back", isRestDay = false)
        )

        val tuesday = LocalDate.of(2026, 10, 20) // Tuesday
        val monday = tuesday.minusDays(1)
        // Monday completed, Tuesday is today and pending (not yet logged)
        val completedLogs = setOf(monday)

        val streak = StreakCalculator.calculateActiveStreak(
            completedLogDates = completedLogs,
            weeklySplit = splitMap,
            today = tuesday
        )
        // Monday's workout is preserved as 1-day active streak while Tuesday is still pending
        assertEquals(1, streak)
    }
}

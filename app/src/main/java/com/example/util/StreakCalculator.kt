package com.example.util

import com.example.data.entity.WeeklySplitEntity
import java.time.DayOfWeek
import java.time.LocalDate

object StreakCalculator {

    fun calculateActiveStreak(
        completedLogDates: Set<LocalDate>,
        weeklySplit: Map<DayOfWeek, WeeklySplitEntity>,
        today: LocalDate = LocalDate.now()
    ): Int {
        if (completedLogDates.isEmpty()) return 0

        var streakDays = 0
        var checkDate = today

        // If today's allocated workout isn't done yet, check if it's still pending today without breaking past streak
        val todaySplit = weeklySplit[checkDate.dayOfWeek]
        val todayIsRest = todaySplit?.isRestDay == true || todaySplit?.routineId == null
        val todayCompleted = checkDate in completedLogDates

        if (!todayCompleted && !todayIsRest) {
            // Today's workout is planned but still pending; start checking from yesterday backwards
            checkDate = checkDate.minusDays(1)
        }

        val earliestDate = completedLogDates.minOrNull()?.minusDays(7) ?: today.minusDays(30)

        // Walk backwards day-by-day
        while (checkDate >= earliestDate) {
            val split = weeklySplit[checkDate.dayOfWeek]
            val isRestDay = split?.isRestDay == true || split?.routineId == null
            val hasCompletedWorkout = checkDate in completedLogDates

            when {
                hasCompletedWorkout -> {
                    streakDays++
                    checkDate = checkDate.minusDays(1)
                }
                isRestDay -> {
                    // Legitimate planned rest day: advance backward without breaking the streak
                    checkDate = checkDate.minusDays(1)
                }
                else -> {
                    // An explicitly scheduled workout day was missed: streak terminates here
                    break
                }
            }
        }
        return streakDays
    }

    fun calculateWeeklyStreak(
        completedLogs: Set<LocalDate>,
        weeklySplit: Map<DayOfWeek, WeeklySplitEntity>,
        today: LocalDate = LocalDate.now()
    ): Int {
        // A week counts as completed if all NON-REST scheduled days were completed
        val scheduledNonRestDays = weeklySplit.filter { !it.value.isRestDay }.keys
        if (scheduledNonRestDays.isEmpty()) return 0

        var weeksStreak = 0
        val currentWeekMonday = today.minusDays((today.dayOfWeek.value - 1).toLong())

        // Check if current week is already completed
        val currentWeekCompleted = scheduledNonRestDays.all { day ->
            val date = currentWeekMonday.plusDays((day.value - 1).toLong())
            date in completedLogs
        }

        var checkMonday = if (currentWeekCompleted) {
            weeksStreak++
            currentWeekMonday.minusWeeks(1)
        } else {
            // Current week is in progress; start evaluating from previous completed week
            currentWeekMonday.minusWeeks(1)
        }

        val earliestDate = completedLogs.minOrNull() ?: today
        while (checkMonday >= earliestDate.minusWeeks(1)) {
            val weekCompleted = scheduledNonRestDays.all { day ->
                val date = checkMonday.plusDays((day.value - 1).toLong())
                date in completedLogs
            }
            if (weekCompleted) {
                weeksStreak++
                checkMonday = checkMonday.minusWeeks(1)
            } else {
                break
            }
        }

        return weeksStreak
    }
}

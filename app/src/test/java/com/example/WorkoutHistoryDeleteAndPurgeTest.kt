package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.AppDatabase
import com.example.data.entity.WorkoutLogEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class WorkoutHistoryDeleteAndPurgeTest {

    private lateinit var db: AppDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun testPurgeSeedLogsRemovesMockHistoryAndPreservesGenuineLogs() = runBlocking {
        val workoutLogDao = db.workoutLogDao()
        val today = LocalDate.now().toEpochDay()

        // 1. Insert genuine user log
        val genuineLog = WorkoutLogEntity(
            id = "user_log_123",
            routineId = "routine_my_custom",
            routineTitle = "Custom Push Hypertrophy",
            dateEpochDay = today,
            durationSeconds = 2700L,
            totalVolumeKg = 8400.0,
            totalSets = 14,
            completedExercisesSummary = "Bench Press, Incline DB Press"
        )
        workoutLogDao.insertLog(genuineLog)

        // 2. Insert mock seed logs that previously inflated stats
        val seedLog1 = WorkoutLogEntity(
            id = "seed_log_1",
            routineId = "routine_legs",
            routineTitle = "Leg Day Power",
            dateEpochDay = today - 3,
            durationSeconds = 3600L,
            totalVolumeKg = 21000.0,
            totalSets = 24,
            completedExercisesSummary = "Squat, RDL"
        )
        val seedLog2 = WorkoutLogEntity(
            id = "mock_log_2",
            routineId = "routine_back_triceps",
            routineTitle = "Back & Triceps",
            dateEpochDay = today - 2,
            durationSeconds = 3200L,
            totalVolumeKg = 18000.0,
            totalSets = 20,
            completedExercisesSummary = "Deadlift, Pull-up"
        )
        val seedLog3 = WorkoutLogEntity(
            id = "seed_log_3",
            routineId = "routine_chest_biceps",
            routineTitle = "Chest & Biceps",
            dateEpochDay = today - 1,
            durationSeconds = 3000L,
            totalVolumeKg = 14000.0,
            totalSets = 18,
            completedExercisesSummary = "Bench Press, Bicep Curl"
        )
        workoutLogDao.insertAll(listOf(seedLog1, seedLog2, seedLog3))

        // Pre-purge count is 4
        val beforePurge = workoutLogDao.getAllLogs().first()
        assertEquals(4, beforePurge.size)

        // Run purge
        workoutLogDao.purgeSeedLogs()

        // Post-purge: only genuine log survives
        val afterPurge = workoutLogDao.getAllLogs().first()
        assertEquals(1, afterPurge.size)
        assertEquals("user_log_123", afterPurge[0].id)
        assertEquals("Custom Push Hypertrophy", afterPurge[0].routineTitle)
        assertEquals(8400.0, afterPurge[0].totalVolumeKg, 0.01)
        assertEquals(14, afterPurge[0].totalSets)
    }

    @Test
    fun testDeleteWorkoutLogRecalculatesMetricsLosslessly() = runBlocking {
        val workoutLogDao = db.workoutLogDao()
        val today = LocalDate.now().toEpochDay()

        val logA = WorkoutLogEntity(
            id = "log_a",
            routineId = "r1",
            routineTitle = "Upper Body Heavy",
            dateEpochDay = today - 1,
            durationSeconds = 3600L,
            totalVolumeKg = 12500.0,
            totalSets = 16,
            completedExercisesSummary = "Overhead Press"
        )
        val logB = WorkoutLogEntity(
            id = "log_b",
            routineId = "r2",
            routineTitle = "Lower Body Hypertrophy",
            dateEpochDay = today,
            durationSeconds = 4000L,
            totalVolumeKg = 15000.0,
            totalSets = 20,
            completedExercisesSummary = "Barbell Squat"
        )

        workoutLogDao.insertAll(listOf(logA, logB))

        var logs = workoutLogDao.getAllLogs().first()
        assertEquals(2, logs.size)
        assertEquals(27500.0, logs.sumOf { it.totalVolumeKg }, 0.01)
        assertEquals(36, logs.sumOf { it.totalSets })

        // Delete logA
        workoutLogDao.deleteById("log_a")

        logs = workoutLogDao.getAllLogs().first()
        assertEquals(1, logs.size)
        assertEquals("log_b", logs[0].id)
        assertEquals(15000.0, logs.sumOf { it.totalVolumeKg }, 0.01)
        assertEquals(20, logs.sumOf { it.totalSets })

        // Delete remaining logB -> completely empty state
        workoutLogDao.delete(logs[0])

        logs = workoutLogDao.getAllLogs().first()
        assertTrue("Workout history should be completely empty", logs.isEmpty())
        assertEquals(0.0, logs.sumOf { it.totalVolumeKg }, 0.01)
        assertEquals(0, logs.sumOf { it.totalSets })
    }
}

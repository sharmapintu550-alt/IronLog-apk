package com.example

import com.example.data.backup.BackupManager
import com.example.data.entity.RoutineEntity
import com.example.data.entity.WorkoutLogEntity
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class BackupManagerTest {

    @Test
    fun testParseBackupJsonValidStructure() {
        val root = JSONObject()
        root.put("version", 1)
        root.put("app", "IronLog")
        root.put("exportTimestamp", System.currentTimeMillis())

        val routinesArray = JSONArray()
        val r1 = JSONObject().apply {
            put("id", "routine_chest_test")
            put("title", "Heavy Chest")
            put("description", "Bench press focus")
            put("exercisesSummary", "Barbell Bench Press (3 sets)")
            put("orderIndex", 0)
            put("createdAt", 1700000000000L)
        }
        routinesArray.put(r1)
        root.put("routines", routinesArray)

        val logsArray = JSONArray()
        val l1 = JSONObject().apply {
            put("id", "log_test_1")
            put("routineId", "routine_chest_test")
            put("routineTitle", "Heavy Chest")
            put("dateEpochDay", LocalDate.of(2025, 4, 10).toEpochDay())
            put("durationSeconds", 3600L)
            put("totalVolumeKg", 5000.0)
            put("totalSets", 12)
            put("completedExercisesSummary", "Barbell Bench Press (3 sets)")
            put("timestamp", 1700000000000L)
        }
        logsArray.put(l1)
        root.put("workoutLogs", logsArray)

        val customArray = JSONArray()
        val c1 = JSONObject().apply {
            put("id", "ex_custom_test")
            put("name", "Deficit Deadlift")
            put("muscleGroup", "Back")
            put("equipment", "Barbell")
            put("isCustom", true)
            put("prWeight", 180.0)
            put("prReps", 5)
        }
        customArray.put(c1)
        root.put("customExercises", customArray)

        val parseResult = BackupManager.parseBackupJson(root.toString())
        assertTrue("Parsing should succeed", parseResult.isSuccess)

        val parsedData = parseResult.getOrThrow()
        assertEquals(1, parsedData.summary.routinesCount)
        assertEquals(1, parsedData.summary.workoutLogsCount)
        assertEquals(1, parsedData.summary.customExercisesCount)

        assertEquals("routine_chest_test", parsedData.routines[0].id)
        assertEquals("Heavy Chest", parsedData.routines[0].title)
        assertEquals("log_test_1", parsedData.workoutLogs[0].id)
        assertEquals("Deficit Deadlift", parsedData.customExercises[0].name)
    }

    @Test
    fun testParseBackupJsonEmptyOrMalformed() {
        val parseResult = BackupManager.parseBackupJson("not valid json")
        assertTrue("Parsing invalid json should fail safely", parseResult.isFailure)
    }
}

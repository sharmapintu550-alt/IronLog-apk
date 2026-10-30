package com.example

import com.example.data.entity.ExerciseEntity
import com.example.data.entity.RoutineEntity
import com.example.util.QrCodeGenerator
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class QrCodeGeneratorTest {

    @Test
    fun testGenerateStandardQrBitmap() {
        val testContent = "ironlog://routine?data=eyJ2IjoxLCJyb3V0aW5lX25hbWUiOiJUZXN0In0="
        val bitmap = QrCodeGenerator.generateStandardQrBitmap(testContent, 256)
        assertNotNull(bitmap)
        assertTrue(bitmap.width >= 200)
        assertTrue(bitmap.height >= 200)
    }

    @Test
    fun testGenerateAmoledQrBitmap() {
        val testContent = "ironlog://routine/share?data=eyJ2IjoxLCJyb3V0aW5lX25hbWUiOiJUZXN0In0="
        val bitmap = QrCodeGenerator.generateAmoledQrBitmap(testContent, 256)
        assertNotNull(bitmap)
        assertTrue(bitmap.width >= 200)
        assertTrue(bitmap.height >= 200)
    }

    @Test
    fun testBuildRoutinePayloadAndDeepLinkRoundTrip() {
        val routine = RoutineEntity(
            id = "routine_push_1",
            title = "Push Day Heavy",
            description = "Focus on heavy pressing",
            exercisesSummary = "Barbell Bench Press, Incline Dumbbell Press, Tricep Pushdown",
            sortOrder = 0
        )

        val catalogExercises = listOf(
            ExerciseEntity(id = "bench_press", name = "Barbell Bench Press", muscleGroup = "Chest", equipment = "Barbell"),
            ExerciseEntity(id = "incline_db_press", name = "Incline Dumbbell Press", muscleGroup = "Chest", equipment = "Dumbbell"),
            ExerciseEntity(id = "tricep_pushdown", name = "Tricep Pushdown", muscleGroup = "Arms", equipment = "Cable")
        )

        val payload = QrCodeGenerator.buildRoutinePayload(routine, catalogExercises)
        assertEquals("Push Day Heavy", payload.routineName)
        assertEquals(3, payload.exercises.size)
        assertEquals("Barbell Bench Press", payload.exercises[0].name)
        assertEquals("Chest", payload.exercises[0].muscle)

        // Test Deep Link creation
        val deepLink = QrCodeGenerator.createDeepLink(payload)
        assertTrue(deepLink.startsWith("ironlog://routine?data=") || deepLink.startsWith("ironlog://routine/share?data="))

        // Test Deep Link parsing
        val decodedPayload = QrCodeGenerator.parseInput(deepLink)
        assertNotNull(decodedPayload)
        assertEquals("Push Day Heavy", decodedPayload!!.routineName)
        assertEquals(3, decodedPayload.exercises.size)
        assertEquals("Incline Dumbbell Press", decodedPayload.exercises[1].name)
    }

    @Test
    fun testParseRawJsonAndBase64() {
        val rawJson = """
            {
                "v": 1,
                "routine_name": "Leg Annihilation",
                "description": "Quads & Hamstrings",
                "exercises_summary": "Barbell Squat, Romanian Deadlift",
                "exercises": [
                    {"name": "Barbell Squat", "muscle": "Legs", "equipment": "Barbell", "default_sets": 4, "target_reps": "6-8"},
                    {"name": "Romanian Deadlift", "muscle": "Legs", "equipment": "Barbell", "default_sets": 3, "target_reps": "8-10"}
                ]
            }
        """.trimIndent()

        // 1. Raw JSON parsing
        val parsedFromJson = QrCodeGenerator.parseInput(rawJson)
        assertNotNull(parsedFromJson)
        assertEquals("Leg Annihilation", parsedFromJson!!.routineName)
        assertEquals(2, parsedFromJson.exercises.size)

        // 2. Base64 string parsing
        val base64 = QrCodeGenerator.encodeToBase64(rawJson)
        val parsedFromBase64 = QrCodeGenerator.parseInput(base64)
        assertNotNull(parsedFromBase64)
        assertEquals("Leg Annihilation", parsedFromBase64!!.routineName)
        assertEquals(2, parsedFromBase64.exercises.size)
    }

    @Test
    fun testParseInvalidInputGracefulFallback() {
        assertNull(QrCodeGenerator.parseInput(""))
        assertNull(QrCodeGenerator.parseInput("not_a_valid_json_or_link"))
        assertNull(QrCodeGenerator.parseInput("ironlog://unrelated/path"))
    }
}

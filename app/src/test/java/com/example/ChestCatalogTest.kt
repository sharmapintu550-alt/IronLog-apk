package com.example

import com.example.data.DefaultExercisesData
import com.example.data.entity.ExerciseEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ChestCatalogTest {

    private val expected61ChestExerciseNames = listOf(
        // 1. BARBELL (9)
        "Flat Barbell Bench Press (Standard Grip)",
        "Flat Barbell Bench Press (Wide Grip)",
        "Flat Barbell Bench Press (Close Grip)",
        "Incline Barbell Bench Press (30 Degree)",
        "Incline Barbell Bench Press (45 Degree)",
        "Incline Barbell Bench Press (Close Grip)",
        "Decline Barbell Bench Press",
        "Reverse-Grip Barbell Bench Press (Upper Chest Focus)",
        "Guillotine Bench Press (Neck Press / High Flare)",

        // 2. DUMBBELL (15)
        "Flat Dumbbell Bench Press (Pronated Grip)",
        "Flat Dumbbell Bench Press (Neutral Grip)",
        "Incline Dumbbell Bench Press (30 Degree)",
        "Incline Dumbbell Bench Press (45 Degree)",
        "Incline Dumbbell Bench Press (Neutral Grip)",
        "Decline Dumbbell Bench Press",
        "Flat Dumbbell Fly",
        "Incline Dumbbell Fly (30 Degree)",
        "Incline Dumbbell Fly (45 Degree)",
        "Decline Dumbbell Fly",
        "Dumbbell Pullover (Chest Focus)",
        "Incline Dumbbell Squeeze Press (Hex Press)",
        "Flat Dumbbell Squeeze Press (Hex Press)",
        "Single-Arm Dumbbell Flat Bench Press",
        "Single-Arm Dumbbell Incline Press",

        // 3. CABLE (11)
        "Cable Crossover (High-to-Low / Lower Chest)",
        "Cable Fly (Mid-Chest / Level Height)",
        "Low-to-High Cable Fly (Upper Chest Clavicular Focus)",
        "Seated Incline Cable Press",
        "Seated Flat Cable Chest Press",
        "Single-Arm Cable Crossover (High-to-Low)",
        "Single-Arm Cable Fly (Mid-Chest)",
        "Single-Arm Low-to-High Cable Fly",
        "Standing Cable Chest Press (Staggered Stance)",
        "Lying Cable Fly on Flat Bench",
        "Lying Cable Fly on Incline Bench",

        // 4. MACHINE (9)
        "Machine Chest Press (Seated Flat / Horizontal)",
        "Machine Incline Chest Press",
        "Machine Decline Chest Press",
        "Machine Converging Chest Press",
        "Plate-Loaded Incline Chest Press (Iso-Lateral)",
        "Plate-Loaded Flat Chest Press (Iso-Lateral)",
        "Plate-Loaded Decline Chest Press (Iso-Lateral)",
        "Pec Deck Fly Machine (Seated)",
        "Single-Arm Machine Chest Press",

        // 5. SMITH MACHINE (6)
        "Smith Machine Flat Bench Press",
        "Smith Machine Incline Bench Press (30 Degree)",
        "Smith Machine Incline Bench Press (45 Degree)",
        "Smith Machine Decline Bench Press",
        "Smith Machine Close-Grip Incline Press",
        "Smith Machine Reverse-Grip Bench Press",

        // 6. BODYWEIGHT / CALISTHENICS (11)
        "Push-ups (Standard Grip)",
        "Push-ups (Wide Grip)",
        "Diamond Push-ups (Chest & Triceps Focus)",
        "Incline Push-ups (Hands Elevated / Lower Chest Focus)",
        "Decline Push-ups (Feet Elevated / Upper Chest Focus)",
        "Deficit Push-ups (Deep Stretch on Parallettes/Blocks)",
        "Weighted Push-ups",
        "Chest Dips (Leaning Forward / Torso Angled)",
        "Weighted Chest Dips",
        "Ring Push-ups (Gymnastic Rings)",
        "Ring Dips (Gymnastic Rings)"
    )

    @Test
    fun testChestCatalogContainsExactly61Items() {
        val chestItems = DefaultExercisesData.chestExerciseItems
        assertEquals(
            "Expected exactly 61 Chest exercises, but found ${chestItems.size}",
            61,
            chestItems.size
        )
    }

    @Test
    fun testZeroDuplicateIdsInChestCatalog() {
        val chestItems = DefaultExercisesData.chestExerciseItems
        val ids = chestItems.map { it.id }
        val duplicates = ids.groupBy { it }.filter { it.value.size > 1 }.keys
        assertTrue("Duplicate IDs found in chest catalog: $duplicates", duplicates.isEmpty())
    }

    @Test
    fun testZeroDuplicateNormalizedNamesInChestCatalog() {
        val chestItems = DefaultExercisesData.chestExerciseItems
        val normalizedNames = chestItems.map { it.name.trim().lowercase() }
        val duplicates = normalizedNames.groupBy { it }.filter { it.value.size > 1 }.keys
        assertTrue("Duplicate normalized names found in chest catalog: $duplicates", duplicates.isEmpty())
    }

    @Test
    fun testAllRequired61ChestExerciseNamesExist() {
        val chestItems = DefaultExercisesData.chestExerciseItems
        val namesSet = chestItems.map { it.name.trim() }.toSet()

        for (expected in expected61ChestExerciseNames) {
            assertTrue(
                "Missing required chest exercise: '$expected'",
                namesSet.contains(expected)
            )
        }
    }

    @Test
    fun testEquipmentCountsInChestCatalog() {
        val chestItems = DefaultExercisesData.chestExerciseItems

        val barbellCount = chestItems.count { it.equipment == "Barbell" }
        val dumbbellCount = chestItems.count { it.equipment == "Dumbbell" }
        val cableCount = chestItems.count { it.equipment == "Cable" }
        val machineCount = chestItems.count { it.equipment == "Machine" }
        val smithCount = chestItems.count { it.equipment == "Smith Machine" }
        val bodyweightCount = chestItems.count { it.equipment == "Bodyweight" }

        assertEquals("Expected 9 Barbell chest exercises", 9, barbellCount)
        assertEquals("Expected 15 Dumbbell chest exercises", 15, dumbbellCount)
        assertEquals("Expected 11 Cable chest exercises", 11, cableCount)
        assertEquals("Expected 9 Machine chest exercises", 9, machineCount)
        assertEquals("Expected 6 Smith Machine chest exercises", 6, smithCount)
        assertEquals("Expected 11 Bodyweight chest exercises", 11, bodyweightCount)
    }

    @Test
    fun testChestFilterMatchesAll61Exercises() {
        val chestEntities = DefaultExercisesData.chestExercises

        val filtered = chestEntities.filter { ex ->
            ex.primaryMuscle.contains("Chest", ignoreCase = true) ||
            ex.muscleGroup.contains("Chest", ignoreCase = true) ||
            ex.category.contains("Chest", ignoreCase = true)
        }

        assertEquals(
            "Every chest exercise must match the Chest category filter",
            61,
            filtered.size
        )
    }

    @Test
    fun testEquipmentFilterChipsMatchCleanly() {
        val chestEntities = DefaultExercisesData.chestExercises

        val equipmentFilters = listOf(
            "Barbell" to 9,
            "Dumbbell" to 15,
            "Cable" to 11,
            "Machine" to 9,
            "Smith Machine" to 6,
            "Bodyweight" to 11
        )

        for ((equipment, expectedCount) in equipmentFilters) {
            val matches = chestEntities.filter { exercise ->
                when (equipment) {
                    "Smith Machine" -> exercise.equipmentType.contains("Smith", ignoreCase = true) ||
                            exercise.equipmentCategory.contains("Smith", ignoreCase = true) ||
                            exercise.equipment.contains("Smith", ignoreCase = true) ||
                            exercise.name.contains("Smith", ignoreCase = true)
                    "Bodyweight" -> exercise.equipmentType.contains("Bodyweight", ignoreCase = true) ||
                            exercise.equipmentCategory.contains("Bodyweight", ignoreCase = true) ||
                            exercise.equipment.contains("Bodyweight", ignoreCase = true)
                    else -> exercise.equipmentType.equals(equipment, ignoreCase = true) ||
                            exercise.equipmentCategory.equals(equipment, ignoreCase = true) ||
                            exercise.equipment.equals(equipment, ignoreCase = true)
                }
            }

            assertEquals(
                "Filter for equipment '$equipment' should return $expectedCount items",
                expectedCount,
                matches.size
            )
        }
    }
}

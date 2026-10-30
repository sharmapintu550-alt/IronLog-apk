package com.example

import com.example.data.DefaultExercisesData
import com.example.data.entity.ExerciseEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ForearmAndTrapCatalogTest {

    private val expected16ForearmExerciseNames = listOf(
        // Barbell (4)
        "Barbell Wrist Curl (Palms Up / Forearm Flexors - Seated on Bench)",
        "Barbell Reverse Wrist Curl (Palms Down / Forearm Extensors)",
        "Behind-the-Back Barbell Wrist Curl (Standing)",
        "Standing Barbell Reverse Curl (Thumbless / Overhand Grip)",

        // Dumbbell (5)
        "Dumbbell Seated Wrist Curl (Palms Up - Single Arm)",
        "Dumbbell Seated Reverse Wrist Curl (Palms Down)",
        "Dumbbell Hammer Curl (Wrist / Brachioradialis Focus)",
        "Dumbbell Radial / Ulnar Deviation (Hammer Levering)",
        "Dumbbell Pinch Hold (Timed Grip)",

        // Cable (3)
        "Standing Cable Wrist Curl (Straight Bar / Low Pulley)",
        "Standing Cable Reverse Wrist Curl (Straight Bar)",
        "Cable Rope Wrist Extension",

        // Bodyweight & Specialty (4)
        "Dead Hang from Pull-up Bar (Timed Grip Strength)",
        "Towel Grip Dead Hang",
        "Plate Pinch Carry / Hold (Two Smooth Plates Together)",
        "Wrist Roller (Extension & Flexion)"
    )

    private val expected18TrapsExerciseNames = listOf(
        // Barbell (5)
        "Barbell Shrugs (Standard Overhand Grip)",
        "Barbell Shrugs (Behind the Back)",
        "Snatch-Grip Barbell Shrugs (Wide Grip - Upper Traps Focus)",
        "Barbell Power Shrugs (Slight Knee Dip & Pop)",
        "Landmine Shrugs (Single Arm)",

        // Dumbbell (4)
        "Dumbbell Shrugs (Standing Neutral Grip)",
        "Incline Prone Dumbbell Shrugs (Chest Supported - Mid Traps Focus)",
        "Dumbbell Overhead Shrug (Scapular Upward Elevation)",
        "Farmer's Walk / Carry (Heavy Dumbbells)",

        // Cable (2)
        "Cable Shrugs (Dual Low Pulleys / Rope)",
        "Cable Face Pull with High External Rotation (Traps & Rotator Cuff)",

        // Machine (3)
        "Plate-Loaded Shrug Machine (Neutral Handles)",
        "Seated Shrug Machine (Pin-Loaded)",
        "Calf Raise Machine Shoulder Shrugs (Padded Shoulders)",

        // Smith Machine (2)
        "Smith Machine Front Shrugs",
        "Smith Machine Behind-the-Back Shrugs",

        // Trap Bar / Hex Bar (2)
        "Trap Bar Shrugs (Neutral Grip)",
        "Trap Bar Farmer's Walk"
    )

    @Test
    fun testForearmCatalogContainsExactly16Items() {
        val forearmItems = DefaultExercisesData.forearmExerciseItems
        assertEquals(
            "Expected exactly 16 Forearms exercises, but found ${forearmItems.size}",
            16,
            forearmItems.size
        )
    }

    @Test
    fun testTrapsCatalogContainsExactly18Items() {
        val trapsItems = DefaultExercisesData.trapsExerciseItems
        assertEquals(
            "Expected exactly 18 Traps exercises, but found ${trapsItems.size}",
            18,
            trapsItems.size
        )
    }

    @Test
    fun testZeroDuplicateIdsInForearmAndTrapsCatalogs() {
        val forearmIds = DefaultExercisesData.forearmExerciseItems.map { it.id }
        val trapsIds = DefaultExercisesData.trapsExerciseItems.map { it.id }

        val forearmDuplicates = forearmIds.groupBy { it }.filter { it.value.size > 1 }.keys
        assertTrue("Duplicate IDs found in forearm exercises: $forearmDuplicates", forearmDuplicates.isEmpty())

        val trapsDuplicates = trapsIds.groupBy { it }.filter { it.value.size > 1 }.keys
        assertTrue("Duplicate IDs found in traps exercises: $trapsDuplicates", trapsDuplicates.isEmpty())

        val crossOverlap = forearmIds.intersect(trapsIds.toSet())
        assertTrue("Overlapping IDs between forearms and traps: $crossOverlap", crossOverlap.isEmpty())
    }

    @Test
    fun testZeroDuplicateNormalizedNamesInForearmsAndTraps() {
        val forearmNames = DefaultExercisesData.forearmExerciseItems.map { it.name.trim().lowercase() }
        val trapsNames = DefaultExercisesData.trapsExerciseItems.map { it.name.trim().lowercase() }

        val forearmDuplicates = forearmNames.groupBy { it }.filter { it.value.size > 1 }.keys
        assertTrue("Duplicate names in forearms: $forearmDuplicates", forearmDuplicates.isEmpty())

        val trapsDuplicates = trapsNames.groupBy { it }.filter { it.value.size > 1 }.keys
        assertTrue("Duplicate names in traps: $trapsDuplicates", trapsDuplicates.isEmpty())
    }

    @Test
    fun testAllForearmExpectedNamesArePresent() {
        val actualNames = DefaultExercisesData.forearmExerciseItems.map { it.name }.toSet()
        for (expected in expected16ForearmExerciseNames) {
            assertTrue("Expected forearm exercise '$expected' was not found", actualNames.contains(expected))
        }
    }

    @Test
    fun testAllTrapsExpectedNamesArePresent() {
        val actualNames = DefaultExercisesData.trapsExerciseItems.map { it.name }.toSet()
        for (expected in expected18TrapsExerciseNames) {
            assertTrue("Expected traps exercise '$expected' was not found", actualNames.contains(expected))
        }
    }

    @Test
    fun testForearmEquipmentDistribution() {
        val forearmItems = DefaultExercisesData.forearmExerciseItems
        val barbellCount = forearmItems.count { it.equipment == "Barbell" }
        val dumbbellCount = forearmItems.count { it.equipment == "Dumbbell" }
        val cableCount = forearmItems.count { it.equipment == "Cable" }
        val bodyweightCount = forearmItems.count { it.equipment == "Bodyweight" }
        val otherCount = forearmItems.count { it.equipment == "Other" }

        assertEquals("Expected 4 Barbell forearm exercises", 4, barbellCount)
        assertEquals("Expected 5 Dumbbell forearm exercises", 5, dumbbellCount)
        assertEquals("Expected 3 Cable forearm exercises", 3, cableCount)
        assertEquals("Expected 2 Bodyweight forearm exercises", 2, bodyweightCount)
        assertEquals("Expected 2 Other forearm exercises", 2, otherCount)
        assertEquals(16, barbellCount + dumbbellCount + cableCount + bodyweightCount + otherCount)
    }

    @Test
    fun testTrapsEquipmentDistribution() {
        val trapsItems = DefaultExercisesData.trapsExerciseItems
        val barbellCount = trapsItems.count { it.equipment == "Barbell" }
        val dumbbellCount = trapsItems.count { it.equipment == "Dumbbell" }
        val cableCount = trapsItems.count { it.equipment == "Cable" }
        val machineCount = trapsItems.count { it.equipment == "Machine" }
        val smithCount = trapsItems.count { it.equipment == "Smith Machine" }

        assertEquals("Expected 7 Barbell / Hex Bar traps exercises", 7, barbellCount)
        assertEquals("Expected 4 Dumbbell traps exercises", 4, dumbbellCount)
        assertEquals("Expected 2 Cable traps exercises", 2, cableCount)
        assertEquals("Expected 3 Machine traps exercises", 3, machineCount)
        assertEquals("Expected 2 Smith Machine traps exercises", 2, smithCount)
        assertEquals(18, barbellCount + dumbbellCount + cableCount + machineCount + smithCount)
    }

    @Test
    fun testCrossCatalogNonCollisionWithOtherMuscleGroups() {
        val all = DefaultExercisesData.allDefaultExercises
        val allIds = all.map { it.id }
        val idDuplicates = allIds.groupBy { it }.filter { it.value.size > 1 }.keys
        assertTrue("Duplicate IDs detected across full catalog: $idDuplicates", idDuplicates.isEmpty())

        val allNames = all.map { it.name.trim().lowercase() }
        val nameDuplicates = allNames.groupBy { it }.filter { it.value.size > 1 }.keys
        assertTrue("Duplicate normalized names detected across full catalog: $nameDuplicates", nameDuplicates.isEmpty())
    }

    @Test
    fun testForearmsAndTrapsFilterChipsResolution() {
        val all = DefaultExercisesData.allDefaultExercises

        // Filter Forearms
        val forearmsFilter = all.filter { ex ->
            ex.muscleGroup.contains("Forearm", ignoreCase = true) ||
            ex.primaryMuscle.contains("Forearm", ignoreCase = true) ||
            ex.category.contains("Forearm", ignoreCase = true) ||
            ex.secondaryMuscles.any { it.contains("Forearm", ignoreCase = true) || it.contains("Grip", ignoreCase = true) } ||
            ex.name.contains("Wrist Curl", ignoreCase = true) ||
            ex.name.contains("Dead Hang", ignoreCase = true) ||
            ex.name.contains("Plate Pinch", ignoreCase = true) ||
            ex.name.contains("Wrist Roller", ignoreCase = true)
        }

        for (expected in expected16ForearmExerciseNames) {
            assertTrue(
                "Filter 'Forearms' should include '$expected'",
                forearmsFilter.any { it.name == expected }
            )
        }

        // Filter Traps
        val trapsFilter = all.filter { ex ->
            ex.muscleGroup.contains("Trap", ignoreCase = true) ||
            ex.primaryMuscle.contains("Trap", ignoreCase = true) ||
            ex.category.contains("Trap", ignoreCase = true) ||
            ex.secondaryMuscles.any { it.contains("Trap", ignoreCase = true) } ||
            ex.name.contains("Shrug", ignoreCase = true)
        }

        for (expected in expected18TrapsExerciseNames) {
            assertTrue(
                "Filter 'Traps' should include '$expected'",
                trapsFilter.any { it.name == expected }
            )
        }
    }

    @Test
    fun testForearmsAndTrapsEntitiesProperlyMapped() {
        val forearmEntities = DefaultExercisesData.forearmExercises
        assertEquals(16, forearmEntities.size)
        for (entity in forearmEntities) {
            assertNotNull(entity.id)
            assertNotNull(entity.name)
            assertTrue(entity.primaryMuscle.isNotBlank())
            assertEquals("WEIGHT_AND_REPS", entity.trackingType)
            assertFalse(entity.isCustom)
        }

        val trapsEntities = DefaultExercisesData.trapsExercises
        assertEquals(18, trapsEntities.size)
        for (entity in trapsEntities) {
            assertNotNull(entity.id)
            assertNotNull(entity.name)
            assertTrue(entity.primaryMuscle.isNotBlank())
            assertEquals("WEIGHT_AND_REPS", entity.trackingType)
            assertFalse(entity.isCustom)
        }
    }
}

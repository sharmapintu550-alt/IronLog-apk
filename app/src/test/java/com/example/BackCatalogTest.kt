package com.example

import com.example.data.DefaultExercisesData
import com.example.data.entity.ExerciseEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BackCatalogTest {

    private val expected62BackExerciseNames = listOf(
        // 1. BARBELL (12)
        "Conventional Deadlift",
        "Barbell Bent-Over Row (Overhand / Pronated Grip)",
        "Barbell Bent-Over Row (Underhand / Supinated Grip / Yates Row)",
        "Pendlay Row (Strict Dead-Stop from Floor)",
        "T-Bar Row (Barbell Corner / Landmine - Neutral Grip)",
        "T-Bar Row (Wide Overhand Grip)",
        "Barbell Shrugs (Traps Focus)",
        "Behind-the-Back Barbell Shrugs",
        "Barbell Good Mornings (Lower Back Focus)",
        "Romanian Deadlift (Back & Posterior Chain Focus)",
        "Snatch-Grip Deadlift (Upper Back & Traps)",
        "Meadow's Row (Single-Arm Landmine Row)",

        // 2. DUMBBELL (10)
        "Single-Arm Dumbbell Row (Knee on Bench)",
        "Single-Arm Dumbbell Row (Standing Supported)",
        "Chest-Supported Incline Dumbbell Row (Neutral Grip)",
        "Chest-Supported Incline Dumbbell Row (Elbows Flared / Upper Back)",
        "Dumbbell Pullover (Lat Focus)",
        "Dumbbell Deadlift",
        "Dumbbell Shrugs (Neutral Grip)",
        "Incline Dumbbell Shrugs",
        "Dumbbell Seal Row (Flat Bench Elevated)",
        "Incline Prone Dumbbell Row",

        // 3. CABLE (14)
        "Lat Pulldown (Wide Overhand Grip)",
        "Lat Pulldown (Close Neutral Grip / V-Bar)",
        "Lat Pulldown (Underhand / Supinated Grip)",
        "Lat Pulldown (Behind-the-Neck)",
        "Single-Arm Half-Kneeling Lat Pulldown",
        "Seated Cable Row (Close Neutral Grip / V-Bar)",
        "Seated Cable Row (Wide Grip Bar - Overhand)",
        "Seated Cable Row (Wide Neutral Grip)",
        "Single-Arm Seated Cable Row (Lat Sweep Focus)",
        "Standing Straight-Arm Cable Pulldown (Rope Attachment)",
        "Standing Straight-Arm Cable Pulldown (Straight Bar)",
        "Cable Face Pull (Rope - Upper Traps & Rear Delts)",
        "High Cable Row (Upper Lat / Mid Back)",
        "Cable Shrugs (Traps Focus)",

        // 4. MACHINE (9)
        "Plate-Loaded Iso-Lateral Front Lat Pulldown",
        "Plate-Loaded Iso-Lateral High Row",
        "Plate-Loaded Iso-Lateral Low Row",
        "Seated Machine Row (Chest Supported - Neutral Grip)",
        "Seated Machine Row (Wide Overhand Grip - Upper Back Focus)",
        "Machine Lat Pulldown (Dual Axis / Pin-Loaded)",
        "T-Bar Row Machine (Chest Supported)",
        "Machine Shrug (Seated or Standing)",
        "Back Extension Machine (Hyperextension)",

        // 5. SMITH MACHINE (6)
        "Smith Machine Bent-Over Row (Overhand Grip)",
        "Smith Machine Bent-Over Row (Underhand Grip)",
        "Smith Machine Romanian Deadlift",
        "Smith Machine Shrugs (Front)",
        "Smith Machine Shrugs (Behind the Back)",
        "Smith Machine Inverted Bodyweight Row",

        // 6. BODYWEIGHT / CALISTHENICS (11)
        "Pull-ups (Standard Overhand Grip)",
        "Pull-ups (Wide Grip)",
        "Chin-ups (Underhand / Supinated Grip)",
        "Neutral-Grip Pull-ups (Parallel Grips)",
        "Weighted Pull-ups",
        "Weighted Chin-ups",
        "Inverted Rows (Australian Pull-ups)",
        "Inverted Rows (Elevated Feet)",
        "Back Hyperextensions (45 Degree Bench)",
        "Weighted Hyperextensions (Holding Plate)",
        "Superman Holds (Floor)"
    )

    @Test
    fun testBackCatalogContainsExactly62Items() {
        val backItems = DefaultExercisesData.backExerciseItems
        assertEquals(
            "Expected exactly 62 Back exercises, but found ${backItems.size}",
            62,
            backItems.size
        )
    }

    @Test
    fun testZeroDuplicateIdsInBackCatalog() {
        val backItems = DefaultExercisesData.backExerciseItems
        val ids = backItems.map { it.id }
        val duplicates = ids.groupBy { it }.filter { it.value.size > 1 }.keys
        assertTrue("Duplicate IDs found in back catalog: $duplicates", duplicates.isEmpty())
    }

    @Test
    fun testZeroDuplicateNormalizedNamesInBackCatalog() {
        val backItems = DefaultExercisesData.backExerciseItems
        val normalizedNames = backItems.map { it.name.trim().lowercase() }
        val duplicates = normalizedNames.groupBy { it }.filter { it.value.size > 1 }.keys
        assertTrue("Duplicate normalized names found in back catalog: $duplicates", duplicates.isEmpty())
    }

    @Test
    fun testAllRequired62BackExerciseNamesExist() {
        val backItems = DefaultExercisesData.backExerciseItems
        val namesSet = backItems.map { it.name.trim() }.toSet()

        for (expected in expected62BackExerciseNames) {
            assertTrue(
                "Missing required back exercise: '$expected'",
                namesSet.contains(expected)
            )
        }
    }

    @Test
    fun testEquipmentCountsInBackCatalog() {
        val backItems = DefaultExercisesData.backExerciseItems

        val barbellCount = backItems.count { it.equipment == "Barbell" }
        val dumbbellCount = backItems.count { it.equipment == "Dumbbell" }
        val cableCount = backItems.count { it.equipment == "Cable" }
        val machineCount = backItems.count { it.equipment == "Machine" }
        val smithCount = backItems.count { it.equipment == "Smith Machine" }
        val bodyweightCount = backItems.count { it.equipment == "Bodyweight" }

        assertEquals("Expected 12 Barbell back exercises", 12, barbellCount)
        assertEquals("Expected 10 Dumbbell back exercises", 10, dumbbellCount)
        assertEquals("Expected 14 Cable back exercises", 14, cableCount)
        assertEquals("Expected 9 Machine back exercises", 9, machineCount)
        assertEquals("Expected 6 Smith Machine back exercises", 6, smithCount)
        assertEquals("Expected 11 Bodyweight back exercises", 11, bodyweightCount)
    }

    @Test
    fun testBackFilterMatchesAll62Exercises() {
        val backEntities = DefaultExercisesData.backExercises

        val filtered = backEntities.filter { ex ->
            ex.primaryMuscle.contains("Back", ignoreCase = true) ||
            ex.primaryMuscle.contains("Lat", ignoreCase = true) ||
            ex.primaryMuscle.contains("Trap", ignoreCase = true) ||
            ex.muscleGroup.contains("Back", ignoreCase = true) ||
            ex.category.contains("Back", ignoreCase = true)
        }

        assertEquals(
            "Every back exercise must match the Back category filter",
            62,
            filtered.size
        )
    }

    @Test
    fun testChestCatalogPreservedIntactAt61Items() {
        val chestItems = DefaultExercisesData.chestExerciseItems
        assertEquals("Chest catalog must remain 61 items intact", 61, chestItems.size)
    }

    @Test
    fun testEquipmentFilterChipsMatchCleanlyForBack() {
        val backEntities = DefaultExercisesData.backExercises

        val equipmentFilters = listOf(
            "Barbell" to 12,
            "Dumbbell" to 10,
            "Cable" to 14,
            "Machine" to 9,
            "Smith Machine" to 6,
            "Bodyweight" to 11
        )

        for ((equipment, expectedCount) in equipmentFilters) {
            val matches = backEntities.filter { exercise ->
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

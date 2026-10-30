package com.example

import com.example.data.DefaultExercisesData
import com.example.data.entity.ExerciseEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ShoulderCatalogTest {

    private val expected51ShoulderExerciseNames = listOf(
        // 1. BARBELL (10)
        "Standing Overhead Press (OHP / Military Press)",
        "Seated Overhead Barbell Press",
        "Behind-the-Neck Barbell Press",
        "Barbell Upright Row (Wide Grip - Lateral Delt Focus)",
        "Barbell Upright Row (Standard Grip)",
        "Barbell Front Raise (Overhand Grip)",
        "Landmine Shoulder Press (Single-Arm)",
        "Landmine Shoulder Press (Two-Handed)",
        "Push Press (Barbell Power)",
        "Barbell Rear Delt Row (Wide Grip / 45-degree Torso)",

        // 2. DUMBBELL (15)
        "Seated Dumbbell Shoulder Press (Pronated Grip)",
        "Seated Dumbbell Shoulder Press (Neutral / Hammer Grip)",
        "Standing Dumbbell Overhead Press",
        "Arnold Press (Rotating Grip)",
        "Standing Dumbbell Lateral Raise (Side Delts)",
        "Seated Dumbbell Lateral Raise",
        "Incline Leaning Dumbbell Lateral Raise (Single-Arm)",
        "Bent-Over Dumbbell Rear Delt Fly (Seated)",
        "Bent-Over Dumbbell Rear Delt Fly (Standing)",
        "Chest-Supported Incline Rear Delt Fly (Dumbbell)",
        "Dumbbell Front Raise (Alternating Pronated)",
        "Dumbbell Front Raise (Neutral Grip / Hammer)",
        "Incline Dumbbell Front Raise",
        "Dumbbell Upright Row",
        "Dumbbell Lu Raises (Full Overhead Lateral Raise)",

        // 3. CABLE (11)
        "Cable Lateral Raise (Behind the Back)",
        "Cable Lateral Raise (In Front of Body)",
        "Cable Lateral Raise (Cuff Attachment at Wrist)",
        "Dual Cable Lateral Raise (Cross-Body)",
        "Cable Face Pull (Rope - Rear Delts / High Angle)",
        "Single-Arm Cable Rear Delt Fly (No Handle / At Ear Height)",
        "Dual Cable Rear Delt Fly (Reverse Cable Crossover)",
        "Cable Upright Row (Rope / Straight Bar)",
        "Cable Front Raise (Straight Bar / Low Pulley)",
        "Single-Arm Cable Front Raise (Rope / Low Pulley)",
        "Half-Kneeling Single-Arm Cable Shoulder Press",

        // 4. MACHINE (6)
        "Machine Shoulder Press (Pin-Loaded - Neutral Grip)",
        "Machine Shoulder Press (Pin-Loaded - Overhand Grip)",
        "Plate-Loaded Iso-Lateral Shoulder Press",
        "Rear Delt Machine Fly (Reverse Pec Deck)",
        "Machine Lateral Raise (Seated Pad-Loaded)",
        "Plate-Loaded Lateral Raise Machine",

        // 5. SMITH MACHINE (5)
        "Smith Machine Seated Overhead Press (Front)",
        "Smith Machine Behind-the-Neck Press",
        "Smith Machine Standing Overhead Press",
        "Smith Machine Upright Row (Wide Grip)",
        "Smith Machine Rear Delt Row",

        // 6. BODYWEIGHT / CALISTHENICS (4)
        "Pike Push-ups (Shoulder Focus)",
        "Elevated Feet Pike Push-ups",
        "Handstand Push-ups (Wall-Supported)",
        "Handstand Hold (Wall-Supported Static)"
    )

    @Test
    fun testShoulderCatalogContainsExactly51Items() {
        val shoulderItems = DefaultExercisesData.shoulderExerciseItems
        assertEquals(
            "Expected exactly 51 Shoulder exercises, but found ${shoulderItems.size}",
            51,
            shoulderItems.size
        )
    }

    @Test
    fun testZeroDuplicateIdsInShoulderCatalog() {
        val shoulderItems = DefaultExercisesData.shoulderExerciseItems
        val ids = shoulderItems.map { it.id }
        val duplicates = ids.groupBy { it }.filter { it.value.size > 1 }.keys
        assertTrue("Duplicate IDs found in shoulder exercises: $duplicates", duplicates.isEmpty())
        assertEquals(51, ids.toSet().size)
    }

    @Test
    fun testZeroDuplicateNormalizedNamesInShoulderCatalog() {
        val shoulderItems = DefaultExercisesData.shoulderExerciseItems
        val normalizedNames = shoulderItems.map { it.name.trim().lowercase() }
        val duplicates = normalizedNames.groupBy { it }.filter { it.value.size > 1 }.keys
        assertTrue("Duplicate names found in shoulder exercises: $duplicates", duplicates.isEmpty())
        assertEquals(51, normalizedNames.toSet().size)
    }

    @Test
    fun testAll51ExpectedNamesArePresent() {
        val actualNames = DefaultExercisesData.shoulderExerciseItems.map { it.name }.toSet()
        for (expected in expected51ShoulderExerciseNames) {
            assertTrue("Expected shoulder exercise '$expected' was not found in catalog", actualNames.contains(expected))
        }
    }

    @Test
    fun testEquipmentDistributionMatchesSpecification() {
        val shoulderItems = DefaultExercisesData.shoulderExerciseItems

        val barbellCount = shoulderItems.count { it.equipment.equals("Barbell", ignoreCase = true) }
        val dumbbellCount = shoulderItems.count { it.equipment.equals("Dumbbell", ignoreCase = true) }
        val cableCount = shoulderItems.count { it.equipment.equals("Cable", ignoreCase = true) }
        val machineCount = shoulderItems.count { it.equipment.equals("Machine", ignoreCase = true) }
        val smithMachineCount = shoulderItems.count { it.equipment.equals("Smith Machine", ignoreCase = true) }
        val bodyweightCount = shoulderItems.count { it.equipment.equals("Bodyweight", ignoreCase = true) }

        assertEquals("Barbell shoulder exercises count should be 10", 10, barbellCount)
        assertEquals("Dumbbell shoulder exercises count should be 15", 15, dumbbellCount)
        assertEquals("Cable shoulder exercises count should be 11", 11, cableCount)
        assertEquals("Machine shoulder exercises count should be 6", 6, machineCount)
        assertEquals("Smith Machine shoulder exercises count should be 5", 5, smithMachineCount)
        assertEquals("Bodyweight shoulder exercises count should be 4", 4, bodyweightCount)
    }

    @Test
    fun testShoulderEntitiesMappedCorrectly() {
        val entities: List<ExerciseEntity> = DefaultExercisesData.shoulderExercises
        assertEquals(51, entities.size)

        for (entity in entities) {
            assertNotNull(entity.id)
            assertNotNull(entity.name)
            assertTrue("Muscle group should reference Shoulders: ${entity.muscleGroup}",
                entity.muscleGroup.contains("Shoulder", ignoreCase = true))
            assertFalse("Default catalog exercises should never be marked as custom", entity.isCustom)
            assertTrue("Equipment must not be blank", entity.equipment.isNotBlank())
            assertTrue("Category must not be blank", entity.category.isNotBlank())
        }
    }

    @Test
    fun testDeduplicationNormalizationLogic() {
        val rawName = "  standing overhead press (ohp / military press)   "
        val normalized = rawName.trim().lowercase()
        val match = DefaultExercisesData.shoulderExerciseItems.find {
            it.name.trim().lowercase() == normalized
        }
        assertNotNull("Normalized matching should resolve '$rawName'", match)
        assertEquals("shoulder_bb_ohp_standing", match?.id)
    }

    @Test
    fun testPreservationOfChestAndBackCatalogs() {
        // Protect Part 1 (Chest - 61 exercises)
        assertEquals(61, DefaultExercisesData.chestExerciseItems.size)
        val chestIds = DefaultExercisesData.chestExerciseItems.map { it.id }.toSet()
        assertEquals(61, chestIds.size)

        // Protect Part 2 (Back - 62 exercises)
        assertEquals(62, DefaultExercisesData.backExerciseItems.size)
        val backIds = DefaultExercisesData.backExerciseItems.map { it.id }.toSet()
        assertEquals(62, backIds.size)

        // Ensure no ID overlap between Chest, Back, and Shoulders
        val shoulderIds = DefaultExercisesData.shoulderExerciseItems.map { it.id }.toSet()
        val chestShoulderIntersect = chestIds.intersect(shoulderIds)
        val backShoulderIntersect = backIds.intersect(shoulderIds)
        assertTrue("No ID overlap between Chest and Shoulders", chestShoulderIntersect.isEmpty())
        assertTrue("No ID overlap between Back and Shoulders", backShoulderIntersect.isEmpty())
    }
}

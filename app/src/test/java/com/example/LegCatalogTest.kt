package com.example

import com.example.data.DefaultExercisesData
import com.example.data.entity.ExerciseEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LegCatalogTest {

    private val expected69LegExerciseNames = listOf(
        // 1. BARBELL (13)
        "Barbell Back Squat (High Bar)",
        "Barbell Back Squat (Low Bar)",
        "Barbell Front Squat (Clean Grip / Cross-Arm)",
        "Barbell Romanian Deadlift (RDL - Hamstrings/Glutes)",
        "Barbell Stiff-Legged Deadlift (SLDL)",
        "Sumo Deadlift (Barbell)",
        "Barbell Hip Thrust (Glutes Focus)",
        "Barbell Good Mornings (Hamstrings & Posterior Chain)",
        "Barbell Walking Lunges",
        "Barbell Reverse Lunges",
        "Zercher Squat (Barbell in Crook of Elbows)",
        "Barbell Standing Calf Raise",
        "Barbell Box Squats",

        // 2. DUMBBELL (12)
        "Dumbbell Romanian Deadlift (RDL)",
        "Dumbbell Bulgarian Split Squat (Rear Foot Elevated)",
        "Dumbbell Goblet Squat (Heels Flat)",
        "Dumbbell Goblet Squat (Heels Elevated - Quad Focus)",
        "Dumbbell Walking Lunges",
        "Dumbbell Reverse Lunges",
        "Dumbbell Step-ups (Bench / Box)",
        "Dumbbell Sumo Squat (Plie Squat)",
        "Dumbbell Single-Leg Romanian Deadlift",
        "Dumbbell Standing Calf Raise (Single-Leg)",
        "Dumbbell Lying Hamstring Curl (Dumbbell Between Feet)",
        "Dumbbell Seated Calf Raise (Dumbbells on Knees)",

        // 3. CABLE (7)
        "Cable Pull-Through (Glutes & Hamstrings)",
        "Cable Glute Kickbacks (Ankle Strap)",
        "Cable Standing Hip Abduction (Ankle Strap)",
        "Cable Standing Hip Adduction (Ankle Strap)",
        "Cable Goblet Squat",
        "Cable Romanian Deadlift (Straight Bar / Rope)",
        "Single-Leg Cable Hamstring Curl (Standing)",

        // 4. MACHINE (19)
        "Leg Press (45 Degree Sled - Standard Foot Placement)",
        "Leg Press (High & Wide Stance - Glute/Hamstring Focus)",
        "Leg Press (Low & Narrow Stance - Quad Sweep Focus)",
        "Single-Leg 45 Degree Leg Press",
        "Hack Squat Machine (Standard Stance)",
        "Hack Squat Machine (Narrow Stance / Quad Focus)",
        "Reverse Hack Squat (Glutes Focus)",
        "Leg Extension Machine (Quads)",
        "Single-Leg Extension Machine",
        "Lying Leg Curl Machine (Hamstrings)",
        "Seated Leg Curl Machine (Hamstrings)",
        "Standing Single-Leg Curl Machine",
        "Hip Abduction Machine (Outer Thighs / Glute Medius)",
        "Hip Adduction Machine (Inner Thighs / Groin)",
        "Standing Calf Raise Machine",
        "Seated Calf Raise Machine (Soleus Focus)",
        "Donkey Calf Raise Machine",
        "Glute Ham Developer (GHD) Raise",
        "Machine Glute Kickback / Plate-Loaded Glute Press",

        // 5. SMITH MACHINE (8)
        "Smith Machine Back Squat",
        "Smith Machine Front Squat",
        "Smith Machine Bulgarian Split Squat",
        "Smith Machine Romanian Deadlift (RDL)",
        "Smith Machine Hip Thrust",
        "Smith Machine Good Mornings",
        "Smith Machine Standing Calf Raise (Elevated on Block)",
        "Smith Machine Reverse Lunges",

        // 6. BODYWEIGHT / CALISTHENICS (10)
        "Bodyweight Air Squats",
        "Bodyweight Bulgarian Split Squats",
        "Bodyweight Walking Lunges",
        "Sissy Squats (Bodyweight Quad Focus)",
        "Nordic Hamstring Curls (Bodyweight / Partner-Assisted)",
        "Single-Leg Glute Bridges (Floor)",
        "Double-Leg Glute Bridges (Floor)",
        "Standing Single-Leg Calf Raises (Deficit on Step)",
        "Pistol Squats (Single-Leg Squats)",
        "Jump Squats (Plyometric)"
    )

    @Test
    fun testLegCatalogContainsExactly69Items() {
        val legItems = DefaultExercisesData.legExerciseItems
        assertEquals(
            "Expected exactly 69 Leg exercises, but found ${legItems.size}",
            69,
            legItems.size
        )
    }

    @Test
    fun testZeroDuplicateIdsInLegCatalog() {
        val legItems = DefaultExercisesData.legExerciseItems
        val ids = legItems.map { it.id }
        val duplicates = ids.groupBy { it }.filter { it.value.size > 1 }.keys
        assertTrue("Duplicate IDs found in leg exercises: $duplicates", duplicates.isEmpty())
        assertEquals(69, ids.toSet().size)
    }

    @Test
    fun testZeroDuplicateNormalizedNamesInLegCatalog() {
        val legItems = DefaultExercisesData.legExerciseItems
        val normalizedNames = legItems.map { it.name.trim().lowercase() }
        val duplicates = normalizedNames.groupBy { it }.filter { it.value.size > 1 }.keys
        assertTrue("Duplicate names found in leg exercises: $duplicates", duplicates.isEmpty())
        assertEquals(69, normalizedNames.toSet().size)
    }

    @Test
    fun testAll69ExpectedNamesArePresent() {
        val actualNames = DefaultExercisesData.legExerciseItems.map { it.name }.toSet()
        for (expected in expected69LegExerciseNames) {
            assertTrue("Expected leg exercise '$expected' was not found in catalog", actualNames.contains(expected))
        }
    }

    @Test
    fun testEquipmentDistributionMatchesSpecification() {
        val legItems = DefaultExercisesData.legExerciseItems

        val barbellCount = legItems.count { it.equipment.equals("Barbell", ignoreCase = true) }
        val dumbbellCount = legItems.count { it.equipment.equals("Dumbbell", ignoreCase = true) }
        val cableCount = legItems.count { it.equipment.equals("Cable", ignoreCase = true) }
        val machineCount = legItems.count { it.equipment.equals("Machine", ignoreCase = true) }
        val smithMachineCount = legItems.count { it.equipment.equals("Smith Machine", ignoreCase = true) }
        val bodyweightCount = legItems.count { it.equipment.equals("Bodyweight", ignoreCase = true) }

        assertEquals("Barbell leg exercises count should be 13", 13, barbellCount)
        assertEquals("Dumbbell leg exercises count should be 12", 12, dumbbellCount)
        assertEquals("Cable leg exercises count should be 7", 7, cableCount)
        assertEquals("Machine leg exercises count should be 19", 19, machineCount)
        assertEquals("Smith Machine leg exercises count should be 8", 8, smithMachineCount)
        assertEquals("Bodyweight leg exercises count should be 10", 10, bodyweightCount)
    }

    @Test
    fun testLegEntitiesMappedCorrectly() {
        val entities: List<ExerciseEntity> = DefaultExercisesData.legExercises
        assertEquals(69, entities.size)

        for (entity in entities) {
            assertNotNull(entity.id)
            assertNotNull(entity.name)
            assertTrue("Muscle group should reference Legs: ${entity.muscleGroup}",
                entity.muscleGroup.contains("Leg", ignoreCase = true))
            assertFalse("Default catalog exercises should never be marked as custom", entity.isCustom)
            assertTrue("Equipment must not be blank", entity.equipment.isNotBlank())
            assertTrue("Category must not be blank", entity.category.isNotBlank())
        }
    }

    @Test
    fun testDeduplicationNormalizationLogic() {
        val rawName = "   barbell back squat (high bar)   "
        val normalized = rawName.trim().lowercase()
        val match = DefaultExercisesData.legExerciseItems.find {
            it.name.trim().lowercase() == normalized
        }
        assertNotNull("Normalized matching should resolve '$rawName'", match)
        assertEquals("legs_bb_back_squat_high_bar", match?.id)
    }

    @Test
    fun testPreservationOfChestBackAndShoulderCatalogs() {
        // Protect Part 1 (Chest - 61 exercises)
        assertEquals(61, DefaultExercisesData.chestExerciseItems.size)
        val chestIds = DefaultExercisesData.chestExerciseItems.map { it.id }.toSet()
        assertEquals(61, chestIds.size)

        // Protect Part 2 (Back - 62 exercises)
        assertEquals(62, DefaultExercisesData.backExerciseItems.size)
        val backIds = DefaultExercisesData.backExerciseItems.map { it.id }.toSet()
        assertEquals(62, backIds.size)

        // Protect Part 3 (Shoulders - 51 exercises)
        assertEquals(51, DefaultExercisesData.shoulderExerciseItems.size)
        val shoulderIds = DefaultExercisesData.shoulderExerciseItems.map { it.id }.toSet()
        assertEquals(51, shoulderIds.size)

        // Ensure zero ID collisions between Legs and other categories
        val legIds = DefaultExercisesData.legExerciseItems.map { it.id }.toSet()
        assertTrue("No ID overlap between Chest and Legs", chestIds.intersect(legIds).isEmpty())
        assertTrue("No ID overlap between Back and Legs", backIds.intersect(legIds).isEmpty())
        assertTrue("No ID overlap between Shoulders and Legs", shoulderIds.intersect(legIds).isEmpty())
    }
}

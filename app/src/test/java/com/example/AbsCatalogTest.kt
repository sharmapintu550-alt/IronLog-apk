package com.example

import com.example.data.DefaultExercisesData
import com.example.data.entity.ExerciseEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AbsCatalogTest {

    private val expected42AbsExerciseNames = listOf(
        // 1. BODYWEIGHT / CALISTHENICS (19)
        "Hanging Leg Raises (Straight Legs - Bar)",
        "Hanging Knee Raises (Bar)",
        "Captain's Chair Leg Raises (Dip Station)",
        "Captain's Chair Knee Raises",
        "Hanging Windshield Wipers (Obliques)",
        "Lying Leg Raises (Floor)",
        "Reverse Crunches (Floor / Flat Bench)",
        "Incline Bench Reverse Crunches",
        "Standard Floor Crunches",
        "Bicycle Crunches (Obliques)",
        "Dead Bug (Core Stabilization)",
        "Bird Dog (Posterior & Core Anti-Rotation)",
        "Standard Plank (Elbows on Floor)",
        "Side Plank (Floor)",
        "Hollow Body Hold",
        "V-Ups (Full Body Fold)",
        "Russian Twists (Bodyweight)",
        "Mountain Climbers (Core Pacing)",
        "Dragon Flag (Bodyweight Bench)",

        // 2. CABLE (7)
        "Kneeling Cable Crunch (Rope Attachment - Rectus Abdominis)",
        "Standing Cable Woodchopper (High-to-Low - Obliques)",
        "Standing Cable Woodchopper (Low-to-High - Obliques)",
        "Standing Cable Woodchopper (Mid / Horizontal - Obliques)",
        "Cable Pallof Press (Anti-Rotation Core Brace)",
        "Cable Side Bend (Standing Oblique Contraction)",
        "Kneeling Cable Oblique Crunch (Single-Side)",

        // 3. MACHINE (5)
        "Seated Abdominal Crunch Machine (Pin-Loaded)",
        "Plate-Loaded Ab Crunch Machine",
        "Rotary Torso Machine (Obliques / Rotation)",
        "Roman Chair Sit-ups / Incline Sit-up Bench",
        "Hyperextension Side Bends (Obliques on 45° Bench)",

        // 4. DUMBBELL (5)
        "Dumbbell Side Bends (Standing Single-Side)",
        "Dumbbell Russian Twists (Holding Dumbbell)",
        "Weighted Incline Sit-ups (Dumbbell to Chest)",
        "Dumbbell Overhead Sit-ups",
        "Dumbbell Suitcase Carry (Unilateral Anti-Lateral Flexion)",

        // 5. BARBELL / OTHER (6)
        "Ab Wheel Rollout (From Knees - Anti-Extension)",
        "Ab Wheel Rollout (Standing Full Stretch)",
        "Barbell Rollout (Loaded Bar on Floor)",
        "Landmine 180s / Rotations (Obliques & Core)",
        "Barbell Suitcase Hold / Carry",
        "Weighted Hanging Leg Raises (Dumbbell Between Feet)"
    )

    @Test
    fun testAbsCatalogContainsExactly42Items() {
        val absItems = DefaultExercisesData.absExerciseItems
        assertEquals(
            "Expected exactly 42 Abs/Core exercises, but found ${absItems.size}",
            42,
            absItems.size
        )
    }

    @Test
    fun testZeroDuplicateIdsInAbsCatalog() {
        val absItems = DefaultExercisesData.absExerciseItems
        val ids = absItems.map { it.id }
        val duplicates = ids.groupBy { it }.filter { it.value.size > 1 }.keys
        assertTrue("Duplicate IDs found in abs exercises: $duplicates", duplicates.isEmpty())
        assertEquals(42, ids.toSet().size)
    }

    @Test
    fun testZeroDuplicateNormalizedNamesInAbsCatalog() {
        val absItems = DefaultExercisesData.absExerciseItems
        val normalizedNames = absItems.map { it.name.trim().lowercase() }
        val duplicates = normalizedNames.groupBy { it }.filter { it.value.size > 1 }.keys
        assertTrue("Duplicate names found in abs exercises: $duplicates", duplicates.isEmpty())
        assertEquals(42, normalizedNames.toSet().size)
    }

    @Test
    fun testAll42ExpectedNamesArePresent() {
        val actualNames = DefaultExercisesData.absExerciseItems.map { it.name }.toSet()
        for (expected in expected42AbsExerciseNames) {
            assertTrue("Expected exercise '$expected' was not found in catalog", actualNames.contains(expected))
        }
    }

    @Test
    fun testEquipmentDistributionMatchesSpecification() {
        val absItems = DefaultExercisesData.absExerciseItems

        val bodyweightCount = absItems.count { it.equipment.equals("Bodyweight", ignoreCase = true) }
        val cableCount = absItems.count { it.equipment.equals("Cable", ignoreCase = true) }
        val machineCount = absItems.count { it.equipment.equals("Machine", ignoreCase = true) }
        val dumbbellCount = absItems.count { it.equipment.equals("Dumbbell", ignoreCase = true) }
        val barbellCount = absItems.count { it.equipment.equals("Barbell", ignoreCase = true) }
        val otherCount = absItems.count { it.equipment.equals("Other", ignoreCase = true) }

        assertEquals("Bodyweight abs exercises count should be 19", 19, bodyweightCount)
        assertEquals("Cable abs exercises count should be 7", 7, cableCount)
        assertEquals("Machine abs exercises count should be 5", 5, machineCount)
        assertEquals("Dumbbell abs exercises count should be 6", 6, dumbbellCount)
        assertEquals("Barbell abs exercises count should be 3", 3, barbellCount)
        assertEquals("Other abs exercises count should be 2", 2, otherCount)
    }

    @Test
    fun testAbsEntitiesMappedCorrectly() {
        val entities: List<ExerciseEntity> = DefaultExercisesData.absExercises
        assertEquals(42, entities.size)

        for (entity in entities) {
            assertNotNull(entity.id)
            assertNotNull(entity.name)
            assertTrue(
                "Muscle group should reference Abs or Core: ${entity.muscleGroup}",
                entity.muscleGroup.contains("Abs", ignoreCase = true) || entity.muscleGroup.contains("Core", ignoreCase = true)
            )
            assertFalse("Default catalog exercises should never be marked as custom", entity.isCustom)
            assertTrue("Equipment must not be blank", entity.equipment.isNotBlank())
            assertTrue("Category must not be blank", entity.category.isNotBlank())
        }
    }

    @Test
    fun testDeduplicationNormalizationLogic() {
        val rawName = "   hanging leg raises (straight legs - bar)   "
        val normalized = rawName.trim().lowercase()
        val match = DefaultExercisesData.absExerciseItems.find {
            it.name.trim().lowercase() == normalized
        }
        assertNotNull("Normalized matching should resolve '$rawName'", match)
        assertEquals("abs_bw_hanging_leg_raise_straight", match?.id)
    }

    @Test
    fun testAbsFilterChipResolvesAllExercises() {
        val entities = DefaultExercisesData.absExercises
        val filtered = entities.filter { ex ->
            ex.muscleGroup.contains("Abs", ignoreCase = true) ||
            ex.muscleGroup.contains("Core", ignoreCase = true) ||
            ex.primaryMuscle.contains("Abs", ignoreCase = true) ||
            ex.primaryMuscle.contains("Core", ignoreCase = true) ||
            ex.category.contains("Abs", ignoreCase = true) ||
            ex.category.contains("Core", ignoreCase = true)
        }
        assertEquals("Selecting 'Abs' filter chip should resolve all 42 movements", 42, filtered.size)
    }

    @Test
    fun testPreservationOfAllOtherCatalogs() {
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

        // Protect Part 4 (Legs - 69 exercises)
        assertEquals(69, DefaultExercisesData.legExerciseItems.size)
        val legIds = DefaultExercisesData.legExerciseItems.map { it.id }.toSet()
        assertEquals(69, legIds.size)

        // Ensure zero ID collisions between Abs and any other category
        val absIds = DefaultExercisesData.absExerciseItems.map { it.id }.toSet()
        assertTrue("No ID overlap between Chest and Abs", chestIds.intersect(absIds).isEmpty())
        assertTrue("No ID overlap between Back and Abs", backIds.intersect(absIds).isEmpty())
        assertTrue("No ID overlap between Shoulders and Abs", shoulderIds.intersect(absIds).isEmpty())
        assertTrue("No ID overlap between Legs and Abs", legIds.intersect(absIds).isEmpty())
    }
}

package com.example

import com.example.data.DefaultExercisesData
import com.example.data.entity.ExerciseEntity
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExerciseFilterAndSeedTest {

    private fun filterExercises(
        allExercises: List<ExerciseEntity>,
        searchQuery: String = "",
        selectedCategoryFilter: String = "All",
        selectedEquipmentFilter: String = "All Equipment"
    ): List<ExerciseEntity> {
        return allExercises.filter { exercise ->
            val matchesQuery = searchQuery.isBlank() ||
                    exercise.name.contains(searchQuery, ignoreCase = true) ||
                    exercise.primaryMuscle.contains(searchQuery, ignoreCase = true) ||
                    exercise.muscleGroup.contains(searchQuery, ignoreCase = true) ||
                    exercise.category.contains(searchQuery, ignoreCase = true) ||
                    exercise.equipmentType.contains(searchQuery, ignoreCase = true)

            val matchesCategory = when (selectedCategoryFilter) {
                "All" -> true
                "Biceps" -> {
                    exercise.primaryMuscle.contains("Bicep", ignoreCase = true) ||
                    exercise.muscleGroup.contains("Bicep", ignoreCase = true) ||
                    exercise.category.contains("Bicep", ignoreCase = true) ||
                    exercise.name.contains("Bicep", ignoreCase = true) ||
                    ((exercise.primaryMuscle.contains("Arm", ignoreCase = true) ||
                      exercise.muscleGroup.contains("Arm", ignoreCase = true) ||
                      exercise.category.contains("Arm", ignoreCase = true)) &&
                     (exercise.name.contains("curl", ignoreCase = true) ||
                      exercise.name.contains("bicep", ignoreCase = true) ||
                      exercise.name.contains("chin-up", ignoreCase = true) ||
                      exercise.name.contains("chin up", ignoreCase = true) ||
                      exercise.name.contains("zottman", ignoreCase = true)))
                }
                "Triceps" -> {
                    exercise.primaryMuscle.contains("Tricep", ignoreCase = true) ||
                    exercise.muscleGroup.contains("Tricep", ignoreCase = true) ||
                    exercise.category.contains("Tricep", ignoreCase = true) ||
                    exercise.name.contains("Tricep", ignoreCase = true) ||
                    ((exercise.primaryMuscle.contains("Arm", ignoreCase = true) ||
                      exercise.muscleGroup.contains("Arm", ignoreCase = true) ||
                      exercise.category.contains("Arm", ignoreCase = true)) &&
                     (exercise.name.contains("tricep", ignoreCase = true) ||
                      exercise.name.contains("extension", ignoreCase = true) ||
                      exercise.name.contains("pushdown", ignoreCase = true) ||
                      exercise.name.contains("dip", ignoreCase = true) ||
                      exercise.name.contains("skull", ignoreCase = true) ||
                      exercise.name.contains("jm press", ignoreCase = true) ||
                      exercise.name.contains("tate press", ignoreCase = true) ||
                      exercise.name.contains("close-grip", ignoreCase = true) ||
                      exercise.name.contains("close grip", ignoreCase = true))) ||
                    exercise.name.contains("diamond push-up", ignoreCase = true) ||
                    exercise.name.contains("diamond pushup", ignoreCase = true)
                }
                "Chest" -> exercise.primaryMuscle.contains("Chest", ignoreCase = true) ||
                        exercise.muscleGroup.contains("Chest", ignoreCase = true) ||
                        exercise.category.contains("Chest", ignoreCase = true)
                "Back" -> exercise.primaryMuscle.contains("Back", ignoreCase = true) ||
                        exercise.primaryMuscle.contains("Lat", ignoreCase = true) ||
                        exercise.primaryMuscle.contains("Trap", ignoreCase = true) ||
                        exercise.muscleGroup.contains("Back", ignoreCase = true) ||
                        exercise.category.contains("Back", ignoreCase = true)
                "Shoulders" -> exercise.primaryMuscle.contains("Shoulder", ignoreCase = true) ||
                        exercise.primaryMuscle.contains("Deltoid", ignoreCase = true) ||
                        exercise.muscleGroup.contains("Shoulder", ignoreCase = true) ||
                        exercise.category.contains("Shoulder", ignoreCase = true)
                "Legs" -> exercise.primaryMuscle.contains("Leg", ignoreCase = true) ||
                        exercise.primaryMuscle.contains("Quad", ignoreCase = true) ||
                        exercise.primaryMuscle.contains("Hamstring", ignoreCase = true) ||
                        exercise.primaryMuscle.contains("Calf", ignoreCase = true) ||
                        exercise.primaryMuscle.contains("Calves", ignoreCase = true) ||
                        exercise.primaryMuscle.contains("Glute", ignoreCase = true) ||
                        exercise.muscleGroup.contains("Leg", ignoreCase = true) ||
                        exercise.category.contains("Leg", ignoreCase = true)
                "Abs & Core" -> exercise.category.contains("Core", true) ||
                        exercise.category.contains("Abs", true) ||
                        exercise.primaryMuscle.contains("Abdominal", true) ||
                        exercise.primaryMuscle.contains("Core", true) ||
                        exercise.primaryMuscle.contains("Abs", true) ||
                        exercise.muscleGroup.contains("Core", true) ||
                        exercise.muscleGroup.contains("Abs", true)
                else -> exercise.category.contains(selectedCategoryFilter, ignoreCase = true) ||
                        exercise.primaryMuscle.contains(selectedCategoryFilter, ignoreCase = true) ||
                        exercise.muscleGroup.contains(selectedCategoryFilter, ignoreCase = true)
            }

            val matchesEquipment = when (selectedEquipmentFilter) {
                "All Equipment" -> true
                "Smith Machine" -> exercise.equipmentType.contains("Smith", ignoreCase = true) ||
                        exercise.equipmentCategory.contains("Smith", ignoreCase = true) ||
                        exercise.equipment.contains("Smith", ignoreCase = true) ||
                        exercise.name.contains("Smith", ignoreCase = true)
                "Bodyweight" -> exercise.equipmentType.contains("Bodyweight", ignoreCase = true) ||
                        exercise.equipmentCategory.contains("Bodyweight", ignoreCase = true) ||
                        exercise.equipment.contains("Bodyweight", ignoreCase = true)
                else -> exercise.equipmentType.equals(selectedEquipmentFilter, ignoreCase = true) ||
                        exercise.equipmentCategory.equals(selectedEquipmentFilter, ignoreCase = true) ||
                        exercise.equipment.equals(selectedEquipmentFilter, ignoreCase = true) ||
                        exercise.equipmentType.contains(selectedEquipmentFilter, ignoreCase = true)
            }

            matchesQuery && matchesCategory && matchesEquipment
        }
    }

    @Test
    fun testBicepsExercisesExistAcrossAllEquipmentTypes() {
        val exercises = DefaultExercisesData.allInitialExercises
        val equipmentTypes = listOf("All Equipment", "Barbell", "Dumbbell", "Cable", "Machine", "Smith Machine", "Bodyweight")

        for (eq in equipmentTypes) {
            val results = filterExercises(exercises, selectedCategoryFilter = "Biceps", selectedEquipmentFilter = eq)
            assertTrue("Expected Biceps exercises for equipment '$eq', but found none!", results.isNotEmpty())
        }
    }

    @Test
    fun testTricepsExercisesExistAcrossAllEquipmentTypes() {
        val exercises = DefaultExercisesData.allInitialExercises
        val equipmentTypes = listOf("All Equipment", "Barbell", "Dumbbell", "Cable", "Machine", "Smith Machine", "Bodyweight")

        for (eq in equipmentTypes) {
            val results = filterExercises(exercises, selectedCategoryFilter = "Triceps", selectedEquipmentFilter = eq)
            assertTrue("Expected Triceps exercises for equipment '$eq', but found none!", results.isNotEmpty())
        }
    }

    @Test
    fun testAllMajorMuscleCategoriesRetainExercises() {
        val exercises = DefaultExercisesData.allInitialExercises
        val categories = listOf("Chest", "Back", "Shoulders", "Legs", "Abs & Core", "Biceps", "Triceps")

        for (cat in categories) {
            val results = filterExercises(exercises, selectedCategoryFilter = cat)
            assertTrue("Expected exercises for category '$cat' to not be empty!", results.isNotEmpty())
        }
    }

    @Test
    fun testBicepsAndTricepsAreMutuallyExclusiveOnTargetMovements() {
        val exercises = DefaultExercisesData.allInitialExercises
        val bicepsOnly = filterExercises(exercises, selectedCategoryFilter = "Biceps")
        val tricepsOnly = filterExercises(exercises, selectedCategoryFilter = "Triceps")

        // Standing Barbell Bicep Curl should be in Biceps, not Triceps
        assertTrue(bicepsOnly.any { it.name == "Standing Barbell Bicep Curl" })
        assertFalse(tricepsOnly.any { it.name == "Standing Barbell Bicep Curl" })

        // Skull Crusher should be in Triceps, not Biceps
        assertTrue(tricepsOnly.any { it.name.contains("Skull Crusher") })
        assertFalse(bicepsOnly.any { it.name.contains("Skull Crusher") })
    }
}

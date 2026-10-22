package com.example.data

import com.example.data.entity.ExerciseEntity

data class DefaultExerciseItem(
    val id: String,
    val name: String,
    val targetMuscleGroup: String,
    val secondaryMuscles: List<String>,
    val equipment: String,
    val category: String,
    val iconResName: String = "ic_launcher_foreground"
) {
    fun toEntity(): ExerciseEntity {
        return ExerciseEntity(
            id = id,
            name = name,
            muscleGroup = targetMuscleGroup,
            equipment = equipment,
            prWeight = 0.0,
            prReps = 0,
            isCustom = false,
            category = category,
            equipmentType = equipment,
            notes = "",
            imageUrl = null,
            primaryMuscle = targetMuscleGroup,
            secondaryMuscles = secondaryMuscles,
            equipmentCategory = equipment,
            trackingType = "WEIGHT_AND_REPS",
            defaultRestSeconds = 90,
            instructions = ""
        )
    }
}

object DefaultExercisesData {

    val chestExerciseItems = listOf(
        DefaultExerciseItem(
            id = "chest_barbell_bench_press",
            name = "Barbell Bench Press",
            targetMuscleGroup = "Chest",
            secondaryMuscles = listOf("Triceps", "Front Deltoids"),
            equipment = "Barbell",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "chest_incline_barbell_bench_press",
            name = "Incline Barbell Bench Press",
            targetMuscleGroup = "Chest (Upper)",
            secondaryMuscles = listOf("Triceps", "Front Deltoids"),
            equipment = "Barbell",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "chest_dumbbell_bench_press",
            name = "Dumbbell Bench Press",
            targetMuscleGroup = "Chest",
            secondaryMuscles = listOf("Triceps", "Front Deltoids"),
            equipment = "Dumbbell",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "chest_incline_dumbbell_press",
            name = "Incline Dumbbell Press",
            targetMuscleGroup = "Chest (Upper)",
            secondaryMuscles = listOf("Triceps", "Front Deltoids"),
            equipment = "Dumbbell",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "chest_decline_barbell_bench_press",
            name = "Decline Barbell Bench Press",
            targetMuscleGroup = "Chest (Lower)",
            secondaryMuscles = listOf("Triceps", "Front Deltoids"),
            equipment = "Barbell",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "chest_decline_dumbbell_press",
            name = "Decline Dumbbell Press",
            targetMuscleGroup = "Chest (Lower)",
            secondaryMuscles = listOf("Triceps"),
            equipment = "Dumbbell",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "chest_cable_chest_fly_mid_pulley",
            name = "Cable Chest Fly (Mid Pulley)",
            targetMuscleGroup = "Chest",
            secondaryMuscles = listOf("Front Deltoids"),
            equipment = "Cable",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "chest_incline_cable_fly_low_to_high",
            name = "Incline Cable Fly (Low to High)",
            targetMuscleGroup = "Chest (Upper)",
            secondaryMuscles = listOf("Front Deltoids"),
            equipment = "Cable",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "chest_high_to_low_cable_crossover",
            name = "High-to-Low Cable Crossover",
            targetMuscleGroup = "Chest (Lower)",
            secondaryMuscles = listOf("Front Deltoids"),
            equipment = "Cable",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "chest_dumbbell_chest_fly",
            name = "Dumbbell Chest Fly",
            targetMuscleGroup = "Chest",
            secondaryMuscles = listOf("Front Deltoids"),
            equipment = "Dumbbell",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "chest_incline_dumbbell_fly",
            name = "Incline Dumbbell Fly",
            targetMuscleGroup = "Chest (Upper)",
            secondaryMuscles = listOf("Front Deltoids"),
            equipment = "Dumbbell",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "chest_pec_deck_fly_machine_fly",
            name = "Pec Deck Fly (Machine Fly)",
            targetMuscleGroup = "Chest",
            secondaryMuscles = listOf("Front Deltoids"),
            equipment = "Machine",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "chest_press_machine",
            name = "Chest Press Machine",
            targetMuscleGroup = "Chest",
            secondaryMuscles = listOf("Triceps", "Front Deltoids"),
            equipment = "Machine",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "chest_incline_chest_press_machine",
            name = "Incline Chest Press Machine",
            targetMuscleGroup = "Chest (Upper)",
            secondaryMuscles = listOf("Triceps", "Front Deltoids"),
            equipment = "Machine",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "chest_push_up",
            name = "Push-Up",
            targetMuscleGroup = "Chest",
            secondaryMuscles = listOf("Triceps", "Core", "Front Deltoids"),
            equipment = "Bodyweight",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "chest_diamond_push_up",
            name = "Diamond Push-Up",
            targetMuscleGroup = "Chest (Inner)",
            secondaryMuscles = listOf("Triceps"),
            equipment = "Bodyweight",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "chest_dips",
            name = "Chest Dips",
            targetMuscleGroup = "Chest (Lower)",
            secondaryMuscles = listOf("Triceps", "Front Deltoids"),
            equipment = "Bodyweight / Parallel Bars",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "chest_weighted_dips",
            name = "Weighted Chest Dips",
            targetMuscleGroup = "Chest (Lower)",
            secondaryMuscles = listOf("Triceps", "Front Deltoids"),
            equipment = "Bodyweight / Weight Belt",
            category = "Compound"
        )
    )

    val chestExercises: List<ExerciseEntity> = chestExerciseItems.map { it.toEntity() }

    val backExerciseItems = listOf(
        DefaultExerciseItem(
            id = "back_conventional_deadlift",
            name = "Conventional Deadlift",
            targetMuscleGroup = "Back (Full / Lower)",
            secondaryMuscles = listOf("Hamstrings", "Glutes", "Forearms", "Traps"),
            equipment = "Barbell",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "back_barbell_bent_over_row_overhand",
            name = "Barbell Bent-Over Row (Overhand Grip)",
            targetMuscleGroup = "Back (Upper / Lats)",
            secondaryMuscles = listOf("Biceps", "Rear Deltoids", "Lower Back"),
            equipment = "Barbell",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "back_barbell_bent_over_row_underhand_yates",
            name = "Barbell Bent-Over Row (Underhand / Yates Grip)",
            targetMuscleGroup = "Back (Lower Lats)",
            secondaryMuscles = listOf("Biceps", "Forearms"),
            equipment = "Barbell",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "back_pendlay_row",
            name = "Pendlay Row",
            targetMuscleGroup = "Back (Upper Back / Lats)",
            secondaryMuscles = listOf("Core", "Hamstrings"),
            equipment = "Barbell",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "back_t_bar_row_chest_supported",
            name = "T-Bar Row (Chest Supported)",
            targetMuscleGroup = "Back (Mid-Back / Rhomboids)",
            secondaryMuscles = listOf("Biceps", "Rear Deltoids"),
            equipment = "Machine",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "back_landmine_t_bar_row",
            name = "Landmine T-Bar Row",
            targetMuscleGroup = "Back (Upper Back / Lats)",
            secondaryMuscles = listOf("Core", "Biceps"),
            equipment = "Barbell",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "back_single_arm_dumbbell_row",
            name = "Single-Arm Dumbbell Row",
            targetMuscleGroup = "Back (Lats)",
            secondaryMuscles = listOf("Biceps", "Forearms", "Core"),
            equipment = "Dumbbell",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "back_chest_supported_incline_dumbbell_row",
            name = "Chest-Supported Incline Dumbbell Row",
            targetMuscleGroup = "Back (Upper Back / Rhomboids)",
            secondaryMuscles = listOf("Rear Deltoids", "Biceps"),
            equipment = "Dumbbell",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "back_kroc_row_heavy_dumbbell_row",
            name = "Kroc Row (Heavy Dumbbell Row)",
            targetMuscleGroup = "Back (Full Lats / Grip)",
            secondaryMuscles = listOf("Forearms", "Traps", "Core"),
            equipment = "Dumbbell",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "back_lat_pulldown_wide_overhand",
            name = "Lat Pulldown (Wide Overhand Grip)",
            targetMuscleGroup = "Back (Lats - Width)",
            secondaryMuscles = listOf("Biceps", "Rear Deltoids"),
            equipment = "Cable",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "back_lat_pulldown_close_neutral",
            name = "Lat Pulldown (Close Neutral Grip)",
            targetMuscleGroup = "Back (Lower Lats)",
            secondaryMuscles = listOf("Biceps", "Brachialis"),
            equipment = "Cable",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "back_lat_pulldown_reverse_underhand",
            name = "Lat Pulldown (Reverse / Underhand Grip)",
            targetMuscleGroup = "Back (Lats)",
            secondaryMuscles = listOf("Biceps"),
            equipment = "Cable",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "back_single_arm_cable_lat_pulldown_half_kneeling",
            name = "Single-Arm Cable Lat Pulldown (Half-Kneeling)",
            targetMuscleGroup = "Back (Lats - Iliac Division)",
            secondaryMuscles = listOf("Biceps", "Obliques"),
            equipment = "Cable",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "back_seated_cable_row_v_bar_close_grip",
            name = "Seated Cable Row (V-Bar / Close Grip)",
            targetMuscleGroup = "Back (Mid-Back / Lats)",
            secondaryMuscles = listOf("Biceps", "Forearms"),
            equipment = "Cable",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "back_seated_cable_row_wide_grip_bar",
            name = "Seated Cable Row (Wide Grip Bar)",
            targetMuscleGroup = "Back (Upper Back / Rhomboids / Rear Delts)",
            secondaryMuscles = listOf("Biceps"),
            equipment = "Cable",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "back_single_arm_seated_cable_row",
            name = "Single-Arm Seated Cable Row",
            targetMuscleGroup = "Back (Lats)",
            secondaryMuscles = listOf("Biceps", "Core"),
            equipment = "Cable",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "back_standing_cable_face_pull",
            name = "Standing Cable Face Pull",
            targetMuscleGroup = "Back (Upper Back / Traps / Rear Delts)",
            secondaryMuscles = listOf("Rotator Cuff"),
            equipment = "Cable",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "back_straight_arm_cable_lat_pushdown_rope",
            name = "Straight-Arm Cable Lat Pushdown (Rope)",
            targetMuscleGroup = "Back (Lats)",
            secondaryMuscles = listOf("Triceps (Long Head)", "Core"),
            equipment = "Cable",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "back_straight_arm_cable_lat_pushdown_straight_bar",
            name = "Straight-Arm Cable Lat Pushdown (Straight Bar)",
            targetMuscleGroup = "Back (Lats)",
            secondaryMuscles = listOf("Triceps (Long Head)"),
            equipment = "Cable",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "back_pull_up_bodyweight_overhand",
            name = "Pull-Up (Bodyweight Overhand)",
            targetMuscleGroup = "Back (Lats - Width)",
            secondaryMuscles = listOf("Biceps", "Forearms", "Core"),
            equipment = "Bodyweight",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "back_weighted_pull_up",
            name = "Weighted Pull-Up",
            targetMuscleGroup = "Back (Lats)",
            secondaryMuscles = listOf("Biceps", "Core"),
            equipment = "Bodyweight / Weight Belt",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "back_chin_up_bodyweight_underhand",
            name = "Chin-Up (Bodyweight Underhand Grip)",
            targetMuscleGroup = "Back (Lats)",
            secondaryMuscles = listOf("Biceps", "Forearms"),
            equipment = "Bodyweight",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "back_weighted_chin_up",
            name = "Weighted Chin-Up",
            targetMuscleGroup = "Back (Lats)",
            secondaryMuscles = listOf("Biceps"),
            equipment = "Bodyweight / Weight Belt",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "back_neutral_grip_pull_up",
            name = "Neutral Grip Pull-Up",
            targetMuscleGroup = "Back (Lats)",
            secondaryMuscles = listOf("Brachialis", "Biceps"),
            equipment = "Bodyweight",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "back_assisted_pull_up_machine",
            name = "Assisted Pull-Up Machine",
            targetMuscleGroup = "Back (Lats)",
            secondaryMuscles = listOf("Biceps"),
            equipment = "Machine",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "back_inverted_row_bodyweight_barbell",
            name = "Inverted Row (Bodyweight / Barbell)",
            targetMuscleGroup = "Back (Mid-Back / Rhomboids)",
            secondaryMuscles = listOf("Biceps", "Core"),
            equipment = "Bodyweight",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "back_machine_iso_lateral_high_row",
            name = "Machine Iso-Lateral High Row",
            targetMuscleGroup = "Back (Upper Lats / Rhomboids)",
            secondaryMuscles = listOf("Biceps"),
            equipment = "Machine",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "back_machine_iso_lateral_low_row",
            name = "Machine Iso-Lateral Low Row",
            targetMuscleGroup = "Back (Lower Lats)",
            secondaryMuscles = listOf("Biceps"),
            equipment = "Machine",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "back_hyperextension_back_extension_45_degree",
            name = "Hyperextension / Back Extension (45 Degree)",
            targetMuscleGroup = "Back (Lower Back / Erector Spinae)",
            secondaryMuscles = listOf("Glutes", "Hamstrings"),
            equipment = "Bodyweight / Bench",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "back_weighted_hyperextension",
            name = "Weighted Hyperextension",
            targetMuscleGroup = "Back (Lower Back / Erector Spinae)",
            secondaryMuscles = listOf("Glutes", "Hamstrings"),
            equipment = "Dumbbell / Plate",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "back_good_morning",
            name = "Good Morning",
            targetMuscleGroup = "Back (Lower Back / Posterior Chain)",
            secondaryMuscles = listOf("Hamstrings", "Glutes"),
            equipment = "Barbell",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "back_barbell_shrug",
            name = "Barbell Shrug",
            targetMuscleGroup = "Back (Upper Traps)",
            secondaryMuscles = listOf("Forearms"),
            equipment = "Barbell",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "back_dumbbell_shrug",
            name = "Dumbbell Shrug",
            targetMuscleGroup = "Back (Upper Traps)",
            secondaryMuscles = listOf("Forearms"),
            equipment = "Dumbbell",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "back_smith_machine_shrug",
            name = "Smith Machine Shrug",
            targetMuscleGroup = "Back (Upper Traps)",
            secondaryMuscles = listOf("Forearms"),
            equipment = "Machine",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "back_dumbbell_pullover",
            name = "Dumbbell Pullover",
            targetMuscleGroup = "Back (Lats)",
            secondaryMuscles = listOf("Chest", "Triceps"),
            equipment = "Dumbbell",
            category = "Compound"
        )
    )

    val backExercises: List<ExerciseEntity> = backExerciseItems.map { it.toEntity() }

    val shoulderExerciseItems = listOf(
        DefaultExerciseItem(
            id = "shoulders_overhead_barbell_press_standing_ohp",
            name = "Overhead Barbell Press (Standing OHP)",
            targetMuscleGroup = "Shoulders (Front / Lateral Deltoids)",
            secondaryMuscles = listOf("Triceps", "Upper Chest", "Core"),
            equipment = "Barbell",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "shoulders_seated_overhead_barbell_press",
            name = "Seated Overhead Barbell Press",
            targetMuscleGroup = "Shoulders (Front Deltoids)",
            secondaryMuscles = listOf("Triceps", "Upper Chest"),
            equipment = "Barbell",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "shoulders_seated_dumbbell_shoulder_press",
            name = "Seated Dumbbell Shoulder Press",
            targetMuscleGroup = "Shoulders (Front / Lateral Deltoids)",
            secondaryMuscles = listOf("Triceps", "Traps"),
            equipment = "Dumbbell",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "shoulders_standing_dumbbell_shoulder_press",
            name = "Standing Dumbbell Shoulder Press",
            targetMuscleGroup = "Shoulders (Front Deltoids)",
            secondaryMuscles = listOf("Triceps", "Core"),
            equipment = "Dumbbell",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "shoulders_arnold_press",
            name = "Arnold Press",
            targetMuscleGroup = "Shoulders (Front / Lateral Deltoids)",
            secondaryMuscles = listOf("Triceps", "Rotator Cuff"),
            equipment = "Dumbbell",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "shoulders_push_press",
            name = "Push Press",
            targetMuscleGroup = "Shoulders (Full Deltoids)",
            secondaryMuscles = listOf("Triceps", "Quads", "Glutes", "Core"),
            equipment = "Barbell",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "shoulders_behind_the_neck_barbell_press",
            name = "Behind-the-Neck Barbell Press",
            targetMuscleGroup = "Shoulders (Lateral / Rear Deltoids)",
            secondaryMuscles = listOf("Triceps", "Traps"),
            equipment = "Barbell",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "shoulders_smith_machine_overhead_press",
            name = "Smith Machine Overhead Press",
            targetMuscleGroup = "Shoulders (Front Deltoids)",
            secondaryMuscles = listOf("Triceps"),
            equipment = "Machine",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "shoulders_machine_shoulder_press",
            name = "Machine Shoulder Press",
            targetMuscleGroup = "Shoulders (Front / Lateral Deltoids)",
            secondaryMuscles = listOf("Triceps"),
            equipment = "Machine",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "shoulders_landmine_single_arm_shoulder_press",
            name = "Landmine Single-Arm Shoulder Press",
            targetMuscleGroup = "Shoulders (Front Deltoids)",
            secondaryMuscles = listOf("Triceps", "Core", "Upper Chest"),
            equipment = "Barbell",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "shoulders_standing_dumbbell_lateral_raise",
            name = "Standing Dumbbell Lateral Raise",
            targetMuscleGroup = "Shoulders (Lateral Deltoids)",
            secondaryMuscles = listOf("Upper Traps"),
            equipment = "Dumbbell",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "shoulders_seated_dumbbell_lateral_raise",
            name = "Seated Dumbbell Lateral Raise",
            targetMuscleGroup = "Shoulders (Lateral Deltoids)",
            secondaryMuscles = listOf("Upper Traps"),
            equipment = "Dumbbell",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "shoulders_lean_away_dumbbell_lateral_raise",
            name = "Lean-Away Dumbbell Lateral Raise",
            targetMuscleGroup = "Shoulders (Lateral Deltoids)",
            secondaryMuscles = listOf("Upper Traps"),
            equipment = "Dumbbell",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "shoulders_single_arm_cable_lateral_raise_behind_back",
            name = "Single-Arm Cable Lateral Raise (Behind Back)",
            targetMuscleGroup = "Shoulders (Lateral Deltoids)",
            secondaryMuscles = listOf("Rotator Cuff"),
            equipment = "Cable",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "shoulders_single_arm_cable_lateral_raise_in_front",
            name = "Single-Arm Cable Lateral Raise (In Front)",
            targetMuscleGroup = "Shoulders (Lateral Deltoids)",
            secondaryMuscles = listOf("Rotator Cuff"),
            equipment = "Cable",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "shoulders_dual_cable_cross_lateral_raise",
            name = "Dual Cable Cross Lateral Raise",
            targetMuscleGroup = "Shoulders (Lateral Deltoids)",
            secondaryMuscles = listOf("Upper Traps"),
            equipment = "Cable",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "shoulders_machine_lateral_raise",
            name = "Machine Lateral Raise",
            targetMuscleGroup = "Shoulders (Lateral Deltoids)",
            secondaryMuscles = listOf("Traps"),
            equipment = "Machine",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "shoulders_incline_bench_dumbbell_lateral_raise",
            name = "Incline Bench Dumbbell Lateral Raise",
            targetMuscleGroup = "Shoulders (Lateral Deltoids)",
            secondaryMuscles = listOf("Upper Traps"),
            equipment = "Dumbbell",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "shoulders_dumbbell_front_raise",
            name = "Dumbbell Front Raise",
            targetMuscleGroup = "Shoulders (Front Deltoids)",
            secondaryMuscles = listOf("Upper Chest"),
            equipment = "Dumbbell",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "shoulders_barbell_front_raise",
            name = "Barbell Front Raise",
            targetMuscleGroup = "Shoulders (Front Deltoids)",
            secondaryMuscles = listOf("Upper Chest"),
            equipment = "Barbell",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "shoulders_cable_front_raise_rope_attachment",
            name = "Cable Front Raise (Rope Attachment)",
            targetMuscleGroup = "Shoulders (Front Deltoids)",
            secondaryMuscles = listOf("Upper Chest"),
            equipment = "Cable",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "shoulders_weight_plate_front_raise",
            name = "Weight Plate Front Raise",
            targetMuscleGroup = "Shoulders (Front Deltoids)",
            secondaryMuscles = listOf("Forearms", "Upper Chest"),
            equipment = "Other",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "shoulders_bent_over_rear_delt_dumbbell_fly",
            name = "Bent-Over Rear Delt Dumbbell Fly",
            targetMuscleGroup = "Shoulders (Rear Deltoids)",
            secondaryMuscles = listOf("Rhomboids", "Traps"),
            equipment = "Dumbbell",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "shoulders_incline_bench_rear_delt_dumbbell_fly",
            name = "Incline Bench Rear Delt Dumbbell Fly",
            targetMuscleGroup = "Shoulders (Rear Deltoids)",
            secondaryMuscles = listOf("Rhomboids", "Upper Back"),
            equipment = "Dumbbell",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "shoulders_rear_delt_machine_fly_reverse_pec_deck",
            name = "Rear Delt Machine Fly (Reverse Pec Deck)",
            targetMuscleGroup = "Shoulders (Rear Deltoids)",
            secondaryMuscles = listOf("Rhomboids", "Middle Traps"),
            equipment = "Machine",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "shoulders_cable_rear_delt_fly_high_cross",
            name = "Cable Rear Delt Fly (High Cross)",
            targetMuscleGroup = "Shoulders (Rear Deltoids)",
            secondaryMuscles = listOf("Upper Back"),
            equipment = "Cable",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "shoulders_single_arm_cable_rear_delt_fly",
            name = "Single-Arm Cable Rear Delt Fly",
            targetMuscleGroup = "Shoulders (Rear Deltoids)",
            secondaryMuscles = listOf("Rhomboids"),
            equipment = "Cable",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "shoulders_standing_cable_face_pull_high_pulley",
            name = "Standing Cable Face Pull (High Pulley)",
            targetMuscleGroup = "Shoulders (Rear Deltoids / Rotator Cuff)",
            secondaryMuscles = listOf("Upper Traps", "Rhomboids"),
            equipment = "Cable",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "shoulders_barbell_upright_row",
            name = "Barbell Upright Row",
            targetMuscleGroup = "Shoulders (Lateral Deltoids / Traps)",
            secondaryMuscles = listOf("Biceps", "Forearms"),
            equipment = "Barbell",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "shoulders_dumbbell_upright_row",
            name = "Dumbbell Upright Row",
            targetMuscleGroup = "Shoulders (Lateral Deltoids / Traps)",
            secondaryMuscles = listOf("Biceps"),
            equipment = "Dumbbell",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "shoulders_cable_upright_row_straight_bar",
            name = "Cable Upright Row (Straight Bar)",
            targetMuscleGroup = "Shoulders (Lateral Deltoids / Traps)",
            secondaryMuscles = listOf("Biceps"),
            equipment = "Cable",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "shoulders_cable_external_rotation_rotator_cuff",
            name = "Cable External Rotation (Rotator Cuff)",
            targetMuscleGroup = "Shoulders (Infraspinatus / Teres Minor)",
            secondaryMuscles = listOf("Rear Deltoids"),
            equipment = "Cable",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "shoulders_cable_internal_rotation_rotator_cuff",
            name = "Cable Internal Rotation (Rotator Cuff)",
            targetMuscleGroup = "Shoulders (Subscapularis)",
            secondaryMuscles = listOf("Front Deltoids"),
            equipment = "Cable",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "shoulders_side_lying_dumbbell_external_rotation",
            name = "Side-Lying Dumbbell External Rotation",
            targetMuscleGroup = "Shoulders (Rotator Cuff)",
            secondaryMuscles = listOf("Rear Deltoids"),
            equipment = "Dumbbell",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "shoulders_pike_push_up",
            name = "Pike Push-Up",
            targetMuscleGroup = "Shoulders (Front / Lateral Deltoids)",
            secondaryMuscles = listOf("Triceps", "Upper Chest", "Core"),
            equipment = "Bodyweight",
            category = "Compound"
        )
    )

    val shoulderExercises: List<ExerciseEntity> = shoulderExerciseItems.map { it.toEntity() }

    val legExerciseItems = listOf(
        DefaultExerciseItem(
            id = "legs_barbell_back_squat_high_bar",
            name = "Barbell Back Squat (High Bar)",
            targetMuscleGroup = "Legs (Quads)",
            secondaryMuscles = listOf("Glutes", "Hamstrings", "Lower Back", "Core"),
            equipment = "Barbell",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "legs_barbell_back_squat_low_bar",
            name = "Barbell Back Squat (Low Bar)",
            targetMuscleGroup = "Legs (Posterior Chain / Glutes)",
            secondaryMuscles = listOf("Quads", "Hamstrings", "Lower Back"),
            equipment = "Barbell",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "legs_barbell_front_squat",
            name = "Barbell Front Squat",
            targetMuscleGroup = "Legs (Quads)",
            secondaryMuscles = listOf("Glutes", "Core", "Upper Back"),
            equipment = "Barbell",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "legs_box_squat",
            name = "Box Squat",
            targetMuscleGroup = "Legs (Glutes / Quads)",
            secondaryMuscles = listOf("Hamstrings", "Core"),
            equipment = "Barbell",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "legs_zercher_squat",
            name = "Zercher Squat",
            targetMuscleGroup = "Legs (Quads / Glutes)",
            secondaryMuscles = listOf("Core", "Biceps", "Upper Back"),
            equipment = "Barbell",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "legs_goblet_squat_dumbbell_kettlebell",
            name = "Goblet Squat (Dumbbell / Kettlebell)",
            targetMuscleGroup = "Legs (Quads)",
            secondaryMuscles = listOf("Glutes", "Core"),
            equipment = "Dumbbell",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "legs_romanian_deadlift_barbell_rdl",
            name = "Romanian Deadlift (Barbell RDL)",
            targetMuscleGroup = "Legs (Hamstrings)",
            secondaryMuscles = listOf("Glutes", "Lower Back", "Forearms"),
            equipment = "Barbell",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "legs_dumbbell_romanian_deadlift",
            name = "Dumbbell Romanian Deadlift",
            targetMuscleGroup = "Legs (Hamstrings)",
            secondaryMuscles = listOf("Glutes", "Lower Back"),
            equipment = "Dumbbell",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "legs_single_leg_romanian_deadlift_dumbbell",
            name = "Single-Leg Romanian Deadlift (Dumbbell)",
            targetMuscleGroup = "Legs (Hamstrings / Glutes)",
            secondaryMuscles = listOf("Core", "Balance / Ankles"),
            equipment = "Dumbbell",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "legs_stiff_legged_deadlift",
            name = "Stiff-Legged Deadlift",
            targetMuscleGroup = "Legs (Hamstrings)",
            secondaryMuscles = listOf("Glutes", "Lower Back"),
            equipment = "Barbell",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "legs_sumo_deadlift",
            name = "Sumo Deadlift",
            targetMuscleGroup = "Legs (Glutes / Adductors)",
            secondaryMuscles = listOf("Quads", "Hamstrings", "Lower Back"),
            equipment = "Barbell",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "legs_trap_bar_deadlift_hex_bar",
            name = "Trap Bar Deadlift (Hex Bar)",
            targetMuscleGroup = "Legs (Quads / Glutes)",
            secondaryMuscles = listOf("Hamstrings", "Lower Back", "Traps"),
            equipment = "Barbell",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "legs_leg_press_45_degree_sled",
            name = "Leg Press (45 Degree Sled)",
            targetMuscleGroup = "Legs (Quads)",
            secondaryMuscles = listOf("Glutes", "Hamstrings"),
            equipment = "Machine",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "legs_single_leg_press",
            name = "Single-Leg Press",
            targetMuscleGroup = "Legs (Quads / Glutes)",
            secondaryMuscles = listOf("Hamstrings"),
            equipment = "Machine",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "legs_hack_squat_machine",
            name = "Hack Squat (Machine)",
            targetMuscleGroup = "Legs (Quads)",
            secondaryMuscles = listOf("Glutes"),
            equipment = "Machine",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "legs_reverse_hack_squat",
            name = "Reverse Hack Squat",
            targetMuscleGroup = "Legs (Glutes / Hamstrings)",
            secondaryMuscles = listOf("Quads", "Lower Back"),
            equipment = "Machine",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "legs_pendulum_squat",
            name = "Pendulum Squat",
            targetMuscleGroup = "Legs (Quads - VMO)",
            secondaryMuscles = listOf("Glutes"),
            equipment = "Machine",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "legs_smith_machine_squat",
            name = "Smith Machine Squat",
            targetMuscleGroup = "Legs (Quads)",
            secondaryMuscles = listOf("Glutes"),
            equipment = "Machine",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "legs_bulgarian_split_squat_dumbbell",
            name = "Bulgarian Split Squat (Dumbbell)",
            targetMuscleGroup = "Legs (Quads / Glutes)",
            secondaryMuscles = listOf("Hamstrings", "Adductors"),
            equipment = "Dumbbell",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "legs_barbell_walking_lunge",
            name = "Barbell Walking Lunge",
            targetMuscleGroup = "Legs (Quads / Glutes)",
            secondaryMuscles = listOf("Hamstrings", "Calves", "Core"),
            equipment = "Barbell",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "legs_dumbbell_walking_lunge",
            name = "Dumbbell Walking Lunge",
            targetMuscleGroup = "Legs (Quads / Glutes)",
            secondaryMuscles = listOf("Hamstrings", "Core"),
            equipment = "Dumbbell",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "legs_reverse_lunge_dumbbell",
            name = "Reverse Lunge (Dumbbell)",
            targetMuscleGroup = "Legs (Glutes / Quads)",
            secondaryMuscles = listOf("Hamstrings"),
            equipment = "Dumbbell",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "legs_barbell_hip_thrust",
            name = "Barbell Hip Thrust",
            targetMuscleGroup = "Legs (Glutes)",
            secondaryMuscles = listOf("Hamstrings", "Core"),
            equipment = "Barbell",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "legs_dumbbell_glute_bridge",
            name = "Dumbbell Glute Bridge",
            targetMuscleGroup = "Legs (Glutes)",
            secondaryMuscles = listOf("Hamstrings"),
            equipment = "Dumbbell",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "legs_machine_hip_thrust",
            name = "Machine Hip Thrust",
            targetMuscleGroup = "Legs (Glutes)",
            secondaryMuscles = listOf("Hamstrings"),
            equipment = "Machine",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "legs_cable_pull_through",
            name = "Cable Pull-Through",
            targetMuscleGroup = "Legs (Glutes / Hamstrings)",
            secondaryMuscles = listOf("Lower Back"),
            equipment = "Cable",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "legs_seated_leg_curl",
            name = "Seated Leg Curl",
            targetMuscleGroup = "Legs (Hamstrings)",
            secondaryMuscles = listOf("Calves"),
            equipment = "Machine",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "legs_lying_leg_curl_prone",
            name = "Lying Leg Curl (Prone)",
            targetMuscleGroup = "Legs (Hamstrings)",
            secondaryMuscles = listOf("Calves"),
            equipment = "Machine",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "legs_standing_single_leg_curl",
            name = "Standing Single-Leg Curl",
            targetMuscleGroup = "Legs (Hamstrings)",
            secondaryMuscles = listOf("Calves"),
            equipment = "Machine",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "legs_nordic_hamstring_curl",
            name = "Nordic Hamstring Curl",
            targetMuscleGroup = "Legs (Hamstrings - Eccentric)",
            secondaryMuscles = listOf("Glutes", "Core"),
            equipment = "Bodyweight",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "legs_leg_extension_machine",
            name = "Leg Extension Machine",
            targetMuscleGroup = "Legs (Quads - Rectus Femoris)",
            secondaryMuscles = emptyList(),
            equipment = "Machine",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "legs_single_leg_extension",
            name = "Single-Leg Extension",
            targetMuscleGroup = "Legs (Quads)",
            secondaryMuscles = emptyList(),
            equipment = "Machine",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "legs_sissy_squat_machine_bodyweight",
            name = "Sissy Squat (Machine / Bodyweight)",
            targetMuscleGroup = "Legs (Quads - Tear Drop)",
            secondaryMuscles = listOf("Core"),
            equipment = "Bodyweight",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "legs_step_up_dumbbell_box",
            name = "Step-Up (Dumbbell / Box)",
            targetMuscleGroup = "Legs (Glutes / Quads)",
            secondaryMuscles = listOf("Hamstrings"),
            equipment = "Dumbbell",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "legs_seated_hip_abduction_machine",
            name = "Seated Hip Abduction Machine",
            targetMuscleGroup = "Legs (Gluteus Medius / Minimus)",
            secondaryMuscles = listOf("Tensor Fasciae Latae"),
            equipment = "Machine",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "legs_seated_hip_adduction_machine",
            name = "Seated Hip Adduction Machine",
            targetMuscleGroup = "Legs (Adductors / Inner Thigh)",
            secondaryMuscles = listOf("Gracilis"),
            equipment = "Machine",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "legs_standing_calf_raise_machine",
            name = "Standing Calf Raise (Machine)",
            targetMuscleGroup = "Legs (Calves - Gastrocnemius)",
            secondaryMuscles = listOf("Soleus"),
            equipment = "Machine",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "legs_seated_calf_raise_machine",
            name = "Seated Calf Raise (Machine)",
            targetMuscleGroup = "Legs (Calves - Soleus)",
            secondaryMuscles = listOf("Gastrocnemius"),
            equipment = "Machine",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "legs_leg_press_calf_raise",
            name = "Leg Press Calf Raise",
            targetMuscleGroup = "Legs (Calves)",
            secondaryMuscles = listOf("Tibialis Posterior"),
            equipment = "Machine",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "legs_donkey_calf_raise",
            name = "Donkey Calf Raise",
            targetMuscleGroup = "Legs (Calves)",
            secondaryMuscles = listOf("Soleus"),
            equipment = "Machine",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "legs_single_leg_dumbbell_calf_raise",
            name = "Single-Leg Dumbbell Calf Raise",
            targetMuscleGroup = "Legs (Calves)",
            secondaryMuscles = listOf("Ankle Stabilizers"),
            equipment = "Dumbbell",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "legs_smith_machine_calf_raise",
            name = "Smith Machine Calf Raise",
            targetMuscleGroup = "Legs (Calves)",
            secondaryMuscles = listOf("Soleus"),
            equipment = "Machine",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "legs_bodyweight_jump_squat",
            name = "Bodyweight Jump Squat",
            targetMuscleGroup = "Legs (Quads / Glutes)",
            secondaryMuscles = listOf("Calves", "Core"),
            equipment = "Bodyweight",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "legs_cossack_squat",
            name = "Cossack Squat",
            targetMuscleGroup = "Legs (Adductors / Quads)",
            secondaryMuscles = listOf("Glutes", "Mobility"),
            equipment = "Bodyweight",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "legs_tibialis_bar_raise_toe_raise",
            name = "Tibialis Bar Raise (Toe Raise)",
            targetMuscleGroup = "Legs (Tibialis Anterior / Shins)",
            secondaryMuscles = listOf("Ankles"),
            equipment = "Other",
            category = "Isolation"
        )
    )

    val legExercises: List<ExerciseEntity> = legExerciseItems.map { it.toEntity() }

    val armExerciseItems = listOf(
        DefaultExerciseItem(
            id = "arms_standing_barbell_bicep_curl",
            name = "Standing Barbell Bicep Curl",
            targetMuscleGroup = "Arms (Biceps - Both Heads)",
            secondaryMuscles = listOf("Forearms", "Brachialis"),
            equipment = "Barbell",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "arms_ez_bar_bicep_curl",
            name = "EZ-Bar Bicep Curl",
            targetMuscleGroup = "Arms (Biceps)",
            secondaryMuscles = listOf("Forearms", "Brachialis"),
            equipment = "Barbell",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "arms_standing_dumbbell_bicep_curl",
            name = "Standing Dumbbell Bicep Curl",
            targetMuscleGroup = "Arms (Biceps)",
            secondaryMuscles = listOf("Forearms"),
            equipment = "Dumbbell",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "arms_seated_incline_dumbbell_curl",
            name = "Seated Incline Dumbbell Curl",
            targetMuscleGroup = "Arms (Biceps - Long Head)",
            secondaryMuscles = listOf("Forearms"),
            equipment = "Dumbbell",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "arms_dumbbell_hammer_curl",
            name = "Dumbbell Hammer Curl",
            targetMuscleGroup = "Arms (Brachialis / Biceps)",
            secondaryMuscles = listOf("Brachioradialis / Forearms"),
            equipment = "Dumbbell",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "arms_cable_rope_hammer_curl",
            name = "Cable Rope Hammer Curl",
            targetMuscleGroup = "Arms (Brachialis / Forearms)",
            secondaryMuscles = listOf("Biceps"),
            equipment = "Cable",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "arms_preacher_curl_ez_bar",
            name = "Preacher Curl (EZ-Bar)",
            targetMuscleGroup = "Arms (Biceps - Short Head)",
            secondaryMuscles = listOf("Forearms"),
            equipment = "Barbell",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "arms_preacher_curl_dumbbell_single_arm",
            name = "Preacher Curl (Dumbbell Single-Arm)",
            targetMuscleGroup = "Arms (Biceps - Short Head)",
            secondaryMuscles = listOf("Forearms"),
            equipment = "Dumbbell",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "arms_machine_preacher_curl",
            name = "Machine Preacher Curl",
            targetMuscleGroup = "Arms (Biceps)",
            secondaryMuscles = listOf("Forearms"),
            equipment = "Machine",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "arms_concentration_curl_dumbbell",
            name = "Concentration Curl (Dumbbell)",
            targetMuscleGroup = "Arms (Biceps - Peak / Short Head)",
            secondaryMuscles = listOf("Brachialis"),
            equipment = "Dumbbell",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "arms_standing_cable_bicep_curl_straight_bar",
            name = "Standing Cable Bicep Curl (Straight Bar)",
            targetMuscleGroup = "Arms (Biceps)",
            secondaryMuscles = listOf("Forearms"),
            equipment = "Cable",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "arms_high_cable_overhead_bicep_curl_hercules_curl",
            name = "High Cable Overhead Bicep Curl (Hercules Curl)",
            targetMuscleGroup = "Arms (Biceps - Peak)",
            secondaryMuscles = listOf("Forearms"),
            equipment = "Cable",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "arms_bayesian_cable_curl_behind_the_back",
            name = "Bayesian Cable Curl (Behind-the-Back)",
            targetMuscleGroup = "Arms (Biceps - Long Head)",
            secondaryMuscles = listOf("Forearms"),
            equipment = "Cable",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "arms_spider_curl_ez_bar_on_incline_bench",
            name = "Spider Curl (EZ-Bar on Incline Bench)",
            targetMuscleGroup = "Arms (Biceps - Short Head)",
            secondaryMuscles = listOf("Forearms"),
            equipment = "Barbell",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "arms_zottman_curl",
            name = "Zottman Curl",
            targetMuscleGroup = "Arms (Biceps & Forearms)",
            secondaryMuscles = listOf("Brachioradialis"),
            equipment = "Dumbbell",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "arms_reverse_barbell_curl",
            name = "Reverse Barbell Curl",
            targetMuscleGroup = "Arms (Brachioradialis / Forearms)",
            secondaryMuscles = listOf("Biceps", "Brachialis"),
            equipment = "Barbell",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "arms_close_grip_barbell_bench_press",
            name = "Close-Grip Barbell Bench Press",
            targetMuscleGroup = "Arms (Triceps - All Heads)",
            secondaryMuscles = listOf("Chest (Inner)", "Front Deltoids"),
            equipment = "Barbell",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "arms_skull_crusher_lying_triceps_extension_ez_bar",
            name = "Skull Crusher / Lying Triceps Extension (EZ-Bar)",
            targetMuscleGroup = "Arms (Triceps - Long & Medial Heads)",
            secondaryMuscles = listOf("Forearms"),
            equipment = "Barbell",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "arms_dumbbell_skull_crusher",
            name = "Dumbbell Skull Crusher",
            targetMuscleGroup = "Arms (Triceps)",
            secondaryMuscles = listOf("Forearms"),
            equipment = "Dumbbell",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "arms_cable_tricep_pushdown_rope_attachment",
            name = "Cable Tricep Pushdown (Rope Attachment)",
            targetMuscleGroup = "Arms (Triceps - Lateral Head)",
            secondaryMuscles = listOf("Core"),
            equipment = "Cable",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "arms_cable_tricep_pushdown_straight_v_bar",
            name = "Cable Tricep Pushdown (Straight / V-Bar)",
            targetMuscleGroup = "Arms (Triceps - Lateral & Medial Heads)",
            secondaryMuscles = listOf("Forearms"),
            equipment = "Cable",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "arms_overhead_cable_tricep_extension_rope",
            name = "Overhead Cable Tricep Extension (Rope)",
            targetMuscleGroup = "Arms (Triceps - Long Head)",
            secondaryMuscles = listOf("Core"),
            equipment = "Cable",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "arms_seated_overhead_dumbbell_tricep_extension",
            name = "Seated Overhead Dumbbell Tricep Extension",
            targetMuscleGroup = "Arms (Triceps - Long Head)",
            secondaryMuscles = listOf("Core"),
            equipment = "Dumbbell",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "arms_single_arm_overhead_dumbbell_tricep_extension",
            name = "Single-Arm Overhead Dumbbell Tricep Extension",
            targetMuscleGroup = "Arms (Triceps - Long Head)",
            secondaryMuscles = listOf("Forearms"),
            equipment = "Dumbbell",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "arms_single_arm_reverse_grip_cable_pushdown",
            name = "Single-Arm Reverse Grip Cable Pushdown",
            targetMuscleGroup = "Arms (Triceps - Medial Head)",
            secondaryMuscles = listOf("Forearms"),
            equipment = "Cable",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "arms_jm_press_smith_machine_barbell",
            name = "JM Press (Smith Machine / Barbell)",
            targetMuscleGroup = "Arms (Triceps)",
            secondaryMuscles = listOf("Chest", "Shoulders"),
            equipment = "Barbell",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "arms_tate_press_dumbbell",
            name = "Tate Press (Dumbbell)",
            targetMuscleGroup = "Arms (Triceps)",
            secondaryMuscles = listOf("Chest"),
            equipment = "Dumbbell",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "arms_tricep_dips_bench_chair",
            name = "Tricep Dips (Bench / Chair)",
            targetMuscleGroup = "Arms (Triceps)",
            secondaryMuscles = listOf("Chest", "Front Deltoids"),
            equipment = "Bodyweight",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "arms_parallel_bar_tricep_dips_upright_torso",
            name = "Parallel Bar Tricep Dips (Upright Torso)",
            targetMuscleGroup = "Arms (Triceps)",
            secondaryMuscles = listOf("Chest", "Front Deltoids"),
            equipment = "Bodyweight",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "arms_weighted_tricep_dips",
            name = "Weighted Tricep Dips",
            targetMuscleGroup = "Arms (Triceps)",
            secondaryMuscles = listOf("Chest", "Shoulders"),
            equipment = "Bodyweight / Weight Belt",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "arms_dumbbell_tricep_kickback",
            name = "Dumbbell Tricep Kickback",
            targetMuscleGroup = "Arms (Triceps - Lateral Head)",
            secondaryMuscles = listOf("Rear Deltoids"),
            equipment = "Dumbbell",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "arms_cable_tricep_kickback",
            name = "Cable Tricep Kickback",
            targetMuscleGroup = "Arms (Triceps)",
            secondaryMuscles = listOf("Rear Deltoids"),
            equipment = "Cable",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "arms_machine_tricep_dip",
            name = "Machine Tricep Dip",
            targetMuscleGroup = "Arms (Triceps)",
            secondaryMuscles = listOf("Chest"),
            equipment = "Machine",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "arms_machine_tricep_extension",
            name = "Machine Tricep Extension",
            targetMuscleGroup = "Arms (Triceps)",
            secondaryMuscles = emptyList(),
            equipment = "Machine",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "arms_cross_body_cable_triceps_extension_katana",
            name = "Cross-Body Cable Triceps Extension (Katana Extension)",
            targetMuscleGroup = "Arms (Triceps - Long Head)",
            secondaryMuscles = emptyList(),
            equipment = "Cable",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "arms_barbell_wrist_curl_forearms_over_bench",
            name = "Barbell Wrist Curl (Forearms Over Bench)",
            targetMuscleGroup = "Arms (Forearm Flexors)",
            secondaryMuscles = listOf("Grip Strength"),
            equipment = "Barbell",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "arms_barbell_reverse_wrist_curl",
            name = "Barbell Reverse Wrist Curl",
            targetMuscleGroup = "Arms (Forearm Extensors)",
            secondaryMuscles = listOf("Grip Strength"),
            equipment = "Barbell",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "arms_dumbbell_wrist_curl",
            name = "Dumbbell Wrist Curl",
            targetMuscleGroup = "Arms (Forearm Flexors)",
            secondaryMuscles = listOf("Grip Strength"),
            equipment = "Dumbbell",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "arms_dumbbell_reverse_wrist_curl",
            name = "Dumbbell Reverse Wrist Curl",
            targetMuscleGroup = "Arms (Forearm Extensors)",
            secondaryMuscles = listOf("Grip Strength"),
            equipment = "Dumbbell",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "arms_standing_behind_the_back_barbell_wrist_curl",
            name = "Standing Behind-the-Back Barbell Wrist Curl",
            targetMuscleGroup = "Arms (Forearm Flexors)",
            secondaryMuscles = listOf("Grip Strength"),
            equipment = "Barbell",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "arms_farmers_walk_heavy_dumbbell_carry",
            name = "Farmers Walk (Heavy Dumbbell Carry)",
            targetMuscleGroup = "Arms (Forearms / Grip)",
            secondaryMuscles = listOf("Traps", "Core", "Calves"),
            equipment = "Dumbbell",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "arms_trap_bar_farmers_carry",
            name = "Trap Bar Farmers Carry",
            targetMuscleGroup = "Arms (Forearms / Grip)",
            secondaryMuscles = listOf("Traps", "Core", "Legs"),
            equipment = "Barbell",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "arms_dead_hang_bar_pull_up_hang",
            name = "Dead Hang (Bar Pull-Up Hang)",
            targetMuscleGroup = "Arms (Forearms / Grip)",
            secondaryMuscles = listOf("Lats", "Shoulders"),
            equipment = "Bodyweight",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "arms_plate_pinch_hold",
            name = "Plate Pinch Hold",
            targetMuscleGroup = "Arms (Pinch Grip / Forearms)",
            secondaryMuscles = listOf("Finger Extensors"),
            equipment = "Other",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "arms_wrist_roller_cable_weight_plate",
            name = "Wrist Roller (Cable / Weight Plate)",
            targetMuscleGroup = "Arms (Forearms - Flexors & Extensors)",
            secondaryMuscles = listOf("Shoulders"),
            equipment = "Other",
            category = "Isolation"
        )
    )

    val armExercises: List<ExerciseEntity> = armExerciseItems.map { it.toEntity() }

    val coreAndCardioExerciseItems = listOf(
        DefaultExerciseItem(
            id = "core_hanging_leg_raise",
            name = "Hanging Leg Raise",
            targetMuscleGroup = "Core (Lower Abs)",
            secondaryMuscles = listOf("Hip Flexors", "Forearms / Grip"),
            equipment = "Bodyweight",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "core_hanging_knee_raise",
            name = "Hanging Knee Raise",
            targetMuscleGroup = "Core (Lower Abs)",
            secondaryMuscles = listOf("Hip Flexors", "Grip"),
            equipment = "Bodyweight",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "core_captains_chair_leg_raise",
            name = "Captain's Chair Leg Raise",
            targetMuscleGroup = "Core (Lower Abs)",
            secondaryMuscles = listOf("Hip Flexors"),
            equipment = "Machine",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "core_cable_kneeling_crunch",
            name = "Cable Kneeling Crunch",
            targetMuscleGroup = "Core (Upper Abs / Rectus Abdominis)",
            secondaryMuscles = listOf("Serratus"),
            equipment = "Cable",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "core_machine_abdominal_crunch",
            name = "Machine Abdominal Crunch",
            targetMuscleGroup = "Core (Rectus Abdominis)",
            secondaryMuscles = emptyList(),
            equipment = "Machine",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "core_decline_bench_weighted_crunch",
            name = "Decline Bench Weighted Crunch",
            targetMuscleGroup = "Core (Upper Abs)",
            secondaryMuscles = listOf("Hip Flexors"),
            equipment = "Other",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "core_ab_wheel_rollout_from_knees",
            name = "Ab Wheel Rollout (From Knees)",
            targetMuscleGroup = "Core (Full Rectus Abdominis / Anti-Extension)",
            secondaryMuscles = listOf("Lats", "Shoulders"),
            equipment = "Other",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "core_standing_ab_wheel_rollout",
            name = "Standing Ab Wheel Rollout",
            targetMuscleGroup = "Core (Full Rectus Abdominis)",
            secondaryMuscles = listOf("Lats", "Shoulders", "Hip Flexors"),
            equipment = "Other",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "core_standard_floor_plank",
            name = "Standard Floor Plank",
            targetMuscleGroup = "Core (Transverse Abdominis / Core Stability)",
            secondaryMuscles = listOf("Shoulders", "Glutes"),
            equipment = "Bodyweight",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "core_weighted_plank",
            name = "Weighted Plank",
            targetMuscleGroup = "Core (Transverse Abdominis)",
            secondaryMuscles = listOf("Shoulders", "Glutes"),
            equipment = "Other",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "core_side_plank_left_and_right",
            name = "Side Plank (Left & Right)",
            targetMuscleGroup = "Core (Obliques)",
            secondaryMuscles = listOf("Abductors", "Shoulders"),
            equipment = "Bodyweight",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "core_cable_woodchopper_high_to_low",
            name = "Cable Woodchopper (High to Low)",
            targetMuscleGroup = "Core (Obliques / Rotational Power)",
            secondaryMuscles = listOf("Shoulders"),
            equipment = "Cable",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "core_cable_woodchopper_low_to_high",
            name = "Cable Woodchopper (Low to High)",
            targetMuscleGroup = "Core (Obliques)",
            secondaryMuscles = listOf("Shoulders", "Hip Rotators"),
            equipment = "Cable",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "core_russian_twist_bodyweight_plate",
            name = "Russian Twist (Bodyweight / Plate)",
            targetMuscleGroup = "Core (Obliques)",
            secondaryMuscles = listOf("Rectus Abdominis"),
            equipment = "Dumbbell",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "core_bicycle_crunch",
            name = "Bicycle Crunch",
            targetMuscleGroup = "Core (Rectus Abdominis & Obliques)",
            secondaryMuscles = listOf("Hip Flexors"),
            equipment = "Bodyweight",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "core_dragon_flag",
            name = "Dragon Flag",
            targetMuscleGroup = "Core (Full Abdominal Wall)",
            secondaryMuscles = listOf("Lower Back", "Lats"),
            equipment = "Bodyweight",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "core_l_sit_hold_parallettes_floor",
            name = "L-Sit Hold (Parallettes / Floor)",
            targetMuscleGroup = "Core (Lower Abs / Hip Flexors)",
            secondaryMuscles = listOf("Triceps", "Shoulders"),
            equipment = "Bodyweight",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "core_hollow_body_hold",
            name = "Hollow Body Hold",
            targetMuscleGroup = "Core (Transverse Abdominis)",
            secondaryMuscles = listOf("Quads"),
            equipment = "Bodyweight",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "core_dead_bug",
            name = "Dead Bug",
            targetMuscleGroup = "Core (Deep Core Stabilizers)",
            secondaryMuscles = listOf("Lower Back"),
            equipment = "Bodyweight",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "core_v_up",
            name = "V-Up",
            targetMuscleGroup = "Core (Rectus Abdominis)",
            secondaryMuscles = listOf("Hip Flexors"),
            equipment = "Bodyweight",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "core_reverse_crunch_flat_bench",
            name = "Reverse Crunch (Flat Bench)",
            targetMuscleGroup = "Core (Lower Abs)",
            secondaryMuscles = emptyList(),
            equipment = "Bodyweight",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "core_landmine_180_landmine_twist",
            name = "Landmine 180 / Landmine Twist",
            targetMuscleGroup = "Core (Obliques / Anti-Rotation)",
            secondaryMuscles = listOf("Shoulders", "Arms"),
            equipment = "Barbell",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "core_pallof_press_cable",
            name = "Pallof Press (Cable)",
            targetMuscleGroup = "Core (Anti-Rotation / Core Stiffness)",
            secondaryMuscles = listOf("Chest", "Shoulders"),
            equipment = "Cable",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "core_dumbbell_side_bend",
            name = "Dumbbell Side Bend",
            targetMuscleGroup = "Core (Obliques / Quadratus Lumborum)",
            secondaryMuscles = listOf("Forearms"),
            equipment = "Dumbbell",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "core_mountain_climbers",
            name = "Mountain Climbers",
            targetMuscleGroup = "Core (Abs / Cardiovascular)",
            secondaryMuscles = listOf("Shoulders", "Quads"),
            equipment = "Bodyweight",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "core_toe_touches",
            name = "Toe Touches",
            targetMuscleGroup = "Core (Upper Abs)",
            secondaryMuscles = listOf("Hamstrings (Flexibility)"),
            equipment = "Bodyweight",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "core_flutter_kicks",
            name = "Flutter Kicks",
            targetMuscleGroup = "Core (Lower Abs)",
            secondaryMuscles = listOf("Hip Flexors"),
            equipment = "Bodyweight",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "core_scissor_kicks",
            name = "Scissor Kicks",
            targetMuscleGroup = "Core (Lower Abs / Adductors)",
            secondaryMuscles = listOf("Hip Flexors"),
            equipment = "Bodyweight",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "core_standing_cable_oblique_crunch",
            name = "Standing Cable Oblique Crunch",
            targetMuscleGroup = "Core (Obliques)",
            secondaryMuscles = listOf("Latissimus Dorsi"),
            equipment = "Cable",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "core_swiss_ball_pike",
            name = "Swiss Ball Pike",
            targetMuscleGroup = "Core (Lower Abs / Balance)",
            secondaryMuscles = listOf("Shoulders"),
            equipment = "Other",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "core_barbell_rollout",
            name = "Barbell Rollout",
            targetMuscleGroup = "Core (Full Abdominal Wall)",
            secondaryMuscles = listOf("Lats", "Triceps"),
            equipment = "Barbell",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "cardio_burpees",
            name = "Burpees",
            targetMuscleGroup = "Full Body / Cardio",
            secondaryMuscles = listOf("Chest", "Quads", "Core"),
            equipment = "Bodyweight",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "cardio_kettlebell_swing_russian_american",
            name = "Kettlebell Swing (Russian / American)",
            targetMuscleGroup = "Posterior Chain / Cardio",
            secondaryMuscles = listOf("Glutes", "Hamstrings", "Core", "Grip"),
            equipment = "Other",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "cardio_battle_ropes_alternating_waves",
            name = "Battle Ropes (Alternating Waves)",
            targetMuscleGroup = "Full Body / Conditioning",
            secondaryMuscles = listOf("Shoulders", "Arms", "Core"),
            equipment = "Other",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "cardio_sled_push_prowler_push",
            name = "Sled Push / Prowler Push",
            targetMuscleGroup = "Legs / Functional Conditioning",
            secondaryMuscles = listOf("Quads", "Glutes", "Calves", "Core"),
            equipment = "Machine",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "cardio_sled_pull_backward_drag",
            name = "Sled Pull (Backward Drag)",
            targetMuscleGroup = "Legs (Quads / Knees VMO)",
            secondaryMuscles = listOf("Calves", "Core"),
            equipment = "Machine",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "cardio_medicine_ball_slam",
            name = "Medicine Ball Slam",
            targetMuscleGroup = "Full Body / Explosive Power",
            secondaryMuscles = listOf("Lats", "Core", "Shoulders"),
            equipment = "Other",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "cardio_box_jump",
            name = "Box Jump",
            targetMuscleGroup = "Legs / Plyometrics",
            secondaryMuscles = listOf("Quads", "Calves", "Glutes"),
            equipment = "Other",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "cardio_jump_rope_skipping",
            name = "Jump Rope (Skipping)",
            targetMuscleGroup = "Calves / Cardiovascular",
            secondaryMuscles = listOf("Forearms", "Shoulders"),
            equipment = "Other",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "cardio_rowing_machine_ergometer",
            name = "Rowing Machine (Ergometer)",
            targetMuscleGroup = "Full Body / Cardio",
            secondaryMuscles = listOf("Back", "Legs", "Core", "Biceps"),
            equipment = "Machine",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "cardio_assault_bike_air_bike_sprint",
            name = "Assault Bike / Air Bike Sprint",
            targetMuscleGroup = "Full Body / Cardio",
            secondaryMuscles = listOf("Quads", "Shoulders", "Core"),
            equipment = "Machine",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "cardio_stair_climber",
            name = "Stair Climber",
            targetMuscleGroup = "Legs / Cardio",
            secondaryMuscles = listOf("Glutes", "Calves", "Hamstrings"),
            equipment = "Machine",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "cardio_incline_treadmill_walk",
            name = "Incline Treadmill Walk",
            targetMuscleGroup = "Posterior Chain / Low-Impact Cardio",
            secondaryMuscles = listOf("Calves", "Hamstrings", "Glutes"),
            equipment = "Machine",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "cardio_turkish_get_up",
            name = "Turkish Get-Up",
            targetMuscleGroup = "Full Body / Shoulder Stability",
            secondaryMuscles = listOf("Core", "Glutes", "Shoulders"),
            equipment = "Dumbbell",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "cardio_bear_crawl",
            name = "Bear Crawl",
            targetMuscleGroup = "Core / Functional Mobility",
            secondaryMuscles = listOf("Shoulders", "Quads"),
            equipment = "Bodyweight",
            category = "Compound"
        )
    )

    val coreAndCardioExercises: List<ExerciseEntity> = coreAndCardioExerciseItems.map { it.toEntity() }

    val allDefaultExercises: List<ExerciseEntity> =
        chestExercises +
        backExercises +
        shoulderExercises +
        legExercises +
        armExercises +
        coreAndCardioExercises

    val allInitialExercises: List<ExerciseEntity> = allDefaultExercises
}


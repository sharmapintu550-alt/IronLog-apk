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
        // 1. BARBELL (9 items)
        DefaultExerciseItem(
            id = "chest_bb_flat_bench_standard",
            name = "Flat Barbell Bench Press (Standard Grip)",
            targetMuscleGroup = "Chest",
            secondaryMuscles = listOf("Triceps", "Front Deltoids"),
            equipment = "Barbell",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "chest_bb_flat_bench_wide",
            name = "Flat Barbell Bench Press (Wide Grip)",
            targetMuscleGroup = "Chest",
            secondaryMuscles = listOf("Front Deltoids", "Triceps"),
            equipment = "Barbell",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "chest_bb_flat_bench_close",
            name = "Flat Barbell Bench Press (Close Grip)",
            targetMuscleGroup = "Chest (Inner)",
            secondaryMuscles = listOf("Triceps", "Front Deltoids"),
            equipment = "Barbell",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "chest_bb_incline_30",
            name = "Incline Barbell Bench Press (30 Degree)",
            targetMuscleGroup = "Chest (Upper)",
            secondaryMuscles = listOf("Front Deltoids", "Triceps"),
            equipment = "Barbell",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "chest_bb_incline_45",
            name = "Incline Barbell Bench Press (45 Degree)",
            targetMuscleGroup = "Chest (Upper)",
            secondaryMuscles = listOf("Front Deltoids", "Triceps"),
            equipment = "Barbell",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "chest_bb_incline_close",
            name = "Incline Barbell Bench Press (Close Grip)",
            targetMuscleGroup = "Chest (Upper)",
            secondaryMuscles = listOf("Triceps", "Front Deltoids"),
            equipment = "Barbell",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "chest_bb_decline",
            name = "Decline Barbell Bench Press",
            targetMuscleGroup = "Chest (Lower)",
            secondaryMuscles = listOf("Triceps", "Front Deltoids"),
            equipment = "Barbell",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "chest_bb_reverse_grip",
            name = "Reverse-Grip Barbell Bench Press (Upper Chest Focus)",
            targetMuscleGroup = "Chest (Upper)",
            secondaryMuscles = listOf("Triceps", "Biceps"),
            equipment = "Barbell",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "chest_bb_guillotine",
            name = "Guillotine Bench Press (Neck Press / High Flare)",
            targetMuscleGroup = "Chest (Upper)",
            secondaryMuscles = listOf("Front Deltoids", "Rotator Cuff"),
            equipment = "Barbell",
            category = "Compound"
        ),

        // 2. DUMBBELL (15 items)
        DefaultExerciseItem(
            id = "chest_db_flat_bench_pronated",
            name = "Flat Dumbbell Bench Press (Pronated Grip)",
            targetMuscleGroup = "Chest",
            secondaryMuscles = listOf("Triceps", "Front Deltoids"),
            equipment = "Dumbbell",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "chest_db_flat_bench_neutral",
            name = "Flat Dumbbell Bench Press (Neutral Grip)",
            targetMuscleGroup = "Chest",
            secondaryMuscles = listOf("Triceps", "Front Deltoids"),
            equipment = "Dumbbell",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "chest_db_incline_30",
            name = "Incline Dumbbell Bench Press (30 Degree)",
            targetMuscleGroup = "Chest (Upper)",
            secondaryMuscles = listOf("Front Deltoids", "Triceps"),
            equipment = "Dumbbell",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "chest_db_incline_45",
            name = "Incline Dumbbell Bench Press (45 Degree)",
            targetMuscleGroup = "Chest (Upper)",
            secondaryMuscles = listOf("Front Deltoids", "Triceps"),
            equipment = "Dumbbell",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "chest_db_incline_neutral",
            name = "Incline Dumbbell Bench Press (Neutral Grip)",
            targetMuscleGroup = "Chest (Upper)",
            secondaryMuscles = listOf("Front Deltoids", "Triceps"),
            equipment = "Dumbbell",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "chest_db_decline",
            name = "Decline Dumbbell Bench Press",
            targetMuscleGroup = "Chest (Lower)",
            secondaryMuscles = listOf("Triceps"),
            equipment = "Dumbbell",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "chest_db_flat_fly",
            name = "Flat Dumbbell Fly",
            targetMuscleGroup = "Chest",
            secondaryMuscles = listOf("Front Deltoids"),
            equipment = "Dumbbell",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "chest_db_incline_fly_30",
            name = "Incline Dumbbell Fly (30 Degree)",
            targetMuscleGroup = "Chest (Upper)",
            secondaryMuscles = listOf("Front Deltoids"),
            equipment = "Dumbbell",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "chest_db_incline_fly_45",
            name = "Incline Dumbbell Fly (45 Degree)",
            targetMuscleGroup = "Chest (Upper)",
            secondaryMuscles = listOf("Front Deltoids"),
            equipment = "Dumbbell",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "chest_db_decline_fly",
            name = "Decline Dumbbell Fly",
            targetMuscleGroup = "Chest (Lower)",
            secondaryMuscles = listOf("Front Deltoids"),
            equipment = "Dumbbell",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "chest_db_pullover",
            name = "Dumbbell Pullover (Chest Focus)",
            targetMuscleGroup = "Chest",
            secondaryMuscles = listOf("Lats", "Serratus Anterior", "Triceps"),
            equipment = "Dumbbell",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "chest_db_incline_squeeze_hex",
            name = "Incline Dumbbell Squeeze Press (Hex Press)",
            targetMuscleGroup = "Chest (Upper)",
            secondaryMuscles = listOf("Triceps", "Front Deltoids"),
            equipment = "Dumbbell",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "chest_db_flat_squeeze_hex",
            name = "Flat Dumbbell Squeeze Press (Hex Press)",
            targetMuscleGroup = "Chest (Inner)",
            secondaryMuscles = listOf("Triceps", "Front Deltoids"),
            equipment = "Dumbbell",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "chest_db_single_arm_flat",
            name = "Single-Arm Dumbbell Flat Bench Press",
            targetMuscleGroup = "Chest",
            secondaryMuscles = listOf("Core", "Triceps", "Front Deltoids"),
            equipment = "Dumbbell",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "chest_db_single_arm_incline",
            name = "Single-Arm Dumbbell Incline Press",
            targetMuscleGroup = "Chest (Upper)",
            secondaryMuscles = listOf("Core", "Front Deltoids", "Triceps"),
            equipment = "Dumbbell",
            category = "Compound"
        ),

        // 3. CABLE (11 items)
        DefaultExerciseItem(
            id = "chest_cable_crossover_high_to_low",
            name = "Cable Crossover (High-to-Low / Lower Chest)",
            targetMuscleGroup = "Chest (Lower)",
            secondaryMuscles = listOf("Front Deltoids"),
            equipment = "Cable",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "chest_cable_fly_mid",
            name = "Cable Fly (Mid-Chest / Level Height)",
            targetMuscleGroup = "Chest",
            secondaryMuscles = listOf("Front Deltoids"),
            equipment = "Cable",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "chest_cable_low_to_high_fly",
            name = "Low-to-High Cable Fly (Upper Chest Clavicular Focus)",
            targetMuscleGroup = "Chest (Upper)",
            secondaryMuscles = listOf("Front Deltoids"),
            equipment = "Cable",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "chest_cable_seated_incline_press",
            name = "Seated Incline Cable Press",
            targetMuscleGroup = "Chest (Upper)",
            secondaryMuscles = listOf("Front Deltoids", "Triceps"),
            equipment = "Cable",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "chest_cable_seated_flat_press",
            name = "Seated Flat Cable Chest Press",
            targetMuscleGroup = "Chest",
            secondaryMuscles = listOf("Triceps", "Front Deltoids"),
            equipment = "Cable",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "chest_cable_single_arm_crossover_high_to_low",
            name = "Single-Arm Cable Crossover (High-to-Low)",
            targetMuscleGroup = "Chest (Lower)",
            secondaryMuscles = listOf("Front Deltoids", "Core"),
            equipment = "Cable",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "chest_cable_single_arm_fly_mid",
            name = "Single-Arm Cable Fly (Mid-Chest)",
            targetMuscleGroup = "Chest",
            secondaryMuscles = listOf("Front Deltoids", "Core"),
            equipment = "Cable",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "chest_cable_single_arm_low_to_high_fly",
            name = "Single-Arm Low-to-High Cable Fly",
            targetMuscleGroup = "Chest (Upper)",
            secondaryMuscles = listOf("Front Deltoids", "Core"),
            equipment = "Cable",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "chest_cable_standing_chest_press_staggered",
            name = "Standing Cable Chest Press (Staggered Stance)",
            targetMuscleGroup = "Chest",
            secondaryMuscles = listOf("Core", "Triceps", "Front Deltoids"),
            equipment = "Cable",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "chest_cable_lying_fly_flat_bench",
            name = "Lying Cable Fly on Flat Bench",
            targetMuscleGroup = "Chest",
            secondaryMuscles = listOf("Front Deltoids"),
            equipment = "Cable",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "chest_cable_lying_fly_incline_bench",
            name = "Lying Cable Fly on Incline Bench",
            targetMuscleGroup = "Chest (Upper)",
            secondaryMuscles = listOf("Front Deltoids"),
            equipment = "Cable",
            category = "Isolation"
        ),

        // 4. MACHINE (9 items)
        DefaultExerciseItem(
            id = "chest_mach_chest_press_seated_flat",
            name = "Machine Chest Press (Seated Flat / Horizontal)",
            targetMuscleGroup = "Chest",
            secondaryMuscles = listOf("Triceps", "Front Deltoids"),
            equipment = "Machine",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "chest_mach_incline_chest_press",
            name = "Machine Incline Chest Press",
            targetMuscleGroup = "Chest (Upper)",
            secondaryMuscles = listOf("Front Deltoids", "Triceps"),
            equipment = "Machine",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "chest_mach_decline_chest_press",
            name = "Machine Decline Chest Press",
            targetMuscleGroup = "Chest (Lower)",
            secondaryMuscles = listOf("Triceps"),
            equipment = "Machine",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "chest_mach_converging_chest_press",
            name = "Machine Converging Chest Press",
            targetMuscleGroup = "Chest",
            secondaryMuscles = listOf("Triceps", "Front Deltoids"),
            equipment = "Machine",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "chest_mach_plate_loaded_incline",
            name = "Plate-Loaded Incline Chest Press (Iso-Lateral)",
            targetMuscleGroup = "Chest (Upper)",
            secondaryMuscles = listOf("Front Deltoids", "Triceps"),
            equipment = "Machine",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "chest_mach_plate_loaded_flat",
            name = "Plate-Loaded Flat Chest Press (Iso-Lateral)",
            targetMuscleGroup = "Chest",
            secondaryMuscles = listOf("Triceps", "Front Deltoids"),
            equipment = "Machine",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "chest_mach_plate_loaded_decline",
            name = "Plate-Loaded Decline Chest Press (Iso-Lateral)",
            targetMuscleGroup = "Chest (Lower)",
            secondaryMuscles = listOf("Triceps"),
            equipment = "Machine",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "chest_mach_pec_deck_fly_seated",
            name = "Pec Deck Fly Machine (Seated)",
            targetMuscleGroup = "Chest",
            secondaryMuscles = listOf("Front Deltoids"),
            equipment = "Machine",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "chest_mach_single_arm_chest_press",
            name = "Single-Arm Machine Chest Press",
            targetMuscleGroup = "Chest",
            secondaryMuscles = listOf("Core", "Triceps", "Front Deltoids"),
            equipment = "Machine",
            category = "Compound"
        ),

        // 5. SMITH MACHINE (6 items)
        DefaultExerciseItem(
            id = "chest_smith_flat_bench_press",
            name = "Smith Machine Flat Bench Press",
            targetMuscleGroup = "Chest",
            secondaryMuscles = listOf("Triceps", "Front Deltoids"),
            equipment = "Smith Machine",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "chest_smith_incline_bench_30",
            name = "Smith Machine Incline Bench Press (30 Degree)",
            targetMuscleGroup = "Chest (Upper)",
            secondaryMuscles = listOf("Front Deltoids", "Triceps"),
            equipment = "Smith Machine",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "chest_smith_incline_bench_45",
            name = "Smith Machine Incline Bench Press (45 Degree)",
            targetMuscleGroup = "Chest (Upper)",
            secondaryMuscles = listOf("Front Deltoids", "Triceps"),
            equipment = "Smith Machine",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "chest_smith_decline_bench_press",
            name = "Smith Machine Decline Bench Press",
            targetMuscleGroup = "Chest (Lower)",
            secondaryMuscles = listOf("Triceps"),
            equipment = "Smith Machine",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "chest_smith_close_grip_incline_press",
            name = "Smith Machine Close-Grip Incline Press",
            targetMuscleGroup = "Chest (Upper)",
            secondaryMuscles = listOf("Triceps", "Front Deltoids"),
            equipment = "Smith Machine",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "chest_smith_reverse_grip_bench_press",
            name = "Smith Machine Reverse-Grip Bench Press",
            targetMuscleGroup = "Chest (Upper)",
            secondaryMuscles = listOf("Triceps", "Biceps"),
            equipment = "Smith Machine",
            category = "Compound"
        ),

        // 6. BODYWEIGHT / CALISTHENICS (11 items)
        DefaultExerciseItem(
            id = "chest_bw_push_ups_standard",
            name = "Push-ups (Standard Grip)",
            targetMuscleGroup = "Chest",
            secondaryMuscles = listOf("Triceps", "Core", "Front Deltoids"),
            equipment = "Bodyweight",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "chest_bw_push_ups_wide",
            name = "Push-ups (Wide Grip)",
            targetMuscleGroup = "Chest",
            secondaryMuscles = listOf("Front Deltoids", "Triceps", "Core"),
            equipment = "Bodyweight",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "chest_bw_diamond_push_ups",
            name = "Diamond Push-ups (Chest & Triceps Focus)",
            targetMuscleGroup = "Chest (Inner)",
            secondaryMuscles = listOf("Triceps", "Core"),
            equipment = "Bodyweight",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "chest_bw_incline_push_ups",
            name = "Incline Push-ups (Hands Elevated / Lower Chest Focus)",
            targetMuscleGroup = "Chest (Lower)",
            secondaryMuscles = listOf("Triceps", "Core"),
            equipment = "Bodyweight",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "chest_bw_decline_push_ups",
            name = "Decline Push-ups (Feet Elevated / Upper Chest Focus)",
            targetMuscleGroup = "Chest (Upper)",
            secondaryMuscles = listOf("Front Deltoids", "Triceps", "Core"),
            equipment = "Bodyweight",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "chest_bw_deficit_push_ups",
            name = "Deficit Push-ups (Deep Stretch on Parallettes/Blocks)",
            targetMuscleGroup = "Chest",
            secondaryMuscles = listOf("Triceps", "Front Deltoids", "Core"),
            equipment = "Bodyweight",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "chest_bw_weighted_push_ups",
            name = "Weighted Push-ups",
            targetMuscleGroup = "Chest",
            secondaryMuscles = listOf("Triceps", "Front Deltoids", "Core"),
            equipment = "Bodyweight",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "chest_bw_chest_dips_leaning",
            name = "Chest Dips (Leaning Forward / Torso Angled)",
            targetMuscleGroup = "Chest (Lower)",
            secondaryMuscles = listOf("Triceps", "Front Deltoids"),
            equipment = "Bodyweight",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "chest_bw_weighted_chest_dips",
            name = "Weighted Chest Dips",
            targetMuscleGroup = "Chest (Lower)",
            secondaryMuscles = listOf("Triceps", "Front Deltoids"),
            equipment = "Bodyweight",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "chest_bw_ring_push_ups",
            name = "Ring Push-ups (Gymnastic Rings)",
            targetMuscleGroup = "Chest",
            secondaryMuscles = listOf("Core", "Shoulder Stabilizers", "Triceps"),
            equipment = "Bodyweight",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "chest_bw_ring_dips",
            name = "Ring Dips (Gymnastic Rings)",
            targetMuscleGroup = "Chest (Lower)",
            secondaryMuscles = listOf("Triceps", "Shoulder Stabilizers", "Core"),
            equipment = "Bodyweight",
            category = "Compound"
        )
    )

    val chestExercises: List<ExerciseEntity> = chestExerciseItems.map { it.toEntity() }

    val backExerciseItems = listOf(
        // 1. BARBELL (12 items)
        DefaultExerciseItem(
            id = "back_bb_conventional_deadlift",
            name = "Conventional Deadlift",
            targetMuscleGroup = "Back (Full / Lower)",
            secondaryMuscles = listOf("Hamstrings", "Glutes", "Forearms", "Traps"),
            equipment = "Barbell",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "back_bb_bent_over_pronated",
            name = "Barbell Bent-Over Row (Overhand / Pronated Grip)",
            targetMuscleGroup = "Back (Upper / Lats)",
            secondaryMuscles = listOf("Biceps", "Rear Deltoids", "Lower Back"),
            equipment = "Barbell",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "back_bb_bent_over_supinated_yates",
            name = "Barbell Bent-Over Row (Underhand / Supinated Grip / Yates Row)",
            targetMuscleGroup = "Back (Lower Lats)",
            secondaryMuscles = listOf("Biceps", "Forearms"),
            equipment = "Barbell",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "back_bb_pendlay_row",
            name = "Pendlay Row (Strict Dead-Stop from Floor)",
            targetMuscleGroup = "Back (Upper Back / Lats)",
            secondaryMuscles = listOf("Core", "Hamstrings", "Rear Deltoids"),
            equipment = "Barbell",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "back_bb_t_bar_landmine_neutral",
            name = "T-Bar Row (Barbell Corner / Landmine - Neutral Grip)",
            targetMuscleGroup = "Back (Mid-Back / Lats)",
            secondaryMuscles = listOf("Core", "Biceps"),
            equipment = "Barbell",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "back_bb_t_bar_wide_overhand",
            name = "T-Bar Row (Wide Overhand Grip)",
            targetMuscleGroup = "Back (Upper Back / Rhomboids)",
            secondaryMuscles = listOf("Rear Deltoids", "Biceps"),
            equipment = "Barbell",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "back_bb_shrugs_traps",
            name = "Barbell Shrugs (Traps Focus)",
            targetMuscleGroup = "Back (Upper Traps)",
            secondaryMuscles = listOf("Forearms"),
            equipment = "Barbell",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "back_bb_behind_the_back_shrugs",
            name = "Behind-the-Back Barbell Shrugs",
            targetMuscleGroup = "Back (Upper Traps)",
            secondaryMuscles = listOf("Forearms"),
            equipment = "Barbell",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "back_bb_good_mornings",
            name = "Barbell Good Mornings (Lower Back Focus)",
            targetMuscleGroup = "Back (Lower Back / Posterior Chain)",
            secondaryMuscles = listOf("Hamstrings", "Glutes"),
            equipment = "Barbell",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "back_bb_romanian_deadlift",
            name = "Romanian Deadlift (Back & Posterior Chain Focus)",
            targetMuscleGroup = "Back (Lower Back / Posterior Chain)",
            secondaryMuscles = listOf("Hamstrings", "Glutes", "Lats"),
            equipment = "Barbell",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "back_bb_snatch_grip_deadlift",
            name = "Snatch-Grip Deadlift (Upper Back & Traps)",
            targetMuscleGroup = "Back (Upper Back / Traps)",
            secondaryMuscles = listOf("Hamstrings", "Glutes", "Forearms"),
            equipment = "Barbell",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "back_bb_meadows_row",
            name = "Meadow's Row (Single-Arm Landmine Row)",
            targetMuscleGroup = "Back (Upper Lats / Rhomboids)",
            secondaryMuscles = listOf("Biceps", "Core", "Rear Deltoids"),
            equipment = "Barbell",
            category = "Compound"
        ),

        // 2. DUMBBELL (10 items)
        DefaultExerciseItem(
            id = "back_db_single_arm_row_bench",
            name = "Single-Arm Dumbbell Row (Knee on Bench)",
            targetMuscleGroup = "Back (Lats)",
            secondaryMuscles = listOf("Biceps", "Forearms", "Core"),
            equipment = "Dumbbell",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "back_db_single_arm_row_standing",
            name = "Single-Arm Dumbbell Row (Standing Supported)",
            targetMuscleGroup = "Back (Lats)",
            secondaryMuscles = listOf("Biceps", "Core", "Rear Deltoids"),
            equipment = "Dumbbell",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "back_db_chest_supported_row_neutral",
            name = "Chest-Supported Incline Dumbbell Row (Neutral Grip)",
            targetMuscleGroup = "Back (Mid-Back / Rhomboids)",
            secondaryMuscles = listOf("Rear Deltoids", "Biceps"),
            equipment = "Dumbbell",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "back_db_chest_supported_row_flared",
            name = "Chest-Supported Incline Dumbbell Row (Elbows Flared / Upper Back)",
            targetMuscleGroup = "Back (Upper Back / Rear Delts)",
            secondaryMuscles = listOf("Rhomboids", "Traps", "Biceps"),
            equipment = "Dumbbell",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "back_db_pullover_lat_focus",
            name = "Dumbbell Pullover (Lat Focus)",
            targetMuscleGroup = "Back (Lats)",
            secondaryMuscles = listOf("Teres Major", "Triceps"),
            equipment = "Dumbbell",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "back_db_deadlift",
            name = "Dumbbell Deadlift",
            targetMuscleGroup = "Back (Lower Back / Full Posterior Chain)",
            secondaryMuscles = listOf("Glutes", "Hamstrings", "Traps", "Forearms"),
            equipment = "Dumbbell",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "back_db_shrugs_neutral",
            name = "Dumbbell Shrugs (Neutral Grip)",
            targetMuscleGroup = "Back (Upper Traps)",
            secondaryMuscles = listOf("Forearms"),
            equipment = "Dumbbell",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "back_db_incline_shrugs",
            name = "Incline Dumbbell Shrugs",
            targetMuscleGroup = "Back (Upper & Mid Traps)",
            secondaryMuscles = listOf("Rear Deltoids"),
            equipment = "Dumbbell",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "back_db_seal_row",
            name = "Dumbbell Seal Row (Flat Bench Elevated)",
            targetMuscleGroup = "Back (Upper Back / Lats)",
            secondaryMuscles = listOf("Rear Deltoids", "Biceps"),
            equipment = "Dumbbell",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "back_db_incline_prone_row",
            name = "Incline Prone Dumbbell Row",
            targetMuscleGroup = "Back (Mid-Back / Rhomboids)",
            secondaryMuscles = listOf("Rear Deltoids", "Biceps"),
            equipment = "Dumbbell",
            category = "Compound"
        ),

        // 3. CABLE (14 items)
        DefaultExerciseItem(
            id = "back_cable_lat_pulldown_wide_overhand",
            name = "Lat Pulldown (Wide Overhand Grip)",
            targetMuscleGroup = "Back (Lats - Width)",
            secondaryMuscles = listOf("Biceps", "Rear Deltoids"),
            equipment = "Cable",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "back_cable_lat_pulldown_close_v_bar",
            name = "Lat Pulldown (Close Neutral Grip / V-Bar)",
            targetMuscleGroup = "Back (Lower Lats)",
            secondaryMuscles = listOf("Biceps", "Brachialis"),
            equipment = "Cable",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "back_cable_lat_pulldown_supinated",
            name = "Lat Pulldown (Underhand / Supinated Grip)",
            targetMuscleGroup = "Back (Lats / Biceps Focus)",
            secondaryMuscles = listOf("Biceps", "Forearms"),
            equipment = "Cable",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "back_cable_lat_pulldown_behind_neck",
            name = "Lat Pulldown (Behind-the-Neck)",
            targetMuscleGroup = "Back (Upper Back / Lats)",
            secondaryMuscles = listOf("Rear Deltoids", "Rotator Cuff"),
            equipment = "Cable",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "back_cable_single_arm_half_kneeling_pulldown",
            name = "Single-Arm Half-Kneeling Lat Pulldown",
            targetMuscleGroup = "Back (Lats - Iliac Division)",
            secondaryMuscles = listOf("Biceps", "Core"),
            equipment = "Cable",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "back_cable_seated_row_close_v_bar",
            name = "Seated Cable Row (Close Neutral Grip / V-Bar)",
            targetMuscleGroup = "Back (Mid-Back / Lats)",
            secondaryMuscles = listOf("Biceps", "Forearms"),
            equipment = "Cable",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "back_cable_seated_row_wide_overhand",
            name = "Seated Cable Row (Wide Grip Bar - Overhand)",
            targetMuscleGroup = "Back (Upper Back / Rhomboids / Rear Delts)",
            secondaryMuscles = listOf("Biceps", "Traps"),
            equipment = "Cable",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "back_cable_seated_row_wide_neutral",
            name = "Seated Cable Row (Wide Neutral Grip)",
            targetMuscleGroup = "Back (Mid-Back / Rhomboids)",
            secondaryMuscles = listOf("Biceps", "Rear Deltoids"),
            equipment = "Cable",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "back_cable_single_arm_seated_row",
            name = "Single-Arm Seated Cable Row (Lat Sweep Focus)",
            targetMuscleGroup = "Back (Lats)",
            secondaryMuscles = listOf("Biceps", "Core"),
            equipment = "Cable",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "back_cable_straight_arm_pulldown_rope",
            name = "Standing Straight-Arm Cable Pulldown (Rope Attachment)",
            targetMuscleGroup = "Back (Lats - Isolation)",
            secondaryMuscles = listOf("Triceps Long Head", "Core"),
            equipment = "Cable",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "back_cable_straight_arm_pulldown_bar",
            name = "Standing Straight-Arm Cable Pulldown (Straight Bar)",
            targetMuscleGroup = "Back (Lats - Isolation)",
            secondaryMuscles = listOf("Triceps Long Head"),
            equipment = "Cable",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "back_cable_face_pull_rope",
            name = "Cable Face Pull (Rope - Upper Traps & Rear Delts)",
            targetMuscleGroup = "Back (Upper Back / Traps / Rear Delts)",
            secondaryMuscles = listOf("Rotator Cuff"),
            equipment = "Cable",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "back_cable_high_row",
            name = "High Cable Row (Upper Lat / Mid Back)",
            targetMuscleGroup = "Back (Upper Lat / Mid Back)",
            secondaryMuscles = listOf("Biceps", "Rear Deltoids"),
            equipment = "Cable",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "back_cable_shrugs",
            name = "Cable Shrugs (Traps Focus)",
            targetMuscleGroup = "Back (Upper Traps)",
            secondaryMuscles = listOf("Forearms"),
            equipment = "Cable",
            category = "Isolation"
        ),

        // 4. MACHINE (9 items)
        DefaultExerciseItem(
            id = "back_mach_plate_loaded_iso_lat_pulldown",
            name = "Plate-Loaded Iso-Lateral Front Lat Pulldown",
            targetMuscleGroup = "Back (Lats - Width)",
            secondaryMuscles = listOf("Biceps", "Rear Deltoids"),
            equipment = "Machine",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "back_mach_plate_loaded_iso_high_row",
            name = "Plate-Loaded Iso-Lateral High Row",
            targetMuscleGroup = "Back (Upper Lats / Rhomboids)",
            secondaryMuscles = listOf("Biceps", "Rear Deltoids"),
            equipment = "Machine",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "back_mach_plate_loaded_iso_low_row",
            name = "Plate-Loaded Iso-Lateral Low Row",
            targetMuscleGroup = "Back (Lower Lats)",
            secondaryMuscles = listOf("Biceps"),
            equipment = "Machine",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "back_mach_seated_row_chest_supported_neutral",
            name = "Seated Machine Row (Chest Supported - Neutral Grip)",
            targetMuscleGroup = "Back (Mid-Back / Lats)",
            secondaryMuscles = listOf("Biceps", "Rear Deltoids"),
            equipment = "Machine",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "back_mach_seated_row_wide_overhand",
            name = "Seated Machine Row (Wide Overhand Grip - Upper Back Focus)",
            targetMuscleGroup = "Back (Upper Back / Rhomboids)",
            secondaryMuscles = listOf("Rear Deltoids", "Traps", "Biceps"),
            equipment = "Machine",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "back_mach_lat_pulldown_dual_axis",
            name = "Machine Lat Pulldown (Dual Axis / Pin-Loaded)",
            targetMuscleGroup = "Back (Lats)",
            secondaryMuscles = listOf("Biceps"),
            equipment = "Machine",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "back_mach_t_bar_row_chest_supported",
            name = "T-Bar Row Machine (Chest Supported)",
            targetMuscleGroup = "Back (Mid-Back / Rhomboids)",
            secondaryMuscles = listOf("Biceps", "Rear Deltoids"),
            equipment = "Machine",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "back_mach_shrug",
            name = "Machine Shrug (Seated or Standing)",
            targetMuscleGroup = "Back (Upper Traps)",
            secondaryMuscles = listOf("Forearms"),
            equipment = "Machine",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "back_mach_back_extension",
            name = "Back Extension Machine (Hyperextension)",
            targetMuscleGroup = "Back (Lower Back / Erector Spinae)",
            secondaryMuscles = listOf("Glutes", "Hamstrings"),
            equipment = "Machine",
            category = "Compound"
        ),

        // 5. SMITH MACHINE (6 items)
        DefaultExerciseItem(
            id = "back_smith_bent_over_row_overhand",
            name = "Smith Machine Bent-Over Row (Overhand Grip)",
            targetMuscleGroup = "Back (Upper Back / Lats)",
            secondaryMuscles = listOf("Biceps", "Rear Deltoids", "Core"),
            equipment = "Smith Machine",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "back_smith_bent_over_row_underhand",
            name = "Smith Machine Bent-Over Row (Underhand Grip)",
            targetMuscleGroup = "Back (Lower Lats)",
            secondaryMuscles = listOf("Biceps", "Core"),
            equipment = "Smith Machine",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "back_smith_romanian_deadlift",
            name = "Smith Machine Romanian Deadlift",
            targetMuscleGroup = "Back (Lower Back / Posterior Chain)",
            secondaryMuscles = listOf("Hamstrings", "Glutes"),
            equipment = "Smith Machine",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "back_smith_shrugs_front",
            name = "Smith Machine Shrugs (Front)",
            targetMuscleGroup = "Back (Upper Traps)",
            secondaryMuscles = listOf("Forearms"),
            equipment = "Smith Machine",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "back_smith_shrugs_behind_back",
            name = "Smith Machine Shrugs (Behind the Back)",
            targetMuscleGroup = "Back (Upper Traps)",
            secondaryMuscles = listOf("Forearms"),
            equipment = "Smith Machine",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "back_smith_inverted_bodyweight_row",
            name = "Smith Machine Inverted Bodyweight Row",
            targetMuscleGroup = "Back (Mid-Back / Rhomboids)",
            secondaryMuscles = listOf("Biceps", "Core"),
            equipment = "Smith Machine",
            category = "Compound"
        ),

        // 6. BODYWEIGHT / CALISTHENICS (11 items)
        DefaultExerciseItem(
            id = "back_bw_pull_ups_standard_overhand",
            name = "Pull-ups (Standard Overhand Grip)",
            targetMuscleGroup = "Back (Lats - Width)",
            secondaryMuscles = listOf("Biceps", "Forearms", "Core"),
            equipment = "Bodyweight",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "back_bw_pull_ups_wide_grip",
            name = "Pull-ups (Wide Grip)",
            targetMuscleGroup = "Back (Lats - Width)",
            secondaryMuscles = listOf("Biceps", "Core"),
            equipment = "Bodyweight",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "back_bw_chin_ups_underhand",
            name = "Chin-ups (Underhand / Supinated Grip)",
            targetMuscleGroup = "Back (Lats / Biceps)",
            secondaryMuscles = listOf("Biceps", "Forearms"),
            equipment = "Bodyweight",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "back_bw_neutral_grip_pull_ups",
            name = "Neutral-Grip Pull-ups (Parallel Grips)",
            targetMuscleGroup = "Back (Lats)",
            secondaryMuscles = listOf("Brachialis", "Biceps"),
            equipment = "Bodyweight",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "back_bw_weighted_pull_ups",
            name = "Weighted Pull-ups",
            targetMuscleGroup = "Back (Lats)",
            secondaryMuscles = listOf("Biceps", "Core"),
            equipment = "Bodyweight",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "back_bw_weighted_chin_ups",
            name = "Weighted Chin-ups",
            targetMuscleGroup = "Back (Lats / Biceps)",
            secondaryMuscles = listOf("Biceps", "Core"),
            equipment = "Bodyweight",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "back_bw_inverted_rows_australian",
            name = "Inverted Rows (Australian Pull-ups)",
            targetMuscleGroup = "Back (Mid-Back / Rhomboids)",
            secondaryMuscles = listOf("Biceps", "Core"),
            equipment = "Bodyweight",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "back_bw_inverted_rows_elevated_feet",
            name = "Inverted Rows (Elevated Feet)",
            targetMuscleGroup = "Back (Upper Back / Lats)",
            secondaryMuscles = listOf("Biceps", "Core"),
            equipment = "Bodyweight",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "back_bw_back_hyperextensions_45",
            name = "Back Hyperextensions (45 Degree Bench)",
            targetMuscleGroup = "Back (Lower Back / Erector Spinae)",
            secondaryMuscles = listOf("Glutes", "Hamstrings"),
            equipment = "Bodyweight",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "back_bw_weighted_hyperextensions",
            name = "Weighted Hyperextensions (Holding Plate)",
            targetMuscleGroup = "Back (Lower Back / Erector Spinae)",
            secondaryMuscles = listOf("Glutes", "Hamstrings"),
            equipment = "Bodyweight",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "back_bw_superman_holds_floor",
            name = "Superman Holds (Floor)",
            targetMuscleGroup = "Back (Lower Back / Erector Spinae)",
            secondaryMuscles = listOf("Glutes"),
            equipment = "Bodyweight",
            category = "Compound"
        )
    )

    val backExercises: List<ExerciseEntity> = backExerciseItems.map { it.toEntity() }

    val shoulderExerciseItems = listOf(
        // 1. BARBELL (10 items)
        DefaultExerciseItem(
            id = "shoulder_bb_ohp_standing",
            name = "Standing Overhead Press (OHP / Military Press)",
            targetMuscleGroup = "Shoulders (Front Deltoids)",
            secondaryMuscles = listOf("Triceps", "Upper Chest", "Core"),
            equipment = "Barbell",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "shoulder_bb_ohp_seated",
            name = "Seated Overhead Barbell Press",
            targetMuscleGroup = "Shoulders (Front Deltoids)",
            secondaryMuscles = listOf("Triceps", "Upper Chest"),
            equipment = "Barbell",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "shoulder_bb_behind_the_neck_press",
            name = "Behind-the-Neck Barbell Press",
            targetMuscleGroup = "Shoulders (Lateral / Rear Deltoids)",
            secondaryMuscles = listOf("Triceps", "Upper Traps"),
            equipment = "Barbell",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "shoulder_bb_upright_row_wide",
            name = "Barbell Upright Row (Wide Grip - Lateral Delt Focus)",
            targetMuscleGroup = "Shoulders (Lateral Deltoids)",
            secondaryMuscles = listOf("Upper Traps", "Biceps", "Forearms"),
            equipment = "Barbell",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "shoulder_bb_upright_row_standard",
            name = "Barbell Upright Row (Standard Grip)",
            targetMuscleGroup = "Shoulders (Lateral Deltoids / Traps)",
            secondaryMuscles = listOf("Upper Traps", "Biceps", "Forearms"),
            equipment = "Barbell",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "shoulder_bb_front_raise_overhand",
            name = "Barbell Front Raise (Overhand Grip)",
            targetMuscleGroup = "Shoulders (Front Deltoids)",
            secondaryMuscles = listOf("Upper Chest", "Traps"),
            equipment = "Barbell",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "shoulder_bb_landmine_press_single_arm",
            name = "Landmine Shoulder Press (Single-Arm)",
            targetMuscleGroup = "Shoulders (Front Deltoids)",
            secondaryMuscles = listOf("Triceps", "Upper Chest", "Core"),
            equipment = "Barbell",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "shoulder_bb_landmine_press_two_handed",
            name = "Landmine Shoulder Press (Two-Handed)",
            targetMuscleGroup = "Shoulders (Front Deltoids)",
            secondaryMuscles = listOf("Upper Chest", "Triceps", "Core"),
            equipment = "Barbell",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "shoulder_bb_push_press",
            name = "Push Press (Barbell Power)",
            targetMuscleGroup = "Shoulders (Front / Full Deltoids)",
            secondaryMuscles = listOf("Triceps", "Core", "Quads", "Glutes"),
            equipment = "Barbell",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "shoulder_bb_rear_delt_row_wide",
            name = "Barbell Rear Delt Row (Wide Grip / 45-degree Torso)",
            targetMuscleGroup = "Shoulders (Rear Deltoids)",
            secondaryMuscles = listOf("Rhomboids", "Middle Traps", "Biceps"),
            equipment = "Barbell",
            category = "Compound"
        ),

        // 2. DUMBBELL (15 items)
        DefaultExerciseItem(
            id = "shoulder_db_press_seated_pronated",
            name = "Seated Dumbbell Shoulder Press (Pronated Grip)",
            targetMuscleGroup = "Shoulders (Front Deltoids)",
            secondaryMuscles = listOf("Triceps", "Upper Chest"),
            equipment = "Dumbbell",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "shoulder_db_press_seated_neutral",
            name = "Seated Dumbbell Shoulder Press (Neutral / Hammer Grip)",
            targetMuscleGroup = "Shoulders (Front Deltoids)",
            secondaryMuscles = listOf("Triceps", "Rotator Cuff"),
            equipment = "Dumbbell",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "shoulder_db_press_standing",
            name = "Standing Dumbbell Overhead Press",
            targetMuscleGroup = "Shoulders (Front Deltoids)",
            secondaryMuscles = listOf("Triceps", "Core", "Traps"),
            equipment = "Dumbbell",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "shoulder_db_arnold_press",
            name = "Arnold Press (Rotating Grip)",
            targetMuscleGroup = "Shoulders (Front / Lateral Deltoids)",
            secondaryMuscles = listOf("Triceps", "Rotator Cuff"),
            equipment = "Dumbbell",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "shoulder_db_lateral_raise_standing",
            name = "Standing Dumbbell Lateral Raise (Side Delts)",
            targetMuscleGroup = "Shoulders (Lateral Deltoids)",
            secondaryMuscles = listOf("Upper Traps"),
            equipment = "Dumbbell",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "shoulder_db_lateral_raise_seated",
            name = "Seated Dumbbell Lateral Raise",
            targetMuscleGroup = "Shoulders (Lateral Deltoids)",
            secondaryMuscles = listOf("Upper Traps"),
            equipment = "Dumbbell",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "shoulder_db_lateral_raise_incline_leaning",
            name = "Incline Leaning Dumbbell Lateral Raise (Single-Arm)",
            targetMuscleGroup = "Shoulders (Lateral Deltoids)",
            secondaryMuscles = listOf("Upper Traps"),
            equipment = "Dumbbell",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "shoulder_db_rear_delt_fly_seated",
            name = "Bent-Over Dumbbell Rear Delt Fly (Seated)",
            targetMuscleGroup = "Shoulders (Rear Deltoids)",
            secondaryMuscles = listOf("Rhomboids", "Middle Traps"),
            equipment = "Dumbbell",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "shoulder_db_rear_delt_fly_standing",
            name = "Bent-Over Dumbbell Rear Delt Fly (Standing)",
            targetMuscleGroup = "Shoulders (Rear Deltoids)",
            secondaryMuscles = listOf("Rhomboids", "Traps", "Lower Back"),
            equipment = "Dumbbell",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "shoulder_db_rear_delt_fly_chest_supported",
            name = "Chest-Supported Incline Rear Delt Fly (Dumbbell)",
            targetMuscleGroup = "Shoulders (Rear Deltoids)",
            secondaryMuscles = listOf("Rhomboids", "Upper Back"),
            equipment = "Dumbbell",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "shoulder_db_front_raise_alternating",
            name = "Dumbbell Front Raise (Alternating Pronated)",
            targetMuscleGroup = "Shoulders (Front Deltoids)",
            secondaryMuscles = listOf("Upper Chest", "Traps"),
            equipment = "Dumbbell",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "shoulder_db_front_raise_neutral",
            name = "Dumbbell Front Raise (Neutral Grip / Hammer)",
            targetMuscleGroup = "Shoulders (Front Deltoids)",
            secondaryMuscles = listOf("Upper Chest"),
            equipment = "Dumbbell",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "shoulder_db_front_raise_incline",
            name = "Incline Dumbbell Front Raise",
            targetMuscleGroup = "Shoulders (Front Deltoids)",
            secondaryMuscles = listOf("Upper Chest"),
            equipment = "Dumbbell",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "shoulder_db_upright_row",
            name = "Dumbbell Upright Row",
            targetMuscleGroup = "Shoulders (Lateral Deltoids / Traps)",
            secondaryMuscles = listOf("Biceps", "Upper Traps"),
            equipment = "Dumbbell",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "shoulder_db_lu_raises",
            name = "Dumbbell Lu Raises (Full Overhead Lateral Raise)",
            targetMuscleGroup = "Shoulders (Lateral Deltoids / Traps)",
            secondaryMuscles = listOf("Traps", "Scapular Stabilizers"),
            equipment = "Dumbbell",
            category = "Isolation"
        ),

        // 3. CABLE (11 items)
        DefaultExerciseItem(
            id = "shoulder_cable_lateral_raise_behind_back",
            name = "Cable Lateral Raise (Behind the Back)",
            targetMuscleGroup = "Shoulders (Lateral Deltoids)",
            secondaryMuscles = listOf("Upper Traps"),
            equipment = "Cable",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "shoulder_cable_lateral_raise_in_front",
            name = "Cable Lateral Raise (In Front of Body)",
            targetMuscleGroup = "Shoulders (Lateral Deltoids)",
            secondaryMuscles = listOf("Upper Traps"),
            equipment = "Cable",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "shoulder_cable_lateral_raise_cuff",
            name = "Cable Lateral Raise (Cuff Attachment at Wrist)",
            targetMuscleGroup = "Shoulders (Lateral Deltoids)",
            secondaryMuscles = listOf("Upper Traps"),
            equipment = "Cable",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "shoulder_cable_lateral_raise_dual_cross_body",
            name = "Dual Cable Lateral Raise (Cross-Body)",
            targetMuscleGroup = "Shoulders (Lateral Deltoids)",
            secondaryMuscles = listOf("Upper Traps"),
            equipment = "Cable",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "shoulder_cable_face_pull_rope_rear_delt",
            name = "Cable Face Pull (Rope - Rear Delts / High Angle)",
            targetMuscleGroup = "Shoulders (Rear Deltoids / Rotator Cuff)",
            secondaryMuscles = listOf("Upper Traps", "Rhomboids"),
            equipment = "Cable",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "shoulder_cable_rear_delt_fly_single_arm",
            name = "Single-Arm Cable Rear Delt Fly (No Handle / At Ear Height)",
            targetMuscleGroup = "Shoulders (Rear Deltoids)",
            secondaryMuscles = listOf("Rhomboids", "Middle Traps"),
            equipment = "Cable",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "shoulder_cable_rear_delt_fly_dual_cross",
            name = "Dual Cable Rear Delt Fly (Reverse Cable Crossover)",
            targetMuscleGroup = "Shoulders (Rear Deltoids)",
            secondaryMuscles = listOf("Rhomboids", "Upper Back"),
            equipment = "Cable",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "shoulder_cable_upright_row",
            name = "Cable Upright Row (Rope / Straight Bar)",
            targetMuscleGroup = "Shoulders (Lateral Deltoids / Traps)",
            secondaryMuscles = listOf("Upper Traps", "Biceps"),
            equipment = "Cable",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "shoulder_cable_front_raise_straight_bar",
            name = "Cable Front Raise (Straight Bar / Low Pulley)",
            targetMuscleGroup = "Shoulders (Front Deltoids)",
            secondaryMuscles = listOf("Upper Chest", "Traps"),
            equipment = "Cable",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "shoulder_cable_front_raise_single_arm",
            name = "Single-Arm Cable Front Raise (Rope / Low Pulley)",
            targetMuscleGroup = "Shoulders (Front Deltoids)",
            secondaryMuscles = listOf("Upper Chest"),
            equipment = "Cable",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "shoulder_cable_press_half_kneeling",
            name = "Half-Kneeling Single-Arm Cable Shoulder Press",
            targetMuscleGroup = "Shoulders (Front Deltoids)",
            secondaryMuscles = listOf("Core", "Triceps"),
            equipment = "Cable",
            category = "Compound"
        ),

        // 4. MACHINE (6 items)
        DefaultExerciseItem(
            id = "shoulder_mach_press_pin_loaded_neutral",
            name = "Machine Shoulder Press (Pin-Loaded - Neutral Grip)",
            targetMuscleGroup = "Shoulders (Front Deltoids)",
            secondaryMuscles = listOf("Triceps", "Upper Chest"),
            equipment = "Machine",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "shoulder_mach_press_pin_loaded_overhand",
            name = "Machine Shoulder Press (Pin-Loaded - Overhand Grip)",
            targetMuscleGroup = "Shoulders (Front Deltoids)",
            secondaryMuscles = listOf("Triceps", "Upper Chest"),
            equipment = "Machine",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "shoulder_mach_plate_loaded_iso_press",
            name = "Plate-Loaded Iso-Lateral Shoulder Press",
            targetMuscleGroup = "Shoulders (Front / Lateral Deltoids)",
            secondaryMuscles = listOf("Triceps"),
            equipment = "Machine",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "shoulder_mach_rear_delt_fly_reverse_pec_deck",
            name = "Rear Delt Machine Fly (Reverse Pec Deck)",
            targetMuscleGroup = "Shoulders (Rear Deltoids)",
            secondaryMuscles = listOf("Rhomboids", "Middle Traps"),
            equipment = "Machine",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "shoulder_mach_lateral_raise_seated",
            name = "Machine Lateral Raise (Seated Pad-Loaded)",
            targetMuscleGroup = "Shoulders (Lateral Deltoids)",
            secondaryMuscles = listOf("Upper Traps"),
            equipment = "Machine",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "shoulder_mach_plate_loaded_lateral_raise",
            name = "Plate-Loaded Lateral Raise Machine",
            targetMuscleGroup = "Shoulders (Lateral Deltoids)",
            secondaryMuscles = listOf("Upper Traps"),
            equipment = "Machine",
            category = "Isolation"
        ),

        // 5. SMITH MACHINE (5 items)
        DefaultExerciseItem(
            id = "shoulder_smith_seated_overhead_press",
            name = "Smith Machine Seated Overhead Press (Front)",
            targetMuscleGroup = "Shoulders (Front Deltoids)",
            secondaryMuscles = listOf("Triceps", "Upper Chest"),
            equipment = "Smith Machine",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "shoulder_smith_behind_the_neck_press",
            name = "Smith Machine Behind-the-Neck Press",
            targetMuscleGroup = "Shoulders (Lateral / Rear Deltoids)",
            secondaryMuscles = listOf("Triceps", "Traps"),
            equipment = "Smith Machine",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "shoulder_smith_standing_overhead_press",
            name = "Smith Machine Standing Overhead Press",
            targetMuscleGroup = "Shoulders (Front Deltoids)",
            secondaryMuscles = listOf("Triceps", "Core"),
            equipment = "Smith Machine",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "shoulder_smith_upright_row_wide",
            name = "Smith Machine Upright Row (Wide Grip)",
            targetMuscleGroup = "Shoulders (Lateral Deltoids)",
            secondaryMuscles = listOf("Upper Traps", "Biceps"),
            equipment = "Smith Machine",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "shoulder_smith_rear_delt_row",
            name = "Smith Machine Rear Delt Row",
            targetMuscleGroup = "Shoulders (Rear Deltoids)",
            secondaryMuscles = listOf("Rhomboids", "Middle Traps", "Biceps"),
            equipment = "Smith Machine",
            category = "Compound"
        ),

        // 6. BODYWEIGHT / CALISTHENICS (4 items)
        DefaultExerciseItem(
            id = "shoulder_bw_pike_push_ups",
            name = "Pike Push-ups (Shoulder Focus)",
            targetMuscleGroup = "Shoulders (Front / Lateral Deltoids)",
            secondaryMuscles = listOf("Triceps", "Upper Chest", "Core"),
            equipment = "Bodyweight",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "shoulder_bw_elevated_feet_pike_push_ups",
            name = "Elevated Feet Pike Push-ups",
            targetMuscleGroup = "Shoulders (Front / Lateral Deltoids)",
            secondaryMuscles = listOf("Triceps", "Core"),
            equipment = "Bodyweight",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "shoulder_bw_handstand_push_ups_wall",
            name = "Handstand Push-ups (Wall-Supported)",
            targetMuscleGroup = "Shoulders (Front / Lateral Deltoids)",
            secondaryMuscles = listOf("Triceps", "Core", "Traps"),
            equipment = "Bodyweight",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "shoulder_bw_handstand_hold_wall",
            name = "Handstand Hold (Wall-Supported Static)",
            targetMuscleGroup = "Shoulders (Full Deltoids / Stability)",
            secondaryMuscles = listOf("Core", "Traps", "Triceps"),
            equipment = "Bodyweight",
            category = "Compound"
        )
    )

    val shoulderExercises: List<ExerciseEntity> = shoulderExerciseItems.map { it.toEntity() }

    val legExerciseItems = listOf(
        // 1. BARBELL (13 items)
        DefaultExerciseItem(
            id = "legs_bb_back_squat_high_bar",
            name = "Barbell Back Squat (High Bar)",
            targetMuscleGroup = "Legs (Quads / Glutes)",
            secondaryMuscles = listOf("Hamstrings", "Lower Back", "Core"),
            equipment = "Barbell",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "legs_bb_back_squat_low_bar",
            name = "Barbell Back Squat (Low Bar)",
            targetMuscleGroup = "Legs (Posterior Chain / Glutes)",
            secondaryMuscles = listOf("Quads", "Hamstrings", "Lower Back"),
            equipment = "Barbell",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "legs_bb_front_squat",
            name = "Barbell Front Squat (Clean Grip / Cross-Arm)",
            targetMuscleGroup = "Legs (Quads)",
            secondaryMuscles = listOf("Glutes", "Core", "Upper Back"),
            equipment = "Barbell",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "legs_bb_romanian_deadlift_rdl",
            name = "Barbell Romanian Deadlift (RDL - Hamstrings/Glutes)",
            targetMuscleGroup = "Legs (Hamstrings / Glutes)",
            secondaryMuscles = listOf("Lower Back", "Forearms"),
            equipment = "Barbell",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "legs_bb_stiff_legged_deadlift_sldl",
            name = "Barbell Stiff-Legged Deadlift (SLDL)",
            targetMuscleGroup = "Legs (Hamstrings)",
            secondaryMuscles = listOf("Glutes", "Lower Back"),
            equipment = "Barbell",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "legs_bb_sumo_deadlift",
            name = "Sumo Deadlift (Barbell)",
            targetMuscleGroup = "Legs (Glutes / Adductors)",
            secondaryMuscles = listOf("Quads", "Hamstrings", "Lower Back"),
            equipment = "Barbell",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "legs_bb_hip_thrust",
            name = "Barbell Hip Thrust (Glutes Focus)",
            targetMuscleGroup = "Legs (Glutes)",
            secondaryMuscles = listOf("Hamstrings", "Core"),
            equipment = "Barbell",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "legs_bb_good_mornings",
            name = "Barbell Good Mornings (Hamstrings & Posterior Chain)",
            targetMuscleGroup = "Legs (Hamstrings / Glutes)",
            secondaryMuscles = listOf("Lower Back", "Core"),
            equipment = "Barbell",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "legs_bb_walking_lunges",
            name = "Barbell Walking Lunges",
            targetMuscleGroup = "Legs (Quads / Glutes)",
            secondaryMuscles = listOf("Hamstrings", "Calves", "Core"),
            equipment = "Barbell",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "legs_bb_reverse_lunges",
            name = "Barbell Reverse Lunges",
            targetMuscleGroup = "Legs (Quads / Glutes)",
            secondaryMuscles = listOf("Hamstrings", "Core"),
            equipment = "Barbell",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "legs_bb_zercher_squat",
            name = "Zercher Squat (Barbell in Crook of Elbows)",
            targetMuscleGroup = "Legs (Quads / Glutes)",
            secondaryMuscles = listOf("Core", "Biceps", "Upper Back"),
            equipment = "Barbell",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "legs_bb_standing_calf_raise",
            name = "Barbell Standing Calf Raise",
            targetMuscleGroup = "Legs (Calves - Gastrocnemius)",
            secondaryMuscles = listOf("Soleus", "Ankles"),
            equipment = "Barbell",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "legs_bb_box_squats",
            name = "Barbell Box Squats",
            targetMuscleGroup = "Legs (Glutes / Quads)",
            secondaryMuscles = listOf("Hamstrings", "Core"),
            equipment = "Barbell",
            category = "Compound"
        ),

        // 2. DUMBBELL (12 items)
        DefaultExerciseItem(
            id = "legs_db_romanian_deadlift_rdl",
            name = "Dumbbell Romanian Deadlift (RDL)",
            targetMuscleGroup = "Legs (Hamstrings)",
            secondaryMuscles = listOf("Glutes", "Lower Back"),
            equipment = "Dumbbell",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "legs_db_bulgarian_split_squat",
            name = "Dumbbell Bulgarian Split Squat (Rear Foot Elevated)",
            targetMuscleGroup = "Legs (Quads / Glutes)",
            secondaryMuscles = listOf("Hamstrings", "Adductors"),
            equipment = "Dumbbell",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "legs_db_goblet_squat_flat",
            name = "Dumbbell Goblet Squat (Heels Flat)",
            targetMuscleGroup = "Legs (Quads)",
            secondaryMuscles = listOf("Glutes", "Core"),
            equipment = "Dumbbell",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "legs_db_goblet_squat_elevated",
            name = "Dumbbell Goblet Squat (Heels Elevated - Quad Focus)",
            targetMuscleGroup = "Legs (Quads)",
            secondaryMuscles = listOf("Glutes", "Core"),
            equipment = "Dumbbell",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "legs_db_walking_lunges",
            name = "Dumbbell Walking Lunges",
            targetMuscleGroup = "Legs (Quads / Glutes)",
            secondaryMuscles = listOf("Hamstrings", "Core"),
            equipment = "Dumbbell",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "legs_db_reverse_lunges",
            name = "Dumbbell Reverse Lunges",
            targetMuscleGroup = "Legs (Glutes / Quads)",
            secondaryMuscles = listOf("Hamstrings"),
            equipment = "Dumbbell",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "legs_db_step_ups",
            name = "Dumbbell Step-ups (Bench / Box)",
            targetMuscleGroup = "Legs (Glutes / Quads)",
            secondaryMuscles = listOf("Hamstrings"),
            equipment = "Dumbbell",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "legs_db_sumo_squat",
            name = "Dumbbell Sumo Squat (Plie Squat)",
            targetMuscleGroup = "Legs (Adductors / Glutes)",
            secondaryMuscles = listOf("Quads", "Hamstrings"),
            equipment = "Dumbbell",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "legs_db_single_leg_rdl",
            name = "Dumbbell Single-Leg Romanian Deadlift",
            targetMuscleGroup = "Legs (Hamstrings / Glutes)",
            secondaryMuscles = listOf("Core", "Balance / Ankles"),
            equipment = "Dumbbell",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "legs_db_standing_calf_raise_single_leg",
            name = "Dumbbell Standing Calf Raise (Single-Leg)",
            targetMuscleGroup = "Legs (Calves)",
            secondaryMuscles = listOf("Ankle Stabilizers"),
            equipment = "Dumbbell",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "legs_db_lying_hamstring_curl",
            name = "Dumbbell Lying Hamstring Curl (Dumbbell Between Feet)",
            targetMuscleGroup = "Legs (Hamstrings)",
            secondaryMuscles = listOf("Calves"),
            equipment = "Dumbbell",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "legs_db_seated_calf_raise",
            name = "Dumbbell Seated Calf Raise (Dumbbells on Knees)",
            targetMuscleGroup = "Legs (Calves - Soleus)",
            secondaryMuscles = listOf("Ankles"),
            equipment = "Dumbbell",
            category = "Isolation"
        ),

        // 3. CABLE (7 items)
        DefaultExerciseItem(
            id = "legs_cable_pull_through",
            name = "Cable Pull-Through (Glutes & Hamstrings)",
            targetMuscleGroup = "Legs (Glutes / Hamstrings)",
            secondaryMuscles = listOf("Lower Back"),
            equipment = "Cable",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "legs_cable_glute_kickback",
            name = "Cable Glute Kickbacks (Ankle Strap)",
            targetMuscleGroup = "Legs (Glutes)",
            secondaryMuscles = listOf("Hamstrings"),
            equipment = "Cable",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "legs_cable_standing_hip_abduction",
            name = "Cable Standing Hip Abduction (Ankle Strap)",
            targetMuscleGroup = "Legs (Glute Medius / Outer Thigh)",
            secondaryMuscles = listOf("Core"),
            equipment = "Cable",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "legs_cable_standing_hip_adduction",
            name = "Cable Standing Hip Adduction (Ankle Strap)",
            targetMuscleGroup = "Legs (Adductors / Inner Thigh)",
            secondaryMuscles = listOf("Core"),
            equipment = "Cable",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "legs_cable_goblet_squat",
            name = "Cable Goblet Squat",
            targetMuscleGroup = "Legs (Quads)",
            secondaryMuscles = listOf("Glutes", "Core"),
            equipment = "Cable",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "legs_cable_romanian_deadlift",
            name = "Cable Romanian Deadlift (Straight Bar / Rope)",
            targetMuscleGroup = "Legs (Hamstrings / Glutes)",
            secondaryMuscles = listOf("Lower Back"),
            equipment = "Cable",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "legs_cable_single_leg_hamstring_curl",
            name = "Single-Leg Cable Hamstring Curl (Standing)",
            targetMuscleGroup = "Legs (Hamstrings)",
            secondaryMuscles = listOf("Calves"),
            equipment = "Cable",
            category = "Isolation"
        ),

        // 4. MACHINE (19 items)
        DefaultExerciseItem(
            id = "legs_mach_leg_press_45_standard",
            name = "Leg Press (45 Degree Sled - Standard Foot Placement)",
            targetMuscleGroup = "Legs (Quads)",
            secondaryMuscles = listOf("Glutes", "Hamstrings"),
            equipment = "Machine",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "legs_mach_leg_press_45_high_wide",
            name = "Leg Press (High & Wide Stance - Glute/Hamstring Focus)",
            targetMuscleGroup = "Legs (Glutes / Hamstrings)",
            secondaryMuscles = listOf("Quads"),
            equipment = "Machine",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "legs_mach_leg_press_45_low_narrow",
            name = "Leg Press (Low & Narrow Stance - Quad Sweep Focus)",
            targetMuscleGroup = "Legs (Quads - Outer Sweep)",
            secondaryMuscles = listOf("Glutes"),
            equipment = "Machine",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "legs_mach_single_leg_press_45",
            name = "Single-Leg 45 Degree Leg Press",
            targetMuscleGroup = "Legs (Quads / Glutes)",
            secondaryMuscles = listOf("Hamstrings"),
            equipment = "Machine",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "legs_mach_hack_squat_standard",
            name = "Hack Squat Machine (Standard Stance)",
            targetMuscleGroup = "Legs (Quads)",
            secondaryMuscles = listOf("Glutes"),
            equipment = "Machine",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "legs_mach_hack_squat_narrow",
            name = "Hack Squat Machine (Narrow Stance / Quad Focus)",
            targetMuscleGroup = "Legs (Quads - Outer Sweep)",
            secondaryMuscles = listOf("Glutes"),
            equipment = "Machine",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "legs_mach_reverse_hack_squat",
            name = "Reverse Hack Squat (Glutes Focus)",
            targetMuscleGroup = "Legs (Glutes / Hamstrings)",
            secondaryMuscles = listOf("Quads", "Lower Back"),
            equipment = "Machine",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "legs_mach_leg_extension",
            name = "Leg Extension Machine (Quads)",
            targetMuscleGroup = "Legs (Quads - Rectus Femoris)",
            secondaryMuscles = emptyList(),
            equipment = "Machine",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "legs_mach_single_leg_extension",
            name = "Single-Leg Extension Machine",
            targetMuscleGroup = "Legs (Quads)",
            secondaryMuscles = emptyList(),
            equipment = "Machine",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "legs_mach_lying_leg_curl",
            name = "Lying Leg Curl Machine (Hamstrings)",
            targetMuscleGroup = "Legs (Hamstrings)",
            secondaryMuscles = listOf("Calves"),
            equipment = "Machine",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "legs_mach_seated_leg_curl",
            name = "Seated Leg Curl Machine (Hamstrings)",
            targetMuscleGroup = "Legs (Hamstrings)",
            secondaryMuscles = listOf("Calves"),
            equipment = "Machine",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "legs_mach_standing_single_leg_curl",
            name = "Standing Single-Leg Curl Machine",
            targetMuscleGroup = "Legs (Hamstrings)",
            secondaryMuscles = listOf("Calves"),
            equipment = "Machine",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "legs_mach_hip_abduction",
            name = "Hip Abduction Machine (Outer Thighs / Glute Medius)",
            targetMuscleGroup = "Legs (Gluteus Medius / Minimus)",
            secondaryMuscles = listOf("Tensor Fasciae Latae"),
            equipment = "Machine",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "legs_mach_hip_adduction",
            name = "Hip Adduction Machine (Inner Thighs / Groin)",
            targetMuscleGroup = "Legs (Adductors / Inner Thigh)",
            secondaryMuscles = listOf("Gracilis"),
            equipment = "Machine",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "legs_mach_standing_calf_raise",
            name = "Standing Calf Raise Machine",
            targetMuscleGroup = "Legs (Calves - Gastrocnemius)",
            secondaryMuscles = listOf("Soleus"),
            equipment = "Machine",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "legs_mach_seated_calf_raise",
            name = "Seated Calf Raise Machine (Soleus Focus)",
            targetMuscleGroup = "Legs (Calves - Soleus)",
            secondaryMuscles = listOf("Gastrocnemius"),
            equipment = "Machine",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "legs_mach_donkey_calf_raise",
            name = "Donkey Calf Raise Machine",
            targetMuscleGroup = "Legs (Calves)",
            secondaryMuscles = listOf("Soleus"),
            equipment = "Machine",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "legs_mach_glute_ham_developer_ghd",
            name = "Glute Ham Developer (GHD) Raise",
            targetMuscleGroup = "Legs (Hamstrings / Glutes)",
            secondaryMuscles = listOf("Lower Back", "Calves"),
            equipment = "Machine",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "legs_mach_glute_kickback_press",
            name = "Machine Glute Kickback / Plate-Loaded Glute Press",
            targetMuscleGroup = "Legs (Glutes)",
            secondaryMuscles = listOf("Hamstrings"),
            equipment = "Machine",
            category = "Compound"
        ),

        // 5. SMITH MACHINE (8 items)
        DefaultExerciseItem(
            id = "legs_smith_back_squat",
            name = "Smith Machine Back Squat",
            targetMuscleGroup = "Legs (Quads / Glutes)",
            secondaryMuscles = listOf("Hamstrings"),
            equipment = "Smith Machine",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "legs_smith_front_squat",
            name = "Smith Machine Front Squat",
            targetMuscleGroup = "Legs (Quads)",
            secondaryMuscles = listOf("Glutes", "Upper Back"),
            equipment = "Smith Machine",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "legs_smith_bulgarian_split_squat",
            name = "Smith Machine Bulgarian Split Squat",
            targetMuscleGroup = "Legs (Quads / Glutes)",
            secondaryMuscles = listOf("Hamstrings"),
            equipment = "Smith Machine",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "legs_smith_romanian_deadlift_rdl",
            name = "Smith Machine Romanian Deadlift (RDL)",
            targetMuscleGroup = "Legs (Hamstrings)",
            secondaryMuscles = listOf("Glutes", "Lower Back"),
            equipment = "Smith Machine",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "legs_smith_hip_thrust",
            name = "Smith Machine Hip Thrust",
            targetMuscleGroup = "Legs (Glutes)",
            secondaryMuscles = listOf("Hamstrings", "Core"),
            equipment = "Smith Machine",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "legs_smith_good_mornings",
            name = "Smith Machine Good Mornings",
            targetMuscleGroup = "Legs (Hamstrings / Lower Back)",
            secondaryMuscles = listOf("Glutes"),
            equipment = "Smith Machine",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "legs_smith_standing_calf_raise",
            name = "Smith Machine Standing Calf Raise (Elevated on Block)",
            targetMuscleGroup = "Legs (Calves)",
            secondaryMuscles = listOf("Soleus"),
            equipment = "Smith Machine",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "legs_smith_reverse_lunges",
            name = "Smith Machine Reverse Lunges",
            targetMuscleGroup = "Legs (Glutes / Quads)",
            secondaryMuscles = listOf("Hamstrings"),
            equipment = "Smith Machine",
            category = "Compound"
        ),

        // 6. BODYWEIGHT / CALISTHENICS (10 items)
        DefaultExerciseItem(
            id = "legs_bw_air_squats",
            name = "Bodyweight Air Squats",
            targetMuscleGroup = "Legs (Quads / Glutes)",
            secondaryMuscles = listOf("Hamstrings", "Calves"),
            equipment = "Bodyweight",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "legs_bw_bulgarian_split_squats",
            name = "Bodyweight Bulgarian Split Squats",
            targetMuscleGroup = "Legs (Quads / Glutes)",
            secondaryMuscles = listOf("Hamstrings"),
            equipment = "Bodyweight",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "legs_bw_walking_lunges",
            name = "Bodyweight Walking Lunges",
            targetMuscleGroup = "Legs (Quads / Glutes)",
            secondaryMuscles = listOf("Hamstrings", "Calves"),
            equipment = "Bodyweight",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "legs_bw_sissy_squats",
            name = "Sissy Squats (Bodyweight Quad Focus)",
            targetMuscleGroup = "Legs (Quads - Tear Drop)",
            secondaryMuscles = listOf("Core"),
            equipment = "Bodyweight",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "legs_bw_nordic_hamstring_curls",
            name = "Nordic Hamstring Curls (Bodyweight / Partner-Assisted)",
            targetMuscleGroup = "Legs (Hamstrings - Eccentric)",
            secondaryMuscles = listOf("Glutes", "Core"),
            equipment = "Bodyweight",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "legs_bw_single_leg_glute_bridges",
            name = "Single-Leg Glute Bridges (Floor)",
            targetMuscleGroup = "Legs (Glutes)",
            secondaryMuscles = listOf("Hamstrings", "Core"),
            equipment = "Bodyweight",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "legs_bw_double_leg_glute_bridges",
            name = "Double-Leg Glute Bridges (Floor)",
            targetMuscleGroup = "Legs (Glutes)",
            secondaryMuscles = listOf("Hamstrings"),
            equipment = "Bodyweight",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "legs_bw_standing_single_leg_calf_raise",
            name = "Standing Single-Leg Calf Raises (Deficit on Step)",
            targetMuscleGroup = "Legs (Calves)",
            secondaryMuscles = listOf("Ankles"),
            equipment = "Bodyweight",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "legs_bw_pistol_squats",
            name = "Pistol Squats (Single-Leg Squats)",
            targetMuscleGroup = "Legs (Quads / Glutes / Balance)",
            secondaryMuscles = listOf("Hamstrings", "Calves", "Core"),
            equipment = "Bodyweight",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "legs_bw_jump_squats",
            name = "Jump Squats (Plyometric)",
            targetMuscleGroup = "Legs (Quads / Glutes / Power)",
            secondaryMuscles = listOf("Calves", "Core"),
            equipment = "Bodyweight",
            category = "Compound"
        )
    )

    val legExercises: List<ExerciseEntity> = legExerciseItems.map { it.toEntity() }

    val bicepsAndTricepsExerciseItems = listOf(
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
            id = "arms_machine_bicep_curl",
            name = "Machine Bicep Curl",
            targetMuscleGroup = "Arms (Biceps)",
            secondaryMuscles = listOf("Forearms"),
            equipment = "Machine",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "arms_smith_machine_drag_curl",
            name = "Smith Machine Drag Curl",
            targetMuscleGroup = "Arms (Biceps)",
            secondaryMuscles = listOf("Forearms"),
            equipment = "Smith Machine",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "arms_smith_machine_bicep_curl",
            name = "Smith Machine Bicep Curl",
            targetMuscleGroup = "Arms (Biceps)",
            secondaryMuscles = listOf("Forearms"),
            equipment = "Smith Machine",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "arms_chin_up_biceps_focus",
            name = "Chin-Up (Biceps Focus)",
            targetMuscleGroup = "Arms (Biceps)",
            secondaryMuscles = listOf("Lats", "Forearms"),
            equipment = "Bodyweight",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "arms_inverted_row_underhand_bicep_grip",
            name = "Inverted Row (Underhand Bicep Grip)",
            targetMuscleGroup = "Arms (Biceps)",
            secondaryMuscles = listOf("Upper Back", "Forearms"),
            equipment = "Bodyweight",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "arms_suspension_trainer_bicep_curl",
            name = "Suspension Trainer Bicep Curl",
            targetMuscleGroup = "Arms (Biceps)",
            secondaryMuscles = listOf("Core", "Forearms"),
            equipment = "Bodyweight",
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
            id = "arms_smith_machine_jm_press",
            name = "Smith Machine JM Press",
            targetMuscleGroup = "Arms (Triceps)",
            secondaryMuscles = listOf("Chest", "Front Deltoids"),
            equipment = "Smith Machine",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "arms_smith_machine_close_grip_bench_press",
            name = "Smith Machine Close-Grip Bench Press",
            targetMuscleGroup = "Arms (Triceps)",
            secondaryMuscles = listOf("Chest", "Front Deltoids"),
            equipment = "Smith Machine",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "arms_diamond_push_up_triceps_focus",
            name = "Diamond Push-Up (Triceps Focus)",
            targetMuscleGroup = "Arms (Triceps)",
            secondaryMuscles = listOf("Chest", "Front Deltoids", "Core"),
            equipment = "Bodyweight",
            category = "Compound"
        ),
    )

    val forearmExerciseItems = listOf(
        // 1. BARBELL (4 items)
        DefaultExerciseItem(
            id = "forearms_bb_wrist_curl_palms_up",
            name = "Barbell Wrist Curl (Palms Up / Forearm Flexors - Seated on Bench)",
            targetMuscleGroup = "Forearms (Flexors)",
            secondaryMuscles = listOf("Grip Strength"),
            equipment = "Barbell",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "forearms_bb_reverse_wrist_curl_palms_down",
            name = "Barbell Reverse Wrist Curl (Palms Down / Forearm Extensors)",
            targetMuscleGroup = "Forearms (Extensors)",
            secondaryMuscles = listOf("Grip Strength"),
            equipment = "Barbell",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "forearms_bb_behind_the_back_wrist_curl",
            name = "Behind-the-Back Barbell Wrist Curl (Standing)",
            targetMuscleGroup = "Forearms (Flexors)",
            secondaryMuscles = listOf("Grip Strength"),
            equipment = "Barbell",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "forearms_bb_standing_reverse_curl",
            name = "Standing Barbell Reverse Curl (Thumbless / Overhand Grip)",
            targetMuscleGroup = "Forearms (Brachioradialis & Extensors)",
            secondaryMuscles = listOf("Biceps", "Brachialis"),
            equipment = "Barbell",
            category = "Isolation"
        ),

        // 2. DUMBBELL (5 items)
        DefaultExerciseItem(
            id = "forearms_db_seated_wrist_curl_palms_up",
            name = "Dumbbell Seated Wrist Curl (Palms Up - Single Arm)",
            targetMuscleGroup = "Forearms (Flexors)",
            secondaryMuscles = listOf("Grip Strength"),
            equipment = "Dumbbell",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "forearms_db_seated_reverse_wrist_curl_palms_down",
            name = "Dumbbell Seated Reverse Wrist Curl (Palms Down)",
            targetMuscleGroup = "Forearms (Extensors)",
            secondaryMuscles = listOf("Grip Strength"),
            equipment = "Dumbbell",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "forearms_db_hammer_curl_wrist_focus",
            name = "Dumbbell Hammer Curl (Wrist / Brachioradialis Focus)",
            targetMuscleGroup = "Forearms (Brachioradialis)",
            secondaryMuscles = listOf("Biceps", "Brachialis"),
            equipment = "Dumbbell",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "forearms_db_radial_ulnar_deviation",
            name = "Dumbbell Radial / Ulnar Deviation (Hammer Levering)",
            targetMuscleGroup = "Forearms (Wrist Stability / Deviators)",
            secondaryMuscles = listOf("Grip Strength"),
            equipment = "Dumbbell",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "forearms_db_pinch_hold_timed",
            name = "Dumbbell Pinch Hold (Timed Grip)",
            targetMuscleGroup = "Forearms (Pinch Grip)",
            secondaryMuscles = listOf("Finger Flexors"),
            equipment = "Dumbbell",
            category = "Isolation"
        ),

        // 3. CABLE (3 items)
        DefaultExerciseItem(
            id = "forearms_cable_wrist_curl_straight_bar",
            name = "Standing Cable Wrist Curl (Straight Bar / Low Pulley)",
            targetMuscleGroup = "Forearms (Flexors)",
            secondaryMuscles = listOf("Grip Strength"),
            equipment = "Cable",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "forearms_cable_reverse_wrist_curl_straight_bar",
            name = "Standing Cable Reverse Wrist Curl (Straight Bar)",
            targetMuscleGroup = "Forearms (Extensors)",
            secondaryMuscles = listOf("Grip Strength"),
            equipment = "Cable",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "forearms_cable_rope_wrist_extension",
            name = "Cable Rope Wrist Extension",
            targetMuscleGroup = "Forearms (Extensors)",
            secondaryMuscles = listOf("Grip Strength"),
            equipment = "Cable",
            category = "Isolation"
        ),

        // 4. BODYWEIGHT / SPECIALTY (4 items)
        DefaultExerciseItem(
            id = "forearms_bw_dead_hang_timed_grip",
            name = "Dead Hang from Pull-up Bar (Timed Grip Strength)",
            targetMuscleGroup = "Forearms (Grip Strength)",
            secondaryMuscles = listOf("Lats", "Shoulders"),
            equipment = "Bodyweight",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "forearms_bw_towel_grip_dead_hang",
            name = "Towel Grip Dead Hang",
            targetMuscleGroup = "Forearms (Grip Strength / Finger Flexors)",
            secondaryMuscles = listOf("Lats", "Shoulders"),
            equipment = "Bodyweight",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "forearms_other_plate_pinch_carry_hold",
            name = "Plate Pinch Carry / Hold (Two Smooth Plates Together)",
            targetMuscleGroup = "Forearms (Pinch Grip Strength)",
            secondaryMuscles = listOf("Core", "Traps"),
            equipment = "Other",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "forearms_other_wrist_roller_flexion_extension",
            name = "Wrist Roller (Extension & Flexion)",
            targetMuscleGroup = "Forearms (Flexors & Extensors)",
            secondaryMuscles = listOf("Shoulders"),
            equipment = "Other",
            category = "Isolation"
        )
    )

    val armExerciseItems = bicepsAndTricepsExerciseItems + forearmExerciseItems
    val armExercises: List<ExerciseEntity> = armExerciseItems.map { it.toEntity() }
    val forearmExercises: List<ExerciseEntity> = forearmExerciseItems.map { it.toEntity() }

    val trapsExerciseItems = listOf(
        // 1. BARBELL (5 items)
        DefaultExerciseItem(
            id = "traps_bb_shrugs_standard_overhand",
            name = "Barbell Shrugs (Standard Overhand Grip)",
            targetMuscleGroup = "Traps (Upper Trapezius)",
            secondaryMuscles = listOf("Forearms / Grip", "Neck Extensors"),
            equipment = "Barbell",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "traps_bb_shrugs_behind_the_back",
            name = "Barbell Shrugs (Behind the Back)",
            targetMuscleGroup = "Traps (Upper & Mid Trapezius)",
            secondaryMuscles = listOf("Forearms / Grip", "Rear Deltoids"),
            equipment = "Barbell",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "traps_bb_snatch_grip_shrugs_wide",
            name = "Snatch-Grip Barbell Shrugs (Wide Grip - Upper Traps Focus)",
            targetMuscleGroup = "Traps (Upper Trapezius)",
            secondaryMuscles = listOf("Forearms", "Upper Back"),
            equipment = "Barbell",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "traps_bb_power_shrugs_dip_pop",
            name = "Barbell Power Shrugs (Slight Knee Dip & Pop)",
            targetMuscleGroup = "Traps (Upper Trapezius)",
            secondaryMuscles = listOf("Glutes", "Hamstrings", "Calves", "Grip"),
            equipment = "Barbell",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "traps_bb_landmine_shrugs_single_arm",
            name = "Landmine Shrugs (Single Arm)",
            targetMuscleGroup = "Traps (Upper Trapezius)",
            secondaryMuscles = listOf("Core", "Forearms / Grip"),
            equipment = "Barbell",
            category = "Isolation"
        ),

        // 2. DUMBBELL (4 items)
        DefaultExerciseItem(
            id = "traps_db_shrugs_standing_neutral",
            name = "Dumbbell Shrugs (Standing Neutral Grip)",
            targetMuscleGroup = "Traps (Upper Trapezius)",
            secondaryMuscles = listOf("Forearms / Grip"),
            equipment = "Dumbbell",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "traps_db_incline_prone_shrugs_chest_supported",
            name = "Incline Prone Dumbbell Shrugs (Chest Supported - Mid Traps Focus)",
            targetMuscleGroup = "Traps (Mid & Lower Trapezius)",
            secondaryMuscles = listOf("Rhomboids", "Rear Deltoids"),
            equipment = "Dumbbell",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "traps_db_overhead_shrug_scapular_elevation",
            name = "Dumbbell Overhead Shrug (Scapular Upward Elevation)",
            targetMuscleGroup = "Traps (Upper Trapezius / Serratus)",
            secondaryMuscles = listOf("Shoulders", "Core"),
            equipment = "Dumbbell",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "traps_db_farmers_walk_carry_heavy",
            name = "Farmer's Walk / Carry (Heavy Dumbbells)",
            targetMuscleGroup = "Traps (Upper Trapezius & Postural Carries)",
            secondaryMuscles = listOf("Forearms / Grip", "Core", "Calves"),
            equipment = "Dumbbell",
            category = "Compound"
        ),

        // 3. CABLE (2 items)
        DefaultExerciseItem(
            id = "traps_cable_shrugs_dual_pulley_rope",
            name = "Cable Shrugs (Dual Low Pulleys / Rope)",
            targetMuscleGroup = "Traps (Upper Trapezius)",
            secondaryMuscles = listOf("Forearms / Grip"),
            equipment = "Cable",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "traps_cable_face_pull_high_external_rotation",
            name = "Cable Face Pull with High External Rotation (Traps & Rotator Cuff)",
            targetMuscleGroup = "Traps (Mid & Upper Trapezius)",
            secondaryMuscles = listOf("Rear Deltoids", "Rotator Cuff"),
            equipment = "Cable",
            category = "Compound"
        ),

        // 4. MACHINE (3 items)
        DefaultExerciseItem(
            id = "traps_machine_plate_loaded_shrug_neutral",
            name = "Plate-Loaded Shrug Machine (Neutral Handles)",
            targetMuscleGroup = "Traps (Upper Trapezius)",
            secondaryMuscles = listOf("Forearms / Grip"),
            equipment = "Machine",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "traps_machine_seated_shrug_pin_loaded",
            name = "Seated Shrug Machine (Pin-Loaded)",
            targetMuscleGroup = "Traps (Upper Trapezius)",
            secondaryMuscles = listOf("Forearms / Grip"),
            equipment = "Machine",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "traps_machine_calf_raise_shoulder_shrugs",
            name = "Calf Raise Machine Shoulder Shrugs (Padded Shoulders)",
            targetMuscleGroup = "Traps (Upper Trapezius - Grip-Free)",
            secondaryMuscles = listOf("Core"),
            equipment = "Machine",
            category = "Isolation"
        ),

        // 5. SMITH MACHINE (2 items)
        DefaultExerciseItem(
            id = "traps_smith_machine_front_shrugs",
            name = "Smith Machine Front Shrugs",
            targetMuscleGroup = "Traps (Upper Trapezius)",
            secondaryMuscles = listOf("Forearms / Grip"),
            equipment = "Smith Machine",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "traps_smith_machine_behind_the_back_shrugs",
            name = "Smith Machine Behind-the-Back Shrugs",
            targetMuscleGroup = "Traps (Upper & Mid Trapezius)",
            secondaryMuscles = listOf("Forearms / Grip"),
            equipment = "Smith Machine",
            category = "Isolation"
        ),

        // 6. TRAP BAR / HEX BAR (2 items)
        DefaultExerciseItem(
            id = "traps_barbell_trap_bar_shrugs_neutral",
            name = "Trap Bar Shrugs (Neutral Grip)",
            targetMuscleGroup = "Traps (Upper Trapezius)",
            secondaryMuscles = listOf("Forearms / Grip", "Core"),
            equipment = "Barbell",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "traps_barbell_trap_bar_farmers_walk",
            name = "Trap Bar Farmer's Walk",
            targetMuscleGroup = "Traps (Upper Trapezius & Postural Carries)",
            secondaryMuscles = listOf("Forearms / Grip", "Core", "Legs"),
            equipment = "Barbell",
            category = "Compound"
        )
    )

    val trapsExercises: List<ExerciseEntity> = trapsExerciseItems.map { it.toEntity() }

    val absExerciseItems = listOf(
        // 1. BODYWEIGHT / CALISTHENICS (19 items)
        DefaultExerciseItem(
            id = "abs_bw_hanging_leg_raise_straight",
            name = "Hanging Leg Raises (Straight Legs - Bar)",
            targetMuscleGroup = "Abs / Core (Lower Rectus Abdominis)",
            secondaryMuscles = listOf("Hip Flexors", "Forearms / Grip"),
            equipment = "Bodyweight",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "abs_bw_hanging_knee_raise",
            name = "Hanging Knee Raises (Bar)",
            targetMuscleGroup = "Abs / Core (Lower Rectus Abdominis)",
            secondaryMuscles = listOf("Hip Flexors", "Grip"),
            equipment = "Bodyweight",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "abs_bw_captains_chair_leg_raise",
            name = "Captain's Chair Leg Raises (Dip Station)",
            targetMuscleGroup = "Abs / Core (Lower Rectus Abdominis)",
            secondaryMuscles = listOf("Hip Flexors"),
            equipment = "Bodyweight",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "abs_bw_captains_chair_knee_raise",
            name = "Captain's Chair Knee Raises",
            targetMuscleGroup = "Abs / Core (Lower Rectus Abdominis)",
            secondaryMuscles = listOf("Hip Flexors"),
            equipment = "Bodyweight",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "abs_bw_hanging_windshield_wipers",
            name = "Hanging Windshield Wipers (Obliques)",
            targetMuscleGroup = "Abs / Core (Obliques / Rotational)",
            secondaryMuscles = listOf("Lats", "Hip Flexors", "Grip"),
            equipment = "Bodyweight",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "abs_bw_lying_leg_raise",
            name = "Lying Leg Raises (Floor)",
            targetMuscleGroup = "Abs / Core (Lower Rectus Abdominis)",
            secondaryMuscles = listOf("Hip Flexors"),
            equipment = "Bodyweight",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "abs_bw_reverse_crunch",
            name = "Reverse Crunches (Floor / Flat Bench)",
            targetMuscleGroup = "Abs / Core (Lower Rectus Abdominis)",
            secondaryMuscles = emptyList(),
            equipment = "Bodyweight",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "abs_bw_incline_bench_reverse_crunch",
            name = "Incline Bench Reverse Crunches",
            targetMuscleGroup = "Abs / Core (Lower Rectus Abdominis)",
            secondaryMuscles = listOf("Hip Flexors"),
            equipment = "Bodyweight",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "abs_bw_standard_floor_crunch",
            name = "Standard Floor Crunches",
            targetMuscleGroup = "Abs / Core (Upper Rectus Abdominis)",
            secondaryMuscles = emptyList(),
            equipment = "Bodyweight",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "abs_bw_bicycle_crunch",
            name = "Bicycle Crunches (Obliques)",
            targetMuscleGroup = "Abs / Core (Obliques / Rectus Abdominis)",
            secondaryMuscles = listOf("Hip Flexors"),
            equipment = "Bodyweight",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "abs_bw_dead_bug",
            name = "Dead Bug (Core Stabilization)",
            targetMuscleGroup = "Abs / Core (Deep Core / Transverse Abdominis)",
            secondaryMuscles = listOf("Lower Back Stabilizers"),
            equipment = "Bodyweight",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "abs_bw_bird_dog",
            name = "Bird Dog (Posterior & Core Anti-Rotation)",
            targetMuscleGroup = "Abs / Core (Anti-Rotation / Deep Core)",
            secondaryMuscles = listOf("Glutes", "Lower Back", "Shoulders"),
            equipment = "Bodyweight",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "abs_bw_standard_plank",
            name = "Standard Plank (Elbows on Floor)",
            targetMuscleGroup = "Abs / Core (Transverse Abdominis / Anti-Extension)",
            secondaryMuscles = listOf("Shoulders", "Glutes"),
            equipment = "Bodyweight",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "abs_bw_side_plank",
            name = "Side Plank (Floor)",
            targetMuscleGroup = "Abs / Core (Obliques / Lateral Stability)",
            secondaryMuscles = listOf("Shoulders", "Glute Medius"),
            equipment = "Bodyweight",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "abs_bw_hollow_body_hold",
            name = "Hollow Body Hold",
            targetMuscleGroup = "Abs / Core (Deep Core / Anti-Extension)",
            secondaryMuscles = listOf("Quads", "Hip Flexors"),
            equipment = "Bodyweight",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "abs_bw_v_ups",
            name = "V-Ups (Full Body Fold)",
            targetMuscleGroup = "Abs / Core (Rectus Abdominis / Core Power)",
            secondaryMuscles = listOf("Hip Flexors"),
            equipment = "Bodyweight",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "abs_bw_russian_twists_bw",
            name = "Russian Twists (Bodyweight)",
            targetMuscleGroup = "Abs / Core (Obliques / Rotational)",
            secondaryMuscles = listOf("Rectus Abdominis"),
            equipment = "Bodyweight",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "abs_bw_mountain_climbers",
            name = "Mountain Climbers (Core Pacing)",
            targetMuscleGroup = "Abs / Core (Rectus Abdominis / Conditioning)",
            secondaryMuscles = listOf("Shoulders", "Hip Flexors"),
            equipment = "Bodyweight",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "abs_bw_dragon_flag",
            name = "Dragon Flag (Bodyweight Bench)",
            targetMuscleGroup = "Abs / Core (Full Abdominal Wall / Advanced Anti-Extension)",
            secondaryMuscles = listOf("Lats", "Lower Back"),
            equipment = "Bodyweight",
            category = "Compound"
        ),

        // 2. CABLE (7 items)
        DefaultExerciseItem(
            id = "abs_cable_kneeling_crunch_rope",
            name = "Kneeling Cable Crunch (Rope Attachment - Rectus Abdominis)",
            targetMuscleGroup = "Abs / Core (Upper Rectus Abdominis)",
            secondaryMuscles = listOf("Serratus"),
            equipment = "Cable",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "abs_cable_woodchopper_high_low",
            name = "Standing Cable Woodchopper (High-to-Low - Obliques)",
            targetMuscleGroup = "Abs / Core (Obliques / Rotational Power)",
            secondaryMuscles = listOf("Shoulders", "Core"),
            equipment = "Cable",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "abs_cable_woodchopper_low_high",
            name = "Standing Cable Woodchopper (Low-to-High - Obliques)",
            targetMuscleGroup = "Abs / Core (Obliques / Rotational Power)",
            secondaryMuscles = listOf("Shoulders", "Hip Rotators"),
            equipment = "Cable",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "abs_cable_woodchopper_horizontal",
            name = "Standing Cable Woodchopper (Mid / Horizontal - Obliques)",
            targetMuscleGroup = "Abs / Core (Obliques / Rotational Power)",
            secondaryMuscles = listOf("Shoulders", "Core"),
            equipment = "Cable",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "abs_cable_pallof_press",
            name = "Cable Pallof Press (Anti-Rotation Core Brace)",
            targetMuscleGroup = "Abs / Core (Anti-Rotation / Core Stiffness)",
            secondaryMuscles = listOf("Chest", "Shoulders"),
            equipment = "Cable",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "abs_cable_side_bend",
            name = "Cable Side Bend (Standing Oblique Contraction)",
            targetMuscleGroup = "Abs / Core (Obliques)",
            secondaryMuscles = listOf("Quadratus Lumborum"),
            equipment = "Cable",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "abs_cable_kneeling_oblique_crunch",
            name = "Kneeling Cable Oblique Crunch (Single-Side)",
            targetMuscleGroup = "Abs / Core (Obliques)",
            secondaryMuscles = listOf("Serratus"),
            equipment = "Cable",
            category = "Isolation"
        ),

        // 3. MACHINE (5 items)
        DefaultExerciseItem(
            id = "abs_mach_seated_crunch_pin_loaded",
            name = "Seated Abdominal Crunch Machine (Pin-Loaded)",
            targetMuscleGroup = "Abs / Core (Rectus Abdominis)",
            secondaryMuscles = emptyList(),
            equipment = "Machine",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "abs_mach_plate_loaded_ab_crunch",
            name = "Plate-Loaded Ab Crunch Machine",
            targetMuscleGroup = "Abs / Core (Rectus Abdominis)",
            secondaryMuscles = emptyList(),
            equipment = "Machine",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "abs_mach_rotary_torso",
            name = "Rotary Torso Machine (Obliques / Rotation)",
            targetMuscleGroup = "Abs / Core (Obliques / Rotational)",
            secondaryMuscles = listOf("Deep Spinal Rotators"),
            equipment = "Machine",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "abs_mach_roman_chair_sit_up",
            name = "Roman Chair Sit-ups / Incline Sit-up Bench",
            targetMuscleGroup = "Abs / Core (Upper & Lower Rectus Abdominis)",
            secondaryMuscles = listOf("Hip Flexors"),
            equipment = "Machine",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "abs_mach_hyperextension_side_bends",
            name = "Hyperextension Side Bends (Obliques on 45° Bench)",
            targetMuscleGroup = "Abs / Core (Obliques / Quadratus Lumborum)",
            secondaryMuscles = listOf("Lower Back"),
            equipment = "Machine",
            category = "Isolation"
        ),

        // 4. DUMBBELL (5 items)
        DefaultExerciseItem(
            id = "abs_db_side_bends_standing",
            name = "Dumbbell Side Bends (Standing Single-Side)",
            targetMuscleGroup = "Abs / Core (Obliques / Lateral Flexion)",
            secondaryMuscles = listOf("Forearms", "Quadratus Lumborum"),
            equipment = "Dumbbell",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "abs_db_russian_twists",
            name = "Dumbbell Russian Twists (Holding Dumbbell)",
            targetMuscleGroup = "Abs / Core (Obliques / Rotational)",
            secondaryMuscles = listOf("Rectus Abdominis"),
            equipment = "Dumbbell",
            category = "Isolation"
        ),
        DefaultExerciseItem(
            id = "abs_db_weighted_incline_sit_ups",
            name = "Weighted Incline Sit-ups (Dumbbell to Chest)",
            targetMuscleGroup = "Abs / Core (Rectus Abdominis)",
            secondaryMuscles = listOf("Hip Flexors"),
            equipment = "Dumbbell",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "abs_db_overhead_sit_ups",
            name = "Dumbbell Overhead Sit-ups",
            targetMuscleGroup = "Abs / Core (Rectus Abdominis / Core Stability)",
            secondaryMuscles = listOf("Shoulders", "Upper Back"),
            equipment = "Dumbbell",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "abs_db_suitcase_carry",
            name = "Dumbbell Suitcase Carry (Unilateral Anti-Lateral Flexion)",
            targetMuscleGroup = "Abs / Core (Anti-Lateral Flexion / Obliques)",
            secondaryMuscles = listOf("Forearms / Grip", "Traps"),
            equipment = "Dumbbell",
            category = "Compound"
        ),

        // 5. BARBELL / OTHER (6 items)
        DefaultExerciseItem(
            id = "abs_other_ab_wheel_knees",
            name = "Ab Wheel Rollout (From Knees - Anti-Extension)",
            targetMuscleGroup = "Abs / Core (Full Rectus Abdominis / Anti-Extension)",
            secondaryMuscles = listOf("Lats", "Shoulders"),
            equipment = "Other",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "abs_other_ab_wheel_standing",
            name = "Ab Wheel Rollout (Standing Full Stretch)",
            targetMuscleGroup = "Abs / Core (Full Abdominal Wall)",
            secondaryMuscles = listOf("Lats", "Shoulders", "Hip Flexors"),
            equipment = "Other",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "abs_bb_rollout_floor",
            name = "Barbell Rollout (Loaded Bar on Floor)",
            targetMuscleGroup = "Abs / Core (Full Abdominal Wall)",
            secondaryMuscles = listOf("Lats", "Triceps"),
            equipment = "Barbell",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "abs_bb_landmine_180s",
            name = "Landmine 180s / Rotations (Obliques & Core)",
            targetMuscleGroup = "Abs / Core (Obliques / Anti-Rotation)",
            secondaryMuscles = listOf("Shoulders", "Arms"),
            equipment = "Barbell",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "abs_bb_suitcase_hold_carry",
            name = "Barbell Suitcase Hold / Carry",
            targetMuscleGroup = "Abs / Core (Anti-Lateral Flexion / Core Brace)",
            secondaryMuscles = listOf("Forearms / Grip", "Traps", "Obliques"),
            equipment = "Barbell",
            category = "Compound"
        ),
        DefaultExerciseItem(
            id = "abs_db_weighted_hanging_leg_raise",
            name = "Weighted Hanging Leg Raises (Dumbbell Between Feet)",
            targetMuscleGroup = "Abs / Core (Lower Rectus Abdominis)",
            secondaryMuscles = listOf("Hip Flexors", "Forearms / Grip"),
            equipment = "Dumbbell",
            category = "Compound"
        )
    )

    val absExercises: List<ExerciseEntity> = absExerciseItems.map { it.toEntity() }

    val cardioExerciseItems = listOf(
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

    val cardioExercises: List<ExerciseEntity> = cardioExerciseItems.map { it.toEntity() }

    val coreAndCardioExerciseItems = absExerciseItems + cardioExerciseItems
    val coreAndCardioExercises: List<ExerciseEntity> = coreAndCardioExerciseItems.map { it.toEntity() }

    val allDefaultExercises: List<ExerciseEntity> =
        chestExercises +
        backExercises +
        shoulderExercises +
        legExercises +
        armExercises +
        trapsExercises +
        coreAndCardioExercises

    val allInitialExercises: List<ExerciseEntity> = allDefaultExercises
}


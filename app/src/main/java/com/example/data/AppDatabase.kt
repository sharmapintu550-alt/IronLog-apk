package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.*
import com.example.data.entity.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate

@Database(
    entities = [
        RoutineEntity::class,
        WeeklySplitEntity::class,
        WorkoutLogEntity::class,
        ExerciseEntity::class,
        BodyMeasurementEntity::class,
        ActiveWorkoutDraftEntity::class,
        ActiveWorkoutStateEntity::class,
        UserProfileEntity::class
    ],
    version = 12,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun routineDao(): RoutineDao
    abstract fun weeklySplitDao(): WeeklySplitDao
    abstract fun workoutLogDao(): WorkoutLogDao
    abstract fun exerciseDao(): ExerciseDao
    abstract fun bodyMeasurementDao(): BodyMeasurementDao
    abstract fun activeWorkoutDraftDao(): ActiveWorkoutDraftDao
    abstract fun activeWorkoutStateDao(): ActiveWorkoutStateDao
    abstract fun userProfileDao(): UserProfileDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "ironlog_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateInitialData(database)
                    }
                }
            }

            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            database.routineDao().deletePresetRoutines()
                            database.workoutLogDao().purgeSeedLogs()
                        } catch (e: Exception) {
                            android.util.Log.e("AppDatabase", "Error purging preset routines or seed logs", e)
                        }

                        val allDefaults = DefaultExercisesData.allInitialExercises
                        seedAndDeduplicateExercises(database, allDefaults)
                    }
                }
            }

            private suspend fun seedAndDeduplicateExercises(
                database: AppDatabase,
                defaults: List<ExerciseEntity>
            ) {
                try {
                    val existing = database.exerciseDao().getAllExercisesOnce()
                    if (existing.isEmpty()) {
                        database.exerciseDao().insertAll(defaults)
                        return
                    }

                    // 1. Map of normalized legacy names to the new standardized default names
                    val legacyNameToStandardName = mapOf(
                        "barbell bench press" to "Flat Barbell Bench Press (Standard Grip)",
                        "incline barbell bench press" to "Incline Barbell Bench Press (30 Degree)",
                        "dumbbell bench press" to "Flat Dumbbell Bench Press (Pronated Grip)",
                        "incline dumbbell press" to "Incline Dumbbell Bench Press (30 Degree)",
                        "decline dumbbell press" to "Decline Dumbbell Bench Press",
                        "cable chest fly (mid pulley)" to "Cable Fly (Mid-Chest / Level Height)",
                        "incline cable fly (low to high)" to "Low-to-High Cable Fly (Upper Chest Clavicular Focus)",
                        "high-to-low cable crossover" to "Cable Crossover (High-to-Low / Lower Chest)",
                        "dumbbell chest fly" to "Flat Dumbbell Fly",
                        "incline dumbbell fly" to "Incline Dumbbell Fly (30 Degree)",
                        "pec deck fly (machine fly)" to "Pec Deck Fly Machine (Seated)",
                        "chest press machine" to "Machine Chest Press (Seated Flat / Horizontal)",
                        "incline chest press machine" to "Machine Incline Chest Press",
                        "push-up" to "Push-ups (Standard Grip)",
                        "push up" to "Push-ups (Standard Grip)",
                        "diamond push-up" to "Diamond Push-ups (Chest & Triceps Focus)",
                        "diamond push up" to "Diamond Push-ups (Chest & Triceps Focus)",
                        "chest dips" to "Chest Dips (Leaning Forward / Torso Angled)",
                        "weighted dips" to "Weighted Chest Dips",
                        "weighted chest dips" to "Weighted Chest Dips",

                        // Back legacy mappings
                        "barbell bent-over row (overhand grip)" to "Barbell Bent-Over Row (Overhand / Pronated Grip)",
                        "barbell bent-over row (underhand / yates grip)" to "Barbell Bent-Over Row (Underhand / Supinated Grip / Yates Row)",
                        "pendlay row" to "Pendlay Row (Strict Dead-Stop from Floor)",
                        "t-bar row (chest supported)" to "T-Bar Row Machine (Chest Supported)",
                        "landmine t-bar row" to "T-Bar Row (Barbell Corner / Landmine - Neutral Grip)",
                        "single-arm dumbbell row" to "Single-Arm Dumbbell Row (Knee on Bench)",
                        "chest-supported incline dumbbell row" to "Chest-Supported Incline Dumbbell Row (Neutral Grip)",
                        "lat pulldown (close neutral grip)" to "Lat Pulldown (Close Neutral Grip / V-Bar)",
                        "lat pulldown (reverse / underhand grip)" to "Lat Pulldown (Underhand / Supinated Grip)",
                        "single-arm cable lat pulldown (half-kneeling)" to "Single-Arm Half-Kneeling Lat Pulldown",
                        "seated cable row (v-bar / close grip)" to "Seated Cable Row (Close Neutral Grip / V-Bar)",
                        "seated cable row (wide grip bar)" to "Seated Cable Row (Wide Grip Bar - Overhand)",
                        "single-arm seated cable row" to "Single-Arm Seated Cable Row (Lat Sweep Focus)",
                        "standing cable face pull" to "Cable Face Pull (Rope - Upper Traps & Rear Delts)",
                        "straight-arm cable lat pushdown (rope)" to "Standing Straight-Arm Cable Pulldown (Rope Attachment)",
                        "straight-arm cable lat pushdown (straight bar)" to "Standing Straight-Arm Cable Pulldown (Straight Bar)",
                        "pull-up (bodyweight overhand)" to "Pull-ups (Standard Overhand Grip)",
                        "weighted pull-up" to "Weighted Pull-ups",
                        "chin-up (bodyweight underhand grip)" to "Chin-ups (Underhand / Supinated Grip)",
                        "weighted chin-up" to "Weighted Chin-ups",
                        "neutral grip pull-up" to "Neutral-Grip Pull-ups (Parallel Grips)",
                        "inverted row (bodyweight / barbell)" to "Inverted Rows (Australian Pull-ups)",
                        "machine iso-lateral high row" to "Plate-Loaded Iso-Lateral High Row",
                        "machine iso-lateral low row" to "Plate-Loaded Iso-Lateral Low Row",
                        "hyperextension / back extension (45 degree)" to "Back Hyperextensions (45 Degree Bench)",
                        "weighted hyperextension" to "Weighted Hyperextensions (Holding Plate)",
                        "good morning" to "Barbell Good Mornings (Lower Back Focus)",
                        "barbell shrug" to "Barbell Shrugs (Traps Focus)",
                        "dumbbell shrug" to "Dumbbell Shrugs (Neutral Grip)",
                        "smith machine shrug" to "Smith Machine Shrugs (Front)",
                        "dumbbell pullover" to "Dumbbell Pullover (Lat Focus)",

                        // Shoulders Legacy Mappings
                        "overhead barbell press (standing ohp)" to "Standing Overhead Press (OHP / Military Press)",
                        "overhead shoulder press" to "Standing Overhead Press (OHP / Military Press)",
                        "standing overhead press" to "Standing Overhead Press (OHP / Military Press)",
                        "seated overhead barbell press" to "Seated Overhead Barbell Press",
                        "behind-the-neck barbell press" to "Behind-the-Neck Barbell Press",
                        "barbell upright row" to "Barbell Upright Row (Standard Grip)",
                        "barbell upright row (wide grip)" to "Barbell Upright Row (Wide Grip - Lateral Delt Focus)",
                        "barbell front raise" to "Barbell Front Raise (Overhand Grip)",
                        "landmine single-arm shoulder press" to "Landmine Shoulder Press (Single-Arm)",
                        "push press" to "Push Press (Barbell Power)",
                        "seated dumbbell shoulder press" to "Seated Dumbbell Shoulder Press (Pronated Grip)",
                        "standing dumbbell shoulder press" to "Standing Dumbbell Overhead Press",
                        "arnold press" to "Arnold Press (Rotating Grip)",
                        "standing dumbbell lateral raise" to "Standing Dumbbell Lateral Raise (Side Delts)",
                        "dumbbell lateral raise" to "Standing Dumbbell Lateral Raise (Side Delts)",
                        "seated dumbbell lateral raise" to "Seated Dumbbell Lateral Raise",
                        "lean-away dumbbell lateral raise" to "Incline Leaning Dumbbell Lateral Raise (Single-Arm)",
                        "bent-over rear delt dumbbell fly" to "Bent-Over Dumbbell Rear Delt Fly (Standing)",
                        "incline bench rear delt dumbbell fly" to "Chest-Supported Incline Rear Delt Fly (Dumbbell)",
                        "dumbbell front raise" to "Dumbbell Front Raise (Alternating Pronated)",
                        "dumbbell upright row" to "Dumbbell Upright Row",
                        "cable lateral raise (behind-the-back)" to "Cable Lateral Raise (Behind the Back)",
                        "single-arm cable lateral raise (behind back)" to "Cable Lateral Raise (Behind the Back)",
                        "cable lateral raise (in-front)" to "Cable Lateral Raise (In Front of Body)",
                        "single-arm cable lateral raise (in front)" to "Cable Lateral Raise (In Front of Body)",
                        "dual cable cross lateral raise" to "Dual Cable Lateral Raise (Cross-Body)",
                        "standing cable face pull (high pulley)" to "Cable Face Pull (Rope - Rear Delts / High Angle)",
                        "cable rear delt fly (high cross)" to "Dual Cable Rear Delt Fly (Reverse Cable Crossover)",
                        "single-arm cable rear delt fly" to "Single-Arm Cable Rear Delt Fly (No Handle / At Ear Height)",
                        "cable upright row (straight bar)" to "Cable Upright Row (Rope / Straight Bar)",
                        "cable front raise (rope / bar)" to "Cable Front Raise (Straight Bar / Low Pulley)",
                        "cable front raise (rope attachment)" to "Single-Arm Cable Front Raise (Rope / Low Pulley)",
                        "machine shoulder press" to "Machine Shoulder Press (Pin-Loaded - Neutral Grip)",
                        "rear delt machine fly (reverse pec deck)" to "Rear Delt Machine Fly (Reverse Pec Deck)",
                        "machine lateral raise" to "Machine Lateral Raise (Seated Pad-Loaded)",
                        "smith machine overhead press" to "Smith Machine Seated Overhead Press (Front)",
                        "pike push-up" to "Pike Push-ups (Shoulder Focus)",

                        // Legs Legacy Mappings
                        "barbell back squat" to "Barbell Back Squat (High Bar)",
                        "squat" to "Barbell Back Squat (High Bar)",
                        "barbell front squat" to "Barbell Front Squat (Clean Grip / Cross-Arm)",
                        "romanian deadlift" to "Barbell Romanian Deadlift (RDL - Hamstrings/Glutes)",
                        "romanian deadlift (barbell rdl)" to "Barbell Romanian Deadlift (RDL - Hamstrings/Glutes)",
                        "barbell rdl" to "Barbell Romanian Deadlift (RDL - Hamstrings/Glutes)",
                        "stiff-legged deadlift" to "Barbell Stiff-Legged Deadlift (SLDL)",
                        "sumo deadlift" to "Sumo Deadlift (Barbell)",
                        "barbell hip thrust" to "Barbell Hip Thrust (Glutes Focus)",
                        "barbell good mornings (lower back focus)" to "Barbell Good Mornings (Hamstrings & Posterior Chain)",
                        "good morning" to "Barbell Good Mornings (Hamstrings & Posterior Chain)",
                        "barbell walking lunge" to "Barbell Walking Lunges",
                        "zercher squat" to "Zercher Squat (Barbell in Crook of Elbows)",
                        "box squat" to "Barbell Box Squats",
                        "dumbbell romanian deadlift" to "Dumbbell Romanian Deadlift (RDL)",
                        "bulgarian split squat (dumbbell)" to "Dumbbell Bulgarian Split Squat (Rear Foot Elevated)",
                        "goblet squat (dumbbell / kettlebell)" to "Dumbbell Goblet Squat (Heels Flat)",
                        "goblet squat" to "Dumbbell Goblet Squat (Heels Flat)",
                        "dumbbell walking lunge" to "Dumbbell Walking Lunges",
                        "reverse lunge (dumbbell)" to "Dumbbell Reverse Lunges",
                        "step-up (dumbbell / box)" to "Dumbbell Step-ups (Bench / Box)",
                        "single-leg romanian deadlift (dumbbell)" to "Dumbbell Single-Leg Romanian Deadlift",
                        "single-leg dumbbell calf raise" to "Dumbbell Standing Calf Raise (Single-Leg)",
                        "cable pull-through" to "Cable Pull-Through (Glutes & Hamstrings)",
                        "leg press (45 degree sled)" to "Leg Press (45 Degree Sled - Standard Foot Placement)",
                        "leg press 45°" to "Leg Press (45 Degree Sled - Standard Foot Placement)",
                        "single-leg press" to "Single-Leg 45 Degree Leg Press",
                        "hack squat (machine)" to "Hack Squat Machine (Standard Stance)",
                        "reverse hack squat" to "Reverse Hack Squat (Glutes Focus)",
                        "leg extension machine" to "Leg Extension Machine (Quads)",
                        "single-leg extension" to "Single-Leg Extension Machine",
                        "lying leg curl (prone)" to "Lying Leg Curl Machine (Hamstrings)",
                        "seated leg curl" to "Seated Leg Curl Machine (Hamstrings)",
                        "standing single-leg curl" to "Standing Single-Leg Curl Machine",
                        "seated hip abduction machine" to "Hip Abduction Machine (Outer Thighs / Glute Medius)",
                        "seated hip adduction machine" to "Hip Adduction Machine (Inner Thighs / Groin)",
                        "standing calf raise (machine)" to "Standing Calf Raise Machine",
                        "seated calf raise (machine)" to "Seated Calf Raise Machine (Soleus Focus)",
                        "donkey calf raise" to "Donkey Calf Raise Machine",
                        "smith machine squat" to "Smith Machine Back Squat",
                        "smith machine calf raise" to "Smith Machine Standing Calf Raise (Elevated on Block)",
                        "sissy squat (machine / bodyweight)" to "Sissy Squats (Bodyweight Quad Focus)",
                        "nordic hamstring curl" to "Nordic Hamstring Curls (Bodyweight / Partner-Assisted)",
                        "dumbbell glute bridge" to "Double-Leg Glute Bridges (Floor)",
                        "machine hip thrust" to "Machine Glute Kickback / Plate-Loaded Glute Press",
                        "bodyweight jump squat" to "Jump Squats (Plyometric)",

                        // Abs & Core Legacy Mappings
                        "hanging leg raise" to "Hanging Leg Raises (Straight Legs - Bar)",
                        "hanging leg raises" to "Hanging Leg Raises (Straight Legs - Bar)",
                        "hanging knee raise" to "Hanging Knee Raises (Bar)",
                        "hanging knee raises" to "Hanging Knee Raises (Bar)",
                        "captain's chair leg raise" to "Captain's Chair Leg Raises (Dip Station)",
                        "captain's chair knee raise" to "Captain's Chair Knee Raises",
                        "cable kneeling crunch" to "Kneeling Cable Crunch (Rope Attachment - Rectus Abdominis)",
                        "kneeling cable crunch" to "Kneeling Cable Crunch (Rope Attachment - Rectus Abdominis)",
                        "machine abdominal crunch" to "Seated Abdominal Crunch Machine (Pin-Loaded)",
                        "ab crunch machine" to "Seated Abdominal Crunch Machine (Pin-Loaded)",
                        "decline bench weighted crunch" to "Weighted Incline Sit-ups (Dumbbell to Chest)",
                        "ab wheel rollout (from knees)" to "Ab Wheel Rollout (From Knees - Anti-Extension)",
                        "ab wheel rollout" to "Ab Wheel Rollout (From Knees - Anti-Extension)",
                        "standing ab wheel rollout" to "Ab Wheel Rollout (Standing Full Stretch)",
                        "standard floor plank" to "Standard Plank (Elbows on Floor)",
                        "plank" to "Standard Plank (Elbows on Floor)",
                        "weighted plank" to "Standard Plank (Elbows on Floor)",
                        "side plank (left & right)" to "Side Plank (Floor)",
                        "side plank" to "Side Plank (Floor)",
                        "cable woodchopper (high to low)" to "Standing Cable Woodchopper (High-to-Low - Obliques)",
                        "cable woodchopper (low to high)" to "Standing Cable Woodchopper (Low-to-High - Obliques)",
                        "cable woodchoppers" to "Standing Cable Woodchopper (High-to-Low - Obliques)",
                        "cable woodchopper" to "Standing Cable Woodchopper (High-to-Low - Obliques)",
                        "russian twist (bodyweight / plate)" to "Russian Twists (Bodyweight)",
                        "russian twist" to "Russian Twists (Bodyweight)",
                        "russian twists" to "Russian Twists (Bodyweight)",
                        "bicycle crunch" to "Bicycle Crunches (Obliques)",
                        "dragon flag" to "Dragon Flag (Bodyweight Bench)",
                        "hollow body hold" to "Hollow Body Hold",
                        "dead bug" to "Dead Bug (Core Stabilization)",
                        "v-up" to "V-Ups (Full Body Fold)",
                        "v-ups" to "V-Ups (Full Body Fold)",
                        "reverse crunch (flat bench)" to "Reverse Crunches (Floor / Flat Bench)",
                        "reverse crunch" to "Reverse Crunches (Floor / Flat Bench)",
                        "landmine 180 / landmine twist" to "Landmine 180s / Rotations (Obliques & Core)",
                        "landmine 180" to "Landmine 180s / Rotations (Obliques & Core)",
                        "pallof press (cable)" to "Cable Pallof Press (Anti-Rotation Core Brace)",
                        "pallof press" to "Cable Pallof Press (Anti-Rotation Core Brace)",
                        "dumbbell side bend" to "Dumbbell Side Bends (Standing Single-Side)",
                        "dumbbell side bends" to "Dumbbell Side Bends (Standing Single-Side)",
                        "mountain climbers" to "Mountain Climbers (Core Pacing)",
                        "standing cable oblique crunch" to "Kneeling Cable Oblique Crunch (Single-Side)",
                        "barbell rollout" to "Barbell Rollout (Loaded Bar on Floor)",
                        "floor crunch" to "Standard Floor Crunches",
                        "crunches" to "Standard Floor Crunches",

                        // Forearms Legacy Mappings
                        "barbell wrist curl (forearms over bench)" to "Barbell Wrist Curl (Palms Up / Forearm Flexors - Seated on Bench)",
                        "barbell wrist curl" to "Barbell Wrist Curl (Palms Up / Forearm Flexors - Seated on Bench)",
                        "barbell reverse wrist curl" to "Barbell Reverse Wrist Curl (Palms Down / Forearm Extensors)",
                        "standing behind-the-back barbell wrist curl" to "Behind-the-Back Barbell Wrist Curl (Standing)",
                        "behind-the-back barbell wrist curl" to "Behind-the-Back Barbell Wrist Curl (Standing)",
                        "standing barbell reverse curl" to "Standing Barbell Reverse Curl (Thumbless / Overhand Grip)",
                        "dumbbell wrist curl" to "Dumbbell Seated Wrist Curl (Palms Up - Single Arm)",
                        "dumbbell seated wrist curl" to "Dumbbell Seated Wrist Curl (Palms Up - Single Arm)",
                        "dumbbell reverse wrist curl" to "Dumbbell Seated Reverse Wrist Curl (Palms Down)",
                        "dumbbell seated reverse wrist curl" to "Dumbbell Seated Reverse Wrist Curl (Palms Down)",
                        "dumbbell hammer curl (wrist / brachioradialis focus)" to "Dumbbell Hammer Curl (Wrist / Brachioradialis Focus)",
                        "dead hang (bar pull-up hang)" to "Dead Hang from Pull-up Bar (Timed Grip Strength)",
                        "dead hang" to "Dead Hang from Pull-up Bar (Timed Grip Strength)",
                        "towel grip dead hang" to "Towel Grip Dead Hang",
                        "plate pinch hold" to "Plate Pinch Carry / Hold (Two Smooth Plates Together)",
                        "plate pinch carry" to "Plate Pinch Carry / Hold (Two Smooth Plates Together)",
                        "wrist roller (cable / weight plate)" to "Wrist Roller (Extension & Flexion)",
                        "wrist roller" to "Wrist Roller (Extension & Flexion)",

                        // Traps Legacy Mappings
                        "farmers walk (heavy dumbbell carry)" to "Farmer's Walk / Carry (Heavy Dumbbells)",
                        "farmers walk" to "Farmer's Walk / Carry (Heavy Dumbbells)",
                        "farmer's walk" to "Farmer's Walk / Carry (Heavy Dumbbells)",
                        "trap bar farmers carry" to "Trap Bar Farmer's Walk",
                        "trap bar farmer's walk" to "Trap Bar Farmer's Walk",
                        "trap bar shrugs" to "Trap Bar Shrugs (Neutral Grip)",
                        "trap bar shrug" to "Trap Bar Shrugs (Neutral Grip)",
                        "seated shrug machine" to "Seated Shrug Machine (Pin-Loaded)",
                        "calf raise machine shoulder shrugs" to "Calf Raise Machine Shoulder Shrugs (Padded Shoulders)",
                        "landmine shrugs" to "Landmine Shrugs (Single Arm)",
                        "barbell power shrugs" to "Barbell Power Shrugs (Slight Knee Dip & Pop)",
                        "snatch-grip barbell shrugs" to "Snatch-Grip Barbell Shrugs (Wide Grip - Upper Traps Focus)"
                    )

                    val defaultsByName = defaults.associateBy { it.name.trim().lowercase() }
                    val currentList = database.exerciseDao().getAllExercisesOnce().toMutableList()
                    val currentByName = currentList.associateBy { it.name.trim().lowercase() }.toMutableMap()
                    val currentById = currentList.associateBy { it.id }.toMutableMap()

                    // Upgrade any existing legacy names to their new standardized names if not already present
                    for ((legacyLower, standardName) in legacyNameToStandardName) {
                        val standardLower = standardName.trim().lowercase()
                        val legacyExercise = currentByName[legacyLower]
                        if (legacyExercise != null && !legacyExercise.isCustom) {
                            val alreadyHasStandard = currentByName.containsKey(standardLower)
                            if (!alreadyHasStandard) {
                                val standardTemplate = defaultsByName[standardLower]
                                val updated = legacyExercise.copy(
                                    name = standardName,
                                    equipment = standardTemplate?.equipment ?: legacyExercise.equipment,
                                    equipmentType = standardTemplate?.equipmentType ?: legacyExercise.equipmentType,
                                    equipmentCategory = standardTemplate?.equipmentCategory ?: legacyExercise.equipmentCategory,
                                    muscleGroup = standardTemplate?.muscleGroup ?: legacyExercise.muscleGroup,
                                    primaryMuscle = standardTemplate?.primaryMuscle ?: legacyExercise.primaryMuscle,
                                    secondaryMuscles = standardTemplate?.secondaryMuscles ?: legacyExercise.secondaryMuscles,
                                    category = standardTemplate?.category ?: legacyExercise.category
                                )
                                database.exerciseDao().updateExercise(updated)
                                currentByName.remove(legacyLower)
                                currentByName[standardLower] = updated
                                currentById[updated.id] = updated
                            } else {
                                // If we already have the standard entry, remove the redundant legacy duplicate if no PR
                                if (legacyExercise.prWeight <= 0.0 && legacyExercise.prReps == 0) {
                                    database.exerciseDao().deleteExerciseById(legacyExercise.id)
                                    currentByName.remove(legacyLower)
                                    currentById.remove(legacyExercise.id)
                                }
                            }
                        }
                    }

                    // Refresh current snapshot after upgrades
                    val refreshed = database.exerciseDao().getAllExercisesOnce()
                    val refreshedByName = refreshed.map { it.name.trim().lowercase() }.toSet()
                    val refreshedById = refreshed.map { it.id }.toSet()

                    // Insert missing defaults with zero duplicates guarantee
                    val toInsert = defaults.filter { defaultEx ->
                        val normName = defaultEx.name.trim().lowercase()
                        !refreshedById.contains(defaultEx.id) && !refreshedByName.contains(normName)
                    }

                    if (toInsert.isNotEmpty()) {
                        database.exerciseDao().insertExercisesIgnore(toInsert)
                    }

                    // Final deduplication pass: clean up any accidental duplicates by normalized name
                    val allAfterInsert = database.exerciseDao().getAllExercisesOnce()
                    val groupedByName = allAfterInsert.groupBy { it.name.trim().lowercase() }
                    for ((_, group) in groupedByName) {
                        if (group.size > 1) {
                            val best = group.maxByOrNull {
                                (if (it.prWeight > 0.0) 100 else 0) +
                                (if (it.prReps > 0) 50 else 0) +
                                (if (defaultsByName.containsKey(it.name.trim().lowercase())) 10 else 0)
                            } ?: group.first()
                            for (duplicate in group) {
                                if (duplicate.id != best.id && !duplicate.isCustom) {
                                    database.exerciseDao().deleteExerciseById(duplicate.id)
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    android.util.Log.e("AppDatabase", "Error during seed and deduplication", e)
                }
            }

            private suspend fun populateInitialData(database: AppDatabase) {
                // 1. Initial Routines: Clean slate (0 hardcoded presets)
                // Routines initialize 100% empty for fresh logins / guest sessions.

                // 2. Initial 7-Day Recurring Split (All default rest days)
                val splits = (1..7).map { day ->
                    WeeklySplitEntity(
                        dayOfWeek = day,
                        routineId = null,
                        routineTitle = null,
                        reminderTime = "06:30 PM",
                        isRestDay = true,
                        userId = "guest"
                    )
                }
                database.weeklySplitDao().insertAll(splits)

                // 3. Initial Exercises Library (258 exercises preserved intact)
                val otherExercises = listOf(
                    ExerciseEntity("ex_squat", "Barbell Back Squat", "Legs", "Barbell", 120.0, 6),
                    ExerciseEntity("ex_rdl", "Romanian Deadlift", "Legs", "Barbell", 95.0, 8),
                    ExerciseEntity("ex_leg_press", "Leg Press 45°", "Legs", "Machine", 240.0, 10),
                    ExerciseEntity("ex_overhead", "Overhead Shoulder Press", "Shoulders", "Barbell", 60.0, 6),
                    ExerciseEntity("ex_lat_raise", "Dumbbell Lateral Raise", "Shoulders", "Dumbbells", 14.0, 12),
                    ExerciseEntity("ex_bicep_curl", "Barbell Bicep Curl", "Arms", "Barbell", 40.0, 8),
                    ExerciseEntity("ex_rope_push", "Tricep Rope Pushdown", "Arms", "Cable", 32.5, 12),
                    ExerciseEntity("ex_plank", "Weighted Plank", "Core", "Bodyweight", 20.0, 60)
                )
                database.exerciseDao().insertAll(DefaultExercisesData.allInitialExercises + otherExercises)

                // 4. Initial Measurements
                val today = LocalDate.now()
                val m1 = BodyMeasurementEntity("m_wt_1", "Weight", 78.4, "kg", today.minusDays(6).toEpochDay())
                val m2 = BodyMeasurementEntity("m_wt_2", "Weight", 78.2, "kg", today.minusDays(4).toEpochDay())
                val m3 = BodyMeasurementEntity("m_wt_3", "Weight", 77.9, "kg", today.minusDays(2).toEpochDay())
                val m4 = BodyMeasurementEntity("m_wt_4", "Weight", 77.8, "kg", today.toEpochDay())
                database.bodyMeasurementDao().insertAll(listOf(m1, m2, m3, m4))
            }
        }
    }
}

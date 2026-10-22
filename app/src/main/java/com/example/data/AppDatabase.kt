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
        ActiveWorkoutDraftEntity::class
    ],
    version = 2,
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
                        val allDefaults = DefaultExercisesData.allInitialExercises
                        if (database.exerciseDao().getExerciseCount() == 0) {
                            database.exerciseDao().insertAll(allDefaults)
                        } else {
                            database.exerciseDao().insertExercisesIgnore(allDefaults)
                        }
                    }
                }
            }

            private suspend fun populateInitialData(database: AppDatabase) {
                // 1. Initial Routines
                val r1 = RoutineEntity(
                    id = "routine_chest_biceps",
                    title = "Chest & Biceps",
                    description = "Hypertrophy focus on pectorals & arms",
                    exercisesSummary = "Barbell Bench Press, Incline DB Press, Cable Fly, Barbell Curl, Hammer Curl",
                    orderIndex = 0
                )
                val r2 = RoutineEntity(
                    id = "routine_back_triceps",
                    title = "Back & Triceps",
                    description = "Lat width, thickness and triceps isolation",
                    exercisesSummary = "Deadlift, Lat Pulldown, Seated Cable Row, Tricep Rope Pushdown, Skull Crushers",
                    orderIndex = 1
                )
                val r3 = RoutineEntity(
                    id = "routine_legs",
                    title = "Leg Day Power",
                    description = "Quads, hamstrings, and calves heavy work",
                    exercisesSummary = "Barbell Back Squat, Romanian Deadlift, Leg Press, Standing Calf Raise",
                    orderIndex = 2
                )
                val r4 = RoutineEntity(
                    id = "routine_shoulders_core",
                    title = "Shoulder & Core",
                    description = "Deltoid boulder development & abdominal stability",
                    exercisesSummary = "Overhead Barbell Press, Lateral DB Raise, Rear Delt Fly, Hanging Leg Raise, Plank",
                    orderIndex = 3
                )
                database.routineDao().insertAll(listOf(r1, r2, r3, r4))

                // 2. Initial 7-Day Recurring Split (Monday = 1 ... Sunday = 7)
                val splits = listOf(
                    WeeklySplitEntity(dayOfWeek = 1, routineId = r1.id, routineTitle = r1.title, reminderTime = "06:30 PM", isRestDay = false),
                    WeeklySplitEntity(dayOfWeek = 2, routineId = r2.id, routineTitle = r2.title, reminderTime = "06:30 PM", isRestDay = false),
                    WeeklySplitEntity(dayOfWeek = 3, routineId = null, routineTitle = null, reminderTime = "06:30 PM", isRestDay = true),
                    WeeklySplitEntity(dayOfWeek = 4, routineId = r3.id, routineTitle = r3.title, reminderTime = "06:30 PM", isRestDay = false),
                    WeeklySplitEntity(dayOfWeek = 5, routineId = r4.id, routineTitle = r4.title, reminderTime = "06:30 PM", isRestDay = false),
                    WeeklySplitEntity(dayOfWeek = 6, routineId = null, routineTitle = null, reminderTime = "06:30 PM", isRestDay = true),
                    WeeklySplitEntity(dayOfWeek = 7, routineId = null, routineTitle = null, reminderTime = "06:30 PM", isRestDay = true)
                )
                database.weeklySplitDao().insertAll(splits)

                // 3. Completed workout logs for attendance streak through recent days
                val today = LocalDate.now()
                val log1 = WorkoutLogEntity(
                    id = "log_prev_1",
                    routineId = r1.id,
                    routineTitle = "Chest & Biceps",
                    dateEpochDay = today.minusDays(4).toEpochDay(),
                    durationSeconds = 3420,
                    totalVolumeKg = 8450.0,
                    totalSets = 18,
                    completedExercisesSummary = "Bench Press (4 sets), Incline DB (4 sets), Cable Fly (3 sets), Barbell Curl (4 sets), Hammer Curl (3 sets)"
                )
                val log2 = WorkoutLogEntity(
                    id = "log_prev_2",
                    routineId = r2.id,
                    routineTitle = "Back & Triceps",
                    dateEpochDay = today.minusDays(3).toEpochDay(),
                    durationSeconds = 3600,
                    totalVolumeKg = 9200.0,
                    totalSets = 19,
                    completedExercisesSummary = "Deadlift (4 sets), Lat Pulldown (4 sets), Cable Row (4 sets), Rope Pushdown (4 sets), Skull Crushers (3 sets)"
                )
                val log3 = WorkoutLogEntity(
                    id = "log_prev_3",
                    routineId = r3.id,
                    routineTitle = "Leg Day Power",
                    dateEpochDay = today.minusDays(1).toEpochDay(),
                    durationSeconds = 3780,
                    totalVolumeKg = 11300.0,
                    totalSets = 17,
                    completedExercisesSummary = "Squats (5 sets), RDL (4 sets), Leg Press (4 sets), Calf Raises (4 sets)"
                )
                database.workoutLogDao().insertAll(listOf(log1, log2, log3))

                // 4. Initial Exercises Library (Set 1: Chest + Set 2: Back + other categories)
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

                // 5. Initial Measurements
                val m1 = BodyMeasurementEntity("m_wt_1", "Weight", 78.4, "kg", today.minusDays(6).toEpochDay())
                val m2 = BodyMeasurementEntity("m_wt_2", "Weight", 78.2, "kg", today.minusDays(4).toEpochDay())
                val m3 = BodyMeasurementEntity("m_wt_3", "Weight", 77.9, "kg", today.minusDays(2).toEpochDay())
                val m4 = BodyMeasurementEntity("m_wt_4", "Weight", 77.8, "kg", today.toEpochDay())
                database.bodyMeasurementDao().insertAll(listOf(m1, m2, m3, m4))
            }
        }
    }
}

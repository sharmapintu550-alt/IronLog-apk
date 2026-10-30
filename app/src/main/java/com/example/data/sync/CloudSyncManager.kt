package com.example.data.sync

import android.content.Context
import android.util.Log
import com.example.R
import com.example.data.AppDatabase
import com.example.data.entity.ExerciseEntity
import com.example.data.entity.RoutineEntity
import com.example.data.entity.UserProfileEntity
import com.example.data.entity.WeeklySplitEntity
import com.example.data.entity.WorkoutLogEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout

sealed interface SyncStatus {
    data object Idle : SyncStatus
    data object Syncing : SyncStatus
    data class Success(
        val lastSyncTime: Long,
        val routinesSynced: Int,
        val historySynced: Int
    ) : SyncStatus
    data class Error(val message: String) : SyncStatus
}

class CloudSyncManager(
    private val context: Context,
    private val appDatabase: AppDatabase = AppDatabase.getDatabase(context)
) {

    companion object {
        private const val TAG = "CloudSyncManager"
        private const val SYNC_TIMEOUT_MS = 15000L

        @Volatile
        private var INSTANCE: CloudSyncManager? = null

        fun getInstance(context: Context): CloudSyncManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: CloudSyncManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    private val firestore: FirebaseFirestore by lazy {
        val databaseId = context.getString(R.string.firestore_database_id)
        FirebaseFirestore.getInstance(databaseId)
    }

    private val _syncStatus = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()

    private val routineDao = appDatabase.routineDao()
    private val workoutLogDao = appDatabase.workoutLogDao()
    private val weeklySplitDao = appDatabase.weeklySplitDao()
    private val userProfileDao = appDatabase.userProfileDao()
    private val exerciseDao = appDatabase.exerciseDao()

    /**
     * Executes safe, non-blocking bidirectional synchronization with Cloud Firestore.
     */
    suspend fun migrateGuestDataToCloud(userId: String? = null): Result<SyncStatus.Success> {
        val currentUid = userId ?: FirebaseAuth.getInstance().currentUser?.uid
        if (!currentUid.isNullOrBlank()) {
            try {
                val guestSplits = weeklySplitDao.getSplitsForUserOnce("guest")
                val activeGuestSplits = guestSplits.filter { !it.isRestDay && !it.routineId.isNullOrBlank() }
                if (activeGuestSplits.isNotEmpty()) {
                    val userSplits = activeGuestSplits.map { it.copy(userId = currentUid, updatedAt = System.currentTimeMillis()) }
                    weeklySplitDao.insertAll(userSplits)
                }
            } catch (e: Exception) {
                Log.w(TAG, "Error associating guest splits to cloud: ${e.message}")
            }
        }
        return syncAll(userId)
    }

    suspend fun syncAll(userId: String?): Result<SyncStatus.Success> = withContext(Dispatchers.IO) {
        val currentUid = userId ?: FirebaseAuth.getInstance().currentUser?.uid
        if (currentUid.isNullOrBlank()) {
            Log.d(TAG, "Sync skipped: No authenticated cloud user (Guest mode).")
            Log.e("AuthSyncDebug", "Firestore sync skipped: currentUid is null or blank (Guest mode)")
            val idle = SyncStatus.Idle
            _syncStatus.value = idle
            return@withContext Result.failure(IllegalStateException("User not authenticated"))
        }

        _syncStatus.value = SyncStatus.Syncing
        Log.i("AuthSyncDebug", "Starting Firestore syncAll for UID: $currentUid")

        try {
            withTimeout(SYNC_TIMEOUT_MS) {
                // 1. Sync User Profile
                syncUserProfile(currentUid)

                // 2. Sync Routines
                val routinesSynced = syncRoutines(currentUid)

                // 3. Sync Workout Logs (History)
                val historySynced = syncWorkoutLogs(currentUid)

                // 4. Sync Custom Exercises
                val customExercisesSynced = syncCustomExercises(currentUid)

                // 5. Sync Weekly Schedule Split
                syncWeeklySchedule(currentUid)

                val result = SyncStatus.Success(
                    lastSyncTime = System.currentTimeMillis(),
                    routinesSynced = routinesSynced,
                    historySynced = historySynced
                )
                _syncStatus.value = result
                Log.d(TAG, "Cloud sync completed successfully for $currentUid: $routinesSynced routines, $historySynced logs, $customExercisesSynced custom exercises.")
                Log.i("AuthSyncDebug", "Cloud sync succeeded for UID $currentUid: $routinesSynced routines, $historySynced logs, $customExercisesSynced custom exercises.")
                Result.success(result)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Cloud sync safely caught error: ${e.localizedMessage}")
            Log.e("AuthSyncDebug", "Firestore sync failed for UID $currentUid [${e.javaClass.simpleName}]: ${e.message}", e)
            val errorStatus = SyncStatus.Error(e.localizedMessage ?: "Sync encountered a network issue")
            _syncStatus.value = errorStatus
            Result.failure(e)
        }
    }

    private suspend fun syncRoutines(userId: String): Int {
        val userRoutinesRef = firestore.collection("users").document(userId).collection("routines")
        val localRoutines = routineDao.getRoutinesForUser(userId).first()
        val localMap = localRoutines.associateBy { it.id }

        // Fetch remote routines
        val remoteSnapshot = userRoutinesRef.get().await()
        val remoteMap = mutableMapOf<String, RoutineEntity>()

        for (doc in remoteSnapshot.documents) {
            val id = doc.id
            val title = doc.getString("title") ?: continue
            val description = doc.getString("description") ?: ""
            val exercisesSummary = doc.getString("exercisesSummary") ?: ""
            val exercisesJson = doc.getString("exercisesJson") ?: ""
            val sortOrder = (doc.getLong("sortOrder") ?: doc.getLong("orderIndex") ?: 0L).toInt()
            val createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()

            remoteMap[id] = RoutineEntity(
                id = id,
                userId = userId,
                title = title,
                description = description,
                exercisesSummary = exercisesSummary,
                exercisesJson = exercisesJson,
                sortOrder = sortOrder,
                createdAt = createdAt
            )
        }

        // Remote -> Local: insert new or updated routines into Room
        val toInsertLocally = mutableListOf<RoutineEntity>()
        for ((id, remoteRoutine) in remoteMap) {
            val localRoutine = localMap[id]
            if (localRoutine == null || remoteRoutine.createdAt > localRoutine.createdAt) {
                toInsertLocally.add(remoteRoutine)
            }
        }
        if (toInsertLocally.isNotEmpty()) {
            routineDao.insertAll(toInsertLocally)
        }

        // Local -> Remote: push new local routines to Firestore
        for (local in localRoutines) {
            val remote = remoteMap[local.id]
            if (remote == null || local.createdAt > remote.createdAt) {
                val data = mapOf(
                    "id" to local.id,
                    "title" to local.title,
                    "description" to local.description,
                    "exercisesSummary" to local.exercisesSummary,
                    "exercisesJson" to local.exercisesJson,
                    "sortOrder" to local.sortOrder,
                    "orderIndex" to local.sortOrder,
                    "createdAt" to local.createdAt
                )
                userRoutinesRef.document(local.id).set(data, SetOptions.merge()).await()
            }
        }

        return localRoutines.size + toInsertLocally.size
    }

    private suspend fun syncWorkoutLogs(userId: String): Int {
        val userWorkoutHistoryRef = firestore.collection("users").document(userId).collection("workout_history")
        val legacyHistoryRef = firestore.collection("users").document(userId).collection("history")
        val localLogs = workoutLogDao.getAllLogs().first()
        val localMap = localLogs.associateBy { it.id }

        // Fetch remote workout history logs (querying workout_history, with fallback to legacy history)
        var remoteSnapshot = try {
            userWorkoutHistoryRef.get().await()
        } catch (e: Exception) {
            Log.w(TAG, "Error fetching workout_history: ${e.message}")
            null
        }

        if (remoteSnapshot == null || remoteSnapshot.isEmpty) {
            remoteSnapshot = try {
                legacyHistoryRef.get().await()
            } catch (e: Exception) {
                null
            }
        }

        val seedTitles = setOf("Leg Day Power", "Back & Triceps", "Chest & Biceps", "Shoulder & Core", "Legs & Shoulders", "Back & Tricep", "Chest - Biceps")
        val remoteMap = mutableMapOf<String, WorkoutLogEntity>()
        if (remoteSnapshot != null) {
            for (doc in remoteSnapshot.documents) {
                val id = doc.id
                val routineId = doc.getString("routineId")
                val routineTitle = doc.getString("routineTitle") ?: "Workout"
                val dateEpochDay = doc.getLong("dateEpochDay") ?: 0L
                val durationSeconds = doc.getLong("durationSeconds") ?: 0L
                val totalVolumeKg = doc.getDouble("totalVolumeKg") ?: 0.0
                val totalSets = (doc.getLong("totalSets") ?: 0L).toInt()
                val summary = doc.getString("completedExercisesSummary") ?: ""
                val timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis()

                if (routineTitle in seedTitles || id.startsWith("seed_") || id.startsWith("mock_")) {
                    try {
                        userWorkoutHistoryRef.document(id).delete()
                    } catch (_: Exception) {}
                    continue
                }

                remoteMap[id] = WorkoutLogEntity(
                    id = id,
                    routineId = routineId,
                    routineTitle = routineTitle,
                    dateEpochDay = dateEpochDay,
                    durationSeconds = durationSeconds,
                    totalVolumeKg = totalVolumeKg,
                    totalSets = totalSets,
                    completedExercisesSummary = summary,
                    timestamp = timestamp
                )
            }
        }

        // Remote -> Local (Merge into Room with OnConflictStrategy.REPLACE)
        val toInsertLocally = mutableListOf<WorkoutLogEntity>()
        for ((id, remoteLog) in remoteMap) {
            if (!localMap.containsKey(id)) {
                toInsertLocally.add(remoteLog)
            }
        }
        if (toInsertLocally.isNotEmpty()) {
            workoutLogDao.insertAll(toInsertLocally)
        }

        // Local -> Remote (Backup to users/{userId}/workout_history/{workoutId})
        for (local in localLogs) {
            if (local.routineTitle in seedTitles || local.id.startsWith("seed_") || local.id.startsWith("mock_")) {
                continue
            }
            if (!remoteMap.containsKey(local.id)) {
                val data = mapOf(
                    "id" to local.id,
                    "workoutId" to local.id,
                    "routineId" to local.routineId,
                    "routineTitle" to local.routineTitle,
                    "dateEpochDay" to local.dateEpochDay,
                    "durationSeconds" to local.durationSeconds,
                    "totalVolumeKg" to local.totalVolumeKg,
                    "totalSets" to local.totalSets,
                    "completedExercisesSummary" to local.completedExercisesSummary,
                    "timestamp" to local.timestamp
                ).filterValues { it != null }
                userWorkoutHistoryRef.document(local.id).set(data, SetOptions.merge()).await()
            }
        }

        return localLogs.size + toInsertLocally.size
    }

    private suspend fun syncCustomExercises(userId: String): Int {
        val userCustomExRef = firestore.collection("users").document(userId).collection("custom_exercises")
        val allLocal = exerciseDao.getAllExercisesOnce()
        val localCustom = allLocal.filter { it.isCustom }
        val localMap = localCustom.associateBy { it.id }

        // Fetch remote custom exercises
        val remoteSnapshot = try {
            userCustomExRef.get().await()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to fetch remote custom exercises: ${e.message}")
            return localCustom.size
        }

        val remoteMap = mutableMapOf<String, ExerciseEntity>()
        for (doc in remoteSnapshot.documents) {
            val id = doc.id
            val name = doc.getString("name") ?: continue
            val muscleGroup = doc.getString("muscleGroup") ?: doc.getString("category") ?: "Other"
            val equipment = doc.getString("equipment") ?: doc.getString("equipmentType") ?: "Barbell"
            val notes = doc.getString("notes") ?: ""
            val barbellWeight = doc.getDouble("barbellWeightKg") ?: 20.0

            remoteMap[id] = ExerciseEntity(
                id = id,
                name = name,
                muscleGroup = muscleGroup,
                equipment = equipment,
                category = muscleGroup,
                equipmentType = equipment,
                notes = notes,
                barbellWeightKg = barbellWeight,
                isCustom = true
            )
        }

        // Remote -> Local (Merge into Room with OnConflictStrategy.REPLACE)
        val toInsertLocally = mutableListOf<ExerciseEntity>()
        for ((id, remoteEx) in remoteMap) {
            if (!localMap.containsKey(id)) {
                toInsertLocally.add(remoteEx)
            }
        }
        if (toInsertLocally.isNotEmpty()) {
            exerciseDao.insertAll(toInsertLocally)
        }

        // Local -> Remote
        for (local in localCustom) {
            if (!remoteMap.containsKey(local.id)) {
                val data = mapOf(
                    "id" to local.id,
                    "exerciseId" to local.id,
                    "name" to local.name,
                    "muscleGroup" to local.muscleGroup,
                    "equipment" to local.equipment,
                    "category" to local.category,
                    "equipmentType" to local.equipmentType,
                    "notes" to local.notes,
                    "barbellWeightKg" to local.barbellWeightKg,
                    "isCustom" to true,
                    "createdAt" to System.currentTimeMillis()
                )
                userCustomExRef.document(local.id).set(data, SetOptions.merge()).await()
            }
        }

        return localCustom.size + toInsertLocally.size
    }

    suspend fun syncWeeklySchedule(userId: String): Int {
        val userWeeklyScheduleRef = firestore.collection("users").document(userId).collection("weekly_schedule")
        val legacyScheduleRef = firestore.collection("users").document(userId).collection("schedule")
        val localSplits = weeklySplitDao.getSplitsForUser(userId).first()
        val localMap = localSplits.associateBy { it.dayOfWeek }

        // Fetch remote schedule documents from weekly_schedule, falling back to legacy schedule if empty
        var remoteSnapshot = try {
            userWeeklyScheduleRef.get().await()
        } catch (e: Exception) {
            Log.w(TAG, "Failed fetching weekly_schedule: ${e.message}")
            null
        }

        if (remoteSnapshot == null || remoteSnapshot.isEmpty) {
            remoteSnapshot = try {
                legacyScheduleRef.get().await()
            } catch (e: Exception) {
                null
            }
        }

        val remoteMap = mutableMapOf<Int, WeeklySplitEntity>()
        if (remoteSnapshot != null) {
            for (doc in remoteSnapshot.documents) {
                val dayOfWeek = (doc.getLong("dayOfWeek")
                    ?: doc.id.replace("day_", "").toIntOrNull()
                    ?: continue).toInt()
                val routineId = doc.getString("routineId")?.takeIf { it.isNotBlank() }
                val routineName = doc.getString("routineName") ?: doc.getString("routineTitle")
                val reminderTime = doc.getString("reminderTime") ?: "06:30 PM"
                val isRestDay = doc.getBoolean("isRestDay") ?: (routineId == null)
                val updatedAt = doc.getLong("updatedAt") ?: System.currentTimeMillis()

                remoteMap[dayOfWeek] = WeeklySplitEntity(
                    dayOfWeek = dayOfWeek,
                    routineId = routineId,
                    routineTitle = routineName,
                    reminderTime = reminderTime,
                    isRestDay = isRestDay,
                    userId = userId,
                    updatedAt = updatedAt
                )
            }
        }

        // Remote -> Local (Upsert into Room under current userId)
        val toInsertLocally = mutableListOf<WeeklySplitEntity>()
        for ((day, remoteSplit) in remoteMap) {
            val local = localMap[day]
            if (local == null || remoteSplit.updatedAt >= local.updatedAt) {
                toInsertLocally.add(remoteSplit)
            }
        }
        if (toInsertLocally.isNotEmpty()) {
            weeklySplitDao.insertAll(toInsertLocally)
        }

        // Local -> Remote (Backup to users/{userId}/weekly_schedule/{dayOfWeek})
        for (local in localSplits) {
            val remote = remoteMap[local.dayOfWeek]
            if (remote == null || local.updatedAt > remote.updatedAt) {
                val data = mapOf(
                    "dayOfWeek" to local.dayOfWeek,
                    "routineId" to (local.routineId ?: ""),
                    "routineName" to (local.routineTitle ?: ""),
                    "routineTitle" to (local.routineTitle ?: ""),
                    "reminderTime" to local.reminderTime,
                    "isRestDay" to local.isRestDay,
                    "userId" to userId,
                    "updatedAt" to (if (local.updatedAt > 0) local.updatedAt else System.currentTimeMillis())
                )
                userWeeklyScheduleRef.document(local.dayOfWeek.toString()).set(data, SetOptions.merge()).await()
            }
        }

        return localSplits.size + toInsertLocally.size
    }

    suspend fun syncUserProfile(userId: String): UserProfileEntity? {
        val userProfileRef = firestore.collection("users").document(userId).collection("profile").document("main")
        val userRootRef = firestore.collection("users").document(userId)
        val localProfile = userProfileDao.getUserProfile(userId).first()

        // Fetch remote document
        val remoteDoc = try {
            val doc = userProfileRef.get().await()
            if (doc.exists()) doc else userRootRef.get().await()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to fetch remote profile: ${e.message}")
            null
        }

        if (remoteDoc != null && remoteDoc.exists()) {
            val isComplete = remoteDoc.getBoolean("isProfileComplete") ?: false
            val updatedAt = remoteDoc.getLong("updatedAt") ?: System.currentTimeMillis()

            val remoteProfile = UserProfileEntity(
                userId = userId,
                email = remoteDoc.getString("email") ?: "",
                displayName = remoteDoc.getString("displayName") ?: "",
                gender = remoteDoc.getString("gender") ?: "Male",
                age = (remoteDoc.getLong("age") ?: 25L).toInt(),
                height = remoteDoc.getDouble("height") ?: remoteDoc.getDouble("heightCm") ?: 175.0,
                heightUnit = remoteDoc.getString("heightUnit") ?: "cm",
                bodyweight = remoteDoc.getDouble("bodyweight") ?: remoteDoc.getDouble("bodyweightKg") ?: 75.0,
                bodyweightUnit = remoteDoc.getString("bodyweightUnit") ?: "kg",
                preferredWeightUnit = remoteDoc.getString("preferredWeightUnit") ?: "kg",
                primaryGoal = remoteDoc.getString("primaryGoal") ?: "Strength & Hypertrophy",
                experienceLevel = remoteDoc.getString("experienceLevel") ?: "Intermediate (1-3 yrs)",
                isProfileComplete = isComplete,
                updatedAt = updatedAt
            )

            if (localProfile == null || remoteProfile.updatedAt >= localProfile.updatedAt) {
                userProfileDao.saveUserProfile(remoteProfile)
                return remoteProfile
            }
        }

        // If local profile exists and is newer or remote was absent, push local to remote
        if (localProfile != null) {
            pushProfileToFirestore(localProfile)
            return localProfile
        }

        return null
    }

    suspend fun saveAndSyncProfile(profile: UserProfileEntity) {
        userProfileDao.saveUserProfile(profile)
        pushProfileToFirestore(profile)
    }

    private suspend fun pushProfileToFirestore(profile: UserProfileEntity) {
        try {
            val data = mapOf(
                "userId" to profile.userId,
                "email" to profile.email,
                "displayName" to profile.displayName,
                "gender" to profile.gender,
                "age" to profile.age,
                "height" to profile.height,
                "heightUnit" to profile.heightUnit,
                "bodyweight" to profile.bodyweight,
                "bodyweightUnit" to profile.bodyweightUnit,
                "preferredWeightUnit" to profile.preferredWeightUnit,
                "primaryGoal" to profile.primaryGoal,
                "experienceLevel" to profile.experienceLevel,
                "isProfileComplete" to profile.isProfileComplete,
                "updatedAt" to profile.updatedAt
            )
            val subDoc = firestore.collection("users").document(profile.userId).collection("profile").document("main")
            val rootDoc = firestore.collection("users").document(profile.userId)

            subDoc.set(data, SetOptions.merge()).await()
            rootDoc.set(data, SetOptions.merge()).await()
            Log.i("AuthSyncDebug", "Pushed user profile to Firestore for UID: ${profile.userId}")
        } catch (e: Exception) {
            Log.e("AuthSyncDebug", "Failed to push profile to Firestore: ${e.message}", e)
        }
    }

    suspend fun pushRoutine(userId: String, routine: RoutineEntity) {
        try {
            val data = mapOf(
                "id" to routine.id,
                "title" to routine.title,
                "description" to routine.description,
                "exercisesSummary" to routine.exercisesSummary,
                "exercisesJson" to routine.exercisesJson,
                "sortOrder" to routine.sortOrder,
                "orderIndex" to routine.sortOrder,
                "createdAt" to routine.createdAt,
                "updatedAt" to System.currentTimeMillis()
            )
            firestore.collection("users").document(userId).collection("routines").document(routine.id).set(data, SetOptions.merge()).await()
            Log.i("AuthSyncDebug", "Pushed routine ${routine.id} to Firestore")
        } catch (e: Exception) {
            Log.e("AuthSyncDebug", "Failed to push routine ${routine.id} to Firestore: ${e.message}", e)
        }
    }

    suspend fun pushRoutineOrders(userId: String, orderedRoutines: List<Pair<String, Int>>) {
        if (userId.isBlank() || userId == "guest") return
        try {
            val batch = firestore.batch()
            val routinesCol = firestore.collection("users").document(userId).collection("routines")
            val now = System.currentTimeMillis()
            orderedRoutines.forEach { (routineId, order) ->
                val docRef = routinesCol.document(routineId)
                batch.set(
                    docRef,
                    mapOf(
                        "sortOrder" to order,
                        "orderIndex" to order,
                        "updatedAt" to now
                    ),
                    SetOptions.merge()
                )
            }
            batch.commit().await()
            Log.i("AuthSyncDebug", "Batch pushed ${orderedRoutines.size} routine orders to Firestore")
        } catch (e: Exception) {
            Log.e("AuthSyncDebug", "Failed to batch push routine orders: ${e.message}", e)
        }
    }

    suspend fun deleteRoutineFromCloud(userId: String, routineId: String) {
        try {
            firestore.collection("users").document(userId).collection("routines").document(routineId).delete().await()
            Log.i("AuthSyncDebug", "Deleted routine $routineId from Firestore")
        } catch (e: Exception) {
            Log.e("AuthSyncDebug", "Failed to delete routine $routineId from Firestore: ${e.message}", e)
        }
    }

    suspend fun pushWorkoutLog(userId: String, log: WorkoutLogEntity) {
        try {
            val data = mapOf(
                "id" to log.id,
                "workoutId" to log.id,
                "routineId" to log.routineId,
                "routineTitle" to log.routineTitle,
                "dateEpochDay" to log.dateEpochDay,
                "durationSeconds" to log.durationSeconds,
                "totalVolumeKg" to log.totalVolumeKg,
                "totalSets" to log.totalSets,
                "completedExercisesSummary" to log.completedExercisesSummary,
                "timestamp" to log.timestamp
            ).filterValues { it != null }
            firestore.collection("users").document(userId).collection("workout_history").document(log.id).set(data, SetOptions.merge()).await()
            Log.i("AuthSyncDebug", "Pushed workout log ${log.id} to Firestore")
        } catch (e: Exception) {
            Log.e("AuthSyncDebug", "Failed to push workout log ${log.id} to Firestore: ${e.message}", e)
        }
    }

    suspend fun deleteWorkoutLog(userId: String, logId: String) {
        try {
            firestore.collection("users").document(userId).collection("workout_history").document(logId).delete().await()
            firestore.collection("users").document(userId).collection("history").document(logId).delete().await()
            Log.i("AuthSyncDebug", "Deleted workout log $logId from Firestore")
        } catch (e: Exception) {
            Log.e("AuthSyncDebug", "Failed to delete workout log $logId from Firestore: ${e.message}", e)
        }
    }

    suspend fun pushCustomExercise(userId: String, exercise: ExerciseEntity) {
        try {
            val data = mapOf(
                "id" to exercise.id,
                "exerciseId" to exercise.id,
                "name" to exercise.name,
                "muscleGroup" to exercise.muscleGroup,
                "equipment" to exercise.equipment,
                "category" to exercise.category,
                "equipmentType" to exercise.equipmentType,
                "notes" to exercise.notes,
                "barbellWeightKg" to exercise.barbellWeightKg,
                "isCustom" to true,
                "createdAt" to System.currentTimeMillis()
            )
            firestore.collection("users").document(userId).collection("custom_exercises").document(exercise.id).set(data, SetOptions.merge()).await()
            Log.i("AuthSyncDebug", "Pushed custom exercise ${exercise.id} to Firestore")
        } catch (e: Exception) {
            Log.e("AuthSyncDebug", "Failed to push custom exercise ${exercise.id} to Firestore: ${e.message}", e)
        }
    }

    suspend fun pushWeeklyScheduleDay(userId: String, split: WeeklySplitEntity) {
        if (userId.isBlank() || userId == "guest") return
        try {
            val data = mapOf(
                "dayOfWeek" to split.dayOfWeek,
                "routineId" to (split.routineId ?: ""),
                "routineName" to (split.routineTitle ?: ""),
                "routineTitle" to (split.routineTitle ?: ""),
                "reminderTime" to split.reminderTime,
                "isRestDay" to split.isRestDay,
                "userId" to userId,
                "updatedAt" to (if (split.updatedAt > 0) split.updatedAt else System.currentTimeMillis())
            )
            firestore.collection("users").document(userId)
                .collection("weekly_schedule")
                .document(split.dayOfWeek.toString())
                .set(data, SetOptions.merge()).await()
            Log.i("AuthSyncDebug", "Pushed weekly schedule day ${split.dayOfWeek} to Firestore for user $userId")
        } catch (e: Exception) {
            Log.e("AuthSyncDebug", "Failed to push weekly schedule day ${split.dayOfWeek}: ${e.message}", e)
        }
    }

    suspend fun pushAllWeeklySchedule(userId: String, splits: List<WeeklySplitEntity>) {
        if (userId.isBlank() || userId == "guest") return
        try {
            val userScheduleRef = firestore.collection("users").document(userId).collection("weekly_schedule")
            val batch = firestore.batch()
            for (split in splits) {
                val docRef = userScheduleRef.document(split.dayOfWeek.toString())
                val data = mapOf(
                    "dayOfWeek" to split.dayOfWeek,
                    "routineId" to (split.routineId ?: ""),
                    "routineName" to (split.routineTitle ?: ""),
                    "routineTitle" to (split.routineTitle ?: ""),
                    "reminderTime" to split.reminderTime,
                    "isRestDay" to split.isRestDay,
                    "userId" to userId,
                    "updatedAt" to (if (split.updatedAt > 0) split.updatedAt else System.currentTimeMillis())
                )
                batch.set(docRef, data, SetOptions.merge())
            }
            batch.commit().await()
            Log.i("AuthSyncDebug", "Pushed all ${splits.size} weekly schedule days to Firestore for user $userId")
        } catch (e: Exception) {
            Log.e("AuthSyncDebug", "Failed to push all weekly schedule: ${e.message}", e)
        }
    }
}

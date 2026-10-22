package com.example.data.sync

import android.content.Context
import android.util.Log
import com.example.R
import com.example.data.AppDatabase
import com.example.data.entity.RoutineEntity
import com.example.data.entity.WeeklySplitEntity
import com.example.data.entity.WorkoutLogEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
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
        val databaseId = runCatching {
            context.getString(R.string.firestore_database_id)
        }.getOrNull()?.takeIf { it.isNotBlank() } ?: "ai-studio-android-ironpuls-28522c93-c468-47b9-ad45-00b4ca15bdb3"
        FirebaseFirestore.getInstance(databaseId)
    }

    private val _syncStatus = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()

    private val routineDao = appDatabase.routineDao()
    private val workoutLogDao = appDatabase.workoutLogDao()
    private val weeklySplitDao = appDatabase.weeklySplitDao()

    /**
     * Executes safe, non-blocking bidirectional synchronization with Cloud Firestore.
     */
    suspend fun syncAll(userId: String?): Result<SyncStatus.Success> = withContext(Dispatchers.IO) {
        val currentUid = userId ?: FirebaseAuth.getInstance().currentUser?.uid
        if (currentUid.isNullOrBlank()) {
            Log.d(TAG, "Sync skipped: No authenticated cloud user (Guest mode).")
            val idle = SyncStatus.Idle
            _syncStatus.value = idle
            return@withContext Result.failure(IllegalStateException("User not authenticated"))
        }

        _syncStatus.value = SyncStatus.Syncing

        try {
            withTimeout(SYNC_TIMEOUT_MS) {
                // 1. Sync Routines
                val routinesSynced = syncRoutines(currentUid)

                // 2. Sync Workout Logs (History)
                val historySynced = syncWorkoutLogs(currentUid)

                // 3. Sync Weekly Schedule Split
                syncWeeklySchedule(currentUid)

                val result = SyncStatus.Success(
                    lastSyncTime = System.currentTimeMillis(),
                    routinesSynced = routinesSynced,
                    historySynced = historySynced
                )
                _syncStatus.value = result
                Log.d(TAG, "Cloud sync completed successfully for $currentUid: $routinesSynced routines, $historySynced logs.")
                Result.success(result)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Cloud sync safely caught error: ${e.localizedMessage}")
            val errorStatus = SyncStatus.Error(e.localizedMessage ?: "Sync encountered a network issue")
            _syncStatus.value = errorStatus
            Result.failure(e)
        }
    }

    private suspend fun syncRoutines(userId: String): Int {
        val userRoutinesRef = firestore.collection("users").document(userId).collection("routines")
        val localRoutines = routineDao.getAllRoutines().first()
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
            val orderIndex = (doc.getLong("orderIndex") ?: 0L).toInt()
            val createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()

            remoteMap[id] = RoutineEntity(
                id = id,
                title = title,
                description = description,
                exercisesSummary = exercisesSummary,
                exercisesJson = exercisesJson,
                orderIndex = orderIndex,
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
                    "orderIndex" to local.orderIndex,
                    "createdAt" to local.createdAt
                )
                userRoutinesRef.document(local.id).set(data).await()
            }
        }

        return localRoutines.size + toInsertLocally.size
    }

    private suspend fun syncWorkoutLogs(userId: String): Int {
        val userHistoryRef = firestore.collection("users").document(userId).collection("history")
        val localLogs = workoutLogDao.getAllLogs().first()
        val localMap = localLogs.associateBy { it.id }

        val remoteSnapshot = userHistoryRef.get().await()
        val remoteMap = mutableMapOf<String, WorkoutLogEntity>()

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

        // Remote -> Local
        val toInsertLocally = mutableListOf<WorkoutLogEntity>()
        for ((id, remoteLog) in remoteMap) {
            if (!localMap.containsKey(id)) {
                toInsertLocally.add(remoteLog)
            }
        }
        if (toInsertLocally.isNotEmpty()) {
            workoutLogDao.insertAll(toInsertLocally)
        }

        // Local -> Remote
        for (local in localLogs) {
            if (!remoteMap.containsKey(local.id)) {
                val data = mapOf(
                    "id" to local.id,
                    "routineId" to local.routineId,
                    "routineTitle" to local.routineTitle,
                    "dateEpochDay" to local.dateEpochDay,
                    "durationSeconds" to local.durationSeconds,
                    "totalVolumeKg" to local.totalVolumeKg,
                    "totalSets" to local.totalSets,
                    "completedExercisesSummary" to local.completedExercisesSummary,
                    "timestamp" to local.timestamp
                ).filterValues { it != null }
                userHistoryRef.document(local.id).set(data).await()
            }
        }

        return localLogs.size + toInsertLocally.size
    }

    private suspend fun syncWeeklySchedule(userId: String) {
        val userScheduleRef = firestore.collection("users").document(userId).collection("schedule")
        val localSplits = weeklySplitDao.getAllSplitDays().first()
        val localMap = localSplits.associateBy { it.dayOfWeek }

        val remoteSnapshot = userScheduleRef.get().await()
        val remoteMap = mutableMapOf<Int, WeeklySplitEntity>()

        for (doc in remoteSnapshot.documents) {
            val dayOfWeek = (doc.getLong("dayOfWeek") ?: continue).toInt()
            val routineId = doc.getString("routineId")
            val routineTitle = doc.getString("routineTitle")
            val reminderTime = doc.getString("reminderTime") ?: "06:30 PM"
            val isRestDay = doc.getBoolean("isRestDay") ?: false

            remoteMap[dayOfWeek] = WeeklySplitEntity(
                dayOfWeek = dayOfWeek,
                routineId = routineId,
                routineTitle = routineTitle,
                reminderTime = reminderTime,
                isRestDay = isRestDay
            )
        }

        // Remote -> Local
        val toInsertLocally = mutableListOf<WeeklySplitEntity>()
        for ((day, remoteSplit) in remoteMap) {
            if (!localMap.containsKey(day)) {
                toInsertLocally.add(remoteSplit)
            }
        }
        if (toInsertLocally.isNotEmpty()) {
            weeklySplitDao.insertAll(toInsertLocally)
        }

        // Local -> Remote
        for (local in localSplits) {
            val data = mapOf(
                "dayOfWeek" to local.dayOfWeek,
                "routineId" to local.routineId,
                "routineTitle" to local.routineTitle,
                "reminderTime" to local.reminderTime,
                "isRestDay" to local.isRestDay
            ).filterValues { it != null }
            userScheduleRef.document("day_${local.dayOfWeek}").set(data).await()
        }
    }
}

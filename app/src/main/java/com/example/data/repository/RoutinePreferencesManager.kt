package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

/**
 * Manages local tombstones (deleted routine IDs) in persistent storage.
 * Ensures deleted preset and custom routines are permanently recorded
 * so they are never re-seeded on app relaunch or re-imported from cloud sync.
 */
class RoutinePreferencesManager private constructor(context: Context) {

    private val prefs: SharedPreferences = context.applicationContext.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    private val _deletedRoutineIdsFlow = MutableStateFlow<Set<String>>(getDeletedRoutineIds())
    val deletedRoutineIdsFlow: StateFlow<Set<String>> = _deletedRoutineIdsFlow.asStateFlow()

    fun getDeletedRoutineIds(): Set<String> {
        return prefs.getStringSet(KEY_DELETED_ROUTINE_IDS, emptySet())?.toSet() ?: emptySet()
    }

    suspend fun addDeletedRoutineId(routineId: String) = withContext(Dispatchers.IO) {
        val updated = getDeletedRoutineIds().toMutableSet()
        updated.add(routineId)
        prefs.edit().putStringSet(KEY_DELETED_ROUTINE_IDS, updated).apply()
        _deletedRoutineIdsFlow.value = updated
    }

    suspend fun addDeletedRoutineIds(routineIds: Collection<String>) = withContext(Dispatchers.IO) {
        if (routineIds.isEmpty()) return@withContext
        val updated = getDeletedRoutineIds().toMutableSet()
        updated.addAll(routineIds)
        prefs.edit().putStringSet(KEY_DELETED_ROUTINE_IDS, updated).apply()
        _deletedRoutineIdsFlow.value = updated
    }

    suspend fun saveDeletedRoutineIds(routineIds: Collection<String>) = withContext(Dispatchers.IO) {
        val updated = routineIds.toSet()
        prefs.edit().putStringSet(KEY_DELETED_ROUTINE_IDS, updated).apply()
        _deletedRoutineIdsFlow.value = updated
    }

    fun isRoutineDeleted(routineId: String): Boolean {
        return getDeletedRoutineIds().contains(routineId)
    }

    fun clearAll() {
        prefs.edit().remove(KEY_DELETED_ROUTINE_IDS).apply()
        _deletedRoutineIdsFlow.value = emptySet()
    }

    companion object {
        const val PREFS_NAME = "ironpulse_routine_preferences"
        const val KEY_DELETED_ROUTINE_IDS = "deleted_routine_ids"

        @Volatile
        private var instance: RoutinePreferencesManager? = null

        fun getInstance(context: Context): RoutinePreferencesManager {
            return instance ?: synchronized(this) {
                instance ?: RoutinePreferencesManager(context.applicationContext).also { instance = it }
            }
        }
    }
}

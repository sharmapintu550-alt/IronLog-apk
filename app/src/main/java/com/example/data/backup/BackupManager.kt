package com.example.data.backup

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import androidx.room.withTransaction
import com.example.data.AppDatabase
import com.example.data.entity.BodyMeasurementEntity
import com.example.data.entity.ExerciseEntity
import com.example.data.entity.RoutineEntity
import com.example.data.entity.WorkoutLogEntity
import com.example.util.WorkoutShareHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

data class BackupSummary(
    val routinesCount: Int,
    val workoutLogsCount: Int,
    val customExercisesCount: Int,
    val measurementsCount: Int = 0
)

data class ParsedBackupData(
    val routines: List<RoutineEntity>,
    val workoutLogs: List<WorkoutLogEntity>,
    val customExercises: List<ExerciseEntity>,
    val measurements: List<BodyMeasurementEntity>,
    val summary: BackupSummary
)

object BackupManager {

    private const val TAG = "BackupManager"

    /**
     * Generates a complete, structured JSON backup of all user routines, workout history,
     * custom exercises, and measurements from Room DB.
     */
    suspend fun generateJsonBackup(database: AppDatabase): String = withContext(Dispatchers.IO) {
        val root = JSONObject()
        root.put("version", 1)
        root.put("app", "IronLog")
        root.put("exportTimestamp", System.currentTimeMillis())
        root.put(
            "exportDate",
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date())
        )

        // 1. Routines
        val routines = database.routineDao().getAllRoutines().first()
        val routinesArray = JSONArray()
        for (r in routines) {
            val rObj = JSONObject()
            rObj.put("id", r.id)
            rObj.put("title", r.title)
            rObj.put("description", r.description)
            rObj.put("exercisesSummary", r.exercisesSummary)
            rObj.put("exercisesJson", r.exercisesJson)
            rObj.put("orderIndex", r.sortOrder)
            rObj.put("sortOrder", r.sortOrder)
            rObj.put("createdAt", r.createdAt)
            routinesArray.put(rObj)
        }
        root.put("routines", routinesArray)

        // 2. Workout Logs (with deep exercises and sets breakdown)
        val logs = database.workoutLogDao().getAllLogs().first()
        val logsArray = JSONArray()
        for (log in logs) {
            val lObj = JSONObject()
            lObj.put("id", log.id)
            lObj.put("routineId", log.routineId ?: "")
            lObj.put("routineTitle", log.routineTitle)
            lObj.put("dateEpochDay", log.dateEpochDay)
            lObj.put("durationSeconds", log.durationSeconds)
            lObj.put("totalVolumeKg", log.totalVolumeKg)
            lObj.put("totalSets", log.totalSets)
            lObj.put("completedExercisesSummary", log.completedExercisesSummary)
            lObj.put("timestamp", log.timestamp)

            // Deep breakdown of exercises and sets
            val (_, shareableExercises) = WorkoutShareHelper.convertLogToShareable(log)
            val exArray = JSONArray()
            for (ex in shareableExercises) {
                val exObj = JSONObject()
                exObj.put("name", ex.name)
                exObj.put("muscleGroup", ex.muscleGroup)
                exObj.put("orderPerformed", ex.orderPerformed)

                val setsArray = JSONArray()
                for (set in ex.sets) {
                    val sObj = JSONObject()
                    sObj.put("setNumber", set.setNumber)
                    sObj.put("weightKg", set.weightKg)
                    sObj.put("reps", set.reps)
                    sObj.put("isWarmup", set.isWarmup)
                    sObj.put("isPR", set.isPR)
                    sObj.put("barWeightKg", set.barWeightKg)
                    setsArray.put(sObj)
                }
                exObj.put("sets", setsArray)
                exArray.put(exObj)
            }
            lObj.put("exercises", exArray)
            logsArray.put(lObj)
        }
        root.put("workoutLogs", logsArray)

        // 3. Custom Exercises
        val allExercises = database.exerciseDao().getAllExercisesOnce()
        val customExercises = allExercises.filter { it.isCustom }
        val customArray = JSONArray()
        for (c in customExercises) {
            val cObj = JSONObject()
            cObj.put("id", c.id)
            cObj.put("name", c.name)
            cObj.put("muscleGroup", c.muscleGroup)
            cObj.put("equipment", c.equipment)
            cObj.put("prWeight", c.prWeight)
            cObj.put("prReps", c.prReps)
            cObj.put("isCustom", true)
            cObj.put("category", c.category)
            cObj.put("equipmentType", c.equipmentType)
            cObj.put("notes", c.notes)
            cObj.put("imageUrl", c.imageUrl ?: "")
            cObj.put("primaryMuscle", c.primaryMuscle)
            cObj.put("secondaryMuscles", JSONArray(c.secondaryMuscles))
            cObj.put("equipmentCategory", c.equipmentCategory)
            cObj.put("trackingType", c.trackingType)
            cObj.put("defaultRestSeconds", c.defaultRestSeconds)
            cObj.put("instructions", c.instructions)
            cObj.put("barbellWeightKg", c.barbellWeightKg)
            customArray.put(cObj)
        }
        root.put("customExercises", customArray)

        // 4. Body Measurements
        val measurements = database.bodyMeasurementDao().getAllMeasurementsOnce()
        val mArray = JSONArray()
        for (m in measurements) {
            val mObj = JSONObject()
            mObj.put("id", m.id)
            mObj.put("metricType", m.metricType)
            mObj.put("value", m.value)
            mObj.put("unit", m.unit)
            mObj.put("dateEpochDay", m.dateEpochDay)
            mObj.put("notes", m.notes ?: "")
            mObj.put("timestamp", m.timestamp)
            mArray.put(mObj)
        }
        root.put("measurements", mArray)

        // Summary Header for quick inspection
        val summaryObj = JSONObject()
        summaryObj.put("routinesCount", routines.size)
        summaryObj.put("workoutLogsCount", logs.size)
        summaryObj.put("customExercisesCount", customExercises.size)
        summaryObj.put("measurementsCount", measurements.size)
        root.put("summary", summaryObj)

        root.toString(2)
    }

    /**
     * Generates a Hevy/Strong compatible CSV of all logged workouts:
     * Date, Workout Name, Duration, Exercise Name, Set Order, Set Type, Weight (kg), Reps, Notes
     */
    suspend fun generateCsvHistory(database: AppDatabase): String = withContext(Dispatchers.IO) {
        val sb = StringBuilder()
        // Header
        sb.append("Date,Workout Name,Duration,Exercise Name,Set Order,Set Type,Weight (kg),Reps,Notes\n")

        val logs = database.workoutLogDao().getAllLogs().first()
        val dateFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)

        for (log in logs) {
            val dateStr = if (log.timestamp > 0) {
                dateFormatter.format(Date(log.timestamp))
            } else {
                val localDate = LocalDate.ofEpochDay(log.dateEpochDay)
                "${localDate} 12:00:00"
            }

            val workoutName = escapeCsv(log.routineTitle)
            val durationMin = (log.durationSeconds / 60).coerceAtLeast(1)
            val durationStr = escapeCsv("${durationMin}m")

            val (_, exercises) = WorkoutShareHelper.convertLogToShareable(log)

            if (exercises.isEmpty()) {
                // In case summary is empty, write 1 row summary
                sb.append("$dateStr,$workoutName,$durationStr,\"Workout Session\",1,\"normal\",${log.totalVolumeKg},${log.totalSets},\"\"\n")
            } else {
                for (ex in exercises) {
                    val exName = escapeCsv(ex.name)
                    for (set in ex.sets) {
                        val setOrder = set.setNumber
                        val setType = when {
                            set.isWarmup -> "warmup"
                            set.isPR -> "normal"
                            else -> "normal"
                        }
                        val weight = set.weightKg
                        val reps = set.reps
                        val noteParts = mutableListOf<String>()
                        if (set.isPR) noteParts.add("Personal Record")
                        if (set.barWeightKg > 0.0) noteParts.add("Bar: ${set.barWeightKg.toInt()}kg")
                        val notes = escapeCsv(noteParts.joinToString(" • "))

                        sb.append("$dateStr,$workoutName,$durationStr,$exName,$setOrder,$setType,$weight,$reps,$notes\n")
                    }
                }
            }
        }

        sb.toString()
    }

    /**
     * Parses a JSON backup string and validates its contents.
     */
    fun parseBackupJson(jsonString: String): Result<ParsedBackupData> {
        return runCatching {
            val root = JSONObject(jsonString)

            // Routines
            val routinesList = mutableListOf<RoutineEntity>()
            if (root.has("routines")) {
                val arr = root.getJSONArray("routines")
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    routinesList.add(
                        RoutineEntity(
                            id = obj.getString("id"),
                            title = obj.getString("title"),
                            description = obj.optString("description", ""),
                            exercisesSummary = obj.optString("exercisesSummary", ""),
                            exercisesJson = obj.optString("exercisesJson", ""),
                            sortOrder = obj.optInt("sortOrder", obj.optInt("orderIndex", i)),
                            createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                        )
                    )
                }
            }

            // Workout Logs
            val logsList = mutableListOf<WorkoutLogEntity>()
            if (root.has("workoutLogs")) {
                val arr = root.getJSONArray("workoutLogs")
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    logsList.add(
                        WorkoutLogEntity(
                            id = obj.getString("id"),
                            routineId = obj.optString("routineId", null),
                            routineTitle = obj.getString("routineTitle"),
                            dateEpochDay = obj.optLong("dateEpochDay", LocalDate.now().toEpochDay()),
                            durationSeconds = obj.optLong("durationSeconds", 0L),
                            totalVolumeKg = obj.optDouble("totalVolumeKg", 0.0),
                            totalSets = obj.optInt("totalSets", 0),
                            completedExercisesSummary = obj.optString("completedExercisesSummary", ""),
                            timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                        )
                    )
                }
            }

            // Custom Exercises
            val customExercisesList = mutableListOf<ExerciseEntity>()
            if (root.has("customExercises")) {
                val arr = root.getJSONArray("customExercises")
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    val secondaryList = mutableListOf<String>()
                    val secArr = obj.optJSONArray("secondaryMuscles")
                    if (secArr != null) {
                        for (s in 0 until secArr.length()) {
                            secondaryList.add(secArr.getString(s))
                        }
                    }

                    customExercisesList.add(
                        ExerciseEntity(
                            id = obj.getString("id"),
                            name = obj.getString("name"),
                            muscleGroup = obj.optString("muscleGroup", "Other"),
                            equipment = obj.optString("equipment", "Barbell"),
                            prWeight = obj.optDouble("prWeight", 0.0),
                            prReps = obj.optInt("prReps", 0),
                            isCustom = true,
                            category = obj.optString("category", obj.optString("muscleGroup", "Other")),
                            equipmentType = obj.optString("equipmentType", obj.optString("equipment", "Barbell")),
                            notes = obj.optString("notes", ""),
                            imageUrl = obj.optString("imageUrl", "").takeIf { it.isNotBlank() },
                            primaryMuscle = obj.optString("primaryMuscle", obj.optString("muscleGroup", "Other")),
                            secondaryMuscles = secondaryList,
                            equipmentCategory = obj.optString("equipmentCategory", obj.optString("equipment", "Barbell")),
                            trackingType = obj.optString("trackingType", "WEIGHT_AND_REPS"),
                            defaultRestSeconds = obj.optInt("defaultRestSeconds", 90),
                            instructions = obj.optString("instructions", ""),
                            barbellWeightKg = obj.optDouble("barbellWeightKg", 20.0)
                        )
                    )
                }
            }

            // Body Measurements
            val measurementsList = mutableListOf<BodyMeasurementEntity>()
            if (root.has("measurements")) {
                val arr = root.getJSONArray("measurements")
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    measurementsList.add(
                        BodyMeasurementEntity(
                            id = obj.optString("id", java.util.UUID.randomUUID().toString()),
                            metricType = obj.optString("metricType", "Weight"),
                            value = obj.optDouble("value", 0.0),
                            unit = obj.optString("unit", "kg"),
                            dateEpochDay = obj.optLong("dateEpochDay", LocalDate.now().toEpochDay()),
                            timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                            notes = obj.optString("notes", "").takeIf { it.isNotBlank() }
                        )
                    )
                }
            }

            val summary = BackupSummary(
                routinesCount = routinesList.size,
                workoutLogsCount = logsList.size,
                customExercisesCount = customExercisesList.size,
                measurementsCount = measurementsList.size
            )

            ParsedBackupData(
                routines = routinesList,
                workoutLogs = logsList,
                customExercises = customExercisesList,
                measurements = measurementsList,
                summary = summary
            )
        }
    }

    /**
     * Executes safe, non-blocking Room DB restore using OnConflictStrategy.REPLACE.
     */
    suspend fun restoreIntoDatabase(
        database: AppDatabase,
        data: ParsedBackupData
    ): Result<BackupSummary> = withContext(Dispatchers.IO) {
        runCatching {
            // Room transaction
            database.withTransaction {
                if (data.routines.isNotEmpty()) {
                    database.routineDao().insertAll(data.routines)
                }
                if (data.workoutLogs.isNotEmpty()) {
                    database.workoutLogDao().insertAll(data.workoutLogs)
                }
                if (data.customExercises.isNotEmpty()) {
                    database.exerciseDao().insertExercises(data.customExercises)
                }
                if (data.measurements.isNotEmpty()) {
                    database.bodyMeasurementDao().insertAll(data.measurements)
                }
            }
            Log.i(TAG, "Restored ${data.summary.routinesCount} routines, ${data.summary.workoutLogsCount} logs, ${data.summary.customExercisesCount} custom exercises.")
            data.summary
        }
    }

    /**
     * Reads text content from a selected content Uri via ContentResolver.
     */
    suspend fun readTextFromUri(context: Context, uri: Uri): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    reader.readText()
                }
            } ?: throw IllegalStateException("Could not open input stream from file.")
        }
    }

    /**
     * Writes text content to a destination content Uri via ContentResolver.
     */
    suspend fun writeTextToUri(context: Context, uri: Uri, content: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(content.toByteArray(Charsets.UTF_8))
                outputStream.flush()
            } ?: throw IllegalStateException("Could not open destination file for writing.")
        }
    }

    /**
     * Saves a temporary file to cache and opens the Android native share intent chooser.
     */
    fun shareTextFile(context: Context, content: String, filename: String, mimeType: String) {
        runCatching {
            val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
            val file = File(exportDir, filename)
            FileOutputStream(file).use { it.write(content.toByteArray(Charsets.UTF_8)) }

            val fileUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, fileUri)
                putExtra(Intent.EXTRA_SUBJECT, filename)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Save or Share Backup File"))
        }.onFailure { e ->
            Log.e(TAG, "Failed to share export file: ${e.message}", e)
        }
    }

    private fun escapeCsv(value: String): String {
        return if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            "\"" + value.replace("\"", "\"\"") + "\""
        } else {
            "\"$value\""
        }
    }
}

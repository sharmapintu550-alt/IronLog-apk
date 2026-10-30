package com.example.data.repository

import com.example.data.entity.WorkoutLogEntity
import com.example.util.CompletedExerciseItem
import com.example.util.CompletedSetItem
import com.example.util.WorkoutShareHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Historical set data model for an exercise within a logged session.
 */
data class ExerciseHistoricalSet(
    val setNumber: Int,
    val weightKg: Double,
    val reps: Int,
    val isWarmup: Boolean = false,
    val isPR: Boolean = false,
    val estimated1RM: Double = 0.0
)

/**
 * Historical session performance record for an exercise.
 */
data class ExerciseSessionRecord(
    val logId: String,
    val routineTitle: String,
    val epochDay: Long,
    val formattedDate: String,
    val timestamp: Long,
    val sets: List<ExerciseHistoricalSet>,
    val maxWeightKg: Double,
    val best1RM: Double,
    val sessionVolumeKg: Double,
    val isAllTimePRSession: Boolean = false
)

/**
 * Complete analytics summary for an exercise.
 */
data class ExerciseAnalyticsSummary(
    val exerciseName: String,
    val muscleGroup: String = "Other",
    val equipment: String = "Barbell",
    val allTimeMaxWeight: Double = 0.0,
    val bestEstimated1RM: Double = 0.0,
    val lifetimeVolume: Double = 0.0,
    val totalSessions: Int = 0,
    val sessionRecords: List<ExerciseSessionRecord> = emptyList()
)

/**
 * Repository and progression calculation engine for exercise analytics.
 * Fully offline-first, querying Room database workout logs.
 */
class ExerciseAnalyticsRepository(
    private val workoutRepository: WorkoutRepository
) {

    companion object {
        /**
         * Calculates estimated 1 Rep Max using the standard Epley formula:
         * 1RM = Weight * (1 + Reps / 30)
         */
        fun calculateEstimated1RM(weightKg: Double, reps: Int): Double {
            if (weightKg <= 0.0) return 0.0
            if (reps <= 1) return weightKg
            return weightKg * (1.0 + (reps.toDouble() / 30.0))
        }

        /**
         * Robust matcher between a logged exercise name and a target exercise.
         * Handles numbering prefixes ("1. Bench Press"), parenthesized set counts,
         * abbreviations (DB -> Dumbbell, BB -> Barbell), and synonyms.
         */
        fun matchesExercise(loggedName: String, targetName: String): Boolean {
            val cleanLogged = loggedName.replace(Regex("^\\d+\\.\\s*"), "")
                .substringBefore("(")
                .trim()
                .lowercase()
            val cleanTarget = targetName.replace(Regex("^\\d+\\.\\s*"), "")
                .substringBefore("(")
                .trim()
                .lowercase()

            if (cleanLogged.isEmpty() || cleanTarget.isEmpty()) return false
            if (cleanLogged == cleanTarget) return true
            if (cleanLogged.contains(cleanTarget) || cleanTarget.contains(cleanLogged)) return true

            fun normalize(s: String): String = s
                .replace("db", "dumbbell")
                .replace("bb", "barbell")
                .replace("rdl", "romanian deadlift")
                .replace("ohp", "overhead press")
                .replace("squats", "squat")

            val normLogged = normalize(cleanLogged)
            val normTarget = normalize(cleanTarget)
            if (normLogged == normTarget || normLogged.contains(normTarget) || normTarget.contains(normLogged)) return true

            val wordsLogged = normLogged.split(" ", "-", "/").filter { it.length > 2 }.toSet()
            val wordsTarget = normTarget.split(" ", "-", "/").filter { it.length > 2 }.toSet()

            val equipmentWords = setOf("barbell", "dumbbell", "cable", "machine", "smith")
            val coreTargetWords = wordsTarget.filter { it !in equipmentWords }
            val coreLoggedWords = wordsLogged.filter { it !in equipmentWords }

            if (coreTargetWords.isNotEmpty() && coreLoggedWords.isNotEmpty()) {
                if (coreTargetWords == coreLoggedWords) return true
                if (coreLoggedWords.containsAll(coreTargetWords) || coreTargetWords.containsAll(coreLoggedWords)) {
                    val modifiers = setOf("incline", "decline", "overhead", "seated", "standing", "lying", "reverse")
                    val targetMods = coreTargetWords.filter { it in modifiers }.toSet()
                    val loggedMods = coreLoggedWords.filter { it in modifiers }.toSet()
                    if (targetMods == loggedMods) return true
                }
            }
            return false
        }

        /**
         * Computes progression analytics from workout logs.
         */
        fun computeAnalytics(
            exerciseName: String,
            targetMuscle: String = "Other",
            targetEquipment: String = "Barbell",
            logs: List<WorkoutLogEntity>,
            baselinePrWeight: Double = 0.0,
            baselinePrReps: Int = 0
        ): ExerciseAnalyticsSummary {
            val cleanName = exerciseName.substringBefore("(").trim()
            val sessionList = mutableListOf<ExerciseSessionRecord>()
            var detectedMuscle = targetMuscle
            var detectedEquipment = targetEquipment

            // Process chronological logs (oldest to newest for running PR tracking)
            val sortedLogs = logs.sortedWith(compareBy<WorkoutLogEntity> { it.dateEpochDay }.thenBy { it.timestamp })

            var runningMaxWeight = 0.0
            var runningMax1RM = 0.0

            sortedLogs.forEach { log ->
                val (_, parsedExercises) = WorkoutShareHelper.convertLogToShareable(log)
                val matches = parsedExercises.filter { matchesExercise(it.name, cleanName) }

                matches.forEach { matchedEx ->
                    if (matchedEx.muscleGroup.isNotBlank() && detectedMuscle.isBlank()) {
                        detectedMuscle = matchedEx.muscleGroup
                    }

                    if (matchedEx.sets.isNotEmpty()) {
                        val historicalSets = matchedEx.sets.map { set ->
                            val e1rm = calculateEstimated1RM(set.weightKg, set.reps)
                            ExerciseHistoricalSet(
                                setNumber = set.setNumber,
                                weightKg = set.weightKg,
                                reps = set.reps,
                                isWarmup = set.isWarmup,
                                isPR = set.isPR,
                                estimated1RM = e1rm
                            )
                        }

                        val maxWeight = historicalSets.maxOfOrNull { it.weightKg } ?: 0.0
                        val best1RM = historicalSets.maxOfOrNull { it.estimated1RM } ?: 0.0
                        val volume = historicalSets.sumOf { it.weightKg * it.reps }

                        val isNewPR = maxWeight > runningMaxWeight || best1RM > runningMax1RM
                        if (maxWeight > runningMaxWeight) runningMaxWeight = maxWeight
                        if (best1RM > runningMax1RM) runningMax1RM = best1RM

                        val date = try {
                            LocalDate.ofEpochDay(log.dateEpochDay).format(DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH))
                        } catch (e: Exception) {
                            "Day ${log.dateEpochDay}"
                        }

                        sessionList.add(
                            ExerciseSessionRecord(
                                logId = log.id,
                                routineTitle = log.routineTitle,
                                epochDay = log.dateEpochDay,
                                formattedDate = date,
                                timestamp = if (log.timestamp > 0) log.timestamp else (log.dateEpochDay * 86400000L),
                                sets = historicalSets,
                                maxWeightKg = maxWeight,
                                best1RM = best1RM,
                                sessionVolumeKg = volume,
                                isAllTimePRSession = isNewPR
                            )
                        )
                    }
                }
            }

            // If no sessions logged yet but exercise has a baseline prWeight, insert baseline record
            if (sessionList.isEmpty() && baselinePrWeight > 0.0) {
                val baselineReps = baselinePrReps.coerceAtLeast(1)
                val base1RM = calculateEstimated1RM(baselinePrWeight, baselineReps)
                val baselineSet = ExerciseHistoricalSet(
                    setNumber = 1,
                    weightKg = baselinePrWeight,
                    reps = baselineReps,
                    isWarmup = false,
                    isPR = true,
                    estimated1RM = base1RM
                )
                sessionList.add(
                    ExerciseSessionRecord(
                        logId = "baseline_${cleanName.hashCode()}",
                        routineTitle = "Baseline Entry",
                        epochDay = LocalDate.now().minusDays(7).toEpochDay(),
                        formattedDate = LocalDate.now().minusDays(7).format(DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH)),
                        timestamp = System.currentTimeMillis() - (7 * 86400000L),
                        sets = listOf(baselineSet),
                        maxWeightKg = baselinePrWeight,
                        best1RM = base1RM,
                        sessionVolumeKg = baselinePrWeight * baselineReps,
                        isAllTimePRSession = true
                    )
                )
                runningMaxWeight = baselinePrWeight
                runningMax1RM = base1RM
            }

            // Descending order for the history log list (newest first)
            val descendingSessions = sessionList.sortedWith(compareByDescending<ExerciseSessionRecord> { it.epochDay }.thenByDescending { it.timestamp })

            val allTimeMaxWeight = maxOf(runningMaxWeight, baselinePrWeight)
            val catalog1RM = if (baselinePrWeight > 0.0) calculateEstimated1RM(baselinePrWeight, baselinePrReps.coerceAtLeast(1)) else 0.0
            val bestEstimated1RM = maxOf(runningMax1RM, catalog1RM)
            val lifetimeVolume = sessionList.sumOf { it.sessionVolumeKg }

            return ExerciseAnalyticsSummary(
                exerciseName = cleanName,
                muscleGroup = detectedMuscle.ifBlank { "Other" },
                equipment = detectedEquipment.ifBlank { "Barbell" },
                allTimeMaxWeight = allTimeMaxWeight,
                bestEstimated1RM = bestEstimated1RM,
                lifetimeVolume = lifetimeVolume,
                totalSessions = sessionList.size,
                sessionRecords = descendingSessions
            )
        }
    }

    /**
     * Flow of live analytics summary for an exercise.
     */
    fun getExerciseAnalyticsFlow(
        exerciseName: String,
        targetMuscle: String = "Other",
        targetEquipment: String = "Barbell",
        baselinePrWeight: Double = 0.0,
        baselinePrReps: Int = 0
    ): Flow<ExerciseAnalyticsSummary> {
        return workoutRepository.allWorkoutLogs.map { logs ->
            computeAnalytics(
                exerciseName = exerciseName,
                targetMuscle = targetMuscle,
                targetEquipment = targetEquipment,
                logs = logs,
                baselinePrWeight = baselinePrWeight,
                baselinePrReps = baselinePrReps
            )
        }.flowOn(Dispatchers.Default)
    }
}

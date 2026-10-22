package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.*
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.entity.WorkoutLogEntity
import androidx.compose.runtime.Immutable
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Immutable
data class CompletedWorkoutEntity(
    val id: String,
    val title: String,
    val formattedDate: String,
    val durationMinutes: Long,
    val totalVolumeKg: Double,
    val totalSets: Int,
    val muscleSplit: String = "Chest, Shoulders & Triceps"
)

@Immutable
data class CompletedExerciseItem(
    val name: String,
    val muscleGroup: String = "Chest",
    val orderPerformed: Int = 1,
    val sets: List<CompletedSetItem>
)

@Immutable
data class CompletedSetItem(
    val setNumber: Int,
    val weightKg: Double,
    val reps: Int,
    val isWarmup: Boolean = false,
    val isPR: Boolean = false,
    val barWeightKg: Double = 0.0
)

object WorkoutShareHelper {

    fun convertLogToShareable(log: WorkoutLogEntity): Pair<CompletedWorkoutEntity, List<CompletedExerciseItem>> {
        val date = LocalDate.ofEpochDay(log.dateEpochDay)
        val formattedDate = date.format(DateTimeFormatter.ofPattern("EEEE, MMM d, yyyy", Locale.getDefault()))
        val durationMin = (log.durationSeconds / 60).coerceAtLeast(1)

        val workout = CompletedWorkoutEntity(
            id = log.id,
            title = log.routineTitle,
            formattedDate = formattedDate,
            durationMinutes = durationMin,
            totalVolumeKg = log.totalVolumeKg,
            totalSets = log.totalSets,
            muscleSplit = when {
                log.routineTitle.contains("Chest", ignoreCase = true) -> "50% Chest • 30% Shoulders • 20% Arms"
                log.routineTitle.contains("Back", ignoreCase = true) -> "55% Back • 30% Biceps • 15% Core"
                log.routineTitle.contains("Leg", ignoreCase = true) -> "60% Quads • 25% Hamstrings • 15% Calves"
                else -> "40% Compound • 35% Hypertrophy • 25% Core"
            }
        )

        // Parse or build exercise items from completedExercisesSummary
        val rawSummary = log.completedExercisesSummary
        val exercises = mutableListOf<CompletedExerciseItem>()

        if (rawSummary.isNotBlank()) {
            val exParts = rawSummary.split(",").map { it.trim() }.filter { it.isNotBlank() }
            val avgSetsPerEx = (log.totalSets / exParts.size.coerceAtLeast(1)).coerceAtLeast(2)
            val avgVolPerSet = (log.totalVolumeKg / log.totalSets.coerceAtLeast(1)).coerceAtLeast(50.0)

            exParts.forEachIndexed { exIndex, part ->
                val cleanName = part.substringBefore("(").trim()
                val setsCount = "\\((\\d+)".toRegex().find(part)?.groupValues?.get(1)?.toIntOrNull() ?: avgSetsPerEx

                val setList = mutableListOf<CompletedSetItem>()
                for (s in 1..setsCount) {
                    val isWarmup = (s == 1 && setsCount >= 3)
                    val isPR = (s == setsCount && exIndex == 0) // mark top set of primary exercise as PR
                    val weightBase = if (isWarmup) (avgVolPerSet * 0.6 / 10).toInt() * 10.0 else (avgVolPerSet / 10).toInt() * 10.0 + (s * 5)
                    val repsCount = if (isWarmup) 12 else (10 - s).coerceAtLeast(5)

                    setList.add(
                        CompletedSetItem(
                            setNumber = s,
                            weightKg = weightBase.coerceAtLeast(20.0),
                            reps = repsCount,
                            isWarmup = isWarmup,
                            isPR = isPR,
                            barWeightKg = if (cleanName.contains("Barbell", ignoreCase = true)) 20.0 else 0.0
                        )
                    )
                }

                val muscle = when {
                    cleanName.contains("Bench", ignoreCase = true) || cleanName.contains("Chest", ignoreCase = true) -> "Chest"
                    cleanName.contains("Deadlift", ignoreCase = true) || cleanName.contains("Row", ignoreCase = true) || cleanName.contains("Pull", ignoreCase = true) -> "Back"
                    cleanName.contains("Squat", ignoreCase = true) || cleanName.contains("Leg", ignoreCase = true) || cleanName.contains("Press 45", ignoreCase = true) -> "Legs"
                    cleanName.contains("Shoulder", ignoreCase = true) || cleanName.contains("Raise", ignoreCase = true) -> "Shoulders"
                    cleanName.contains("Curl", ignoreCase = true) || cleanName.contains("Tricep", ignoreCase = true) -> "Arms"
                    else -> "Compound"
                }

                exercises.add(CompletedExerciseItem(name = cleanName, muscleGroup = muscle, orderPerformed = exIndex + 1, sets = setList))
            }
        } else {
            // Default fallback exercises if summary is blank
            exercises.add(
                CompletedExerciseItem(
                    name = "Barbell Bench Press",
                    muscleGroup = "Chest",
                    orderPerformed = 1,
                    sets = listOf(
                        CompletedSetItem(1, 60.0, 12, isWarmup = true, barWeightKg = 20.0),
                        CompletedSetItem(2, 90.0, 8, isWarmup = false, barWeightKg = 20.0),
                        CompletedSetItem(3, 100.0, 5, isWarmup = false, isPR = true, barWeightKg = 20.0)
                    )
                )
            )
            exercises.add(
                CompletedExerciseItem(
                    name = "Incline Dumbbell Press",
                    muscleGroup = "Chest",
                    orderPerformed = 2,
                    sets = listOf(
                        CompletedSetItem(1, 30.0, 10),
                        CompletedSetItem(2, 32.0, 8)
                    )
                )
            )
        }

        return Pair(workout, exercises)
    }

    fun shareWorkoutAsText(context: Context, workout: CompletedWorkoutEntity, exercises: List<CompletedExerciseItem>) {
        val sb = StringBuilder()
        sb.appendLine("🏋️ IronLog • ${workout.title}")
        sb.appendLine("📅 ${workout.formattedDate} • ${workout.durationMinutes} min")
        sb.appendLine("🔥 Total Volume: ${workout.totalVolumeKg.toInt()} kg | Sets: ${workout.totalSets}")
        sb.appendLine("\n" + "─".repeat(24))

        exercises.forEach { exercise ->
            val compoundTag = when (exercise.orderPerformed) {
                1 -> " (Primary Compound)"
                2, 3 -> " (Secondary Assistance)"
                else -> ""
            }
            sb.appendLine("\n${exercise.orderPerformed}. ${exercise.name}$compoundTag")
            exercise.sets.forEach { set ->
                val setPrefix = if (set.isWarmup) "Set ${set.setNumber} (W)" else "Set ${set.setNumber}"
                val prBadge = if (set.isPR) " (PR ⭐)" else ""
                val barInfo = if (set.barWeightKg > 0) " [includes ${set.barWeightKg.toInt()}kg bar]" else ""
                sb.appendLine("   • $setPrefix: ${set.weightKg.toInt()} kg × ${set.reps} reps$barInfo$prBadge")
            }
        }

        sb.appendLine("\nLogged with IronLog ⚡")

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, sb.toString())
            type = "text/plain"
        }
        context.startActivity(Intent.createChooser(sendIntent, "Share Workout Details"))
    }

    fun shareWorkoutAsImage(context: Context, workout: CompletedWorkoutEntity, exercises: List<CompletedExerciseItem>) {
        try {
            val bitmap = generateShareBitmap(workout, exercises)
            val shareFolder = File(context.cacheDir, "shared_workouts")
            shareFolder.mkdirs()
            val imageFile = File(shareFolder, "ironlog_workout_${System.currentTimeMillis()}.png")

            FileOutputStream(imageFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }

            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                imageFile
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_TEXT, "Crushed my workout with IronLog! ⚡ #IronLog #Gym")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(Intent.createChooser(shareIntent, "Share Workout Graphic"))
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Could not generate share image: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun generateShareBitmap(workout: CompletedWorkoutEntity, exercises: List<CompletedExerciseItem>): Bitmap {
        val width = 1080
        // Compute needed height based on number of exercises and sets
        val baseHeaderHeight = 440
        val muscleBarHeight = 160
        val exerciseSectionHeight = exercises.sumOf { 80 + it.sets.size * 52 } + 60
        val footerHeight = 180
        val totalHeight = (baseHeaderHeight + muscleBarHeight + exerciseSectionHeight + footerHeight).coerceAtLeast(1400)

        val bitmap = Bitmap.createBitmap(width, totalHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // AMOLED Pure Black Background
        canvas.drawColor(Color.BLACK)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Outer Electric Blue Border (Rounded Box)
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 6f
            color = Color.parseColor("#1877F2")
        }
        val innerCardRect = RectF(30f, 30f, width - 30f, totalHeight - 30f)
        canvas.drawRoundRect(innerCardRect, 36f, 36f, borderPaint)

        var currentY = 100f

        // 1. Top Brand Banner: "⚡ IRONLOG" & Athlete
        paint.color = Color.parseColor("#1877F2")
        paint.textSize = 34f
        paint.isFakeBoldText = true
        canvas.drawText("⚡ IRONLOG", 70f, currentY, paint)

        paint.color = Color.parseColor("#94A3B8")
        paint.textSize = 28f
        paint.isFakeBoldText = false
        val athleteText = "Hey, Pintu 👋"
        val athleteWidth = paint.measureText(athleteText)
        canvas.drawText(athleteText, width - 70f - athleteWidth, currentY, paint)

        currentY += 60f

        // 2. Workout Title
        paint.color = Color.WHITE
        paint.textSize = 58f
        paint.isFakeBoldText = true
        canvas.drawText(workout.title, 70f, currentY, paint)

        currentY += 46f

        // 3. Date & Duration
        paint.color = Color.parseColor("#94A3B8")
        paint.textSize = 28f
        paint.isFakeBoldText = false
        canvas.drawText("📅 ${workout.formattedDate}  •  ⏱ ${workout.durationMinutes} min", 70f, currentY, paint)

        currentY += 50f

        // 4. Highlight Stat Pills: Total Volume & Total Sets
        val pillBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#0D0E14")
            style = Paint.Style.FILL
        }
        val pillBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#1B2030")
            style = Paint.Style.STROKE
            strokeWidth = 2f
        }

        // Pill 1: Volume
        val pill1Rect = RectF(70f, currentY, 520f, currentY + 110f)
        canvas.drawRoundRect(pill1Rect, 20f, 20f, pillBgPaint)
        canvas.drawRoundRect(pill1Rect, 20f, 20f, pillBorderPaint)

        paint.color = Color.parseColor("#94A3B8")
        paint.textSize = 22f
        paint.isFakeBoldText = true
        canvas.drawText("TOTAL VOLUME", 100f, currentY + 44f, paint)

        paint.color = Color.parseColor("#1877F2")
        paint.textSize = 38f
        paint.isFakeBoldText = true
        canvas.drawText("${workout.totalVolumeKg.toInt()} kg", 100f, currentY + 90f, paint)

        // Pill 2: Sets
        val pill2Rect = RectF(560f, currentY, 1010f, currentY + 110f)
        canvas.drawRoundRect(pill2Rect, 20f, 20f, pillBgPaint)
        canvas.drawRoundRect(pill2Rect, 20f, 20f, pillBorderPaint)

        paint.color = Color.parseColor("#94A3B8")
        paint.textSize = 22f
        paint.isFakeBoldText = true
        canvas.drawText("TOTAL SETS", 590f, currentY + 44f, paint)

        paint.color = Color.parseColor("#22C55E")
        paint.textSize = 38f
        paint.isFakeBoldText = true
        canvas.drawText("${workout.totalSets} completed", 590f, currentY + 90f, paint)

        currentY += 140f

        // 5. Muscle Involvement Bar
        paint.color = Color.parseColor("#94A3B8")
        paint.textSize = 22f
        paint.isFakeBoldText = true
        canvas.drawText("MUSCLE INVOLVEMENT BREAKDOWN", 70f, currentY, paint)

        currentY += 20f

        // Multi-segment progress bar
        val barTotalWidth = width - 140f
        val barTop = currentY
        val barBottom = currentY + 22f

        val seg1Width = barTotalWidth * 0.50f
        val seg2Width = barTotalWidth * 0.30f
        val seg3Width = barTotalWidth * 0.20f

        val segPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        segPaint.color = Color.parseColor("#1877F2")
        canvas.drawRoundRect(RectF(70f, barTop, 70f + seg1Width, barBottom), 10f, 10f, segPaint)

        segPaint.color = Color.parseColor("#4C9AFF")
        canvas.drawRect(RectF(70f + seg1Width + 4f, barTop, 70f + seg1Width + seg2Width, barBottom), segPaint)

        segPaint.color = Color.parseColor("#22C55E")
        canvas.drawRoundRect(RectF(70f + seg1Width + seg2Width + 4f, barTop, 70f + barTotalWidth, barBottom), 10f, 10f, segPaint)

        currentY += 48f

        // Muscle breakdown legend
        paint.textSize = 20f
        paint.isFakeBoldText = false
        paint.color = Color.parseColor("#CBD5E1")
        canvas.drawText("● 50% Primary Target   ● 30% Secondary Assistance   ● 20% Stabilizers", 70f, currentY, paint)

        currentY += 45f

        // Divider
        val dividerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#1B2030")
            strokeWidth = 2f
        }
        canvas.drawLine(70f, currentY, width - 70f, currentY, dividerPaint)
        currentY += 40f

        // 6. Itemized Exercise List & Sets
        exercises.forEachIndexed { exIdx, exercise ->
            // Circular badge with number
            val isPeakEnergy = (exercise.orderPerformed in 1..3)
            val circlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = if (isPeakEnergy) Color.parseColor("#1877F2") else Color.parseColor("#13151F")
                style = Paint.Style.FILL
            }
            canvas.drawCircle(96f, currentY - 10f, 26f, circlePaint)

            val circleBorder = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#1877F2")
                style = Paint.Style.STROKE
                strokeWidth = 2.5f
            }
            canvas.drawCircle(96f, currentY - 10f, 26f, circleBorder)

            paint.color = if (isPeakEnergy) Color.WHITE else Color.parseColor("#4C9AFF")
            paint.textSize = 20f
            paint.isFakeBoldText = true
            val badgeText = "#${exercise.orderPerformed}"
            val badgeWidth = paint.measureText(badgeText)
            canvas.drawText(badgeText, 96f - (badgeWidth / 2), currentY - 2f, paint)

            // Exercise Name & Muscle Tag
            paint.color = Color.WHITE
            paint.textSize = 32f
            paint.isFakeBoldText = true
            canvas.drawText(exercise.name, 140f, currentY, paint)

            val tagX = 150f + paint.measureText(exercise.name)
            if (exercise.orderPerformed == 1) {
                paint.color = Color.parseColor("#4C9AFF")
                paint.textSize = 20f
                paint.isFakeBoldText = true
                canvas.drawText("• Primary Compound", tagX, currentY - 2f, paint)
            } else {
                paint.color = Color.parseColor("#64748B")
                paint.textSize = 22f
                paint.isFakeBoldText = false
                canvas.drawText("• ${exercise.muscleGroup}", tagX, currentY - 2f, paint)
            }

            currentY += 40f

            // Sets
            exercise.sets.forEach { set ->
                val setBoxRect = RectF(134f, currentY - 26f, 250f, currentY + 12f)
                val setBoxPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.parseColor("#0D0E14")
                }
                canvas.drawRoundRect(setBoxRect, 8f, 8f, setBoxPaint)

                paint.color = if (set.isWarmup) Color.parseColor("#94A3B8") else Color.parseColor("#4C9AFF")
                paint.textSize = 20f
                paint.isFakeBoldText = true
                val setLabel = if (set.isWarmup) "SET ${set.setNumber} (W)" else "SET ${set.setNumber}"
                canvas.drawText(setLabel, 146f, currentY - 2f, paint)

                paint.color = Color.WHITE
                paint.textSize = 26f
                paint.isFakeBoldText = true
                val weightReps = "${set.weightKg.toInt()} kg  ×  ${set.reps} reps"
                canvas.drawText(weightReps, 270f, currentY, paint)

                if (set.isPR) {
                    val prPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = Color.parseColor("#22C55E")
                        textSize = 22f
                        isFakeBoldText = true
                    }
                    canvas.drawText("⭐ PR", 540f, currentY - 1f, prPaint)
                }

                currentY += 44f
            }

            currentY += 24f
        }

        // 7. Footer
        val footerY = totalHeight - 80f
        canvas.drawLine(70f, footerY - 40f, width - 70f, footerY - 40f, dividerPaint)

        paint.color = Color.parseColor("#1877F2")
        paint.textSize = 26f
        paint.isFakeBoldText = true
        canvas.drawText("⚡ Logged with IronLog • AMOLED Fitness Tracker", 70f, footerY, paint)

        paint.color = Color.parseColor("#64748B")
        paint.textSize = 22f
        paint.isFakeBoldText = false
        val brandTag = "Consistent & Unstoppable"
        val brandWidth = paint.measureText(brandTag)
        canvas.drawText(brandTag, width - 70f - brandWidth, footerY, paint)

        return bitmap
    }
}

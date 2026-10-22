package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import android.content.Context
import android.content.Intent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.WorkoutViewModel
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import com.example.data.entity.WorkoutLogEntity
import com.example.util.WorkoutShareHelper
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun WorkoutHistoryScreen(
    viewModel: WorkoutViewModel
) {
    val logs by viewModel.workoutLogs.collectAsState()
    val completedDates by viewModel.completedEpochDays.collectAsState()
    val activeStreak by viewModel.activeStreak.collectAsState()

    val context = LocalContext.current
    var selectedDetailLog by remember { mutableStateOf<WorkoutLogEntity?>(null) }

    val totalWorkouts by remember { derivedStateOf { logs.size } }
    val totalVolumeKg by remember { derivedStateOf { logs.sumOf { it.totalVolumeKg } } }
    val totalSets by remember { derivedStateOf { logs.sumOf { it.totalSets } } }

    AnimatedContent(
        targetState = selectedDetailLog,
        transitionSpec = {
            if (targetState != null) {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Start,
                    animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing)
                ) + fadeIn(animationSpec = tween(180)) togetherWith
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Start,
                    animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing)
                ) + fadeOut(animationSpec = tween(150))
            } else {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing)
                ) + fadeIn(animationSpec = tween(180)) togetherWith
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing)
                ) + fadeOut(animationSpec = tween(150))
            }
        },
        label = "WorkoutDetailTransition"
    ) { detailLog ->
        if (detailLog != null) {
            WorkoutDetailScreen(
                log = detailLog,
                onBack = { selectedDetailLog = null }
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(AmoledBlack)
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
        // Header
        item {
            Column {
                Text(
                    text = "Workout History",
                    color = TextWhite,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "All recorded sessions and lifetime milestones",
                    color = TextMuted,
                    fontSize = 13.sp
                )
            }
        }

        // Top 3 Metric Cards: WORKOUTS, TOTAL VOL, TOTAL SETS
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Workouts
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .border(1.dp, DarkBorder, RoundedCornerShape(14.dp)),
                    color = DarkCardBg
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("WORKOUTS", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("$totalWorkouts", color = TextWhite, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
                    }
                }

                // Total Vol
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .border(1.dp, DarkBorder, RoundedCornerShape(14.dp)),
                    color = DarkCardBg
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("TOTAL VOL", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "${(totalVolumeKg / 1000).toInt()}k kg",
                            color = ElectricBlue,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                // Total Sets
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .border(1.dp, DarkBorder, RoundedCornerShape(14.dp)),
                    color = DarkCardBg
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("TOTAL SETS", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("$totalSets", color = StreakGreen, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
        }


        // Section Title: All Activity Logs
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "All Activity Logs",
                    color = TextWhite,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${logs.size} sessions",
                    color = TextMuted,
                    fontSize = 12.sp
                )
            }
        }

        // Logs List
        if (logs.isEmpty()) {
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp)),
                    color = DarkCardBg
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.History, contentDescription = null, tint = TextDark, modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No workouts recorded yet", color = TextMuted, fontSize = 14.sp)
                    }
                }
            }
        } else {
            items(logs, key = { it.id }) { log ->
                val date = LocalDate.ofEpochDay(log.dateEpochDay)
                val formattedDuration = "${log.durationSeconds / 60}m"

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .border(1.dp, DarkBorder, RoundedCornerShape(14.dp))
                        .clickable { selectedDetailLog = log }
                        .testTag("workout_log_${log.id}"),
                    color = DarkCardBg
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = log.routineTitle,
                                    color = TextWhite,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${date.month.name.take(3)} ${date.dayOfMonth}, ${date.year} • $formattedDuration",
                                    color = TextMuted,
                                    fontSize = 12.sp
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Surface(
                                    color = DarkCardSurface,
                                    shape = RoundedCornerShape(8.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                                ) {
                                    Text(
                                        text = "${log.totalSets} sets",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        color = ElectricBlueLight,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                Surface(
                                    color = DarkCardSurface,
                                    shape = RoundedCornerShape(8.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                                ) {
                                    Text(
                                        text = "${log.totalVolumeKg.toInt()} kg",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        color = StreakGreen,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                // Share action button on card
                                IconButton(
                                    onClick = { shareWorkoutSummary(context, log) },
                                    modifier = Modifier
                                        .size(32.dp)
                                        .testTag("share_log_${log.id}")
                                ) {
                                    Icon(
                                        Icons.Default.Share,
                                        contentDescription = "Share",
                                        tint = ElectricBlue,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        if (log.completedExercisesSummary.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            val exerciseList = log.completedExercisesSummary
                                .split(",")
                                .map { it.trim() }
                                .filter { it.isNotBlank() }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                exerciseList.forEachIndexed { idx, rawEx ->
                                    val cleanName = rawEx.substringBefore("(").trim()
                                    val prefix = if (cleanName.firstOrNull()?.isDigit() == true && cleanName.contains(".")) "" else "${idx + 1}. "
                                    Surface(
                                        color = DarkCardSurface,
                                        shape = RoundedCornerShape(6.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                                    ) {
                                        Text(
                                            text = "[$prefix$cleanName]",
                                            color = if (idx == 0) ElectricBlueLight else TextMuted,
                                            fontSize = 11.sp,
                                            fontWeight = if (idx == 0) FontWeight.Bold else FontWeight.Medium,
                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            }
        }
    }

    }
}

private fun formatLogDate(log: WorkoutLogEntity): String {
    return try {
        if (log.timestamp > 0) {
            val instant = Instant.ofEpochMilli(log.timestamp)
            val zdt = instant.atZone(ZoneId.systemDefault())
            zdt.format(DateTimeFormatter.ofPattern("EEEE, MMM d • h:mm a", Locale.ENGLISH))
        } else {
            val localDate = LocalDate.ofEpochDay(log.dateEpochDay)
            localDate.format(DateTimeFormatter.ofPattern("EEEE, MMM d", Locale.ENGLISH))
        }
    } catch (e: Exception) {
        val localDate = LocalDate.ofEpochDay(log.dateEpochDay)
        localDate.toString()
    }
}

fun buildWorkoutShareText(log: WorkoutLogEntity): String {
    val (_, exercises) = WorkoutShareHelper.convertLogToShareable(log)
    val dateStr = formatLogDate(log)
    val durationMin = (log.durationSeconds / 60).coerceAtLeast(1)
    val durationFormatted = "$durationMin min"
    val volumeFormatted = "${String.format(Locale.US, "%,d", log.totalVolumeKg.toInt())} kg"
    val setsFormatted = "${log.totalSets} sets"
    val divider = "━━━━━━━━━━━━━━━━━━━━━━"

    val sb = StringBuilder()
    sb.appendLine("🏋️ IronLog Workout Summary")
    sb.appendLine(divider)
    sb.appendLine("Routine: ${log.routineTitle}")
    sb.appendLine("Date: $dateStr")
    sb.appendLine("Duration: $durationFormatted")
    sb.appendLine("Total Volume: $volumeFormatted")
    sb.appendLine("Sets Completed: $setsFormatted")
    sb.appendLine(divider)
    sb.appendLine()
    sb.appendLine("💪 Exercises & Sets:")

    exercises.forEachIndexed { index, exercise ->
        sb.appendLine()
        sb.appendLine("${index + 1}. ${exercise.name}")
        exercise.sets.forEach { set ->
            val weightStr = if (set.weightKg % 1.0 == 0.0) {
                set.weightKg.toInt().toString()
            } else {
                String.format(Locale.US, "%.1f", set.weightKg)
            }
            sb.appendLine("   • Set ${set.setNumber}: $weightStr kg × ${set.reps} reps")
        }
    }

    sb.appendLine()
    sb.appendLine(divider)
    sb.append("Tracked with IronLog ⚡")
    return sb.toString()
}

fun shareWorkoutSummary(context: Context, log: WorkoutLogEntity) {
    val shareText = buildWorkoutShareText(log)
    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "IronLog: ${log.routineTitle}")
        putExtra(Intent.EXTRA_TEXT, shareText)
    }
    val chooser = Intent.createChooser(sendIntent, "Share Workout Summary")
    context.startActivity(chooser)
}


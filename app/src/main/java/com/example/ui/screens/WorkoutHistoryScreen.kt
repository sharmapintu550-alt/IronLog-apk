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
import androidx.compose.ui.graphics.Color
import android.content.Context
import android.content.Intent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.WorkoutViewModel
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.saveable.rememberSaveable
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
    var selectedSubTab by rememberSaveable { mutableStateOf(0) } // 0 = History Logs, 1 = Analytics & Progression
    var logToDelete by remember { mutableStateOf<WorkoutLogEntity?>(null) }

    val totalWorkouts by remember { derivedStateOf { logs.size } }
    val totalVolumeKg by remember { derivedStateOf { logs.sumOf { it.totalVolumeKg } } }
    val totalSets by remember { derivedStateOf { logs.sumOf { it.totalSets } } }

    if (logToDelete != null) {
        val target = logToDelete!!
        AlertDialog(
            onDismissRequest = { logToDelete = null },
            containerColor = Color(0xFF141414),
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 6.dp,
            title = {
                Text(
                    text = "Delete Workout Log?",
                    color = TextWhite,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete this session for '${target.routineTitle}'? This will recalculate your total volume and sets, and remove the entry permanently.",
                    color = TextMuted,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteWorkoutLog(target)
                        logToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF453A),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("confirm_delete_log_button")
                ) {
                    Text("Delete", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { logToDelete = null },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF9CA3AF)),
                    modifier = Modifier.testTag("cancel_delete_log_button")
                ) {
                    Text("Cancel")
                }
            }
        )
    }

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
                onBack = { selectedDetailLog = null },
                onDelete = {
                    viewModel.deleteWorkoutLog(detailLog)
                    selectedDetailLog = null
                }
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(AmoledBlack)
            ) {
                // Top Sub-Tab Switcher: Activity Logs vs Analytics & Progression
                Surface(
                    color = Color(0xFF141414),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Color(0xFF262626)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(3.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (selectedSubTab == 0) ElectricBlue else Color.Transparent)
                                .clickable { selectedSubTab = 0 }
                                .padding(vertical = 7.dp)
                                .testTag("tab_history_logs"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Activity Logs (${logs.size})",
                                color = if (selectedSubTab == 0) Color.White else TextMuted,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (selectedSubTab == 1) ElectricBlue else Color.Transparent)
                                .clickable { selectedSubTab = 1 }
                                .padding(vertical = 7.dp)
                                .testTag("tab_history_analytics"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "Analytics & 1RM 📈",
                                    color = if (selectedSubTab == 1) Color.White else TextMuted,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                if (selectedSubTab == 1) {
                    AnalyticsDashboardScreen(
                        viewModel = viewModel,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(AmoledBlack)
                            .padding(horizontal = 16.dp),
                        contentPadding = PaddingValues(top = 8.dp, bottom = 96.dp),
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
                        Text(
                            text = if (totalWorkouts == 0) "0 Workouts" else "$totalWorkouts",
                            color = TextWhite,
                            fontSize = if (totalWorkouts == 0) 16.sp else 22.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
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
                        val volText = when {
                            totalVolumeKg <= 0.0 -> "0 kg Vol"
                            totalVolumeKg >= 1000.0 -> "${(totalVolumeKg / 1000).toInt()}k kg"
                            else -> "${totalVolumeKg.toInt()} kg"
                        }
                        Text(
                            text = volText,
                            color = ElectricBlue,
                            fontSize = if (totalVolumeKg <= 0.0) 16.sp else 22.sp,
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
                        Text(
                            text = if (totalSets == 0) "0 Sets" else "$totalSets",
                            color = StreakGreen,
                            fontSize = if (totalSets == 0) 16.sp else 22.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
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
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, DarkBorder, RoundedCornerShape(16.dp))
                        .testTag("empty_history_state"),
                    color = DarkCardBg
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp, horizontal = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            modifier = Modifier.size(56.dp),
                            shape = CircleShape,
                            color = ElectricBlue.copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, ElectricBlue.copy(alpha = 0.3f))
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.FitnessCenter,
                                    contentDescription = null,
                                    tint = ElectricBlue,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No workout logs yet",
                            color = TextWhite,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Complete a workout session to see your progress, volume, and history here.",
                            color = TextMuted,
                            fontSize = 13.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            lineHeight = 18.sp
                        )
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

                                // Delete action button on card
                                IconButton(
                                    onClick = { logToDelete = log },
                                    modifier = Modifier
                                        .size(32.dp)
                                        .testTag("delete_log_${log.id}")
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = Color(0xFFFF453A),
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


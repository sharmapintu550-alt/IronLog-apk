package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.ExerciseEntity
import com.example.data.entity.RoutineEntity
import com.example.data.entity.WorkoutLogEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.WorkoutViewModel
import com.example.util.CompletedExerciseItem
import com.example.util.CompletedSetItem
import com.example.util.WorkoutShareHelper
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

enum class ChartMetric(val label: String, val unit: String) {
    ONE_RM("Estimated 1RM", "kg"),
    MAX_WEIGHT("Max Weight", "kg"),
    TOTAL_VOLUME("Total Volume", "kg")
}

enum class TimeFilter(val label: String, val days: Long) {
    ONE_MONTH("1M", 30L),
    THREE_MONTHS("3M", 90L),
    SIX_MONTHS("6M", 180L),
    ONE_YEAR("1Y", 365L),
    ALL("All", Long.MAX_VALUE)
}

data class ExerciseSessionHistory(
    val logId: String,
    val routineTitle: String,
    val epochDay: Long,
    val timestamp: Long,
    val sets: List<CompletedSetItem>,
    val bestWeight: Double,
    val best1RM: Double,
    val totalVolume: Double
)

data class ChartPoint(
    val epochDay: Long,
    val dateLabel: String,
    val value: Double,
    val weightKg: Double,
    val reps: Int,
    val epley1RM: Double,
    val volumeKg: Double
)

fun calculateEpley1RM(weightKg: Double, reps: Int): Double {
    if (weightKg <= 0.0) return 0.0
    if (reps <= 1) return weightKg
    return weightKg * (1.0 + (reps.toDouble() / 30.0))
}

private fun isExerciseMatch(loggedName: String, catalogExercise: ExerciseEntity): Boolean {
    val cleanLogged = loggedName.replace(Regex("^\\d+\\.\\s*"), "").trim().lowercase()
    val cleanTarget = catalogExercise.name.trim().lowercase()
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
    val intersection = wordsLogged.intersect(wordsTarget)
    if (intersection.any { it in listOf("squat", "deadlift", "bench", "press", "curl", "pulldown", "row", "pushdown", "plank", "lunge") }) {
        return true
    }
    return false
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseDetailHistoryScreen(
    exercise: ExerciseEntity,
    viewModel: WorkoutViewModel,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)

    val context = LocalContext.current
    val workoutLogs by viewModel.workoutLogs.collectAsState()
    val routines by viewModel.routines.collectAsState()

    var selectedMetric by remember { mutableStateOf(ChartMetric.ONE_RM) }
    var selectedTimeFilter by remember { mutableStateOf(TimeFilter.ALL) }
    var showAddToRoutineDialog by remember { mutableStateOf(false) }

    // Parse session history for this exercise
    val sessionHistories = remember(workoutLogs, exercise) {
        val list = mutableListOf<ExerciseSessionHistory>()

        workoutLogs.forEach { log ->
            val (_, parsedExercises) = WorkoutShareHelper.convertLogToShareable(log)
            val matchingItems = parsedExercises.filter { isExerciseMatch(it.name, exercise) }

            if (matchingItems.isNotEmpty()) {
                matchingItems.forEach { matchedEx ->
                    if (matchedEx.sets.isNotEmpty()) {
                        val bestSetWeight = matchedEx.sets.maxOfOrNull { it.weightKg } ?: 0.0
                        val best1RM = matchedEx.sets.maxOfOrNull { calculateEpley1RM(it.weightKg, it.reps) } ?: 0.0
                        val totalVol = matchedEx.sets.sumOf { it.weightKg * it.reps }

                        list.add(
                            ExerciseSessionHistory(
                                logId = log.id,
                                routineTitle = log.routineTitle,
                                epochDay = log.dateEpochDay,
                                timestamp = if (log.timestamp > 0) log.timestamp else (log.dateEpochDay * 86400000L),
                                sets = matchedEx.sets,
                                bestWeight = bestSetWeight,
                                best1RM = best1RM,
                                totalVolume = totalVol
                            )
                        )
                    }
                }
            }
        }

        // Baseline entry if exercise has prWeight and no prior logs
        if (list.isEmpty() && exercise.prWeight > 0) {
            val baseline1RM = calculateEpley1RM(exercise.prWeight, exercise.prReps.coerceAtLeast(1))
            val dummySets = listOf(
                CompletedSetItem(
                    setNumber = 1,
                    weightKg = exercise.prWeight * 0.8,
                    reps = 10,
                    isWarmup = true
                ),
                CompletedSetItem(
                    setNumber = 2,
                    weightKg = exercise.prWeight,
                    reps = exercise.prReps.coerceAtLeast(1),
                    isWarmup = false,
                    isPR = true
                )
            )
            val totalVol = dummySets.sumOf { it.weightKg * it.reps }
            list.add(
                ExerciseSessionHistory(
                    logId = "baseline_pr_${exercise.id}",
                    routineTitle = "Baseline Personal Record",
                    epochDay = LocalDate.now().minusDays(14).toEpochDay(),
                    timestamp = System.currentTimeMillis() - 14 * 86400000L,
                    sets = dummySets,
                    bestWeight = exercise.prWeight,
                    best1RM = baseline1RM,
                    totalVolume = totalVol
                )
            )
        }

        list.sortedByDescending { it.epochDay }
    }

    // High Impact PR Metrics Calculations
    val maxWeightAcrossAllTime = remember(sessionHistories, exercise) {
        val historyMax = sessionHistories.maxOfOrNull { it.bestWeight } ?: 0.0
        maxOf(historyMax, exercise.prWeight)
    }

    val max1RMAcrossAllTime = remember(sessionHistories, exercise) {
        val history1RM = sessionHistories.maxOfOrNull { it.best1RM } ?: 0.0
        val catalog1RM = if (exercise.prWeight > 0) calculateEpley1RM(exercise.prWeight, exercise.prReps.coerceAtLeast(1)) else 0.0
        maxOf(history1RM, catalog1RM)
    }

    val bestSessionVolume = remember(sessionHistories) {
        sessionHistories.maxOfOrNull { it.totalVolume } ?: 0.0
    }

    // Filter points for chart
    val todayEpochDay = remember { LocalDate.now().toEpochDay() }
    val filteredPoints = remember(sessionHistories, selectedMetric, selectedTimeFilter) {
        val cutoffDay = if (selectedTimeFilter.days == Long.MAX_VALUE) 0L else (todayEpochDay - selectedTimeFilter.days)
        val filteredSessions = sessionHistories.filter { it.epochDay >= cutoffDay }.sortedBy { it.epochDay }

        filteredSessions.map { session ->
            val dateLabel = try {
                LocalDate.ofEpochDay(session.epochDay).format(DateTimeFormatter.ofPattern("MMM d", Locale.ENGLISH))
            } catch (e: Exception) {
                "Day ${session.epochDay}"
            }
            val bestSet = session.sets.maxByOrNull { calculateEpley1RM(it.weightKg, it.reps) }
            val metricVal = when (selectedMetric) {
                ChartMetric.ONE_RM -> session.best1RM
                ChartMetric.MAX_WEIGHT -> session.bestWeight
                ChartMetric.TOTAL_VOLUME -> session.totalVolume
            }
            ChartPoint(
                epochDay = session.epochDay,
                dateLabel = dateLabel,
                value = metricVal,
                weightKg = bestSet?.weightKg ?: session.bestWeight,
                reps = bestSet?.reps ?: 1,
                epley1RM = session.best1RM,
                volumeKg = session.totalVolume
            )
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("exercise_detail_history_screen"),
        containerColor = AmoledBlack,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = exercise.name,
                            color = TextWhite,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Surface(
                                color = ElectricBlueAlpha,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = exercise.muscleGroup,
                                    color = ElectricBlueLight,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Text(
                                text = "• ${exercise.equipment}",
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                            if (exercise.trackingType.isNotBlank()) {
                                Text(
                                    text = "• ${exercise.trackingType.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() }}",
                                    color = TextDark,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("back_button_exercise_detail")
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextWhite
                        )
                    }
                },
                actions = {
                    Button(
                        onClick = { showAddToRoutineDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                        shape = RoundedCornerShape(16.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .testTag("add_to_routine_quick_action")
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Add to Routine",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AmoledBlack
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Personal Records (PR) High-Impact Summary Bar
            item {
                Text(
                    text = "PERSONAL RECORDS",
                    color = TextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    PRMetricCard(
                        title = "Estimated 1RM",
                        value = if (max1RMAcrossAllTime > 0) "${String.format(Locale.US, "%.1f", max1RMAcrossAllTime)} kg" else "—",
                        subtitle = "Epley formula",
                        badge = "🏆 1RM",
                        badgeColor = StreakGreen,
                        modifier = Modifier.weight(1f).testTag("metric_card_1rm")
                    )
                    PRMetricCard(
                        title = "Max Weight",
                        value = if (maxWeightAcrossAllTime > 0) "${if (maxWeightAcrossAllTime % 1.0 == 0.0) maxWeightAcrossAllTime.toInt().toString() else String.format(Locale.US, "%.1f", maxWeightAcrossAllTime)} kg" else "—",
                        subtitle = "All-time peak",
                        badge = "⚡ PEAK",
                        badgeColor = ElectricBlueLight,
                        modifier = Modifier.weight(1f).testTag("metric_card_max_weight")
                    )
                    PRMetricCard(
                        title = "Best Volume",
                        value = if (bestSessionVolume > 0) "${String.format(Locale.US, "%,d", bestSessionVolume.roundToInt())} kg" else "—",
                        subtitle = "Single session",
                        badge = "🔥 VOL",
                        badgeColor = WarmupAmber,
                        modifier = Modifier.weight(1f).testTag("metric_card_best_volume")
                    )
                }
            }

            // 2. Interactive Progression Chart Section
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, DarkBorder, RoundedCornerShape(16.dp))
                        .testTag("progression_chart_card"),
                    color = DarkCardBg
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Header & Metric Toggle Chips
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Progression Curve",
                                color = TextWhite,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                text = "${filteredPoints.size} logged sessions",
                                color = TextDark,
                                fontSize = 12.sp
                            )
                        }

                        // Metric Selector Chips
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ChartMetric.values().forEach { metric ->
                                val isSelected = metric == selectedMetric
                                Surface(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable { selectedMetric = metric }
                                        .testTag("metric_chip_${metric.name}"),
                                    color = if (isSelected) ElectricBlue else DarkCardSurface,
                                    border = BorderStroke(1.dp, if (isSelected) ElectricBlue else DarkBorder),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text(
                                        text = metric.label,
                                        color = if (isSelected) Color.White else TextMuted,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }

                        // Time Range Filters (1M, 3M, 6M, 1Y, All)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            TimeFilter.values().forEach { filter ->
                                val isSelected = filter == selectedTimeFilter
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { selectedTimeFilter = filter }
                                        .testTag("time_filter_${filter.name}"),
                                    color = if (isSelected) ElectricBlueAlpha else Color.Transparent,
                                    border = BorderStroke(1.dp, if (isSelected) ElectricBlue else DarkBorder),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = filter.label,
                                        color = if (isSelected) ElectricBlueLight else TextMuted,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(vertical = 5.dp)
                                    )
                                }
                            }
                        }

                        // Canvas Chart Area
                        if (filteredPoints.isEmpty()) {
                            // Empty State
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .background(DarkCardSurface, RoundedCornerShape(12.dp))
                                    .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
                                    .padding(20.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        Icons.Default.ShowChart,
                                        contentDescription = null,
                                        tint = TextDark,
                                        modifier = Modifier.size(36.dp)
                                    )
                                    Text(
                                        text = "No history in this time range",
                                        color = TextWhite,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "Complete a workout including ${exercise.name} to see your progression curve!",
                                        color = TextMuted,
                                        fontSize = 12.sp,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.widthIn(max = 280.dp)
                                    )
                                }
                            }
                        } else {
                            InteractiveProgressionCanvas(
                                points = filteredPoints,
                                metric = selectedMetric,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(220.dp)
                            )
                        }
                    }
                }
            }

            // 3. Chronological Session-by-Session History List
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SESSION HISTORY",
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "${sessionHistories.size} total sessions",
                        color = TextDark,
                        fontSize = 12.sp
                    )
                }
            }

            if (sessionHistories.isEmpty()) {
                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .border(1.dp, DarkBorder, RoundedCornerShape(14.dp)),
                        color = DarkCardBg
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.FitnessCenter,
                                contentDescription = null,
                                tint = ElectricBlue,
                                modifier = Modifier.size(32.dp)
                            )
                            Text(
                                text = "No Workout Sessions Logged Yet",
                                color = TextWhite,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Start an active workout and add ${exercise.name} to record your sets, weights, and unlock personal records.",
                                color = TextMuted,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(sessionHistories, key = { it.logId }) { session ->
                    SessionHistoryCard(
                        session = session,
                        exerciseName = exercise.name,
                        max1RMAcrossAllTime = max1RMAcrossAllTime,
                        maxWeightAcrossAllTime = maxWeightAcrossAllTime
                    )
                }
            }
        }
    }

    // Add to Routine Dialog
    if (showAddToRoutineDialog) {
        AlertDialog(
            onDismissRequest = { showAddToRoutineDialog = false },
            containerColor = DarkCardBg,
            title = {
                Text(
                    text = "Add to Routine",
                    color = TextWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Choose which routine to add ${exercise.name} into:",
                        color = TextMuted,
                        fontSize = 13.sp
                    )

                    if (routines.isEmpty()) {
                        Text(
                            text = "No routines created yet. Create a routine in the Workouts tab first!",
                            color = TextDark,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 300.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(routines, key = { it.id }) { routine ->
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .border(1.dp, DarkBorder, RoundedCornerShape(10.dp))
                                        .clickable {
                                            viewModel.addExerciseToRoutine(routine, exercise.name)
                                            Toast.makeText(
                                                context,
                                                "Added ${exercise.name} to ${routine.title}!",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            showAddToRoutineDialog = false
                                        }
                                        .testTag("routine_picker_item_${routine.id}"),
                                    color = DarkCardSurface
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = routine.title,
                                                color = TextWhite,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            if (routine.exercisesSummary.isNotBlank()) {
                                                Text(
                                                    text = routine.exercisesSummary,
                                                    color = TextDark,
                                                    fontSize = 11.sp,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                        Icon(
                                            Icons.Default.AddCircle,
                                            contentDescription = "Add",
                                            tint = ElectricBlue,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAddToRoutineDialog = false }) {
                    Text("Close", color = TextMuted)
                }
            }
        )
    }
}

@Composable
private fun PRMetricCard(
    title: String,
    value: String,
    subtitle: String,
    badge: String,
    badgeColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, DarkBorder, RoundedCornerShape(14.dp)),
        color = DarkCardBg
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title.uppercase(),
                    color = TextMuted,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    color = badgeColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = badge,
                        color = badgeColor,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }

            Text(
                text = value,
                color = TextWhite,
                fontSize = 17.sp,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1
            )

            Text(
                text = subtitle,
                color = TextDark,
                fontSize = 10.sp
            )
        }
    }
}

@Composable
private fun InteractiveProgressionCanvas(
    points: List<ChartPoint>,
    metric: ChartMetric,
    modifier: Modifier = Modifier
) {
    var selectedPointIndex by remember(points) { mutableStateOf<Int?>(points.lastIndex.takeIf { it >= 0 }) }

    val values = points.map { it.value }
    val rawMin = values.minOrNull() ?: 0.0
    val rawMax = values.maxOrNull() ?: 100.0

    // Provide breathing room on Y-axis
    val minY = if (rawMin == rawMax) (rawMin * 0.85).coerceAtLeast(0.0) else (rawMin * 0.9).coerceAtLeast(0.0)
    val maxY = if (rawMin == rawMax) (rawMax * 1.15).coerceAtLeast(10.0) else (rawMax * 1.08).coerceAtLeast(10.0)
    val rangeY = (maxY - minY).coerceAtLeast(1.0)

    Column(modifier = modifier) {
        // Selected Point Tooltip Pill
        val activePoint = selectedPointIndex?.let { points.getOrNull(it) } ?: points.lastOrNull()
        if (activePoint != null) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                color = DarkCardSurface,
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, ElectricBlueAlpha)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 7.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = ElectricBlueLight,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = activePoint.dateLabel,
                            color = TextWhite,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Peak: ${activePoint.weightKg.toInt()} kg × ${activePoint.reps}",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                        Surface(
                            color = ElectricBlue,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "${String.format(Locale.US, "%.1f", activePoint.value)} ${metric.unit}",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }

        // Canvas Area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .pointerInput(points) {
                    detectTapGestures { offset ->
                        if (points.isNotEmpty()) {
                            val stepX = size.width / (points.size - 1).coerceAtLeast(1)
                            val clickedIndex = (offset.x / stepX).roundToInt().coerceIn(0, points.lastIndex)
                            selectedPointIndex = clickedIndex
                        }
                    }
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val canvasWidth = size.width
                val canvasHeight = size.height
                val paddingBottom = 24.dp.toPx()
                val usableHeight = canvasHeight - paddingBottom

                // Draw dashed grid lines (4 levels: 0%, 33%, 66%, 100%)
                val gridLevels = listOf(0f, 0.33f, 0.66f, 1f)
                val dashEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)

                gridLevels.forEach { level ->
                    val y = usableHeight * (1f - level)
                    drawLine(
                        color = Color(0xFF1B2030),
                        start = Offset(0f, y),
                        end = Offset(canvasWidth, y),
                        strokeWidth = 1.dp.toPx(),
                        pathEffect = dashEffect
                    )
                }

                if (points.size == 1) {
                    // Single data point representation
                    val singlePoint = points[0]
                    val yNorm = 1f - ((singlePoint.value - minY) / rangeY).toFloat().coerceIn(0f, 1f)
                    val y = usableHeight * yNorm
                    val x = canvasWidth / 2f

                    // Horizontal glowing guide
                    drawLine(
                        color = Color(0x661877F2),
                        start = Offset(0f, y),
                        end = Offset(canvasWidth, y),
                        strokeWidth = 2.dp.toPx()
                    )

                    // Outer halo
                    drawCircle(
                        color = Color(0x331877F2),
                        radius = 16.dp.toPx(),
                        center = Offset(x, y)
                    )
                    // Inner node
                    drawCircle(
                        color = Color(0xFF1877F2),
                        radius = 6.dp.toPx(),
                        center = Offset(x, y)
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 2.5.dp.toPx(),
                        center = Offset(x, y)
                    )
                } else {
                    // Multi-point progression curve
                    val stepX = canvasWidth / (points.size - 1).toFloat()
                    val calculatedOffsets = points.mapIndexed { idx, pt ->
                        val yNorm = 1f - ((pt.value - minY) / rangeY).toFloat().coerceIn(0f, 1f)
                        Offset(x = idx * stepX, y = usableHeight * yNorm)
                    }

                    // 1. Draw smooth curve path
                    val curvePath = Path()
                    val fillPath = Path()

                    calculatedOffsets.forEachIndexed { i, pt ->
                        if (i == 0) {
                            curvePath.moveTo(pt.x, pt.y)
                            fillPath.moveTo(pt.x, usableHeight)
                            fillPath.lineTo(pt.x, pt.y)
                        } else {
                            val prev = calculatedOffsets[i - 1]
                            val controlX1 = prev.x + (pt.x - prev.x) / 2f
                            val controlY1 = prev.y
                            val controlX2 = prev.x + (pt.x - prev.x) / 2f
                            val controlY2 = pt.y
                            curvePath.cubicTo(controlX1, controlY1, controlX2, controlY2, pt.x, pt.y)
                            fillPath.cubicTo(controlX1, controlY1, controlX2, controlY2, pt.x, pt.y)
                        }
                    }

                    fillPath.lineTo(calculatedOffsets.last().x, usableHeight)
                    fillPath.close()

                    // Gradient fill underneath
                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0x551877F2),
                                Color(0x181877F2),
                                Color.Transparent
                            ),
                            startY = 0f,
                            endY = usableHeight
                        )
                    )

                    // Stroke line
                    drawPath(
                        path = curvePath,
                        color = Color(0xFF1877F2),
                        style = Stroke(
                            width = 3.5.dp.toPx(),
                            pathEffect = PathEffect.cornerPathEffect(12f)
                        )
                    )

                    // Draw vertical guide line if a point is selected
                    selectedPointIndex?.let { selIdx ->
                        if (selIdx in calculatedOffsets.indices) {
                            val selOffset = calculatedOffsets[selIdx]
                            drawLine(
                                color = Color(0x991877F2),
                                start = Offset(selOffset.x, 0f),
                                end = Offset(selOffset.x, usableHeight),
                                strokeWidth = 1.5.dp.toPx(),
                                pathEffect = dashEffect
                            )
                        }
                    }

                    // Draw data nodes
                    calculatedOffsets.forEachIndexed { i, offset ->
                        val isSelected = (i == selectedPointIndex)
                        if (isSelected) {
                            drawCircle(
                                color = Color(0x441877F2),
                                radius = 14.dp.toPx(),
                                center = offset
                            )
                            drawCircle(
                                color = Color(0xFF1877F2),
                                radius = 6.dp.toPx(),
                                center = offset
                            )
                            drawCircle(
                                color = Color.White,
                                radius = 2.5.dp.toPx(),
                                center = offset
                            )
                        } else {
                            drawCircle(
                                color = Color(0xFF1877F2),
                                radius = 4.dp.toPx(),
                                center = offset
                            )
                            drawCircle(
                                color = Color(0xFF000000),
                                radius = 2.dp.toPx(),
                                center = offset
                            )
                        }
                    }
                }
            }
        }

        // X-Axis Date Reference Labels
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = points.firstOrNull()?.dateLabel ?: "",
                color = TextDark,
                fontSize = 10.sp
            )
            if (points.size > 2) {
                Text(
                    text = points[points.size / 2].dateLabel,
                    color = TextDark,
                    fontSize = 10.sp
                )
            }
            Text(
                text = points.lastOrNull()?.dateLabel ?: "",
                color = TextDark,
                fontSize = 10.sp
            )
        }
    }
}

@Composable
private fun SessionHistoryCard(
    session: ExerciseSessionHistory,
    exerciseName: String,
    max1RMAcrossAllTime: Double,
    maxWeightAcrossAllTime: Double
) {
    val dateFormatted = try {
        val date = LocalDate.ofEpochDay(session.epochDay)
        date.format(DateTimeFormatter.ofPattern("EEEE, MMM d, yyyy", Locale.ENGLISH))
    } catch (e: Exception) {
        "Day ${session.epochDay}"
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, DarkBorder, RoundedCornerShape(14.dp))
            .testTag("session_history_card_${session.logId}"),
        color = DarkCardBg
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Row: Routine Name & Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = session.routineTitle,
                        color = TextWhite,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = dateFormatted,
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                }

                // Volume Pill
                Surface(
                    color = DarkCardSurface,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, DarkBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "${session.sets.size} sets",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                        Text(
                            text = "•",
                            color = TextDark,
                            fontSize = 10.sp
                        )
                        Text(
                            text = "${String.format(Locale.US, "%,d", session.totalVolume.roundToInt())} kg",
                            color = ElectricBlueLight,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            HorizontalDivider(color = DarkBorder, thickness = 1.dp)

            // Itemized Set List
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                session.sets.forEach { set ->
                    val set1RM = calculateEpley1RM(set.weightKg, set.reps)
                    val isPRSet = set.isPR || (set.weightKg >= maxWeightAcrossAllTime && maxWeightAcrossAllTime > 0) || (set1RM >= max1RMAcrossAllTime && max1RMAcrossAllTime > 0)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isPRSet) Color(0x1522C55E) else DarkCardSurface)
                            .padding(horizontal = 10.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                color = if (set.isWarmup) WarmupAmber.copy(alpha = 0.2f) else DarkCardBg,
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(1.dp, if (set.isWarmup) WarmupAmber.copy(alpha = 0.4f) else DarkBorder)
                            ) {
                                Text(
                                    text = if (set.isWarmup) "W" else "SET ${set.setNumber}",
                                    color = if (set.isWarmup) WarmupAmber else TextMuted,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }

                            Text(
                                text = "${if (set.weightKg % 1.0 == 0.0) set.weightKg.toInt().toString() else String.format(Locale.US, "%.1f", set.weightKg)} kg  ×  ${set.reps} reps",
                                color = TextWhite,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "1RM: ${String.format(Locale.US, "%.1f", set1RM)} kg",
                                color = TextDark,
                                fontSize = 11.sp
                            )

                            if (isPRSet) {
                                Surface(
                                    color = StreakGreen.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(4.dp),
                                    border = BorderStroke(1.dp, StreakGreen.copy(alpha = 0.5f))
                                ) {
                                    Text(
                                        text = "🏆 PR",
                                        color = StreakGreen,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
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

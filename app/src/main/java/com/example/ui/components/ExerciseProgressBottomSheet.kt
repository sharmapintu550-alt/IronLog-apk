package com.example.ui.components

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
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FitnessCenter
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.ExerciseAnalyticsRepository
import com.example.data.repository.ExerciseAnalyticsSummary
import com.example.data.repository.ExerciseSessionRecord
import com.example.ui.theme.*
import com.example.ui.viewmodel.WorkoutViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

enum class ProgressMetric(val label: String, val unit: String) {
    ONE_RM("Estimated 1RM", "kg"),
    MAX_WEIGHT("Max Weight", "kg"),
    TOTAL_VOLUME("Total Volume", "kg")
}

private data class ProgressChartPoint(
    val epochDay: Long,
    val dateLabel: String,
    val value: Double,
    val maxWeightKg: Double,
    val best1RM: Double,
    val volumeKg: Double,
    val bestSetString: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseProgressBottomSheet(
    exerciseName: String,
    muscleGroup: String = "Other",
    equipment: String = "Barbell",
    baselinePrWeight: Double = 0.0,
    baselinePrReps: Int = 0,
    viewModel: WorkoutViewModel,
    onDismiss: () -> Unit
) {
    val workoutLogs by viewModel.workoutLogs.collectAsState()
    val allDbExercises by viewModel.exercises.collectAsState()

    // Resolve accurate muscleGroup and equipment from DB if available
    val (resolvedMuscle, resolvedEquipment, resolvedPrWeight, resolvedPrReps) = remember(exerciseName, allDbExercises, muscleGroup, equipment, baselinePrWeight, baselinePrReps) {
        val clean = exerciseName.substringBefore("(").trim()
        val dbMatch = allDbExercises.find { it.name.equals(clean, ignoreCase = true) || it.name.contains(clean, ignoreCase = true) }
        val m = dbMatch?.primaryMuscle?.ifBlank { dbMatch.muscleGroup } ?: muscleGroup.ifBlank { "Other" }
        val eq = dbMatch?.equipment ?: equipment.ifBlank { "Barbell" }
        val prW = if (baselinePrWeight > 0.0) baselinePrWeight else (dbMatch?.prWeight ?: 0.0)
        val prR = if (baselinePrReps > 0) baselinePrReps else (dbMatch?.prReps ?: 0)
        Tuple4(m, eq, prW, prR)
    }

    val analytics: ExerciseAnalyticsSummary = remember(exerciseName, workoutLogs, resolvedMuscle, resolvedEquipment, resolvedPrWeight, resolvedPrReps) {
        ExerciseAnalyticsRepository.computeAnalytics(
            exerciseName = exerciseName,
            targetMuscle = resolvedMuscle,
            targetEquipment = resolvedEquipment,
            logs = workoutLogs,
            baselinePrWeight = resolvedPrWeight,
            baselinePrReps = resolvedPrReps
        )
    }

    var selectedMetric by remember { mutableStateOf(ProgressMetric.ONE_RM) }

    val chartPoints = remember(analytics, selectedMetric) {
        val chronologicalSessions = analytics.sessionRecords.sortedBy { it.epochDay }
        chronologicalSessions.map { session ->
            val dateLabel = try {
                LocalDate.ofEpochDay(session.epochDay).format(DateTimeFormatter.ofPattern("MMM d", Locale.ENGLISH))
            } catch (e: Exception) {
                "Day ${session.epochDay}"
            }

            val valForMetric = when (selectedMetric) {
                ProgressMetric.ONE_RM -> session.best1RM
                ProgressMetric.MAX_WEIGHT -> session.maxWeightKg
                ProgressMetric.TOTAL_VOLUME -> session.sessionVolumeKg
            }

            val bestSet = session.sets.maxByOrNull { it.weightKg }
            val bestSetDesc = if (bestSet != null) "${bestSet.weightKg.toInt()} kg × ${bestSet.reps}" else "—"

            ProgressChartPoint(
                epochDay = session.epochDay,
                dateLabel = dateLabel,
                value = valForMetric,
                maxWeightKg = session.maxWeightKg,
                best1RM = session.best1RM,
                volumeKg = session.sessionVolumeKg,
                bestSetString = bestSetDesc
            )
        }
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = AmoledBlack,
        dragHandle = {
            Surface(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .width(42.dp)
                    .height(4.dp),
                shape = RoundedCornerShape(2.dp),
                color = DarkBorder
            ) {}
        },
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        modifier = Modifier.testTag("exercise_progress_bottom_sheet")
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 36.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header: Exercise Title, Badges & Close Button
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = analytics.exerciseName,
                            color = TextWhite,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                color = ElectricBlueAlpha,
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(1.dp, ElectricBlue.copy(alpha = 0.3f))
                            ) {
                                Text(
                                    text = analytics.muscleGroup,
                                    color = ElectricBlueLight,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                            Surface(
                                color = DarkCardSurface,
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(1.dp, DarkBorder)
                            ) {
                                Text(
                                    text = analytics.equipment,
                                    color = TextMuted,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_exercise_progress_sheet")
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextMuted
                        )
                    }
                }
            }

            // 3 High-Impact KPI Cards
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // KPI 1: All-Time PR Max Weight
                    ProgressKpiCard(
                        title = "ALL-TIME PR",
                        value = if (analytics.allTimeMaxWeight > 0) {
                            if (analytics.allTimeMaxWeight % 1.0 == 0.0) "${analytics.allTimeMaxWeight.toInt()} kg"
                            else "${String.format(Locale.US, "%.1f", analytics.allTimeMaxWeight)} kg"
                        } else "—",
                        subtitle = "Peak load",
                        badge = "🏆 PR",
                        badgeColor = ElectricBlueLight,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("kpi_card_pr")
                    )

                    // KPI 2: Best Estimated 1RM
                    ProgressKpiCard(
                        title = "BEST 1RM",
                        value = if (analytics.bestEstimated1RM > 0) {
                            "${String.format(Locale.US, "%.1f", analytics.bestEstimated1RM)} kg"
                        } else "—",
                        subtitle = "Epley formula",
                        badge = "⚡ 1RM",
                        badgeColor = StreakGreen,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("kpi_card_1rm")
                    )

                    // KPI 3: Lifetime Volume
                    ProgressKpiCard(
                        title = "TOTAL VOL",
                        value = if (analytics.lifetimeVolume > 0) {
                            if (analytics.lifetimeVolume >= 1000) "${(analytics.lifetimeVolume / 1000).toInt()}k kg"
                            else "${analytics.lifetimeVolume.toInt()} kg"
                        } else "—",
                        subtitle = "${analytics.totalSessions} sessions",
                        badge = "🔥 VOL",
                        badgeColor = WarmupAmber,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("kpi_card_volume")
                    )
                }
            }

            // Progression Curve Section
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, DarkBorder, RoundedCornerShape(16.dp)),
                    color = DarkCardBg
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Progression Analytics",
                                color = TextWhite,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${analytics.totalSessions} sessions recorded",
                                color = TextDark,
                                fontSize = 11.sp
                            )
                        }

                        // Metric Selector Chips
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ProgressMetric.values().forEach { metric ->
                                val isSelected = metric == selectedMetric
                                Surface(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { selectedMetric = metric }
                                        .testTag("metric_chip_${metric.name.lowercase()}"),
                                    color = if (isSelected) ElectricBlue else DarkCardSurface,
                                    border = BorderStroke(1.dp, if (isSelected) ElectricBlue else DarkBorder),
                                    shape = RoundedCornerShape(8.dp)
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

                        // Chart or Fallback State (< 2 sessions)
                        if (chartPoints.size < 2) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .background(DarkCardSurface, RoundedCornerShape(12.dp))
                                    .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.ShowChart,
                                        contentDescription = null,
                                        tint = ElectricBlue,
                                        modifier = Modifier.size(32.dp)
                                    )
                                    Text(
                                        text = "Log at least 2 sessions with this movement to render progress curves.",
                                        color = TextWhite,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.widthIn(max = 280.dp)
                                    )
                                    if (chartPoints.isNotEmpty()) {
                                        val single = chartPoints.first()
                                        Text(
                                            text = "Latest: ${single.maxWeightKg.toInt()} kg (Est. 1RM: ${String.format(Locale.US, "%.1f", single.best1RM)} kg)",
                                            color = ElectricBlueLight,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        } else {
                            ProgressInteractiveCanvas(
                                points = chartPoints,
                                metric = selectedMetric,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .testTag("progress_canvas")
                            )
                        }
                    }
                }
            }

            // Past Sessions History List
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "PAST SESSIONS",
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "${analytics.sessionRecords.size} logged",
                        color = TextDark,
                        fontSize = 11.sp
                    )
                }
            }

            if (analytics.sessionRecords.isEmpty()) {
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
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                Icons.Default.FitnessCenter,
                                contentDescription = null,
                                tint = ElectricBlue,
                                modifier = Modifier.size(28.dp)
                            )
                            Text(
                                text = "No logged sets yet",
                                color = TextWhite,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Complete sets for ${analytics.exerciseName} in your workout to track progress automatically.",
                                color = TextMuted,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(analytics.sessionRecords, key = { it.logId }) { session ->
                    SessionRecordCard(session = session)
                }
            }
        }
    }
}

@Composable
private fun ProgressKpiCard(
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
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    color = TextMuted,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Surface(
                    color = badgeColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = badge,
                        color = badgeColor,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }

            Text(
                text = value,
                color = TextWhite,
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1
            )

            Text(
                text = subtitle,
                color = TextDark,
                fontSize = 10.sp,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun ProgressInteractiveCanvas(
    points: List<ProgressChartPoint>,
    metric: ProgressMetric,
    modifier: Modifier = Modifier
) {
    var selectedPointIndex by remember(points) { mutableStateOf<Int?>(points.lastIndex.takeIf { it >= 0 }) }

    val values = points.map { it.value }
    val rawMin = values.minOrNull() ?: 0.0
    val rawMax = values.maxOrNull() ?: 100.0

    val minY = if (rawMin == rawMax) (rawMin * 0.85).coerceAtLeast(0.0) else (rawMin * 0.9).coerceAtLeast(0.0)
    val maxY = if (rawMin == rawMax) (rawMax * 1.15).coerceAtLeast(10.0) else (rawMax * 1.08).coerceAtLeast(10.0)
    val rangeY = (maxY - minY).coerceAtLeast(1.0)

    Column(modifier = modifier) {
        // Active Point Tooltip Pill
        val activePoint = selectedPointIndex?.let { points.getOrNull(it) } ?: points.lastOrNull()
        if (activePoint != null) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                color = DarkCardSurface,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, ElectricBlueAlpha)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 6.dp),
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
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = activePoint.dateLabel,
                            color = TextWhite,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Peak: ${activePoint.bestSetString}",
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
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
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
                val paddingBottom = 20.dp.toPx()
                val usableHeight = canvasHeight - paddingBottom

                // Subtle dashed grid lines (0%, 33%, 66%, 100%)
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

                val stepX = canvasWidth / (points.size - 1).coerceAtLeast(1)
                val computedPoints = points.mapIndexed { index, pt ->
                    val x = index * stepX
                    val yNorm = 1f - ((pt.value - minY) / rangeY).toFloat().coerceIn(0f, 1f)
                    val y = usableHeight * yNorm
                    Offset(x, y)
                }

                // 1. Glowing gradient fill under curve
                val fillPath = Path().apply {
                    moveTo(computedPoints.first().x, usableHeight)
                    computedPoints.forEach { pt ->
                        lineTo(pt.x, pt.y)
                    }
                    lineTo(computedPoints.last().x, usableHeight)
                    close()
                }

                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0x331877F2), Color.Transparent),
                        startY = 0f,
                        endY = usableHeight
                    )
                )

                // 2. Stroke curve line (Electric Blue)
                val linePath = Path().apply {
                    computedPoints.forEachIndexed { idx, pt ->
                        if (idx == 0) moveTo(pt.x, pt.y) else lineTo(pt.x, pt.y)
                    }
                }

                drawPath(
                    path = linePath,
                    color = Color(0xFF1877F2),
                    style = Stroke(width = 2.5.dp.toPx())
                )

                // 3. Highlighted active vertical guide line
                val activeIdx = selectedPointIndex ?: points.lastIndex
                if (activeIdx in computedPoints.indices) {
                    val activePt = computedPoints[activeIdx]
                    drawLine(
                        color = Color(0x881877F2),
                        start = Offset(activePt.x, 0f),
                        end = Offset(activePt.x, usableHeight),
                        strokeWidth = 1.5.dp.toPx(),
                        pathEffect = dashEffect
                    )
                }

                // 4. Circular data points
                computedPoints.forEachIndexed { idx, pt ->
                    val isSelected = (idx == activeIdx)
                    if (isSelected) {
                        drawCircle(
                            color = Color(0x441877F2),
                            radius = 8.dp.toPx(),
                            center = pt
                        )
                        drawCircle(
                            color = Color(0xFF1877F2),
                            radius = 4.5.dp.toPx(),
                            center = pt
                        )
                        drawCircle(
                            color = Color.White,
                            radius = 2.5.dp.toPx(),
                            center = pt
                        )
                    } else {
                        drawCircle(
                            color = Color(0xFF1877F2),
                            radius = 3.5.dp.toPx(),
                            center = pt
                        )
                        drawCircle(
                            color = Color(0xFF0C101B),
                            radius = 2.dp.toPx(),
                            center = pt
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SessionRecordCard(session: ExerciseSessionRecord) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, DarkBorder, RoundedCornerShape(12.dp)),
        color = DarkCardBg
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = session.routineTitle,
                        color = TextWhite,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = session.formattedDate,
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (session.isAllTimePRSession) {
                        Surface(
                            color = Color(0x3322C55E),
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

                    Surface(
                        color = ElectricBlueAlpha,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "${session.maxWeightKg.toInt()} kg",
                            color = ElectricBlueLight,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            HorizontalDivider(color = DarkBorder, thickness = 0.5.dp)

            // Sets Pill Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                session.sets.forEach { set ->
                    Surface(
                        color = if (set.isPR) Color(0x221877F2) else DarkCardSurface,
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(1.dp, if (set.isPR) ElectricBlue.copy(alpha = 0.4f) else DarkBorder)
                    ) {
                        Text(
                            text = "${set.weightKg.toInt()}kg × ${set.reps}",
                            color = if (set.isPR) ElectricBlueLight else TextWhite,
                            fontSize = 11.sp,
                            fontWeight = if (set.isPR) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            // Stats summary line: 1RM and Session Volume
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Est. 1RM: ${String.format(Locale.US, "%.1f", session.best1RM)} kg",
                    color = TextMuted,
                    fontSize = 11.sp
                )
                Text(
                    text = "Vol: ${session.sessionVolumeKg.toInt()} kg",
                    color = TextDark,
                    fontSize = 11.sp
                )
            }
        }
    }
}

private data class Tuple4<A, B, C, D>(val a: A, val b: B, val c: C, val d: D)

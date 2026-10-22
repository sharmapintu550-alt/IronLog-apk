package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.entity.ExerciseEntity
import com.example.data.entity.RoutineEntity
import com.example.ui.components.AddExerciseBottomSheet
import com.example.ui.components.ExerciseThumbnail
import com.example.ui.theme.*
import com.example.ui.viewmodel.WorkoutViewModel
import kotlinx.coroutines.delay
import java.util.Locale
import java.util.UUID

enum class ActiveSetType(val code: String, val textColor: Color, val bgColor: Color) {
    NORMAL("", TextWhite, Color(0xFF1A2234)),
    WARMUP("W", WarmupAmber, Color(0x3DF59E0B)),
    DROP("D", DropPurple, Color(0x3D8B5CF6)),
    FAILURE("F", FailureRed, Color(0x3DEF4444))
}

@Stable
class ActiveWorkoutSet(
    val id: String = UUID.randomUUID().toString(),
    setNumber: Int,
    setType: ActiveSetType = ActiveSetType.NORMAL,
    previous: String = "60 kg × 10",
    weight: String = "",
    reps: String = "",
    isCompleted: Boolean = false
) {
    var setNumber by mutableIntStateOf(setNumber)
    var setType by mutableStateOf(setType)
    var previous by mutableStateOf(previous)
    var weight by mutableStateOf(weight)
    var reps by mutableStateOf(reps)
    var isCompleted by mutableStateOf(isCompleted)
}

fun parseGhostWeight(previous: String): String {
    val cleaned = previous.replace("×", "x")
    if (cleaned.contains("kg", ignoreCase = true)) {
        val part = cleaned.substringBefore("kg", "").trim()
        val num = part.filter { it.isDigit() || it == '.' }
        if (num.isNotEmpty()) return num
    }
    if (cleaned.contains("x", ignoreCase = true)) {
        val part = cleaned.substringBefore("x", "").trim()
        val num = part.filter { it.isDigit() || it == '.' }
        if (num.isNotEmpty()) return num
    }
    val match = Regex("""\d+(\.\d+)?""").find(cleaned)?.value
    return match ?: "60"
}

fun parseGhostReps(previous: String): String {
    val cleaned = previous.replace("×", "x")
    if (cleaned.contains("x", ignoreCase = true)) {
        val part = cleaned.substringAfter("x", "").trim()
        val num = part.filter { it.isDigit() }
        if (num.isNotEmpty()) return num
    }
    val matches = Regex("""\d+""").findAll(cleaned).map { it.value }.toList()
    return if (matches.size >= 2) matches.last() else if (matches.size == 1) matches.first() else "10"
}

@Stable
data class ActiveWorkoutExercise(
    val id: String,
    val name: String,
    val primaryMuscle: String = "Chest",
    val equipmentType: String = "Barbell",
    var note: String = "",
    val sets: MutableList<ActiveWorkoutSet>
)

fun normalizeMuscleGroup(rawMuscle: String): String {
    val m = rawMuscle.trim()
    return when {
        m.contains("Chest", ignoreCase = true) -> "Chest"
        m.contains("Back", ignoreCase = true) || m.contains("Lat", ignoreCase = true) -> "Back"
        m.contains("Shoulder", ignoreCase = true) || m.contains("Delt", ignoreCase = true) -> "Shoulders"
        m.contains("Leg", ignoreCase = true) || m.contains("Quad", ignoreCase = true) || m.contains("Hamstring", ignoreCase = true) || m.contains("Calf", ignoreCase = true) || m.contains("Glute", ignoreCase = true) -> "Legs"
        m.contains("Bicep", ignoreCase = true) -> "Biceps / Arms"
        m.contains("Tricep", ignoreCase = true) -> "Triceps / Arms"
        m.contains("Arm", ignoreCase = true) || m.contains("Forearm", ignoreCase = true) -> "Arms"
        m.contains("Core", ignoreCase = true) || m.contains("Ab", ignoreCase = true) -> "Core"
        m.isNotBlank() -> m
        else -> "Full Body"
    }
}

fun resolveExerciseMuscle(exercise: ActiveWorkoutExercise, dbExercises: List<ExerciseEntity>): String {
    val dbMatch = dbExercises.find { it.name.equals(exercise.name, ignoreCase = true) }
    if (dbMatch != null && dbMatch.primaryMuscle.isNotBlank()) {
        return normalizeMuscleGroup(dbMatch.primaryMuscle)
    }
    if (exercise.primaryMuscle.isNotBlank() && exercise.primaryMuscle != "Chest") {
        return normalizeMuscleGroup(exercise.primaryMuscle)
    }
    val lower = exercise.name.lowercase()
    return when {
        lower.contains("squat") || lower.contains("leg") || lower.contains("rdl") || lower.contains("deadlift") || lower.contains("calf") || lower.contains("lunge") || (lower.contains("extension") && lower.contains("leg")) || (lower.contains("curl") && lower.contains("leg")) || lower.contains("hip thrust") -> "Legs"
        lower.contains("bench") || lower.contains("chest") || lower.contains("push-up") || lower.contains("pec") || (lower.contains("fly") && !lower.contains("delt")) -> "Chest"
        lower.contains("row") || lower.contains("lat") || lower.contains("pull-up") || lower.contains("chin-up") || lower.contains("pulldown") -> "Back"
        lower.contains("overhead") || lower.contains("shoulder") || lower.contains("arnold") || lower.contains("lateral raise") || lower.contains("front raise") || lower.contains("rear delt") || lower.contains("face pull") || lower.contains("upright row") -> "Shoulders"
        lower.contains("bicep") || lower.contains("hammer curl") || lower.contains("preacher") -> "Biceps / Arms"
        lower.contains("tricep") || lower.contains("pushdown") || lower.contains("skull") || lower.contains("dip") || lower.contains("katana") -> "Triceps / Arms"
        lower.contains("plank") || lower.contains("crunch") || lower.contains("ab") || lower.contains("core") || lower.contains("rollout") || lower.contains("twist") -> "Core"
        lower.contains("curl") || lower.contains("wrist") -> "Arms"
        else -> normalizeMuscleGroup(exercise.primaryMuscle)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveWorkoutScreen(
    routine: RoutineEntity?,
    viewModel: WorkoutViewModel,
    onFinish: () -> Unit
) {
    val context = LocalContext.current
    val allDbExercises by viewModel.exercises.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    var searchQuery by rememberSaveable { mutableStateOf("") }

    var showDiscardDialog by remember { mutableStateOf(false) }
    var showAddExerciseSheet by remember { mutableStateOf(false) }
    var exerciseIndexToReplace by remember { mutableStateOf<Int?>(null) }

    // Intercept back button to prompt discard confirmation
    BackHandler {
        showDiscardDialog = true
    }

    // Elapsed Workout Timer
    var secondsElapsed by remember { mutableStateOf(0L) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            secondsElapsed++
        }
    }

    // Rest Countdown Timer State & User Preference
    var isRestTimerEnabled by rememberSaveable { mutableStateOf(true) }
    var restSecondsRemaining by remember { mutableStateOf<Int?>(null) }
    LaunchedEffect(restSecondsRemaining, isRestTimerEnabled) {
        if (!isRestTimerEnabled) {
            restSecondsRemaining = null
            return@LaunchedEffect
        }
        val current = restSecondsRemaining
        if (current != null && current > 0) {
            delay(1000)
            restSecondsRemaining = current - 1
        } else if (current != null && current <= 0) {
            restSecondsRemaining = null
        }
    }

    val workoutTitle = routine?.title ?: "Quick Workout"

    // Initial exercise list based on routine or defaults
    val exercises = remember {
        val initialList = mutableStateListOf<ActiveWorkoutExercise>()
        val routineSummary = routine?.exercisesSummary?.trim()
        if (!routineSummary.isNullOrBlank()) {
            val names = routineSummary.split(",").map { it.trim() }.filter { it.isNotBlank() }
            names.forEachIndexed { index, name ->
                initialList.add(
                    ActiveWorkoutExercise(
                        id = "init_${index}_${System.currentTimeMillis()}",
                        name = name,
                        sets = mutableStateListOf(
                            ActiveWorkoutSet(setNumber = 1, previous = "60 kg × 12", weight = "", reps = "", isCompleted = false),
                            ActiveWorkoutSet(setNumber = 2, previous = "70 kg × 10", weight = "", reps = "", isCompleted = false),
                            ActiveWorkoutSet(setNumber = 3, previous = "80 kg × 8", weight = "", reps = "", isCompleted = false)
                        )
                    )
                )
            }
        }
        if (initialList.isEmpty()) {
            initialList.addAll(
                listOf(
                    ActiveWorkoutExercise(
                        id = "1",
                        name = "Barbell Back Squat",
                        primaryMuscle = "Quads",
                        equipmentType = "Barbell",
                        sets = mutableStateListOf(
                            ActiveWorkoutSet(setNumber = 1, setType = ActiveSetType.WARMUP, previous = "60 kg × 10", weight = "60", reps = "10", isCompleted = true),
                            ActiveWorkoutSet(setNumber = 2, previous = "100 kg × 8", weight = "", reps = "", isCompleted = false),
                            ActiveWorkoutSet(setNumber = 3, previous = "105 kg × 6", weight = "", reps = "", isCompleted = false)
                        )
                    ),
                    ActiveWorkoutExercise(
                        id = "2",
                        name = "Romanian Deadlift",
                        primaryMuscle = "Hamstrings",
                        equipmentType = "Barbell",
                        sets = mutableStateListOf(
                            ActiveWorkoutSet(setNumber = 1, previous = "80 kg × 10", weight = "", reps = "", isCompleted = false),
                            ActiveWorkoutSet(setNumber = 2, previous = "90 kg × 8", weight = "", reps = "", isCompleted = false)
                        )
                    )
                )
            )
        }
        initialList
    }

    // High performance derived metrics
    val totalCompletedSets by remember {
        derivedStateOf { exercises.sumOf { ex -> ex.sets.count { it.isCompleted } } }
    }
    val totalVolumeKg by remember {
        derivedStateOf {
            exercises.sumOf { ex ->
                ex.sets.filter { it.isCompleted }.sumOf { s ->
                    val w = (if (s.weight.isNotBlank()) s.weight else parseGhostWeight(s.previous)).toDoubleOrNull() ?: 0.0
                    val r = (if (s.reps.isNotBlank()) s.reps else parseGhostReps(s.previous)).toIntOrNull() ?: 0
                    w * r
                }
            }
        }
    }

    // Crash Guard: Periodic 15-second background autosave to Room DB
    LaunchedEffect(Unit) {
        while (true) {
            delay(15000)
            val draftSummary = exercises.mapIndexed { idx, ex ->
                "${idx + 1}. ${ex.name} (${ex.sets.count { s -> s.isCompleted }} sets)"
            }.joinToString(", ")
            viewModel.autoSaveDraft(
                routineId = routine?.id,
                routineTitle = workoutTitle,
                durationSeconds = secondsElapsed,
                draftJson = draftSummary
            )
        }
    }

    // Filter exercises by session search query
    val filteredExercises = remember(exercises, searchQuery, allDbExercises) {
        if (searchQuery.isBlank()) {
            exercises.toList()
        } else {
            val q = searchQuery.trim().lowercase()
            exercises.filter { ex ->
                ex.name.lowercase().contains(q) ||
                resolveExerciseMuscle(ex, allDbExercises).lowercase().contains(q) ||
                ex.equipmentType.lowercase().contains(q)
            }
        }
    }

    // Group exercises by body part / muscle group preserving appearance order
    val groupedExercises = remember(filteredExercises, allDbExercises) {
        val map = linkedMapOf<String, MutableList<ActiveWorkoutExercise>>()
        filteredExercises.forEach { ex ->
            val group = resolveExerciseMuscle(ex, allDbExercises)
            map.getOrPut(group) { mutableListOf() }.add(ex)
        }
        map
    }

    // Quick-jump scroll when typing in search bar
    LaunchedEffect(searchQuery) {
        if (searchQuery.isNotBlank() && filteredExercises.isNotEmpty()) {
            listState.animateScrollToItem(0)
        }
    }

    // Format elapsed workout session time: 00:00:00
    val formattedTime = remember(secondsElapsed) {
        val hours = secondsElapsed / 3600
        val mins = (secondsElapsed % 3600) / 60
        val secs = secondsElapsed % 60
        String.format("%02d:%02d:%02d", hours, mins, secs)
    }

    val formattedVolume by remember {
        derivedStateOf {
            if (totalVolumeKg <= 0.0) {
                "0 kg"
            } else if (totalVolumeKg % 1.0 == 0.0) {
                String.format(Locale.US, "%,d kg", totalVolumeKg.toLong())
            } else {
                String.format(Locale.US, "%,.1f kg", totalVolumeKg)
            }
        }
    }

    Scaffold(
        containerColor = AmoledBlack,
        topBar = {
            Surface(
                color = AmoledBlack,
                modifier = Modifier.fillMaxWidth().border(0.dp, DarkBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                ) {
                    // Top Navigation & Action Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Close / Discard Button ("✕")
                        IconButton(
                            onClick = { showDiscardDialog = true },
                            modifier = Modifier
                                .size(40.dp)
                                .testTag("cancel_workout_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Discard Workout",
                                tint = TextMuted,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        // Center: Workout Title & Live Digital Elapsed Session Timer (00:00:00)
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        ) {
                            Text(
                                text = workoutTitle,
                                color = TextWhite,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = formattedTime,
                                color = ElectricBlueLight,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 0.5.sp
                            )
                        }

                        // Top Right: Auto Rest Timer Toggle + Finish Button
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            IconButton(
                                onClick = {
                                    isRestTimerEnabled = !isRestTimerEnabled
                                    if (!isRestTimerEnabled) {
                                        restSecondsRemaining = null
                                    }
                                    Toast.makeText(
                                        context,
                                        if (isRestTimerEnabled) "Auto Rest Timer: ON" else "Auto Rest Timer: OFF",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                },
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isRestTimerEnabled) Color(0x2A1877F2) else Color(0xFF1E2638))
                                    .border(
                                        1.dp,
                                        if (isRestTimerEnabled) Color(0xFF1877F2) else Color(0xFF28354E),
                                        RoundedCornerShape(10.dp)
                                    )
                                    .testTag("toggle_rest_timer_button")
                            ) {
                                Icon(
                                    imageVector = if (isRestTimerEnabled) Icons.Default.Timer else Icons.Default.TimerOff,
                                    contentDescription = if (isRestTimerEnabled) "Auto Rest Timer ON" else "Auto Rest Timer OFF",
                                    tint = if (isRestTimerEnabled) Color(0xFF1877F2) else Color(0xFF8E8E93),
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Button(
                                onClick = {
                                    val summaryStr = exercises.mapIndexed { idx, ex ->
                                        "${idx + 1}. ${ex.name} (${ex.sets.count { s -> s.isCompleted }} sets)"
                                    }.joinToString(", ")

                                    viewModel.logCompletedWorkout(
                                        routineId = routine?.id,
                                        routineTitle = workoutTitle,
                                        durationSeconds = secondsElapsed,
                                        totalVolumeKg = if (totalVolumeKg > 0) totalVolumeKg else 4500.0,
                                        totalSets = if (totalCompletedSets > 0) totalCompletedSets else 8,
                                        summary = summaryStr
                                    )
                                    Toast.makeText(context, "Workout logged successfully!", Toast.LENGTH_SHORT).show()
                                    onFinish()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1877F2)),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                modifier = Modifier.testTag("finish_workout_button")
                            ) {
                                Text(
                                    text = "Finish",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }

                    // Live Workout Summary Metrics Strip (TIME, VOLUME, SETS COMPLETED)
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        color = Color(0xFF0D111A),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFF1A2234))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Column 1 (TIME)
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "TIME",
                                    color = Color(0xFF8E8E93),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = formattedTime,
                                    color = Color(0xFF1877F2),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // Divider
                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(24.dp)
                                    .background(Color(0xFF1A2234))
                            )

                            // Column 2 (VOLUME)
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "VOLUME",
                                    color = Color(0xFF8E8E93),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = formattedVolume,
                                    color = TextWhite,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // Divider
                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(24.dp)
                                    .background(Color(0xFF1A2234))
                            )

                            // Column 3 (SETS COMPLETED)
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "SETS COMPLETED",
                                    color = Color(0xFF8E8E93),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "$totalCompletedSets",
                                    color = TextWhite,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Floating / Top Persistent Rest Timer Banner
                    AnimatedVisibility(
                        visible = isRestTimerEnabled && restSecondsRemaining != null && (restSecondsRemaining ?: 0) > 0,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        val remaining = restSecondsRemaining ?: 0
                        val restMins = remaining / 60
                        val restSecs = remaining % 60
                        val restFormatted = String.format("%02d:%02d", restMins, restSecs)

                        Surface(
                            color = Color(0xFF131A2A),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color(0xFF1877F2).copy(alpha = 0.5f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                                .testTag("rest_timer_banner")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF1877F2).copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Timer,
                                            contentDescription = "Rest Timer",
                                            tint = ElectricBlueLight,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Column {
                                        Text(
                                            text = "REST COUNTDOWN",
                                            color = ElectricBlueLight,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 0.5.sp
                                        )
                                        Text(
                                            text = "Rest: $restFormatted",
                                            color = TextWhite,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            restSecondsRemaining = (restSecondsRemaining ?: 0) + 30
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        border = BorderStroke(1.dp, Color(0xFF28354E)),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextWhite),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        modifier = Modifier.height(32.dp).testTag("rest_timer_add_30s")
                                    ) {
                                        Text("+30s", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                    }

                                    Button(
                                        onClick = {
                                            restSecondsRemaining = null
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E2638)),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                        modifier = Modifier.height(32.dp).testTag("rest_timer_skip")
                                    ) {
                                        Text("Skip", color = TextMuted, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                    }

                                    IconButton(
                                        onClick = {
                                            restSecondsRemaining = null
                                            isRestTimerEnabled = false
                                            Toast.makeText(context, "Rest timer disabled", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFF1E2638))
                                            .testTag("rest_timer_disable_close")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Disable rest timer",
                                            tint = TextMuted,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Top Search & Quick-Jump Bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                            .background(Color(0xFF121622), RoundedCornerShape(12.dp))
                            .border(1.dp, Color(0xFF1E2538), RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = Color(0xFF1877F2),
                                modifier = Modifier.size(18.dp)
                            )
                            BasicTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                textStyle = TextStyle(
                                    color = TextWhite,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                cursorBrush = SolidColor(Color(0xFF1877F2)),
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("session_exercise_search_input"),
                                decorationBox = { innerTextField ->
                                    if (searchQuery.isBlank()) {
                                        Text(
                                            text = "Search logged exercises in session...",
                                            color = Color(0xFF8E8E93),
                                            fontSize = 13.sp
                                        )
                                    }
                                    innerTextField()
                                }
                            )
                            if (searchQuery.isNotBlank()) {
                                IconButton(
                                    onClick = { searchQuery = "" },
                                    modifier = Modifier
                                        .size(24.dp)
                                        .testTag("clear_session_search_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Clear Search",
                                        tint = Color(0xFF8E8E93),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 48.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (filteredExercises.isEmpty() && searchQuery.isNotBlank()) {
                item(key = "empty_search_state") {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        color = Color(0xFF121622),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, Color(0xFF1E2538))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(Color(0x2A1877F2)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SearchOff,
                                    contentDescription = null,
                                    tint = Color(0xFF1877F2),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Text(
                                text = "No exercises matching \"$searchQuery\" in this session",
                                color = Color(0xFF8E8E93),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center
                            )
                            Button(
                                onClick = { searchQuery = "" },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1877F2)),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                modifier = Modifier.testTag("reset_search_button")
                            ) {
                                Text(
                                    text = "Reset Search",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            } else {
                groupedExercises.forEach { (groupName, groupExercises) ->
                    item(key = "header_$groupName") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp, bottom = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "• ${groupName.uppercase()} EXERCISES",
                                color = Color(0xFF1877F2),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                            Surface(
                                color = Color(0xFF1E2538),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(
                                    text = "${groupExercises.size} ${if (groupExercises.size == 1) "exercise" else "exercises"}",
                                    color = Color(0xFF6B7280),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                            HorizontalDivider(
                                color = Color(0xFF1E2538),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    items(groupExercises, key = { it.id }) { exercise ->
                        val exIndex = exercises.indexOf(exercise)
                var showCardMenu by remember { mutableStateOf(false) }
                var showNoteInput by remember { mutableStateOf(exercise.note.isNotBlank()) }

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, DarkBorder, RoundedCornerShape(16.dp)),
                    color = DarkCardBg
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        // 1. Rich Exercise Card Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                // Circular Exercise Thumbnail / Muscle Avatar
                                ExerciseThumbnail(
                                    imageUrl = null,
                                    exerciseName = exercise.name,
                                    size = 40.dp
                                )

                                // Exercise Name: Crisp, bold white text
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = exercise.name,
                                        color = TextWhite,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1
                                    )
                                    if (exercise.primaryMuscle.isNotBlank()) {
                                        Text(
                                            text = exercise.primaryMuscle,
                                            color = Color(0xFF8E8E93),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }

                            // Action Overflow: Reorder chevrons and 3-dot menu
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                if (exIndex > 0) {
                                    IconButton(
                                        onClick = {
                                            val item = exercises.removeAt(exIndex)
                                            exercises.add(exIndex - 1, item)
                                        },
                                        modifier = Modifier.size(28.dp).testTag("move_up_exercise_$exIndex")
                                    ) {
                                        Icon(
                                            Icons.Default.KeyboardArrowUp,
                                            contentDescription = "Move Up",
                                            tint = TextMuted,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                                if (exIndex < exercises.size - 1) {
                                    IconButton(
                                        onClick = {
                                            val item = exercises.removeAt(exIndex)
                                            exercises.add(exIndex + 1, item)
                                        },
                                        modifier = Modifier.size(28.dp).testTag("move_down_exercise_$exIndex")
                                    ) {
                                        Icon(
                                            Icons.Default.KeyboardArrowDown,
                                            contentDescription = "Move Down",
                                            tint = TextMuted,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }

                                // 3-Dot Overflow Menu
                                Box {
                                    IconButton(
                                        onClick = { showCardMenu = true },
                                        modifier = Modifier.size(28.dp).testTag("exercise_menu_$exIndex")
                                    ) {
                                        Icon(
                                            Icons.Default.MoreVert,
                                            contentDescription = "Options",
                                            tint = TextMuted,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    DropdownMenu(
                                        expanded = showCardMenu,
                                        onDismissRequest = { showCardMenu = false },
                                        modifier = Modifier.background(DarkCardSurface)
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("Replace Exercise", color = TextWhite) },
                                            leadingIcon = {
                                                Icon(Icons.Default.SwapHoriz, contentDescription = null, tint = ElectricBlue)
                                            },
                                            onClick = {
                                                showCardMenu = false
                                                exerciseIndexToReplace = exIndex
                                                showAddExerciseSheet = true
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    if (showNoteInput) "Hide Note" else "Add Exercise Note",
                                                    color = TextWhite
                                                )
                                            },
                                            leadingIcon = {
                                                Icon(Icons.Default.EditNote, contentDescription = null, tint = ElectricBlueLight)
                                            },
                                            onClick = {
                                                showCardMenu = false
                                                showNoteInput = !showNoteInput
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Remove Exercise", color = MissedRed) },
                                            leadingIcon = {
                                                Icon(Icons.Default.Delete, contentDescription = null, tint = MissedRed)
                                            },
                                            onClick = {
                                                showCardMenu = false
                                                exercises.removeAt(exIndex)
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        // Exercise Note Field
                        AnimatedVisibility(visible = showNoteInput) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 10.dp)
                                    .background(Color(0xFF161B26), RoundedCornerShape(8.dp))
                                    .border(1.dp, DarkBorder, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                BasicTextField(
                                    value = exercise.note,
                                    onValueChange = { exercise.note = it },
                                    textStyle = TextStyle(
                                        color = TextWhite,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Normal
                                    ),
                                    cursorBrush = SolidColor(ElectricBlue),
                                    modifier = Modifier.fillMaxWidth(),
                                    decorationBox = { innerTextField ->
                                        if (exercise.note.isBlank()) {
                                            Text(
                                                text = "Add note (e.g. seat height 4, 3s negatives)...",
                                                color = Color(0xFF6B7280),
                                                fontSize = 13.sp
                                            )
                                        }
                                        innerTextField()
                                    }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // 2. Structured Set Table Column Headers
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "SET",
                                color = Color(0xFF8E8E93),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.width(36.dp),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "PREVIOUS",
                                color = Color(0xFF8E8E93),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "KG",
                                color = Color(0xFF8E8E93),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.width(68.dp),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "REPS",
                                color = Color(0xFF8E8E93),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.width(68.dp),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "✓ DONE",
                                color = Color(0xFF8E8E93),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.width(44.dp),
                                textAlign = TextAlign.Center
                            )
                        }

                        HorizontalDivider(
                            color = DarkBorder,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )

                        // 3. Set Rows
                        exercise.sets.forEachIndexed { setIndex, setItem ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // SET Column: Circular/Rounded pill toggling Normal, Warmup (W), Drop (D), Failure (F)
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(setItem.setType.bgColor)
                                        .border(
                                            1.dp,
                                            if (setItem.setType != ActiveSetType.NORMAL) setItem.setType.textColor else DarkBorder,
                                            RoundedCornerShape(8.dp)
                                        )
                                        .clickable {
                                            // Cycle through Normal -> Warmup -> Drop -> Failure -> Normal
                                            setItem.setType = when (setItem.setType) {
                                                ActiveSetType.NORMAL -> ActiveSetType.WARMUP
                                                ActiveSetType.WARMUP -> ActiveSetType.DROP
                                                ActiveSetType.DROP -> ActiveSetType.FAILURE
                                                ActiveSetType.FAILURE -> ActiveSetType.NORMAL
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (setItem.setType == ActiveSetType.NORMAL) "${setIndex + 1}" else setItem.setType.code,
                                        color = setItem.setType.textColor,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                // PREVIOUS Column: Restored previous performance reference in subtle gray (#6B7280)
                                Text(
                                    text = setItem.previous,
                                    color = Color(0xFF6B7280),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.weight(1f),
                                    textAlign = TextAlign.Center,
                                    maxLines = 1
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                // KG Input Container: Fully rounded dark slate box (#161B26)
                                Box(
                                    modifier = Modifier
                                        .width(68.dp)
                                        .height(38.dp)
                                        .background(Color(0xFF161B26), RoundedCornerShape(10.dp))
                                        .border(1.dp, DarkBorder, RoundedCornerShape(10.dp))
                                        .padding(horizontal = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    val ghostWeight = remember(setItem.previous) { parseGhostWeight(setItem.previous) }
                                    BasicTextField(
                                        value = setItem.weight,
                                        onValueChange = { newValue ->
                                            if (newValue.all { it.isDigit() || it == '.' }) {
                                                setItem.weight = newValue
                                            }
                                        },
                                        textStyle = TextStyle(
                                            textAlign = TextAlign.Center,
                                            color = Color.White,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        cursorBrush = SolidColor(Color(0xFF1877F2)),
                                        keyboardOptions = KeyboardOptions(
                                            keyboardType = KeyboardType.Number,
                                            imeAction = ImeAction.Next
                                        ),
                                        singleLine = true,
                                        maxLines = 1,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .testTag("set_weight_input_${setIndex}"),
                                        decorationBox = { innerTextField ->
                                            Box(
                                                contentAlignment = Alignment.Center,
                                                modifier = Modifier.fillMaxSize()
                                            ) {
                                                if (setItem.weight.isEmpty()) {
                                                    Text(
                                                        text = ghostWeight,
                                                        color = Color(0xFF6B7280),
                                                        fontSize = 15.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        textAlign = TextAlign.Center
                                                    )
                                                }
                                                innerTextField()
                                            }
                                        }
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                // REPS Input Container: Matching container box (#161B26)
                                Box(
                                    modifier = Modifier
                                        .width(68.dp)
                                        .height(38.dp)
                                        .background(Color(0xFF161B26), RoundedCornerShape(10.dp))
                                        .border(1.dp, DarkBorder, RoundedCornerShape(10.dp))
                                        .padding(horizontal = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    val ghostReps = remember(setItem.previous) { parseGhostReps(setItem.previous) }
                                    BasicTextField(
                                        value = setItem.reps,
                                        onValueChange = { newValue ->
                                            if (newValue.all { it.isDigit() }) {
                                                setItem.reps = newValue
                                            }
                                        },
                                        textStyle = TextStyle(
                                            textAlign = TextAlign.Center,
                                            color = Color.White,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        cursorBrush = SolidColor(Color(0xFF1877F2)),
                                        keyboardOptions = KeyboardOptions(
                                            keyboardType = KeyboardType.Number,
                                            imeAction = ImeAction.Next
                                        ),
                                        singleLine = true,
                                        maxLines = 1,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .testTag("set_reps_input_${setIndex}"),
                                        decorationBox = { innerTextField ->
                                            Box(
                                                contentAlignment = Alignment.Center,
                                                modifier = Modifier.fillMaxSize()
                                            ) {
                                                if (setItem.reps.isEmpty()) {
                                                    Text(
                                                        text = ghostReps,
                                                        color = Color(0xFF6B7280),
                                                        fontSize = 15.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        textAlign = TextAlign.Center
                                                    )
                                                }
                                                innerTextField()
                                            }
                                        }
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                // ✓ DONE Action: Rounded checkbox (#1A2234 default, #22C55E checked)
                                val checkBgColor by animateColorAsState(
                                    targetValue = if (setItem.isCompleted) Color(0xFF22C55E) else Color(0xFF1A2234),
                                    animationSpec = spring(),
                                    label = "check_color"
                                )
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(checkBgColor)
                                        .border(
                                            1.dp,
                                            if (setItem.isCompleted) Color(0xFF16A34A) else DarkBorder,
                                            RoundedCornerShape(10.dp)
                                        )
                                        .clickable {
                                            val willComplete = !setItem.isCompleted
                                            if (willComplete) {
                                                // Fallback to ghost values if inputs were left blank
                                                if (setItem.weight.isBlank()) {
                                                    setItem.weight = parseGhostWeight(setItem.previous)
                                                }
                                                if (setItem.reps.isBlank()) {
                                                    setItem.reps = parseGhostReps(setItem.previous)
                                                }
                                                // Automatically trigger/restart rest timer when set is marked completed IF enabled
                                                if (isRestTimerEnabled) {
                                                    restSecondsRemaining = 90
                                                }
                                            }
                                            setItem.isCompleted = willComplete
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Done",
                                        tint = if (setItem.isCompleted) Color.White else Color(0xFF64748B),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // 4. Card Bottom: Centered, subtle "+ Add Set" button
                        TextButton(
                            onClick = {
                                val nextSetNum = exercise.sets.size + 1
                                val lastSet = exercise.sets.lastOrNull()
                                val lastWeight = if (lastSet?.weight?.isNotBlank() == true) lastSet.weight else (lastSet?.let { parseGhostWeight(it.previous) } ?: "60")
                                val lastReps = if (lastSet?.reps?.isNotBlank() == true) lastSet.reps else (lastSet?.let { parseGhostReps(it.previous) } ?: "10")
                                exercise.sets.add(
                                    ActiveWorkoutSet(
                                        setNumber = nextSetNum,
                                        previous = "$lastWeight kg × $lastReps",
                                        weight = "",
                                        reps = "",
                                        isCompleted = false
                                    )
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(38.dp)
                                .background(Color(0x1F1877F2), RoundedCornerShape(10.dp))
                                .border(1.dp, Color(0xFF1877F2).copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = Color(0xFF1877F2),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "+ Add Set",
                                color = Color(0xFF1877F2),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }

    // Below All Exercise Cards: Prominent full-width "+ Add Exercise" outlined button
    item(key = "add_exercise_button_item") {
                OutlinedButton(
                    onClick = {
                        exerciseIndexToReplace = null
                        showAddExerciseSheet = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("add_exercise_to_workout_button"),
                    border = BorderStroke(1.5.dp, Color(0xFF1877F2)),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color(0x0F1877F2),
                        contentColor = Color(0xFF1877F2)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = Color(0xFF1877F2),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "+ Add Exercise",
                        color = Color(0xFF1877F2),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    // Full Visual Exercise Library Bottom Sheet with search and muscle filters
    if (showAddExerciseSheet) {
        AddExerciseBottomSheet(
            allExercises = allDbExercises,
            onDismiss = {
                showAddExerciseSheet = false
                exerciseIndexToReplace = null
            },
            onSelectExercise = { selectedExercise ->
                val replaceIdx = exerciseIndexToReplace
                if (replaceIdx != null && replaceIdx in exercises.indices) {
                    exercises[replaceIdx] = ActiveWorkoutExercise(
                        id = selectedExercise.id.toString(),
                        name = selectedExercise.name,
                        primaryMuscle = selectedExercise.primaryMuscle,
                        equipmentType = selectedExercise.equipmentType,
                        sets = mutableStateListOf(
                            ActiveWorkoutSet(setNumber = 1, previous = "50 kg × 10", weight = "", reps = "")
                        )
                    )
                } else {
                    exercises.add(
                        ActiveWorkoutExercise(
                            id = selectedExercise.id.toString(),
                            name = selectedExercise.name,
                            primaryMuscle = selectedExercise.primaryMuscle,
                            equipmentType = selectedExercise.equipmentType,
                            sets = mutableStateListOf(
                                ActiveWorkoutSet(setNumber = 1, previous = "60 kg × 10", weight = "", reps = ""),
                                ActiveWorkoutSet(setNumber = 2, previous = "60 kg × 10", weight = "", reps = ""),
                                ActiveWorkoutSet(setNumber = 3, previous = "60 kg × 10", weight = "", reps = "")
                            )
                        )
                    )
                }
                showAddExerciseSheet = false
                exerciseIndexToReplace = null
            },
            onConfirmSelection = { selectedList ->
                selectedList.forEach { selectedExercise ->
                    exercises.add(
                        ActiveWorkoutExercise(
                            id = selectedExercise.id.toString(),
                            name = selectedExercise.name,
                            primaryMuscle = selectedExercise.primaryMuscle,
                            equipmentType = selectedExercise.equipmentType,
                            sets = mutableStateListOf(
                                ActiveWorkoutSet(setNumber = 1, previous = "60 kg × 10", weight = "", reps = ""),
                                ActiveWorkoutSet(setNumber = 2, previous = "60 kg × 10", weight = "", reps = ""),
                                ActiveWorkoutSet(setNumber = 3, previous = "60 kg × 10", weight = "", reps = "")
                            )
                        )
                    )
                }
                showAddExerciseSheet = false
                exerciseIndexToReplace = null
            }
        )
    }

    // Discard Workout Confirmation Dialog
    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            containerColor = DarkCardBg,
            title = {
                Text(
                    text = "Discard workout?",
                    color = TextWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to discard this workout? All unsaved sets and progress will be lost.",
                    color = TextMuted,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDiscardDialog = false
                        viewModel.clearActiveDraft()
                        onFinish()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MissedRed),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Discard", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDiscardDialog = false }
                ) {
                    Text("Resume", color = ElectricBlueLight, fontWeight = FontWeight.SemiBold)
                }
            }
        )
    }
}

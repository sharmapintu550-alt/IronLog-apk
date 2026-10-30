package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.data.entity.ExerciseEntity
import com.example.data.entity.RoutineEntity
import com.example.ui.components.AddExerciseBottomSheet
import com.example.ui.components.ExerciseProgressBottomSheet
import com.example.ui.components.ExerciseThumbnail
import com.example.ui.theme.*
import com.example.ui.viewmodel.WorkoutViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import java.text.SimpleDateFormat
import java.util.Date
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
    previous: String = "—",
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
    if (previous.isBlank() || previous == "—" || previous == "-") return "0"
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
    return match ?: "0"
}

fun parseGhostReps(previous: String): String {
    if (previous.isBlank() || previous == "—" || previous == "-") return "0"
    val cleaned = previous.replace("×", "x")
    if (cleaned.contains("x", ignoreCase = true)) {
        val part = cleaned.substringAfter("x", "").trim()
        val num = part.filter { it.isDigit() }
        if (num.isNotEmpty()) return num
    }
    val matches = Regex("""\d+""").findAll(cleaned).map { it.value }.toList()
    return if (matches.size >= 2) matches.last() else if (matches.size == 1) matches.first() else "0"
}

data class BarbellPreset(
    val name: String,
    val shortName: String,
    val weightKg: Double,
    val weightLbs: Double
)

val BARBELL_PRESETS = listOf(
    BarbellPreset("Olympic Barbell", "Olympic Bar", 20.0, 45.0),
    BarbellPreset("Women's Olympic Barbell", "Women's Bar", 15.0, 33.0),
    BarbellPreset("EZ Curl Bar", "EZ Curl Bar", 10.0, 22.0),
    BarbellPreset("Standard Straight Bar", "Standard Bar", 7.5, 15.0),
    BarbellPreset("Trap / Hex Bar", "Trap Bar", 25.0, 55.0),
    BarbellPreset("Safety Squat Bar (SSB)", "Safety Squat Bar", 30.0, 65.0),
    BarbellPreset("Swiss / Multi-Grip Bar", "Swiss Bar", 18.0, 40.0),
    BarbellPreset("Smith Machine Bar", "Smith Machine", 0.0, 0.0)
)

fun isBarbellExercise(exercise: ActiveWorkoutExercise, dbExercises: List<ExerciseEntity>): Boolean {
    val dbMatch = dbExercises.find { it.name.equals(exercise.name, ignoreCase = true) || it.id == exercise.id }
    val eq = dbMatch?.equipment ?: dbMatch?.equipmentType ?: exercise.equipmentType
    if (eq.contains("Barbell", ignoreCase = true)) return true
    val name = exercise.name.lowercase()
    return name.contains("barbell") || name.contains("squat") || name.contains("bench press") || name.contains("deadlift") || name.contains("overhead press") || name.contains("clean") || name.contains("snatch")
}

fun formatBarbellLabel(weightKg: Double): String {
    val matched = BARBELL_PRESETS.find { kotlin.math.abs(it.weightKg - weightKg) < 0.01 }
    val weightStr = if (weightKg % 1.0 == 0.0) "${weightKg.toInt()} kg" else "$weightKg kg"
    return if (matched != null) {
        "🏋️ ${matched.shortName}: $weightStr [Edit]"
    } else {
        "🏋️ Custom Bar: $weightStr [Edit]"
    }
}

@Stable
data class ActiveWorkoutExercise(
    val id: String,
    val name: String,
    val primaryMuscle: String = "Chest",
    val equipmentType: String = "Barbell",
    var note: String = "",
    val sets: MutableList<ActiveWorkoutSet>,
    val initialBarbellWeightKg: Double = 20.0,
    val initialSetupMediaUri: String? = null,
    val initialMachineCalibration: String? = null,
    val initialIsCompleted: Boolean = false
) {
    var barbellWeightKg by mutableStateOf(initialBarbellWeightKg)
    var setupMediaUri by mutableStateOf(initialSetupMediaUri)
    var machineCalibration by mutableStateOf(initialMachineCalibration)
    var isCompleted by mutableStateOf(initialIsCompleted)
}

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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ActiveWorkoutScreen(
    routine: RoutineEntity?,
    viewModel: WorkoutViewModel,
    restoredState: com.example.data.entity.ActiveWorkoutStateEntity? = null,
    onMinimize: () -> Unit = {},
    onFinish: () -> Unit
) {
    val context = LocalContext.current
    val allDbExercises by viewModel.exercises.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var searchQuery by rememberSaveable { mutableStateOf("") }

    var showFinishModal by remember { mutableStateOf(false) }
    var showDiscardConfirmDialog by remember { mutableStateOf(false) }
    var showWorkoutHighlightsModal by remember { mutableStateOf(false) }
    var showWorkoutProgressModal by remember { mutableStateOf(false) }
    var showAddExerciseSheet by remember { mutableStateOf(false) }
    var exerciseIndexToReplace by remember { mutableStateOf<Int?>(null) }
    var exerciseTargetMuscleForSwap by remember { mutableStateOf<String?>(null) }
    var exerciseForBarbellSelection by remember { mutableStateOf<ActiveWorkoutExercise?>(null) }
    var exerciseForPlateCalculator by remember { mutableStateOf<ActiveWorkoutExercise?>(null) }
    var targetSetForPlateCalculator by remember { mutableStateOf<ActiveWorkoutSet?>(null) }
    var exerciseForWarmupCalculator by remember { mutableStateOf<ActiveWorkoutExercise?>(null) }
    var exerciseForCalibration by remember { mutableStateOf<ActiveWorkoutExercise?>(null) }
    var exerciseForMediaAttachment by remember { mutableStateOf<ActiveWorkoutExercise?>(null) }
    var exerciseForMediaViewing by remember { mutableStateOf<ActiveWorkoutExercise?>(null) }
    var showMediaAttachmentSheet by remember { mutableStateOf(false) }
    var pendingCaptureFile by remember { mutableStateOf<File?>(null) }
    var pendingCaptureUri by remember { mutableStateOf<Uri?>(null) }
    var analyticsExerciseName by remember { mutableStateOf<String?>(null) }
    var analyticsMuscleGroup by remember { mutableStateOf<String>("Other") }

    // Visual media picker (Photos & Videos from Device Gallery)
    val pickMediaLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        val targetEx = exerciseForMediaAttachment
        if (uri != null && targetEx != null) {
            coroutineScope.launch(Dispatchers.IO) {
                val savedUri = saveMediaToPrivateStorage(context, targetEx.id, uri)
                if (savedUri != null) {
                    withContext(Dispatchers.Main) {
                        targetEx.setupMediaUri = savedUri.toString()
                    }
                    viewModel.updateExerciseSetupMedia(targetEx.id, targetEx.name, savedUri.toString())
                }
            }
        }
        exerciseForMediaAttachment = null
    }

    // Camera photo capture
    val takePhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        val targetEx = exerciseForMediaAttachment
        val captureFile = pendingCaptureFile
        if (success && targetEx != null && captureFile != null && captureFile.exists() && captureFile.length() > 0) {
            val fileUri = Uri.fromFile(captureFile).toString()
            targetEx.setupMediaUri = fileUri
            viewModel.updateExerciseSetupMedia(targetEx.id, targetEx.name, fileUri)
        } else if (captureFile != null && (!success || captureFile.length() == 0L)) {
            captureFile.delete()
        }
        pendingCaptureFile = null
        pendingCaptureUri = null
        exerciseForMediaAttachment = null
    }

    // Camera video capture (for short looping form setup clips)
    val captureVideoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CaptureVideo()
    ) { success ->
        val targetEx = exerciseForMediaAttachment
        val captureFile = pendingCaptureFile
        if (success && targetEx != null && captureFile != null && captureFile.exists() && captureFile.length() > 0) {
            val fileUri = Uri.fromFile(captureFile).toString()
            targetEx.setupMediaUri = fileUri
            viewModel.updateExerciseSetupMedia(targetEx.id, targetEx.name, fileUri)
        } else if (captureFile != null && (!success || captureFile.length() == 0L)) {
            captureFile.delete()
        }
        pendingCaptureFile = null
        pendingCaptureUri = null
        exerciseForMediaAttachment = null
    }

    // Intercept back button: dismiss modals if visible, otherwise minimize active workout
    BackHandler {
        if (analyticsExerciseName != null) {
            analyticsExerciseName = null
        } else if (showWorkoutProgressModal) {
            showWorkoutProgressModal = false
        } else if (exerciseForCalibration != null) {
            exerciseForCalibration = null
        } else if (exerciseForMediaViewing != null) {
            exerciseForMediaViewing = null
        } else if (showMediaAttachmentSheet) {
            showMediaAttachmentSheet = false
            exerciseForMediaAttachment = null
        } else if (exerciseForWarmupCalculator != null) {
            exerciseForWarmupCalculator = null
        } else if (exerciseForPlateCalculator != null) {
            exerciseForPlateCalculator = null
            targetSetForPlateCalculator = null
        } else if (exerciseForBarbellSelection != null) {
            exerciseForBarbellSelection = null
        } else if (showWorkoutHighlightsModal) {
            onFinish()
        } else if (showFinishModal) {
            showFinishModal = false
        } else if (showDiscardConfirmDialog) {
            showDiscardConfirmDialog = false
        } else {
            onMinimize()
        }
    }

    val workoutTitle = routine?.title ?: restoredState?.routineName ?: "Quick Workout"
    val initialStart = restoredState?.startTimestamp ?: System.currentTimeMillis()
    val startTimestamp = rememberSaveable { initialStart }
    var isPaused by rememberSaveable { mutableStateOf(restoredState?.isPaused ?: false) }
    var pauseTimestamp by rememberSaveable { mutableStateOf(restoredState?.pauseTimestamp) }
    var totalPausedSeconds by rememberSaveable { mutableStateOf(restoredState?.totalPausedSeconds ?: 0L) }

    fun calculateCurrentDuration(): Long {
        if (startTimestamp <= 0L) return 0L
        val now = System.currentTimeMillis()
        val rawElapsed = (now - startTimestamp) / 1000L
        val ongoingPaused = if (isPaused && pauseTimestamp != null && pauseTimestamp!! > 0L) {
            (now - pauseTimestamp!!) / 1000L
        } else {
            0L
        }
        val effectivePaused = totalPausedSeconds + ongoingPaused
        return maxOf(0L, rawElapsed - effectivePaused)
    }

    // Start / ensure persistent Foreground Service tracking with wall-clock epoch timestamp
    LaunchedEffect(workoutTitle, startTimestamp, isPaused, pauseTimestamp, totalPausedSeconds) {
        try {
            com.example.service.WorkoutTrackingService.start(
                context = context,
                routineTitle = workoutTitle,
                startTimestamp = startTimestamp,
                isPaused = isPaused,
                pauseTimestamp = pauseTimestamp,
                totalPausedSeconds = totalPausedSeconds
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e("ActiveWorkout", "Failed to start tracking service: ${e.message}", e)
        }
    }

    // UI Ticker for top stats bar: purely queries continuous wall-clock epoch math
    var secondsElapsed by remember { mutableStateOf(calculateCurrentDuration()) }
    LaunchedEffect(startTimestamp, isPaused, pauseTimestamp, totalPausedSeconds) {
        try {
            secondsElapsed = calculateCurrentDuration()
            while (isActive) {
                delay(1000)
                secondsElapsed = calculateCurrentDuration()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e("ActiveWorkout", "Ticker error: ${e.message}", e)
        }
    }

    // Doze & Lock Screen Immunity: when app returns from background or screen is unlocked, immediately refresh duration
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, startTimestamp, isPaused, pauseTimestamp, totalPausedSeconds) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                secondsElapsed = calculateCurrentDuration()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
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

    // Initial exercise list based on restored state, routine, or defaults
    val exercises = remember {
        val initialList = mutableStateListOf<ActiveWorkoutExercise>()
        if (restoredState != null && restoredState.exercisesJson.isNotBlank()) {
            val restored = com.example.util.ActiveWorkoutSerializer.deserialize(restoredState.exercisesJson)
            if (restored.isNotEmpty()) {
                val sanitized = restored.map { ex ->
                    if (ex.name.contains("(") && ex.name.contains("set", ignoreCase = true)) {
                        val cleanName = ex.name.substringBefore("(").trim()
                        val matchedDb = allDbExercises.find { it.name.equals(cleanName, ignoreCase = true) }
                        ex.copy(
                            name = cleanName,
                            primaryMuscle = matchedDb?.primaryMuscle ?: ex.primaryMuscle,
                            equipmentType = matchedDb?.equipmentType ?: ex.equipmentType
                        )
                    } else {
                        ex
                    }
                }
                initialList.addAll(sanitized)
            }
        }
        if (initialList.isEmpty() && routine != null) {
            // First check routine.exercisesJson if present
            if (routine.exercisesJson.isNotBlank()) {
                val payload = com.example.util.QrCodeGenerator.parseInput(routine.exercisesJson)
                if (payload != null && payload.exercises.isNotEmpty()) {
                    payload.exercises.forEachIndexed { index, sharedEx ->
                        val cleanName = sharedEx.name.substringBefore("(").trim()
                        val matchedDb = allDbExercises.find { it.name.equals(cleanName, ignoreCase = true) }
                        val muscle = matchedDb?.primaryMuscle ?: sharedEx.muscle.ifBlank { "Full Body" }
                        val equip = matchedDb?.equipmentType ?: sharedEx.equipment.ifBlank { "Barbell" }
                        val prevStr = if (matchedDb != null && matchedDb.prWeight > 0.0 && matchedDb.prReps > 0) {
                            val prW = if (matchedDb.prWeight % 1.0 == 0.0) matchedDb.prWeight.toInt() else matchedDb.prWeight
                            "$prW kg × ${matchedDb.prReps}"
                        } else "—"
                        val setsList = if (sharedEx.sets.isNotEmpty()) {
                            sharedEx.sets.mapIndexed { sIdx, s ->
                                val wStr = if (s.weightKg > 0.0) {
                                    if (s.weightKg % 1.0 == 0.0) s.weightKg.toInt().toString() else s.weightKg.toString()
                                } else ""
                                val rStr = if (s.reps > 0) s.reps.toString() else ""
                                ActiveWorkoutSet(
                                    setNumber = sIdx + 1,
                                    previous = prevStr,
                                    weight = wStr,
                                    reps = rStr,
                                    isCompleted = false
                                )
                            }
                        } else {
                            val count = if (sharedEx.defaultSets > 0) sharedEx.defaultSets else 3
                            val targetR = sharedEx.targetReps.filter { it.isDigit() }.takeIf { it.isNotBlank() } ?: ""
                            (1..count).map { sIdx ->
                                ActiveWorkoutSet(
                                    setNumber = sIdx,
                                    previous = prevStr,
                                    weight = "",
                                    reps = targetR,
                                    isCompleted = false
                                )
                            }
                        }
                        initialList.add(
                            ActiveWorkoutExercise(
                                id = "init_${index}_${System.currentTimeMillis()}",
                                name = cleanName,
                                primaryMuscle = muscle,
                                equipmentType = equip,
                                note = matchedDb?.notes ?: "",
                                sets = mutableStateListOf<ActiveWorkoutSet>().apply { addAll(setsList) },
                                initialBarbellWeightKg = matchedDb?.barbellWeightKg ?: 20.0,
                                initialSetupMediaUri = matchedDb?.setupMediaUri,
                                initialMachineCalibration = matchedDb?.machineCalibration
                            )
                        )
                    }
                }
            }

            // Fallback: parse from routine.exercisesSummary without redundant set count in name
            if (initialList.isEmpty()) {
                val routineSummary = routine.exercisesSummary.trim()
                if (routineSummary.isNotBlank()) {
                    val setRegex = Regex("""^(.*?)\s*\((\d+)\s*sets?\)$""", RegexOption.IGNORE_CASE)
                    val items = routineSummary.split(",").map { it.trim() }.filter { it.isNotBlank() }
                    items.forEachIndexed { index, item ->
                        val match = setRegex.find(item)
                        val cleanName = (match?.groupValues?.get(1)?.trim() ?: item.substringBefore("(").trim()).ifBlank { item.trim() }
                        val setCount = match?.groupValues?.get(2)?.toIntOrNull() ?: 3
                        val matchedDb = allDbExercises.find { it.name.equals(cleanName, ignoreCase = true) }
                        val muscle = matchedDb?.primaryMuscle ?: "Full Body"
                        val equip = matchedDb?.equipmentType ?: "Barbell"

                        val prevStr = if (matchedDb != null && matchedDb.prWeight > 0.0 && matchedDb.prReps > 0) {
                            val prW = if (matchedDb.prWeight % 1.0 == 0.0) matchedDb.prWeight.toInt() else matchedDb.prWeight
                            "$prW kg × ${matchedDb.prReps}"
                        } else "—"

                        val setsList = (1..setCount).map { sIdx ->
                            ActiveWorkoutSet(
                                setNumber = sIdx,
                                previous = prevStr,
                                weight = "",
                                reps = "",
                                isCompleted = false
                            )
                        }
                        initialList.add(
                            ActiveWorkoutExercise(
                                id = "init_${index}_${System.currentTimeMillis()}",
                                name = cleanName,
                                primaryMuscle = muscle,
                                equipmentType = equip,
                                note = matchedDb?.notes ?: "",
                                sets = mutableStateListOf<ActiveWorkoutSet>().apply { addAll(setsList) },
                                initialBarbellWeightKg = matchedDb?.barbellWeightKg ?: 20.0,
                                initialSetupMediaUri = matchedDb?.setupMediaUri,
                                initialMachineCalibration = matchedDb?.machineCalibration
                            )
                        )
                    }
                }
            }
        }
        if (initialList.isEmpty()) {
            val squatDb = allDbExercises.find { it.name.equals("Barbell Back Squat", ignoreCase = true) }
            val rdlDb = allDbExercises.find { it.name.equals("Romanian Deadlift", ignoreCase = true) }
            val squatPrev = if (squatDb != null && squatDb.prWeight > 0.0 && squatDb.prReps > 0) {
                val prW = if (squatDb.prWeight % 1.0 == 0.0) squatDb.prWeight.toInt() else squatDb.prWeight
                "$prW kg × ${squatDb.prReps}"
            } else "—"
            val rdlPrev = if (rdlDb != null && rdlDb.prWeight > 0.0 && rdlDb.prReps > 0) {
                val prW = if (rdlDb.prWeight % 1.0 == 0.0) rdlDb.prWeight.toInt() else rdlDb.prWeight
                "$prW kg × ${rdlDb.prReps}"
            } else "—"
            initialList.addAll(
                listOf(
                    ActiveWorkoutExercise(
                        id = "1",
                        name = "Barbell Back Squat",
                        primaryMuscle = "Quads",
                        equipmentType = "Barbell",
                        note = squatDb?.notes ?: "",
                        sets = mutableStateListOf(
                            ActiveWorkoutSet(setNumber = 1, setType = ActiveSetType.WARMUP, previous = squatPrev, weight = "", reps = "", isCompleted = false),
                            ActiveWorkoutSet(setNumber = 2, previous = squatPrev, weight = "", reps = "", isCompleted = false),
                            ActiveWorkoutSet(setNumber = 3, previous = squatPrev, weight = "", reps = "", isCompleted = false)
                        ),
                        initialBarbellWeightKg = squatDb?.barbellWeightKg ?: 20.0,
                        initialSetupMediaUri = squatDb?.setupMediaUri,
                        initialMachineCalibration = squatDb?.machineCalibration
                    ),
                    ActiveWorkoutExercise(
                        id = "2",
                        name = "Romanian Deadlift",
                        primaryMuscle = "Hamstrings",
                        equipmentType = "Barbell",
                        note = rdlDb?.notes ?: "",
                        sets = mutableStateListOf(
                            ActiveWorkoutSet(setNumber = 1, previous = rdlPrev, weight = "", reps = "", isCompleted = false),
                            ActiveWorkoutSet(setNumber = 2, previous = rdlPrev, weight = "", reps = "", isCompleted = false)
                        ),
                        initialBarbellWeightKg = rdlDb?.barbellWeightKg ?: 20.0,
                        initialSetupMediaUri = rdlDb?.setupMediaUri,
                        initialMachineCalibration = rdlDb?.machineCalibration
                    )
                )
            )
        }
        initialList
    }

    // Carry over persistent exercise notes, barbell weight, setup media, and machine calibration from database
    LaunchedEffect(allDbExercises) {
        if (allDbExercises.isNotEmpty()) {
            exercises.forEach { ex ->
                val cleanName = ex.name.substringBefore("(").trim()
                val dbMatch = allDbExercises.find { it.name.equals(cleanName, ignoreCase = true) || it.name.equals(ex.name, ignoreCase = true) || it.id == ex.id }
                if (dbMatch != null) {
                    if (ex.note.isBlank() && dbMatch.notes.isNotBlank()) {
                        ex.note = dbMatch.notes
                    }
                    if (ex.barbellWeightKg == 20.0 && dbMatch.barbellWeightKg != 20.0) {
                        ex.barbellWeightKg = dbMatch.barbellWeightKg
                    }
                    if (ex.setupMediaUri == null && dbMatch.setupMediaUri != null) {
                        ex.setupMediaUri = dbMatch.setupMediaUri
                    }
                    if (ex.machineCalibration == null && dbMatch.machineCalibration != null) {
                        ex.machineCalibration = dbMatch.machineCalibration
                    }
                }
            }
        }
    }

    // High performance derived metrics (warm-up sets tagged with 'W' excluded from working volume)
    val totalCompletedSets by remember {
        derivedStateOf { exercises.sumOf { ex -> ex.sets.count { it.isCompleted && it.setType != ActiveSetType.WARMUP } } }
    }
    val totalVolumeKg by remember {
        derivedStateOf {
            exercises.sumOf { ex ->
                val barWeight = if (isBarbellExercise(ex, allDbExercises)) ex.barbellWeightKg else 0.0
                ex.sets.filter { it.isCompleted && it.setType != ActiveSetType.WARMUP }.sumOf { s ->
                    val w = (if (s.weight.isNotBlank()) s.weight else parseGhostWeight(s.previous)).toDoubleOrNull() ?: 0.0
                    val r = (if (s.reps.isNotBlank()) s.reps else parseGhostReps(s.previous)).toIntOrNull() ?: 0
                    (w + barWeight) * r
                }
            }
        }
    }
    val totalCompletedReps by remember {
        derivedStateOf {
            exercises.sumOf { ex ->
                ex.sets.filter { it.isCompleted && it.setType != ActiveSetType.WARMUP }.sumOf { s ->
                    (if (s.reps.isNotBlank()) s.reps else parseGhostReps(s.previous)).toIntOrNull() ?: 0
                }
            }
        }
    }

    val completedExerciseCount by remember {
        derivedStateOf { exercises.count { it.isCompleted } }
    }

    // Sync live metrics with WorkoutViewModel and Foreground Service
    LaunchedEffect(secondsElapsed, totalCompletedSets, exercises.size, isPaused, pauseTimestamp, totalPausedSeconds) {
        try {
            val totalSets = exercises.sumOf { it.sets.size }
            viewModel.updateActiveWorkoutMetrics(
                durationSeconds = secondsElapsed,
                completedSets = totalCompletedSets,
                totalSets = if (totalSets > 0) totalSets else 1
            )
            com.example.service.WorkoutTrackingService.update(
                context = context,
                completedSets = totalCompletedSets,
                totalSets = if (totalSets > 0) totalSets else 1,
                isPaused = isPaused,
                pauseTimestamp = pauseTimestamp,
                totalPausedSeconds = totalPausedSeconds
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e("ActiveWorkout", "Failed to update metrics: ${e.message}", e)
        }
    }

    // Atomic Real-Time Persistence of Active Workout State to Room DB
    LaunchedEffect(exercises.size, totalCompletedSets, totalVolumeKg, completedExerciseCount, isPaused, pauseTimestamp, totalPausedSeconds) {
        try {
            val json = com.example.util.ActiveWorkoutSerializer.serialize(exercises)
            val state = com.example.data.entity.ActiveWorkoutStateEntity(
                workoutId = "active_session",
                routineId = routine?.id ?: restoredState?.routineId,
                routineName = workoutTitle,
                startTimestamp = startTimestamp,
                lastUpdatedTimestamp = System.currentTimeMillis(),
                isPaused = isPaused,
                pauseTimestamp = pauseTimestamp,
                totalPausedSeconds = totalPausedSeconds,
                userId = "guest",
                exercisesJson = json
            )
            viewModel.saveActiveWorkoutState(state)
            val draftSummary = exercises.mapIndexed { idx, ex ->
                "${idx + 1}. ${ex.name.substringBefore("(").trim()} (${ex.sets.count { s -> s.isCompleted }} sets)"
            }.joinToString(", ")
            viewModel.autoSaveDraft(
                routineId = routine?.id ?: restoredState?.routineId,
                routineTitle = workoutTitle,
                durationSeconds = secondsElapsed,
                draftJson = draftSummary
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e("ActiveWorkout", "Failed to persist active workout state: ${e.message}", e)
        }
    }

    // Secondary Heartbeat Persistence to Room DB (every 5 seconds)
    LaunchedEffect(isPaused, pauseTimestamp, totalPausedSeconds) {
        try {
            while (isActive) {
                delay(5000)
                try {
                    val json = com.example.util.ActiveWorkoutSerializer.serialize(exercises)
                    val state = com.example.data.entity.ActiveWorkoutStateEntity(
                        workoutId = "active_session",
                        routineId = routine?.id ?: restoredState?.routineId,
                        routineName = workoutTitle,
                        startTimestamp = startTimestamp,
                        lastUpdatedTimestamp = System.currentTimeMillis(),
                        isPaused = isPaused,
                        pauseTimestamp = pauseTimestamp,
                        totalPausedSeconds = totalPausedSeconds,
                        userId = "guest",
                        exercisesJson = json
                    )
                    viewModel.saveActiveWorkoutState(state)
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    Log.e("ActiveWorkout", "Heartbeat save error: ${e.message}", e)
                }
            }
        } catch (e: CancellationException) {
            // Normal coroutine cancellation on scope exit
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
        modifier = Modifier
            .fillMaxSize()
            .imePadding(),
        containerColor = AmoledBlack,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.padding(16.dp)
            ) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = Color(0xFF1E2638),
                    contentColor = Color.White,
                    actionColor = Color(0xFF1877F2),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        topBar = {
            Surface(
                color = AmoledBlack,
                modifier = Modifier.fillMaxWidth().border(0.dp, DarkBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .displayCutoutPadding()
                ) {
                    // Top Navigation & Action Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Top-Left: Minimize Button ("KeyboardArrowDown") & Discard Button ("✕")
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            IconButton(
                                onClick = onMinimize,
                                modifier = Modifier
                                    .size(38.dp)
                                    .testTag("minimize_workout_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Minimize Workout",
                                    tint = TextWhite,
                                    modifier = Modifier.size(26.dp)
                                )
                            }

                            IconButton(
                                onClick = { showFinishModal = true },
                                modifier = Modifier
                                    .size(38.dp)
                                    .testTag("cancel_workout_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Discard Workout",
                                    tint = TextMuted,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        // Center: Workout Title & Live Digital Elapsed Session Timer (00:00:00)
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 8.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    val now = System.currentTimeMillis()
                                    if (isPaused) {
                                        val pausedDuration = if (pauseTimestamp != null && pauseTimestamp!! > 0L) (now - pauseTimestamp!!) / 1000L else 0L
                                        totalPausedSeconds += pausedDuration
                                        pauseTimestamp = null
                                        isPaused = false
                                        Toast.makeText(context, "Workout Resumed ▶", Toast.LENGTH_SHORT).show()
                                    } else {
                                        pauseTimestamp = now
                                        isPaused = true
                                        Toast.makeText(context, "Workout Paused ⏸", Toast.LENGTH_SHORT).show()
                                    }
                                }
                        ) {
                            Text(
                                text = workoutTitle,
                                color = TextWhite,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                if (isPaused) {
                                    Text(
                                        text = "PAUSED",
                                        color = Color(0xFFF59E0B),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1
                                    )
                                }
                                Text(
                                    text = formattedTime,
                                    color = if (isPaused) Color(0xFFF59E0B) else ElectricBlueLight,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    letterSpacing = 0.5.sp,
                                    maxLines = 1
                                )
                            }
                        }

                        // Top Right: Auto Rest Timer Toggle + Pause/Resume Toggle + Finish Button
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            IconButton(
                                onClick = {
                                    val now = System.currentTimeMillis()
                                    if (isPaused) {
                                        val pausedDuration = if (pauseTimestamp != null && pauseTimestamp!! > 0L) (now - pauseTimestamp!!) / 1000L else 0L
                                        totalPausedSeconds += pausedDuration
                                        pauseTimestamp = null
                                        isPaused = false
                                        Toast.makeText(context, "Workout Resumed ▶", Toast.LENGTH_SHORT).show()
                                    } else {
                                        pauseTimestamp = now
                                        isPaused = true
                                        Toast.makeText(context, "Workout Paused ⏸", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isPaused) Color(0x3DF59E0B) else Color(0xFF1E2638))
                                    .border(
                                        1.dp,
                                        if (isPaused) Color(0xFFF59E0B) else Color(0xFF28354E),
                                        RoundedCornerShape(10.dp)
                                    )
                                    .testTag("toggle_pause_workout_button")
                            ) {
                                Icon(
                                    imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                                    contentDescription = if (isPaused) "Resume Workout" else "Pause Workout",
                                    tint = if (isPaused) Color(0xFFF59E0B) else Color(0xFF8E8E93),
                                    modifier = Modifier.size(20.dp)
                                )
                            }

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
                                    .size(40.dp)
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
                                onClick = { showFinishModal = true },
                                modifier = Modifier
                                    .height(40.dp)
                                    .defaultMinSize(minWidth = 72.dp)
                                    .testTag("finish_workout_button"),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1877F2)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = "Finish",
                                    maxLines = 1,
                                    softWrap = false,
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
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
                                    text = if (isPaused) "TIME (PAUSED)" else "TIME",
                                    color = if (isPaused) Color(0xFFF59E0B) else Color(0xFF8E8E93),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = formattedTime,
                                    color = if (isPaused) Color(0xFFF59E0B) else Color(0xFF1877F2),
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

                    // Real-Time Workout Progress Pill Badge Trigger
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = Color(0xFF0F172A),
                            shape = RoundedCornerShape(20.dp),
                            border = BorderStroke(1.dp, Color(0xFF1877F2).copy(alpha = 0.5f)),
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .clickable { showWorkoutProgressModal = true }
                                .testTag("workout_progress_trigger_badge")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(text = "📊", fontSize = 12.sp)
                                Text(
                                    text = "Progress: $completedExerciseCount/${exercises.size} Exercises Done",
                                    color = Color(0xFF4C9AFF),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = "View Muscle Breakdown",
                                    tint = Color(0xFF4C9AFF),
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }

                        Text(
                            text = "${if (exercises.isNotEmpty()) (completedExerciseCount * 100 / exercises.size) else 0}% Completed",
                            color = Color(0xFF8E8E93),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
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
        val navBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .navigationBarsPadding()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(
                top = 8.dp,
                bottom = 120.dp + navBarPadding
            ),
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
                        var currentNote by remember(exercise.id, exercise.name) { mutableStateOf(exercise.note) }
                        var isNoteEditing by remember { mutableStateOf(false) }
                        var showFillDownPill by remember { mutableStateOf(false) }
                        var fillDownWeightToCopy by remember { mutableStateOf("") }

                        LaunchedEffect(exercise.note) {
                            if (exercise.note != currentNote && !isNoteEditing) {
                                currentNote = exercise.note
                            }
                        }

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .border(
                            1.dp,
                            if (exercise.isCompleted) Color(0xFF22C55E).copy(alpha = 0.5f) else DarkBorder,
                            RoundedCornerShape(16.dp)
                        ),
                    color = if (exercise.isCompleted) Color(0xFF0C1310) else DarkCardBg
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateContentSize()
                            .padding(start = 14.dp, top = 14.dp, end = 14.dp, bottom = 16.dp)
                    ) {
                        // 1. Rich Exercise Card Header
                        val cleanExerciseTitle = remember(exercise.name) {
                            exercise.name.substringBefore("(").trim()
                        }
                        val muscleSubtitle = remember(exercise.name, exercise.primaryMuscle, allDbExercises) {
                            val clean = exercise.name.substringBefore("(").trim()
                            val match = allDbExercises.find { it.name.equals(clean, ignoreCase = true) }
                            match?.primaryMuscle ?: exercise.primaryMuscle.ifBlank { "Full Body" }
                        }

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
                                    exerciseName = cleanExerciseTitle,
                                    size = 40.dp
                                )

                                // Exercise Name: Crisp, bold white text + optional Done badge + Analytics tap target
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(6.dp))
                                        .clickable {
                                            analyticsExerciseName = cleanExerciseTitle
                                            analyticsMuscleGroup = muscleSubtitle
                                        }
                                        .testTag("active_exercise_header_${exercise.id}")
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = cleanExerciseTitle,
                                            color = if (exercise.isCompleted) Color(0xFF9CA3AF) else TextWhite,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            modifier = Modifier.weight(1f, fill = false)
                                        )
                                        Surface(
                                            color = ElectricBlueAlpha,
                                            shape = RoundedCornerShape(4.dp),
                                            border = BorderStroke(1.dp, ElectricBlue.copy(alpha = 0.3f)),
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .clickable {
                                                    analyticsExerciseName = cleanExerciseTitle
                                                    analyticsMuscleGroup = muscleSubtitle
                                                }
                                                .testTag("active_exercise_analytics_button_${exercise.id}")
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                                            ) {
                                                Text(
                                                    text = "📈",
                                                    fontSize = 10.sp
                                                )
                                            }
                                        }
                                        if (exercise.isCompleted) {
                                            Surface(
                                                color = Color(0x2A22C55E),
                                                shape = RoundedCornerShape(6.dp),
                                                border = BorderStroke(1.dp, Color(0xFF22C55E).copy(alpha = 0.5f))
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Check,
                                                        contentDescription = "Completed",
                                                        tint = Color(0xFF22C55E),
                                                        modifier = Modifier.size(11.dp)
                                                    )
                                                    Text(
                                                        text = "DONE",
                                                        color = Color(0xFF22C55E),
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }
                                    }
                                    if (muscleSubtitle.isNotBlank()) {
                                        Text(
                                            text = muscleSubtitle,
                                            color = Color(0xFF8E8E93),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }

                                    // Smart Equipment Pin, Seat & Lever Calibration Preset Pill
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Surface(
                                        color = Color(0xFF1A1A1A),
                                        shape = RoundedCornerShape(16.dp),
                                        border = BorderStroke(1.dp, Color(0xFF262626)),
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(16.dp))
                                            .clickable { exerciseForCalibration = exercise }
                                            .testTag("machine_calibration_pill_${exercise.id}")
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                                        ) {
                                            if (!exercise.machineCalibration.isNullOrBlank()) {
                                                Icon(
                                                    imageVector = Icons.Default.Tune,
                                                    contentDescription = "Machine Preset",
                                                    tint = Color(0xFF1877F2),
                                                    modifier = Modifier.size(13.dp)
                                                )
                                                Text(
                                                    text = formatCalibrationSummary(exercise.machineCalibration),
                                                    color = Color(0xFFEDEDED),
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    maxLines = 1
                                                )
                                            } else {
                                                Icon(
                                                    imageVector = Icons.Default.Tune,
                                                    contentDescription = null,
                                                    tint = Color(0xFF757575),
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Text(
                                                    text = "+ Set Machine Preset",
                                                    color = Color(0xFF757575),
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }
                                        }
                                    }

                                    if (isBarbellExercise(exercise, allDbExercises)) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Surface(
                                                color = Color(0xFF162036),
                                                shape = RoundedCornerShape(6.dp),
                                                border = BorderStroke(1.dp, Color(0xFF1E2E52)),
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .clickable { exerciseForBarbellSelection = exercise }
                                                    .testTag("barbell_badge_${exercise.id}")
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                    Text(
                                                        text = formatBarbellLabel(exercise.barbellWeightKg),
                                                        color = Color(0xFF60A5FA),
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.SemiBold
                                                    )
                                                }
                                            }

                                            Surface(
                                                color = Color(0xFF1E1B2E),
                                                shape = RoundedCornerShape(6.dp),
                                                border = BorderStroke(1.dp, Color(0xFF3B2D5E)),
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .clickable {
                                                        exerciseForPlateCalculator = exercise
                                                        targetSetForPlateCalculator = exercise.sets.firstOrNull { it.weight.isNotBlank() } ?: exercise.sets.firstOrNull()
                                                    }
                                                    .testTag("plate_calc_badge_${exercise.id}")
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                    Text(
                                                        text = "🧮 Plates",
                                                        color = Color(0xFFA78BFA),
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.SemiBold
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // Action Overflow: Reorder chevrons and 3-dot menu
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                val currentIdx = exercises.indexOfFirst { it.id == exercise.id }
                                if (currentIdx > 0) {
                                    IconButton(
                                        onClick = {
                                            val liveIdx = exercises.indexOfFirst { it.id == exercise.id }
                                            if (liveIdx > 0 && liveIdx < exercises.size) {
                                                val item = exercises.removeAt(liveIdx)
                                                exercises.add(liveIdx - 1, item)
                                            }
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
                                if (currentIdx >= 0 && currentIdx < exercises.size - 1) {
                                    IconButton(
                                        onClick = {
                                            val liveIdx = exercises.indexOfFirst { it.id == exercise.id }
                                            if (liveIdx >= 0 && liveIdx < exercises.size - 1) {
                                                val item = exercises.removeAt(liveIdx)
                                                exercises.add(liveIdx + 1, item)
                                            }
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
                                            text = { Text("⇄ Swap Exercise (This Workout Only)", color = TextWhite) },
                                            leadingIcon = {
                                                Icon(Icons.Default.SwapHoriz, contentDescription = null, tint = ElectricBlue)
                                            },
                                            onClick = {
                                                showCardMenu = false
                                                val liveIdx = exercises.indexOfFirst { it.id == exercise.id }.takeIf { it >= 0 } ?: exIndex
                                                exerciseIndexToReplace = liveIdx
                                                exerciseTargetMuscleForSwap = muscleSubtitle.ifBlank { exercise.primaryMuscle }
                                                showAddExerciseSheet = true
                                            }
                                        )
                                        if (isBarbellExercise(exercise, allDbExercises)) {
                                            DropdownMenuItem(
                                                text = { Text("Plate Calculator", color = TextWhite) },
                                                leadingIcon = {
                                                    Icon(Icons.Default.Calculate, contentDescription = null, tint = Color(0xFFA78BFA))
                                                },
                                                onClick = {
                                                    showCardMenu = false
                                                    exerciseForPlateCalculator = exercise
                                                    targetSetForPlateCalculator = exercise.sets.firstOrNull { it.weight.isNotBlank() } ?: exercise.sets.firstOrNull()
                                                }
                                            )
                                        }
                                        DropdownMenuItem(
                                            text = { Text("Warm-up Calculator", color = TextWhite) },
                                            leadingIcon = {
                                                Icon(Icons.Default.Whatshot, contentDescription = null, tint = WarmupAmber)
                                            },
                                            onClick = {
                                                showCardMenu = false
                                                exerciseForWarmupCalculator = exercise
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    if (currentNote.isNotBlank() || isNoteEditing) "Clear Note" else "Add Exercise Note",
                                                    color = TextWhite
                                                )
                                            },
                                            leadingIcon = {
                                                Icon(Icons.Default.EditNote, contentDescription = null, tint = ElectricBlueLight)
                                            },
                                            onClick = {
                                                showCardMenu = false
                                                if (currentNote.isNotBlank() || isNoteEditing) {
                                                    currentNote = ""
                                                    exercise.note = ""
                                                    isNoteEditing = false
                                                    viewModel.updateExerciseNote(exercise.id, exercise.name, "")
                                                } else {
                                                    isNoteEditing = true
                                                }
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    if (exercise.setupMediaUri != null) "View / Edit Setup Media" else "Attach Setup Media",
                                                    color = TextWhite
                                                )
                                            },
                                            leadingIcon = {
                                                Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = ElectricBlueLight)
                                            },
                                            onClick = {
                                                showCardMenu = false
                                                exerciseForMediaAttachment = exercise
                                                showMediaAttachmentSheet = true
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    if (!exercise.machineCalibration.isNullOrBlank()) "Edit Machine Preset" else "Set Machine Preset",
                                                    color = TextWhite
                                                )
                                            },
                                            leadingIcon = {
                                                Icon(Icons.Default.Tune, contentDescription = null, tint = ElectricBlueLight)
                                            },
                                            onClick = {
                                                showCardMenu = false
                                                exerciseForCalibration = exercise
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Remove Exercise (This Session Only)", color = MissedRed) },
                                            leadingIcon = {
                                                Icon(Icons.Default.Delete, contentDescription = null, tint = MissedRed)
                                            },
                                            onClick = {
                                                showCardMenu = false
                                                val liveIdx = exercises.indexOfFirst { it.id == exercise.id }.takeIf { it >= 0 } ?: exIndex
                                                if (liveIdx in exercises.indices) {
                                                    exercises.removeAt(liveIdx)
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        // Persistent Exercise Note Component & Setup Media Action
                        Spacer(modifier = Modifier.height(8.dp))
                        if (currentNote.isBlank() && !isNoteEditing) {
                            // State A: Note is Empty
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(6.dp))
                                        .clickable { isNoteEditing = true }
                                        .padding(vertical = 4.dp, horizontal = 2.dp)
                                        .testTag("add_exercise_note_${exercise.id}"),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "📝 Add exercise note...",
                                        color = Color(0xFF1877F2),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        exerciseForMediaAttachment = exercise
                                        showMediaAttachmentSheet = true
                                    },
                                    modifier = Modifier
                                        .size(30.dp)
                                        .testTag("add_exercise_media_${exercise.id}")
                                ) {
                                    Icon(
                                        imageVector = if (exercise.setupMediaUri != null) Icons.Default.PhotoCamera else Icons.Default.AddAPhoto,
                                        contentDescription = "Attach Form/Setup Media",
                                        tint = Color(0xFF1877F2),
                                        modifier = Modifier.size(17.dp)
                                    )
                                }
                            }
                        } else {
                            // State B: Note is Populated
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF121622), RoundedCornerShape(8.dp))
                                    .border(1.dp, Color(0xFF1E2538), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                                    .testTag("exercise_note_container_${exercise.id}")
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "📌",
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(top = 1.dp)
                                    )
                                    BasicTextField(
                                        value = currentNote,
                                        onValueChange = { newNote ->
                                            currentNote = newNote
                                            exercise.note = newNote
                                            viewModel.updateExerciseNote(exercise.id, exercise.name, newNote)
                                        },
                                        textStyle = TextStyle(
                                            color = Color(0xFFD1D5DB),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Normal
                                        ),
                                        cursorBrush = SolidColor(Color(0xFF1877F2)),
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("exercise_note_input_${exercise.id}"),
                                        decorationBox = { innerTextField ->
                                            if (currentNote.isBlank()) {
                                                Text(
                                                    text = "Note machine settings, seat height, cues...",
                                                    color = Color(0xFF6B7280),
                                                    fontSize = 12.sp
                                                )
                                            }
                                            innerTextField()
                                        }
                                    )
                                    IconButton(
                                        onClick = {
                                            exerciseForMediaAttachment = exercise
                                            showMediaAttachmentSheet = true
                                        },
                                        modifier = Modifier
                                            .size(24.dp)
                                            .testTag("edit_exercise_media_${exercise.id}")
                                    ) {
                                        Icon(
                                            imageVector = if (exercise.setupMediaUri != null) Icons.Default.PhotoCamera else Icons.Default.AddAPhoto,
                                            contentDescription = "Attach Form/Setup Media",
                                            tint = Color(0xFF1877F2),
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }
                                    IconButton(
                                        onClick = {
                                            currentNote = ""
                                            exercise.note = ""
                                            isNoteEditing = false
                                            viewModel.updateExerciseNote(exercise.id, exercise.name, "")
                                        },
                                        modifier = Modifier
                                            .size(22.dp)
                                            .testTag("clear_exercise_note_${exercise.id}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Clear Note",
                                            tint = Color(0xFF8E8E93),
                                            modifier = Modifier.size(13.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Compact In-Card Media Preview Component (72.dp height, AMOLED #121212, Electric Blue border #1877F2)
                        if (!exercise.setupMediaUri.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            ExerciseMediaThumbnailCard(
                                exercise = exercise,
                                onClick = { exerciseForMediaViewing = exercise },
                                onChange = {
                                    exerciseForMediaAttachment = exercise
                                    showMediaAttachmentSheet = true
                                },
                                onRemove = {
                                    val oldUri = exercise.setupMediaUri
                                    exercise.setupMediaUri = null
                                    deleteExerciseMedia(context, exercise.id, oldUri)
                                    viewModel.updateExerciseSetupMedia(exercise.id, exercise.name, null)
                                }
                            )
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
                                modifier = Modifier
                                    .width(68.dp)
                                    .clickable {
                                        val firstSet = exercise.sets.firstOrNull()
                                        val w = if (firstSet?.weight?.isNotBlank() == true) firstSet.weight else firstSet?.let { parseGhostWeight(it.previous) } ?: "0"
                                        if (w.isNotBlank() && w != "0") {
                                            fillDownWeightToCopy = w
                                            showFillDownPill = true
                                            Toast.makeText(context, "Copy $w kg to all empty sets below?", Toast.LENGTH_SHORT).show()
                                        }
                                    },
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

                        // Long-Press Fill-Down Confirmation Pill
                        AnimatedVisibility(
                            visible = showFillDownPill,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            val hapticPill = LocalHapticFeedback.current
                            Surface(
                                color = Color(0xFF162036),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, Color(0xFF1877F2)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 6.dp)
                                    .testTag("fill_down_pill_${exercise.id}")
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
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.VerticalAlignBottom,
                                            contentDescription = "Fill Down",
                                            tint = Color(0xFF1877F2),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = "Copy ${fillDownWeightToCopy.ifBlank { "0" }} kg to all empty sets below?",
                                            color = Color.White,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            maxLines = 1
                                        )
                                    }
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Button(
                                            onClick = {
                                                val w = fillDownWeightToCopy.ifBlank {
                                                    val firstSet = exercise.sets.firstOrNull()
                                                    if (firstSet?.weight?.isNotBlank() == true) firstSet.weight else firstSet?.let { parseGhostWeight(it.previous) } ?: "0"
                                                }
                                                var copiedCount = 0
                                                exercise.sets.forEachIndexed { idx, s ->
                                                    if (idx > 0 && !s.isCompleted && s.weight.isBlank()) {
                                                        s.weight = w
                                                        copiedCount++
                                                    }
                                                }
                                                showFillDownPill = false
                                                hapticPill.performHapticFeedback(HapticFeedbackType.LongPress)
                                                Toast.makeText(
                                                    context,
                                                    "Copied $w kg to $copiedCount set${if (copiedCount == 1) "" else "s"}",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1877F2)),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                            modifier = Modifier.height(28.dp).testTag("apply_fill_down_button")
                                        ) {
                                            Text("Apply", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                        IconButton(
                                            onClick = { showFillDownPill = false },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Dismiss",
                                                tint = Color(0xFF8E8E93),
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // 3. Set Rows wrapped with SwipeToDismissBox (Swipe Right = Clone, Swipe Left = Delete)
                        exercise.sets.forEachIndexed { setIndex, setItem ->
                            key(setItem.id) {
                                val isFirstSet = setIndex == 0
                                val focusRequester = remember { FocusRequester() }
                                val haptic = LocalHapticFeedback.current

                                val dismissState = rememberSwipeToDismissBoxState(
                                    positionalThreshold = { totalDistance -> totalDistance * 0.4f },
                                    confirmValueChange = { dismissValue ->
                                        when (dismissValue) {
                                            SwipeToDismissBoxValue.StartToEnd -> {
                                                // Swipe Right: Duplicate Set
                                                val liveSetIdx = exercise.sets.indexOfFirst { it.id == setItem.id }
                                                val insertIndex = if (liveSetIdx != -1) (liveSetIdx + 1).coerceAtMost(exercise.sets.size) else exercise.sets.size
                                                val clonedSet = ActiveWorkoutSet(
                                                    setNumber = setItem.setNumber + 1,
                                                    setType = setItem.setType,
                                                    previous = setItem.previous,
                                                    weight = setItem.weight,
                                                    reps = setItem.reps,
                                                    isCompleted = false
                                                )
                                                exercise.sets.add(insertIndex, clonedSet)
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                Toast.makeText(context, "Set duplicated", Toast.LENGTH_SHORT).show()
                                                false
                                            }
                                            SwipeToDismissBoxValue.EndToStart -> {
                                                // Swipe Left: Delete Set
                                                if (exercise.sets.size <= 1) {
                                                    Toast.makeText(context, "Cannot delete the only set", Toast.LENGTH_SHORT).show()
                                                    false
                                                } else {
                                                    val liveSetIdx = exercise.sets.indexOfFirst { it.id == setItem.id }
                                                    if (liveSetIdx in exercise.sets.indices) {
                                                        val deletedSet = setItem
                                                        exercise.sets.removeAt(liveSetIdx)
                                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                        coroutineScope.launch {
                                                            val result = snackbarHostState.showSnackbar(
                                                                message = "Set removed",
                                                                actionLabel = "UNDO",
                                                                duration = SnackbarDuration.Short
                                                            )
                                                            if (result == SnackbarResult.ActionPerformed) {
                                                                val restoreIndex = liveSetIdx.coerceAtMost(exercise.sets.size)
                                                                exercise.sets.add(restoreIndex, deletedSet)
                                                            }
                                                        }
                                                        true
                                                    } else {
                                                        false
                                                    }
                                                }
                                            }
                                            SwipeToDismissBoxValue.Settled -> false
                                        }
                                    }
                                )

                                SwipeToDismissBox(
                                    state = dismissState,
                                    enableDismissFromStartToEnd = true,
                                    enableDismissFromEndToStart = exercise.sets.size > 1,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 2.dp),
                                    backgroundContent = {
                                        val direction = dismissState.dismissDirection
                                        val isStartToEnd = direction == SwipeToDismissBoxValue.StartToEnd
                                        val isEndToStart = direction == SwipeToDismissBoxValue.EndToStart

                                        val bgColor = when {
                                            isStartToEnd -> Color(0xFF1877F2)
                                            isEndToStart -> Color(0xFFFF3B30)
                                            else -> Color.Transparent
                                        }

                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(bgColor)
                                                .padding(horizontal = 14.dp),
                                            contentAlignment = if (isStartToEnd) Alignment.CenterStart else Alignment.CenterEnd
                                        ) {
                                            if (isStartToEnd) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.ContentCopy,
                                                        contentDescription = "Duplicate Set",
                                                        tint = Color.White,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                    Text(
                                                        text = "Duplicate Set",
                                                        color = Color.White,
                                                        fontSize = 13.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            } else if (isEndToStart) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    Text(
                                                        text = "Delete Set",
                                                        color = Color.White,
                                                        fontSize = 13.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    Icon(
                                                        imageVector = Icons.Default.Delete,
                                                        contentDescription = "Delete Set",
                                                        tint = Color.White,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(DarkCardBg)
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
                                            val workingSetNum = remember(exercise.sets, setIndex, setItem.setType) {
                                                val normalCount = exercise.sets.take(setIndex + 1).count { it.setType == ActiveSetType.NORMAL }
                                                if (normalCount > 0) normalCount else (setIndex + 1)
                                            }
                                            Text(
                                                text = if (setItem.setType == ActiveSetType.NORMAL) "$workingSetNum" else setItem.setType.code,
                                                color = setItem.setType.textColor,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(8.dp))

                                        // PREVIOUS Column: Restored previous performance reference in subtle gray (#6B7280)
                                        Text(
                                            text = if (setItem.previous.isBlank()) "—" else setItem.previous,
                                            color = Color(0xFF6B7280),
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium,
                                            modifier = Modifier.weight(1f),
                                            textAlign = TextAlign.Center,
                                            maxLines = 1
                                        )

                                        Spacer(modifier = Modifier.width(8.dp))

                                        // KG Input Container: Fully rounded dark slate box (#161B26) with Set 1 Long-Press Fill-Down shortcut
                                        Box(
                                            modifier = Modifier
                                                .width(68.dp)
                                                .height(38.dp)
                                                .background(Color(0xFF161B26), RoundedCornerShape(10.dp))
                                                .border(
                                                    1.dp,
                                                    if (isFirstSet && showFillDownPill) Color(0xFF1877F2) else DarkBorder,
                                                    RoundedCornerShape(10.dp)
                                                )
                                                .clip(RoundedCornerShape(10.dp))
                                                .then(
                                                    if (isFirstSet) {
                                                        Modifier.combinedClickable(
                                                            onClick = {
                                                                focusRequester.requestFocus()
                                                            },
                                                            onLongClick = {
                                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                                val weightVal = if (setItem.weight.isNotBlank()) {
                                                                    setItem.weight
                                                                } else {
                                                                    val gw = parseGhostWeight(setItem.previous)
                                                                    if (gw != "0") gw else ""
                                                                }
                                                                if (weightVal.isNotBlank()) {
                                                                    fillDownWeightToCopy = weightVal
                                                                    showFillDownPill = true
                                                                    Toast.makeText(
                                                                        context,
                                                                        "Copy $weightVal kg to all empty sets below?",
                                                                        Toast.LENGTH_SHORT
                                                                    ).show()
                                                                }
                                                            }
                                                        )
                                                    } else {
                                                        Modifier
                                                    }
                                                )
                                                .padding(horizontal = 4.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            val ghostWeight = remember(setItem.previous) { parseGhostWeight(setItem.previous) }
                                            var weightTextFieldValue by remember {
                                                mutableStateOf(TextFieldValue(text = setItem.weight, selection = TextRange(setItem.weight.length)))
                                            }
                                            LaunchedEffect(setItem.weight) {
                                                if (weightTextFieldValue.text != setItem.weight) {
                                                    weightTextFieldValue = weightTextFieldValue.copy(
                                                        text = setItem.weight,
                                                        selection = TextRange(setItem.weight.length)
                                                    )
                                                }
                                            }

                                            BasicTextField(
                                                value = weightTextFieldValue,
                                                onValueChange = { newValue ->
                                                    val filteredText = newValue.text.filter { it.isDigit() || it == '.' }
                                                    if (filteredText == newValue.text && filteredText.count { it == '.' } <= 1) {
                                                        weightTextFieldValue = newValue
                                                        setItem.weight = newValue.text
                                                        if (isFirstSet && showFillDownPill) {
                                                            fillDownWeightToCopy = newValue.text
                                                        }
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
                                                    keyboardType = KeyboardType.Decimal,
                                                    imeAction = ImeAction.Next
                                                ),
                                                singleLine = true,
                                                maxLines = 1,
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .focusRequester(focusRequester)
                                                    .onFocusChanged { focusState ->
                                                        if (focusState.isFocused) {
                                                            weightTextFieldValue = weightTextFieldValue.copy(
                                                                selection = TextRange(0, weightTextFieldValue.text.length)
                                                            )
                                                        }
                                                    }
                                                    .testTag("set_weight_input_${setIndex}"),
                                                decorationBox = { innerTextField ->
                                                    Box(
                                                        contentAlignment = Alignment.Center,
                                                        modifier = Modifier.fillMaxSize()
                                                    ) {
                                                        if (setItem.weight.isEmpty()) {
                                                            Text(
                                                                text = if (ghostWeight.isNotBlank() && ghostWeight != "0") ghostWeight else "0",
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
                                            var repsTextFieldValue by remember {
                                                mutableStateOf(TextFieldValue(text = setItem.reps, selection = TextRange(setItem.reps.length)))
                                            }
                                            LaunchedEffect(setItem.reps) {
                                                if (repsTextFieldValue.text != setItem.reps) {
                                                    repsTextFieldValue = repsTextFieldValue.copy(
                                                        text = setItem.reps,
                                                        selection = TextRange(setItem.reps.length)
                                                    )
                                                }
                                            }

                                            BasicTextField(
                                                value = repsTextFieldValue,
                                                onValueChange = { newValue ->
                                                    if (newValue.text.all { it.isDigit() }) {
                                                        repsTextFieldValue = newValue
                                                        setItem.reps = newValue.text
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
                                                    .onFocusChanged { focusState ->
                                                        if (focusState.isFocused) {
                                                            repsTextFieldValue = repsTextFieldValue.copy(
                                                                selection = TextRange(0, repsTextFieldValue.text.length)
                                                             )
                                                        }
                                                    }
                                                    .testTag("set_reps_input_${setIndex}"),
                                                decorationBox = { innerTextField ->
                                                    Box(
                                                        contentAlignment = Alignment.Center,
                                                        modifier = Modifier.fillMaxSize()
                                                    ) {
                                                        if (setItem.reps.isEmpty()) {
                                                            Text(
                                                                text = if (ghostReps.isNotBlank() && ghostReps != "0") ghostReps else "0",
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
                                                        // Treat empty weight as 0 kg, empty reps as 0
                                                        if (setItem.weight.isBlank()) {
                                                            setItem.weight = "0"
                                                        }
                                                        if (setItem.reps.isBlank()) {
                                                            setItem.reps = "0"
                                                        }
                                                        // Automatically trigger/restart rest timer when set is marked completed IF enabled
                                                        if (isRestTimerEnabled) {
                                                            restSecondsRemaining = 90
                                                        }
                                                    }
                                                    setItem.isCompleted = willComplete
                                                    if (willComplete) {
                                                        if (exercise.sets.all { it.isCompleted }) {
                                                            exercise.isCompleted = true
                                                        }
                                                    } else {
                                                        exercise.isCompleted = false
                                                    }
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
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // 4. Card Bottom: "+ Add Set" and dedicated "Warm-up" calculator buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(
                                onClick = {
                                    val nextSetNum = exercise.sets.size + 1
                                    val lastSet = exercise.sets.lastOrNull()
                                    val prevStr = when {
                                        lastSet != null && lastSet.weight.isNotBlank() && lastSet.reps.isNotBlank() -> {
                                            "${lastSet.weight} kg × ${lastSet.reps}"
                                        }
                                        lastSet != null && lastSet.previous.isNotBlank() && lastSet.previous != "—" && lastSet.previous != "-" -> {
                                            lastSet.previous
                                        }
                                        else -> "—"
                                    }
                                    exercise.sets.add(
                                        ActiveWorkoutSet(
                                            setNumber = nextSetNum,
                                            previous = prevStr,
                                            weight = "",
                                            reps = "",
                                            isCompleted = false
                                        )
                                    )
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .background(Color(0x1F1877F2), RoundedCornerShape(10.dp))
                                    .border(1.dp, Color(0xFF1877F2).copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                                    .testTag("add_set_button_${exercise.id}")
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

                            // Dedicated Warm-up Sets Button
                            OutlinedButton(
                                onClick = { exerciseForWarmupCalculator = exercise },
                                modifier = Modifier
                                    .height(44.dp)
                                    .testTag("warmup_calc_button_${exercise.id}"),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, WarmupAmber.copy(alpha = 0.45f)),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = Color(0x1FF59E0B),
                                    contentColor = WarmupAmber
                                ),
                                contentPadding = PaddingValues(horizontal = 12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Whatshot,
                                    contentDescription = "Warm-up",
                                    tint = WarmupAmber,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Warm-up",
                                    color = WarmupAmber,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Per-Exercise "Complete Exercise" Action Button
                        if (exercise.isCompleted) {
                            Button(
                                onClick = {
                                    exercise.isCompleted = false
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp)
                                    .testTag("complete_exercise_button_${exercise.id}"),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF13221B),
                                    contentColor = Color(0xFF22C55E)
                                ),
                                border = BorderStroke(1.dp, Color(0xFF22C55E).copy(alpha = 0.6f))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF22C55E),
                                    modifier = Modifier.size(17.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "✓ Exercise Completed (Tap to Reopen)",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF22C55E)
                                )
                            }
                        } else {
                            OutlinedButton(
                                onClick = {
                                    // Mark all pending sets as completed if not done
                                    exercise.sets.forEach { s ->
                                        if (!s.isCompleted) {
                                            if (s.weight.isBlank()) s.weight = parseGhostWeight(s.previous)
                                            if (s.reps.isBlank()) s.reps = parseGhostReps(s.previous)
                                            s.isCompleted = true
                                        }
                                    }
                                    exercise.isCompleted = true
                                    Toast.makeText(context, "✓ ${exercise.name} marked complete!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp)
                                    .testTag("complete_exercise_button_${exercise.id}"),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.2.dp, Color(0xFF1877F2)),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = Color(0x141877F2),
                                    contentColor = Color(0xFF1877F2)
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color(0xFF1877F2),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Mark Exercise Complete",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF1877F2)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Below All Exercise Cards: Prominent full-width "+ Add Exercise to Session" outlined button
    item(key = "add_exercise_button_item") {
                OutlinedButton(
                    onClick = {
                        exerciseIndexToReplace = null
                        exerciseTargetMuscleForSwap = null
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
                        text = "+ Add Exercise to Session",
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
        val isSwapMode = exerciseIndexToReplace != null
        AddExerciseBottomSheet(
            allExercises = allDbExercises,
            initialMuscleFilter = exerciseTargetMuscleForSwap,
            isSwapMode = isSwapMode,
            onDismiss = {
                showAddExerciseSheet = false
                exerciseIndexToReplace = null
                exerciseTargetMuscleForSwap = null
            },
            onSelectExercise = { selectedExercise ->
                val replaceIdx = exerciseIndexToReplace
                val defaultPrev = if (selectedExercise.prWeight > 0.0 && selectedExercise.prReps > 0) {
                    val prW = if (selectedExercise.prWeight % 1.0 == 0.0) selectedExercise.prWeight.toInt() else selectedExercise.prWeight
                    "$prW kg × ${selectedExercise.prReps}"
                } else {
                    "—"
                }

                if (replaceIdx != null && replaceIdx in exercises.indices) {
                    val oldExercise = exercises[replaceIdx]
                    val newSets = mutableStateListOf<ActiveWorkoutSet>()
                    if (oldExercise.sets.isNotEmpty()) {
                        oldExercise.sets.forEachIndexed { sIdx, oldSet ->
                            newSets.add(
                                ActiveWorkoutSet(
                                    setNumber = sIdx + 1,
                                    setType = oldSet.setType,
                                    previous = defaultPrev,
                                    weight = "",
                                    reps = "",
                                    isCompleted = false
                                )
                            )
                        }
                    } else {
                        repeat(3) { sIdx ->
                            newSets.add(
                                ActiveWorkoutSet(
                                    setNumber = sIdx + 1,
                                    setType = ActiveSetType.NORMAL,
                                    previous = defaultPrev,
                                    weight = "",
                                    reps = "",
                                    isCompleted = false
                                )
                            )
                        }
                    }

                    exercises[replaceIdx] = ActiveWorkoutExercise(
                        id = selectedExercise.id.toString(),
                        name = selectedExercise.name,
                        primaryMuscle = selectedExercise.primaryMuscle,
                        equipmentType = selectedExercise.equipmentType,
                        note = selectedExercise.notes,
                        sets = newSets,
                        initialBarbellWeightKg = selectedExercise.barbellWeightKg,
                        initialSetupMediaUri = selectedExercise.setupMediaUri,
                        initialMachineCalibration = selectedExercise.machineCalibration
                    )
                } else {
                    exercises.add(
                        ActiveWorkoutExercise(
                            id = selectedExercise.id.toString(),
                            name = selectedExercise.name,
                            primaryMuscle = selectedExercise.primaryMuscle,
                            equipmentType = selectedExercise.equipmentType,
                            note = selectedExercise.notes,
                            sets = mutableStateListOf(
                                ActiveWorkoutSet(setNumber = 1, previous = defaultPrev, weight = "", reps = ""),
                                ActiveWorkoutSet(setNumber = 2, previous = defaultPrev, weight = "", reps = ""),
                                ActiveWorkoutSet(setNumber = 3, previous = defaultPrev, weight = "", reps = "")
                            ),
                            initialBarbellWeightKg = selectedExercise.barbellWeightKg,
                            initialSetupMediaUri = selectedExercise.setupMediaUri,
                            initialMachineCalibration = selectedExercise.machineCalibration
                        )
                    )
                }
                showAddExerciseSheet = false
                exerciseIndexToReplace = null
                exerciseTargetMuscleForSwap = null
            },
            onConfirmSelection = { selectedList ->
                selectedList.forEach { selectedExercise ->
                    val defaultPrev = if (selectedExercise.prWeight > 0.0 && selectedExercise.prReps > 0) {
                        val prW = if (selectedExercise.prWeight % 1.0 == 0.0) selectedExercise.prWeight.toInt() else selectedExercise.prWeight
                        "$prW kg × ${selectedExercise.prReps}"
                    } else {
                        "—"
                    }
                    exercises.add(
                        ActiveWorkoutExercise(
                            id = selectedExercise.id.toString(),
                            name = selectedExercise.name,
                            primaryMuscle = selectedExercise.primaryMuscle,
                            equipmentType = selectedExercise.equipmentType,
                            note = selectedExercise.notes,
                            sets = mutableStateListOf(
                                ActiveWorkoutSet(setNumber = 1, previous = defaultPrev, weight = "", reps = ""),
                                ActiveWorkoutSet(setNumber = 2, previous = defaultPrev, weight = "", reps = ""),
                                ActiveWorkoutSet(setNumber = 3, previous = defaultPrev, weight = "", reps = "")
                            ),
                            initialBarbellWeightKg = selectedExercise.barbellWeightKg,
                            initialSetupMediaUri = selectedExercise.setupMediaUri,
                            initialMachineCalibration = selectedExercise.machineCalibration
                        )
                    )
                }
                showAddExerciseSheet = false
                exerciseIndexToReplace = null
                exerciseTargetMuscleForSwap = null
            }
        )
    }

    // Hevy-Style Workout Completion Confirmation Modal
    if (showFinishModal) {
        Dialog(
            onDismissRequest = { showFinishModal = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .navigationBarsPadding()
                    .imePadding()
                    .border(1.dp, Color(0xFF262626), RoundedCornerShape(20.dp)),
                color = Color(0xFF121212),
                shape = RoundedCornerShape(20.dp),
                tonalElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Trophy / Dumbbell Icon Badge
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(Color(0x2A1877F2))
                            .border(1.dp, Color(0xFF1877F2).copy(alpha = 0.4f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF1877F2),
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Finish Workout?",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Review your session stats before saving to history",
                        color = TextMuted,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Summary Info Row: Quick recap of duration, total volume, and completed sets count
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFF000000),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFF262626))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Column 1: Duration
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "DURATION",
                                    color = Color(0xFF8E8E93),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = formattedTime,
                                    color = Color(0xFF1877F2),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // Divider
                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(28.dp)
                                    .background(Color(0xFF262626))
                            )

                            // Column 2: Total Volume
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
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = formattedVolume,
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // Divider
                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(28.dp)
                                    .background(Color(0xFF262626))
                            )

                            // Column 3: Completed Sets
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "SETS",
                                    color = Color(0xFF8E8E93),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "$totalCompletedSets",
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Action 1: "Finish Workout" (Primary Button)
                    Button(
                        onClick = {
                            showFinishModal = false
                            exercises.forEach { ex ->
                                if (ex.note.isNotBlank()) {
                                    viewModel.updateExerciseNote(ex.id, ex.name, ex.note)
                                }
                                viewModel.updateExerciseBarbellWeight(ex.id, ex.name, ex.barbellWeightKg)
                            }
                            val summaryStr = exercises.mapIndexed { idx, ex ->
                                "${idx + 1}. ${ex.name.substringBefore("(").trim()} (${ex.sets.count { s -> s.isCompleted }} sets)"
                            }.joinToString(", ")

                            val endTimestamp = System.currentTimeMillis()
                            val ongoingPaused = if (isPaused && pauseTimestamp != null && pauseTimestamp!! > 0L) {
                                (endTimestamp - pauseTimestamp!!) / 1000L
                            } else {
                                0L
                            }
                            val totalWorkoutDurationSeconds = maxOf(0L, ((endTimestamp - startTimestamp) / 1000L) - (totalPausedSeconds + ongoingPaused))

                            viewModel.logCompletedWorkout(
                                routineId = routine?.id,
                                routineTitle = workoutTitle,
                                durationSeconds = totalWorkoutDurationSeconds,
                                totalVolumeKg = if (totalVolumeKg > 0) totalVolumeKg else 4500.0,
                                totalSets = if (totalCompletedSets > 0) totalCompletedSets else 8,
                                summary = summaryStr
                            )
                            viewModel.clearActiveWorkoutState(context)
                            Toast.makeText(context, "Workout logged successfully!", Toast.LENGTH_SHORT).show()
                            showWorkoutHighlightsModal = true
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("finish_workout_confirm_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1877F2)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Finish Workout",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Action 2: "Discard Workout" (Destructive Option)
                    OutlinedButton(
                        onClick = {
                            showFinishModal = false
                            showDiscardConfirmDialog = true
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("discard_workout_button"),
                        border = BorderStroke(1.dp, Color(0xFFFF3B30).copy(alpha = 0.5f)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color(0x1AFF3B30),
                            contentColor = Color(0xFFFF3B30)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            tint = Color(0xFFFF3B30),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Discard Workout",
                            color = Color(0xFFFF3B30),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Action 3: "Resume Workout" / "Cancel" (Dismiss)
                    TextButton(
                        onClick = { showFinishModal = false },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("resume_workout_button")
                    ) {
                        Text(
                            text = "Resume Workout",
                            color = Color(0xFF94A3B8),
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }

    // Secondary Discard Workout Confirmation Dialog
    if (showDiscardConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardConfirmDialog = false },
            containerColor = Color(0xFF121212),
            modifier = Modifier.border(1.dp, Color(0xFF262626), RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            title = {
                Text(
                    text = "Discard Workout?",
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
                        showDiscardConfirmDialog = false
                        viewModel.clearActiveDraft()
                        viewModel.clearActiveWorkoutState(context)
                        Toast.makeText(context, "Workout discarded", Toast.LENGTH_SHORT).show()
                        onFinish()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF3B30)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("confirm_discard_dialog_button")
                ) {
                    Text("Discard", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDiscardConfirmDialog = false },
                    modifier = Modifier.testTag("cancel_discard_dialog_button")
                ) {
                    Text("Cancel", color = ElectricBlueLight, fontWeight = FontWeight.SemiBold)
                }
            }
        )
    }

    // Hevy-Style Workout Completed Summary Highlights Modal
    if (showWorkoutHighlightsModal) {
        val completedDateStr = remember {
            val sdf = SimpleDateFormat("EEEE, MMM d • hh:mm a", Locale.getDefault())
            sdf.format(Date())
        }

        // Compute completed exercises breakdown
        val completedExercises = remember(exercises) {
            val list = exercises.filter { it.sets.any { s -> s.isCompleted } }
            if (list.isNotEmpty()) list else exercises.toList()
        }

        val shareText = remember(workoutTitle, formattedTime, formattedVolume, totalCompletedSets, totalCompletedReps, completedExercises) {
            buildString {
                append("🏋️ IronPulse Workout Completed!\n")
                append("📋 Routine: $workoutTitle\n")
                append("⏱️ Duration: $formattedTime\n")
                append("💥 Total Volume: $formattedVolume\n")
                append("🔢 Sets: $totalCompletedSets | Reps: $totalCompletedReps\n\n")
                append("Exercises:\n")
                completedExercises.forEach { ex ->
                    val doneSets = ex.sets.filter { it.isCompleted && it.setType != ActiveSetType.WARMUP }.ifEmpty { ex.sets.filter { it.isCompleted } }.ifEmpty { ex.sets }
                    val best = doneSets.maxByOrNull { s ->
                        (if (s.weight.isNotBlank()) s.weight else parseGhostWeight(s.previous)).toDoubleOrNull() ?: 0.0
                    }
                    val bestW = if (best?.weight?.isNotBlank() == true) best.weight else (best?.let { parseGhostWeight(it.previous) } ?: "0")
                    val bestR = if (best?.reps?.isNotBlank() == true) best.reps else (best?.let { parseGhostReps(it.previous) } ?: "0")
                    val barWeight = if (isBarbellExercise(ex, allDbExercises)) ex.barbellWeightKg else 0.0
                    val totalWeight = (bestW.toDoubleOrNull() ?: 0.0) + barWeight
                    append("• ${ex.name}: ${doneSets.size} sets (Best: ${totalWeight}kg × $bestR)\n")
                }
                append("\nLogged with IronPulse Gym Tracker 💪")
            }
        }

        Dialog(
            onDismissRequest = { onFinish() },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false
            )
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF000000)),
                color = Color(0xFF000000)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .displayCutoutPadding()
                        .navigationBarsPadding()
                        .imePadding()
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        contentPadding = PaddingValues(top = 12.dp, bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // 1. Celebratory Header
                        item(key = "celebration_header") {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(68.dp)
                                        .clip(CircleShape)
                                        .background(Color(0x2A1877F2))
                                        .border(2.dp, Color(0xFF1877F2), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "🎉",
                                        fontSize = 32.sp
                                    )
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                Text(
                                    text = "Workout Completed! 🎉",
                                    color = Color.White,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = workoutTitle,
                                    color = Color(0xFF1877F2),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(2.dp))

                                Text(
                                    text = completedDateStr,
                                    color = Color(0xFF8E8E93),
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        // 2. Key Workout Highlights Cards: Primary Metrics Row
                        item(key = "session_metrics_card") {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, Color(0xFF262626), RoundedCornerShape(16.dp)),
                                color = Color(0xFF121212),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                ) {
                                    Text(
                                        text = "SESSION SUMMARY",
                                        color = Color(0xFF8E8E93),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.5.sp
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        // Duration
                                        Column(
                                            modifier = Modifier.weight(1f),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = "DURATION",
                                                color = Color(0xFF8E8E93),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = formattedTime,
                                                color = Color(0xFF1877F2),
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        Box(modifier = Modifier.width(1.dp).height(32.dp).background(Color(0xFF262626)))

                                        // Total Volume
                                        Column(
                                            modifier = Modifier.weight(1f),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = "VOLUME",
                                                color = Color(0xFF8E8E93),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = formattedVolume,
                                                color = Color.White,
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        Box(modifier = Modifier.width(1.dp).height(32.dp).background(Color(0xFF262626)))

                                        // Sets
                                        Column(
                                            modifier = Modifier.weight(1f),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = "SETS",
                                                color = Color(0xFF8E8E93),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "$totalCompletedSets",
                                                color = Color.White,
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        Box(modifier = Modifier.width(1.dp).height(32.dp).background(Color(0xFF262626)))

                                        // Reps
                                        Column(
                                            modifier = Modifier.weight(1f),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = "REPS",
                                                color = Color(0xFF8E8E93),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "$totalCompletedReps",
                                                color = Color.White,
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // 3. Personal Records (PRs) Badge Section
                        item(key = "pr_highlights_card") {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, Color(0xFFF59E0B).copy(alpha = 0.35f), RoundedCornerShape(16.dp)),
                                color = Color(0xFF121212),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(text = "🏆", fontSize = 16.sp)
                                        Text(
                                            text = "PERSONAL RECORDS & HIGHLIGHTS",
                                            color = Color(0xFFF59E0B),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 0.5.sp
                                        )
                                    }

                                    // Display highlight badges for completed exercises
                                    completedExercises.take(3).forEach { ex ->
                                        val doneSets = ex.sets.filter { it.isCompleted && it.setType != ActiveSetType.WARMUP }.ifEmpty { ex.sets.filter { it.isCompleted } }.ifEmpty { ex.sets }
                                        val best = doneSets.maxByOrNull { s ->
                                            (if (s.weight.isNotBlank()) s.weight else parseGhostWeight(s.previous)).toDoubleOrNull() ?: 0.0
                                        }
                                        val bestW = (if (best?.weight?.isNotBlank() == true) best.weight else (best?.let { parseGhostWeight(it.previous) } ?: "0")).toDoubleOrNull() ?: 0.0
                                        val bestR = if (best?.reps?.isNotBlank() == true) best.reps else (best?.let { parseGhostReps(it.previous) } ?: "0")
                                        val barWeight = if (isBarbellExercise(ex, allDbExercises)) ex.barbellWeightKg else 0.0
                                        val totalBestW = bestW + barWeight
                                        val est1Rm = (totalBestW * (1.0 + (bestR.toDoubleOrNull() ?: 10.0) / 30.0)).toInt()

                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(Color(0xFF1A1A1A), RoundedCornerShape(10.dp))
                                                .border(1.dp, Color(0xFF2A2A2A), RoundedCornerShape(10.dp))
                                                .padding(horizontal = 12.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = ex.name,
                                                    color = Color.White,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                                Text(
                                                    text = "Best: ${if (totalBestW % 1.0 == 0.0) totalBestW.toInt().toString() else totalBestW.toString()} kg × $bestR  •  Est. 1RM: ${est1Rm} kg",
                                                    color = Color(0xFF8E8E93),
                                                    fontSize = 11.sp
                                                )
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .background(Color(0x3DF59E0B), RoundedCornerShape(6.dp))
                                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Text(
                                                    text = "PR ★",
                                                    color = Color(0xFFF59E0B),
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }

                                    // Total volume milestone badge
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color(0x1F1877F2), RoundedCornerShape(10.dp))
                                            .border(1.dp, Color(0xFF1877F2).copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(text = "🔥", fontSize = 14.sp)
                                        Text(
                                            text = "Total Volume Milestone: $formattedVolume successfully logged!",
                                            color = Color(0xFF4C9AFF),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }

                        // 4. Exercise Breakdown List Header
                        item(key = "breakdown_header") {
                            Text(
                                text = "EXERCISE BREAKDOWN (${completedExercises.size})",
                                color = Color(0xFF8E8E93),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp,
                                modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                            )
                        }

                        items(completedExercises, key = { "summary_ex_${it.id}" }) { ex ->
                            val doneSets = ex.sets.filter { it.isCompleted }.ifEmpty { ex.sets }
                            val best = doneSets.maxByOrNull { s ->
                                (if (s.weight.isNotBlank()) s.weight else parseGhostWeight(s.previous)).toDoubleOrNull() ?: 0.0
                            }
                            val bestW = (if (best?.weight?.isNotBlank() == true) best.weight else (best?.let { parseGhostWeight(it.previous) } ?: "0")).toDoubleOrNull() ?: 0.0
                            val bestR = if (best?.reps?.isNotBlank() == true) best.reps else (best?.let { parseGhostReps(it.previous) } ?: "0")
                            val barWeight = if (isBarbellExercise(ex, allDbExercises)) ex.barbellWeightKg else 0.0
                            val totalBestW = bestW + barWeight

                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, Color(0xFF262626), RoundedCornerShape(14.dp)),
                                color = Color(0xFF121212),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = ex.name,
                                                color = Color.White,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .background(Color(0xFF1E2538), RoundedCornerShape(4.dp))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = resolveExerciseMuscle(ex, allDbExercises),
                                                    color = Color(0xFF4C9AFF),
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(6.dp))

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text(
                                                text = "${doneSets.size} sets completed",
                                                color = Color(0xFF8E8E93),
                                                fontSize = 12.sp
                                            )
                                            if (isBarbellExercise(ex, allDbExercises)) {
                                                Text(
                                                    text = "•  🏋️ ${if (ex.barbellWeightKg % 1.0 == 0.0) ex.barbellWeightKg.toInt() else ex.barbellWeightKg}kg bar",
                                                    color = Color(0xFF8E8E93),
                                                    fontSize = 11.sp
                                                )
                                            }
                                        }
                                    }

                                    // Best Set Pill
                                    Box(
                                        modifier = Modifier
                                            .background(Color(0x2A1877F2), RoundedCornerShape(8.dp))
                                            .border(1.dp, Color(0xFF1877F2).copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = "${if (totalBestW % 1.0 == 0.0) totalBestW.toInt().toString() else totalBestW.toString()} kg × $bestR",
                                            color = Color(0xFF1877F2),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Pinned Bottom Action Container: Done and Share Workout buttons
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFF1B2030)),
                        color = Color(0xFF0D0E14),
                        tonalElevation = 8.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // "Done / Close" button in prominent Electric Blue (#1877F2)
                            Button(
                                onClick = { onFinish() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("summary_done_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1877F2)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = "Done",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }

                            // "Share Workout" button
                            OutlinedButton(
                                onClick = {
                                    val sendIntent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(Intent.EXTRA_TEXT, shareText)
                                        type = "text/plain"
                                    }
                                    val shareIntent = Intent.createChooser(sendIntent, "Share Workout")
                                    context.startActivity(shareIntent)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(46.dp)
                                    .testTag("share_workout_button"),
                                border = BorderStroke(1.dp, Color(0xFF1877F2).copy(alpha = 0.5f)),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = Color(0x1F1877F2),
                                    contentColor = Color(0xFF1877F2)
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "Share",
                                    tint = Color(0xFF1877F2),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Share Workout",
                                    color = Color(0xFF1877F2),
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Barbell Weight Selection Bottom Sheet Modal
    if (exerciseForBarbellSelection != null) {
        BarbellSelectionBottomSheet(
            exercise = exerciseForBarbellSelection!!,
            onDismiss = { exerciseForBarbellSelection = null },
            onSelectWeight = { newWeightKg ->
                exerciseForBarbellSelection?.let { ex ->
                    ex.barbellWeightKg = newWeightKg
                    viewModel.updateExerciseBarbellWeight(ex.id, ex.name, newWeightKg)
                }
                exerciseForBarbellSelection = null
            }
        )
    }

    // Barbell Plate Calculator Modal Dialog
    if (exerciseForPlateCalculator != null) {
        val calcExercise = exerciseForPlateCalculator!!
        val activeSet = targetSetForPlateCalculator ?: calcExercise.sets.firstOrNull()
        val initialWeight = activeSet?.weight?.toDoubleOrNull()
            ?: activeSet?.let { parseGhostWeight(it.previous).toDoubleOrNull() }
            ?: maxOf(calcExercise.barbellWeightKg, 60.0)

        BarbellPlateCalculatorDialog(
            exercise = calcExercise,
            initialTargetWeight = initialWeight,
            targetSet = activeSet,
            onDismiss = {
                exerciseForPlateCalculator = null
                targetSetForPlateCalculator = null
            },
            onApplyWeight = { weight, applyToAll ->
                val weightStr = if (weight % 1.0 == 0.0) weight.toInt().toString() else weight.toString()
                if (applyToAll) {
                    calcExercise.sets.forEach { s -> s.weight = weightStr }
                } else if (activeSet != null) {
                    activeSet.weight = weightStr
                } else {
                    calcExercise.sets.firstOrNull()?.weight = weightStr
                }
                exerciseForPlateCalculator = null
                targetSetForPlateCalculator = null
            }
        )
    }

    // Warm-up Sets Calculator Modal Dialog
    if (exerciseForWarmupCalculator != null) {
        val warmupExercise = exerciseForWarmupCalculator!!
        val firstWorkingSet = warmupExercise.sets.firstOrNull { it.setType != ActiveSetType.WARMUP && it.weight.isNotBlank() }
            ?: warmupExercise.sets.firstOrNull { it.weight.isNotBlank() }
            ?: warmupExercise.sets.firstOrNull { it.setType != ActiveSetType.WARMUP }
            ?: warmupExercise.sets.firstOrNull()

        val initialWeight = firstWorkingSet?.weight?.toDoubleOrNull()
            ?: firstWorkingSet?.let { parseGhostWeight(it.previous).toDoubleOrNull() }
            ?: maxOf(warmupExercise.barbellWeightKg, 60.0)

        WarmupCalculatorDialog(
            exercise = warmupExercise,
            initialTargetWeight = initialWeight,
            onDismiss = { exerciseForWarmupCalculator = null },
            onAddWarmupSets = { tiers ->
                val newWarmupSets = tiers.mapIndexed { idx, tier ->
                    ActiveWorkoutSet(
                        setNumber = idx + 1,
                        setType = ActiveSetType.WARMUP,
                        previous = "${formatPlateWeight(tier.weightKg)} kg × ${tier.reps}",
                        weight = formatPlateWeight(tier.weightKg),
                        reps = tier.reps.toString(),
                        isCompleted = false
                    )
                }
                warmupExercise.sets.addAll(0, newWarmupSets)
                warmupExercise.sets.forEachIndexed { i, s -> s.setNumber = i + 1 }
                Toast.makeText(context, "${newWarmupSets.size} warm-up sets added!", Toast.LENGTH_SHORT).show()
                exerciseForWarmupCalculator = null
            }
        )
    }

    // Setup Media Attachment Bottom Sheet
    if (showMediaAttachmentSheet && exerciseForMediaAttachment != null) {
        val targetExercise = exerciseForMediaAttachment!!
        SetupMediaAttachmentSheet(
            exercise = targetExercise,
            onDismiss = {
                showMediaAttachmentSheet = false
                exerciseForMediaAttachment = null
            },
            onTakePhoto = {
                val pair = createPrivateMediaFileUri(context, targetExercise.id, isVideo = false)
                if (pair != null) {
                    pendingCaptureFile = pair.first
                    pendingCaptureUri = pair.second
                    takePhotoLauncher.launch(pair.second)
                }
                showMediaAttachmentSheet = false
            },
            onRecordClip = {
                val pair = createPrivateMediaFileUri(context, targetExercise.id, isVideo = true)
                if (pair != null) {
                    pendingCaptureFile = pair.first
                    pendingCaptureUri = pair.second
                    captureVideoLauncher.launch(pair.second)
                }
                showMediaAttachmentSheet = false
            },
            onChooseGallery = {
                pickMediaLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
                )
                showMediaAttachmentSheet = false
            },
            onViewFullscreen = {
                showMediaAttachmentSheet = false
                exerciseForMediaViewing = targetExercise
            },
            onRemoveMedia = {
                val oldUri = targetExercise.setupMediaUri
                targetExercise.setupMediaUri = null
                deleteExerciseMedia(context, targetExercise.id, oldUri)
                viewModel.updateExerciseSetupMedia(targetExercise.id, targetExercise.name, null)
                showMediaAttachmentSheet = false
                exerciseForMediaAttachment = null
            }
        )
    }

    // Fullscreen Setup Media Viewer Dialog
    if (exerciseForMediaViewing != null) {
        val targetExercise = exerciseForMediaViewing!!
        SetupMediaViewerDialog(
            exercise = targetExercise,
            onDismiss = { exerciseForMediaViewing = null },
            onDeleteMedia = {
                val oldUri = targetExercise.setupMediaUri
                targetExercise.setupMediaUri = null
                deleteExerciseMedia(context, targetExercise.id, oldUri)
                viewModel.updateExerciseSetupMedia(targetExercise.id, targetExercise.name, null)
                exerciseForMediaViewing = null
            }
        )
    }

    // Smart Equipment Pin, Seat & Lever Calibration Preset Bottom Sheet
    if (exerciseForCalibration != null) {
        val targetExercise = exerciseForCalibration!!
        MachineCalibrationBottomSheet(
            exercise = targetExercise,
            onDismiss = { exerciseForCalibration = null },
            onSaveCalibration = { calibrationJson ->
                targetExercise.machineCalibration = calibrationJson
                viewModel.updateExerciseCalibration(targetExercise.id, targetExercise.name, calibrationJson)
                exerciseForCalibration = null
                Toast.makeText(context, "Machine preset saved!", Toast.LENGTH_SHORT).show()
            },
            onClearCalibration = {
                targetExercise.machineCalibration = null
                viewModel.updateExerciseCalibration(targetExercise.id, targetExercise.name, null)
                exerciseForCalibration = null
                Toast.makeText(context, "Machine preset cleared", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Real-Time Muscle Breakdown Workout Progress Modal
    if (showWorkoutProgressModal) {
        WorkoutProgressBottomSheet(
            exercises = exercises,
            allDbExercises = allDbExercises,
            onDismiss = { showWorkoutProgressModal = false }
        )
    }

    // Exercise Historical Analytics & Progression Quick Bottom Sheet
    analyticsExerciseName?.let { exName ->
        ExerciseProgressBottomSheet(
            exerciseName = exName,
            muscleGroup = analyticsMuscleGroup,
            viewModel = viewModel,
            onDismiss = { analyticsExerciseName = null }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutProgressBottomSheet(
    exercises: List<ActiveWorkoutExercise>,
    allDbExercises: List<ExerciseEntity>,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Group exercises by target muscle
    val groupedByMuscle = remember(exercises.map { it.id to (it.isCompleted to it.sets.map { s -> s.isCompleted to (s.weight to s.reps) }) }) {
        val map = linkedMapOf<String, MutableList<ActiveWorkoutExercise>>()
        for (ex in exercises) {
            val muscle = resolveExerciseMuscle(ex, allDbExercises)
            map.getOrPut(muscle) { mutableListOf() }.add(ex)
        }
        map
    }

    val totalExercises = exercises.size
    val completedExercises = exercises.count { it.isCompleted }
    val totalSets = exercises.sumOf { it.sets.size }
    val completedSets = exercises.sumOf { it.sets.count { s -> s.isCompleted } }
    val progressPercent = if (totalExercises > 0) (completedExercises * 100) / totalExercises else 0

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF000000), // AMOLED Pitch-Black
        scrimColor = Color.Black.copy(alpha = 0.70f),
        dragHandle = {
            BottomSheetDefaults.DragHandle(
                color = Color(0xFF374151)
            )
        },
        modifier = Modifier.testTag("workout_progress_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .navigationBarsPadding()
                .imePadding()
                .padding(bottom = 20.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Workout Progress",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Real-Time Muscle Breakdown",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFF1877F2),
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("workout_progress_close_icon_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Overall Progress Bar & Stats Pill
            Surface(
                color = Color(0xFF0F172A),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFF1E293B)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Overall Completion",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        Text(
                            text = "$progressPercent%",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = if (progressPercent == 100) Color(0xFF22C55E) else Color(0xFF1877F2)
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { if (totalExercises > 0) completedExercises.toFloat() / totalExercises.toFloat() else 0f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = if (progressPercent == 100) Color(0xFF22C55E) else Color(0xFF1877F2),
                        trackColor = Color(0xFF1E293B)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Scrollable Muscle Groups List
            LazyColumn(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                groupedByMuscle.forEach { (muscle, muscleExercises) ->
                    val muscleCompleted = muscleExercises.count { it.isCompleted }
                    val muscleTotal = muscleExercises.size

                    item(key = "header_$muscle") {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "• ${muscle.uppercase()} ($muscleCompleted of $muscleTotal Completed)",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1877F2),
                                        letterSpacing = 0.5.sp
                                    )
                                )
                                if (muscleCompleted == muscleTotal && muscleTotal > 0) {
                                    Surface(
                                        color = Color(0xFF22C55E).copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = "ALL DONE",
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = Color(0xFF22C55E),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 10.sp
                                            )
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                    }

                    items(muscleExercises, key = { it.id }) { ex ->
                        val exDoneSets = ex.sets.count { it.isCompleted }
                        val exTotalSets = ex.sets.size
                        val exVolume = ex.sets.sumOf { s ->
                            val w = s.weight.toDoubleOrNull() ?: 0.0
                            val r = s.reps.toIntOrNull() ?: 0
                            w * r
                        }

                        Surface(
                            color = if (ex.isCompleted) Color(0xFF0C1310) else Color(0xFF0B101B),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(
                                1.dp,
                                if (ex.isCompleted) Color(0xFF22C55E).copy(alpha = 0.5f) else Color(0xFF1E293B)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("workout_progress_item_${ex.id}")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Status Icon: Green checkmark for completed, Gray hourglass for remaining
                                    Surface(
                                        shape = CircleShape,
                                        color = if (ex.isCompleted) Color(0xFF22C55E).copy(alpha = 0.2f) else Color(0xFF1F2937),
                                        border = BorderStroke(
                                            1.dp,
                                            if (ex.isCompleted) Color(0xFF22C55E) else Color(0xFF4B5563)
                                        ),
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier.fillMaxSize()
                                        ) {
                                            if (ex.isCompleted) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = "Completed",
                                                    tint = Color(0xFF22C55E),
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            } else {
                                                Text(
                                                    text = "⏳",
                                                    fontSize = 11.sp
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = ex.name,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = if (ex.isCompleted) Color.White else Color(0xFFE2E8F0)
                                            ),
                                            maxLines = 1,
                                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        val formattedVolume = if (exVolume >= 1000) {
                                            String.format("%,.0f kg", exVolume)
                                        } else if (exVolume > 0) {
                                            "${exVolume.toInt()} kg"
                                        } else {
                                            "0 kg"
                                        }
                                        Text(
                                            text = "$exDoneSets/$exTotalSets sets • $formattedVolume total volume",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = if (ex.isCompleted) Color(0xFF4ADE80) else Color.Gray,
                                                fontSize = 11.sp
                                            )
                                        )
                                    }
                                }

                                if (ex.isCompleted) {
                                    Surface(
                                        color = Color(0xFF22C55E).copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = "DONE",
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = Color(0xFF22C55E),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 10.sp
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Total Summary Footer
            Surface(
                color = Color(0xFF0F172A),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFF1E293B)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Total Exercises Completed:",
                            style = MaterialTheme.typography.bodyMedium.copy(color = Color.LightGray)
                        )
                        Text(
                            text = "$completedExercises of $totalExercises",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Total Sets Done:",
                            style = MaterialTheme.typography.bodyMedium.copy(color = Color.LightGray)
                        )
                        Text(
                            text = "$completedSets of $totalSets",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Electric Blue Close Button
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1877F2),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("workout_progress_close_button")
            ) {
                Text(
                    text = "Close",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarbellSelectionBottomSheet(
    exercise: ActiveWorkoutExercise,
    onDismiss: () -> Unit,
    onSelectWeight: (Double) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var customWeightText by remember { mutableStateOf("") }
    var isLbsUnit by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF0F1422),
        dragHandle = {
            BottomSheetDefaults.DragHandle(
                color = Color(0xFF374151)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Barbell Weight Selection",
                        color = TextWhite,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = exercise.name,
                        color = Color(0xFF9CA3AF),
                        fontSize = 13.sp
                    )
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(32.dp).testTag("close_barbell_sheet_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color(0xFF9CA3AF),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "BARBELL PRESETS",
                color = Color(0xFF6B7280),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(BARBELL_PRESETS) { preset ->
                    val isSelected = kotlin.math.abs(exercise.barbellWeightKg - preset.weightKg) < 0.01
                    Surface(
                        color = if (isSelected) Color(0x261877F2) else Color(0xFF161D2F),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) Color(0xFF1877F2) else Color(0xFF26324D)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                onSelectWeight(preset.weightKg)
                            }
                            .testTag("barbell_preset_${preset.weightKg.toInt()}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = if (preset.weightKg == 0.0) "⚙️" else "🏋️",
                                    fontSize = 16.sp
                                )
                                Column {
                                    Text(
                                        text = preset.name,
                                        color = if (isSelected) Color(0xFF60A5FA) else TextWhite,
                                        fontSize = 14.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                    Text(
                                        text = "${preset.weightKg} kg / ${preset.weightLbs} lbs",
                                        color = if (isSelected) Color(0xFF93C5FD) else Color(0xFF8E8E93),
                                        fontSize = 12.sp
                                    )
                                }
                            }
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Selected",
                                    tint = Color(0xFF1877F2),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "CUSTOM BAR WEIGHT",
                color = Color(0xFF6B7280),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = customWeightText,
                    onValueChange = { customWeightText = it },
                    placeholder = {
                        Text("e.g. 17.5", color = Color(0xFF6B7280), fontSize = 14.sp)
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Done
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("custom_barbell_input"),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        focusedBorderColor = Color(0xFF1877F2),
                        unfocusedBorderColor = Color(0xFF26324D),
                        focusedContainerColor = Color(0xFF161D2F),
                        unfocusedContainerColor = Color(0xFF161D2F)
                    )
                )

                // Unit Toggle Button (KG / LBS)
                Button(
                    onClick = { isLbsUnit = !isLbsUnit },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isLbsUnit) Color(0xFF2563EB) else Color(0xFF1E2638)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Color(0xFF2E3E5E)),
                    modifier = Modifier
                        .height(52.dp)
                        .testTag("toggle_custom_unit_button")
                ) {
                    Text(
                        text = if (isLbsUnit) "LBS" else "KG",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                // Apply button
                Button(
                    onClick = {
                        val parsed = customWeightText.toDoubleOrNull()
                        if (parsed != null && parsed >= 0.0) {
                            val inKg = if (isLbsUnit) {
                                Math.round((parsed / 2.20462) * 10.0) / 10.0
                            } else {
                                parsed
                            }
                            onSelectWeight(inKg)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1877F2)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .height(52.dp)
                        .testTag("apply_custom_barbell_button")
                ) {
                    Text(
                        text = "Apply",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// ==========================================
// BARBELL PLATE CALCULATOR
// ==========================================

data class PlateSpec(
    val weightKg: Double,
    val color: Color,
    val textColor: Color,
    val heightDp: Int,
    val label: String
)

val STANDARD_METRIC_PLATES = listOf(
    PlateSpec(25.0, Color(0xFFEF4444), Color.White, 84, "25"),
    PlateSpec(20.0, Color(0xFF1877F2), Color.White, 76, "20"),
    PlateSpec(15.0, Color(0xFFEAB308), Color.Black, 66, "15"),
    PlateSpec(10.0, Color(0xFF22C55E), Color.White, 58, "10"),
    PlateSpec(7.5, Color(0xFFF97316), Color.White, 50, "7.5"),
    PlateSpec(5.0, Color(0xFFF8FAFC), Color.Black, 42, "5"),
    PlateSpec(2.5, Color(0xFF475569), Color.White, 34, "2.5"),
    PlateSpec(1.25, Color(0xFF94A3B8), Color.Black, 26, "1.25")
)

fun formatPlateWeight(weight: Double): String {
    return if (weight % 1.0 == 0.0) {
        weight.toInt().toString()
    } else {
        String.format(Locale.US, "%.2f", weight).trimEnd('0').trimEnd('.')
    }
}

fun calculatePlatesForBarbell(
    targetWeightKg: Double,
    barWeightKg: Double
): Pair<List<PlateSpec>, Double> {
    if (targetWeightKg <= barWeightKg) {
        return Pair(emptyList(), 0.0)
    }
    val netPerSide = (targetWeightKg - barWeightKg) / 2.0
    val result = mutableListOf<PlateSpec>()
    var remaining = Math.round(netPerSide * 100.0) / 100.0

    for (plate in STANDARD_METRIC_PLATES) {
        while (remaining >= plate.weightKg - 0.001) {
            result.add(plate)
            remaining = Math.round((remaining - plate.weightKg) * 100.0) / 100.0
        }
    }
    val loadedPerSide = result.sumOf { it.weightKg }
    val loadedTotal = barWeightKg + loadedPerSide * 2.0
    val remainder = maxOf(0.0, Math.round((targetWeightKg - loadedTotal) * 100.0) / 100.0)
    return Pair(result, remainder)
}

@Composable
fun BarbellPlateCalculatorDialog(
    exercise: ActiveWorkoutExercise,
    initialTargetWeight: Double,
    targetSet: ActiveWorkoutSet?,
    onDismiss: () -> Unit,
    onApplyWeight: (Double, Boolean) -> Unit
) {
    val barWeight = exercise.barbellWeightKg
    var targetWeight by remember { mutableStateOf(maxOf(barWeight, initialTargetWeight)) }
    var targetWeightText by remember { mutableStateOf(formatPlateWeight(targetWeight)) }
    var applyToAllSets by remember { mutableStateOf(false) }
    val (platesPerSide, remainderKg) = remember(targetWeight, barWeight) {
        calculatePlatesForBarbell(targetWeight, barWeight)
    }
    val loadedPerSide = remember(platesPerSide) { platesPerSide.sumOf { it.weightKg } }
    val loadedTotal = barWeight + loadedPerSide * 2.0

    val plateGroups = remember(platesPerSide) {
        platesPerSide.groupBy { it.weightKg }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .widthIn(max = 440.dp)
                .wrapContentHeight()
                .navigationBarsPadding()
                .imePadding()
                .clip(RoundedCornerShape(20.dp))
                .border(1.dp, Color(0xFF262626), RoundedCornerShape(20.dp))
                .testTag("plate_calculator_dialog"),
            color = Color(0xFF0F1422),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF1E2842)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Calculate,
                                contentDescription = null,
                                tint = Color(0xFFA78BFA),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Plate Calculator",
                                color = TextWhite,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${exercise.name} • Bar: ${formatPlateWeight(barWeight)} kg",
                                color = Color(0xFF9CA3AF),
                                fontSize = 12.sp
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp).testTag("close_plate_calc_dialog")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color(0xFF9CA3AF),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Target Weight Display & Quick Increments
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFF161B26),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, Color(0xFF262E40))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "TARGET WEIGHT",
                            color = Color(0xFF6B7280),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = formatPlateWeight(targetWeight),
                                color = TextWhite,
                                fontSize = 34.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "kg",
                                color = Color(0xFF60A5FA),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = if (targetWeight <= barWeight) {
                                "Bar weight only • 0 kg plates per side"
                            } else {
                                "Bar: ${formatPlateWeight(barWeight)} kg + Loaded: ${formatPlateWeight(loadedPerSide * 2.0)} kg"
                            },
                            color = Color(0xFF9CA3AF),
                            fontSize = 12.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Quick +/- Weight adjustment buttons (Horizontal Scrollable Row)
                        val quickAdjustDeltas = listOf(-10.0, -7.5, -5.0, -2.5, 2.5, 5.0, 7.5, 10.0)
                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            contentPadding = PaddingValues(horizontal = 2.dp)
                        ) {
                            items(quickAdjustDeltas) { delta ->
                                val isNegative = delta < 0
                                Button(
                                    onClick = {
                                        val newW = maxOf(barWeight, Math.round((targetWeight + delta) * 100.0) / 100.0)
                                        targetWeight = newW
                                        targetWeightText = formatPlateWeight(newW)
                                    },
                                    modifier = Modifier
                                        .height(36.dp)
                                        .testTag("quick_adjust_${if (isNegative) "minus" else "plus"}_${formatPlateWeight(kotlin.math.abs(delta))}"),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isNegative) Color(0xFF2A1616) else Color(0x331877F2)
                                    ),
                                    border = BorderStroke(
                                        1.dp,
                                        if (isNegative) Color(0xFF451D1D) else Color(0xFF1877F2)
                                    ),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                                ) {
                                    Text(
                                        text = if (delta > 0) "+${formatPlateWeight(delta)}" else formatPlateWeight(delta),
                                        color = if (isNegative) Color(0xFFF87171) else Color(0xFF60A5FA),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Manual weight input row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                                    .background(Color(0xFF0F1422), RoundedCornerShape(8.dp))
                                    .border(1.dp, Color(0xFF262E40), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 12.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                BasicTextField(
                                    value = targetWeightText,
                                    onValueChange = { newText ->
                                        if (newText.all { it.isDigit() || it == '.' }) {
                                            targetWeightText = newText
                                            newText.toDoubleOrNull()?.let {
                                                targetWeight = maxOf(barWeight, it)
                                            }
                                        }
                                    },
                                    textStyle = TextStyle(
                                        color = TextWhite,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    cursorBrush = SolidColor(Color(0xFF1877F2)),
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Number,
                                        imeAction = ImeAction.Done
                                    ),
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    decorationBox = { inner ->
                                        if (targetWeightText.isEmpty()) {
                                            Text(
                                                text = "Enter target weight (kg)",
                                                color = Color(0xFF4B5563),
                                                fontSize = 13.sp
                                            )
                                        }
                                        inner()
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Visual Barbell Sleeve Illustration
                Text(
                    text = "VISUAL BARBELL SLEEVE (ONE SIDE)",
                    color = Color(0xFF6B7280),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(115.dp),
                    color = Color(0xFF0A0F1A),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFF1E2638))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        // Horizontal Barbell Rod
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(12.dp)
                                .background(Color(0xFF374151), RoundedCornerShape(2.dp))
                                .border(0.5.dp, Color(0xFF4B5563), RoundedCornerShape(2.dp))
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            // Inside Collar Stop
                            Box(
                                modifier = Modifier
                                    .width(10.dp)
                                    .height(68.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(Color(0xFF4B5563))
                                    .border(1.dp, Color(0xFF6B7280), RoundedCornerShape(2.dp))
                            )

                            // Stacked Plates
                            if (platesPerSide.isNotEmpty()) {
                                platesPerSide.forEach { plate ->
                                    val plateWidth = when {
                                        plate.weightKg >= 20.0 -> if (platesPerSide.size > 8) 15.dp else 22.dp
                                        plate.weightKg >= 10.0 -> if (platesPerSide.size > 8) 13.dp else 19.dp
                                        plate.weightKg == 7.5 -> if (platesPerSide.size > 8) 12.dp else 17.dp
                                        plate.weightKg == 5.0 -> if (platesPerSide.size > 8) 10.dp else 15.dp
                                        plate.weightKg == 2.5 -> if (platesPerSide.size > 8) 9.dp else 13.dp
                                        else -> if (platesPerSide.size > 8) 8.dp else 11.dp
                                    }
                                    Box(
                                        modifier = Modifier
                                            .width(plateWidth)
                                            .height(plate.heightDp.dp)
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(plate.color)
                                            .border(1.dp, Color.Black.copy(alpha = 0.45f), RoundedCornerShape(3.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = plate.label,
                                            color = plate.textColor,
                                            fontSize = if (platesPerSide.size > 8 || plate.weightKg <= 2.5) 7.sp else 8.5.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            maxLines = 1,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }

                                // Collar Clamp
                                Box(
                                    modifier = Modifier
                                        .width(7.dp)
                                        .height(34.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(Color(0xFF94A3B8))
                                        .border(0.5.dp, Color(0xFFCBD5E1), RoundedCornerShape(2.dp))
                                )
                            } else {
                                // Empty Bar
                                Box(
                                    modifier = Modifier.padding(start = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (targetWeight <= barWeight) {
                                            "Empty Sleeve • Bar only (${formatPlateWeight(barWeight)} kg)"
                                        } else {
                                            "Empty Sleeve • No plates needed"
                                        },
                                        color = Color(0xFF6B7280),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Breakdown Text Summary
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFF161B26),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Color(0xFF262E40))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Per Side Load:",
                                color = Color(0xFF9CA3AF),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "${formatPlateWeight(loadedPerSide)} kg",
                                color = Color(0xFF60A5FA),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (platesPerSide.isNotEmpty()) {
                            LazyRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                items(plateGroups.toList()) { (weight, list) ->
                                    val spec = STANDARD_METRIC_PLATES.firstOrNull { it.weightKg == weight }
                                    Surface(
                                        color = Color(0xFF0F1422),
                                        shape = RoundedCornerShape(6.dp),
                                        border = BorderStroke(1.dp, spec?.color?.copy(alpha = 0.5f) ?: Color(0xFF262E40))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .clip(CircleShape)
                                                    .background(spec?.color ?: Color.White)
                                            )
                                            Text(
                                                text = "${list.size} × ${formatPlateWeight(weight)} kg",
                                                color = TextWhite,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }
                                }
                            }
                        } else {
                            Text(
                                text = if (targetWeight <= barWeight) {
                                    "No plates required (Bar weight only: ${formatPlateWeight(barWeight)} kg)"
                                } else {
                                    "No plates loaded"
                                },
                                color = Color(0xFF9CA3AF),
                                fontSize = 12.sp
                            )
                        }

                        if (remainderKg > 0.01) {
                            Text(
                                text = "⚠️ Closest load: ${formatPlateWeight(loadedTotal)} kg (+${formatPlateWeight(remainderKg)} kg remainder)",
                                color = Color(0xFFF59E0B),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Apply target toggle: single set vs all sets
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { applyToAllSets = !applyToAllSets }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Checkbox(
                        checked = applyToAllSets,
                        onCheckedChange = { applyToAllSets = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = Color(0xFF1877F2),
                            uncheckedColor = Color(0xFF4B5563)
                        )
                    )
                    Text(
                        text = if (targetSet != null) "Apply to all sets in ${exercise.name}" else "Apply to all sets",
                        color = Color(0xFFD1D5DB),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("cancel_plate_calc_button"),
                        border = BorderStroke(1.dp, Color(0xFF374151)),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.Transparent,
                            contentColor = Color(0xFF9CA3AF)
                        )
                    ) {
                        Text(
                            text = "Cancel",
                            color = Color(0xFF9CA3AF),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                    }

                    Button(
                        onClick = {
                            onApplyWeight(targetWeight, applyToAllSets)
                        },
                        modifier = Modifier
                            .weight(1.4f)
                            .height(48.dp)
                            .testTag("apply_plates_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1877F2)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Apply to ${if (applyToAllSets) "All Sets" else "Set ${targetSet?.setNumber ?: 1}"}",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// AUTOMATED WARM-UP SETS CALCULATOR
// ==========================================

data class WarmupTier(
    val tierNumber: Int,
    val name: String,
    val percentage: Int,
    val weightKg: Double,
    val reps: Int
)

fun calculateWarmupSets(targetWeightKg: Double, barWeightKg: Double): List<WarmupTier> {
    fun roundToNearest2_5(weight: Double): Double {
        val rounded = Math.round(weight / 2.5) * 2.5
        return Math.round(rounded * 100.0) / 100.0
    }

    val set1Weight = maxOf(barWeightKg, roundToNearest2_5(targetWeightKg * 0.40))
    val set2Weight = maxOf(set1Weight, roundToNearest2_5(targetWeightKg * 0.50))
    val set3Weight = maxOf(set2Weight, roundToNearest2_5(targetWeightKg * 0.70))
    val set4Weight = maxOf(set3Weight, roundToNearest2_5(targetWeightKg * 0.85))

    return listOf(
        WarmupTier(1, "Joint Prep & Movement", 40, set1Weight, 10),
        WarmupTier(2, "Motor Acclimation", 50, set2Weight, 5),
        WarmupTier(3, "Speed & Activation", 70, set3Weight, 3),
        WarmupTier(4, "CNS Potentiation", 85, set4Weight, 2)
    )
}

@Composable
fun WarmupCalculatorDialog(
    exercise: ActiveWorkoutExercise,
    initialTargetWeight: Double,
    onDismiss: () -> Unit,
    onAddWarmupSets: (List<WarmupTier>) -> Unit
) {
    val barWeight = exercise.barbellWeightKg
    var targetWeight by remember { mutableStateOf(maxOf(barWeight, initialTargetWeight)) }
    var targetWeightText by remember { mutableStateOf(formatPlateWeight(targetWeight)) }

    val warmupTiers = remember(targetWeight, barWeight) {
        calculateWarmupSets(targetWeight, barWeight)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .widthIn(max = 440.dp)
                .wrapContentHeight()
                .navigationBarsPadding()
                .imePadding()
                .clip(RoundedCornerShape(20.dp))
                .border(1.dp, Color(0xFF262626), RoundedCornerShape(20.dp))
                .testTag("warmup_calculator_dialog"),
            color = Color(0xFF0F1422),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0x3DF59E0B)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Whatshot,
                                contentDescription = null,
                                tint = WarmupAmber,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Warm-up Calculator",
                                color = TextWhite,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${exercise.name} • Bar: ${formatPlateWeight(barWeight)} kg",
                                color = Color(0xFF9CA3AF),
                                fontSize = 12.sp
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp).testTag("close_warmup_calc_dialog")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color(0xFF9CA3AF),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Target Working Load Box
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFF161B26),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, Color(0xFF262E40))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "TARGET WORKING LOAD",
                            color = Color(0xFF6B7280),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = formatPlateWeight(targetWeight),
                                color = TextWhite,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "kg",
                                color = Color(0xFF60A5FA),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Quick +/- Adjust Buttons
                        val quickDeltas = listOf(-10.0, -2.5, 2.5, 10.0)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            quickDeltas.forEach { delta ->
                                val isNeg = delta < 0
                                Button(
                                    onClick = {
                                        val newW = maxOf(barWeight, Math.round((targetWeight + delta) * 100.0) / 100.0)
                                        targetWeight = newW
                                        targetWeightText = formatPlateWeight(newW)
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(34.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isNeg) Color(0xFF2A1616) else Color(0x331877F2)
                                    ),
                                    border = BorderStroke(
                                        1.dp,
                                        if (isNeg) Color(0xFF451D1D) else Color(0xFF1877F2)
                                    ),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text(
                                        text = if (delta > 0) "+${formatPlateWeight(delta)}" else formatPlateWeight(delta),
                                        color = if (isNeg) Color(0xFFF87171) else Color(0xFF60A5FA),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Manual weight input row
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(38.dp)
                                .background(Color(0xFF0F1422), RoundedCornerShape(8.dp))
                                .border(1.dp, Color(0xFF262E40), RoundedCornerShape(8.dp))
                                .padding(horizontal = 12.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            BasicTextField(
                                value = targetWeightText,
                                onValueChange = { newText ->
                                    if (newText.all { it.isDigit() || it == '.' }) {
                                        targetWeightText = newText
                                        newText.toDoubleOrNull()?.let {
                                            targetWeight = maxOf(barWeight, it)
                                        }
                                    }
                                },
                                textStyle = TextStyle(
                                    color = TextWhite,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                cursorBrush = SolidColor(Color(0xFF1877F2)),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Done
                                ),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                decorationBox = { inner ->
                                    if (targetWeightText.isEmpty()) {
                                        Text(
                                            text = "Enter working weight (kg)",
                                            color = Color(0xFF4B5563),
                                            fontSize = 12.sp
                                        )
                                    }
                                    inner()
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 4-Tier Pyramid Preview List
                Text(
                    text = "PROGRESSIVE WARM-UP PYRAMID",
                    color = Color(0xFF6B7280),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    warmupTiers.forEach { tier ->
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = Color(0xFF141924),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, Color(0xFF242C3E))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 9.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(0x3DF59E0B))
                                            .border(1.dp, WarmupAmber.copy(alpha = 0.5f), RoundedCornerShape(6.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "W",
                                            color = WarmupAmber,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    }

                                    Column {
                                        Text(
                                            text = "Set ${tier.tierNumber} • ${tier.name}",
                                            color = TextWhite,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = "${tier.percentage}% of Working Weight",
                                            color = Color(0xFF9CA3AF),
                                            fontSize = 11.sp
                                        )
                                    }
                                }

                                Surface(
                                    color = Color(0xFF1E2842),
                                    shape = RoundedCornerShape(6.dp),
                                    border = BorderStroke(1.dp, Color(0xFF2E3E6E))
                                ) {
                                    Text(
                                        text = "${formatPlateWeight(tier.weightKg)} kg × ${tier.reps}",
                                        color = Color(0xFF60A5FA),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Footnote
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(text = "ℹ️", fontSize = 12.sp)
                    Text(
                        text = "Warm-up sets are tagged with 'W' and excluded from total volume and PR statistics.",
                        color = Color(0xFF9CA3AF),
                        fontSize = 11.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("cancel_warmup_calc_button"),
                        border = BorderStroke(1.dp, Color(0xFF374151)),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.Transparent,
                            contentColor = Color(0xFF9CA3AF)
                        )
                    ) {
                        Text(
                            text = "Cancel",
                            color = Color(0xFF9CA3AF),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                    }

                    Button(
                        onClick = {
                            onAddWarmupSets(warmupTiers)
                        },
                        modifier = Modifier
                            .weight(1.4f)
                            .height(48.dp)
                            .testTag("add_warmup_sets_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1877F2)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Add to Exercise",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ExerciseMediaThumbnailCard(
    exercise: ActiveWorkoutExercise,
    onClick: () -> Unit,
    onChange: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uriString = exercise.setupMediaUri ?: return
    val isVideo = remember(uriString) { isVideoUri(uriString) }
    var videoThumbnail by remember(uriString) { mutableStateOf<android.graphics.Bitmap?>(null) }

    LaunchedEffect(uriString, isVideo) {
        if (isVideo) {
            withContext(Dispatchers.IO) {
                val bmp = getVideoThumbnailBitmap(context, uriString)
                withContext(Dispatchers.Main) {
                    videoThumbnail = bmp
                }
            }
        }
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(72.dp)
            .clip(RoundedCornerShape(10.dp))
            .border(1.dp, Color(0xFF1877F2), RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .testTag("exercise_media_card_${exercise.id}"),
        color = Color(0xFF121212)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 16:9 Thumbnail Area (72dp height, ~128dp width)
            Box(
                modifier = Modifier
                    .width(128.dp)
                    .fillMaxHeight()
                    .background(Color(0xFF0A0A0A))
            ) {
                if (isVideo) {
                    if (videoThumbnail != null) {
                        Image(
                            bitmap = videoThumbnail!!.asImageBitmap(),
                            contentDescription = "Setup video clip thumbnail",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Videocam,
                                contentDescription = null,
                                tint = Color(0xFF1877F2),
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    // Corner CLIP indicator
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(4.dp)
                            .background(Color(0xCC000000), RoundedCornerShape(4.dp))
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = Color(0xFF1877F2),
                                modifier = Modifier.size(11.dp)
                            )
                            Text(
                                text = "CLIP",
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else {
                    AsyncImage(
                        model = uriString,
                        contentDescription = "Setup form photo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Corner PHOTO zoom indicator
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(4.dp)
                            .background(Color(0xCC000000), RoundedCornerShape(4.dp))
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ZoomIn,
                                contentDescription = null,
                                tint = Color(0xFF1877F2),
                                modifier = Modifier.size(11.dp)
                            )
                            Text(
                                text = "PHOTO",
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Right text info & quick actions
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = if (isVideo) Icons.Default.Videocam else Icons.Default.CameraAlt,
                            contentDescription = null,
                            tint = Color(0xFF1877F2),
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = if (isVideo) "Form & Setup Clip" else "Form & Setup Photo",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1
                        )
                    }
                    Text(
                        text = "Tap to inspect fullscreen",
                        color = Color(0xFF9CA3AF),
                        fontSize = 11.sp,
                        maxLines = 1
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Change",
                        color = Color(0xFF1877F2),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .clickable { onChange() }
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                            .testTag("change_media_${exercise.id}")
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Remove",
                        color = Color(0xFFEF4444),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .clickable { onRemove() }
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                            .testTag("remove_media_${exercise.id}")
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupMediaAttachmentSheet(
    exercise: ActiveWorkoutExercise,
    onDismiss: () -> Unit,
    onTakePhoto: () -> Unit,
    onRecordClip: () -> Unit,
    onChooseGallery: () -> Unit,
    onViewFullscreen: () -> Unit,
    onRemoveMedia: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF121212),
        dragHandle = {
            BottomSheetDefaults.DragHandle(color = Color(0xFF374151))
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .navigationBarsPadding()
                .imePadding()
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = exercise.name,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Form & Setup Media Reference",
                        color = Color(0xFF1877F2),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color(0xFF9CA3AF),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            HorizontalDivider(color = Color(0xFF262626))

            // Option 1: Take Photo
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, Color(0xFF262626), RoundedCornerShape(12.dp))
                    .clickable { onTakePhoto() }
                    .testTag("option_take_photo"),
                color = Color(0xFF1A1F2E)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .background(Color(0x261877F2), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoCamera,
                            contentDescription = null,
                            tint = Color(0xFF1877F2),
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Take Form Photo",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Snap pin height, bench notch, or foot setup",
                            color = Color(0xFF9CA3AF),
                            fontSize = 12.sp
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = Color(0xFF6B7280),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Option 2: Record Setup Clip
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, Color(0xFF262626), RoundedCornerShape(12.dp))
                    .clickable { onRecordClip() }
                    .testTag("option_record_clip"),
                color = Color(0xFF1A1F2E)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .background(Color(0x261877F2), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Videocam,
                            contentDescription = null,
                            tint = Color(0xFF1877F2),
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Record Setup Clip (10s)",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Record quick looping clip for setup cues",
                            color = Color(0xFF9CA3AF),
                            fontSize = 12.sp
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = Color(0xFF6B7280),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Option 3: Choose from Gallery
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, Color(0xFF262626), RoundedCornerShape(12.dp))
                    .clickable { onChooseGallery() }
                    .testTag("option_choose_gallery"),
                color = Color(0xFF1A1F2E)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .background(Color(0x261877F2), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoLibrary,
                            contentDescription = null,
                            tint = Color(0xFF1877F2),
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Choose from Device Gallery",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Select reference photo, clip, or GIF",
                            color = Color(0xFF9CA3AF),
                            fontSize = 12.sp
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = Color(0xFF6B7280),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Extra actions if media is currently attached
            if (!exercise.setupMediaUri.isNullOrBlank()) {
                HorizontalDivider(color = Color(0xFF262626), modifier = Modifier.padding(vertical = 4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onViewFullscreen,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("option_view_fullscreen"),
                        border = BorderStroke(1.dp, Color(0xFF1877F2)),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color(0x1F1877F2),
                            contentColor = Color(0xFF1877F2)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Fullscreen,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "View Fullscreen",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    OutlinedButton(
                        onClick = onRemoveMedia,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("option_remove_media"),
                        border = BorderStroke(1.dp, Color(0x66EF4444)),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color(0x1AEF4444),
                            contentColor = Color(0xFFEF4444)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Remove Media",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SetupMediaViewerDialog(
    exercise: ActiveWorkoutExercise,
    onDismiss: () -> Unit,
    onDeleteMedia: () -> Unit
) {
    val uriString = exercise.setupMediaUri ?: return
    val isVideo = remember(uriString) { isVideoUri(uriString) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    // Pinch-to-zoom state for photo
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(androidx.compose.ui.geometry.Offset.Zero) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            // Media View (Photo or Video)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 64.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isVideo) {
                    var isPlaying by remember { mutableStateOf(true) }
                    var videoViewRef by remember { mutableStateOf<android.widget.VideoView?>(null) }

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable {
                                videoViewRef?.let { vv ->
                                    if (vv.isPlaying) {
                                        vv.pause()
                                        isPlaying = false
                                    } else {
                                        vv.start()
                                        isPlaying = true
                                    }
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        AndroidView(
                            factory = { ctx ->
                                android.widget.VideoView(ctx).apply {
                                    val uri = Uri.parse(uriString)
                                    setVideoURI(uri)
                                    setOnPreparedListener { mp ->
                                        mp.isLooping = true
                                        mp.setVolume(0f, 0f) // Muted for focused gym environment
                                        start()
                                    }
                                    setOnCompletionListener {
                                        start()
                                    }
                                    videoViewRef = this
                                }
                            },
                            update = { vv ->
                                videoViewRef = vv
                            },
                            modifier = Modifier.fillMaxSize()
                        )

                        if (!isPlaying) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .background(Color(0x99000000), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Play Video",
                                    tint = Color(0xFF1877F2),
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }
                    }
                } else {
                    AsyncImage(
                        model = uriString,
                        contentDescription = "Setup Form Photo",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(Unit) {
                                detectTransformGestures { _, pan, zoom, _ ->
                                    scale = (scale * zoom).coerceIn(1f, 5f)
                                    if (scale > 1f) {
                                        offset += pan
                                    } else {
                                        offset = androidx.compose.ui.geometry.Offset.Zero
                                    }
                                }
                            }
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                                translationX = offset.x
                                translationY = offset.y
                            }
                    )
                }
            }

            // Top Bar Overlay
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .background(Color(0xCC000000))
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .statusBarsPadding(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = exercise.name,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    Text(
                        text = if (isVideo) "Looping Setup Clip" else "Form & Setup Reference",
                        color = Color(0xFF1877F2),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Delete Button
                    IconButton(
                        onClick = { showDeleteConfirm = true },
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("delete_setup_media_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Media",
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    // Close Button
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("close_setup_media_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            // Bottom Cue / Tip Bar Overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(Color(0xCC000000))
                    .padding(horizontal = 16.dp, vertical = 14.dp)
                    .navigationBarsPadding(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isVideo) "Tap video to pause/play • Auto-loops for quick form recall" else "Pinch to zoom in • Double check pin, bench notch & grip",
                    color = Color(0xFF9CA3AF),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
            }

            // Delete Confirmation Dialog
            if (showDeleteConfirm) {
                AlertDialog(
                    onDismissRequest = { showDeleteConfirm = false },
                    title = { Text("Delete Setup Media?", color = Color.White, fontWeight = FontWeight.Bold) },
                    text = { Text("This will permanently remove the form setup reference for ${exercise.name}.", color = Color(0xFFD1D5DB)) },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                showDeleteConfirm = false
                                onDeleteMedia()
                                onDismiss()
                            }
                        ) {
                            Text("Delete", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteConfirm = false }) {
                            Text("Cancel", color = Color(0xFF9CA3AF))
                        }
                    },
                    containerColor = Color(0xFF1E1E1E),
                    shape = RoundedCornerShape(16.dp)
                )
            }
        }
    }
}

fun isVideoUri(uriString: String?): Boolean {
    if (uriString.isNullOrBlank()) return false
    val lower = uriString.lowercase()
    return lower.endsWith(".mp4") || lower.endsWith(".mov") || lower.endsWith(".mkv") || lower.contains("video")
}

fun getVideoThumbnailBitmap(context: android.content.Context, uriString: String): android.graphics.Bitmap? {
    return try {
        val retriever = android.media.MediaMetadataRetriever()
        val uri = Uri.parse(uriString)
        if (uri.scheme == "file" && uri.path != null) {
            retriever.setDataSource(uri.path)
        } else {
            retriever.setDataSource(context, uri)
        }
        val bitmap = retriever.getFrameAtTime(0, android.media.MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
        retriever.release()
        bitmap
    } catch (e: Exception) {
        null
    }
}

fun createPrivateMediaFileUri(context: android.content.Context, exerciseId: String, isVideo: Boolean): Pair<File, Uri>? {
    return try {
        val dir = File(context.filesDir, "exercise_media/$exerciseId")
        if (!dir.exists()) dir.mkdirs()
        val ext = if (isVideo) "mp4" else "jpg"
        val file = File(dir, "setup_${System.currentTimeMillis()}.$ext")
        val uri = androidx.core.content.FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        Pair(file, uri)
    } catch (e: Exception) {
        null
    }
}

fun saveMediaToPrivateStorage(context: android.content.Context, exerciseId: String, sourceUri: Uri): Uri? {
    return try {
        val dir = File(context.filesDir, "exercise_media/$exerciseId")
        if (!dir.exists()) dir.mkdirs()
        val mimeType = context.contentResolver.getType(sourceUri)
        val ext = when {
            mimeType?.startsWith("video") == true -> "mp4"
            mimeType?.contains("png") == true -> "png"
            mimeType?.contains("gif") == true -> "gif"
            else -> "jpg"
        }
        val destFile = File(dir, "setup_${System.currentTimeMillis()}.$ext")
        context.contentResolver.openInputStream(sourceUri)?.use { input ->
            destFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        Uri.fromFile(destFile)
    } catch (e: Exception) {
        null
    }
}

fun deleteExerciseMedia(context: android.content.Context, exerciseId: String, uriString: String?) {
    try {
        if (!uriString.isNullOrBlank()) {
            val uri = Uri.parse(uriString)
            if (uri.scheme == "file") {
                val path = uri.path
                if (path != null) {
                    val file = File(path)
                    if (file.exists()) file.delete()
                }
            }
        }
        val dir = File(context.filesDir, "exercise_media/$exerciseId")
        if (dir.exists()) {
            dir.listFiles()?.forEach { it.delete() }
        }
    } catch (e: Exception) {
        // safe ignore
    }
}

data class CalibrationData(
    val seat: String = "",
    val backrest: String = "",
    val pad: String = "",
    val pin: String = "",
    val footplate: String = "",
    val customNote: String = ""
)

fun parseCalibrationData(calibrationStr: String?): CalibrationData {
    if (calibrationStr.isNullOrBlank()) return CalibrationData()
    return try {
        if (calibrationStr.startsWith("{") && calibrationStr.endsWith("}")) {
            val json = org.json.JSONObject(calibrationStr)
            CalibrationData(
                seat = json.optString("seat", ""),
                backrest = json.optString("backrest", ""),
                pad = json.optString("pad", ""),
                pin = json.optString("pin", ""),
                footplate = json.optString("footplate", ""),
                customNote = json.optString("note", "")
            )
        } else {
            CalibrationData(customNote = calibrationStr)
        }
    } catch (e: Exception) {
        CalibrationData(customNote = calibrationStr)
    }
}

fun buildCalibrationJson(
    seat: String,
    backrest: String,
    pad: String,
    pin: String,
    footplate: String,
    customNote: String
): String? {
    val obj = org.json.JSONObject()
    var hasAny = false
    if (seat.isNotBlank()) { obj.put("seat", seat); hasAny = true }
    if (backrest.isNotBlank()) { obj.put("backrest", backrest); hasAny = true }
    if (pad.isNotBlank()) { obj.put("pad", pad); hasAny = true }
    if (pin.isNotBlank()) { obj.put("pin", pin); hasAny = true }
    if (footplate.isNotBlank()) { obj.put("footplate", footplate); hasAny = true }
    if (customNote.isNotBlank()) { obj.put("note", customNote.trim()); hasAny = true }
    return if (hasAny) obj.toString() else null
}

fun formatCalibrationSummary(calibrationStr: String?): String {
    if (calibrationStr.isNullOrBlank()) return ""
    return try {
        val parts = mutableListOf<String>()
        if (calibrationStr.startsWith("{") && calibrationStr.endsWith("}")) {
            val json = org.json.JSONObject(calibrationStr)
            if (json.has("seat") && json.getString("seat").isNotBlank()) {
                parts.add("Seat: ${json.getString("seat")}")
            }
            if (json.has("backrest") && json.getString("backrest").isNotBlank()) {
                parts.add("Back: ${json.getString("backrest")}")
            }
            if (json.has("pad") && json.getString("pad").isNotBlank()) {
                parts.add("Pad: ${json.getString("pad")}")
            }
            if (json.has("pin") && json.getString("pin").isNotBlank()) {
                parts.add("Pin: ${json.getString("pin")}")
            }
            if (json.has("footplate") && json.getString("footplate").isNotBlank()) {
                parts.add("Foot: ${json.getString("footplate")}")
            }
            if (json.has("note") && json.getString("note").isNotBlank()) {
                parts.add(json.getString("note"))
            }
        } else {
            parts.add(calibrationStr)
        }
        if (parts.isNotEmpty()) parts.joinToString(" • ") else calibrationStr
    } catch (e: Exception) {
        calibrationStr
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MachineCalibrationBottomSheet(
    exercise: ActiveWorkoutExercise,
    onDismiss: () -> Unit,
    onSaveCalibration: (String?) -> Unit,
    onClearCalibration: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val initialData = remember(exercise.machineCalibration) { parseCalibrationData(exercise.machineCalibration) }

    var seat by remember { mutableStateOf(initialData.seat) }
    var backrest by remember { mutableStateOf(initialData.backrest) }
    var pad by remember { mutableStateOf(initialData.pad) }
    var pin by remember { mutableStateOf(initialData.pin) }
    var footplate by remember { mutableStateOf(initialData.footplate) }
    var customNote by remember { mutableStateOf(initialData.customNote) }

    val liveSummary = remember(seat, backrest, pad, pin, footplate, customNote) {
        val parts = mutableListOf<String>()
        if (seat.isNotBlank()) parts.add("Seat: $seat")
        if (backrest.isNotBlank()) parts.add("Back: $backrest")
        if (pad.isNotBlank()) parts.add("Pad: $pad")
        if (pin.isNotBlank()) parts.add("Pin: $pin")
        if (footplate.isNotBlank()) parts.add("Foot: $footplate")
        if (customNote.isNotBlank()) parts.add(customNote.trim())
        if (parts.isNotEmpty()) parts.joinToString(" • ") else "No preset selected"
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF0D0D0D),
        dragHandle = {
            BottomSheetDefaults.DragHandle(color = Color(0xFF374151))
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .navigationBarsPadding()
                .imePadding()
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = exercise.name,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    Text(
                        text = "Smart Machine Pin, Seat & Lever Presets",
                        color = Color(0xFF1877F2),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("calibration_sheet_dismiss")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color(0xFF9CA3AF),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Live Preview Card
            Surface(
                color = Color(0xFF161616),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFF262626)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = null,
                        tint = Color(0xFF1877F2),
                        modifier = Modifier.size(16.dp)
                    )
                    Column {
                        Text(
                            text = "ACTIVE PRESET PREVIEW",
                            color = Color(0xFF9CA3AF),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = liveSummary,
                            color = if (liveSummary != "No preset selected") Color(0xFFEDEDED) else Color(0xFF6B7280),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Dynamic Preset Selectors
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 1. Seat Height Selector
                PresetChipSelector(
                    label = "Seat Height",
                    currentValue = seat,
                    options = listOf("", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12"),
                    onSelect = { seat = it }
                )

                // 2. Backrest Angle Selector
                PresetChipSelector(
                    label = "Backrest Angle",
                    currentValue = backrest,
                    options = listOf("", "0°", "15°", "30°", "45°", "60°", "75°", "90°"),
                    onSelect = { backrest = it }
                )

                // 3. Roller / Pad Position
                PresetChipSelector(
                    label = "Roller / Pad Position",
                    currentValue = pad,
                    options = listOf("", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10"),
                    onSelect = { pad = it }
                )

                // 4. Cable / Pulley Pin Height
                PresetChipSelector(
                    label = "Cable / Pin Height",
                    currentValue = pin,
                    options = listOf("", "1", "2", "3", "4", "5", "6", "8", "10", "12", "15", "18", "20", "25"),
                    onSelect = { pin = it }
                )

                // 5. Footplate Distance
                PresetChipSelector(
                    label = "Footplate Distance",
                    currentValue = footplate,
                    options = listOf("", "1", "2", "3", "4", "5", "6", "7", "8"),
                    onSelect = { footplate = it }
                )
            }

            // Freeform Input for Custom Gym Notes
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Freeform Gym / Machine Code",
                    color = Color(0xFFD1D5DB),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                OutlinedTextField(
                    value = customNote,
                    onValueChange = { customNote = it },
                    placeholder = {
                        Text(
                            text = "e.g., Matrix Leg Press: Yellow Pin 5, Bench 3rd slot",
                            color = Color(0xFF6B7280),
                            fontSize = 13.sp
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("custom_calibration_note_input"),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF141414),
                        unfocusedContainerColor = Color(0xFF141414),
                        focusedBorderColor = Color(0xFF1877F2),
                        unfocusedBorderColor = Color(0xFF262626),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color(0xFFD1D5DB)
                    ),
                    singleLine = true
                )
            }

            // Action Buttons: Clear Preset & Save Preset
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onClearCalibration,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("clear_calibration_button"),
                    border = BorderStroke(1.dp, Color(0x66EF4444)),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color(0x1AEF4444),
                        contentColor = Color(0xFFEF4444)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Clear Preset",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Button(
                    onClick = {
                        val json = buildCalibrationJson(seat, backrest, pad, pin, footplate, customNote)
                        onSaveCalibration(json)
                    },
                    modifier = Modifier
                        .weight(1.3f)
                        .height(48.dp)
                        .testTag("save_calibration_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1877F2)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Save Preset",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun PresetChipSelector(
    label: String,
    currentValue: String,
    options: List<String>,
    onSelect: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                color = Color(0xFFD1D5DB),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
            if (currentValue.isNotBlank()) {
                Text(
                    text = currentValue,
                    color = Color(0xFF1877F2),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(options) { opt ->
                val isSelected = (opt.isBlank() && currentValue.isBlank()) || (opt == currentValue)
                val displayLabel = if (opt.isBlank()) "None" else opt

                Surface(
                    color = if (isSelected) Color(0xFF1877F2) else Color(0xFF181818),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, if (isSelected) Color(0xFF1877F2) else Color(0xFF262626)),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onSelect(opt) }
                ) {
                    Text(
                        text = displayLabel,
                        color = if (isSelected) Color.White else Color(0xFF9CA3AF),
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }
        }
    }
}



package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.entity.RoutineEntity
import com.example.data.entity.ExerciseEntity
import com.example.util.QrCodeGenerator
import com.example.util.SharedRoutinePayload
import com.example.util.SharedRoutineExercise
import com.example.util.SharedExerciseSet
import com.example.ui.components.AddExerciseBottomSheet
import com.example.ui.components.ExerciseThumbnail
import com.example.ui.theme.*
import com.example.ui.viewmodel.WorkoutViewModel
import java.util.UUID

@Stable
data class RoutineTemplateSet(
    val id: String = UUID.randomUUID().toString(),
    var weight: String = "0",
    var reps: String = "10"
)

@Stable
data class RoutineTemplateExercise(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val primaryMuscle: String = "",
    val equipmentType: String = "",
    val sets: MutableList<RoutineTemplateSet> = mutableStateListOf(
        RoutineTemplateSet(weight = "0", reps = "10"),
        RoutineTemplateSet(weight = "0", reps = "10"),
        RoutineTemplateSet(weight = "0", reps = "10")
    )
)

/**
 * Parses exercises from an existing RoutineEntity, preferring exercisesJson if available
 * and falling back to exercisesSummary with default sets.
 */
fun parseRoutineExercises(
    routine: RoutineEntity,
    allDbExercises: List<ExerciseEntity>
): List<RoutineTemplateExercise> {
    val result = mutableListOf<RoutineTemplateExercise>()

    // Try parsing from exercisesJson first
    if (routine.exercisesJson.isNotBlank()) {
        val payload = QrCodeGenerator.parseInput(routine.exercisesJson)
        if (payload != null && payload.exercises.isNotEmpty()) {
            payload.exercises.forEach { sharedEx ->
                val matchedDb = allDbExercises.find { it.name.equals(sharedEx.name, ignoreCase = true) }
                val muscle = matchedDb?.primaryMuscle ?: sharedEx.muscle.ifBlank { "Full Body" }
                val equip = matchedDb?.equipmentType ?: sharedEx.equipment.ifBlank { "Barbell" }

                val templateSets = if (sharedEx.sets.isNotEmpty()) {
                    sharedEx.sets.map { s ->
                        val wStr = if (s.weightKg % 1.0 == 0.0) s.weightKg.toInt().toString() else s.weightKg.toString()
                        RoutineTemplateSet(
                            id = UUID.randomUUID().toString(),
                            weight = if (wStr == "0") "0" else wStr,
                            reps = s.reps.toString()
                        )
                    }.toMutableList()
                } else {
                    val count = if (sharedEx.defaultSets > 0) sharedEx.defaultSets else 3
                    val reps = sharedEx.targetReps.filter { it.isDigit() }.takeIf { it.isNotBlank() } ?: "10"
                    (1..count).map {
                        RoutineTemplateSet(id = UUID.randomUUID().toString(), weight = "0", reps = reps)
                    }.toMutableList()
                }

                result.add(
                    RoutineTemplateExercise(
                        id = sharedEx.id.ifBlank { UUID.randomUUID().toString() },
                        name = sharedEx.name,
                        primaryMuscle = muscle,
                        equipmentType = equip,
                        sets = templateSets
                    )
                )
            }
        }
    }

    // Fallback: parse from exercisesSummary if exercisesJson is missing or produced no exercises
    if (result.isEmpty() && routine.exercisesSummary.isNotBlank()) {
        val items = routine.exercisesSummary.split(",").map { it.trim() }.filter { it.isNotBlank() }
        val setRegex = Regex("""^(.*?)\s*\((\d+)\s*sets?\)$""", RegexOption.IGNORE_CASE)

        items.forEach { item ->
            val match = setRegex.find(item)
            val name = match?.groupValues?.get(1)?.trim() ?: item
            val setCount = match?.groupValues?.get(2)?.toIntOrNull() ?: 3

            val matchedDb = allDbExercises.find { it.name.equals(name, ignoreCase = true) }
            val muscle = matchedDb?.primaryMuscle ?: ""
            val equip = matchedDb?.equipmentType ?: ""

            val sets = (1..setCount).map {
                RoutineTemplateSet(id = UUID.randomUUID().toString(), weight = "0", reps = "10")
            }.toMutableList()

            result.add(
                RoutineTemplateExercise(
                    id = UUID.randomUUID().toString(),
                    name = name,
                    primaryMuscle = muscle,
                    equipmentType = equip,
                    sets = sets
                )
            )
        }
    }

    return result
}

/**
 * Serializes exercises and sets into a JSON string compatible with QrCodeGenerator / SharedRoutinePayload.
 */
fun serializeExercisesToJson(
    routineTitle: String,
    routineNotes: String,
    exercises: List<RoutineTemplateExercise>
): String {
    val sharedExercises = exercises.mapIndexed { index, ex ->
        SharedRoutineExercise(
            id = ex.id,
            name = ex.name,
            exerciseName = ex.name,
            muscle = ex.primaryMuscle.ifBlank { "Full Body" },
            targetMuscle = ex.primaryMuscle.ifBlank { "Full Body" },
            equipment = ex.equipmentType.ifBlank { "Barbell" },
            defaultSets = ex.sets.size,
            targetReps = ex.sets.firstOrNull()?.reps ?: "10",
            notes = "",
            sets = ex.sets.mapIndexed { sIndex, s ->
                SharedExerciseSet(
                    setNumber = sIndex + 1,
                    reps = s.reps.toIntOrNull() ?: 10,
                    weightKg = s.weight.toDoubleOrNull() ?: 0.0,
                    setType = "NORMAL"
                )
            }
        )
    }
    val payload = SharedRoutinePayload(
        version = 2,
        routineName = routineTitle,
        description = routineNotes,
        exercisesSummary = exercises.joinToString(", ") { "${it.name} (${it.sets.size} sets)" },
        defaultSets = exercises.firstOrNull()?.sets?.size ?: 3,
        exerciseIds = exercises.map { it.id },
        exercises = sharedExercises
    )
    return QrCodeGenerator.serializeToJson(payload)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateRoutineScreen(
    viewModel: WorkoutViewModel,
    routineId: String? = null,
    editingRoutine: RoutineEntity? = null,
    onDismiss: () -> Unit,
    onRoutineCreated: () -> Unit = {},
    onRoutineSaved: () -> Unit = onRoutineCreated
) {
    val context = LocalContext.current
    val allDbExercises by viewModel.exercises.collectAsStateWithLifecycle()
    val allRoutines by viewModel.routines.collectAsStateWithLifecycle()

    val targetRoutine = editingRoutine ?: allRoutines.find { it.id == routineId }
    val isEditing = targetRoutine != null

    var routineTitle by remember(targetRoutine?.id) { mutableStateOf(targetRoutine?.title ?: "") }
    var routineNotes by remember(targetRoutine?.id) { mutableStateOf(targetRoutine?.description ?: "") }
    val exercises = remember(targetRoutine?.id) {
        val initialList = mutableStateListOf<RoutineTemplateExercise>()
        if (targetRoutine != null) {
            initialList.addAll(parseRoutineExercises(targetRoutine, allDbExercises))
        }
        initialList
    }

    var hasLoadedRoutine by remember(targetRoutine?.id) { mutableStateOf(targetRoutine != null && exercises.isNotEmpty()) }

    LaunchedEffect(targetRoutine?.id, allDbExercises.size) {
        if (targetRoutine != null && !hasLoadedRoutine) {
            routineTitle = targetRoutine.title
            routineNotes = targetRoutine.description
            exercises.clear()
            exercises.addAll(parseRoutineExercises(targetRoutine, allDbExercises))
            hasLoadedRoutine = true
        }
    }

    var showExercisePicker by remember { mutableStateOf(false) }

    // Intercept system back to cleanly dismiss
    BackHandler {
        onDismiss()
    }

    Scaffold(
        containerColor = AmoledBlack,
        topBar = {
            Surface(
                color = AmoledBlack,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(0.dp, DarkBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Cancel / Close button
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("cancel_create_routine_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cancel",
                            tint = TextWhite,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    // Center Header
                    Text(
                        text = if (isEditing) "Edit Routine" else "New Routine",
                        color = TextWhite,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    // Prominent Save / Save Changes Button
                    val canSave = routineTitle.isNotBlank()
                    Button(
                        onClick = {
                            if (canSave) {
                                val summaryStr = if (exercises.isEmpty()) {
                                    ""
                                } else {
                                    exercises.joinToString(", ") { ex ->
                                        "${ex.name} (${ex.sets.size} sets)"
                                    }
                                }
                                val jsonStr = serializeExercisesToJson(routineTitle.trim(), routineNotes.trim(), exercises)

                                if (targetRoutine != null) {
                                    viewModel.updateRoutine(
                                        id = targetRoutine.id,
                                        title = routineTitle.trim(),
                                        desc = routineNotes.trim(),
                                        exercisesSummary = summaryStr,
                                        exercisesJson = jsonStr
                                    )
                                    Toast.makeText(context, "Routine updated successfully", Toast.LENGTH_SHORT).show()
                                    onRoutineSaved()
                                } else {
                                    viewModel.createRoutine(
                                        title = routineTitle.trim(),
                                        desc = routineNotes.trim(),
                                        exercisesSummary = summaryStr,
                                        exercisesJson = jsonStr
                                    )
                                    viewModel.syncWithCloud()
                                    Toast.makeText(context, "Routine created successfully", Toast.LENGTH_SHORT).show()
                                    onRoutineCreated()
                                }
                            }
                        },
                        enabled = canSave,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ElectricBlue,
                            disabledContainerColor = ElectricBlue.copy(alpha = 0.3f),
                            contentColor = Color.White,
                            disabledContentColor = Color.White.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("save_routine_button")
                    ) {
                        Text(
                            text = if (isEditing) "Save Changes" else "Save",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 48.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Routine Details Header (Title and Notes)
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, Color(0xFF1E2538), RoundedCornerShape(16.dp)),
                    color = Color(0xFF121622)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        // Title Input
                        BasicTextField(
                            value = routineTitle,
                            onValueChange = { routineTitle = it },
                            textStyle = TextStyle(
                                color = TextWhite,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            cursorBrush = SolidColor(ElectricBlue),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("create_routine_title_input"),
                            decorationBox = { innerTextField ->
                                if (routineTitle.isEmpty()) {
                                    Text(
                                        text = "Routine Title (e.g. Push Day, Chest & Triceps)",
                                        color = Color(0xFF6B7280),
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                innerTextField()
                            }
                        )

                        HorizontalDivider(
                            color = DarkBorder,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )

                        // Notes Input
                        BasicTextField(
                            value = routineNotes,
                            onValueChange = { routineNotes = it },
                            textStyle = TextStyle(
                                color = TextWhite,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Normal
                            ),
                            cursorBrush = SolidColor(ElectricBlue),
                            minLines = 2,
                            maxLines = 4,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("create_routine_notes_input"),
                            decorationBox = { innerTextField ->
                                if (routineNotes.isEmpty()) {
                                    Text(
                                        text = "Add notes, focus, or instructions...",
                                        color = Color(0xFF8E8E93),
                                        fontSize = 14.sp
                                    )
                                }
                                innerTextField()
                            }
                        )
                    }
                }
            }

            // 2. Dynamic Exercise Template List
            if (exercises.isEmpty()) {
                // Sleek Empty State
                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .border(1.dp, DarkBorder, RoundedCornerShape(14.dp)),
                        color = Color(0xFF0D0E14)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp, horizontal = 20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(ElectricBlueAlpha),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FitnessCenter,
                                    contentDescription = null,
                                    tint = ElectricBlueLight,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Text(
                                text = "No exercises added yet",
                                color = TextWhite,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Tap below to build your routine template with target sets and reps.",
                                color = Color(0xFF8E8E93),
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                itemsIndexed(exercises, key = { _, ex -> ex.id }) { exIndex, ex ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .border(1.dp, Color(0xFF1E2538), RoundedCornerShape(14.dp)),
                        color = Color(0xFF121622)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp)
                        ) {
                            // Exercise Header
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
                                    ExerciseThumbnail(
                                        imageUrl = null,
                                        exerciseName = ex.name,
                                        size = 38.dp
                                    )

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = ex.name,
                                            color = TextWhite,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1
                                        )
                                        if (ex.primaryMuscle.isNotBlank()) {
                                            Text(
                                                text = ex.primaryMuscle,
                                                color = Color(0xFF8E8E93),
                                                fontSize = 12.sp
                                            )
                                        }
                                    }
                                }

                                // Reorder and Delete Actions
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
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.KeyboardArrowUp,
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
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.KeyboardArrowDown,
                                                contentDescription = "Move Down",
                                                tint = TextMuted,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                    IconButton(
                                        onClick = { exercises.removeAt(exIndex) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Remove",
                                            tint = MissedRed,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Default Sets Table Headers
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "SET",
                                    color = Color(0xFF8E8E93),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.width(36.dp),
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "KG",
                                    color = Color(0xFF8E8E93),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f),
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "REPS",
                                    color = Color(0xFF8E8E93),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f),
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.width(36.dp))
                            }

                            HorizontalDivider(
                                color = DarkBorder,
                                modifier = Modifier.padding(vertical = 6.dp)
                            )

                            // Default Sets Rows
                            ex.sets.forEachIndexed { setIndex, setItem ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Set Number Pill
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp, 32.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFF1A2234)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "${setIndex + 1}",
                                            color = TextWhite,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    // KG Input Box
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(36.dp)
                                            .background(Color(0xFF161B26), RoundedCornerShape(8.dp))
                                            .border(1.dp, DarkBorder, RoundedCornerShape(8.dp))
                                            .padding(horizontal = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        BasicTextField(
                                            value = setItem.weight,
                                            onValueChange = { setItem.weight = it },
                                            textStyle = TextStyle(
                                                color = TextWhite,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                textAlign = TextAlign.Center
                                            ),
                                            cursorBrush = SolidColor(ElectricBlue),
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            singleLine = true,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    // REPS Input Box
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(36.dp)
                                            .background(Color(0xFF161B26), RoundedCornerShape(8.dp))
                                            .border(1.dp, DarkBorder, RoundedCornerShape(8.dp))
                                            .padding(horizontal = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        BasicTextField(
                                            value = setItem.reps,
                                            onValueChange = { setItem.reps = it },
                                            textStyle = TextStyle(
                                                color = TextWhite,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                textAlign = TextAlign.Center
                                            ),
                                            cursorBrush = SolidColor(ElectricBlue),
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            singleLine = true,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    // Delete Set Action (if >1 set)
                                    if (ex.sets.size > 1) {
                                        IconButton(
                                            onClick = { ex.sets.removeAt(setIndex) },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Remove Set",
                                                tint = Color(0xFF64748B),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    } else {
                                        Spacer(modifier = Modifier.width(28.dp))
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // + Add Set Button
                            TextButton(
                                onClick = {
                                    val lastWeight = ex.sets.lastOrNull()?.weight ?: "0"
                                    val lastReps = ex.sets.lastOrNull()?.reps ?: "10"
                                    ex.sets.add(RoutineTemplateSet(weight = lastWeight, reps = lastReps))
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(34.dp)
                                    .background(Color(0x1A1877F2), RoundedCornerShape(8.dp))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    tint = ElectricBlue,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "+ Add Set",
                                    color = ElectricBlue,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }

            // 3. Prominent Full-Width "+ Add Exercise" Button
            item {
                OutlinedButton(
                    onClick = { showExercisePicker = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("create_routine_add_exercise_button"),
                    border = BorderStroke(1.5.dp, ElectricBlue),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color(0x0F1877F2),
                        contentColor = ElectricBlue
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = ElectricBlue,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "+ Add Exercise",
                        color = ElectricBlue,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    // Exercise Library Picker Bottom Sheet
    if (showExercisePicker) {
        AddExerciseBottomSheet(
            allExercises = allDbExercises,
            onDismiss = { showExercisePicker = false },
            onSelectExercise = { selectedExercise ->
                exercises.add(
                    RoutineTemplateExercise(
                        id = UUID.randomUUID().toString(),
                        name = selectedExercise.name,
                        primaryMuscle = selectedExercise.primaryMuscle,
                        equipmentType = selectedExercise.equipmentType,
                        sets = mutableStateListOf(
                            RoutineTemplateSet(weight = "0", reps = "10"),
                            RoutineTemplateSet(weight = "0", reps = "10"),
                            RoutineTemplateSet(weight = "0", reps = "10")
                        )
                    )
                )
                showExercisePicker = false
            },
            onConfirmSelection = { selectedList ->
                selectedList.forEach { selectedExercise ->
                    exercises.add(
                        RoutineTemplateExercise(
                            id = UUID.randomUUID().toString(),
                            name = selectedExercise.name,
                            primaryMuscle = selectedExercise.primaryMuscle,
                            equipmentType = selectedExercise.equipmentType,
                            sets = mutableStateListOf(
                                RoutineTemplateSet(weight = "0", reps = "10"),
                                RoutineTemplateSet(weight = "0", reps = "10"),
                                RoutineTemplateSet(weight = "0", reps = "10")
                            )
                        )
                    )
                }
                showExercisePicker = false
            }
        )
    }
}

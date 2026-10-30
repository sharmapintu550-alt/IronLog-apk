package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.auth.AuthManager
import com.example.data.AppDatabase
import com.example.data.entity.ExerciseEntity
import com.example.data.sync.CloudSyncManager
import com.example.ui.theme.ElectricBlue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

/**
 * Standard Add Exercises Header with "+ New" button for custom exercises.
 */
@Composable
fun AddExerciseHeader(
    totalCount: Int,
    onCreateExerciseClick: () -> Unit,
    onCloseClick: () -> Unit,
    title: String = "Add Exercises",
    subtitle: String = "$totalCount movements available"
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = title,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                color = Color(0xFF8E8E93),
                fontSize = 12.sp
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(
                onClick = onCreateExerciseClick,
                colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF1877F2)),
                modifier = Modifier.testTag("header_create_new_exercise_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "New",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
            IconButton(
                onClick = onCloseClick,
                modifier = Modifier.testTag("close_add_exercises_sheet_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Color.White
                )
            }
        }
    }
}

/**
 * Dedicated pinned "+ Create Custom Exercise" button component placed below filter chips.
 */
@Composable
fun PinnedCreateCustomExerciseButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .testTag("pinned_create_custom_exercise_button"),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF14141A),
        border = BorderStroke(1.dp, Color(0xFF232532))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF0F1B2B)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = ElectricBlue,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "+ Create Custom Exercise",
                        color = ElectricBlue,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Can't find your movement? Add it permanently",
                        color = Color(0xFF8E8E93),
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

/**
 * Dark AMOLED sheet for creating a custom exercise on the fly.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateCustomExerciseModalBottomSheet(
    onDismiss: () -> Unit,
    onCreateExercise: (ExerciseEntity, Double) -> Unit // returns created ExerciseEntity and barWeight
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    var name by remember { mutableStateOf("") }
    var selectedMuscle by remember { mutableStateOf("Chest") }
    var selectedEquipment by remember { mutableStateOf("Barbell") }
    var barWeightText by remember { mutableStateOf("20.0") }

    val muscleOptions = listOf("Chest", "Back", "Shoulders", "Legs", "Biceps", "Triceps", "Traps", "Abs & Core")
    val equipmentOptions = listOf("Barbell", "Dumbbell", "Cable", "Machine", "Bodyweight", "Smith Machine")

    val isBarbell = selectedEquipment.equals("Barbell", ignoreCase = true)

    // Autofocus the name field upon display
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF090A0E),
        dragHandle = {
            Surface(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .size(width = 38.dp, height = 4.dp),
                shape = CircleShape,
                color = Color(0xFF262833)
            ) {}
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp)
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
                        text = "Create Custom Exercise",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "Added to your database and active session",
                        color = Color(0xFF8E8E93),
                        fontSize = 12.sp
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF8E8E93))
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 1. Exercise Name Input
            Text(
                text = "EXERCISE NAME",
                color = Color(0xFF8E8E93),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp
            )
            Spacer(modifier = Modifier.height(6.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                placeholder = {
                    Text(
                        text = "e.g. Incline Smith Press, Nordic Curl",
                        color = Color(0xFF5E6072),
                        fontSize = 14.sp
                    )
                },
                textStyle = TextStyle(
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.clearFocus() }
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
                    .testTag("custom_exercise_name_input"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFF141418),
                    unfocusedContainerColor = Color(0xFF141418),
                    focusedBorderColor = ElectricBlue,
                    unfocusedBorderColor = Color(0xFF262833),
                    cursorColor = ElectricBlue
                )
            )

            Spacer(modifier = Modifier.height(18.dp))

            // 2. Primary Muscle Group
            Text(
                text = "PRIMARY MUSCLE GROUP",
                color = Color(0xFF8E8E93),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                muscleOptions.forEach { muscle ->
                    val isSelected = selectedMuscle == muscle
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) ElectricBlue else Color(0xFF141418))
                            .border(
                                1.dp,
                                if (isSelected) ElectricBlue else Color(0xFF262833),
                                RoundedCornerShape(10.dp)
                            )
                            .clickable { selectedMuscle = muscle }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                            .testTag("custom_muscle_chip_$muscle")
                    ) {
                        Text(
                            text = muscle,
                            color = if (isSelected) Color.White else Color(0xFF9CA3AF),
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 3. Equipment Type
            Text(
                text = "EQUIPMENT TYPE",
                color = Color(0xFF8E8E93),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                equipmentOptions.forEach { eq ->
                    val isSelected = selectedEquipment == eq
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) ElectricBlue else Color(0xFF141418))
                            .border(
                                1.dp,
                                if (isSelected) ElectricBlue else Color(0xFF262833),
                                RoundedCornerShape(10.dp)
                            )
                            .clickable {
                                selectedEquipment = eq
                                if (eq.equals("Barbell", ignoreCase = true) && barWeightText.isBlank()) {
                                    barWeightText = "20.0"
                                }
                            }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                            .testTag("custom_equipment_chip_$eq")
                    ) {
                        Text(
                            text = eq,
                            color = if (isSelected) Color.White else Color(0xFF9CA3AF),
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }

            // 4. Barbell Weight Section (when Barbell is selected)
            if (isBarbell) {
                Spacer(modifier = Modifier.height(18.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "BARBELL WEIGHT (KG)",
                        color = Color(0xFF8E8E93),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )
                    Text(
                        text = "Olympic standard is 20 kg",
                        color = ElectricBlue,
                        fontSize = 11.sp
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    listOf("20.0", "15.0", "10.0", "0.0").forEach { weightPreset ->
                        val isPresetSelected = barWeightText == weightPreset
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isPresetSelected) Color(0xFF0F1E36) else Color(0xFF141418))
                            .border(
                                1.dp,
                                if (isPresetSelected) ElectricBlue else Color(0xFF262833),
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { barWeightText = weightPreset }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "${weightPreset}kg",
                                color = if (isPresetSelected) ElectricBlue else Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    OutlinedTextField(
                        value = barWeightText,
                        onValueChange = { barWeightText = it },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("custom_bar_weight_input"),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        textStyle = TextStyle(color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFF141418),
                            unfocusedContainerColor = Color(0xFF141418),
                            focusedBorderColor = ElectricBlue,
                            unfocusedBorderColor = Color(0xFF262833)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(26.dp))

            // Action Buttons: Cancel and Create & Add
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("cancel_custom_exercise_button"),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFF2E3140))
                ) {
                    Text("Cancel", color = Color(0xFF8E8E93), fontWeight = FontWeight.SemiBold)
                }

                Button(
                    onClick = {
                        if (name.isNotBlank()) {
                            val barWeight = barWeightText.toDoubleOrNull() ?: if (isBarbell) 20.0 else 0.0
                            val category = mapMuscleToCategory(selectedMuscle)
                            val newExercise = ExerciseEntity(
                                id = UUID.randomUUID().toString(),
                                name = name.trim(),
                                muscleGroup = selectedMuscle,
                                equipment = selectedEquipment,
                                category = category,
                                equipmentType = selectedEquipment,
                                notes = "",
                                isCustom = true,
                                primaryMuscle = selectedMuscle,
                                secondaryMuscles = emptyList(),
                                equipmentCategory = selectedEquipment,
                                trackingType = if (selectedEquipment.equals("Bodyweight", true)) "BODYWEIGHT_REP" else "WEIGHT_AND_REPS",
                                defaultRestSeconds = 90,
                                barbellWeightKg = barWeight,
                                customBarWeight = barWeight.toFloat()
                            )
                            onCreateExercise(newExercise, barWeight)
                        }
                    },
                    enabled = name.isNotBlank(),
                    modifier = Modifier
                        .weight(1.5f)
                        .height(48.dp)
                        .testTag("create_and_add_custom_exercise_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ElectricBlue,
                        disabledContainerColor = ElectricBlue.copy(alpha = 0.3f)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Create & Add",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

/**
 * Shared Add Exercise Bottom Sheet used across Active Workout and Routine Creator.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExerciseBottomSheet(
    allExercises: List<ExerciseEntity>,
    alreadySelectedIds: Set<UUID> = emptySet(),
    exerciseToReplaceId: UUID? = null,
    initialMuscleFilter: String? = null,
    isSwapMode: Boolean = false,
    onDismiss: () -> Unit,
    onSelectExercise: ((ExerciseEntity) -> Unit)? = null,
    onConfirmSelection: ((List<ExerciseEntity>) -> Unit)? = null,
    onCreateAndAddCustomExercise: ((ExerciseEntity, Double) -> Unit)? = null
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val initialCategory = remember(initialMuscleFilter) {
        if (!initialMuscleFilter.isNullOrBlank()) {
            val m = initialMuscleFilter.trim().lowercase()
            when {
                m.contains("chest") -> "Chest"
                m.contains("back") || m.contains("lat") -> "Back"
                m.contains("shoulder") || m.contains("delt") || m.contains("trap") -> "Shoulders"
                m.contains("bicep") -> "Biceps"
                m.contains("tricep") -> "Triceps"
                m.contains("leg") || m.contains("quad") || m.contains("hamstring") || m.contains("glute") || m.contains("calf") || m.contains("calves") -> "Legs"
                m.contains("abs") || m.contains("core") || m.contains("abdom") -> "Abs & Core"
                else -> "All"
            }
        } else {
            "All"
        }
    }

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf(initialCategory) }
    var selectedEquipmentFilter by remember { mutableStateOf("All Equipment") }
    val selectedIds = remember { mutableStateListOf<UUID>() }

    var showCreateCustomSheet by remember { mutableStateOf(false) }

    val categoryChips = listOf("All", "Chest", "Back", "Shoulders", "Biceps", "Triceps", "Legs", "Abs & Core", "Forearms", "Traps")
    val equipmentChips = listOf("All Equipment", "Barbell", "Dumbbell", "Cable", "Machine", "Smith Machine", "Bodyweight")

    val filteredExercises = remember(allExercises, searchQuery, selectedCategoryFilter, selectedEquipmentFilter) {
        allExercises.filter { exercise ->
            val matchesQuery = searchQuery.isBlank() ||
                    exercise.name.contains(searchQuery, ignoreCase = true) ||
                    exercise.primaryMuscle.contains(searchQuery, ignoreCase = true) ||
                    exercise.muscleGroup.contains(searchQuery, ignoreCase = true) ||
                    exercise.category.contains(searchQuery, ignoreCase = true) ||
                    exercise.equipmentType.contains(searchQuery, ignoreCase = true)

            val matchesCategory = when (selectedCategoryFilter) {
                "All" -> true
                "Biceps" -> {
                    exercise.primaryMuscle.contains("Bicep", ignoreCase = true) ||
                    exercise.muscleGroup.contains("Bicep", ignoreCase = true) ||
                    exercise.category.contains("Bicep", ignoreCase = true) ||
                    exercise.name.contains("Bicep", ignoreCase = true) ||
                    ((exercise.primaryMuscle.contains("Arm", ignoreCase = true) ||
                      exercise.muscleGroup.contains("Arm", ignoreCase = true) ||
                      exercise.category.contains("Arm", ignoreCase = true)) &&
                     (exercise.name.contains("curl", ignoreCase = true) ||
                      exercise.name.contains("bicep", ignoreCase = true) ||
                      exercise.name.contains("chin-up", ignoreCase = true) ||
                      exercise.name.contains("chin up", ignoreCase = true) ||
                      exercise.name.contains("zottman", ignoreCase = true)))
                }
                "Triceps" -> {
                    exercise.primaryMuscle.contains("Tricep", ignoreCase = true) ||
                    exercise.muscleGroup.contains("Tricep", ignoreCase = true) ||
                    exercise.category.contains("Tricep", ignoreCase = true) ||
                    exercise.name.contains("Tricep", ignoreCase = true) ||
                    ((exercise.primaryMuscle.contains("Arm", ignoreCase = true) ||
                      exercise.muscleGroup.contains("Arm", ignoreCase = true) ||
                      exercise.category.contains("Arm", ignoreCase = true)) &&
                     (exercise.name.contains("tricep", ignoreCase = true) ||
                      exercise.name.contains("extension", ignoreCase = true) ||
                      exercise.name.contains("pushdown", ignoreCase = true) ||
                      exercise.name.contains("dip", ignoreCase = true) ||
                      exercise.name.contains("skull", ignoreCase = true) ||
                      exercise.name.contains("jm press", ignoreCase = true) ||
                      exercise.name.contains("tate press", ignoreCase = true) ||
                      exercise.name.contains("close-grip", ignoreCase = true) ||
                      exercise.name.contains("close grip", ignoreCase = true))) ||
                    exercise.name.contains("diamond push-up", ignoreCase = true) ||
                    exercise.name.contains("diamond pushup", ignoreCase = true)
                }
                "Chest" -> exercise.primaryMuscle.contains("Chest", ignoreCase = true) ||
                        exercise.muscleGroup.contains("Chest", ignoreCase = true) ||
                        exercise.category.contains("Chest", ignoreCase = true)
                "Back" -> exercise.primaryMuscle.contains("Back", ignoreCase = true) ||
                        exercise.primaryMuscle.contains("Lat", ignoreCase = true) ||
                        exercise.primaryMuscle.contains("Trap", ignoreCase = true) ||
                        exercise.muscleGroup.contains("Back", ignoreCase = true) ||
                        exercise.category.contains("Back", ignoreCase = true)
                "Shoulders" -> exercise.primaryMuscle.contains("Shoulder", ignoreCase = true) ||
                        exercise.primaryMuscle.contains("Deltoid", ignoreCase = true) ||
                        exercise.muscleGroup.contains("Shoulder", ignoreCase = true) ||
                        exercise.category.contains("Shoulder", ignoreCase = true)
                "Legs" -> exercise.primaryMuscle.contains("Leg", ignoreCase = true) ||
                        exercise.primaryMuscle.contains("Quad", ignoreCase = true) ||
                        exercise.primaryMuscle.contains("Hamstring", ignoreCase = true) ||
                        exercise.primaryMuscle.contains("Calf", ignoreCase = true) ||
                        exercise.primaryMuscle.contains("Calves", ignoreCase = true) ||
                        exercise.primaryMuscle.contains("Glute", ignoreCase = true) ||
                        exercise.muscleGroup.contains("Leg", ignoreCase = true) ||
                        exercise.category.contains("Leg", ignoreCase = true)
                "Abs & Core", "Abs", "Core" -> exercise.category.contains("Core", true) ||
                        exercise.category.contains("Abs", true) ||
                        exercise.primaryMuscle.contains("Abdominal", true) ||
                        exercise.primaryMuscle.contains("Core", true) ||
                        exercise.primaryMuscle.contains("Abs", true) ||
                        exercise.muscleGroup.contains("Core", true) ||
                        exercise.muscleGroup.contains("Abs", true)
                "Forearms" -> exercise.primaryMuscle.contains("Forearm", ignoreCase = true) ||
                        exercise.muscleGroup.contains("Forearm", ignoreCase = true) ||
                        exercise.category.contains("Forearm", ignoreCase = true) ||
                        exercise.secondaryMuscles.any { it.contains("Forearm", ignoreCase = true) || it.contains("Grip", ignoreCase = true) } ||
                        exercise.name.contains("Wrist Curl", ignoreCase = true) ||
                        exercise.name.contains("Dead Hang", ignoreCase = true) ||
                        exercise.name.contains("Plate Pinch", ignoreCase = true) ||
                        exercise.name.contains("Wrist Roller", ignoreCase = true)
                "Traps" -> exercise.primaryMuscle.contains("Trap", ignoreCase = true) ||
                        exercise.muscleGroup.contains("Trap", ignoreCase = true) ||
                        exercise.category.contains("Trap", ignoreCase = true) ||
                        exercise.secondaryMuscles.any { it.contains("Trap", ignoreCase = true) } ||
                        exercise.name.contains("Shrug", ignoreCase = true)
                else -> exercise.category.contains(selectedCategoryFilter, ignoreCase = true) ||
                        exercise.primaryMuscle.contains(selectedCategoryFilter, ignoreCase = true) ||
                        exercise.muscleGroup.contains(selectedCategoryFilter, ignoreCase = true)
            }

            val matchesEquipment = when (selectedEquipmentFilter) {
                "All Equipment" -> true
                "Smith Machine" -> exercise.equipmentType.contains("Smith", ignoreCase = true) ||
                        exercise.equipmentCategory.contains("Smith", ignoreCase = true) ||
                        exercise.equipment.contains("Smith", ignoreCase = true) ||
                        exercise.name.contains("Smith", ignoreCase = true)
                "Bodyweight" -> exercise.equipmentType.contains("Bodyweight", ignoreCase = true) ||
                        exercise.equipmentCategory.contains("Bodyweight", ignoreCase = true) ||
                        exercise.equipment.contains("Bodyweight", ignoreCase = true)
                else -> exercise.equipmentType.equals(selectedEquipmentFilter, ignoreCase = true) ||
                        exercise.equipmentCategory.equals(selectedEquipmentFilter, ignoreCase = true) ||
                        exercise.equipment.equals(selectedEquipmentFilter, ignoreCase = true) ||
                        exercise.equipmentType.contains(selectedEquipmentFilter, ignoreCase = true)
            }

            matchesQuery && matchesCategory && matchesEquipment
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF0D0D10),
        dragHandle = {
            Surface(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .size(width = 38.dp, height = 4.dp),
                shape = CircleShape,
                color = Color(0xFF2E2E36)
            ) {}
        }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            val swapActive = isSwapMode || exerciseToReplaceId != null
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding()
                    .imePadding()
                    .padding(bottom = if (selectedIds.isNotEmpty() && !swapActive) 80.dp else 16.dp)
            ) {
                // Header with + New button
                AddExerciseHeader(
                    totalCount = allExercises.size,
                    onCreateExerciseClick = {
                        showCreateCustomSheet = true
                    },
                    onCloseClick = onDismiss,
                    title = if (swapActive) "⇄ Swap Exercise" else "Add Exercises",
                    subtitle = if (swapActive) "Select an alternate movement for this workout session" else "${allExercises.size} movements available"
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Sticky Search Bar
                TextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            text = "Search exercises...",
                            color = Color(0xFF8E8E93),
                            fontSize = 15.sp
                        )
                    },
                    textStyle = TextStyle(
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Normal
                    ),
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = Color(0xFF1877F2),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                        focusedContainerColor = Color(0xFF16161C),
                        unfocusedContainerColor = Color(0xFF16161C)
                    ),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = Color(0xFF8E8E93)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear search",
                                    tint = Color(0xFF8E8E93)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .border(1.dp, Color(0xFF272732), RoundedCornerShape(12.dp))
                        .testTag("exercise_picker_search_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Muscle Category Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categoryChips.forEach { cat ->
                        val isSelected = selectedCategoryFilter == cat
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                selectedCategoryFilter = cat
                            },
                            label = {
                                Text(
                                    text = cat,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF1877F2),
                                selectedLabelColor = Color.White,
                                containerColor = Color(0xFF16161A),
                                labelColor = Color(0xFF8E8E93)
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = if (isSelected) Color(0xFF1877F2) else Color(0xFF26262D),
                                selectedBorderColor = Color(0xFF1877F2)
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Equipment Filter Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    equipmentChips.forEach { eq ->
                        val isSelected = selectedEquipmentFilter == eq
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                selectedEquipmentFilter = eq
                            },
                            label = {
                                Text(
                                    text = eq,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF1E293B),
                                selectedLabelColor = Color(0xFF60A5FA),
                                containerColor = Color(0xFF131317),
                                labelColor = Color(0xFF8E8E93)
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = if (isSelected) Color(0xFF1877F2).copy(alpha = 0.5f) else Color(0xFF222228),
                                selectedBorderColor = Color(0xFF1877F2)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Pinned "+ Create Custom Exercise" button right below category chips
                PinnedCreateCustomExerciseButton(
                    onClick = {
                        showCreateCustomSheet = true
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Exercise List
                if (filteredExercises.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.FitnessCenter,
                                contentDescription = null,
                                tint = Color(0xFF8E8E93).copy(alpha = 0.4f),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = if (searchQuery.isNotBlank()) {
                                    "No exercises found for \"$searchQuery\""
                                } else if (selectedCategoryFilter != "All") {
                                    "No exercises found for $selectedCategoryFilter"
                                } else {
                                    "No exercises found"
                                },
                                color = Color(0xFF8E8E93),
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { showCreateCustomSheet = true },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1877F2)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("+ Create Custom Exercise", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(horizontal = 16.dp),
                        contentPadding = PaddingValues(top = 4.dp, bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredExercises, key = { it.id }) { exercise ->
                            val isAlreadyIn = alreadySelectedIds.contains(exercise.uuid)
                            val isChecked = selectedIds.contains(exercise.uuid)

                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable(enabled = !isAlreadyIn) {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        if (swapActive) {
                                            onSelectExercise?.invoke(exercise)
                                            onDismiss()
                                        } else {
                                            if (isChecked) {
                                                selectedIds.remove(exercise.uuid)
                                            } else {
                                                selectedIds.add(exercise.uuid)
                                            }
                                        }
                                    }
                                    .testTag("exercise_picker_item_${exercise.id}"),
                                color = if (isChecked) Color(0xFF1877F2).copy(alpha = 0.12f) else Color(0xFF141417),
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(
                                    width = 1.dp,
                                    color = if (isChecked) Color(0xFF1877F2) else Color(0xFF222228)
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        ExerciseThumbnail(
                                            imageUrl = exercise.imageUrl,
                                            exerciseName = exercise.name
                                        )

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column {
                                            Text(
                                                text = exercise.name,
                                                color = if (isAlreadyIn) Color(0xFF8E8E93).copy(alpha = 0.5f) else Color.White,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                Text(
                                                    text = exercise.primaryMuscle,
                                                    color = Color(0xFF8E8E93),
                                                    fontSize = 11.sp
                                                )
                                                Text(
                                                    text = "•",
                                                    color = Color(0xFF3A3A44),
                                                    fontSize = 11.sp
                                                )
                                                Text(
                                                    text = exercise.equipmentType,
                                                    color = Color(0xFF60A5FA),
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }
                                        }
                                    }

                                    // Selection checkbox, "Swap" action pill, or "Added" state
                                    if (isAlreadyIn) {
                                        Text(
                                            text = "Added",
                                            color = Color(0xFF8E8E93).copy(alpha = 0.4f),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    } else if (swapActive) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = Color(0xFF1877F2).copy(alpha = 0.15f),
                                            border = BorderStroke(1.dp, Color(0xFF1877F2).copy(alpha = 0.5f))
                                        ) {
                                            Text(
                                                text = "Swap",
                                                color = Color(0xFF60A5FA),
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                            )
                                        }
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    if (isChecked) Color(0xFF1877F2) else Color.Transparent
                                                )
                                                .border(
                                                    width = 2.dp,
                                                    color = if (isChecked) Color(0xFF1877F2) else Color(0xFF2E3140),
                                                    shape = CircleShape
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (isChecked) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = null,
                                                    tint = Color.White,
                                                    modifier = Modifier.size(14.dp)
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

            // Floating Confirm Button (appears when count > 0)
            if (selectedIds.isNotEmpty() && !swapActive) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                        .imePadding()
                        .padding(16.dp)
                ) {
                    Button(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            val chosen = allExercises.filter { selectedIds.contains(it.uuid) }
                            onConfirmSelection?.invoke(chosen)
                            onDismiss()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("exercise_picker_confirm_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1877F2)),
                        shape = RoundedCornerShape(16.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Add ${selectedIds.size} Exercise${if (selectedIds.size > 1) "s" else ""}",
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }
    }

    // Modal Sheet for Custom Exercise Creation
    if (showCreateCustomSheet) {
        CreateCustomExerciseModalBottomSheet(
            onDismiss = { showCreateCustomSheet = false },
            onCreateExercise = { candidateExercise, barWeight ->
                showCreateCustomSheet = false
                coroutineScope.launch {
                    runCatching {
                        val db = AppDatabase.getDatabase(context)
                        val authManager = AuthManager.getInstance(context)
                        val syncManager = CloudSyncManager.getInstance(context)
                        val uid = authManager.currentUser?.uid ?: "guest"

                        val trimmedName = candidateExercise.name.trim()
                        val existing = withContext(Dispatchers.IO) {
                            db.exerciseDao().getExerciseByName(trimmedName)
                        }

                        val finalExercise = if (existing != null) {
                            existing
                        } else {
                            val exerciseToInsert = candidateExercise.copy(
                                id = candidateExercise.id.ifBlank { UUID.randomUUID().toString() },
                                name = trimmedName,
                                muscleGroup = candidateExercise.primaryMuscle,
                                equipment = candidateExercise.equipmentType,
                                category = mapMuscleToCategory(candidateExercise.primaryMuscle),
                                equipmentType = candidateExercise.equipmentType,
                                primaryMuscle = candidateExercise.primaryMuscle,
                                equipmentCategory = candidateExercise.equipmentType,
                                isCustom = true,
                                userId = uid,
                                barbellWeightKg = barWeight,
                                customBarWeight = barWeight.toFloat()
                            )
                            withContext(Dispatchers.IO) {
                                db.exerciseDao().insertCustomExercise(exerciseToInsert)
                            }
                            exerciseToInsert
                        }

                        if (uid != "guest" && uid.isNotBlank()) {
                            withContext(Dispatchers.IO) {
                                syncManager.pushCustomExercise(uid, finalExercise)
                            }
                        }

                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "Custom exercise created and added!", Toast.LENGTH_SHORT).show()
                            if (onCreateAndAddCustomExercise != null) {
                                onCreateAndAddCustomExercise.invoke(finalExercise, barWeight)
                            } else {
                                onSelectExercise?.invoke(finalExercise) ?: onConfirmSelection?.invoke(listOf(finalExercise))
                            }
                            onDismiss()
                        }
                    }.onFailure { err ->
                        android.util.Log.e("AddExerciseBottomSheet", "Error creating custom exercise: ${err.message}", err)
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "Custom exercise created and added!", Toast.LENGTH_SHORT).show()
                            if (onCreateAndAddCustomExercise != null) {
                                onCreateAndAddCustomExercise.invoke(candidateExercise, barWeight)
                            } else {
                                onSelectExercise?.invoke(candidateExercise) ?: onConfirmSelection?.invoke(listOf(candidateExercise))
                            }
                            onDismiss()
                        }
                    }
                }
            }
        )
    }
}

/**
 * Maps single-selection muscle chip to parent exercise category.
 */
private fun mapMuscleToCategory(muscle: String): String {
    return when (muscle.trim()) {
        "Chest" -> "Chest"
        "Back" -> "Back"
        "Shoulders", "Traps" -> "Shoulders"
        "Legs" -> "Legs"
        "Biceps", "Triceps" -> "Arms"
        "Abs & Core" -> "Core"
        else -> "Other"
    }
}

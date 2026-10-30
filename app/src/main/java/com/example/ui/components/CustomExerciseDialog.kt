package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.ExerciseEntity
import com.example.ui.theme.*
import java.util.UUID

data class BarWeightPreset(
    val label: String,
    val weightKg: Float,
    val isCustom: Boolean = false
)

private val BARBELL_PRESETS = listOf(
    BarWeightPreset("Olympic 20 kg", 20.0f),
    BarWeightPreset("Women's 15 kg", 15.0f),
    BarWeightPreset("EZ Curl 10 kg", 10.0f),
    BarWeightPreset("Smith Machine 0 kg", 0.0f),
    BarWeightPreset("Smith Machine 7.5 kg", 7.5f),
    BarWeightPreset("Trap Bar 25 kg", 25.0f),
    BarWeightPreset("Custom kg", 20.0f, isCustom = true)
)

private val PRIMARY_MUSCLE_GROUPS = listOf(
    "Chest", "Back", "Shoulders", "Biceps", "Triceps",
    "Quads", "Hamstrings", "Glutes", "Calves", "Core", "Full Body", "Cardio"
)

private val SECONDARY_MUSCLE_OPTIONS = listOf(
    "Chest", "Back", "Shoulders", "Biceps", "Triceps", "Forearms",
    "Traps", "Quads", "Hamstrings", "Glutes", "Calves", "Core", "Lower Back", "Lats"
)

private val EQUIPMENT_TYPES = listOf(
    "Barbell", "Dumbbell", "Cable", "Machine", "Bodyweight", "Smith Machine", "Other"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomExerciseDialog(
    onDismiss: () -> Unit,
    onSave: (ExerciseEntity) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val focusManager = LocalFocusManager.current

    var name by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf(false) }
    var primaryMuscle by remember { mutableStateOf("Chest") }
    var secondaryMuscles by remember { mutableStateOf(setOf<String>()) }
    var equipmentType by remember { mutableStateOf("Barbell") }

    // Dynamic Equipment Settings
    var selectedBarPreset by remember { mutableStateOf(BARBELL_PRESETS[0]) }
    var customBarWeightText by remember { mutableStateOf("20") }
    var isDualPulley by remember { mutableStateOf(false) }

    // Setup notes
    var notes by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = AmoledBlack,
        dragHandle = {
            Surface(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .width(36.dp)
                    .height(4.dp),
                shape = CircleShape,
                color = DarkBorderHover
            ) {}
        },
        modifier = Modifier
            .fillMaxHeight(0.92f)
            .testTag("custom_exercise_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .imePadding()
                .padding(bottom = 24.dp)
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
                        color = TextWhite,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Configure equipment presets and target muscles",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("close_custom_exercise_dialog")
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 1. Exercise Name
            Text(
                text = "EXERCISE NAME *",
                color = ElectricBlueLight,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    if (nameError && it.isNotBlank()) nameError = false
                },
                placeholder = { Text("e.g. Belt Squat, Pendlay Row, Incline DB Press", color = TextDark) },
                isError = nameError,
                supportingText = if (nameError) {
                    { Text("Exercise name is required", color = FailureRed, fontSize = 11.sp) }
                } else null,
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ElectricBlue,
                    unfocusedBorderColor = DarkBorder,
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextWhite,
                    focusedContainerColor = DarkCardBg,
                    unfocusedContainerColor = DarkCardBg,
                    errorBorderColor = FailureRed
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("custom_exercise_name_input")
            )

            Spacer(modifier = Modifier.height(18.dp))

            // 2. Primary Muscle Group
            Text(
                text = "PRIMARY MUSCLE GROUP",
                color = ElectricBlueLight,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PRIMARY_MUSCLE_GROUPS.forEach { muscle ->
                    val isSelected = muscle.equals(primaryMuscle, ignoreCase = true)
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            primaryMuscle = muscle
                            // Remove from secondary muscles if present
                            secondaryMuscles = secondaryMuscles - muscle
                        },
                        label = {
                            Text(
                                text = muscle,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = DarkCardBg,
                            labelColor = TextMuted,
                            selectedContainerColor = ElectricBlue,
                            selectedLabelColor = TextWhite
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = DarkBorder,
                            selectedBorderColor = ElectricBlue
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("primary_muscle_chip_${muscle.lowercase().replace(" ", "_")}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 3. Secondary Muscles
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SECONDARY MUSCLES",
                    color = ElectricBlueLight,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                if (secondaryMuscles.isNotEmpty()) {
                    Text(
                        text = "${secondaryMuscles.size} selected",
                        color = StreakGreen,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Enhances recovery heatmap and muscle balance tracking",
                color = TextDark,
                fontSize = 11.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SECONDARY_MUSCLE_OPTIONS.filterNot { it.equals(primaryMuscle, ignoreCase = true) }.forEach { muscle ->
                    val isSelected = secondaryMuscles.contains(muscle)
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            secondaryMuscles = if (isSelected) {
                                secondaryMuscles - muscle
                            } else {
                                secondaryMuscles + muscle
                            }
                        },
                        label = {
                            Text(
                                text = muscle,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = DarkCardBg,
                            labelColor = TextMuted,
                            selectedContainerColor = ElectricBlueAlpha,
                            selectedLabelColor = ElectricBlueLight
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = DarkBorder,
                            selectedBorderColor = ElectricBlueLight
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("secondary_muscle_chip_${muscle.lowercase().replace(" ", "_")}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 4. Equipment Type Selector
            Text(
                text = "EQUIPMENT TYPE",
                color = ElectricBlueLight,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                EQUIPMENT_TYPES.forEach { equip ->
                    val isSelected = equip.equals(equipmentType, ignoreCase = true)
                    FilterChip(
                        selected = isSelected,
                        onClick = { equipmentType = equip },
                        label = {
                            Text(
                                text = equip,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = DarkCardBg,
                            labelColor = TextMuted,
                            selectedContainerColor = ElectricBlue,
                            selectedLabelColor = TextWhite
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = DarkBorder,
                            selectedBorderColor = ElectricBlue
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("equipment_chip_${equip.lowercase().replace(" ", "_")}")
                    )
                }
            }

            // Dynamic Equipment Settings: Barbell or Smith Machine
            AnimatedVisibility(
                visible = equipmentType.equals("Barbell", ignoreCase = true) ||
                        equipmentType.equals("Smith Machine", ignoreCase = true),
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                        .background(DarkCardBg, RoundedCornerShape(14.dp))
                        .border(1.dp, DarkBorderHover, RoundedCornerShape(14.dp))
                        .padding(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.FitnessCenter,
                            contentDescription = null,
                            tint = ElectricBlue,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "DEFAULT BAR WEIGHT (PLATE CALCULATOR)",
                            color = ElectricBlueLight,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Used automatically in Plate Calculator & barbell tare loads",
                        color = TextMuted,
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        BARBELL_PRESETS.forEach { preset ->
                            val isSelected = selectedBarPreset.label == preset.label
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .border(
                                        1.dp,
                                        if (isSelected) ElectricBlue else DarkBorder,
                                        RoundedCornerShape(10.dp)
                                    )
                                    .clickable { selectedBarPreset = preset }
                                    .testTag("bar_weight_preset_${preset.label.lowercase().replace(" ", "_").replace("'", "")}"),
                                color = if (isSelected) ElectricBlueAlpha else DarkCardSurface
                            ) {
                                Text(
                                    text = preset.label,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    color = if (isSelected) TextWhite else TextMuted,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }

                    // Custom weight input if custom preset chosen
                    if (selectedBarPreset.isCustom) {
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = customBarWeightText,
                            onValueChange = { customBarWeightText = it.filter { ch -> ch.isDigit() || ch == '.' } },
                            label = { Text("Bar Weight (kg)", color = TextMuted) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ElectricBlue,
                                unfocusedBorderColor = DarkBorder,
                                focusedTextColor = TextWhite,
                                unfocusedTextColor = TextWhite,
                                focusedContainerColor = DarkCardSurface,
                                unfocusedContainerColor = DarkCardSurface
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("custom_bar_weight_input")
                        )
                    }
                }
            }

            // Dynamic Equipment Settings: Cable Dual Pulley
            AnimatedVisibility(
                visible = equipmentType.equals("Cable", ignoreCase = true),
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    shape = RoundedCornerShape(14.dp),
                    color = DarkCardBg,
                    border = BorderStroke(1.dp, DarkBorderHover)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Dual Pulley (1:2 Ratio)",
                                color = TextWhite,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Single pin half-weight calculation standard",
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }
                        Switch(
                            checked = isDualPulley,
                            onCheckedChange = { isDualPulley = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = TextWhite,
                                checkedTrackColor = ElectricBlue,
                                uncheckedThumbColor = TextMuted,
                                uncheckedTrackColor = DarkCardSurface,
                                uncheckedBorderColor = DarkBorder
                            ),
                            modifier = Modifier.testTag("dual_pulley_switch")
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 5. Exercise Notes / Instructions
            Text(
                text = "NOTES & SETUP CUES (OPTIONAL)",
                color = ElectricBlueLight,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                placeholder = { Text("e.g. Pin #5, bench at 30°, pause 1 sec at bottom", color = TextDark) },
                minLines = 2,
                maxLines = 4,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { focusManager.clearFocus() }
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ElectricBlue,
                    unfocusedBorderColor = DarkBorder,
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextWhite,
                    focusedContainerColor = DarkCardBg,
                    unfocusedContainerColor = DarkCardBg
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("custom_exercise_notes_input")
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Action Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("cancel_custom_exercise_button"),
                    border = BorderStroke(1.dp, DarkBorder),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextMuted)
                ) {
                    Text("Cancel", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }

                Button(
                    onClick = {
                        if (name.isBlank()) {
                            nameError = true
                            return@Button
                        }

                        val resolvedBarWeight = when {
                            equipmentType.equals("Barbell", ignoreCase = true) ||
                                    equipmentType.equals("Smith Machine", ignoreCase = true) -> {
                                if (selectedBarPreset.isCustom) {
                                    customBarWeightText.toFloatOrNull() ?: 20.0f
                                } else {
                                    selectedBarPreset.weightKg
                                }
                            }
                            else -> 0.0f
                        }

                        val customExercise = ExerciseEntity(
                            id = "ex_custom_" + UUID.randomUUID().toString().take(8),
                            name = name.trim(),
                            muscleGroup = primaryMuscle,
                            equipment = equipmentType,
                            category = primaryMuscle,
                            equipmentType = equipmentType,
                            notes = notes.trim(),
                            primaryMuscle = primaryMuscle,
                            secondaryMuscles = secondaryMuscles.toList(),
                            equipmentCategory = equipmentType,
                            instructions = notes.trim(),
                            isCustom = true,
                            barbellWeightKg = resolvedBarWeight.toDouble(),
                            customBarWeight = resolvedBarWeight,
                            isDualPulley = isDualPulley
                        )
                        onSave(customExercise)
                    },
                    modifier = Modifier
                        .weight(1.5f)
                        .height(48.dp)
                        .testTag("save_custom_exercise_confirm_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Save Exercise", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                }
            }
        }
    }
}

package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import com.example.ui.components.CustomExerciseDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.ExerciseEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.WorkoutViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExercisesScreen(
    viewModel: WorkoutViewModel
) {
    val context = LocalContext.current
    val allExercises by viewModel.exercises.collectAsState()
    val routines by viewModel.routines.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var showAddDialog by remember { mutableStateOf(false) }

    var selectedExerciseForHistory by remember { mutableStateOf<ExerciseEntity?>(null) }
    var exerciseForAddToRoutine by remember { mutableStateOf<ExerciseEntity?>(null) }

    val categories = listOf("All", "Custom", "Chest", "Back", "Legs", "Shoulders", "Arms", "Abs", "Forearms", "Traps")

    val filteredExercises = remember(allExercises, searchQuery, selectedCategory) {
        allExercises
            .filter { ex ->
                val matchesCategory = when (selectedCategory) {
                    "All" -> true
                    "Custom" -> ex.isCustom
                    "Abs", "Core", "Abs & Core" ->
                        ex.muscleGroup.contains("Abs", ignoreCase = true) ||
                        ex.muscleGroup.contains("Core", ignoreCase = true) ||
                        ex.primaryMuscle.contains("Abs", ignoreCase = true) ||
                        ex.primaryMuscle.contains("Core", ignoreCase = true) ||
                        ex.category.contains("Abs", ignoreCase = true) ||
                        ex.category.contains("Core", ignoreCase = true)
                    "Forearms" ->
                        ex.muscleGroup.contains("Forearm", ignoreCase = true) ||
                        ex.primaryMuscle.contains("Forearm", ignoreCase = true) ||
                        ex.category.contains("Forearm", ignoreCase = true) ||
                        ex.secondaryMuscles.any { it.contains("Forearm", ignoreCase = true) || it.contains("Grip", ignoreCase = true) } ||
                        ex.name.contains("Wrist Curl", ignoreCase = true) ||
                        ex.name.contains("Dead Hang", ignoreCase = true) ||
                        ex.name.contains("Plate Pinch", ignoreCase = true) ||
                        ex.name.contains("Wrist Roller", ignoreCase = true)
                    "Traps" ->
                        ex.muscleGroup.contains("Trap", ignoreCase = true) ||
                        ex.primaryMuscle.contains("Trap", ignoreCase = true) ||
                        ex.category.contains("Trap", ignoreCase = true) ||
                        ex.secondaryMuscles.any { it.contains("Trap", ignoreCase = true) } ||
                        ex.name.contains("Shrug", ignoreCase = true)
                    "Arms" ->
                        ex.muscleGroup.contains("Arm", ignoreCase = true) ||
                        ex.primaryMuscle.contains("Arm", ignoreCase = true) ||
                        ex.muscleGroup.contains("Bicep", ignoreCase = true) ||
                        ex.muscleGroup.contains("Tricep", ignoreCase = true) ||
                        ex.muscleGroup.contains("Forearm", ignoreCase = true) ||
                        ex.category.contains("Arm", ignoreCase = true)
                    else -> ex.muscleGroup.contains(selectedCategory, ignoreCase = true) ||
                            ex.primaryMuscle.contains(selectedCategory, ignoreCase = true) ||
                            ex.category.contains(selectedCategory, ignoreCase = true)
                }
                val matchesSearch = searchQuery.isBlank() ||
                        ex.name.contains(searchQuery, ignoreCase = true) ||
                        ex.muscleGroup.contains(searchQuery, ignoreCase = true) ||
                        ex.primaryMuscle.contains(searchQuery, ignoreCase = true)
                matchesCategory && matchesSearch
            }
            .sortedWith(compareByDescending<ExerciseEntity> { it.isCustom }.thenBy { it.name.lowercase() })
    }

    AnimatedContent(
        targetState = selectedExerciseForHistory,
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
        label = "ExerciseDetailTransition"
    ) { targetExercise ->
        if (targetExercise != null) {
            ExerciseDetailHistoryScreen(
                exercise = targetExercise,
                viewModel = viewModel,
                onBack = { selectedExerciseForHistory = null }
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(AmoledBlack)
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(top = 16.dp, bottom = 120.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Header
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Exercises Library",
                                    color = TextWhite,
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = "${allExercises.size} exercises with personal records",
                                    color = TextMuted,
                                    fontSize = 13.sp
                                )
                            }

                            Button(
                                onClick = { showAddDialog = true },
                                modifier = Modifier
                                    .height(36.dp)
                                    .testTag("add_custom_exercise_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                                shape = RoundedCornerShape(18.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("+ Create Exercise", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                // Search Bar
                item {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("exercise_search_bar"),
                        placeholder = { Text("Search by exercise or muscle group...", color = TextDark) },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = "Search", tint = TextMuted)
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear", tint = TextMuted)
                                }
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricBlue,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite,
                            focusedContainerColor = DarkCardBg,
                            unfocusedContainerColor = DarkCardBg
                        ),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true
                    )
                }

                // Filter Chips Row
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        categories.forEach { cat ->
                            val isSelected = cat == selectedCategory
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedCategory = cat },
                                label = { Text(cat, fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
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
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.testTag("filter_chip_$cat")
                            )
                        }
                    }
                }

                // Exercises List
                items(filteredExercises, key = { it.id }) { ex ->
                    var menuExpanded by remember { mutableStateOf(false) }

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .border(1.dp, DarkBorder, RoundedCornerShape(14.dp))
                            .clickable { selectedExerciseForHistory = ex }
                            .testTag("exercise_item_${ex.id}"),
                        color = DarkCardBg
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f, fill = false),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Circular Thumbnail / Illustration
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .background(DarkCardSurface, CircleShape)
                                        .border(1.dp, DarkBorder, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.FitnessCenter,
                                        contentDescription = null,
                                        tint = ElectricBlue,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Column {
                                    Text(
                                        text = ex.name,
                                        color = TextWhite,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        if (ex.isCustom) {
                                            Surface(
                                                color = ElectricBlueAlpha,
                                                shape = RoundedCornerShape(4.dp),
                                                border = BorderStroke(0.5.dp, ElectricBlue)
                                            ) {
                                                Text(
                                                    text = "CUSTOM",
                                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.5.dp),
                                                    color = ElectricBlueLight,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    letterSpacing = 0.5.sp
                                                )
                                            }
                                        }
                                        Surface(
                                            color = DarkCardSurface,
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                text = ex.muscleGroup,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                color = ElectricBlueLight,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                        val equipmentDetail = when {
                                            ex.barbellWeightKg != 20.0 && (ex.equipment.equals("Barbell", true) || ex.equipment.equals("Smith Machine", true)) ->
                                                "• ${ex.equipment} (${if (ex.barbellWeightKg % 1.0 == 0.0) ex.barbellWeightKg.toInt() else ex.barbellWeightKg}kg)"
                                            ex.isDualPulley -> "• ${ex.equipment} (Dual Pulley)"
                                            else -> "• ${ex.equipment}"
                                        }
                                        Text(
                                            text = equipmentDetail,
                                            color = TextDark,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                // PR Pill
                                if (ex.prWeight > 0) {
                                    Surface(
                                        color = DarkCardSurface,
                                        shape = RoundedCornerShape(10.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                            horizontalAlignment = Alignment.End
                                        ) {
                                            Text("PR", color = StreakGreen, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
                                            Text("${ex.prWeight.toInt()} kg", color = TextWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }

                                // 3-dot Menu
                                Box {
                                    IconButton(
                                        onClick = { menuExpanded = true },
                                        modifier = Modifier
                                            .size(36.dp)
                                            .testTag("exercise_menu_button_${ex.id}")
                                    ) {
                                        Icon(
                                            Icons.Default.MoreVert,
                                            contentDescription = "Options",
                                            tint = TextMuted,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    DropdownMenu(
                                        expanded = menuExpanded,
                                        onDismissRequest = { menuExpanded = false },
                                        modifier = Modifier
                                            .background(DarkCardBg)
                                            .border(1.dp, DarkBorder, RoundedCornerShape(8.dp))
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("View Exercise History", color = TextWhite, fontSize = 13.sp) },
                                            leadingIcon = {
                                                Icon(
                                                    Icons.Default.ShowChart,
                                                    contentDescription = null,
                                                    tint = ElectricBlue,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            },
                                            onClick = {
                                                menuExpanded = false
                                                selectedExerciseForHistory = ex
                                            },
                                            modifier = Modifier.testTag("menu_view_history_${ex.id}")
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Add to Routine", color = TextWhite, fontSize = 13.sp) },
                                            leadingIcon = {
                                                Icon(
                                                    Icons.Default.Add,
                                                    contentDescription = null,
                                                    tint = StreakGreen,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            },
                                            onClick = {
                                                menuExpanded = false
                                                exerciseForAddToRoutine = ex
                                            },
                                            modifier = Modifier.testTag("menu_add_to_routine_${ex.id}")
                                        )
                                    }
                                }
                            }
                    }
                }
            }
        }

        ExtendedFloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 96.dp, end = 16.dp)
                .testTag("create_custom_exercise_fab"),
            containerColor = ElectricBlue,
            contentColor = TextWhite,
            icon = { Icon(Icons.Default.Add, contentDescription = "Create Exercise") },
            text = { Text("Create Exercise", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
        )
    }
}
}

    // Add to Routine Dialog (triggered from 3-dot menu)
    exerciseForAddToRoutine?.let { exToAssign ->
        AlertDialog(
            onDismissRequest = { exerciseForAddToRoutine = null },
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
                        text = "Choose which routine to add ${exToAssign.name} into:",
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
                                .heightIn(max = 280.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(routines, key = { it.id }) { routine ->
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .border(1.dp, DarkBorder, RoundedCornerShape(10.dp))
                                        .clickable {
                                            viewModel.addExerciseToRoutine(routine, exToAssign.name)
                                            Toast.makeText(
                                                context,
                                                "Added ${exToAssign.name} to ${routine.title}!",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            exerciseForAddToRoutine = null
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
                TextButton(onClick = { exerciseForAddToRoutine = null }) {
                    Text("Close", color = TextMuted)
                }
            }
        )
    }

    // Custom Exercise Creator Dialog / Bottom Sheet
    if (showAddDialog) {
        CustomExerciseDialog(
            onDismiss = { showAddDialog = false },
            onSave = { newExercise ->
                viewModel.saveCustomExercise(newExercise) {
                    showAddDialog = false
                    Toast.makeText(context, "${newExercise.name} created and saved!", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }
}

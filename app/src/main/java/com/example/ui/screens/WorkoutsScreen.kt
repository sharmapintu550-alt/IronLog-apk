package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.app.Activity
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.example.data.entity.RoutineEntity
import com.example.data.sync.SyncStatus
import com.example.ui.components.ManageWeeklySplitSheet
import com.example.ui.components.ProfileAuthSheet
import com.example.ui.components.ReorderRoutinesBottomSheet
import com.example.ui.components.WeeklyScheduleSection
import com.example.ui.theme.*
import com.example.ui.viewmodel.WorkoutViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutsScreen(
    viewModel: WorkoutViewModel,
    onStartRoutine: (RoutineEntity) -> Unit
) {
    val context = LocalContext.current
    val routines by viewModel.routines.collectAsState()
    val weeklySplits by viewModel.weeklySplits.collectAsState()
    val workoutLogs by viewModel.workoutLogs.collectAsState()
    val completedEpochDays by viewModel.completedEpochDays.collectAsState()
    val activeStreak by viewModel.activeStreak.collectAsState()
    val authState by viewModel.authState.collectAsState()
    val syncStatus by viewModel.syncStatus.collectAsState()

    var showProfileSheet by remember { mutableStateOf(false) }
    var showManageSplitSheet by remember { mutableStateOf(false) }
    var showReorderSheet by remember { mutableStateOf(false) }
    var showCreateRoutineScreen by remember { mutableStateOf(false) }

    if (showCreateRoutineScreen) {
        CreateRoutineScreen(
            viewModel = viewModel,
            onDismiss = { showCreateRoutineScreen = false },
            onRoutineCreated = { showCreateRoutineScreen = false }
        )
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AmoledBlack)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Personalized User Profile Header
        item {
            val user = authState.user
            val isGuest = authState.isGuest || user == null
            val displayName = user?.displayName?.split(" ")?.firstOrNull() ?: "Athlete"
            val userGreeting = "Hey, $displayName 👋"

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("user_profile_header"),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { showProfileSheet = true }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = userGreeting,
                            color = TextWhite,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        val indicatorColor = if (!isGuest) {
                            when (syncStatus) {
                                is SyncStatus.Syncing -> ElectricBlueLight
                                is SyncStatus.Success -> StreakGreen
                                is SyncStatus.Error -> WarmupAmber
                                is SyncStatus.Idle -> StreakGreen
                            }
                        } else {
                            TextDark
                        }
                        val syncSubtext = if (!isGuest) {
                            when (syncStatus) {
                                is SyncStatus.Syncing -> "Syncing with Cloud..."
                                is SyncStatus.Success -> "Synced with Cloud"
                                is SyncStatus.Error -> "Sync Pending"
                                is SyncStatus.Idle -> "Cloud Backup Active"
                            }
                        } else {
                            "Guest Mode • Tap to backup"
                        }

                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(indicatorColor, CircleShape)
                        )
                        Text(
                            text = syncSubtext,
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                    }
                }

                // Profile Avatar / Sign-In Button
                if (!isGuest && user != null) {
                    val photoUrl = user.photoUrl?.toString()
                    val initial = displayName.firstOrNull()?.uppercaseChar()?.toString() ?: "A"
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(ElectricBlueAlpha, CircleShape)
                            .border(1.5.dp, ElectricBlue, CircleShape)
                            .clickable { showProfileSheet = true }
                            .testTag("profile_avatar"),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!photoUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = photoUrl,
                                contentDescription = "Profile Avatar",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Text(
                                text = initial,
                                color = ElectricBlueLight,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else {
                    // Guest Pill: "Sign In" with Google G
                    Surface(
                        color = DarkCardBg,
                        shape = RoundedCornerShape(20.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ElectricBlue.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .clickable { showProfileSheet = true }
                            .testTag("profile_avatar")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "G",
                                color = ElectricBlue,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "Sign In",
                                color = TextWhite,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        // 2. Title & Stats Pills
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Workouts",
                    color = TextWhite,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        color = DarkCardBg,
                        shape = RoundedCornerShape(20.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(modifier = Modifier.size(6.dp).background(ElectricBlue, CircleShape))
                            Text("This Week: ${completedEpochDays.size} Workouts", color = TextMuted, fontSize = 11.sp)
                        }
                    }

                    Surface(
                        color = DarkCardBg,
                        shape = RoundedCornerShape(20.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(modifier = Modifier.size(6.dp).background(StreakGreen, CircleShape))
                            val streakText = if (activeStreak > 0) "Streak: $activeStreak Days 🔥" else "Streak: Active 🔥"
                            Text(streakText, color = TextMuted, fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // WeeklyScheduleSection seamlessly placed right below stats pill and directly above My Routines
        item {
            WeeklyScheduleSection(
                weeklySplits = weeklySplits,
                completedDatesEpoch = completedEpochDays,
                routines = routines,
                onManageSplitClick = { showManageSplitSheet = true },
                onStartRoutine = onStartRoutine
            )
        }

        // 5. "My Routines" Section Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "My Routines",
                    color = TextWhite,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(
                        onClick = { showReorderSheet = true },
                        modifier = Modifier.testTag("reorder_routines_button"),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Icon(
                            Icons.Default.SwapVert,
                            contentDescription = null,
                            tint = ElectricBlue,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Reorder",
                            color = ElectricBlue,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Button(
                        onClick = { showCreateRoutineScreen = true },
                        modifier = Modifier
                            .height(34.dp)
                            .testTag("new_routine_button"),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("+ New Routine", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 6. Routine Cards List
        items(routines, key = { it.id }) { routine ->
            var showMenu by remember { mutableStateOf(false) }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .border(1.dp, DarkBorder, RoundedCornerShape(14.dp))
                    .testTag("routine_card_${routine.id}"),
                color = DarkCardBg
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Title & 3-dot Menu
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = routine.title,
                                color = TextWhite,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (routine.description.isNotEmpty()) {
                                Text(
                                    text = routine.description,
                                    color = TextMuted,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Box {
                            IconButton(
                                onClick = { showMenu = true },
                                modifier = Modifier.size(32.dp).testTag("routine_menu_${routine.id}")
                            ) {
                                Icon(Icons.Default.MoreVert, contentDescription = "Options", tint = TextMuted)
                            }

                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false },
                                modifier = Modifier.background(DarkCardSurface).border(1.dp, DarkBorder)
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Duplicate Routine", color = TextWhite) },
                                    leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null, tint = ElectricBlue) },
                                    onClick = {
                                        showMenu = false
                                        viewModel.duplicateRoutine(routine)
                                        Toast.makeText(context, "Routine duplicated", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.testTag("duplicate_routine_${routine.id}")
                                )
                                DropdownMenuItem(
                                    text = { Text("Delete", color = MissedRed) },
                                    leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MissedRed) },
                                    onClick = {
                                        showMenu = false
                                        viewModel.deleteRoutine(routine.id)
                                        Toast.makeText(context, "Routine deleted", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.testTag("delete_routine_${routine.id}")
                                )
                            }
                        }
                    }

                    // Exercise Tags
                    if (routine.exercisesSummary.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = routine.exercisesSummary,
                            color = TextDark,
                            fontSize = 12.sp,
                            maxLines = 2
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                Toast.makeText(context, "Editing ${routine.title}", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f).height(42.dp),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorderHover),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextWhite)
                        ) {
                            Text("Edit", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }

                        Button(
                            onClick = { onStartRoutine(routine) },
                            modifier = Modifier
                                .weight(2f)
                                .height(42.dp)
                                .testTag("start_routine_${routine.id}"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("▶ Start Routine", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // Sheet: Manage Weekly Split
    if (showManageSplitSheet) {
        ManageWeeklySplitSheet(
            existingSplits = weeklySplits,
            routines = routines,
            onDismiss = { showManageSplitSheet = false },
            onSave = { updatedSplits ->
                viewModel.saveWeeklySplit(updatedSplits)
                Toast.makeText(context, "Weekly split saved!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Sheet: Reorder Routines
    if (showReorderSheet) {
        ReorderRoutinesBottomSheet(
            routines = routines,
            onDismiss = { showReorderSheet = false },
            onSaveOrder = { newOrderList ->
                viewModel.updateRoutinesOrder(newOrderList)
                Toast.makeText(context, "Routine order updated", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Sheet: Profile & Cloud Sync
    if (showProfileSheet) {
        ProfileAuthSheet(
            user = authState.user,
            isGuest = authState.isGuest,
            isLoading = authState.isLoading,
            errorMessage = authState.errorMessage,
            syncStatus = syncStatus,
            routinesCount = routines.size,
            historyCount = workoutLogs.size,
            onSignInWithGoogle = {
                (context as? Activity)?.let { activity ->
                    viewModel.signInWithGoogle(activity)
                }
            },
            onContinueAsGuest = {
                viewModel.continueAsGuest()
                showProfileSheet = false
            },
            onSyncNow = {
                viewModel.syncWithCloud()
            },
            onSignOut = {
                viewModel.signOut()
            },
            onDismiss = { showProfileSheet = false }
        )
    }
}

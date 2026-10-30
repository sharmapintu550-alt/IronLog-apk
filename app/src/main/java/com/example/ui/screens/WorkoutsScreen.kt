package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.delay
import android.app.Activity
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.example.data.entity.RoutineEntity
import com.example.data.sync.SyncStatus
import com.example.ui.components.ManageWeeklySplitSheet
import com.example.ui.components.ProfileAuthSheet
import com.example.ui.components.ReorderRoutinesBottomSheet
import com.example.ui.components.WeeklyScheduleSection
import com.example.ui.components.ShareRoutineQrSheet
import com.example.ui.components.ImportRoutineDialog
import com.example.util.QrCodeGenerator
import com.example.util.SharedRoutinePayload
import com.example.ui.theme.*
import com.example.ui.viewmodel.WorkoutViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutsScreen(
    viewModel: WorkoutViewModel,
    onStartRoutine: (RoutineEntity) -> Unit,
    onResumeWorkout: ((com.example.data.entity.ActiveWorkoutStateEntity) -> Unit)? = null,
    initialImportData: String? = null,
    onClearDeepLink: () -> Unit = {}
) {
    val context = LocalContext.current
    val routines by viewModel.routines.collectAsState()
    val weeklySplits by viewModel.weeklySplits.collectAsState()
    val workoutLogs by viewModel.workoutLogs.collectAsState()
    val completedEpochDays by viewModel.completedEpochDays.collectAsState()
    val activeStreak by viewModel.activeStreak.collectAsState()
    val authState by viewModel.authState.collectAsState()
    val syncStatus by viewModel.syncStatus.collectAsState()
    val activeWorkoutState by viewModel.activeWorkoutState.collectAsState()

    var showProfileSheet by remember { mutableStateOf(false) }
    var showManageSplitSheet by remember { mutableStateOf(false) }
    var showReorderSheet by remember { mutableStateOf(false) }
    var showCreateRoutineScreen by remember { mutableStateOf(false) }
    var editingRoutine by remember { mutableStateOf<RoutineEntity?>(null) }

    var routineToShare by remember { mutableStateOf<RoutineEntity?>(null) }
    var showImportRoutineDialog by remember { mutableStateOf(false) }
    var pendingImportPayload by remember { mutableStateOf<SharedRoutinePayload?>(null) }

    LaunchedEffect(initialImportData) {
        if (!initialImportData.isNullOrBlank()) {
            val parsed = QrCodeGenerator.parseInput(initialImportData)
            if (parsed != null) {
                pendingImportPayload = parsed
                showImportRoutineDialog = true
            }
        }
    }

    // Auto-dismiss cloud backup / profile bottom sheet immediately upon successful authentication
    LaunchedEffect(authState.user, authState.isGuest) {
        if (authState.user != null && !authState.isGuest) {
            showProfileSheet = false
        }
    }

    // Ensure the bottom sheet remains visible to display the inline red error chip on auth error
    LaunchedEffect(authState.errorMessage) {
        if (!authState.errorMessage.isNullOrBlank()) {
            showProfileSheet = true
        }
    }

    if (showCreateRoutineScreen) {
        CreateRoutineScreen(
            viewModel = viewModel,
            editingRoutine = editingRoutine,
            onDismiss = {
                showCreateRoutineScreen = false
                editingRoutine = null
            },
            onRoutineCreated = {
                showCreateRoutineScreen = false
                editingRoutine = null
            },
            onRoutineSaved = {
                showCreateRoutineScreen = false
                editingRoutine = null
            }
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
        // 0. Sticky Active Workout Restoration Banner (Crash / Cold Start Immunity)
        if (activeWorkoutState != null) {
            item(key = "active_workout_resume_banner") {
                val state = activeWorkoutState!!
                fun calcBannerSeconds(): Long {
                    if (state.startTimestamp <= 0L) return 0L
                    val now = System.currentTimeMillis()
                    val rawElapsed = (now - state.startTimestamp) / 1000L
                    val ongoingPaused = if (state.isPaused && state.pauseTimestamp != null && state.pauseTimestamp > 0L) {
                        (now - state.pauseTimestamp) / 1000L
                    } else {
                        0L
                    }
                    val effectivePaused = state.totalPausedSeconds + ongoingPaused
                    return maxOf(0L, rawElapsed - effectivePaused)
                }

                var bannerSeconds by remember(state.startTimestamp, state.isPaused, state.pauseTimestamp, state.totalPausedSeconds) {
                    mutableStateOf(calcBannerSeconds())
                }
                LaunchedEffect(state.startTimestamp, state.isPaused, state.pauseTimestamp, state.totalPausedSeconds) {
                    bannerSeconds = calcBannerSeconds()
                    while (true) {
                        delay(1000)
                        bannerSeconds = calcBannerSeconds()
                    }
                }

                val hrs = bannerSeconds / 3600
                val mins = (bannerSeconds % 3600) / 60
                val secs = bannerSeconds % 60
                val timeStr = if (hrs > 0) {
                    String.format("%02d:%02d:%02d", hrs, mins, secs)
                } else {
                    String.format("%02d:%02d", mins, secs)
                }

                var showDiscardDialog by remember { mutableStateOf(false) }

                Surface(
                    color = Color(0xFF0F141C),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.5.dp, ElectricBlue),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("active_workout_resume_card")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(ElectricBlueAlpha),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("⚡", fontSize = 18.sp)
                            }
                            Column {
                                Text(
                                    text = "Workout in Progress",
                                    color = TextWhite,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${state.routineName} • $timeStr",
                                    color = ElectricBlueLight,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(
                                onClick = { showDiscardDialog = true },
                                modifier = Modifier.testTag("discard_restored_workout_button")
                            ) {
                                Text("Discard", color = TextMuted, fontSize = 12.sp)
                            }

                            Button(
                                onClick = {
                                    if (onResumeWorkout != null) {
                                        onResumeWorkout(state)
                                    } else {
                                        onStartRoutine(
                                            RoutineEntity(
                                                id = state.routineId ?: "",
                                                title = state.routineName,
                                                description = "",
                                                exercisesSummary = "",
                                                sortOrder = 0
                                            )
                                        )
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("resume_workout_button")
                            ) {
                                Text("Resume", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }
                }

                if (showDiscardDialog) {
                    AlertDialog(
                        onDismissRequest = { showDiscardDialog = false },
                        containerColor = Color(0xFF121212),
                        modifier = Modifier.border(1.dp, Color(0xFF262626), RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp),
                        title = {
                            Text("Discard Workout?", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        },
                        text = {
                            Text("Discard active session? All progress will be removed.", color = TextMuted, fontSize = 14.sp)
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    showDiscardDialog = false
                                    viewModel.clearActiveWorkoutState(context)
                                    Toast.makeText(context, "Workout discarded", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF3B30)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Discard", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDiscardDialog = false }) {
                                Text("Cancel", color = ElectricBlueLight)
                            }
                        }
                    )
                }
            }
        }

        // 1. Personalized User Profile Header
        item {
            val user = authState.user
            val isGuest = authState.isGuest || user == null
            val firstName = user?.displayName?.split(" ")?.firstOrNull()?.takeIf { it.isNotBlank() } ?: "Athlete"
            val userEmail = user?.email ?: ""

            val titleText = if (isGuest) "Hey, Athlete 💪" else "Hey, $firstName 💪"
            val subtitleText = if (isGuest) {
                "• Guest Mode • Tap to backup"
            } else {
                if (userEmail.isNotBlank()) "• Synced with $userEmail • All data saved" else "• Synced with Cloud • All data saved"
            }

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
                    Text(
                        text = titleText,
                        color = TextWhite,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitleText,
                        color = if (isGuest) TextMuted else ElectricBlueLight,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Profile Avatar (Signed-in) / Sign-In Pill Button (Guest)
                if (!isGuest && user != null) {
                    val photoUrl = user.photoUrl?.toString()
                    val initial = firstName.firstOrNull()?.uppercaseChar()?.toString() ?: "A"

                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(ElectricBlueAlpha, CircleShape)
                            .border(2.dp, ElectricBlue, CircleShape)
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
                    // Guest Mode: "Sign In" Pill Button
                    Surface(
                        color = DarkCardBg,
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.dp, ElectricBlue),
                        modifier = Modifier
                            .clickable { showProfileSheet = true }
                            .testTag("sign_in_pill_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
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
                                fontSize = 13.sp,
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
                        onClick = { showImportRoutineDialog = true },
                        modifier = Modifier
                            .height(34.dp)
                            .testTag("scan_qr_button"),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = DarkCardBg, contentColor = ElectricBlue),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ElectricBlue.copy(alpha = 0.6f)),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Scan QR", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick = {
                            editingRoutine = null
                            showCreateRoutineScreen = true
                        },
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

        // 6. Routine Cards List or Empty State
        if (routines.isEmpty()) {
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, DarkBorder, RoundedCornerShape(16.dp))
                        .testTag("empty_routines_container"),
                    color = DarkCardBg
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp, horizontal = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(DarkCardSurface)
                                .border(1.dp, DarkBorder, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.FitnessCenter,
                                contentDescription = null,
                                tint = ElectricBlue,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "No routines yet",
                            color = TextWhite,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Create your first routine or scan a friend's QR code to begin.",
                            color = TextMuted,
                            fontSize = 13.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        } else {
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
                                    text = { Text("Edit Routine", color = TextWhite) },
                                    leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, tint = ElectricBlue) },
                                    onClick = {
                                        showMenu = false
                                        editingRoutine = routine
                                        showCreateRoutineScreen = true
                                    },
                                    modifier = Modifier.testTag("edit_routine_menu_${routine.id}")
                                )
                                DropdownMenuItem(
                                    text = { Text("Share Routine", color = TextWhite) },
                                    leadingIcon = { Icon(Icons.Default.QrCode, contentDescription = null, tint = ElectricBlue) },
                                    onClick = {
                                        showMenu = false
                                        routineToShare = routine
                                    },
                                    modifier = Modifier.testTag("share_routine_${routine.id}")
                                )
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
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { routineToShare = routine },
                            modifier = Modifier
                                .size(42.dp)
                                .border(1.dp, DarkBorderHover, RoundedCornerShape(10.dp))
                                .background(DarkCardSurface, RoundedCornerShape(10.dp))
                                .testTag("share_routine_btn_${routine.id}")
                        ) {
                            Icon(Icons.Default.QrCode, contentDescription = "Share", tint = ElectricBlue, modifier = Modifier.size(18.dp))
                        }

                        OutlinedButton(
                            onClick = {
                                editingRoutine = routine
                                showCreateRoutineScreen = true
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .testTag("edit_routine_btn_${routine.id}"),
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
                val orderedPairs = newOrderList.mapIndexed { index, routine -> routine.id to index }
                viewModel.saveRoutineOrder(orderedPairs)
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
                val activity = context.findActivity() ?: (context as? Activity)
                if (activity != null) {
                    viewModel.signInWithGoogle(activity)
                } else {
                    android.util.Log.e("AuthSyncDebug", "WorkoutsScreen: context is not an Activity, cannot launch CredentialManager")
                    Toast.makeText(context, "Unable to find parent Activity for Sign-In", Toast.LENGTH_SHORT).show()
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

    // Sheet: Share Routine QR Code
    if (routineToShare != null) {
        val allDbExercises by viewModel.exercises.collectAsState()
        ShareRoutineQrSheet(
            routine = routineToShare!!,
            allDbExercises = allDbExercises,
            onDismiss = { routineToShare = null }
        )
    }

    // Dialog: Import Routine (CameraX Scanner / Paste Link)
    if (showImportRoutineDialog) {
        ImportRoutineDialog(
            viewModel = viewModel,
            initialPayload = pendingImportPayload,
            onDismiss = {
                showImportRoutineDialog = false
                pendingImportPayload = null
                onClearDeepLink()
            }
        )
    }
}

private fun android.content.Context.findActivity(): Activity? {
    var ctx = this
    while (ctx is android.content.ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}

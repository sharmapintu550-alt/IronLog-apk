package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.backup.BackupSummary
import com.example.ui.theme.*
import com.example.ui.viewmodel.WorkoutViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeasureScreen(
    viewModel: WorkoutViewModel
) {
    val context = LocalContext.current
    val measurements by viewModel.measurements.collectAsStateWithLifecycle()
    val completedEpochDays by viewModel.completedEpochDays.collectAsStateWithLifecycle()
    val routines by viewModel.routines.collectAsStateWithLifecycle()
    val workoutLogs by viewModel.workoutLogs.collectAsStateWithLifecycle()
    val exercises by viewModel.exercises.collectAsStateWithLifecycle()
    val isBackupOperating by viewModel.isBackupOperating.collectAsStateWithLifecycle()

    var showLogWeightDialog by remember { mutableStateOf(false) }
    var weightInput by remember { mutableStateOf("77.8") }
    var selectedTab by rememberSaveable { mutableStateOf(0) } // 0 = Body Measures, 1 = Analytics & 1RM

    var showExportDialog by remember { mutableStateOf(false) }
    var showRestoreConfirmDialog by remember { mutableStateOf(false) }
    var restorePreviewSummary by remember { mutableStateOf<BackupSummary?>(null) }

    val todayDateStr = remember {
        SimpleDateFormat("yyyyMMdd", Locale.US).format(Date())
    }

    // Storage Access Framework contracts (zero permissions required)
    val jsonExportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            viewModel.exportJsonToUri(uri, context) { success, msg ->
                Toast.makeText(context, msg, if (success) Toast.LENGTH_SHORT else Toast.LENGTH_LONG).show()
            }
        }
    }

    val csvExportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        if (uri != null) {
            viewModel.exportCsvToUri(uri, context) { success, msg ->
                Toast.makeText(context, msg, if (success) Toast.LENGTH_SHORT else Toast.LENGTH_LONG).show()
            }
        }
    }

    val jsonImportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            viewModel.inspectBackupFile(
                uri = uri,
                context = context,
                onReady = { summary ->
                    restorePreviewSummary = summary
                    showRestoreConfirmDialog = true
                },
                onError = { errorMsg ->
                    Toast.makeText(context, "Error reading backup: $errorMsg", Toast.LENGTH_LONG).show()
                }
            )
        }
    }

    val latestWeight by remember {
        derivedStateOf {
            measurements.filter { it.metricType == "Weight" }.firstOrNull()?.value ?: 77.8
        }
    }
    val targetWeight = 75.0
    val startWeight = 82.0
    val progressPercent by remember {
        derivedStateOf {
            ((startWeight - latestWeight) / (startWeight - targetWeight)).coerceIn(0.0, 1.0).toFloat()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AmoledBlack)
    ) {
        // Top Sub-Tab Switcher: Body Measures vs Analytics & 1RM
        Surface(
            color = Color(0xFF141414),
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, Color(0xFF262626)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(3.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (selectedTab == 0) ElectricBlue else Color.Transparent)
                        .clickable { selectedTab = 0 }
                        .padding(vertical = 7.dp)
                        .testTag("tab_body_measures"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Body Measures",
                        color = if (selectedTab == 0) Color.White else TextMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (selectedTab == 1) ElectricBlue else Color.Transparent)
                        .clickable { selectedTab = 1 }
                        .padding(vertical = 7.dp)
                        .testTag("tab_muscle_analytics"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Muscle & 1RM Analytics 📊",
                        color = if (selectedTab == 1) Color.White else TextMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        if (selectedTab == 1) {
            AnalyticsDashboardScreen(
                viewModel = viewModel,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(AmoledBlack)
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Measure & Profile",
                        color = TextWhite,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "Body metrics, physique goals & habits",
                        color = TextMuted,
                        fontSize = 13.sp
                    )
                }

                Button(
                    onClick = { showLogWeightDialog = true },
                    modifier = Modifier.height(36.dp).testTag("log_weight_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                    shape = RoundedCornerShape(18.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Log", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Weight & Goal Tracker Card
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, DarkBorder, RoundedCornerShape(16.dp))
                    .testTag("weight_goal_tracker_card"),
                color = DarkCardBg
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("CURRENT WEIGHT", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text("$latestWeight", color = TextWhite, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("kg", color = ElectricBlueLight, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("TARGET GOAL", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text("$targetWeight", color = StreakGreen, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("kg", color = TextDark, fontSize = 12.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Progress Bar
                    LinearProgressIndicator(
                        progress = { progressPercent },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = ElectricBlue,
                        trackColor = DarkCardSurface
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${(progressPercent * 100).toInt()}% of goal achieved",
                            color = ElectricBlueLight,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "-${String.format("%.1f", startWeight - latestWeight)} kg down",
                            color = StreakGreen,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Section Title: Body Measurements (2x3 Grid)
        item {
            Text(
                text = "Body Measurements",
                color = TextWhite,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // 2x3 Metrics Grid
        item {
            val metrics = listOf(
                Triple("Body Fat", "14.2%", "-0.4%"),
                Triple("Waist", "31.5 in", "-0.2 in"),
                Triple("Arms (L/R)", "15.8 in", "+0.3 in"),
                Triple("Chest", "42.0 in", "+0.5 in"),
                Triple("Thighs", "24.2 in", "+0.2 in"),
                Triple("Calves", "15.2 in", "0.0 in")
            )

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                for (row in 0 until 3) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        for (col in 0 until 2) {
                            val item = metrics[row * 2 + col]
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(14.dp))
                                    .border(1.dp, DarkBorder, RoundedCornerShape(14.dp)),
                                color = DarkCardBg
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(item.first, color = TextMuted, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(item.second, color = TextWhite, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        item.third,
                                        color = if (item.third.startsWith("+")) ElectricBlueLight else StreakGreen,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 12-Week Consistency Heatmap Grid
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, DarkBorder, RoundedCornerShape(16.dp))
                    .testTag("consistency_heatmap_card"),
                color = DarkCardBg
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "12-Week Consistency Grid",
                            color = TextWhite,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${completedEpochDays.size} Sessions",
                            color = StreakGreen,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 7 rows x 12 columns grid of habit dots
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        for (row in 0 until 7) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                for (col in 0 until 12) {
                                    val isFilled = (col >= 8 && (row == 0 || row == 1 || row == 3 || row == 4)) || (col == 11 && row < 3)
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .background(
                                                if (isFilled) StreakGreen else DarkCardSurface,
                                                RoundedCornerShape(3.dp)
                                            )
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Less", color = TextDark, fontSize = 10.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Box(modifier = Modifier.size(10.dp).background(DarkCardSurface, RoundedCornerShape(2.dp)))
                        Spacer(modifier = Modifier.width(4.dp))
                        Box(modifier = Modifier.size(10.dp).background(StreakGreen, RoundedCornerShape(2.dp)))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("More", color = TextDark, fontSize = 10.sp)
                    }
                }
            }
        }

        // Section Title: Data & Storage
        item {
            Text(
                text = "Data & Storage",
                color = TextWhite,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Data & Storage Card Section
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, Color(0xFF262626), RoundedCornerShape(16.dp))
                    .testTag("data_storage_card"),
                color = Color(0xFF121212)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .background(ElectricBlueAlpha, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Storage,
                                    contentDescription = null,
                                    tint = ElectricBlue,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Local Backup & Export",
                                    color = TextWhite,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Full JSON archive • Strong/Hevy CSV",
                                    color = TextMuted,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        if (isBackupOperating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = ElectricBlue,
                                strokeWidth = 2.dp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Database Status Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF1A1A1A), RoundedCornerShape(10.dp))
                            .border(1.dp, Color(0xFF262626), RoundedCornerShape(10.dp))
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("${routines.size}", color = TextWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Text("Routines", color = TextMuted, fontSize = 10.sp)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("${workoutLogs.size}", color = ElectricBlue, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Text("Workouts", color = TextMuted, fontSize = 10.sp)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            val customCount = exercises.count { it.isCustom }
                            Text("$customCount", color = StreakGreen, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Text("Custom Ex.", color = TextMuted, fontSize = 10.sp)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("${measurements.size}", color = WarmupAmber, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Text("Metrics", color = TextMuted, fontSize = 10.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Action Item 1: Export Backup (JSON / CSV)
                    Button(
                        onClick = { showExportDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("export_backup_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ElectricBlue,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isBackupOperating
                    ) {
                        Icon(
                            imageVector = Icons.Default.FileUpload,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Export Backup (JSON / CSV)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Action Item 2: Restore from Backup File
                    OutlinedButton(
                        onClick = {
                            jsonImportLauncher.launch(arrayOf("application/json", "application/octet-stream", "*/*"))
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("restore_backup_button"),
                        border = BorderStroke(1.2.dp, Color(0xFF262626)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.Transparent,
                            contentColor = TextWhite
                        ),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isBackupOperating
                    ) {
                        Icon(
                            imageVector = Icons.Default.FileDownload,
                            contentDescription = null,
                            tint = ElectricBlue,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Restore from Backup File",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextWhite
                        )
                    }
                }
            }
        }
        }
    }
    }

    // Log Weight Dialog
    if (showLogWeightDialog) {
        AlertDialog(
            onDismissRequest = { showLogWeightDialog = false },
            containerColor = DarkCardBg,
            title = { Text("Log Body Weight", color = TextWhite, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(
                        value = weightInput,
                        onValueChange = { weightInput = it },
                        label = { Text("Weight (kg)") },
                        modifier = Modifier.fillMaxWidth().testTag("weight_input_field"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricBlue,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val wt = weightInput.toDoubleOrNull()
                        if (wt != null) {
                            viewModel.addWeightLog(wt)
                            showLogWeightDialog = false
                            Toast.makeText(context, "Weight logged: $wt kg", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                    modifier = Modifier.testTag("save_weight_button")
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogWeightDialog = false }) {
                    Text("Cancel", color = TextMuted)
                }
            }
        )
    }

    // Export Options Dialog
    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            containerColor = Color(0xFF141414),
            shape = RoundedCornerShape(16.dp),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.FileUpload,
                        contentDescription = null,
                        tint = ElectricBlue,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Export Data Backup",
                        color = TextWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        text = "Choose your preferred export format. Files can be saved directly via Android Storage Access Framework or shared to cloud drives.",
                        color = TextMuted,
                        fontSize = 12.sp
                    )

                    // Card 1: Full JSON Backup
                    Surface(
                        color = Color(0xFF1C1C1E),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFF2C2C2E)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Full JSON Backup",
                                    color = TextWhite,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = ".json",
                                    color = ElectricBlue,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "IronLog_Backup_${todayDateStr}.json",
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                            Text(
                                text = "Complete archive of routines, sets, reps, notes & custom exercises.",
                                color = TextDark,
                                fontSize = 11.sp
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        showExportDialog = false
                                        jsonExportLauncher.launch("IronLog_Backup_${todayDateStr}.json")
                                    },
                                    modifier = Modifier.weight(1f).height(36.dp).testTag("save_json_button"),
                                    colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp)
                                ) {
                                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Save File", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                OutlinedButton(
                                    onClick = {
                                        showExportDialog = false
                                        viewModel.shareJsonBackup(context)
                                    },
                                    modifier = Modifier.weight(1f).height(36.dp).testTag("share_json_button"),
                                    border = BorderStroke(1.dp, Color(0xFF3A3A3C)),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp)
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = null, tint = TextWhite, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Share", color = TextWhite, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }

                    // Card 2: CSV History Export
                    Surface(
                        color = Color(0xFF1C1C1E),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFF2C2C2E)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Workout History CSV",
                                    color = TextWhite,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = ".csv",
                                    color = StreakGreen,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "IronLog_Workout_History_${todayDateStr}.csv",
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                            Text(
                                text = "Hevy & Strong compatible spreadsheet with weights, reps & timestamps.",
                                color = TextDark,
                                fontSize = 11.sp
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        showExportDialog = false
                                        csvExportLauncher.launch("IronLog_Workout_History_${todayDateStr}.csv")
                                    },
                                    modifier = Modifier.weight(1f).height(36.dp).testTag("save_csv_button"),
                                    colors = ButtonDefaults.buttonColors(containerColor = StreakGreen),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp)
                                ) {
                                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Save CSV", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                OutlinedButton(
                                    onClick = {
                                        showExportDialog = false
                                        viewModel.shareCsvHistory(context)
                                    },
                                    modifier = Modifier.weight(1f).height(36.dp).testTag("share_csv_button"),
                                    border = BorderStroke(1.dp, Color(0xFF3A3A3C)),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp)
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = null, tint = TextWhite, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Share", color = TextWhite, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(
                    onClick = { showExportDialog = false },
                    modifier = Modifier.testTag("close_export_dialog")
                ) {
                    Text("Close", color = TextMuted)
                }
            }
        )
    }

    // Restore Confirmation Preview Dialog
    if (showRestoreConfirmDialog && restorePreviewSummary != null) {
        val summary = restorePreviewSummary!!
        AlertDialog(
            onDismissRequest = {
                showRestoreConfirmDialog = false
                viewModel.dismissRestorePreview()
            },
            containerColor = Color(0xFF141414),
            shape = RoundedCornerShape(16.dp),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Restore,
                        contentDescription = null,
                        tint = ElectricBlue,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Confirm Data Restore",
                        color = TextWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Found ${summary.routinesCount} Routines, ${summary.workoutLogsCount} Logged Workouts, ${summary.customExercisesCount} Custom Exercises. Confirm Restore?",
                        color = TextWhite,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Surface(
                        color = Color(0xFF1C1C1E),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Color(0xFF2C2C2E)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Routines to import:", color = TextMuted, fontSize = 12.sp)
                                Text("${summary.routinesCount}", color = TextWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Workout logs to import:", color = TextMuted, fontSize = 12.sp)
                                Text("${summary.workoutLogsCount}", color = ElectricBlue, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Custom exercises to import:", color = TextMuted, fontSize = 12.sp)
                                Text("${summary.customExercisesCount}", color = StreakGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            if (summary.measurementsCount > 0) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Body metrics to import:", color = TextMuted, fontSize = 12.sp)
                                    Text("${summary.measurementsCount}", color = WarmupAmber, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    Text(
                        text = "Safe merge operation: Records will be merged into Room database with REPLACE on conflict. No existing standard exercises will be deleted.",
                        color = TextDark,
                        fontSize = 11.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.confirmRestore(
                            onSuccess = {
                                showRestoreConfirmDialog = false
                                Toast.makeText(context, "Data restored successfully! Refreshing workouts...", Toast.LENGTH_LONG).show()
                            },
                            onError = { err ->
                                Toast.makeText(context, "Restore failed: $err", Toast.LENGTH_LONG).show()
                            }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("confirm_restore_button"),
                    enabled = !isBackupOperating
                ) {
                    if (isBackupOperating) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Text("Confirm Restore")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showRestoreConfirmDialog = false
                        viewModel.dismissRestorePreview()
                    },
                    modifier = Modifier.testTag("cancel_restore_button"),
                    enabled = !isBackupOperating
                ) {
                    Text("Cancel", color = TextMuted)
                }
            }
        )
    }
}

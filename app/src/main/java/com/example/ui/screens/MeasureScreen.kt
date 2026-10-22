package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.ui.theme.*
import com.example.ui.viewmodel.WorkoutViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeasureScreen(
    viewModel: WorkoutViewModel
) {
    val context = LocalContext.current
    val measurements by viewModel.measurements.collectAsState()
    val completedEpochDays by viewModel.completedEpochDays.collectAsState()

    var showLogWeightDialog by remember { mutableStateOf(false) }
    var weightInput by remember { mutableStateOf("77.8") }

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

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AmoledBlack)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
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
}

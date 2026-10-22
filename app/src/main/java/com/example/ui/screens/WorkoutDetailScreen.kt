package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.WorkoutLogEntity
import com.example.ui.components.WorkoutShareSheet
import com.example.ui.theme.*
import com.example.util.WorkoutShareHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutDetailScreen(
    log: WorkoutLogEntity,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val (workout, exercises) = remember(log) {
        WorkoutShareHelper.convertLogToShareable(log)
    }

    var showShareSheet by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = AmoledBlack,
        topBar = {
            Surface(
                color = AmoledBlack,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(width = 0.dp, color = DarkBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("detail_back_button")
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextWhite
                        )
                    }

                    Text(
                        text = "Session Details",
                        color = TextWhite,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )

                    // Share Action Button in Top App Bar
                    IconButton(
                        onClick = { showShareSheet = true },
                        modifier = Modifier.testTag("detail_share_button")
                    ) {
                        Icon(
                            Icons.Default.Share,
                            contentDescription = "Share",
                            tint = ElectricBlue
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
            // Header: Workout Title & Date
            item {
                Column {
                    Text(
                        text = workout.title,
                        color = TextWhite,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "📅 ${workout.formattedDate} • ⏱ ${workout.durationMinutes} min",
                        color = TextMuted,
                        fontSize = 13.sp
                    )
                }
            }

            // Stat Cards Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .border(1.dp, DarkBorder, RoundedCornerShape(14.dp)),
                        color = DarkCardBg
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("VOLUME", color = TextDark, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("${workout.totalVolumeKg.toInt()} kg", color = ElectricBlue, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .border(1.dp, DarkBorder, RoundedCornerShape(14.dp)),
                        color = DarkCardBg
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("SETS", color = TextDark, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("${workout.totalSets}", color = StreakGreen, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .border(1.dp, DarkBorder, RoundedCornerShape(14.dp)),
                        color = DarkCardBg
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("DURATION", color = TextDark, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("${workout.durationMinutes}m", color = TextWhite, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Muscle Involvement Breakdown Card
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .border(1.dp, DarkBorder, RoundedCornerShape(14.dp)),
                    color = DarkCardBg
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Text(
                            text = "MUSCLE INVOLVEMENT BREAKDOWN",
                            color = TextDark,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                        ) {
                            Box(modifier = Modifier.weight(0.5f).fillMaxHeight().background(ElectricBlue))
                            Spacer(modifier = Modifier.width(2.dp))
                            Box(modifier = Modifier.weight(0.3f).fillMaxHeight().background(ElectricBlueLight))
                            Spacer(modifier = Modifier.width(2.dp))
                            Box(modifier = Modifier.weight(0.2f).fillMaxHeight().background(StreakGreen))
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = workout.muscleSplit,
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // Exercise Breakdown Header
            item {
                Text(
                    text = "Exercise Log (${exercises.size})",
                    color = TextWhite,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Itemized Exercises
            itemsIndexed(exercises, key = { index, exercise -> "${exercise.orderPerformed}_${exercise.name}_$index" }) { index, exercise ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .border(1.dp, DarkBorder, RoundedCornerShape(14.dp)),
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
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                val isPeakEnergy = (exercise.orderPerformed in 1..3)
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(
                                            if (isPeakEnergy) ElectricBlueAlpha else DarkCardSurface,
                                            CircleShape
                                        )
                                        .border(
                                            width = if (isPeakEnergy) 1.5.dp else 1.dp,
                                            color = if (isPeakEnergy) ElectricBlue else DarkBorder,
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "#${exercise.orderPerformed}",
                                        color = if (isPeakEnergy) ElectricBlueLight else TextMuted,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Column {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = exercise.name,
                                            color = TextWhite,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        if (exercise.orderPerformed == 1) {
                                            Surface(
                                                color = ElectricBlueDark,
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    text = "PRIMARY COMPOUND",
                                                    color = TextWhite,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                )
                                            }
                                        } else if (exercise.orderPerformed in 2..3) {
                                            Surface(
                                                color = DarkCardSurface,
                                                shape = RoundedCornerShape(4.dp),
                                                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                                            ) {
                                                Text(
                                                    text = "PEAK ENERGY",
                                                    color = ElectricBlueLight,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            Surface(
                                color = ElectricBlueAlpha,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = exercise.muscleGroup,
                                    color = ElectricBlueLight,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("SET", color = TextDark, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(50.dp))
                            Text("WEIGHT", color = TextDark, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(80.dp))
                            Text("REPS", color = TextDark, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(70.dp))
                            Text("BADGE", color = TextDark, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(60.dp))
                        }

                        HorizontalDivider(color = DarkBorder, modifier = Modifier.padding(vertical = 6.dp))

                        exercise.sets.forEach { set ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (set.isWarmup) "W" else "${set.setNumber}",
                                    color = if (set.isWarmup) TextDark else TextMuted,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.width(50.dp)
                                )

                                Text(
                                    text = "${set.weightKg.toInt()} kg",
                                    color = TextWhite,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.width(80.dp)
                                )

                                Text(
                                    text = "${set.reps}",
                                    color = TextWhite,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.width(70.dp)
                                )

                                Box(modifier = Modifier.width(60.dp)) {
                                    if (set.isPR) {
                                        Text(
                                            text = "⭐ PR",
                                            color = StreakGreen,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    } else if (set.isWarmup) {
                                        Text(
                                            text = "WARMUP",
                                            color = TextDark,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Share Action Button at bottom
            item {
                Button(
                    onClick = { showShareSheet = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("detail_bottom_share_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Share Workout", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
        }
    }

    if (showShareSheet) {
        WorkoutShareSheet(
            workout = workout,
            exercises = exercises,
            onDismiss = { showShareSheet = false }
        )
    }
}

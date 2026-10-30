package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.RoutineEntity
import com.example.data.entity.WeeklySplitEntity
import com.example.ui.theme.*
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageWeeklySplitSheet(
    existingSplits: List<WeeklySplitEntity>,
    routines: List<RoutineEntity>,
    onDismiss: () -> Unit,
    onSave: (List<WeeklySplitEntity>) -> Unit
) {
    // Maintain state for all 7 days (1 = Monday ... 7 = Sunday)
    var splitsMap by remember {
        mutableStateOf(
            (1..7).associateWith { dayInt ->
                existingSplits.find { it.dayOfWeek == dayInt } ?: WeeklySplitEntity(
                    dayOfWeek = dayInt,
                    routineId = null,
                    routineTitle = null,
                    isRestDay = (dayInt == 3 || dayInt == 6 || dayInt == 7)
                )
            }
        )
    }

    var selectedDayForPicker by remember { mutableStateOf<Int?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = DarkCardBg,
        dragHandle = {
            BottomSheetDefaults.DragHandle(color = DarkBorderHover)
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 20.dp)
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
                        text = "Manage Weekly Split",
                        color = TextWhite,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Allots 1 routine per weekday. Recurs automatically every week.",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("close_split_sheet_button")
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 7 Days List
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items((1..7).toList(), key = { dayInt -> dayInt }) { dayInt ->
                    val daySplit = splitsMap[dayInt] ?: WeeklySplitEntity(dayInt, null, null, isRestDay = true)
                    val dayName = DayOfWeek.of(dayInt).getDisplayName(TextStyle.FULL, Locale.getDefault())

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .border(1.dp, DarkBorder, RoundedCornerShape(14.dp))
                            .clickable { selectedDayForPicker = dayInt }
                            .testTag("day_row_$dayInt"),
                        color = DarkCardSurface
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(
                                            if (daySplit.isRestDay) DarkBorder else ElectricBlueAlpha,
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (daySplit.isRestDay) {
                                        Icon(
                                            Icons.Default.Bedtime,
                                            contentDescription = "Rest",
                                            tint = TextMuted,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    } else {
                                        Icon(
                                            Icons.Default.FitnessCenter,
                                            contentDescription = "Workout",
                                            tint = ElectricBlue,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }

                                Column {
                                    Text(
                                        text = dayName,
                                        color = TextWhite,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = if (daySplit.isRestDay) "Rest & Recovery Day" else (daySplit.routineTitle ?: "No Routine Assigned"),
                                        color = if (daySplit.isRestDay) RestGray else ElectricBlueLight,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = if (daySplit.isRestDay) "Rest" else "Active",
                                    color = if (daySplit.isRestDay) TextDark else ElectricBlue,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Icon(
                                    Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Select Routine",
                                    tint = TextMuted,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Save Split Button
            Button(
                onClick = {
                    onSave(splitsMap.values.toList())
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("save_weekly_split_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ElectricBlue,
                    contentColor = TextWhite
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Done / Save", fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
        }
    }

    // Modal Dialog to pick routine for selected day
    selectedDayForPicker?.let { dayInt ->
        val dayName = DayOfWeek.of(dayInt).getDisplayName(TextStyle.FULL, Locale.getDefault())
        AlertDialog(
            onDismissRequest = { selectedDayForPicker = null },
            containerColor = DarkCardBg,
            title = {
                Text(
                    text = "Assign for $dayName",
                    color = TextWhite,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Choose 1 routine from My Routines or select Rest Day:",
                        color = TextMuted,
                        fontSize = 13.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Option 1: Rest Day
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .border(1.dp, DarkBorder, RoundedCornerShape(10.dp))
                            .clickable {
                                splitsMap = splitsMap + (dayInt to WeeklySplitEntity(
                                    dayOfWeek = dayInt,
                                    routineId = null,
                                    routineTitle = null,
                                    isRestDay = true
                                ))
                                selectedDayForPicker = null
                            }
                            .testTag("pick_rest_day_$dayInt"),
                        color = DarkCardSurface
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Default.Bedtime, contentDescription = null, tint = TextMuted)
                            Text("🛌 Rest & Recovery Day", color = TextWhite, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        }
                    }

                    // Options from My Routines
                    routines.forEach { routine ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .border(1.dp, DarkBorder, RoundedCornerShape(10.dp))
                            .clickable {
                                splitsMap = splitsMap + (dayInt to WeeklySplitEntity(
                                    dayOfWeek = dayInt,
                                    routineId = routine.id,
                                    routineTitle = routine.title,
                                    isRestDay = false
                                ))
                                selectedDayForPicker = null
                            }
                            .testTag("pick_routine_${routine.id}"),
                            color = DarkCardSurface
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(Icons.Default.FitnessCenter, contentDescription = null, tint = ElectricBlue)
                                Column {
                                    Text(routine.title, color = TextWhite, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                    if (routine.exercisesSummary.isNotEmpty()) {
                                        Text(routine.exercisesSummary, color = TextDark, fontSize = 11.sp, maxLines = 1)
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(
                    onClick = { selectedDayForPicker = null },
                    modifier = Modifier.testTag("cancel_pick_routine_button")
                ) {
                    Text("Cancel", color = TextMuted)
                }
            }
        )
    }
}

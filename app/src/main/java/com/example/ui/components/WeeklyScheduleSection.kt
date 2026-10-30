package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.RoutineEntity
import com.example.data.entity.WeeklySplitEntity
import com.example.ui.theme.*
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun WeeklyScheduleSection(
    weeklySplits: List<WeeklySplitEntity>,
    completedDatesEpoch: Set<Long>,
    routines: List<RoutineEntity>,
    onManageSplitClick: () -> Unit,
    onStartRoutine: (RoutineEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val today = remember { LocalDate.now() }
    var selectedDate by remember { mutableStateOf(today) }

    // Generate dates around today (e.g., 7 days back, 7 days forward)
    val dates = remember(today) {
        (-7..7).map { today.plusDays(it.toLong()) }
    }

    // Determine allocated routine or rest day for the selected date
    val selectedDayOfWeek = selectedDate.dayOfWeek.value // 1 = Monday ... 7 = Sunday
    val splitForSelectedDay = weeklySplits.find { it.dayOfWeek == selectedDayOfWeek }

    val allocatedRoutine = remember(splitForSelectedDay, routines) {
        if (splitForSelectedDay?.isRestDay == true || splitForSelectedDay?.routineId.isNullOrBlank()) {
            null
        } else {
            val byId = routines.find { it.id == splitForSelectedDay?.routineId }
            val byTitle = if (byId == null && !splitForSelectedDay?.routineTitle.isNullOrBlank()) {
                routines.find { it.title.equals(splitForSelectedDay?.routineTitle, ignoreCase = true) }
            } else null
            byId ?: byTitle ?: RoutineEntity(
                id = splitForSelectedDay!!.routineId!!,
                userId = splitForSelectedDay.userId,
                title = splitForSelectedDay.routineTitle ?: "Scheduled Workout",
                description = "",
                exercisesSummary = ""
            )
        }
    }

    val isSelectedDayRest = splitForSelectedDay?.isRestDay == true ||
            (splitForSelectedDay == null && (selectedDayOfWeek == 3 || selectedDayOfWeek == 6 || selectedDayOfWeek == 7))

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, DarkBorder, RoundedCornerShape(16.dp))
            .testTag("weekly_schedule_section"),
        color = DarkCardBg
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header: Title & "+ Manage Split" button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = ElectricBlue,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Weekly Schedule",
                        color = TextWhite,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // "+ Manage Split" action button
                FilledTonalButton(
                    onClick = onManageSplitClick,
                    modifier = Modifier
                        .height(34.dp)
                        .testTag("manage_split_button"),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = ElectricBlueAlpha,
                        contentColor = ElectricBlueLight
                    ),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Icon(
                        Icons.Default.Tune,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "+ Manage Split",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Horizontal Scrollable Date Strip
            val scrollState = rememberScrollState()
            LaunchedEffect(Unit) {
                // Scroll roughly towards today
                scrollState.scrollTo(420)
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                dates.forEach { date ->
                    val isSelected = date == selectedDate
                    val isCurrentDay = date == today
                    val epochDay = date.toEpochDay()
                    val hasCompleted = completedDatesEpoch.contains(epochDay)

                    val dayOfWeekVal = date.dayOfWeek.value
                    val daySplit = weeklySplits.find { it.dayOfWeek == dayOfWeekVal }
                    val isRestDay = daySplit?.isRestDay == true ||
                            (daySplit == null && (dayOfWeekVal == 3 || dayOfWeekVal == 6 || dayOfWeekVal == 7)) ||
                            daySplit?.routineId == null

                    // Determine dot status color:
                    // - Completed sessions: vibrant Green (#22C55E)
                    // - Today's pending session: Electric Blue (#1877F2) until logged
                    // - Designated rest days: do NOT penalize or show missed dot
                    // - Missed past scheduled session: Red
                    val dotColor: Color? = when {
                        hasCompleted -> StreakGreen // Vibrant Green (#22C55E)
                        date == today && !isRestDay -> ElectricBlue // Today's pending workout (#1877F2)
                        isRestDay -> null // Designated rest days do NOT count as missed
                        date.isBefore(today) -> MissedRed // Missed scheduled workout
                        else -> null
                    }

                    Column(
                        modifier = Modifier
                            .width(50.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                when {
                                    isSelected -> ElectricBlue
                                    isCurrentDay -> DarkBorderHover
                                    else -> DarkCardSurface
                                }
                            )
                            .border(
                                width = if (isCurrentDay && !isSelected) 1.5.dp else 1.dp,
                                color = if (isCurrentDay && !isSelected) ElectricBlue else DarkBorder,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { selectedDate = date }
                            .padding(vertical = 10.dp)
                            .testTag("date_pill_${date.dayOfMonth}"),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Day of week abbreviation (MO, TU, WE, etc.)
                        Text(
                            text = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
                                .take(2).uppercase(),
                            color = if (isSelected) TextWhite else TextMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Day of Month number
                        Text(
                            text = "${date.dayOfMonth}",
                            color = TextWhite,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Status Streak Dot
                        if (dotColor != null) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(
                                        if (isSelected && dotColor == PlannedBlue) TextWhite else dotColor,
                                        CircleShape
                                    )
                            )
                        } else {
                            Spacer(modifier = Modifier.size(6.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Attendance Streak Legend Directly Under Date Strip
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(modifier = Modifier.size(7.dp).background(StreakGreen, CircleShape))
                    Text("Completed", color = TextMuted, fontSize = 11.sp)
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(modifier = Modifier.size(7.dp).background(PlannedBlue, CircleShape))
                    Text("Planned", color = TextMuted, fontSize = 11.sp)
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(modifier = Modifier.size(7.dp).background(MissedRed, CircleShape))
                    Text("Missed", color = TextMuted, fontSize = 11.sp)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Selected Day Header
            val fullDayName = selectedDate.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())
            val monthName = selectedDate.month.getDisplayName(TextStyle.FULL, Locale.getDefault())
            val dateDisplayString = "$fullDayName, $monthName ${selectedDate.dayOfMonth}"

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = dateDisplayString,
                    color = TextWhite,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                if (selectedDate == today) {
                    Text(
                        text = "TODAY",
                        color = ElectricBlue,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Display ONLY the single routine allocated to that specific day of the week, or Rest Day card
            if (isSelectedDayRest || allocatedRoutine == null) {
                // Sleek AMOLED Rest & Recovery Day card
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .border(1.dp, DarkBorder, RoundedCornerShape(14.dp))
                        .testTag("rest_day_card"),
                    color = DarkCardSurface
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(DarkBorderHover, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Bedtime,
                                    contentDescription = "Rest Day",
                                    tint = TextMuted,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Column {
                                Text(
                                    text = "🛌 Rest & Recovery Day",
                                    color = TextWhite,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Time to rest and grow!",
                                    color = TextMuted,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        FilledTonalButton(
                            onClick = onManageSplitClick,
                            modifier = Modifier
                                .height(34.dp)
                                .testTag("assign_workout_button"),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = ElectricBlueAlpha,
                                contentColor = ElectricBlueLight
                            ),
                            shape = RoundedCornerShape(18.dp)
                        ) {
                            Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Assign", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            } else {
                // Single Allocated Routine Card for this Day
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .border(1.dp, ElectricBlueDark, RoundedCornerShape(14.dp))
                        .testTag("allocated_routine_card"),
                    color = DarkCardSurface
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
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .background(ElectricBlueAlpha, CircleShape),
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
                                        text = allocatedRoutine.title,
                                        color = TextWhite,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    val reminder = splitForSelectedDay?.reminderTime ?: "06:30 PM"
                                    Text(
                                        text = "⏰ Reminder at $reminder",
                                        color = ElectricBlueLight,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }

                        if (allocatedRoutine.exercisesSummary.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = allocatedRoutine.exercisesSummary,
                                color = TextMuted,
                                fontSize = 12.sp,
                                maxLines = 2
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // ▶ Start Button
                        Button(
                            onClick = { onStartRoutine(allocatedRoutine) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("start_allocated_routine_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ElectricBlue,
                                contentColor = TextWhite
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(
                                Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "▶ Start",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

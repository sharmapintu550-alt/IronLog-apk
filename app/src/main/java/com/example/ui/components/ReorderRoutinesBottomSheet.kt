package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.RoutineEntity
import com.example.ui.theme.*

private val SETS_PATTERN = Regex("""\(\s*\d+\s*sets?\s*\)""", RegexOption.IGNORE_CASE)

private fun cleanRoutineSummary(text: String): String {
    return text
        .replace(SETS_PATTERN, "")
        .replace(Regex("""\s*,\s*,"""), ",")
        .replace(Regex("""\s{2,}"""), " ")
        .trim()
        .trimEnd(',')
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReorderRoutinesBottomSheet(
    routines: List<RoutineEntity>,
    onDismiss: () -> Unit,
    onSaveOrder: (List<RoutineEntity>) -> Unit
) {
    val reorderedList = remember(routines) {
        mutableStateListOf<RoutineEntity>().apply {
            addAll(routines)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = DarkCardBg,
        dragHandle = { BottomSheetDefaults.DragHandle(color = DarkBorderHover) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .navigationBarsPadding()
                .imePadding()
                .padding(bottom = 20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Reorder Routines",
                        color = TextWhite,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Drag handle or tap chevrons to arrange routines",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (reorderedList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No routines found to reorder",
                        color = TextMuted,
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(
                        items = reorderedList,
                        key = { _, routine -> routine.id }
                    ) { index, routine ->
                        var dragAccumulator by remember { mutableFloatStateOf(0f) }
                        val cleanedSummary = remember(routine.exercisesSummary) {
                            cleanRoutineSummary(routine.exercisesSummary)
                        }

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.dp, DarkBorder, RoundedCornerShape(12.dp)),
                            color = DarkCardSurface
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFF1E1E24))
                                            .pointerInput(routine.id) {
                                                detectDragGestures(
                                                    onDragStart = { dragAccumulator = 0f },
                                                    onDragEnd = { dragAccumulator = 0f },
                                                    onDragCancel = { dragAccumulator = 0f },
                                                    onDrag = { change, dragAmount ->
                                                        change.consume()
                                                        dragAccumulator += dragAmount.y
                                                        val threshold = 70f
                                                        val currentIndex = reorderedList.indexOfFirst { it.id == routine.id }
                                                        if (currentIndex != -1) {
                                                            if (dragAccumulator > threshold && currentIndex < reorderedList.size - 1) {
                                                                val item = reorderedList.removeAt(currentIndex)
                                                                reorderedList.add(currentIndex + 1, item)
                                                                dragAccumulator = 0f
                                                            } else if (dragAccumulator < -threshold && currentIndex > 0) {
                                                                val item = reorderedList.removeAt(currentIndex)
                                                                reorderedList.add(currentIndex - 1, item)
                                                                dragAccumulator = 0f
                                                            }
                                                        }
                                                    }
                                                )
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.DragHandle,
                                            contentDescription = "Drag to reorder",
                                            tint = ElectricBlue,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "${index + 1}. ${routine.title}",
                                            color = TextWhite,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        if (cleanedSummary.isNotEmpty()) {
                                            Text(
                                                text = cleanedSummary,
                                                color = TextDark,
                                                fontSize = 11.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(
                                        onClick = {
                                            val curIdx = reorderedList.indexOfFirst { it.id == routine.id }
                                            if (curIdx > 0) {
                                                val item = reorderedList.removeAt(curIdx)
                                                reorderedList.add(curIdx - 1, item)
                                            }
                                        },
                                        enabled = index > 0,
                                        modifier = Modifier
                                            .size(36.dp)
                                            .testTag("move_up_$index")
                                    ) {
                                        Icon(
                                            Icons.Default.ArrowUpward,
                                            contentDescription = "Move Up",
                                            tint = if (index > 0) ElectricBlue else TextDark,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    IconButton(
                                        onClick = {
                                            val curIdx = reorderedList.indexOfFirst { it.id == routine.id }
                                            if (curIdx != -1 && curIdx < reorderedList.size - 1) {
                                                val item = reorderedList.removeAt(curIdx)
                                                reorderedList.add(curIdx + 1, item)
                                            }
                                        },
                                        enabled = index < reorderedList.size - 1,
                                        modifier = Modifier
                                            .size(36.dp)
                                            .testTag("move_down_$index")
                                    ) {
                                        Icon(
                                            Icons.Default.ArrowDownward,
                                            contentDescription = "Move Down",
                                            tint = if (index < reorderedList.size - 1) ElectricBlue else TextDark,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    onSaveOrder(reorderedList.toList())
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("save_reorder_button"),
                colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save New Order", fontWeight = FontWeight.Bold, color = TextWhite)
            }
        }
    }
}

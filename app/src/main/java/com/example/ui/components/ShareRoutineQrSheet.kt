package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.ExerciseEntity
import com.example.data.entity.RoutineEntity
import com.example.ui.theme.*
import com.example.util.QrCodeGenerator
import com.example.util.SharedRoutinePayload

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareRoutineQrSheet(
    routine: RoutineEntity,
    allDbExercises: List<ExerciseEntity>,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    val payload = remember(routine, allDbExercises) {
        QrCodeGenerator.buildRoutinePayload(routine, allDbExercises)
    }

    val deepLink = remember(payload) {
        QrCodeGenerator.createDeepLink(payload)
    }

    val qrBitmap = remember(deepLink) {
        try {
            QrCodeGenerator.generateStandardQrBitmap(deepLink, 512)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    val muscleGroups = remember(payload) {
        payload.exercises.map { it.muscle }
            .filter { it.isNotBlank() && !it.equals("Full Body", ignoreCase = true) }
            .distinct()
            .take(3)
            .joinToString(", ")
            .ifEmpty { "Full Body" }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = AmoledBlack,
        dragHandle = { BottomSheetDefaults.DragHandle(color = DarkBorderHover) },
        modifier = Modifier.testTag("share_routine_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 24.dp)
                .padding(bottom = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Row with Routine Name in bold Electric Blue (#1877F2)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = routine.title,
                        color = ElectricBlue,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${payload.exercises.size} Exercises • Target: $muscleGroups",
                        color = TextMuted,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("close_routine_share_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = TextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Centered QR Image with subtle rounded white border for contrast
            Surface(
                modifier = Modifier
                    .size(260.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.5.dp, androidx.compose.ui.graphics.Color.White.copy(alpha = 0.9f), RoundedCornerShape(16.dp))
                    .testTag("qr_code_display_container"),
                color = androidx.compose.ui.graphics.Color.White
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (qrBitmap != null) {
                        Image(
                            bitmap = qrBitmap.asImageBitmap(),
                            contentDescription = "Routine QR Code",
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag("routine_qr_image")
                        )
                    } else {
                        CircularProgressIndicator(
                            color = ElectricBlue,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // "Scan with IronLog to copy this routine" hint text
            Text(
                text = "Scan with IronLog to copy this routine",
                color = TextWhite,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Copy Link Button
                OutlinedButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("IronLog Routine Share Link", deepLink)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Routine link copied to clipboard!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("copy_share_link_button"),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ElectricBlue.copy(alpha = 0.6f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextWhite)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = null,
                        tint = ElectricBlue,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Copy Link",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Native Share Intent Button
                Button(
                    onClick = {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, "IronLog Routine: ${routine.title}")
                            putExtra(
                                Intent.EXTRA_TEXT,
                                "Check out my \"${routine.title}\" workout template on IronLog!\n\nImport Link:\n$deepLink\n\nExercises (${payload.exercises.size}):\n${routine.exercisesSummary}"
                            )
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share Workout Template"))
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("share_routine_intent_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Share Apps",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

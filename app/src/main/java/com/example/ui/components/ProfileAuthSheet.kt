package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.sync.SyncStatus
import com.example.ui.theme.*
import com.google.firebase.auth.FirebaseUser
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileAuthSheet(
    user: FirebaseUser?,
    isGuest: Boolean,
    isLoading: Boolean,
    errorMessage: String?,
    syncStatus: SyncStatus,
    routinesCount: Int,
    historyCount: Int,
    onSignInWithGoogle: () -> Unit,
    onContinueAsGuest: () -> Unit,
    onSyncNow: () -> Unit,
    onSignOut: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = AmoledBlack,
        scrimColor = Color.Black.copy(alpha = 0.75f),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = {
            BottomSheetDefaults.DragHandle(
                color = DarkBorderHover,
                modifier = Modifier.padding(top = 10.dp, bottom = 4.dp)
            )
        },
        modifier = Modifier.testTag("profile_auth_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 24.dp)
                .padding(bottom = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            if (user == null || isGuest) {
                // ==================== GUEST STATE ====================
                GuestAuthView(
                    isLoading = isLoading,
                    errorMessage = errorMessage,
                    onSignInWithGoogle = onSignInWithGoogle,
                    onContinueAsGuest = onContinueAsGuest
                )
            } else {
                // ==================== SIGNED-IN STATE ====================
                SignedInProfileView(
                    user = user,
                    syncStatus = syncStatus,
                    routinesCount = routinesCount,
                    historyCount = historyCount,
                    onSyncNow = onSyncNow,
                    onSignOut = onSignOut
                )
            }
        }
    }
}

@Composable
private fun GuestAuthView(
    isLoading: Boolean,
    errorMessage: String?,
    onSignInWithGoogle: () -> Unit,
    onContinueAsGuest: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(64.dp)
            .background(ElectricBlueAlpha, CircleShape)
            .border(1.5.dp, ElectricBlue, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.CloudSync,
            contentDescription = "Cloud Backup",
            tint = ElectricBlueLight,
            modifier = Modifier.size(34.dp)
        )
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = "Cloud Backup & Sync",
            color = TextWhite,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Sign in to securely back up your workout history, custom routines, and schedule across devices.",
            color = TextMuted,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            lineHeight = 19.sp,
            modifier = Modifier.padding(horizontal = 12.dp)
        )
    }

    AnimatedVisibility(visible = !errorMessage.isNullOrBlank()) {
        if (!errorMessage.isNullOrBlank()) {
            Surface(
                color = FailureRed.copy(alpha = 0.15f),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, FailureRed.copy(alpha = 0.6f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("auth_error_chip")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Sign-in error",
                        tint = FailureRed,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = errorMessage,
                        color = FailureRed,
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }

    // "Sign in with Google" Pill Button
    Button(
        onClick = onSignInWithGoogle,
        enabled = !isLoading,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.White,
            contentColor = Color.Black
        ),
        shape = RoundedCornerShape(26.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .testTag("google_sign_in_button")
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = ElectricBlue,
                modifier = Modifier.size(22.dp),
                strokeWidth = 2.5.dp
            )
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "G",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = ElectricBlue
                )
                Text(
                    text = "Sign in with Google",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1F1F1F)
                )
            }
        }
    }

    // "Continue as Guest (Local Only)" Button
    OutlinedButton(
        onClick = onContinueAsGuest,
        shape = RoundedCornerShape(26.dp),
        border = BorderStroke(1.dp, DarkBorder),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = TextMuted
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .testTag("continue_as_guest_button")
    ) {
        Text(
            text = "Continue as Guest (Local Only)",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun SignedInProfileView(
    user: FirebaseUser,
    syncStatus: SyncStatus,
    routinesCount: Int,
    historyCount: Int,
    onSyncNow: () -> Unit,
    onSignOut: () -> Unit
) {
    val photoUrl = user.photoUrl?.toString()
    val displayName = user.displayName ?: "Athlete"
    val email = user.email ?: ""
    val initial = displayName.firstOrNull()?.uppercaseChar()?.toString() ?: "U"

    // Profile Avatar & Info
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkCardBg, RoundedCornerShape(18.dp))
            .border(1.dp, DarkBorder, RoundedCornerShape(18.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        if (!photoUrl.isNullOrBlank()) {
            AsyncImage(
                model = photoUrl,
                contentDescription = "User Avatar",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .border(2.dp, ElectricBlue, CircleShape)
            )
        } else {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .background(ElectricBlueAlpha, CircleShape)
                    .border(2.dp, ElectricBlue, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initial,
                    color = ElectricBlueLight,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = displayName,
                color = TextWhite,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )
            if (email.isNotBlank()) {
                Text(
                    text = email,
                    color = TextMuted,
                    fontSize = 12.sp
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Box(modifier = Modifier.size(6.dp).background(StreakGreen, CircleShape))
                Text(
                    text = "Cloud Account Connected",
                    color = StreakGreen,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }

    // Cloud Sync Card & Metrics
    Surface(
        color = DarkCardBg,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, DarkBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
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
                        imageVector = Icons.Default.CloudDone,
                        contentDescription = "Sync",
                        tint = ElectricBlue,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Cloud Sync Status",
                        color = TextWhite,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Status indicator
                val statusText = when (syncStatus) {
                    is SyncStatus.Syncing -> "Syncing..."
                    is SyncStatus.Success -> "Up to date"
                    is SyncStatus.Error -> "Pending"
                    is SyncStatus.Idle -> "Connected"
                }
                val statusColor = when (syncStatus) {
                    is SyncStatus.Syncing -> ElectricBlueLight
                    is SyncStatus.Success -> StreakGreen
                    is SyncStatus.Error -> WarmupAmber
                    is SyncStatus.Idle -> TextMuted
                }

                Surface(
                    color = statusColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = statusText,
                        color = statusColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            // Sync metrics strip
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkCardSurface, RoundedCornerShape(12.dp))
                    .padding(vertical = 10.dp, horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "$routinesCount", color = TextWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text(text = "Routines", color = TextMuted, fontSize = 11.sp)
                }
                Box(modifier = Modifier.width(1.dp).height(24.dp).background(DarkBorder))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "$historyCount", color = TextWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text(text = "Logs", color = TextMuted, fontSize = 11.sp)
                }
                Box(modifier = Modifier.width(1.dp).height(24.dp).background(DarkBorder))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "7", color = TextWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text(text = "Days Split", color = TextMuted, fontSize = 11.sp)
                }
            }

            val syncSubtitle = when (syncStatus) {
                is SyncStatus.Success -> {
                    val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
                    val formattedTime = timeFormat.format(Date(syncStatus.lastSyncTime))
                    "Synced just now at $formattedTime • All data saved"
                }
                is SyncStatus.Syncing -> "Syncing data with cloud..."
                is SyncStatus.Error -> "Sync pending • Changes kept locally"
                is SyncStatus.Idle -> "Synced just now • All data saved"
            }
            Row(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val dotColor = when (syncStatus) {
                    is SyncStatus.Syncing -> ElectricBlueLight
                    is SyncStatus.Error -> WarmupAmber
                    else -> StreakGreen
                }
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .background(dotColor, CircleShape)
                )
                Text(
                    text = syncSubtitle,
                    color = TextMuted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }

    // "Sync Now" Button
    val isSyncing = syncStatus is SyncStatus.Syncing
    Button(
        onClick = onSyncNow,
        enabled = !isSyncing,
        colors = ButtonDefaults.buttonColors(
            containerColor = ElectricBlue,
            contentColor = Color.White
        ),
        shape = RoundedCornerShape(26.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .testTag("sync_now_button")
    ) {
        if (isSyncing) {
            CircularProgressIndicator(
                color = Color.White,
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Syncing with Cloud...", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Sync Now",
                    modifier = Modifier.size(18.dp)
                )
                Text(text = "Sync Now", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }

    // "Log Out" Button
    OutlinedButton(
        onClick = onSignOut,
        shape = RoundedCornerShape(26.dp),
        border = BorderStroke(1.dp, FailureRed.copy(alpha = 0.5f)),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = FailureRed
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .testTag("log_out_button")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                contentDescription = "Log Out",
                tint = FailureRed.copy(alpha = 0.9f),
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = "Log Out",
                color = FailureRed.copy(alpha = 0.9f),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

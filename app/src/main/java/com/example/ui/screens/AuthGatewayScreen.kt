package com.example.ui.screens

import android.app.Activity
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.AuthViewModel

private val AmoledBackground = Color(0xFF000000)
private val DarkCardPill = Color(0xFF1A1A1A)
private val ErrorContainerRed = Color(0xFF2A0808)
private val ErrorTextRed = Color(0xFFFF3B30)
private val MutedFootnote = Color(0xFF505050)
private val GuestTextGray = Color(0xFF757575)
private val SubtitleGray = Color(0xFFA0A0A0)

@Composable
fun AuthGatewayScreen(
    authViewModel: AuthViewModel,
    onNavigateToApp: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val isLoading by authViewModel.isLoading.collectAsState()
    val errorMessage by authViewModel.errorMessage.collectAsState()
    val authErrorMessage by authViewModel.authErrorMessage

    var showEmailSheet by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AmoledBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("auth_gateway_screen")
    ) {
        // Main Hevy-Style Pitch-Black Auth Stack
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // 1. Top/Center: IronLog Brand Identity
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                // Electric Blue Dumbbell Logo with glowing accent
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .clip(RoundedCornerShape(26.dp))
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(ElectricBlueAlpha, Color(0xFF0D1B2A))
                            )
                        )
                        .border(1.5.dp, ElectricBlue, RoundedCornerShape(26.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.FitnessCenter,
                        contentDescription = "IronLog Logo",
                        tint = ElectricBlue,
                        modifier = Modifier.size(46.dp)
                    )
                }

                // Brand Title
                Text(
                    text = "IronLog",
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.5.sp
                )

                // Subtitle
                Text(
                    text = "Track workouts, analyze progression, and build strength.",
                    color = SubtitleGray,
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
            }

            // 2. Bottom Action Stack
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // AMOLED-styled red warning container right above "Sign in with Google" button
                val activeError = authErrorMessage ?: errorMessage
                if (!activeError.isNullOrBlank()) {
                    Surface(
                        color = Color(0xFF2A0808),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, Color(0xFFFF3B30)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_error_container")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Sign-in exception",
                                tint = Color(0xFFFF3B30),
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = activeError,
                                color = Color(0xFFFF3B30),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                lineHeight = 16.sp,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Dismiss error",
                                tint = Color(0xFFFF3B30).copy(alpha = 0.8f),
                                modifier = Modifier
                                    .size(18.dp)
                                    .clickable { authViewModel.clearError() }
                            )
                        }
                    }
                }

                // Primary: Sign in with Google Pill Button
                Button(
                    onClick = {
                        authViewModel.clearError()
                        if (activity != null) {
                            authViewModel.signInWithGoogle(activity) {
                                onNavigateToApp()
                            }
                        } else {
                            Toast.makeText(context, "Activity reference not available", Toast.LENGTH_SHORT).show()
                        }
                    },
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color(0xFF1F1F1F)
                    ),
                    shape = RoundedCornerShape(27.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("google_sign_in_pill")
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = ElectricBlue,
                            strokeWidth = 2.5.dp,
                            modifier = Modifier.size(22.dp)
                        )
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "G",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = ElectricBlue,
                                modifier = Modifier.padding(end = 12.dp)
                            )
                            Text(
                                text = "Sign in with Google",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1F1F1F)
                            )
                        }
                    }
                }

                // Secondary: Continue with Email Pill Button
                Button(
                    onClick = {
                        authViewModel.clearError()
                        showEmailSheet = true
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DarkCardPill,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(25.dp),
                    border = BorderStroke(1.dp, ElectricBlue),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("email_sign_in_pill")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mail,
                            contentDescription = "Email",
                            tint = Color.White,
                            modifier = Modifier
                                .size(18.dp)
                                .padding(end = 6.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Continue with Email",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Guest Mode Option: Subtle text button at the bottom
                Text(
                    text = "Continue as Guest",
                    color = GuestTextGray,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier
                        .clickable {
                            authViewModel.continueAsGuest {
                                onNavigateToApp()
                            }
                        }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .testTag("continue_as_guest_text_btn")
                )

                // Legal / Disclaimer
                Text(
                    text = "By continuing, you agree to IronLog Terms and Privacy Policy.",
                    color = MutedFootnote,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 15.sp,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }
        }

        // Animated In-Screen Email/Password Modal Sheet
        AnimatedVisibility(
            visible = showEmailSheet,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            EmailAuthSheet(
                isLoading = isLoading,
                onDismiss = { showEmailSheet = false },
                onGoogleSignIn = {
                    showEmailSheet = false
                    if (activity != null) {
                        authViewModel.signInWithGoogle(activity) {
                            onNavigateToApp()
                        }
                    }
                },
                onContinueAsGuest = {
                    showEmailSheet = false
                    authViewModel.continueAsGuest {
                        onNavigateToApp()
                    }
                }
            )
        }
    }
}

@Composable
private fun EmailAuthSheet(
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onGoogleSignIn: () -> Unit,
    onContinueAsGuest: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Login, 1: Register
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf<String?>(null) }
    var infoNotice by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.85f))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.BottomCenter
    ) {
        Surface(
            color = Color(0xFF121212),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            border = BorderStroke(1.dp, DarkBorder),
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = false) {} // Prevent backdrop dismiss on content click
                .testTag("email_auth_sheet")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .navigationBarsPadding(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header with Tab selector and close icon
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = Color.Transparent,
                        contentColor = ElectricBlue,
                        indicator = {},
                        divider = {},
                        modifier = Modifier.width(200.dp)
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = {
                                selectedTab = 0
                                infoNotice = null
                            },
                            text = {
                                Text(
                                    text = "Log In",
                                    fontSize = 15.sp,
                                    fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium,
                                    color = if (selectedTab == 0) ElectricBlue else TextMuted
                                )
                            }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = {
                                selectedTab = 1
                                infoNotice = null
                            },
                            text = {
                                Text(
                                    text = "Register",
                                    fontSize = 15.sp,
                                    fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium,
                                    color = if (selectedTab == 1) ElectricBlue else TextMuted
                                )
                            }
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(36.dp)
                            .background(DarkCardSurface, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Info banner explaining Google Sign-In recommendation for instant cloud backup
                Surface(
                    color = ElectricBlueAlpha,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, ElectricBlue.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "💡 Instant Cloud Backup",
                            color = ElectricBlueLight,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Google Sign-In enables immediate 1-tap cloud backup without password hassles. You can also continue as Guest offline.",
                            color = TextWhite,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }
                }

                // Email Input
                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        emailError = null
                    },
                    label = { Text("Email Address") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Mail,
                            contentDescription = null,
                            tint = ElectricBlue
                        )
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricBlue,
                        unfocusedBorderColor = DarkBorder,
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        focusedLabelColor = ElectricBlue,
                        unfocusedLabelColor = TextMuted,
                        cursorColor = ElectricBlue
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Password Input
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = ElectricBlue
                        )
                    },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (email.isBlank() || password.isBlank()) {
                                emailError = "Please enter both email and password"
                            } else {
                                infoNotice = "For verified cross-device sync, please sign in with Google or continue as Guest."
                            }
                        }
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricBlue,
                        unfocusedBorderColor = DarkBorder,
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        focusedLabelColor = ElectricBlue,
                        unfocusedLabelColor = TextMuted,
                        cursorColor = ElectricBlue
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                if (emailError != null) {
                    Text(
                        text = emailError ?: "",
                        color = ErrorTextRed,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                if (infoNotice != null) {
                    Text(
                        text = infoNotice ?: "",
                        color = ElectricBlueLight,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 16.sp
                    )
                }

                // Primary Action Button
                Button(
                    onClick = {
                        if (email.isBlank() || password.isBlank()) {
                            emailError = "Please enter both email and password"
                        } else {
                            infoNotice = "Instant cloud backup is verified via Google Sign-In. Tap below to connect securely, or continue as Guest."
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ElectricBlue,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text(
                        text = if (selectedTab == 0) "Log In" else "Create Account",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Secondary: Quick Google Sign-In Shortcut Button
                OutlinedButton(
                    onClick = onGoogleSignIn,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, DarkBorder),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "G",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = ElectricBlue
                        )
                        Text(
                            text = "Use 1-Tap Google Sign-In instead",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Or Continue as Guest
                TextButton(
                    onClick = onContinueAsGuest,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text(
                        text = "Continue as Guest (Offline)",
                        color = GuestTextGray,
                        fontSize = 13.sp,
                        textDecoration = TextDecoration.Underline
                    )
                }
            }
        }
    }
}


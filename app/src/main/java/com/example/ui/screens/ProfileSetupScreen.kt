package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.*
import com.example.ui.viewmodel.WorkoutViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSetupScreen(
    viewModel: WorkoutViewModel,
    onProfileCompleted: () -> Unit,
    onDismiss: () -> Unit = onProfileCompleted
) {
    BackHandler {
        onDismiss()
    }

    val authState by viewModel.authState.collectAsStateWithLifecycle()
    val existingProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current

    val googleUser = authState.user
    val initialName = remember(googleUser, existingProfile) {
        existingProfile?.displayName?.takeIf { it.isNotBlank() }
            ?: googleUser?.displayName?.takeIf { it.isNotBlank() }
            ?: ""
    }

    var displayName by remember(initialName) { mutableStateOf(initialName) }
    var selectedGender by remember(existingProfile) {
        mutableStateOf(existingProfile?.gender ?: "Male")
    }
    var ageText by remember(existingProfile) {
        mutableStateOf((existingProfile?.age ?: 25).toString())
    }

    // Height state
    var heightUnit by remember(existingProfile) {
        mutableStateOf(existingProfile?.heightUnit ?: "cm")
    }
    var heightText by remember(existingProfile) {
        val initialHeight = existingProfile?.height ?: 175.0
        mutableStateOf(if (heightUnit == "cm") initialHeight.toInt().toString() else "%.1f".format(initialHeight))
    }

    // Bodyweight state
    var bodyweightUnit by remember(existingProfile) {
        mutableStateOf(existingProfile?.bodyweightUnit ?: "kg")
    }
    var bodyweightText by remember(existingProfile) {
        val initialWeight = existingProfile?.bodyweight ?: 75.0
        mutableStateOf(if (initialWeight % 1.0 == 0.0) initialWeight.toInt().toString() else initialWeight.toString())
    }

    // Preferred workout logging unit
    var preferredWeightUnit by remember(existingProfile) {
        mutableStateOf(existingProfile?.preferredWeightUnit ?: "kg")
    }

    // Fitness Goal
    val goalOptions = remember {
        listOf(
            "Strength & Hypertrophy",
            "Fat Loss",
            "Muscle Gain",
            "General Fitness"
        )
    }
    var selectedGoal by remember(existingProfile) {
        mutableStateOf(existingProfile?.primaryGoal ?: goalOptions[0])
    }

    // Experience Level
    val experienceOptions = remember {
        listOf(
            "Beginner (<1 yr)",
            "Intermediate (1-3 yrs)",
            "Advanced (3+ yrs)"
        )
    }
    var selectedExperience by remember(existingProfile) {
        mutableStateOf(existingProfile?.experienceLevel ?: experienceOptions[1])
    }

    var isSaving by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(AmoledBlack)
            .statusBarsPadding()
            .navigationBarsPadding(),
        containerColor = AmoledBlack,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
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
                            .clip(CircleShape)
                            .background(ElectricBlueAlpha)
                            .border(1.dp, ElectricBlue, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.FitnessCenter,
                            contentDescription = "Profile Setup Icon",
                            tint = ElectricBlue,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "PROFILE SETUP",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Black,
                            color = TextWhite,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = if (googleUser != null) "Linked to ${googleUser.email ?: "Google"}" else "Customize your experience",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                }

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("skip_profile_setup_button")
                ) {
                    Text(
                        text = "Skip",
                        color = TextMuted,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Card / Motivation Banner
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, DarkBorder, RoundedCornerShape(14.dp)),
                colors = CardDefaults.cardColors(containerColor = DarkCardBg),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Tailor Your Training",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = ElectricBlue
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Your physical metrics and fitness goals allow IronPulse to optimize your volume, strength targets, and progress analytics.",
                        fontSize = 12.sp,
                        color = TextMuted,
                        lineHeight = 17.sp
                    )
                }
            }

            // Section 1: Personal Details
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, DarkBorder, RoundedCornerShape(14.dp)),
                colors = CardDefaults.cardColors(containerColor = DarkCardBg),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "1. PERSONAL DETAILS",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = ElectricBlue,
                        letterSpacing = 0.5.sp
                    )

                    // Display Name
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Display Name",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextWhite
                        )
                        OutlinedTextField(
                            value = displayName,
                            onValueChange = { displayName = it },
                            placeholder = { Text("e.g. Alex Hunter", color = TextDark) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("profile_name_input"),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ElectricBlue,
                                unfocusedBorderColor = DarkBorder,
                                focusedTextColor = TextWhite,
                                unfocusedTextColor = TextWhite,
                                focusedContainerColor = DarkCardSurface,
                                unfocusedContainerColor = DarkCardSurface
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }

                    // Gender Chips
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Gender",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextWhite
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("Male", "Female", "Prefer not to say").forEach { gender ->
                                val isSelected = selectedGender.equals(gender, ignoreCase = true)
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) ElectricBlueAlpha else DarkCardSurface)
                                        .border(
                                            width = 1.dp,
                                            color = if (isSelected) ElectricBlue else DarkBorder,
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .clickable { selectedGender = gender }
                                        .padding(vertical = 10.dp)
                                        .testTag("gender_chip_${gender.lowercase().replace(" ", "_")}"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = gender,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) ElectricBlue else TextMuted,
                                        textAlign = TextAlign.Center,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }

                    // Age / Date of Birth
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Age",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextWhite
                        )
                        OutlinedTextField(
                            value = ageText,
                            onValueChange = { input ->
                                if (input.isEmpty() || input.all { it.isDigit() }) {
                                    ageText = input.take(3)
                                }
                            },
                            placeholder = { Text("e.g. 26", color = TextDark) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("profile_age_input"),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ElectricBlue,
                                unfocusedBorderColor = DarkBorder,
                                focusedTextColor = TextWhite,
                                unfocusedTextColor = TextWhite,
                                focusedContainerColor = DarkCardSurface,
                                unfocusedContainerColor = DarkCardSurface
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }
            }

            // Section 2: Physical Metrics (Height & Bodyweight)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, DarkBorder, RoundedCornerShape(14.dp)),
                colors = CardDefaults.cardColors(containerColor = DarkCardBg),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "2. BODY MEASUREMENTS",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = ElectricBlue,
                        letterSpacing = 0.5.sp
                    )

                    // Height with unit toggle
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Height",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextWhite
                            )

                            // cm / ft toggle
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(DarkCardSurface)
                                    .border(1.dp, DarkBorder, RoundedCornerShape(6.dp))
                                    .padding(2.dp)
                            ) {
                                listOf("cm", "ft").forEach { u ->
                                    val isSelected = heightUnit == u
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(if (isSelected) ElectricBlue else Color.Transparent)
                                            .clickable {
                                                if (heightUnit != u) {
                                                    val curr = heightText.toDoubleOrNull() ?: 175.0
                                                    if (u == "ft") {
                                                        heightText = "%.1f".format(curr / 30.48)
                                                    } else {
                                                        heightText = "%.0f".format(curr * 30.48)
                                                    }
                                                    heightUnit = u
                                                }
                                            }
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                            .testTag("height_unit_${u}"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = u,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) TextWhite else TextMuted
                                        )
                                    }
                                }
                            }
                        }

                        OutlinedTextField(
                            value = heightText,
                            onValueChange = { input ->
                                if (input.isEmpty() || input.count { it == '.' } <= 1 && input.all { it.isDigit() || it == '.' }) {
                                    heightText = input.take(5)
                                }
                            },
                            placeholder = { Text(if (heightUnit == "cm") "178" else "5.10", color = TextDark) },
                            singleLine = true,
                            trailingIcon = {
                                Text(
                                    text = heightUnit,
                                    fontSize = 12.sp,
                                    color = TextMuted,
                                    modifier = Modifier.padding(end = 12.dp)
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("profile_height_input"),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Decimal,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ElectricBlue,
                                unfocusedBorderColor = DarkBorder,
                                focusedTextColor = TextWhite,
                                unfocusedTextColor = TextWhite,
                                focusedContainerColor = DarkCardSurface,
                                unfocusedContainerColor = DarkCardSurface
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }

                    // Bodyweight with unit toggle
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Current Bodyweight",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextWhite
                            )

                            // kg / lbs toggle
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(DarkCardSurface)
                                    .border(1.dp, DarkBorder, RoundedCornerShape(6.dp))
                                    .padding(2.dp)
                            ) {
                                listOf("kg", "lbs").forEach { u ->
                                    val isSelected = bodyweightUnit == u
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(if (isSelected) ElectricBlue else Color.Transparent)
                                            .clickable {
                                                if (bodyweightUnit != u) {
                                                    val curr = bodyweightText.toDoubleOrNull() ?: 75.0
                                                    if (u == "lbs") {
                                                        bodyweightText = "%.1f".format(curr * 2.20462)
                                                    } else {
                                                        bodyweightText = "%.1f".format(curr / 2.20462)
                                                    }
                                                    bodyweightUnit = u
                                                }
                                            }
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                            .testTag("bodyweight_unit_${u}"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = u,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) TextWhite else TextMuted
                                        )
                                    }
                                }
                            }
                        }

                        OutlinedTextField(
                            value = bodyweightText,
                            onValueChange = { input ->
                                if (input.isEmpty() || input.count { it == '.' } <= 1 && input.all { it.isDigit() || it == '.' }) {
                                    bodyweightText = input.take(5)
                                }
                            },
                            placeholder = { Text(if (bodyweightUnit == "kg") "78.5" else "173", color = TextDark) },
                            singleLine = true,
                            trailingIcon = {
                                Text(
                                    text = bodyweightUnit,
                                    fontSize = 12.sp,
                                    color = TextMuted,
                                    modifier = Modifier.padding(end = 12.dp)
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("profile_weight_input"),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Decimal,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ElectricBlue,
                                unfocusedBorderColor = DarkBorder,
                                focusedTextColor = TextWhite,
                                unfocusedTextColor = TextWhite,
                                focusedContainerColor = DarkCardSurface,
                                unfocusedContainerColor = DarkCardSurface
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }

                    // Preferred Unit for Workout Logging
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Preferred Weight Unit (For Workouts & Exercises)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextWhite
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            listOf("kg", "lbs").forEach { unit ->
                                val isSelected = preferredWeightUnit == unit
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) ElectricBlueAlpha else DarkCardSurface)
                                        .border(
                                            width = 1.dp,
                                            color = if (isSelected) ElectricBlue else DarkBorder,
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .clickable { preferredWeightUnit = unit }
                                        .padding(vertical = 12.dp)
                                        .testTag("pref_unit_${unit}"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (unit == "kg") Icons.Default.Scale else Icons.Default.FitnessCenter,
                                            contentDescription = null,
                                            tint = if (isSelected) ElectricBlue else TextMuted,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = if (unit == "kg") "Kilograms (kg)" else "Pounds (lbs)",
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) ElectricBlue else TextWhite
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Section 3: Primary Fitness Goal
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, DarkBorder, RoundedCornerShape(14.dp)),
                colors = CardDefaults.cardColors(containerColor = DarkCardBg),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "3. PRIMARY FITNESS GOAL",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = ElectricBlue,
                        letterSpacing = 0.5.sp
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        goalOptions.forEach { goal ->
                            val isSelected = selectedGoal == goal
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) ElectricBlueAlpha else DarkCardSurface)
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) ElectricBlue else DarkBorder,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .clickable { selectedGoal = goal }
                                    .padding(horizontal = 14.dp, vertical = 12.dp)
                                    .testTag("goal_chip_${goal.lowercase().replace(" ", "_")}"),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = goal,
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) TextWhite else TextMuted
                                    )
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Selected",
                                            tint = ElectricBlue,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Section 4: Workout Experience Level
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, DarkBorder, RoundedCornerShape(14.dp)),
                colors = CardDefaults.cardColors(containerColor = DarkCardBg),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "4. EXPERIENCE LEVEL",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = ElectricBlue,
                        letterSpacing = 0.5.sp
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        experienceOptions.forEach { exp ->
                            val isSelected = selectedExperience == exp
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) ElectricBlueAlpha else DarkCardSurface)
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) ElectricBlue else DarkBorder,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .clickable { selectedExperience = exp }
                                    .padding(horizontal = 14.dp, vertical = 12.dp)
                                    .testTag("exp_chip_${exp.lowercase().take(5)}"),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = exp,
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) TextWhite else TextMuted
                                    )
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Selected",
                                            tint = ElectricBlue,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Save & Continue Button
            Button(
                onClick = {
                    if (!isSaving) {
                        isSaving = true
                        val heightVal = heightText.toDoubleOrNull() ?: 175.0
                        val weightVal = bodyweightText.toDoubleOrNull() ?: 75.0
                        val ageVal = ageText.toIntOrNull() ?: 25
                        val finalName = displayName.ifBlank { googleUser?.displayName ?: "Athlete" }

                        viewModel.saveUserProfile(
                            displayName = finalName,
                            gender = selectedGender,
                            age = ageVal,
                            height = heightVal,
                            heightUnit = heightUnit,
                            bodyweight = weightVal,
                            bodyweightUnit = bodyweightUnit,
                            preferredWeightUnit = preferredWeightUnit,
                            primaryGoal = selectedGoal,
                            experienceLevel = selectedExperience,
                            onComplete = {
                                isSaving = false
                                onProfileCompleted()
                            }
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("save_profile_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ElectricBlue,
                    contentColor = TextWhite
                ),
                shape = RoundedCornerShape(12.dp),
                enabled = !isSaving
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = TextWhite,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "SAVING...",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                } else {
                    Text(
                        text = "SAVE & CONTINUE",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

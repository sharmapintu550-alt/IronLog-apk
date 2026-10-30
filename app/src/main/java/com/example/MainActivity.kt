package com.example

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.view.WindowCompat
import java.security.MessageDigest
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.RoutineEntity
import com.example.ui.components.MinimizedWorkoutBar
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.WorkoutViewModel

enum class NavigationTab(val title: String, val icon: ImageVector) {
    WORKOUTS("Workouts", Icons.Default.FitnessCenter),
    HISTORY("History", Icons.Default.History),
    EXERCISES("Exercises", Icons.AutoMirrored.Filled.FormatListBulleted),
    MEASURE("Measure", Icons.Default.Person)
}

class MainActivity : ComponentActivity() {

    private val viewModel: WorkoutViewModel by viewModels()
    private val authViewModel: com.example.ui.viewmodel.AuthViewModel by viewModels()
    private val deepLinkDataState = mutableStateOf<String?>(null)
    private val appSigningSha1State = mutableStateOf("")

    private val notificationPermissionLauncher = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        Log.i("MainActivity", "Notification permission granted: $isGranted")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleDeepLink(intent)
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        // One-time logger to compute and print the app's signing SHA-1 fingerprint
        val signingSha1 = getAppSigningSha1(this)
        appSigningSha1State.value = signingSha1
        Log.i("AppSigningSHA1", "========================================")
        Log.i("AppSigningSHA1", "App Signing SHA-1: $signingSha1")
        Log.i("IronLogAuth", "App Signing SHA-1: $signingSha1")
        Log.i("AppSigningSHA1", "========================================")

        setContent {
            IronLogTheme {
                val coroutineScope = rememberCoroutineScope()
                val tabs = remember { NavigationTab.entries }
                val pagerState = rememberPagerState(initialPage = 0, pageCount = { tabs.size })
                val deepLinkData by deepLinkDataState
                var activeWorkoutRoutine by remember { mutableStateOf<RoutineEntity?>(null) }
                var isWorkoutActive by remember { mutableStateOf(false) }
                var isWorkoutMinimized by remember { mutableStateOf(false) }
                var showSplashScreen by remember { mutableStateOf(true) }

                LaunchedEffect(deepLinkData) {
                    if (deepLinkData?.startsWith("action_open_active_workout") == true) {
                        isWorkoutActive = true
                        isWorkoutMinimized = false
                    } else if (!deepLinkData.isNullOrBlank()) {
                        pagerState.scrollToPage(0)
                    }
                }

                val authState by viewModel.authState.collectAsStateWithLifecycle()
                val hasCompletedOnboarding by viewModel.hasCompletedOnboarding.collectAsStateWithLifecycle()
                val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
                val showProfileSetup by viewModel.showProfileSetup.collectAsStateWithLifecycle()
                val activeWorkoutState by viewModel.activeWorkoutState.collectAsStateWithLifecycle()

                // If launched or reopened from Ongoing Workout Notification
                LaunchedEffect(Unit) {
                    if (intent?.action == com.example.service.WorkoutTrackingService.ACTION_OPEN_ACTIVE_WORKOUT) {
                        isWorkoutActive = true
                        isWorkoutMinimized = false
                    }
                }

                val shouldShowOnboardingGate = remember(hasCompletedOnboarding, authState.user) {
                    !hasCompletedOnboarding && authState.user == null
                }

                val shouldShowProfileSetup = remember(authState, userProfile, showProfileSetup) {
                    showProfileSetup || (authState.user != null && userProfile != null && !userProfile!!.isProfileComplete)
                }

                if (showSplashScreen) {
                    SplashScreen(
                        onSplashFinished = { showSplashScreen = false }
                    )
                } else if (shouldShowOnboardingGate) {
                    AuthGatewayScreen(
                        authViewModel = authViewModel,
                        onNavigateToApp = {
                            viewModel.setHasCompletedOnboarding(true)
                        }
                    )
                } else if (shouldShowProfileSetup) {
                    ProfileSetupScreen(
                        viewModel = viewModel,
                        onProfileCompleted = { viewModel.dismissProfileSetup() },
                        onDismiss = { viewModel.dismissProfileSetup() }
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Scaffold(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(AmoledBlack),
                            containerColor = AmoledBlack,
                            contentWindowInsets = WindowInsets.navigationBars,
                            bottomBar = {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .windowInsetsPadding(WindowInsets.navigationBars.only(WindowInsetsSides.Bottom))
                                ) {
                                    // Persistent Minimized Workout Bar
                                    if (isWorkoutActive && isWorkoutMinimized) {
                                        MinimizedWorkoutBar(
                                            routineTitle = activeWorkoutRoutine?.title ?: "Quick Workout",
                                            viewModel = viewModel,
                                            onExpand = { isWorkoutMinimized = false },
                                            onFinish = {
                                                isWorkoutActive = false
                                                isWorkoutMinimized = false
                                                activeWorkoutRoutine = null
                                            }
                                        )
                                    }
                                    NavigationBar(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .border(width = 1.dp, color = DarkBorder)
                                            .testTag("bottom_nav_bar"),
                                        containerColor = AmoledBlack,
                                        tonalElevation = 0.dp,
                                        windowInsets = WindowInsets(0, 0, 0, 0)
                                    ) {
                                        tabs.forEachIndexed { index, tab ->
                                            val isSelected = pagerState.currentPage == index
                                            NavigationBarItem(
                                                selected = isSelected,
                                                onClick = {
                                                    if (pagerState.currentPage != index) {
                                                        coroutineScope.launch {
                                                            pagerState.animateScrollToPage(
                                                                page = index,
                                                                animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing)
                                                            )
                                                        }
                                                    }
                                                },
                                                icon = {
                                                    Icon(
                                                        imageVector = tab.icon,
                                                        contentDescription = tab.title,
                                                        modifier = Modifier.size(24.dp)
                                                    )
                                                },
                                                label = {
                                                    Text(
                                                        text = tab.title,
                                                        fontSize = 11.sp
                                                    )
                                                },
                                                colors = NavigationBarItemDefaults.colors(
                                                    selectedIconColor = ElectricBlue,
                                                    unselectedIconColor = TextMuted,
                                                    selectedTextColor = ElectricBlue,
                                                    unselectedTextColor = TextMuted,
                                                    indicatorColor = ElectricBlueAlpha
                                                ),
                                                modifier = Modifier.testTag("tab_${tab.name.lowercase()}")
                                            )
                                        }
                                    }
                                }
                            }
                        ) { innerPadding ->
                            HorizontalPager(
                                state = pagerState,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .statusBarsPadding()
                                    .displayCutoutPadding()
                                    .padding(innerPadding),
                                beyondViewportPageCount = 1,
                                key = { pageIndex -> tabs[pageIndex].name }
                            ) { page ->
                                when (tabs[page]) {
                                    NavigationTab.WORKOUTS -> {
                                        WorkoutsScreen(
                                            viewModel = viewModel,
                                            onStartRoutine = { routine ->
                                                runCatching {
                                                    activeWorkoutRoutine = routine
                                                    isWorkoutActive = true
                                                    isWorkoutMinimized = false
                                                }.onFailure { Log.e("MainActivity", "Error launching active workout routine", it) }
                                            },
                                            onResumeWorkout = { state ->
                                                runCatching {
                                                    activeWorkoutRoutine = RoutineEntity(
                                                        id = state.routineId ?: "",
                                                        title = state.routineName,
                                                        description = "",
                                                        exercisesSummary = "",
                                                        sortOrder = 0
                                                    )
                                                    isWorkoutActive = true
                                                    isWorkoutMinimized = false
                                                }.onFailure { Log.e("MainActivity", "Error resuming active workout", it) }
                                            },
                                            initialImportData = deepLinkData,
                                            onClearDeepLink = { deepLinkDataState.value = null }
                                        )
                                    }
                                    NavigationTab.HISTORY -> {
                                        WorkoutHistoryScreen(viewModel = viewModel)
                                    }
                                    NavigationTab.EXERCISES -> {
                                        ExercisesScreen(viewModel = viewModel)
                                    }
                                    NavigationTab.MEASURE -> {
                                        MeasureScreen(viewModel = viewModel)
                                    }
                                }
                            }
                        }

                        // Full-screen ActiveWorkoutScreen Overlay with zero state loss
                        if (isWorkoutActive) {
                            val offsetY by animateDpAsState(
                                targetValue = if (isWorkoutMinimized) 1400.dp else 0.dp,
                                animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing),
                                label = "workout_minimize_anim"
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .offset(y = offsetY)
                                    .graphicsLayer {
                                        alpha = if (isWorkoutMinimized) 0f else 1f
                                    }
                            ) {
                                ActiveWorkoutScreen(
                                    routine = activeWorkoutRoutine,
                                    viewModel = viewModel,
                                    restoredState = activeWorkoutState,
                                    onMinimize = { isWorkoutMinimized = true },
                                    onFinish = {
                                        isWorkoutActive = false
                                        isWorkoutMinimized = false
                                        activeWorkoutRoutine = null
                                        viewModel.endWorkout(this@MainActivity)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleDeepLink(intent)
        if (intent.action == com.example.service.WorkoutTrackingService.ACTION_OPEN_ACTIVE_WORKOUT) {
            deepLinkDataState.value = "action_open_active_workout_${System.currentTimeMillis()}"
        }
    }

    private fun handleDeepLink(intent: Intent?) {
        val data = intent?.data
        if (data != null && (data.scheme == "ironlog" || data.path?.contains("routine/share") == true)) {
            deepLinkDataState.value = data.toString()
        }
    }

    private fun getAppSigningSha1(context: Context): String {
        return try {
            val pm = context.packageManager
            val packageName = context.packageName
            val signatures = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val packageInfo = pm.getPackageInfo(packageName, PackageManager.GET_SIGNING_CERTIFICATES)
                packageInfo.signingInfo?.apkContentsSigners
            } else {
                @Suppress("DEPRECATION")
                val packageInfo = pm.getPackageInfo(packageName, PackageManager.GET_SIGNATURES)
                @Suppress("DEPRECATION")
                packageInfo.signatures
            }
            val cert = signatures?.firstOrNull()?.toByteArray()
            if (cert != null) {
                val md = MessageDigest.getInstance("SHA-1")
                val digest = md.digest(cert)
                digest.joinToString(":") { String.format("%02X", it) }
            } else {
                "No certificate found"
            }
        } catch (e: Exception) {
            "Error reading SHA-1: ${e.message}"
        }
    }
}

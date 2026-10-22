package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.RoutineEntity
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            IronLogTheme {
                val coroutineScope = rememberCoroutineScope()
                val tabs = remember { NavigationTab.entries }
                val pagerState = rememberPagerState(initialPage = 0, pageCount = { tabs.size })
                var activeWorkoutRoutine by remember { mutableStateOf<RoutineEntity?>(null) }
                var isWorkoutActive by remember { mutableStateOf(false) }

                AnimatedContent(
                    targetState = isWorkoutActive,
                    transitionSpec = {
                        if (targetState) {
                            slideIntoContainer(
                                towards = AnimatedContentTransitionScope.SlideDirection.Up,
                                animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing)
                            ) + fadeIn(animationSpec = tween(180)) togetherWith
                            fadeOut(animationSpec = tween(150))
                        } else {
                            fadeIn(animationSpec = tween(180)) togetherWith
                            slideOutOfContainer(
                                towards = AnimatedContentTransitionScope.SlideDirection.Down,
                                animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing)
                            ) + fadeOut(animationSpec = tween(150))
                        }
                    },
                    label = "ActiveWorkoutTransition"
                ) { workoutActive ->
                    if (workoutActive) {
                        ActiveWorkoutScreen(
                            routine = activeWorkoutRoutine,
                            viewModel = viewModel,
                            onFinish = {
                                isWorkoutActive = false
                                activeWorkoutRoutine = null
                            }
                        )
                    } else {
                        Scaffold(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(AmoledBlack),
                            containerColor = AmoledBlack,
                            contentWindowInsets = WindowInsets(0, 0, 0, 0),
                            bottomBar = {
                                NavigationBar(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(width = 1.dp, color = DarkBorder)
                                        .testTag("bottom_nav_bar"),
                                    containerColor = AmoledBlack,
                                    tonalElevation = 0.dp
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
                        ) { innerPadding ->
                            HorizontalPager(
                                state = pagerState,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .statusBarsPadding()
                                    .padding(innerPadding),
                                beyondViewportPageCount = 1,
                                key = { pageIndex -> tabs[pageIndex].name }
                            ) { page ->
                                when (tabs[page]) {
                                    NavigationTab.WORKOUTS -> {
                                        WorkoutsScreen(
                                            viewModel = viewModel,
                                            onStartRoutine = { routine ->
                                                activeWorkoutRoutine = routine
                                                isWorkoutActive = true
                                            }
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
                    }
                }
            }
        }
    }
}

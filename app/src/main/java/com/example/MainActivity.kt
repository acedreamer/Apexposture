package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarViewDay
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material.icons.outlined.CalendarViewDay
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.PlayCircleOutline
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.TimerMode
import com.example.ui.components.formatMinutesSeconds
import com.example.ui.screens.ActiveWorkoutScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.HistoryAndBackupScreen
import com.example.ui.screens.WorkoutPlanScreen
import com.example.ui.theme.AmberBright
import com.example.ui.theme.EmeraldBright
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.OledBackground
import com.example.ui.theme.OledCardBorder
import com.example.ui.theme.OledSurface
import com.example.ui.theme.OledSurfaceVariant
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.WorkoutViewModel
import kotlinx.coroutines.delay

enum class AppDestination(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
) {
    DASHBOARD(
        route = "dashboard",
        label = "Dashboard",
        selectedIcon = Icons.Filled.Dashboard,
        unselectedIcon = Icons.Outlined.Dashboard,
        testTag = "nav_dashboard"
    ),
    PLAN(
        route = "plan",
        label = "4-Day Plan",
        selectedIcon = Icons.Filled.CalendarViewDay,
        unselectedIcon = Icons.Outlined.CalendarViewDay,
        testTag = "nav_plan"
    ),
    LIVE_TRACKER(
        route = "live_tracker",
        label = "Live Tracker",
        selectedIcon = Icons.Filled.PlayCircleFilled,
        unselectedIcon = Icons.Outlined.PlayCircleOutline,
        testTag = "nav_live_tracker"
    ),
    HISTORY_BACKUP(
        route = "history_backup",
        label = "Logs & Backup",
        selectedIcon = Icons.Filled.History,
        unselectedIcon = Icons.Outlined.History,
        testTag = "nav_history_backup"
    )
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                ApexPostureApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApexPostureApp(
    workoutViewModel: WorkoutViewModel = viewModel()
) {
    val context = LocalContext.current
    var currentDestination by rememberSaveable { mutableStateOf(AppDestination.DASHBOARD) }

    val days by workoutViewModel.days.collectAsStateWithLifecycle()
    val completedLogs by workoutViewModel.completedLogs.collectAsStateWithLifecycle()
    val currentStreak by workoutViewModel.currentStreak.collectAsStateWithLifecycle()
    val bestStreak by workoutViewModel.bestStreak.collectAsStateWithLifecycle()
    val selectedPlanDayIndex by workoutViewModel.selectedPlanDayIndex.collectAsStateWithLifecycle()
    val activeWorkoutDayId by workoutViewModel.activeWorkoutDayId.collectAsStateWithLifecycle()
    val activeExerciseIndex by workoutViewModel.activeExerciseIndex.collectAsStateWithLifecycle()
    val completedSetsMap by workoutViewModel.completedSetsMap.collectAsStateWithLifecycle()
    val sessionElapsedSeconds by workoutViewModel.sessionElapsedSeconds.collectAsStateWithLifecycle()
    val timerState by workoutViewModel.timerState.collectAsStateWithLifecycle()
    val statusMessage by workoutViewModel.statusMessage.collectAsStateWithLifecycle()

    // Runtime POST_NOTIFICATIONS permission launcher for Android 13+ (API 33+)
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ -> }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!hasPermission) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    // Auto-clear status toast banner after 4 seconds
    LaunchedEffect(statusMessage) {
        if (statusMessage != null) {
            delay(4000L)
            workoutViewModel.clearStatusMessage()
        }
    }

    // BackHandler on secondary tabs to return to Dashboard cleanly
    BackHandler(enabled = currentDestination != AppDestination.DASHBOARD) {
        currentDestination = AppDestination.DASHBOARD
    }

    val activeWorkoutDay = days.find { it.id == activeWorkoutDayId } ?: days.firstOrNull()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = OledBackground,
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "ApexPosture",
                            style = MaterialTheme.typography.titleLarge,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Box(
                            modifier = Modifier
                                .background(EmeraldContainer, RoundedCornerShape(6.dp))
                                .border(1.dp, EmeraldPrimary.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "UPPER CHEST & APT",
                                style = MaterialTheme.typography.labelSmall,
                                color = EmeraldBright,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                },
                actions = {
                    if (timerState.mode != TimerMode.IDLE) {
                        Surface(
                            modifier = Modifier
                                .padding(end = 12.dp)
                                .clickable { currentDestination = AppDestination.LIVE_TRACKER }
                                .testTag("topbar_active_timer_pill"),
                            color = EmeraldContainer,
                            shape = RoundedCornerShape(50),
                            border = androidx.compose.foundation.BorderStroke(1.dp, AmberBright)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = formatMinutesSeconds(timerState.remainingSeconds),
                                    fontFamily = JetBrainsMonoFontFamily,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = AmberBright,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = OledSurface,
                    titleContentColor = TextPrimary
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = OledSurface,
                tonalElevation = 8.dp
            ) {
                AppDestination.entries.forEach { dest ->
                    val selected = currentDestination == dest
                    NavigationBarItem(
                        selected = selected,
                        onClick = { currentDestination = dest },
                        icon = {
                            Icon(
                                imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                contentDescription = dest.label
                            )
                        },
                        label = {
                            Text(
                                text = dest.label,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF042219),
                            selectedTextColor = EmeraldBright,
                            indicatorColor = EmeraldBright,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary
                        ),
                        modifier = Modifier.testTag(dest.testTag)
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Non-blocking status banner when actions occur (e.g., Test Alarm, Video Saved, Backup Exported)
            AnimatedVisibility(visible = statusMessage != null) {
                statusMessage?.let { msg ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .border(1.dp, EmeraldBright.copy(alpha = 0.6f), RoundedCornerShape(14.dp))
                            .testTag("status_banner"),
                        color = OledSurfaceVariant,
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = EmeraldBright,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = msg,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextPrimary
                                )
                            }
                            IconButton(
                                onClick = { workoutViewModel.clearStatusMessage() },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Dismiss notification banner",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            AnimatedContent(
                targetState = currentDestination,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "mainScreenDestinationTransition",
                modifier = Modifier.weight(1f)
            ) { targetDest ->
                when (targetDest) {
                    AppDestination.DASHBOARD -> {
                        DashboardScreen(
                            days = days,
                            completedLogs = completedLogs,
                            currentStreak = currentStreak,
                            bestStreak = bestStreak,
                            onStartWorkoutDay = { dayId ->
                                workoutViewModel.startOrSwitchLiveWorkout(dayId)
                                currentDestination = AppDestination.LIVE_TRACKER
                            },
                            onInspectDayInPlanner = { dayIdx ->
                                workoutViewModel.selectPlanDayIndex(dayIdx)
                                currentDestination = AppDestination.PLAN
                            },
                            onTestAlarm = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    val granted = ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.POST_NOTIFICATIONS
                                    ) == PackageManager.PERMISSION_GRANTED
                                    if (!granted) {
                                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    }
                                }
                                workoutViewModel.triggerTestAlarm()
                            }
                        )
                    }

                    AppDestination.PLAN -> {
                        WorkoutPlanScreen(
                            days = days,
                            selectedDayIndex = selectedPlanDayIndex,
                            onSelectDayIndex = { workoutViewModel.selectPlanDayIndex(it) },
                            onStartLiveWorkout = { dayId ->
                                workoutViewModel.startOrSwitchLiveWorkout(dayId)
                                currentDestination = AppDestination.LIVE_TRACKER
                            },
                            onSaveYoutubeVideo = { exerciseId, rawUrl ->
                                workoutViewModel.saveYoutubeVideoForExercise(exerciseId, rawUrl)
                            },
                            onRemoveYoutubeVideo = { exerciseId ->
                                workoutViewModel.removeYoutubeVideoForExercise(exerciseId)
                            },
                            onUpdateExercise = { dayId, updated ->
                                workoutViewModel.updateExercise(dayId, updated)
                            },
                            onAddExercise = { dayId, created ->
                                workoutViewModel.addExerciseToDay(dayId, created)
                            },
                            onDeleteExercise = { dayId, exerciseId ->
                                workoutViewModel.deleteExerciseFromDay(dayId, exerciseId)
                            },
                            onMoveExercise = { dayId, fromIdx, toIdx ->
                                workoutViewModel.moveExercise(dayId, fromIdx, toIdx)
                            }
                        )
                    }

                    AppDestination.LIVE_TRACKER -> {
                        ActiveWorkoutScreen(
                            days = days,
                            activeWorkoutDay = activeWorkoutDay,
                            activeExerciseIndex = activeExerciseIndex,
                            completedSetsMap = completedSetsMap,
                            sessionElapsedSeconds = sessionElapsedSeconds,
                            timerState = timerState,
                            onSwitchWorkoutDay = { dayId ->
                                workoutViewModel.startOrSwitchLiveWorkout(dayId)
                            },
                            onSelectExerciseIndex = { idx ->
                                workoutViewModel.selectActiveExerciseIndex(idx)
                            },
                            onPreviousExercise = { workoutViewModel.previousActiveExercise() },
                            onNextExercise = { workoutViewModel.nextActiveExercise() },
                            onToggleSetCompleted = { exercise, setIdx ->
                                workoutViewModel.toggleSetCompleted(exercise, setIdx)
                            },
                            onStartHoldTimer = { exercise, sideLabel ->
                                workoutViewModel.startIsometricHoldTimer(exercise, sideLabel)
                            },
                            onStartManualRest = { exercise ->
                                workoutViewModel.startRestTimer(
                                    restSeconds = exercise.restSeconds,
                                    exerciseName = exercise.name,
                                    setNumber = 1,
                                    totalSets = exercise.sets
                                )
                            },
                            onToggleTimerPauseResume = { workoutViewModel.toggleTimerPauseResume() },
                            onAdjustTimerSeconds = { delta -> workoutViewModel.adjustTimerSeconds(delta) },
                            onCancelTimer = { workoutViewModel.cancelTimer() },
                            onSaveYoutubeVideo = { exerciseId, rawUrl ->
                                workoutViewModel.saveYoutubeVideoForExercise(exerciseId, rawUrl)
                            },
                            onRemoveYoutubeVideo = { exerciseId ->
                                workoutViewModel.removeYoutubeVideoForExercise(exerciseId)
                            },
                            onFinishWorkoutSession = {
                                workoutViewModel.completeActiveWorkoutSession()
                                currentDestination = AppDestination.HISTORY_BACKUP
                            }
                        )
                    }

                    AppDestination.HISTORY_BACKUP -> {
                        HistoryAndBackupScreen(
                            logs = completedLogs,
                            currentStreak = currentStreak,
                            bestStreak = bestStreak,
                            onCreateBackupJson = { workoutViewModel.createBackupJson() },
                            onRestoreBackupJson = { json -> workoutViewModel.restoreFromBackupJson(json) },
                            onResetDefaultRoutine = { workoutViewModel.resetRoutineToDefaults() },
                            onPostStatus = { msg -> workoutViewModel.postStatusMessage(msg) }
                        )
                    }
                }
            }
        }
    }
}

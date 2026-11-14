package com.example.ui.screens

import android.app.Activity
import android.view.WindowManager
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.Exercise
import com.example.model.LiveTimerState
import com.example.model.WorkoutDay
import com.example.ui.components.AnimatedTimerRingCard
import com.example.ui.components.ExerciseFormAndYoutubeSection
import com.example.ui.components.formatMinutesSeconds
import com.example.ui.theme.AmberBright
import com.example.ui.theme.CyanBright
import com.example.ui.theme.EmeraldBright
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.OledCardBorder
import com.example.ui.theme.OledSurface
import com.example.ui.theme.OledSurfaceElevated
import com.example.ui.theme.OledSurfaceVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun ActiveWorkoutScreen(
    days: List<WorkoutDay>,
    activeWorkoutDay: WorkoutDay?,
    activeExerciseIndex: Int,
    completedSetsMap: Map<String, Set<Int>>,
    sessionElapsedSeconds: Int,
    timerState: LiveTimerState,
    onSwitchWorkoutDay: (String) -> Unit,
    onSelectExerciseIndex: (Int) -> Unit,
    onPreviousExercise: () -> Unit,
    onNextExercise: () -> Unit,
    onToggleSetCompleted: (Exercise, Int) -> Unit,
    onStartHoldTimer: (Exercise, String?) -> Unit,
    onStartManualRest: (Exercise) -> Unit,
    onToggleTimerPauseResume: () -> Unit,
    onAdjustTimerSeconds: (Int) -> Unit,
    onCancelTimer: () -> Unit,
    onSaveYoutubeVideo: (exerciseId: String, rawUrl: String) -> Boolean,
    onRemoveYoutubeVideo: (exerciseId: String) -> Unit,
    onFinishWorkoutSession: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Keep screen awake with WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON during active workout
    DisposableEffect(Unit) {
        val window = (context as? Activity)?.window
        window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    val day = activeWorkoutDay ?: days.firstOrNull() ?: return
    val exercises = day.exercises
    if (exercises.isEmpty()) return

    val safeIndex = activeExerciseIndex.coerceIn(0, exercises.size - 1)
    val currentExercise = exercises[safeIndex]
    val completedSetsForCurrent = completedSetsMap[currentExercise.id] ?: emptySet()

    val totalCompletedSetsInDay = exercises.sumOf { ex -> (completedSetsMap[ex.id]?.size ?: 0) }
    val totalSetsInDay = day.totalSets.coerceAtLeast(1)
    val dayProgress by animateFloatAsState(
        targetValue = (totalCompletedSetsInDay.toFloat() / totalSetsInDay.toFloat()).coerceIn(0f, 1f),
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "dayOverallSetProgress"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("active_workout_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Session Status & Wake Lock Header
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, OledCardBorder, RoundedCornerShape(20.dp)),
                color = OledSurface,
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LockOpen,
                                contentDescription = "Screen Wake Lock Active",
                                tint = EmeraldBright,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "WAKE LOCK ON • ${formatMinutesSeconds(sessionElapsedSeconds)}",
                                fontFamily = JetBrainsMonoFontFamily,
                                style = MaterialTheme.typography.labelMedium,
                                color = EmeraldBright,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = "$totalCompletedSetsInDay / $totalSetsInDay Sets Done",
                            fontFamily = JetBrainsMonoFontFamily,
                            style = MaterialTheme.typography.labelMedium,
                            color = TextSecondary
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Day Switcher Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        days.forEach { d ->
                            val selected = d.id == day.id
                            FilterChip(
                                selected = selected,
                                onClick = { onSwitchWorkoutDay(d.id) },
                                label = {
                                    Text(
                                        text = "D${d.dayNumber}",
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                    )
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("live_day_chip_${d.dayNumber}"),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = EmeraldPrimary,
                                    selectedLabelColor = Color(0xFF042219),
                                    containerColor = OledSurfaceVariant,
                                    labelColor = TextSecondary
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = day.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    LinearProgressIndicator(
                        progress = { dayProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(50)),
                        color = EmeraldBright,
                        trackColor = OledSurfaceElevated
                    )
                }
            }
        }

        // Horizontal Exercise Stepper Rail
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                itemsIndexed(exercises, key = { _, ex -> ex.id }) { idx, ex ->
                    val isCurrent = idx == safeIndex
                    val doneSets = completedSetsMap[ex.id]?.size ?: 0
                    val isAllSetsDone = doneSets >= ex.sets
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .border(
                                width = 1.5.dp,
                                color = when {
                                    isCurrent -> EmeraldBright
                                    isAllSetsDone -> EmeraldPrimary.copy(alpha = 0.6f)
                                    else -> OledCardBorder
                                },
                                shape = RoundedCornerShape(14.dp)
                            )
                            .clickable { onSelectExerciseIndex(idx) }
                            .testTag("live_exercise_pill_${idx + 1}"),
                        color = if (isCurrent) EmeraldContainer else OledSurface,
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            if (isAllSetsDone) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Completed",
                                    tint = EmeraldBright,
                                    modifier = Modifier.size(16.dp)
                                )
                            } else {
                                Text(
                                    text = "${idx + 1}.",
                                    fontFamily = JetBrainsMonoFontFamily,
                                    color = if (isCurrent) EmeraldBright else TextMuted,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = ex.name,
                                style = MaterialTheme.typography.labelLarge,
                                color = if (isCurrent) TextPrimary else TextSecondary,
                                maxLines = 1
                            )
                            Text(
                                text = "($doneSets/${ex.sets})",
                                fontFamily = JetBrainsMonoFontFamily,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isAllSetsDone) EmeraldBright else TextMuted
                            )
                        }
                    }
                }
            }
        }

        // Active Exercise Card + Set Check-Off Grid
        item {
            AnimatedContent(
                targetState = currentExercise,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "activeExerciseCardTransition"
            ) { exercise ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, EmeraldPrimary.copy(alpha = 0.45f), RoundedCornerShape(24.dp)),
                    color = OledSurface,
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (exercise.isWarmup) CyanBright.copy(alpha = 0.16f)
                                        else EmeraldPrimary.copy(alpha = 0.18f)
                                    )
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = if (exercise.isWarmup) {
                                        "WARM-UP • EXERCISE ${safeIndex + 1} OF ${exercises.size}"
                                    } else {
                                        "WORKING • EXERCISE ${safeIndex + 1} OF ${exercises.size}"
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (exercise.isWarmup) CyanBright else EmeraldBright,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Text(
                                text = "Rest: ${exercise.restSeconds}s",
                                fontFamily = JetBrainsMonoFontFamily,
                                style = MaterialTheme.typography.labelMedium,
                                color = AmberBright
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = exercise.name,
                            style = MaterialTheme.typography.headlineMedium,
                            color = TextPrimary
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Target: ${exercise.sets} sets × ${exercise.targetRepsOrHold}",
                            fontFamily = JetBrainsMonoFontFamily,
                            style = MaterialTheme.typography.titleMedium,
                            color = EmeraldBright,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "TAP A SET TO CHECK OFF & AUTO-START ${exercise.restSeconds}s REST TIMER",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Large Thumb-Friendly Set Check-off Buttons
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            for (setIdx in 0 until exercise.sets) {
                                val isChecked = completedSetsForCurrent.contains(setIdx)
                                val rowScale by animateFloatAsState(
                                    targetValue = if (isChecked) 1.015f else 1f,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessMedium
                                    ),
                                    label = "setCheckScale_$setIdx"
                                )

                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(58.dp)
                                        .graphicsLayer {
                                            scaleX = rowScale
                                            scaleY = rowScale
                                        }
                                        .clip(RoundedCornerShape(16.dp))
                                        .border(
                                            width = 1.5.dp,
                                            color = if (isChecked) EmeraldBright else OledCardBorder,
                                            shape = RoundedCornerShape(16.dp)
                                        )
                                        .clickable { onToggleSetCompleted(exercise, setIdx) }
                                        .testTag("set_check_button_${exercise.id}_${setIdx + 1}"),
                                    color = if (isChecked) EmeraldContainer else OledSurfaceVariant,
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(horizontal = 16.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(32.dp)
                                                    .clip(CircleShape)
                                                    .background(
                                                        if (isChecked) EmeraldBright else OledSurfaceElevated
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                if (isChecked) {
                                                    Icon(
                                                        imageVector = Icons.Default.Check,
                                                        contentDescription = "Set Completed",
                                                        tint = Color(0xFF032018),
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                } else {
                                                    Icon(
                                                        imageVector = Icons.Default.RadioButtonUnchecked,
                                                        contentDescription = "Set Incomplete",
                                                        tint = TextSecondary,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column {
                                                Text(
                                                    text = "Set ${setIdx + 1} of ${exercise.sets}",
                                                    style = MaterialTheme.typography.titleMedium,
                                                    color = TextPrimary,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Text(
                                                    text = exercise.targetRepsOrHold,
                                                    fontFamily = JetBrainsMonoFontFamily,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = if (isChecked) EmeraldBright else TextSecondary
                                                )
                                            }
                                        }

                                        Text(
                                            text = if (isChecked) "COMPLETED" else "CHECK OFF",
                                            fontFamily = JetBrainsMonoFontFamily,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = if (isChecked) EmeraldBright else AmberBright,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Previous / Next Exercise Stepper Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = onPreviousExercise,
                                enabled = safeIndex > 0,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp)
                                    .testTag("prev_exercise_button"),
                                shape = RoundedCornerShape(14.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, OledCardBorder)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Previous Exercise"
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Previous", fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = onNextExercise,
                                enabled = safeIndex < exercises.size - 1,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp)
                                    .testTag("next_exercise_button"),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = EmeraldPrimary,
                                    contentColor = Color(0xFF042219)
                                )
                            ) {
                                Text("Next Exercise", fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = "Next Exercise"
                                )
                            }
                        }
                    }
                }
            }
        }

        // Animated Circular Progress Ring Timer (Rest & 5s Prep + Isometric Hold)
        item {
            AnimatedTimerRingCard(
                timerState = timerState,
                currentExercise = currentExercise,
                onTogglePauseResume = onToggleTimerPauseResume,
                onAdjustSeconds = onAdjustTimerSeconds,
                onCancelTimer = onCancelTimer,
                onStartHoldTimer = { sideLabel ->
                    onStartHoldTimer(currentExercise, sideLabel)
                },
                onStartManualRest = {
                    onStartManualRest(currentExercise)
                }
            )
        }

        // Live Exercise Form Cues, Mistakes to Avoid, YouTube Search & Embedded WebView Player
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, OledCardBorder, RoundedCornerShape(24.dp)),
                color = OledSurfaceVariant,
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "LIVE FORM GUIDE & YOUTUBE DEMO",
                        style = MaterialTheme.typography.labelMedium,
                        color = CyanBright,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    ExerciseFormAndYoutubeSection(
                        exercise = currentExercise,
                        onSaveYoutubeVideo = { rawUrl ->
                            onSaveYoutubeVideo(currentExercise.id, rawUrl)
                        },
                        onRemoveYoutubeVideo = {
                            onRemoveYoutubeVideo(currentExercise.id)
                        }
                    )
                }
            }
        }

        // Complete & Log Workout Button
        item {
            Button(
                onClick = onFinishWorkoutSession,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("finish_workout_session_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = EmeraldBright,
                    contentColor = Color(0xFF022017)
                )
            ) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = "Finish and Log Workout"
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Complete & Log ${day.shortTitle} Session",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

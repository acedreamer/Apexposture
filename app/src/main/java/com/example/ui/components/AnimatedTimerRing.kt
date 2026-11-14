package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Exercise
import com.example.model.LiveTimerState
import com.example.model.TimerMode
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.AmberBright
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.CyanBright
import com.example.ui.theme.EmeraldBright
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.OledCardBorder
import com.example.ui.theme.OledSurfaceElevated
import com.example.ui.theme.OledSurfaceVariant
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun AnimatedTimerRingCard(
    timerState: LiveTimerState,
    currentExercise: Exercise,
    onTogglePauseResume: () -> Unit,
    onAdjustSeconds: (Int) -> Unit,
    onCancelTimer: () -> Unit,
    onStartHoldTimer: (sideLabel: String?) -> Unit,
    onStartManualRest: () -> Unit,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = if (timerState.mode == TimerMode.IDLE) 1f else timerState.progressFraction,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "timerRingProgress"
    )

    val isUrgentCountdown = timerState.isRunning && timerState.remainingSeconds in 1..3
    val urgentScale by animateFloatAsState(
        targetValue = if (isUrgentCountdown) 1.07f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "urgentCountdownScale"
    )

    val primaryAccentColor = when (timerState.mode) {
        TimerMode.HOLD_PREP -> AmberBright
        TimerMode.HOLD_WORK -> CyanBright
        TimerMode.REST -> EmeraldBright
        TimerMode.IDLE -> if (currentExercise.isTimedHold) CyanAccent else EmeraldPrimary
    }

    val secondaryAccentColor = when (timerState.mode) {
        TimerMode.HOLD_PREP -> AmberAccent
        TimerMode.HOLD_WORK -> EmeraldPrimary
        TimerMode.REST -> AmberAccent
        TimerMode.IDLE -> OledCardBorder
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.5.dp,
                color = if (timerState.mode != TimerMode.IDLE) primaryAccentColor.copy(alpha = 0.6f) else OledCardBorder,
                shape = RoundedCornerShape(24.dp)
            )
            .testTag("live_timer_card"),
        shape = RoundedCornerShape(24.dp),
        color = OledSurfaceVariant
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Mode Header Badge
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(primaryAccentColor.copy(alpha = 0.14f))
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Timer,
                    contentDescription = "Timer Status",
                    tint = primaryAccentColor,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = when (timerState.mode) {
                        TimerMode.HOLD_PREP -> "5-SECOND GET READY PREP"
                        TimerMode.HOLD_WORK -> "ISOMETRIC HOLD TIMER"
                        TimerMode.REST -> "AUTOMATIC REST TIMER"
                        TimerMode.IDLE -> if (currentExercise.isTimedHold) {
                            "ISOMETRIC HOLD READY (${currentExercise.targetRepsOrHold})"
                        } else {
                            "REST TIMER READY (${currentExercise.restSeconds}s)"
                        }
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = primaryAccentColor,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Animated Circular Progress Ring + Center Readout
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .graphicsLayer {
                        scaleX = urgentScale
                        scaleY = urgentScale
                    },
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(188.dp)) {
                    val strokeWidth = 14.dp.toPx()
                    val diameter = size.minDimension - strokeWidth
                    val topLeftOffset = Offset(strokeWidth / 2f, strokeWidth / 2f)
                    val arcSize = Size(diameter, diameter)

                    // Subtle Track Ring
                    drawArc(
                        color = OledSurfaceElevated,
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = topLeftOffset,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )

                    // Active Animated Sweep Ring
                    val sweep = (360f * animatedProgress).coerceIn(0f, 360f)
                    if (sweep > 0f) {
                        drawArc(
                            brush = Brush.sweepGradient(
                                listOf(primaryAccentColor, secondaryAccentColor, primaryAccentColor)
                            ),
                            startAngle = -90f,
                            sweepAngle = sweep,
                            useCenter = false,
                            topLeft = topLeftOffset,
                            size = arcSize,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    val displaySeconds = if (timerState.mode == TimerMode.IDLE) {
                        if (currentExercise.isTimedHold) currentExercise.holdDurationSeconds else currentExercise.restSeconds
                    } else {
                        timerState.remainingSeconds
                    }

                    AnimatedContent(
                        targetState = displaySeconds,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "timerClockDigits"
                    ) { secs ->
                        Text(
                            text = formatMinutesSeconds(secs),
                            fontFamily = JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 40.sp,
                            color = if (isUrgentCountdown) AmberBright else TextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = if (timerState.mode == TimerMode.IDLE) {
                            if (currentExercise.isTimedHold) "5s Prep -> ${currentExercise.holdDurationSeconds}s Hold"
                            else "Auto-starts on set check"
                        } else {
                            timerState.label
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Controls Row
            if (timerState.mode != TimerMode.IDLE) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilledTonalIconButton(
                        onClick = { onAdjustSeconds(-15) },
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("timer_minus_15_button"),
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = OledSurfaceElevated,
                            contentColor = TextPrimary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Remove,
                            contentDescription = "Subtract 15 seconds"
                        )
                    }

                    Button(
                        onClick = onTogglePauseResume,
                        modifier = Modifier
                            .height(50.dp)
                            .testTag("timer_pause_resume_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = primaryAccentColor,
                            contentColor = Color(0xFF04130E)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(
                            imageVector = if (timerState.isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (timerState.isRunning) "Pause Timer" else "Resume Timer"
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (timerState.isRunning) "Pause" else "Resume",
                            fontWeight = FontWeight.Bold
                        )
                    }

                    FilledTonalIconButton(
                        onClick = { onAdjustSeconds(15) },
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("timer_plus_15_button"),
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = OledSurfaceElevated,
                            contentColor = TextPrimary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add 15 seconds"
                        )
                    }

                    FilledTonalIconButton(
                        onClick = onCancelTimer,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("timer_skip_button"),
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = OledSurfaceElevated,
                            contentColor = AmberBright
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Skip or Reset Timer"
                        )
                    }
                }
            } else {
                // Idle Quick-Launch Timer Buttons
                if (currentExercise.isTimedHold) {
                    if (currentExercise.isPerSideHold) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { onStartHoldTimer("Left Side") },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp)
                                    .testTag("start_left_hold_button"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = CyanAccent,
                                    contentColor = Color(0xFF021B24)
                                ),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = "Start Left Hold")
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Left ${currentExercise.holdDurationSeconds}s",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Button(
                                onClick = { onStartHoldTimer("Right Side") },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp)
                                    .testTag("start_right_hold_button"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = CyanAccent,
                                    contentColor = Color(0xFF021B24)
                                ),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = "Start Right Hold")
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Right ${currentExercise.holdDurationSeconds}s",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    } else {
                        Button(
                            onClick = { onStartHoldTimer(null) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("start_isometric_hold_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CyanAccent,
                                contentColor = Color(0xFF021B24)
                            ),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = "Start Isometric Hold")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Start 5s Prep + ${currentExercise.holdDurationSeconds}s Hold Timer",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                OutlinedButton(
                    onClick = onStartManualRest,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("start_manual_rest_button"),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.6f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = "Start Rest Timer",
                        tint = EmeraldBright
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Start ${currentExercise.restSeconds}s Rest Countdown Now",
                        color = EmeraldBright,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

fun formatMinutesSeconds(totalSeconds: Int): String {
    val clamped = totalSeconds.coerceAtLeast(0)
    val mins = clamped / 60
    val secs = clamped % 60
    return "%02d:%02d".format(mins, secs)
}

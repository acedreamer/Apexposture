package com.example.ui.screens

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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.PreloadedWorkoutPlan
import com.example.model.CompletedWorkoutLog
import com.example.model.WorkoutDay
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.AmberBright
import com.example.ui.theme.AmberContainer
import com.example.ui.theme.CyanAccent
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
import java.util.Calendar

@Composable
fun DashboardScreen(
    days: List<WorkoutDay>,
    completedLogs: List<CompletedWorkoutLog>,
    currentStreak: Int,
    bestStreak: Int,
    onStartWorkoutDay: (String) -> Unit,
    onInspectDayInPlanner: (Int) -> Unit,
    onTestAlarm: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sessionsThisWeek = countSessionsInLast7Days(completedLogs)
    val totalSetsCompleted = completedLogs.sumOf { it.completedSets }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Banner Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                EmeraldContainer,
                                OledSurfaceVariant,
                                Color(0xFF10192B)
                            )
                        )
                    )
                    .border(1.dp, EmeraldPrimary.copy(alpha = 0.45f), RoundedCornerShape(24.dp))
                    .padding(20.dp)
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(EmeraldPrimary.copy(alpha = 0.2f))
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "4-DAY BODYWEIGHT RECOMPOSITION",
                                style = MaterialTheme.typography.labelSmall,
                                color = EmeraldBright,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Upper Chest & APT Correction System",
                        style = MaterialTheme.typography.headlineMedium,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Targeted clavicular pec hypertrophy to lift chest structure and reduce pseudogynecomastia appearance, paired with posterior pelvic tilt & deep core alignment.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Streak & Sessions Metrics Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MetricStatCard(
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.LocalFireDepartment,
                                    contentDescription = "Weekly Streak",
                                    tint = AmberBright,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            value = "$currentStreak",
                            label = "Active Streak",
                            subLabel = "Best: $bestStreak",
                            accentColor = AmberAccent,
                            modifier = Modifier.weight(1f)
                        )

                        MetricStatCard(
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.Verified,
                                    contentDescription = "Total Sessions",
                                    tint = EmeraldBright,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            value = "${completedLogs.size}",
                            label = "Total Sessions",
                            subLabel = "$sessionsThisWeek/4 this wk",
                            accentColor = EmeraldPrimary,
                            modifier = Modifier.weight(1f)
                        )

                        MetricStatCard(
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.FitnessCenter,
                                    contentDescription = "Sets Completed",
                                    tint = CyanBright,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            value = "$totalSetsCompleted",
                            label = "Sets Logged",
                            subLabel = "Zero-equipment",
                            accentColor = CyanAccent,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Prominent "Test Alarm" Button required on Home Screen
                    OutlinedButton(
                        onClick = onTestAlarm,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("test_alarm_button"),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, AmberBright.copy(alpha = 0.65f)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = AmberContainer.copy(alpha = 0.35f),
                            contentColor = AmberBright
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = "Test Alarm",
                            tint = AmberBright,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Test Alarm, Haptics & Timer Alert",
                            fontWeight = FontWeight.Bold,
                            color = AmberBright
                        )
                    }
                }
            }
        }

        // Section Header: Quick-Launch 4-Day Split
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "4-DAY TRAINING SPLIT",
                    style = MaterialTheme.typography.labelMedium,
                    color = EmeraldBright,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Tap a day to customize or launch live",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )
            }
        }

        // 4 Day Routine Cards
        itemsIndexed(days, key = { _, d -> d.id }) { index, day ->
            val isUpperBody = day.dayNumber == 1 || day.dayNumber == 3
            val badgeColor = if (isUpperBody) EmeraldBright else CyanBright

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .border(1.dp, OledCardBorder, RoundedCornerShape(20.dp))
                    .clickable { onInspectDayInPlanner(index) }
                    .testTag("dashboard_day_card_${day.dayNumber}"),
                color = OledSurface,
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(badgeColor.copy(alpha = 0.16f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "D${day.dayNumber}",
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    color = badgeColor
                                )
                            }
                            Column {
                                Text(
                                    text = day.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${day.warmupCount} Warm-ups • ${day.workingExerciseCount} Working Exercises • ${day.totalSets} Total Sets",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = day.anatomicalGoal,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { onStartWorkoutDay(day.id) },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("start_day_${day.dayNumber}_workout_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = EmeraldPrimary,
                                contentColor = Color(0xFF042219)
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Start Live Workout",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Start Day ${day.dayNumber} Live",
                                fontWeight = FontWeight.Bold
                            )
                        }

                        OutlinedButton(
                            onClick = { onInspectDayInPlanner(index) },
                            modifier = Modifier
                                .height(48.dp)
                                .testTag("view_day_${day.dayNumber}_plan_button"),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, OledCardBorder)
                        ) {
                            Text(
                                text = "Edit / Cues",
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "View Plan",
                                tint = TextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        // Grounded Motivational & Biomechanical Cues Section
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.SelfImprovement,
                    contentDescription = "Posture & Recomposition Cues",
                    tint = CyanBright,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "POSTURE, CONSISTENCY & RECOMPOSITION PRINCIPLES",
                    style = MaterialTheme.typography.labelMedium,
                    color = CyanBright,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        itemsIndexed(PreloadedWorkoutPlan.groundedMotivationalCues) { idx, (title, body) ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, OledCardBorder, RoundedCornerShape(16.dp))
                    .testTag("motivation_card_$idx"),
                color = OledSurfaceVariant.copy(alpha = 0.7f),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(OledSurfaceElevated),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccessibilityNew,
                            contentDescription = null,
                            tint = EmeraldBright,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleSmall,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = body,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricStatCard(
    icon: @Composable () -> Unit,
    value: String,
    label: String,
    subLabel: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .border(1.dp, accentColor.copy(alpha = 0.35f), RoundedCornerShape(16.dp)),
        color = OledSurface.copy(alpha = 0.85f),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                icon()
                Text(
                    text = value,
                    fontFamily = JetBrainsMonoFontFamily,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = subLabel,
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted
            )
        }
    }
}

private fun countSessionsInLast7Days(logs: List<CompletedWorkoutLog>): Int {
    val cutoff = Calendar.getInstance().apply {
        add(Calendar.DAY_OF_YEAR, -7)
    }.timeInMillis
    return logs.count { it.completedAtMillis >= cutoff }
}

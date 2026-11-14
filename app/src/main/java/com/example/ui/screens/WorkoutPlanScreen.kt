package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.model.Exercise
import com.example.model.WorkoutDay
import com.example.ui.components.ExerciseFormAndYoutubeSection
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.AmberBright
import com.example.ui.theme.CoralWarning
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.CyanBright
import com.example.ui.theme.EmeraldBright
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.OledBackground
import com.example.ui.theme.OledCardBorder
import com.example.ui.theme.OledSurface
import com.example.ui.theme.OledSurfaceElevated
import com.example.ui.theme.OledSurfaceVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.util.UUID

@Composable
fun WorkoutPlanScreen(
    days: List<WorkoutDay>,
    selectedDayIndex: Int,
    onSelectDayIndex: (Int) -> Unit,
    onStartLiveWorkout: (String) -> Unit,
    onSaveYoutubeVideo: (exerciseId: String, rawUrl: String) -> Boolean,
    onRemoveYoutubeVideo: (exerciseId: String) -> Unit,
    onUpdateExercise: (dayId: String, Exercise) -> Unit,
    onAddExercise: (dayId: String, Exercise) -> Unit,
    onDeleteExercise: (dayId: String, exerciseId: String) -> Unit,
    onMoveExercise: (dayId: String, fromIndex: Int, toIndex: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    if (days.isEmpty()) return
    val safeDayIndex = selectedDayIndex.coerceIn(0, days.size - 1)
    val selectedDay = days[safeDayIndex]

    // Track expanded exercise IDs (default to expanding the first 2 exercises so cues & YouTube embedder are immediately visible)
    var expandedIds by remember(selectedDay.id) {
        mutableStateOf(
            selectedDay.exercises.take(2).map { it.id }.toSet()
        )
    }

    var editingExercise by remember { mutableStateOf<Exercise?>(null) }
    var showAddExerciseDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("workout_plan_screen")
    ) {
        // Day 1 - Day 4 Selector Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(OledSurface)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            days.forEachIndexed { index, day ->
                val isSelected = index == safeDayIndex
                FilterChip(
                    selected = isSelected,
                    onClick = { onSelectDayIndex(index) },
                    label = {
                        Text(
                            text = "Day ${day.dayNumber}",
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("plan_day_tab_${day.dayNumber}"),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = EmeraldPrimary,
                        selectedLabelColor = Color(0xFF042219),
                        containerColor = OledSurfaceVariant,
                        labelColor = TextSecondary
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = OledCardBorder,
                        selectedBorderColor = EmeraldBright
                    )
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Day Header & Start Workout Callout
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, EmeraldPrimary.copy(alpha = 0.4f), RoundedCornerShape(20.dp)),
                    color = OledSurfaceVariant,
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = selectedDay.focusBadge.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = EmeraldBright,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = selectedDay.title,
                            style = MaterialTheme.typography.titleLarge,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = selectedDay.anatomicalGoal,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { onStartLiveWorkout(selectedDay.id) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("plan_start_live_workout_button"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = EmeraldPrimary,
                                    contentColor = Color(0xFF042219)
                                )
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = "Start Live Session")
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Start Day ${selectedDay.dayNumber} Live Session",
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            OutlinedButton(
                                onClick = { showAddExerciseDialog = true },
                                modifier = Modifier
                                    .height(48.dp)
                                    .testTag("add_exercise_button"),
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, CyanBright.copy(alpha = 0.6f))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Add Exercise",
                                    tint = CyanBright
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Add", color = CyanBright, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Exercise Cards
            itemsIndexed(selectedDay.exercises, key = { _, ex -> ex.id }) { idx, exercise ->
                val isExpanded = expandedIds.contains(exercise.id)
                ExercisePlanCard(
                    exercise = exercise,
                    index = idx,
                    totalCount = selectedDay.exercises.size,
                    isExpanded = isExpanded,
                    onToggleExpand = {
                        expandedIds = if (isExpanded) {
                            expandedIds - exercise.id
                        } else {
                            expandedIds + exercise.id
                        }
                    },
                    onMoveUp = {
                        if (idx > 0) onMoveExercise(selectedDay.id, idx, idx - 1)
                    },
                    onMoveDown = {
                        if (idx < selectedDay.exercises.size - 1) onMoveExercise(selectedDay.id, idx, idx + 1)
                    },
                    onEdit = { editingExercise = exercise },
                    onDelete = { onDeleteExercise(selectedDay.id, exercise.id) },
                    onSaveYoutubeVideo = { url -> onSaveYoutubeVideo(exercise.id, url) },
                    onRemoveYoutubeVideo = { onRemoveYoutubeVideo(exercise.id) }
                )
            }
        }
    }

    if (editingExercise != null) {
        ExerciseEditorDialog(
            initialExercise = editingExercise,
            onDismiss = { editingExercise = null },
            onConfirm = { updated ->
                onUpdateExercise(selectedDay.id, updated)
                editingExercise = null
            }
        )
    }

    if (showAddExerciseDialog) {
        ExerciseEditorDialog(
            initialExercise = null,
            onDismiss = { showAddExerciseDialog = false },
            onConfirm = { created ->
                onAddExercise(selectedDay.id, created)
                showAddExerciseDialog = false
            }
        )
    }
}

@Composable
private fun ExercisePlanCard(
    exercise: Exercise,
    index: Int,
    totalCount: Int,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onSaveYoutubeVideo: (String) -> Boolean,
    onRemoveYoutubeVideo: () -> Unit
) {
    val badgeColor = if (exercise.isWarmup) CyanBright else EmeraldBright

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = if (isExpanded) badgeColor.copy(alpha = 0.5f) else OledCardBorder,
                shape = RoundedCornerShape(20.dp)
            )
            .testTag("plan_exercise_card_${exercise.id}"),
        color = OledSurface,
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleExpand() },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(badgeColor.copy(alpha = 0.16f))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = if (exercise.isWarmup) "WARM-UP" else "WORKING SET",
                                style = MaterialTheme.typography.labelSmall,
                                color = badgeColor,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (exercise.isTimedHold) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(AmberAccent.copy(alpha = 0.16f))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "ISOMETRIC HOLD (${exercise.holdDurationSeconds}s)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = AmberBright,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        if (!exercise.savedYoutubeId.isNullOrBlank()) {
                            Icon(
                                imageVector = Icons.Default.PlayCircleFilled,
                                contentDescription = "YouTube Video Saved",
                                tint = EmeraldBright,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "${index + 1}. ${exercise.name}",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Sets x Target & Rest Pills
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "${exercise.sets} sets × ${exercise.targetRepsOrHold}",
                            fontFamily = JetBrainsMonoFontFamily,
                            style = MaterialTheme.typography.labelMedium,
                            color = EmeraldBright,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "•",
                            color = TextMuted
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = "Rest time",
                                tint = AmberBright,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Rest: ${exercise.restSeconds}s",
                                fontFamily = JetBrainsMonoFontFamily,
                                style = MaterialTheme.typography.labelMedium,
                                color = AmberBright
                            )
                        }
                    }
                }

                IconButton(
                    onClick = onToggleExpand,
                    modifier = Modifier.testTag("toggle_expand_${exercise.id}")
                ) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (isExpanded) "Collapse Exercise Details" else "Expand Exercise Details",
                        tint = TextSecondary
                    )
                }
            }

            // Reorder & Edit Toolbar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onToggleExpand,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isExpanded) "Hide Form Cues & YouTube Player" else "Show Form Cues & YouTube Embedder",
                        style = MaterialTheme.typography.labelSmall,
                        color = CyanBright
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onMoveUp,
                        enabled = index > 0,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowUpward,
                            contentDescription = "Move Up",
                            tint = if (index > 0) TextSecondary else TextMuted.copy(alpha = 0.3f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = onMoveDown,
                        enabled = index < totalCount - 1,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowDownward,
                            contentDescription = "Move Down",
                            tint = if (index < totalCount - 1) TextSecondary else TextMuted.copy(alpha = 0.3f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("edit_exercise_${exercise.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Exercise",
                            tint = EmeraldBright,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("delete_exercise_${exercise.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Delete Exercise",
                            tint = CoralWarning,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Expandable Form Cues + Search YouTube + Embed YouTube WebView Section
            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    ExerciseFormAndYoutubeSection(
                        exercise = exercise,
                        onSaveYoutubeVideo = onSaveYoutubeVideo,
                        onRemoveYoutubeVideo = onRemoveYoutubeVideo
                    )
                }
            }
        }
    }
}

@Composable
private fun ExerciseEditorDialog(
    initialExercise: Exercise?,
    onDismiss: () -> Unit,
    onConfirm: (Exercise) -> Unit
) {
    var name by rememberSaveable { mutableStateOf(initialExercise?.name ?: "") }
    var isWarmup by rememberSaveable { mutableStateOf(initialExercise?.isWarmup ?: false) }
    var setsText by rememberSaveable { mutableStateOf((initialExercise?.sets ?: 3).toString()) }
    var repsOrHold by rememberSaveable { mutableStateOf(initialExercise?.targetRepsOrHold ?: "10–12 reps") }
    var restSecondsText by rememberSaveable { mutableStateOf((initialExercise?.restSeconds ?: 60).toString()) }
    var isTimedHold by rememberSaveable { mutableStateOf(initialExercise?.isTimedHold ?: false) }
    var holdSecondsText by rememberSaveable { mutableStateOf((initialExercise?.holdDurationSeconds ?: 30).toString()) }
    var isPerSideHold by rememberSaveable { mutableStateOf(initialExercise?.isPerSideHold ?: false) }
    var searchQuery by rememberSaveable { mutableStateOf(initialExercise?.searchQuery ?: "") }
    var formCues by rememberSaveable { mutableStateOf(initialExercise?.formCues ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = OledSurfaceVariant,
        titleContentColor = TextPrimary,
        textContentColor = TextSecondary,
        title = {
            Text(
                text = if (initialExercise == null) "Add Custom Exercise" else "Edit Exercise",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Exercise Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = isWarmup,
                        onCheckedChange = { isWarmup = it },
                        colors = CheckboxDefaults.colors(checkedColor = CyanBright)
                    )
                    Text("Warm-up / Mobility Exercise", color = TextPrimary)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = setsText,
                        onValueChange = { setsText = it.filter { c -> c.isDigit() } },
                        label = { Text("Sets") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = repsOrHold,
                        onValueChange = { repsOrHold = it },
                        label = { Text("Reps / Hold") },
                        singleLine = true,
                        modifier = Modifier.weight(1.6f)
                    )
                    OutlinedTextField(
                        value = restSecondsText,
                        onValueChange = { restSecondsText = it.filter { c -> c.isDigit() } },
                        label = { Text("Rest (s)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = isTimedHold,
                        onCheckedChange = { isTimedHold = it },
                        colors = CheckboxDefaults.colors(checkedColor = AmberBright)
                    )
                    Text("Timed Isometric Hold (5s Prep + Timer)", color = TextPrimary)
                }

                if (isTimedHold) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = holdSecondsText,
                            onValueChange = { holdSecondsText = it.filter { c -> c.isDigit() } },
                            label = { Text("Hold Duration (s)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Checkbox(
                                checked = isPerSideHold,
                                onCheckedChange = { isPerSideHold = it }
                            )
                            Text("Per Side", color = TextPrimary)
                        }
                    }
                }

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("YouTube Search Query") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = formCues,
                    onValueChange = { formCues = it },
                    label = { Text("Anatomical Form Cues & Mistakes to Avoid") },
                    minLines = 3,
                    maxLines = 5,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val cleanName = name.trim().ifEmpty { "Custom Bodyweight Exercise" }
                    val sets = setsText.toIntOrNull()?.coerceIn(1, 20) ?: 3
                    val rest = restSecondsText.toIntOrNull()?.coerceIn(5, 600) ?: 60
                    val holdSecs = if (isTimedHold) (holdSecondsText.toIntOrNull()?.coerceIn(5, 600) ?: 30) else 0
                    val query = searchQuery.trim().ifEmpty { "$cleanName proper form tutorial" }
                    val cues = formCues.trim().ifEmpty {
                        "Maintain posterior pelvic tilt and ribs stacked down. Avoid arching lower back."
                    }

                    onConfirm(
                        Exercise(
                            id = initialExercise?.id ?: "custom_${UUID.randomUUID()}",
                            name = cleanName,
                            isWarmup = isWarmup,
                            sets = sets,
                            targetRepsOrHold = repsOrHold.trim().ifEmpty { "10 reps" },
                            isTimedHold = isTimedHold,
                            holdDurationSeconds = holdSecs,
                            isPerSideHold = isTimedHold && isPerSideHold,
                            restSeconds = rest,
                            searchQuery = query,
                            formCues = cues,
                            savedYoutubeId = initialExercise?.savedYoutubeId
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = EmeraldPrimary,
                    contentColor = Color(0xFF042219)
                )
            ) {
                Text("Save Exercise", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}

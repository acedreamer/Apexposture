package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.WorkoutRepository
import com.example.model.CompletedWorkoutLog
import com.example.model.Exercise
import com.example.model.LiveTimerState
import com.example.model.TimerMode
import com.example.model.WorkoutDay
import com.example.model.YoutubeUrlParser
import com.example.util.HardwareAlarmHelper
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class WorkoutViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = WorkoutRepository(application.applicationContext)
    private val alarmHelper = HardwareAlarmHelper(application.applicationContext)

    private val _days = MutableStateFlow<List<WorkoutDay>>(emptyList())
    val days: StateFlow<List<WorkoutDay>> = _days.asStateFlow()

    private val _completedLogs = MutableStateFlow<List<CompletedWorkoutLog>>(emptyList())
    val completedLogs: StateFlow<List<CompletedWorkoutLog>> = _completedLogs.asStateFlow()

    private val _currentStreak = MutableStateFlow(0)
    val currentStreak: StateFlow<Int> = _currentStreak.asStateFlow()

    private val _bestStreak = MutableStateFlow(0)
    val bestStreak: StateFlow<Int> = _bestStreak.asStateFlow()

    private val _selectedPlanDayIndex = MutableStateFlow(0)
    val selectedPlanDayIndex: StateFlow<Int> = _selectedPlanDayIndex.asStateFlow()

    // Live Workout State
    private val _activeWorkoutDayId = MutableStateFlow<String?>(null)
    val activeWorkoutDayId: StateFlow<String?> = _activeWorkoutDayId.asStateFlow()

    private val _activeExerciseIndex = MutableStateFlow(0)
    val activeExerciseIndex: StateFlow<Int> = _activeExerciseIndex.asStateFlow()

    // Maps exerciseId -> Set of completed 0-based set indices
    private val _completedSetsMap = MutableStateFlow<Map<String, Set<Int>>>(emptyMap())
    val completedSetsMap: StateFlow<Map<String, Set<Int>>> = _completedSetsMap.asStateFlow()

    private val _sessionElapsedSeconds = MutableStateFlow(0)
    val sessionElapsedSeconds: StateFlow<Int> = _sessionElapsedSeconds.asStateFlow()

    private val _timerState = MutableStateFlow(LiveTimerState())
    val timerState: StateFlow<LiveTimerState> = _timerState.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    private var countdownJob: Job? = null
    private var sessionClockJob: Job? = null

    init {
        val loadedDays = repository.loadWorkoutDays()
        val loadedLogs = repository.loadCompletedLogs()
        val (streak, best) = repository.recalculateStreak(loadedLogs)
        _days.value = loadedDays
        _completedLogs.value = loadedLogs
        _currentStreak.value = streak
        _bestStreak.value = best
        if (loadedDays.isNotEmpty()) {
            _activeWorkoutDayId.value = loadedDays.first().id
        }
    }

    fun selectPlanDayIndex(index: Int) {
        _selectedPlanDayIndex.value = index.coerceIn(0, (_days.value.size - 1).coerceAtLeast(0))
    }

    fun clearStatusMessage() {
        _statusMessage.value = null
    }

    fun postStatusMessage(msg: String) {
        _statusMessage.value = msg
    }

    fun triggerTestAlarm() {
        val message = alarmHelper.testAlarmAndNotification()
        _statusMessage.value = message
    }

    // --- Live Workout Session Management ---

    fun startOrSwitchLiveWorkout(dayId: String) {
        val isNewDay = _activeWorkoutDayId.value != dayId
        _activeWorkoutDayId.value = dayId
        if (isNewDay) {
            _activeExerciseIndex.value = 0
            _completedSetsMap.value = emptyMap()
            _sessionElapsedSeconds.value = 0
            cancelTimer()
        }
        ensureSessionClockRunning()
    }

    private fun ensureSessionClockRunning() {
        if (sessionClockJob?.isActive == true) return
        sessionClockJob = viewModelScope.launch {
            while (true) {
                delay(1000L)
                _sessionElapsedSeconds.value += 1
            }
        }
    }

    fun selectActiveExerciseIndex(index: Int) {
        val day = getActiveWorkoutDay() ?: return
        if (day.exercises.isEmpty()) return
        _activeExerciseIndex.value = index.coerceIn(0, day.exercises.size - 1)
    }

    fun nextActiveExercise() {
        val day = getActiveWorkoutDay() ?: return
        if (_activeExerciseIndex.value < day.exercises.size - 1) {
            _activeExerciseIndex.value += 1
        }
    }

    fun previousActiveExercise() {
        if (_activeExerciseIndex.value > 0) {
            _activeExerciseIndex.value -= 1
        }
    }

    fun getActiveWorkoutDay(): WorkoutDay? {
        val id = _activeWorkoutDayId.value ?: _days.value.firstOrNull()?.id ?: return null
        return _days.value.find { it.id == id } ?: _days.value.firstOrNull()
    }

    /**
     * Toggles a set check-off. Checking off an uncompleted set automatically starts the
     * exercise's rest countdown timer.
     */
    fun toggleSetCompleted(exercise: Exercise, setIndex: Int) {
        ensureSessionClockRunning()
        val currentMap = _completedSetsMap.value.toMutableMap()
        val currentSet = (currentMap[exercise.id] ?: emptySet()).toMutableSet()
        val isNowCompleted = if (currentSet.contains(setIndex)) {
            currentSet.remove(setIndex)
            false
        } else {
            currentSet.add(setIndex)
            true
        }
        currentMap[exercise.id] = currentSet
        _completedSetsMap.value = currentMap

        if (isNowCompleted) {
            startRestTimer(
                restSeconds = exercise.restSeconds,
                exerciseName = exercise.name,
                setNumber = setIndex + 1,
                totalSets = exercise.sets
            )
        }
    }

    fun startRestTimer(
        restSeconds: Int,
        exerciseName: String,
        setNumber: Int,
        totalSets: Int
    ) {
        countdownJob?.cancel()
        val duration = restSeconds.coerceAtLeast(5)
        _timerState.value = LiveTimerState(
            mode = TimerMode.REST,
            remainingSeconds = duration,
            totalSeconds = duration,
            isRunning = true,
            label = "Rest • Set $setNumber/$totalSets Complete",
            exerciseName = exerciseName
        )
        runTimerLoop()
    }

    /**
     * Starts an Isometric Hold Timer with a mandatory 5-second "Get Ready" prep countdown
     * before automatically starting the work hold countdown.
     */
    fun startIsometricHoldTimer(exercise: Exercise, sideLabel: String? = null) {
        ensureSessionClockRunning()
        countdownJob?.cancel()
        val workDuration = if (exercise.holdDurationSeconds > 0) {
            exercise.holdDurationSeconds
        } else {
            30
        }
        val sideSuffix = if (!sideLabel.isNullOrBlank()) " ($sideLabel)" else ""
        _timerState.value = LiveTimerState(
            mode = TimerMode.HOLD_PREP,
            remainingSeconds = 5,
            totalSeconds = 5,
            isRunning = true,
            label = "Get Ready • 5s Prep$sideSuffix",
            exerciseName = exercise.name,
            sideLabel = sideLabel
        )

        countdownJob = viewModelScope.launch {
            // Phase 1: 5-second "Get Ready" Prep Countdown
            while (_timerState.value.remainingSeconds > 0 && _timerState.value.mode == TimerMode.HOLD_PREP) {
                if (_timerState.value.isRunning) {
                    delay(1000L)
                    val next = _timerState.value.remainingSeconds - 1
                    if (next in 1..3) {
                        alarmHelper.playCountdownTick()
                    }
                    _timerState.value = _timerState.value.copy(remainingSeconds = next)
                } else {
                    delay(200L)
                }
            }

            if (_timerState.value.mode != TimerMode.HOLD_PREP) return@launch

            // Transition to Active Isometric Hold Work Timer
            alarmHelper.playHoldStartCue()
            _timerState.value = LiveTimerState(
                mode = TimerMode.HOLD_WORK,
                remainingSeconds = workDuration,
                totalSeconds = workDuration,
                isRunning = true,
                label = "Isometric Hold Active$sideSuffix",
                exerciseName = exercise.name,
                sideLabel = sideLabel
            )

            while (_timerState.value.remainingSeconds > 0 && _timerState.value.mode == TimerMode.HOLD_WORK) {
                if (_timerState.value.isRunning) {
                    delay(1000L)
                    val next = _timerState.value.remainingSeconds - 1
                    if (next in 1..3) {
                        alarmHelper.playCountdownTick()
                    }
                    _timerState.value = _timerState.value.copy(remainingSeconds = next)
                } else {
                    delay(200L)
                }
            }

            if (_timerState.value.mode == TimerMode.HOLD_WORK && _timerState.value.remainingSeconds == 0) {
                alarmHelper.triggerTimerCompletionAlert(
                    title = "Hold Complete! (${exercise.name})",
                    message = "${workDuration}s isometric hold finished$sideSuffix. Great posture tension!"
                )
                _timerState.value = _timerState.value.copy(
                    isRunning = false,
                    label = "Hold Finished! Check off your set or start Rest."
                )
            }
        }
    }

    private fun runTimerLoop() {
        countdownJob = viewModelScope.launch {
            while (_timerState.value.remainingSeconds > 0 && _timerState.value.mode == TimerMode.REST) {
                if (_timerState.value.isRunning) {
                    delay(1000L)
                    val next = _timerState.value.remainingSeconds - 1
                    if (next in 1..3) {
                        alarmHelper.playCountdownTick()
                    }
                    _timerState.value = _timerState.value.copy(remainingSeconds = next)
                } else {
                    delay(200L)
                }
            }
            if (_timerState.value.mode == TimerMode.REST && _timerState.value.remainingSeconds == 0) {
                val exName = _timerState.value.exerciseName
                alarmHelper.triggerTimerCompletionAlert(
                    title = "Rest Complete — Next Set Ready",
                    message = "Time to begin your next set of $exName."
                )
                _timerState.value = _timerState.value.copy(
                    isRunning = false,
                    label = "Rest Complete! Ready for next set"
                )
            }
        }
    }

    fun toggleTimerPauseResume() {
        val current = _timerState.value
        if (current.mode == TimerMode.IDLE || current.remainingSeconds <= 0) return
        _timerState.value = current.copy(isRunning = !current.isRunning)
    }

    fun adjustTimerSeconds(deltaSeconds: Int) {
        val current = _timerState.value
        if (current.mode == TimerMode.IDLE) return
        val updatedRemaining = (current.remainingSeconds + deltaSeconds).coerceAtLeast(1)
        val updatedTotal = maxOf(current.totalSeconds, updatedRemaining)
        _timerState.value = current.copy(
            remainingSeconds = updatedRemaining,
            totalSeconds = updatedTotal
        )
    }

    fun cancelTimer() {
        countdownJob?.cancel()
        _timerState.value = LiveTimerState()
    }

    fun completeActiveWorkoutSession(): CompletedWorkoutLog? {
        val day = getActiveWorkoutDay() ?: return null
        val completedCount = day.exercises.sumOf { ex ->
            (_completedSetsMap.value[ex.id]?.size ?: 0)
        }.coerceAtLeast(1)
        val totalSets = day.totalSets.coerceAtLeast(1)
        val duration = _sessionElapsedSeconds.value.coerceAtLeast(60)

        val newLog = CompletedWorkoutLog(
            id = UUID.randomUUID().toString(),
            dayId = day.id,
            dayNumber = day.dayNumber,
            dayTitle = day.title,
            completedAtMillis = System.currentTimeMillis(),
            durationSeconds = duration,
            completedSets = completedCount,
            totalSets = totalSets
        )

        val updatedLogs = listOf(newLog) + _completedLogs.value
        _completedLogs.value = updatedLogs
        repository.saveCompletedLogs(updatedLogs)

        val (streak, best) = repository.recalculateStreak(updatedLogs)
        _currentStreak.value = streak
        _bestStreak.value = best

        // Reset session state
        sessionClockJob?.cancel()
        cancelTimer()
        _completedSetsMap.value = emptyMap()
        _sessionElapsedSeconds.value = 0
        _activeExerciseIndex.value = 0

        alarmHelper.triggerTimerCompletionAlert(
            title = "Workout Logged! (${day.shortTitle})",
            message = "Completed $completedCount/$totalSets sets. Active streak: $streak!"
        )
        _statusMessage.value = "Session logged: ${day.shortTitle} ($completedCount/$totalSets sets)!"
        return newLog
    }

    // --- YouTube Video Embedder & Persistence ---

    fun saveYoutubeVideoForExercise(exerciseId: String, rawInput: String): Boolean {
        val videoId = YoutubeUrlParser.extractVideoId(rawInput)
        if (videoId == null) {
            _statusMessage.value = "Invalid YouTube link or ID. Paste a youtube.com/watch?v=, youtu.be/, or youtube.com/shorts/ link."
            return false
        }

        val updatedDays = _days.value.map { day ->
            day.copy(
                exercises = day.exercises.map { ex ->
                    if (ex.id == exerciseId) {
                        ex.copy(savedYoutubeId = videoId)
                    } else {
                        ex
                    }
                }
            )
        }
        _days.value = updatedDays
        repository.saveWorkoutDays(updatedDays)
        _statusMessage.value = "Saved YouTube demo (ID: $videoId)"
        return true
    }

    fun removeYoutubeVideoForExercise(exerciseId: String) {
        val updatedDays = _days.value.map { day ->
            day.copy(
                exercises = day.exercises.map { ex ->
                    if (ex.id == exerciseId) {
                        ex.copy(savedYoutubeId = null)
                    } else {
                        ex
                    }
                }
            )
        }
        _days.value = updatedDays
        repository.saveWorkoutDays(updatedDays)
        _statusMessage.value = "Removed embedded YouTube video."
    }

    // --- Routine Customization (Edit, Add, Reorder, Delete, Reset) ---

    fun updateExercise(dayId: String, updatedExercise: Exercise) {
        val updatedDays = _days.value.map { day ->
            if (day.id == dayId) {
                day.copy(
                    exercises = day.exercises.map { ex ->
                        if (ex.id == updatedExercise.id) updatedExercise else ex
                    }
                )
            } else day
        }
        _days.value = updatedDays
        repository.saveWorkoutDays(updatedDays)
        _statusMessage.value = "Updated ${updatedExercise.name}"
    }

    fun addExerciseToDay(dayId: String, newExercise: Exercise) {
        val updatedDays = _days.value.map { day ->
            if (day.id == dayId) {
                day.copy(exercises = day.exercises + newExercise)
            } else day
        }
        _days.value = updatedDays
        repository.saveWorkoutDays(updatedDays)
        _statusMessage.value = "Added ${newExercise.name} to routine"
    }

    fun deleteExerciseFromDay(dayId: String, exerciseId: String) {
        val updatedDays = _days.value.map { day ->
            if (day.id == dayId) {
                day.copy(exercises = day.exercises.filterNot { it.id == exerciseId })
            } else day
        }
        _days.value = updatedDays
        repository.saveWorkoutDays(updatedDays)
        _statusMessage.value = "Exercise removed from routine"
    }

    fun moveExercise(dayId: String, fromIndex: Int, toIndex: Int) {
        val updatedDays = _days.value.map { day ->
            if (day.id == dayId) {
                val list = day.exercises.toMutableList()
                if (fromIndex in list.indices && toIndex in list.indices) {
                    val item = list.removeAt(fromIndex)
                    list.add(toIndex, item)
                }
                day.copy(exercises = list)
            } else day
        }
        _days.value = updatedDays
        repository.saveWorkoutDays(updatedDays)
    }

    fun resetRoutineToDefaults() {
        val defaults = repository.resetToDefaultPlan()
        _days.value = defaults
        _statusMessage.value = "Restored default 4-Day Upper Chest & APT Plan (saved YouTube videos preserved)."
    }

    // --- JSON Backup Export / Import ---

    fun createBackupJson(): String {
        return repository.exportBackupJsonString(
            days = _days.value,
            logs = _completedLogs.value,
            currentStreak = _currentStreak.value,
            bestStreak = _bestStreak.value
        )
    }

    fun restoreFromBackupJson(jsonString: String): Boolean {
        val result = repository.importBackupJsonString(jsonString)
        return result.fold(
            onSuccess = { data ->
                _days.value = data.days
                _completedLogs.value = data.logs
                _currentStreak.value = data.currentStreak
                _bestStreak.value = data.bestStreak
                _statusMessage.value = "Backup imported! Loaded ${data.days.size} routines & ${data.logs.size} workout logs."
                true
            },
            onFailure = { err ->
                _statusMessage.value = "Backup import failed: ${err.localizedMessage ?: "Invalid JSON format"}"
                false
            }
        )
    }
}

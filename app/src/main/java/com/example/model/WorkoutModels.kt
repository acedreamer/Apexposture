package com.example.model

import java.net.URI

data class Exercise(
    val id: String,
    val name: String,
    val isWarmup: Boolean,
    val sets: Int,
    val targetRepsOrHold: String,
    val isTimedHold: Boolean,
    val holdDurationSeconds: Int,
    val isPerSideHold: Boolean,
    val restSeconds: Int,
    val searchQuery: String,
    val formCues: String,
    val savedYoutubeId: String? = null
) {
    /**
     * Splits the anatomical form cues into primary execution cues and "Avoid..." mistake warnings
     * while preserving the exact text.
     */
    val executionCueText: String
        get() {
            val avoidIndex = formCues.indexOf("Avoid ", ignoreCase = false)
            return if (avoidIndex > 0) {
                formCues.substring(0, avoidIndex).trim()
            } else {
                formCues
            }
        }

    val mistakeToAvoidText: String?
        get() {
            val avoidIndex = formCues.indexOf("Avoid ", ignoreCase = false)
            return if (avoidIndex >= 0) {
                formCues.substring(avoidIndex).trim()
            } else {
                null
            }
        }
}

data class WorkoutDay(
    val id: String,
    val dayNumber: Int,
    val title: String,
    val shortTitle: String,
    val focusBadge: String,
    val anatomicalGoal: String,
    val exercises: List<Exercise>
) {
    val totalSets: Int
        get() = exercises.sumOf { it.sets }

    val warmupCount: Int
        get() = exercises.count { it.isWarmup }

    val workingExerciseCount: Int
        get() = exercises.count { !it.isWarmup }
}

data class CompletedWorkoutLog(
    val id: String,
    val dayId: String,
    val dayNumber: Int,
    val dayTitle: String,
    val completedAtMillis: Long,
    val durationSeconds: Int,
    val completedSets: Int,
    val totalSets: Int,
    val postureRating: Int = 5
)

enum class TimerMode {
    IDLE,
    HOLD_PREP,   // 5-second "Get Ready" prep countdown before an isometric hold
    HOLD_WORK,   // Active isometric hold countdown (e.g., 30s, 35s, 45s, 60s)
    REST         // Automatic rest countdown after checking off a set
}

data class LiveTimerState(
    val mode: TimerMode = TimerMode.IDLE,
    val remainingSeconds: Int = 0,
    val totalSeconds: Int = 0,
    val isRunning: Boolean = false,
    val label: String = "",
    val exerciseName: String = "",
    val sideLabel: String? = null
) {
    val progressFraction: Float
        get() = if (totalSeconds > 0) {
            (remainingSeconds.toFloat() / totalSeconds.toFloat()).coerceIn(0f, 1f)
        } else 0f
}

data class BackupData(
    val days: List<WorkoutDay>,
    val logs: List<CompletedWorkoutLog>,
    val currentStreak: Int,
    val bestStreak: Int,
    val exportedAtMillis: Long
)

object YoutubeUrlParser {
    private val validIdRegex = Regex("^[a-zA-Z0-9_-]{11}$")

    /**
     * Extracts the 11-character YouTube VIDEO_ID from:
     * - https://www.youtube.com/watch?v=VIDEO_ID
     * - https://m.youtube.com/watch?v=VIDEO_ID
     * - https://youtu.be/VIDEO_ID
     * - https://www.youtube.com/shorts/VIDEO_ID
     * - https://www.youtube.com/embed/VIDEO_ID
     * - Raw 11-character VIDEO_ID
     */
    fun extractVideoId(rawInput: String): String? {
        val trimmed = rawInput.trim()
        if (trimmed.isEmpty()) return null

        if (validIdRegex.matches(trimmed)) {
            return trimmed
        }

        // Regex extraction for common YouTube URL patterns
        val patterns = listOf(
            Regex("(?:youtube(?:-nocookie)?\\.com/(?:[^/]+/.+/|(?:v|e(?:mbed)?|shorts|live)/|.*[?&]v=)|youtu\\.be/)([a-zA-Z0-9_-]{11})"),
            Regex("[?&]v=([a-zA-Z0-9_-]{11})")
        )

        for (pattern in patterns) {
            val match = pattern.find(trimmed)
            if (match != null && match.groupValues.size > 1) {
                val candidate = match.groupValues[1]
                if (validIdRegex.matches(candidate)) {
                    return candidate
                }
            }
        }

        return try {
            val uri = URI(trimmed)
            val host = uri.host?.lowercase() ?: ""
            when {
                host.contains("youtu.be") -> {
                    val pathSegment = uri.path?.trim('/')?.split('/')?.firstOrNull()
                    pathSegment?.takeIf { validIdRegex.matches(it) }
                }
                host.contains("youtube.com") || host.contains("youtube-nocookie.com") -> {
                    val query = uri.query ?: ""
                    val vParam = query.split('&')
                        .map { it.split('=') }
                        .firstOrNull { it.firstOrNull() == "v" }
                        ?.getOrNull(1)
                    if (vParam != null && validIdRegex.matches(vParam)) {
                        vParam
                    } else {
                        val segments = uri.path?.trim('/')?.split('/') ?: emptyList()
                        val idx = segments.indexOfFirst { it in listOf("shorts", "embed", "v", "live") }
                        if (idx >= 0 && idx + 1 < segments.size) {
                            segments[idx + 1].takeIf { validIdRegex.matches(it) }
                        } else null
                    }
                }
                else -> null
            }
        } catch (_: Exception) {
            null
        }
    }
}

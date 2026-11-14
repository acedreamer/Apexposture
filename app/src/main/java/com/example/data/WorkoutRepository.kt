package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.BackupData
import com.example.model.CompletedWorkoutLog
import com.example.model.Exercise
import com.example.model.WorkoutDay
import org.json.JSONArray
import org.json.JSONObject
import java.util.Calendar

class WorkoutRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun loadWorkoutDays(): List<WorkoutDay> {
        val rawJson = prefs.getString(KEY_WORKOUT_DAYS, null)
        val savedYoutubeMap = loadSavedYoutubeIdMap()
        if (rawJson.isNullOrBlank()) {
            val defaults = PreloadedWorkoutPlan.getDefaultDays().map { day ->
                day.copy(
                    exercises = day.exercises.map { ex ->
                        ex.copy(savedYoutubeId = savedYoutubeMap[ex.id] ?: ex.savedYoutubeId)
                    }
                )
            }
            saveWorkoutDays(defaults)
            return defaults
        }

        return try {
            val daysArray = JSONArray(rawJson)
            parseDaysJsonArray(daysArray, savedYoutubeMap)
        } catch (_: Exception) {
            PreloadedWorkoutPlan.getDefaultDays()
        }
    }

    fun saveWorkoutDays(days: List<WorkoutDay>) {
        val array = serializeDaysToJsonArray(days)
        val youtubeMap = mutableMapOf<String, String>()
        days.forEach { day ->
            day.exercises.forEach { ex ->
                if (!ex.savedYoutubeId.isNullOrBlank()) {
                    youtubeMap[ex.id] = ex.savedYoutubeId
                }
            }
        }
        saveYoutubeIdMap(youtubeMap)
        prefs.edit().putString(KEY_WORKOUT_DAYS, array.toString()).apply()
    }

    fun loadSavedYoutubeIdMap(): Map<String, String> {
        val raw = prefs.getString(KEY_SAVED_YOUTUBE_IDS, null) ?: return emptyMap()
        return try {
            val obj = JSONObject(raw)
            val result = mutableMapOf<String, String>()
            val keys = obj.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                val value = obj.optString(key, "")
                if (value.isNotBlank()) {
                    result[key] = value
                }
            }
            result
        } catch (_: Exception) {
            emptyMap()
        }
    }

    private fun saveYoutubeIdMap(map: Map<String, String>) {
        val obj = JSONObject()
        map.forEach { (k, v) -> obj.put(k, v) }
        prefs.edit().putString(KEY_SAVED_YOUTUBE_IDS, obj.toString()).apply()
    }

    fun loadCompletedLogs(): List<CompletedWorkoutLog> {
        val raw = prefs.getString(KEY_COMPLETED_LOGS, null) ?: return emptyList()
        return try {
            parseLogsJsonArray(JSONArray(raw))
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun saveCompletedLogs(logs: List<CompletedWorkoutLog>) {
        val array = serializeLogsToJsonArray(logs)
        prefs.edit().putString(KEY_COMPLETED_LOGS, array.toString()).apply()
    }

    fun getCurrentStreak(): Int = prefs.getInt(KEY_CURRENT_STREAK, 0)
    fun getBestStreak(): Int = prefs.getInt(KEY_BEST_STREAK, 0)

    fun saveStreaks(currentStreak: Int, bestStreak: Int) {
        prefs.edit()
            .putInt(KEY_CURRENT_STREAK, currentStreak)
            .putInt(KEY_BEST_STREAK, maxOf(bestStreak, currentStreak))
            .apply()
    }

    /**
     * Computes the active workout streak based on completed workout logs and updates SharedPreferences.
     */
    fun recalculateStreak(logs: List<CompletedWorkoutLog>): Pair<Int, Int> {
        if (logs.isEmpty()) {
            val best = getBestStreak()
            saveStreaks(0, best)
            return 0 to best
        }

        // Group sessions by distinct calendar day index
        val distinctEpochDays = logs
            .map { millisToDayBucket(it.completedAtMillis) }
            .distinct()
            .sortedDescending()

        var streak = 1
        for (i in 0 until distinctEpochDays.size - 1) {
            val diff = distinctEpochDays[i] - distinctEpochDays[i + 1]
            // Allow up to 2 days gap in a 4-day weekly split without breaking the active routine streak
            if (diff in 1..2) {
                streak++
            } else {
                break
            }
        }

        val best = maxOf(getBestStreak(), streak)
        saveStreaks(streak, best)
        return streak to best
    }

    private fun millisToDayBucket(millis: Long): Long {
        val cal = Calendar.getInstance().apply {
            timeInMillis = millis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis / (24L * 60L * 60L * 1000L)
    }

    fun resetToDefaultPlan(): List<WorkoutDay> {
        val savedYoutubeMap = loadSavedYoutubeIdMap()
        val defaults = PreloadedWorkoutPlan.getDefaultDays().map { day ->
            day.copy(
                exercises = day.exercises.map { ex ->
                    ex.copy(savedYoutubeId = savedYoutubeMap[ex.id])
                }
            )
        }
        saveWorkoutDays(defaults)
        return defaults
    }

    fun exportBackupJsonString(
        days: List<WorkoutDay>,
        logs: List<CompletedWorkoutLog>,
        currentStreak: Int,
        bestStreak: Int
    ): String {
        val root = JSONObject()
        root.put("schemaVersion", 1)
        root.put("appName", "ApexPosture")
        root.put("exportedAtMillis", System.currentTimeMillis())
        root.put("currentStreak", currentStreak)
        root.put("bestStreak", bestStreak)
        root.put("workoutDays", serializeDaysToJsonArray(days))
        root.put("completedLogs", serializeLogsToJsonArray(logs))

        val youtubeObj = JSONObject()
        days.forEach { day ->
            day.exercises.forEach { ex ->
                if (!ex.savedYoutubeId.isNullOrBlank()) {
                    youtubeObj.put(ex.id, ex.savedYoutubeId)
                }
            }
        }
        root.put("savedYoutubeIds", youtubeObj)
        return root.toString(2)
    }

    fun importBackupJsonString(jsonString: String): Result<BackupData> {
        return try {
            val root = JSONObject(jsonString)
            val youtubeMap = mutableMapOf<String, String>()
            root.optJSONObject("savedYoutubeIds")?.let { ytObj ->
                val keys = ytObj.keys()
                while (keys.hasNext()) {
                    val k = keys.next()
                    val v = ytObj.optString(k, "")
                    if (v.isNotBlank()) youtubeMap[k] = v
                }
            }

            val daysArray = root.getJSONArray("workoutDays")
            val days = parseDaysJsonArray(daysArray, youtubeMap)
            if (days.isEmpty()) {
                return Result.failure(IllegalArgumentException("Backup JSON contains no workout days."))
            }

            val logsArray = root.optJSONArray("completedLogs") ?: JSONArray()
            val logs = parseLogsJsonArray(logsArray)
            val currentStreak = root.optInt("currentStreak", 0)
            val bestStreak = root.optInt("bestStreak", currentStreak)
            val exportedAt = root.optLong("exportedAtMillis", System.currentTimeMillis())

            saveWorkoutDays(days)
            saveCompletedLogs(logs)
            saveStreaks(currentStreak, bestStreak)

            Result.success(
                BackupData(
                    days = days,
                    logs = logs,
                    currentStreak = currentStreak,
                    bestStreak = bestStreak,
                    exportedAtMillis = exportedAt
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun serializeDaysToJsonArray(days: List<WorkoutDay>): JSONArray {
        val daysArray = JSONArray()
        for (day in days) {
            val dayObj = JSONObject()
            dayObj.put("id", day.id)
            dayObj.put("dayNumber", day.dayNumber)
            dayObj.put("title", day.title)
            dayObj.put("shortTitle", day.shortTitle)
            dayObj.put("focusBadge", day.focusBadge)
            dayObj.put("anatomicalGoal", day.anatomicalGoal)

            val exArray = JSONArray()
            for (ex in day.exercises) {
                val exObj = JSONObject()
                exObj.put("id", ex.id)
                exObj.put("name", ex.name)
                exObj.put("isWarmup", ex.isWarmup)
                exObj.put("sets", ex.sets)
                exObj.put("targetRepsOrHold", ex.targetRepsOrHold)
                exObj.put("isTimedHold", ex.isTimedHold)
                exObj.put("holdDurationSeconds", ex.holdDurationSeconds)
                exObj.put("isPerSideHold", ex.isPerSideHold)
                exObj.put("restSeconds", ex.restSeconds)
                exObj.put("searchQuery", ex.searchQuery)
                exObj.put("formCues", ex.formCues)
                exObj.put("savedYoutubeId", ex.savedYoutubeId ?: "")
                exArray.put(exObj)
            }
            dayObj.put("exercises", exArray)
            daysArray.put(dayObj)
        }
        return daysArray
    }

    private fun parseDaysJsonArray(
        daysArray: JSONArray,
        savedYoutubeMap: Map<String, String>
    ): List<WorkoutDay> {
        val result = mutableListOf<WorkoutDay>()
        for (i in 0 until daysArray.length()) {
            val dayObj = daysArray.getJSONObject(i)
            val exArray = dayObj.getJSONArray("exercises")
            val exercises = mutableListOf<Exercise>()
            for (j in 0 until exArray.length()) {
                val exObj = exArray.getJSONObject(j)
                val id = exObj.getString("id")
                val rawYt = exObj.optString("savedYoutubeId", "").takeIf { it.isNotBlank() }
                exercises.add(
                    Exercise(
                        id = id,
                        name = exObj.getString("name"),
                        isWarmup = exObj.optBoolean("isWarmup", false),
                        sets = exObj.optInt("sets", 3),
                        targetRepsOrHold = exObj.optString("targetRepsOrHold", "10 reps"),
                        isTimedHold = exObj.optBoolean("isTimedHold", false),
                        holdDurationSeconds = exObj.optInt("holdDurationSeconds", 0),
                        isPerSideHold = exObj.optBoolean("isPerSideHold", false),
                        restSeconds = exObj.optInt("restSeconds", 60),
                        searchQuery = exObj.optString("searchQuery", ""),
                        formCues = exObj.optString("formCues", ""),
                        savedYoutubeId = rawYt ?: savedYoutubeMap[id]
                    )
                )
            }
            result.add(
                WorkoutDay(
                    id = dayObj.getString("id"),
                    dayNumber = dayObj.optInt("dayNumber", i + 1),
                    title = dayObj.getString("title"),
                    shortTitle = dayObj.optString("shortTitle", dayObj.getString("title")),
                    focusBadge = dayObj.optString("focusBadge", "Posture & Hypertrophy"),
                    anatomicalGoal = dayObj.optString("anatomicalGoal", ""),
                    exercises = exercises
                )
            )
        }
        return result
    }

    private fun serializeLogsToJsonArray(logs: List<CompletedWorkoutLog>): JSONArray {
        val array = JSONArray()
        for (log in logs) {
            val obj = JSONObject()
            obj.put("id", log.id)
            obj.put("dayId", log.dayId)
            obj.put("dayNumber", log.dayNumber)
            obj.put("dayTitle", log.dayTitle)
            obj.put("completedAtMillis", log.completedAtMillis)
            obj.put("durationSeconds", log.durationSeconds)
            obj.put("completedSets", log.completedSets)
            obj.put("totalSets", log.totalSets)
            obj.put("postureRating", log.postureRating)
            array.put(obj)
        }
        return array
    }

    private fun parseLogsJsonArray(array: JSONArray): List<CompletedWorkoutLog> {
        val logs = mutableListOf<CompletedWorkoutLog>()
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            logs.add(
                CompletedWorkoutLog(
                    id = obj.getString("id"),
                    dayId = obj.optString("dayId", "day_1"),
                    dayNumber = obj.optInt("dayNumber", 1),
                    dayTitle = obj.optString("dayTitle", "Workout Session"),
                    completedAtMillis = obj.optLong("completedAtMillis", System.currentTimeMillis()),
                    durationSeconds = obj.optInt("durationSeconds", 1800),
                    completedSets = obj.optInt("completedSets", 18),
                    totalSets = obj.optInt("totalSets", 18),
                    postureRating = obj.optInt("postureRating", 5)
                )
            )
        }
        return logs.sortedByDescending { it.completedAtMillis }
    }

    companion object {
        private const val PREFS_NAME = "apex_posture_workout_prefs"
        private const val KEY_WORKOUT_DAYS = "workout_days_json_v1"
        private const val KEY_COMPLETED_LOGS = "completed_logs_json_v1"
        private const val KEY_SAVED_YOUTUBE_IDS = "saved_youtube_ids_json_v1"
        private const val KEY_CURRENT_STREAK = "current_streak_v1"
        private const val KEY_BEST_STREAK = "best_streak_v1"
    }
}

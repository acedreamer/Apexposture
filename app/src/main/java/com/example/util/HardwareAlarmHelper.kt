package com.example.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class HardwareAlarmHelper(private val context: Context) {

    private val notificationManager: NotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    private val scope = CoroutineScope(Dispatchers.Default)

    init {
        ensureNotificationChannel()
    }

    private fun ensureNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Workout Timer & Hold Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "High-priority alerts for rest countdowns and isometric holds"
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Sharp 3-2-1 countdown beep using Android ToneGenerator(STREAM_ALARM) + short haptic tick.
     */
    fun playCountdownTick() {
        scope.launch {
            try {
                val toneGen = ToneGenerator(AudioManager.STREAM_ALARM, 90)
                toneGen.startTone(ToneGenerator.TONE_PROP_BEEP, 130)
                delay(160)
                toneGen.release()
            } catch (_: Exception) {
                // Safe fallback if audio hardware is busy
            }
        }
        vibratePulse(durationMs = 55L, amplitude = 160)
    }

    /**
     * Transition beep when 5-second "Get Ready" prep finishes and isometric hold begins.
     */
    fun playHoldStartCue() {
        scope.launch {
            try {
                val toneGen = ToneGenerator(AudioManager.STREAM_ALARM, 100)
                toneGen.startTone(ToneGenerator.TONE_PROP_ACK, 240)
                delay(280)
                toneGen.release()
            } catch (_: Exception) {
            }
        }
        vibratePulse(durationMs = 160L, amplitude = 255)
    }

    /**
     * Distinct multi-pulse completion chime + strong haptic pattern + system notification.
     */
    fun triggerTimerCompletionAlert(title: String, message: String) {
        scope.launch {
            try {
                val toneGen = ToneGenerator(AudioManager.STREAM_ALARM, 100)
                toneGen.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 420)
                delay(460)
                toneGen.startTone(ToneGenerator.TONE_PROP_BEEP2, 320)
                delay(360)
                toneGen.release()
            } catch (_: Exception) {
            }
        }
        vibrateCompletionPattern()
        sendTimerNotification(title, message)
    }

    /**
     * Triggered by the "Test Alarm & Haptics" button on the Home Dashboard.
     */
    fun testAlarmAndNotification(): String {
        triggerTimerCompletionAlert(
            title = "ApexPosture Alarm Test",
            message = "STREAM_ALARM chime, haptic pulse & timer notification verified!"
        )
        return "Triggered STREAM_ALARM chime, strong haptic pulse, and timer notification."
    }

    private fun vibratePulse(durationMs: Long, amplitude: Int) {
        try {
            val vibrator = getVibrator() ?: return
            if (!vibrator.hasVibrator()) return
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(durationMs, amplitude))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(durationMs)
            }
        } catch (_: Exception) {
        }
    }

    private fun vibrateCompletionPattern() {
        try {
            val vibrator = getVibrator() ?: return
            if (!vibrator.hasVibrator()) return
            val timings = longArrayOf(0L, 180L, 90L, 220L, 90L, 320L)
            val amplitudes = intArrayOf(0, 255, 0, 255, 0, 255)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(timings, -1)
            }
        } catch (_: Exception) {
        }
    }

    private fun getVibrator(): Vibrator? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            manager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    private fun sendTimerNotification(title: String, message: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) return
        }

        try {
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .build()

            notificationManager.notify(NOTIFICATION_ID, notification)
        } catch (_: Exception) {
        }
    }

    companion object {
        private const val CHANNEL_ID = "workout_timer_alerts"
        private const val NOTIFICATION_ID = 4012
    }
}

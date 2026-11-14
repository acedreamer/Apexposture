package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.WorkoutRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context and verify repository backup roundtrip`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("ApexPosture", appName)

        val repo = WorkoutRepository(context)
        val days = repo.loadWorkoutDays()
        assertEquals(4, days.size)

        val jsonBackup = repo.exportBackupJsonString(
            days = days,
            logs = emptyList(),
            currentStreak = 3,
            bestStreak = 5
        )
        val restored = repo.importBackupJsonString(jsonBackup)
        assertTrue(restored.isSuccess)
        assertEquals(3, restored.getOrThrow().currentStreak)
    }
}

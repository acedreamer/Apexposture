package com.example

import com.example.data.PreloadedWorkoutPlan
import com.example.model.YoutubeUrlParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun youtubeUrlParser_extractsVideoIdsFromAllFormats() {
        assertEquals(
            "dQw4w9WgXcQ",
            YoutubeUrlParser.extractVideoId("https://www.youtube.com/watch?v=dQw4w9WgXcQ&t=12s")
        )
        assertEquals(
            "dQw4w9WgXcQ",
            YoutubeUrlParser.extractVideoId("https://youtu.be/dQw4w9WgXcQ?si=abc123")
        )
        assertEquals(
            "dQw4w9WgXcQ",
            YoutubeUrlParser.extractVideoId("https://www.youtube.com/shorts/dQw4w9WgXcQ")
        )
        assertEquals(
            "dQw4w9WgXcQ",
            YoutubeUrlParser.extractVideoId("dQw4w9WgXcQ")
        )
    }

    @Test
    fun preloadedWorkoutPlan_hasAllFourDaysAndExercises() {
        val days = PreloadedWorkoutPlan.getDefaultDays()
        assertEquals(4, days.size)
        assertEquals(7, days[0].exercises.size)
        assertEquals(6, days[1].exercises.size)
        assertEquals(7, days[2].exercises.size)
        assertEquals(7, days[3].exercises.size)

        days.forEach { day ->
            day.exercises.forEach { ex ->
                assertTrue(ex.searchQuery.isNotBlank())
                assertTrue(ex.formCues.contains("Avoid "))
                assertNotNull(ex.mistakeToAvoidText)
            }
        }
    }
}

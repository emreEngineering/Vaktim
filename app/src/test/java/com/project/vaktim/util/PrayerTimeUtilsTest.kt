package com.project.vaktim.util

import com.project.vaktim.data.model.PrayerTime
import java.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class PrayerTimeUtilsTest {

    @Test
    fun `cleanTime removes timezone suffix`() {
        val cleaned = PrayerTimeUtils.cleanTime("05:31 (+03)")
        assertEquals("05:31", cleaned)
    }

    @Test
    fun `calculateNextPrayer returns next main prayer and remaining time`() {
        val prayers = listOf(
            PrayerTime("Fajr", "Imsak", "05:00"),
            PrayerTime("Sunrise", "Gunes", "06:30"),
            PrayerTime("Dhuhr", "Ogle", "13:15"),
            PrayerTime("Asr", "Ikindi", "16:45"),
            PrayerTime("Maghrib", "Aksam", "19:30"),
            PrayerTime("Isha", "Yatsi", "21:00")
        )

        val (next, remaining) = PrayerTimeUtils.calculateNextPrayer(
            prayerList = prayers,
            now = LocalTime.of(6, 40)
        )

        assertNotNull(next)
        assertEquals("Dhuhr", next?.name)
        assertEquals("6s 35dk", remaining)
    }

    @Test
    fun `calculateNextPrayer wraps to tomorrow when all prayers passed`() {
        val prayers = listOf(
            PrayerTime("Fajr", "Imsak", "05:00"),
            PrayerTime("Dhuhr", "Ogle", "13:15"),
            PrayerTime("Asr", "Ikindi", "16:45"),
            PrayerTime("Maghrib", "Aksam", "19:30"),
            PrayerTime("Isha", "Yatsi", "21:00")
        )

        val (next, remaining) = PrayerTimeUtils.calculateNextPrayer(
            prayerList = prayers,
            now = LocalTime.of(23, 50)
        )

        assertNotNull(next)
        assertEquals("Fajr", next?.name)
        assertEquals("5s 10dk", remaining)
    }
}

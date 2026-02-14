package com.project.vaktim.domain

import com.project.vaktim.data.model.PrayerTime
import com.project.vaktim.data.model.Timings
import com.project.vaktim.util.PrayerTimeUtils

class PrayerScheduleFactory {

    fun createSchedule(timings: Timings): List<PrayerTime> {
        return listOf(
            PrayerTime("Fajr", "Imsak", PrayerTimeUtils.cleanTime(timings.fajr)),
            PrayerTime("Sunrise", "Gunes", PrayerTimeUtils.cleanTime(timings.sunrise)),
            PrayerTime("Dhuhr", "Ogle", PrayerTimeUtils.cleanTime(timings.dhuhr)),
            PrayerTime("Asr", "Ikindi", PrayerTimeUtils.cleanTime(timings.asr)),
            PrayerTime("Maghrib", "Aksam", PrayerTimeUtils.cleanTime(timings.maghrib)),
            PrayerTime("Isha", "Yatsi", PrayerTimeUtils.cleanTime(timings.isha))
        )
    }
}

package com.project.vaktim.domain

import com.project.vaktim.data.model.PrayerTime
import com.project.vaktim.data.model.Timings
import com.project.vaktim.util.PrayerTimeUtils

class PrayerScheduleFactory {

    fun createSchedule(timings: Timings): List<PrayerTime> {
        return listOf(
            PrayerTime("Fajr", "İmsak", PrayerTimeUtils.cleanTime(timings.fajr)),
            PrayerTime("Sunrise", "Güneş", PrayerTimeUtils.cleanTime(timings.sunrise)),
            PrayerTime("Dhuhr", "Öğle", PrayerTimeUtils.cleanTime(timings.dhuhr)),
            PrayerTime("Asr", "İkindi", PrayerTimeUtils.cleanTime(timings.asr)),
            PrayerTime("Maghrib", "Akşam", PrayerTimeUtils.cleanTime(timings.maghrib)),
            PrayerTime("Isha", "Yatsı", PrayerTimeUtils.cleanTime(timings.isha))
        )
    }
}

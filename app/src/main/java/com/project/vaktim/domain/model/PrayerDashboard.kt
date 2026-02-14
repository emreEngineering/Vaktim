package com.project.vaktim.domain.model

import com.project.vaktim.data.model.PrayerTime

data class PrayerDashboard(
    val prayerTimes: List<PrayerTime>,
    val nextPrayer: PrayerTime?,
    val remainingTime: String,
    val hijriDate: String,
    val gregorianDate: String
)

package com.project.vaktim.util

import android.util.Log
import com.project.vaktim.data.model.PrayerTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

object PrayerTimeUtils {

    private const val TAG = "PrayerTimeUtils"
    private val formatter = DateTimeFormatter.ofPattern("HH:mm")

    fun cleanTime(time: String): String {
        return time.replace(Regex("\\s*\\([^)]*\\)"), "").trim()
    }

    fun calculateNextPrayer(
        prayerList: List<PrayerTime>,
        excludeNames: Set<String> = setOf("Sunrise"),
        now: LocalTime = LocalTime.now()
    ): Pair<PrayerTime?, String> {
        val mainPrayers = prayerList.filter { it.name !in excludeNames }

        for (prayer in mainPrayers) {
            try {
                val prayerTime = LocalTime.parse(prayer.time, formatter)
                if (prayerTime.isAfter(now)) {
                    val remaining = formatRemainingTime(now, prayerTime)
                    return Pair(prayer, remaining)
                }
            } catch (e: Exception) {
                Log.w(TAG, "Could not parse prayer time: ${prayer.turkishName} - ${prayer.time}", e)
            }
        }

        val firstPrayer = mainPrayers.firstOrNull()
        return if (firstPrayer != null) {
            try {
                val prayerTime = LocalTime.parse(firstPrayer.time, formatter)
                val minutesUntilMidnight = ChronoUnit.MINUTES.between(now, LocalTime.MAX)
                val minutesAfterMidnight = ChronoUnit.MINUTES.between(LocalTime.MIN, prayerTime)
                val totalMinutes = minutesUntilMidnight + minutesAfterMidnight + 1
                Pair(firstPrayer, formatMinutes(totalMinutes))
            } catch (e: Exception) {
                Log.w(TAG, "Could not calculate next day prayer: ${firstPrayer.turkishName}", e)
                Pair(firstPrayer, "--")
            }
        } else {
            Pair(null, "--")
        }
    }

    private fun formatRemainingTime(now: LocalTime, target: LocalTime): String {
        val minutes = ChronoUnit.MINUTES.between(now, target)
        return formatMinutes(minutes)
    }

    private fun formatMinutes(totalMinutes: Long): String {
        val hours = totalMinutes / 60
        val mins = totalMinutes % 60
        return if (hours > 0) {
            "${hours}s ${mins}dk"
        } else {
            "${mins}dk"
        }
    }
}

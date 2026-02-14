package com.project.vaktim.domain

import com.project.vaktim.data.repository.IPrayerTimesRepository
import com.project.vaktim.domain.model.LocationSelection
import com.project.vaktim.domain.model.PrayerDashboard
import com.project.vaktim.util.PrayerTimeUtils

class GetPrayerDashboardUseCase(
    private val prayerTimesRepository: IPrayerTimesRepository,
    private val scheduleFactory: PrayerScheduleFactory = PrayerScheduleFactory(),
    private val dateFormatter: DateFormatter = DateFormatter()
) {

    suspend operator fun invoke(location: LocationSelection): Result<PrayerDashboard> {
        return prayerTimesRepository.getPrayerTimes(
            city = location.toApiCity(),
            country = location.country
        ).mapCatching { response ->
            val prayerList = scheduleFactory.createSchedule(response.data.timings)
            val (nextPrayer, remaining) = PrayerTimeUtils.calculateNextPrayer(prayerList)

            PrayerDashboard(
                prayerTimes = prayerList,
                nextPrayer = nextPrayer,
                remainingTime = remaining,
                hijriDate = dateFormatter.formatHijri(response.data.date.hijri),
                gregorianDate = dateFormatter.formatGregorian(response.data.date.gregorian)
            )
        }
    }
}

package com.project.vaktim.data.repository

import com.project.vaktim.data.api.PrayerTimesApi
import com.project.vaktim.data.api.RetrofitClient
import com.project.vaktim.data.local.InMemoryPrayerTimesOfflineStore
import com.project.vaktim.data.local.PrayerTimesOfflineStore
import com.project.vaktim.data.model.PrayerData
import com.project.vaktim.data.model.PrayerTimesResponse
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class PrayerTimesRepository(
    private val api: PrayerTimesApi = RetrofitClient.prayerTimesApi,
    private val ttlMillis: Long = DEFAULT_CACHE_TTL_MILLIS,
    private val offlineStore: PrayerTimesOfflineStore = InMemoryPrayerTimesOfflineStore(),
    private val nowProvider: () -> Long = { System.currentTimeMillis() },
    private val todayProvider: () -> LocalDate = { LocalDate.now() }
) : IPrayerTimesRepository {

    private val cache = ConcurrentHashMap<String, CacheEntry>()
    private val requestLocks = ConcurrentHashMap<String, Mutex>()

    override suspend fun getPrayerTimes(city: String, country: String): Result<PrayerTimesResponse> {
        val locationKey = locationKey(city = city, country = country)
        val today = todayProvider()
        val dayKey = dayKey(locationKey = locationKey, day = today)

        freshCacheFor(dayKey)?.let { return Result.success(it) }

        return lockForLocation(locationKey).withLock {
            freshCacheFor(dayKey)?.let { return@withLock Result.success(it) }

            val address = "${city.trim()}, ${country.trim()}"
            val endDay = today.plusDays(PREFETCH_DAYS_AHEAD.toLong())

            try {
                val fetched = fetchRangeByAddress(address = address, start = today, end = endDay)
                if (fetched.isNotEmpty()) {
                    storeFetched(locationKey = locationKey, fetched = fetched, today = today)
                    fetched[today]?.let { return@withLock Result.success(it) }
                }

                offlineStore.get(locationKey, today)?.let { return@withLock Result.success(it) }
                Result.failure(IllegalStateException("Prayer times not found for $today"))
            } catch (e: Exception) {
                offlineStore.get(locationKey, today)?.let { return@withLock Result.success(it) }
                Result.failure(e)
            }
        }
    }

    private suspend fun fetchRangeByAddress(
        address: String,
        start: LocalDate,
        end: LocalDate
    ): Map<LocalDate, PrayerTimesResponse> {
        val requestedDates = mutableSetOf<LocalDate>()
        var cursor = start
        while (!cursor.isAfter(end)) {
            requestedDates += cursor
            cursor = cursor.plusDays(1)
        }

        val groupedByMonth = requestedDates.groupBy { YearMonth.from(it) }
        val result = mutableMapOf<LocalDate, PrayerTimesResponse>()

        groupedByMonth.forEach { (yearMonth, datesInMonth) ->
            val response = api.getCalendarByAddress(
                year = yearMonth.year,
                month = yearMonth.monthValue,
                address = address
            )
            if (response.code != 200) {
                throw IllegalStateException("API Error: ${response.status}")
            }

            response.data.forEach { dayData ->
                val day = parseGregorianDate(dayData) ?: return@forEach
                if (day in datesInMonth) {
                    result[day] = PrayerTimesResponse(
                        code = 200,
                        status = "OK",
                        data = dayData
                    )
                }
            }
        }

        return result
    }

    private fun storeFetched(
        locationKey: String,
        fetched: Map<LocalDate, PrayerTimesResponse>,
        today: LocalDate
    ) {
        offlineStore.saveAll(locationKey, fetched)
        offlineStore.prune(
            locationKey = locationKey,
            keepFrom = today.minusDays(1),
            keepTo = today.plusDays(PREFETCH_DAYS_AHEAD.toLong())
        )

        val savedAt = nowProvider()
        fetched.forEach { (day, response) ->
            cache[dayKey(locationKey, day)] = CacheEntry(response = response, savedAt = savedAt)
        }
    }

    private fun parseGregorianDate(prayerData: PrayerData): LocalDate? {
        return runCatching {
            LocalDate.parse(prayerData.date.gregorian.date, ALADHAN_DATE_FORMAT)
        }.getOrNull()
    }

    private fun freshCacheFor(key: String): PrayerTimesResponse? {
        val entry = cache[key] ?: return null
        val age = nowProvider() - entry.savedAt
        return if (age <= ttlMillis) entry.response else null
    }

    private fun locationKey(city: String, country: String): String {
        return "${city.trim()},${country.trim()}".lowercase()
    }

    private fun dayKey(locationKey: String, day: LocalDate): String {
        return "$locationKey|$day"
    }

    private fun lockForLocation(locationKey: String): Mutex {
        return requestLocks.getOrPut(locationKey) { Mutex() }
    }

    private data class CacheEntry(
        val response: PrayerTimesResponse,
        val savedAt: Long
    )

    companion object {
        private const val DEFAULT_CACHE_TTL_MILLIS = 15 * 60 * 1000L
        private const val PREFETCH_DAYS_AHEAD = 3
        private val ALADHAN_DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
    }
}

package com.project.vaktim.data.repository

import com.project.vaktim.data.api.PrayerTimesApi
import com.project.vaktim.data.local.InMemoryPrayerTimesOfflineStore
import com.project.vaktim.data.model.DateInfo
import com.project.vaktim.data.model.GregorianDate
import com.project.vaktim.data.model.HijriDate
import com.project.vaktim.data.model.HijriMonth
import com.project.vaktim.data.model.Meta
import com.project.vaktim.data.model.Month
import com.project.vaktim.data.model.PrayerCalendarResponse
import com.project.vaktim.data.model.PrayerData
import com.project.vaktim.data.model.PrayerTimesResponse
import com.project.vaktim.data.model.Timings
import com.project.vaktim.data.model.Weekday
import java.time.LocalDate
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PrayerTimesRepositoryTest {

    @Test
    fun `uses fresh in-memory cache without calendar api call`() = runTest {
        val today = LocalDate.of(2026, 3, 16)
        var now = 1_000L
        val api = FakePrayerTimesApi(
            calendarResponses = mutableListOf(
                buildCalendarResponse(today)
            )
        )

        val repository = PrayerTimesRepository(
            api = api,
            ttlMillis = 60_000,
            offlineStore = InMemoryPrayerTimesOfflineStore(),
            nowProvider = { now },
            todayProvider = { today }
        )

        val first = repository.getPrayerTimes(city = "Istanbul", country = "Turkey")
        now += 5_000
        val second = repository.getPrayerTimes(city = "Istanbul", country = "Turkey")

        assertTrue(first.isSuccess)
        assertTrue(second.isSuccess)
        assertEquals(1, api.calendarCallCount)
    }

    @Test
    fun `returns offline data when internet fails`() = runTest {
        val today = LocalDate.of(2026, 3, 16)
        var now = 1_000L
        val api = FakePrayerTimesApi(
            calendarResponses = mutableListOf(
                buildCalendarResponse(today),
                RuntimeException("network down")
            )
        )

        val repository = PrayerTimesRepository(
            api = api,
            ttlMillis = 10,
            offlineStore = InMemoryPrayerTimesOfflineStore(),
            nowProvider = { now },
            todayProvider = { today }
        )

        val first = repository.getPrayerTimes(city = "Istanbul", country = "Turkey")
        now += 100
        val second = repository.getPrayerTimes(city = "Istanbul", country = "Turkey")

        assertTrue(first.isSuccess)
        assertTrue(second.isSuccess)
        assertEquals(2, api.calendarCallCount)
    }

    @Test
    fun `fails when internet fails and offline data missing`() = runTest {
        val today = LocalDate.of(2026, 3, 16)
        val api = FakePrayerTimesApi(
            calendarResponses = mutableListOf(RuntimeException("network down"))
        )

        val repository = PrayerTimesRepository(
            api = api,
            ttlMillis = 10,
            offlineStore = InMemoryPrayerTimesOfflineStore(),
            nowProvider = { 1_000L },
            todayProvider = { today }
        )

        val result = repository.getPrayerTimes(city = "Istanbul", country = "Turkey")

        assertTrue(result.isFailure)
        assertEquals(1, api.calendarCallCount)
    }

    private class FakePrayerTimesApi(
        private val calendarResponses: MutableList<Any>
    ) : PrayerTimesApi {

        var calendarCallCount: Int = 0

        override suspend fun getTimingsByAddress(address: String, method: Int): PrayerTimesResponse {
            throw UnsupportedOperationException("Not used in repository tests")
        }

        override suspend fun getCalendarByAddress(
            year: Int,
            month: Int,
            address: String,
            method: Int
        ): PrayerCalendarResponse {
            calendarCallCount++
            val next = calendarResponses.removeAt(0)
            return when (next) {
                is PrayerCalendarResponse -> next
                is RuntimeException -> throw next
                else -> throw IllegalStateException("Unsupported scripted response")
            }
        }
    }

    private fun buildCalendarResponse(today: LocalDate): PrayerCalendarResponse {
        val data = listOf(
            prayerDataFor(today),
            prayerDataFor(today.plusDays(1)),
            prayerDataFor(today.plusDays(2)),
            prayerDataFor(today.plusDays(3))
        )
        return PrayerCalendarResponse(
            code = 200,
            status = "OK",
            data = data
        )
    }

    private fun prayerDataFor(day: LocalDate): PrayerData {
        return PrayerData(
            timings = Timings(
                fajr = "05:11 (+03)",
                sunrise = "06:40 (+03)",
                dhuhr = "13:09 (+03)",
                asr = "16:31 (+03)",
                sunset = "19:29 (+03)",
                maghrib = "19:29 (+03)",
                isha = "20:50 (+03)",
                imsak = "05:00 (+03)",
                midnight = "00:10 (+03)",
                firstthird = null,
                lastthird = null
            ),
            date = DateInfo(
                readable = day.toString(),
                timestamp = "0",
                gregorian = GregorianDate(
                    date = day.format(java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy")),
                    format = "DD-MM-YYYY",
                    day = day.dayOfMonth.toString(),
                    weekday = Weekday(en = day.dayOfWeek.name),
                    month = Month(number = day.monthValue, en = day.month.name),
                    year = day.year.toString()
                ),
                hijri = HijriDate(
                    date = "07-09-1447",
                    format = "DD-MM-YYYY",
                    day = "7",
                    weekday = Weekday(en = day.dayOfWeek.name),
                    month = HijriMonth(number = 9, en = "Ramadan", ar = "Ramadan"),
                    year = "1447"
                )
            ),
            meta = Meta(
                latitude = 0.0,
                longitude = 0.0,
                timezone = "Europe/Istanbul"
            )
        )
    }
}

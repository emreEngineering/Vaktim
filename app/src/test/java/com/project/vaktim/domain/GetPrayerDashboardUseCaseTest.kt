package com.project.vaktim.domain

import com.project.vaktim.data.model.DateInfo
import com.project.vaktim.data.model.GregorianDate
import com.project.vaktim.data.model.HijriDate
import com.project.vaktim.data.model.HijriMonth
import com.project.vaktim.data.model.Meta
import com.project.vaktim.data.model.Month
import com.project.vaktim.data.model.PrayerData
import com.project.vaktim.data.model.PrayerTimesResponse
import com.project.vaktim.data.model.Timings
import com.project.vaktim.data.model.Weekday
import com.project.vaktim.data.repository.IPrayerTimesRepository
import com.project.vaktim.domain.model.LocationSelection
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GetPrayerDashboardUseCaseTest {

    @Test
    fun `uses district as api city and maps dashboard fields`() = runTest {
        val repository = FakePrayerTimesRepository()
        val useCase = GetPrayerDashboardUseCase(repository)
        val location = LocationSelection(city = "Istanbul", country = "Turkey", district = "Kadikoy")

        val result = useCase(location)

        assertTrue(result.isSuccess)
        assertEquals("Kadikoy", repository.lastCity)
        assertEquals("Turkey", repository.lastCountry)
        val dashboard = result.getOrThrow()
        assertEquals(6, dashboard.prayerTimes.size)
        assertEquals("16 Mart 2026", dashboard.gregorianDate)
        assertEquals("7 Ramazan 1447", dashboard.hijriDate)
    }

    private class FakePrayerTimesRepository : IPrayerTimesRepository {
        var lastCity: String = ""
        var lastCountry: String = ""

        override suspend fun getPrayerTimes(city: String, country: String): Result<PrayerTimesResponse> {
            lastCity = city
            lastCountry = country
            return Result.success(
                PrayerTimesResponse(
                    code = 200,
                    status = "OK",
                    data = PrayerData(
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
                            readable = "16 Mar 2026",
                            timestamp = "0",
                            gregorian = GregorianDate(
                                date = "16-03-2026",
                                format = "DD-MM-YYYY",
                                day = "16",
                                weekday = Weekday(en = "Monday"),
                                month = Month(number = 3, en = "March"),
                                year = "2026"
                            ),
                            hijri = HijriDate(
                                date = "07-09-1447",
                                format = "DD-MM-YYYY",
                                day = "7",
                                weekday = Weekday(en = "Monday"),
                                month = HijriMonth(number = 9, en = "Ramadan", ar = "رمضان"),
                                year = "1447"
                            )
                        ),
                        meta = Meta(
                            latitude = 0.0,
                            longitude = 0.0,
                            timezone = "Europe/Istanbul"
                        )
                    )
                )
            )
        }
    }
}

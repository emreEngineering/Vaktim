package com.project.vaktim.data.api

import com.project.vaktim.data.model.PrayerCalendarResponse
import com.project.vaktim.data.model.PrayerTimesResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface PrayerTimesApi {

    @GET("v1/timingsByAddress")
    suspend fun getTimingsByAddress(
        @Query("address") address: String,
        @Query("method") method: Int = 13 // Diyanet method
    ): PrayerTimesResponse

    @GET("v1/calendarByAddress/{year}/{month}")
    suspend fun getCalendarByAddress(
        @Path("year") year: Int,
        @Path("month") month: Int,
        @Query("address") address: String,
        @Query("method") method: Int = 13
    ): PrayerCalendarResponse
}

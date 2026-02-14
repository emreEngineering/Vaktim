package com.project.vaktim.data.api

import com.project.vaktim.data.model.PrayerTimesResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface PrayerTimesApi {
    
    @GET("v1/timingsByAddress")
    suspend fun getTimingsByAddress(
        @Query("address") address: String,
        @Query("method") method: Int = 13 // Diyanet İşleri Başkanlığı method
    ): PrayerTimesResponse
}

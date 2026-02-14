package com.project.vaktim.data.repository

import com.project.vaktim.data.model.PrayerTimesResponse

/**
 * Interface for prayer times data access (DIP — Dependency Inversion Principle).
 * Allows swapping implementations for testing or alternative data sources.
 */
interface IPrayerTimesRepository {
    suspend fun getPrayerTimes(city: String, country: String): Result<PrayerTimesResponse>
}

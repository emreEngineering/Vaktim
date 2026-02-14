package com.project.vaktim.data.repository

import com.project.vaktim.data.api.PrayerTimesApi
import com.project.vaktim.data.api.RetrofitClient
import com.project.vaktim.data.model.PrayerTimesResponse
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class PrayerTimesRepository(
    private val api: PrayerTimesApi = RetrofitClient.prayerTimesApi,
    private val ttlMillis: Long = DEFAULT_CACHE_TTL_MILLIS
) : IPrayerTimesRepository {

    private val requestLock = Mutex()
    private val cache = ConcurrentHashMap<String, CacheEntry>()

    override suspend fun getPrayerTimes(city: String, country: String): Result<PrayerTimesResponse> {
        val key = "$city,$country".lowercase()
        val now = System.currentTimeMillis()
        val cached = cache[key]
        if (cached != null && now - cached.savedAt <= ttlMillis) {
            return Result.success(cached.response)
        }

        return requestLock.withLock {
            val latestCached = cache[key]
            if (latestCached != null && now - latestCached.savedAt <= ttlMillis) {
                return@withLock Result.success(latestCached.response)
            }

            try {
                val response = api.getTimingsByAddress(address = "$city, $country")
                if (response.code != 200) {
                    val fallback = cache[key]
                    if (fallback != null) {
                        return@withLock Result.success(fallback.response)
                    }
                    return@withLock Result.failure(IllegalStateException("API Error: ${response.status}"))
                }

                cache[key] = CacheEntry(response = response, savedAt = now)
                Result.success(response)
            } catch (e: Exception) {
                val fallback = cache[key]
                if (fallback != null) {
                    Result.success(fallback.response)
                } else {
                    Result.failure(e)
                }
            }
        }
    }

    private data class CacheEntry(
        val response: PrayerTimesResponse,
        val savedAt: Long
    )

    companion object {
        private const val DEFAULT_CACHE_TTL_MILLIS = 15 * 60 * 1000L
    }
}

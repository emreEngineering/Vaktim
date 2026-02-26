package com.project.vaktim.data.local

import android.content.Context
import androidx.core.content.edit
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.project.vaktim.data.model.PrayerTimesResponse
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.concurrent.ConcurrentHashMap

interface PrayerTimesOfflineStore {
    fun get(locationKey: String, date: LocalDate): PrayerTimesResponse?
    fun saveAll(locationKey: String, responses: Map<LocalDate, PrayerTimesResponse>)
    fun prune(locationKey: String, keepFrom: LocalDate, keepTo: LocalDate)
}

class InMemoryPrayerTimesOfflineStore : PrayerTimesOfflineStore {
    private val store = ConcurrentHashMap<String, MutableMap<LocalDate, PrayerTimesResponse>>()

    override fun get(locationKey: String, date: LocalDate): PrayerTimesResponse? {
        return store[locationKey]?.get(date)
    }

    override fun saveAll(locationKey: String, responses: Map<LocalDate, PrayerTimesResponse>) {
        if (responses.isEmpty()) return
        val existing = store.getOrPut(locationKey) { mutableMapOf() }
        existing.putAll(responses)
    }

    override fun prune(locationKey: String, keepFrom: LocalDate, keepTo: LocalDate) {
        val existing = store[locationKey] ?: return
        val iterator = existing.keys.iterator()
        while (iterator.hasNext()) {
            val date = iterator.next()
            if (date.isBefore(keepFrom) || date.isAfter(keepTo)) {
                iterator.remove()
            }
        }
    }
}

class PrayerTimesPreferencesStore(
    context: Context,
    private val gson: Gson = Gson()
) : PrayerTimesOfflineStore {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    @Synchronized
    override fun get(locationKey: String, date: LocalDate): PrayerTimesResponse? {
        val data = read(locationKey)
        return data[date.format(DATE_KEY_FORMATTER)]
    }

    @Synchronized
    override fun saveAll(locationKey: String, responses: Map<LocalDate, PrayerTimesResponse>) {
        if (responses.isEmpty()) return
        val existing = read(locationKey).toMutableMap()
        responses.forEach { (date, response) ->
            existing[date.format(DATE_KEY_FORMATTER)] = response
        }
        write(locationKey, existing)
    }

    @Synchronized
    override fun prune(locationKey: String, keepFrom: LocalDate, keepTo: LocalDate) {
        val existing = read(locationKey).toMutableMap()
        if (existing.isEmpty()) return

        val iterator = existing.keys.iterator()
        while (iterator.hasNext()) {
            val key = iterator.next()
            val date = runCatching { LocalDate.parse(key, DATE_KEY_FORMATTER) }.getOrNull()
            if (date == null || date.isBefore(keepFrom) || date.isAfter(keepTo)) {
                iterator.remove()
            }
        }
        write(locationKey, existing)
    }

    private fun read(locationKey: String): Map<String, PrayerTimesResponse> {
        val json = prefs.getString(prefKey(locationKey), null) ?: return emptyMap()
        val type = object : TypeToken<Map<String, PrayerTimesResponse>>() {}.type
        return gson.fromJson<Map<String, PrayerTimesResponse>>(json, type) ?: emptyMap()
    }

    private fun write(locationKey: String, data: Map<String, PrayerTimesResponse>) {
        prefs.edit {
            if (data.isEmpty()) {
                remove(prefKey(locationKey))
            } else {
                putString(prefKey(locationKey), gson.toJson(data))
            }
        }
    }

    private fun prefKey(locationKey: String): String {
        return KEY_PREFIX + locationKey.lowercase().replace(NON_ALNUM_REGEX, "_")
    }

    companion object {
        private const val PREFS_NAME = "prayer_times_offline_cache"
        private const val KEY_PREFIX = "location_"
        private val DATE_KEY_FORMATTER: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE
        private val NON_ALNUM_REGEX = Regex("[^a-z0-9]")
    }
}

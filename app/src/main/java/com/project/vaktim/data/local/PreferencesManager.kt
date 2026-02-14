package com.project.vaktim.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.project.vaktim.core.AppDefaults
import com.project.vaktim.domain.model.LocationSelection

/**
 * Centralized SharedPreferences manager for prayer location preferences.
 * Single source of truth for reading/writing user location settings.
 */
class PreferencesManager(context: Context) : LocationPreferences {

    companion object {
        private const val PREFS_NAME = "prayer_prefs"
        private const val KEY_CITY = "city"
        private const val KEY_COUNTRY = "country"
        private const val KEY_DISTRICT = "district"
    }

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override fun saveLocation(location: LocationSelection) {
        prefs.edit {
            putString(KEY_CITY, location.city)
            putString(KEY_COUNTRY, location.country)
            putString(KEY_DISTRICT, location.district)
        }
    }

    fun getSavedCity(): String = prefs.getString(KEY_CITY, AppDefaults.DEFAULT_CITY) ?: AppDefaults.DEFAULT_CITY
    fun getSavedCountry(): String = prefs.getString(KEY_COUNTRY, AppDefaults.DEFAULT_COUNTRY) ?: AppDefaults.DEFAULT_COUNTRY
    fun getSavedDistrict(): String = prefs.getString(KEY_DISTRICT, "") ?: ""

    override fun getSavedLocation(): LocationSelection {
        return LocationSelection(
            city = getSavedCity(),
            country = getSavedCountry(),
            district = getSavedDistrict()
        )
    }
}

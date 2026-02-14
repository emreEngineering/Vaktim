package com.project.vaktim.data.local

import com.project.vaktim.domain.model.LocationSelection

interface LocationPreferences {
    fun saveLocation(location: LocationSelection)
    fun getSavedLocation(): LocationSelection
}

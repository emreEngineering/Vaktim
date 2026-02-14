package com.project.vaktim.domain.model

data class LocationSelection(
    val city: String,
    val country: String,
    val district: String = ""
) {
    fun toApiCity(): String = district.ifBlank { city }
}

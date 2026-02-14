package com.project.vaktim.data.model

/**
 * Unified prayer time model used across the entire app (UI, Service, Utils).
 */
data class PrayerTime(
    val name: String,
    val turkishName: String,
    val time: String
)

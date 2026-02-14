package com.project.vaktim.ui.state

import com.project.vaktim.core.AppDefaults
import com.project.vaktim.data.model.DailyQuote
import com.project.vaktim.data.model.PrayerTime

data class UiState(
    val isLoading: Boolean = false,
    val city: String = AppDefaults.DEFAULT_CITY,
    val country: String = AppDefaults.DEFAULT_COUNTRY,
    val district: String = "",
    val prayerTimes: List<PrayerTime> = emptyList(),
    val nextPrayer: PrayerTime? = null,
    val remainingTime: String = "",
    val hijriDate: String = "",
    val gregorianDate: String = "",
    val error: String? = null,
    val quote: DailyQuote? = null,
    val isQuoteLoading: Boolean = false,
    val quoteError: String? = null
)

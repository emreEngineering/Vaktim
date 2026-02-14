package com.project.vaktim.data.model

data class DailyQuote(
    val text: String,
    val source: String,
    val arabicText: String? = null
)

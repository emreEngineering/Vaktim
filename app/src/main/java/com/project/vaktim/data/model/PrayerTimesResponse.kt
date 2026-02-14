package com.project.vaktim.data.model

import com.google.gson.annotations.SerializedName

data class PrayerTimesResponse(
    @SerializedName("code")
    val code: Int,
    @SerializedName("status")
    val status: String,
    @SerializedName("data")
    val data: PrayerData
)

data class PrayerData(
    @SerializedName("timings")
    val timings: Timings,
    @SerializedName("date")
    val date: DateInfo,
    @SerializedName("meta")
    val meta: Meta
)

data class Timings(
    @SerializedName("Fajr")
    val fajr: String,
    @SerializedName("Sunrise")
    val sunrise: String,
    @SerializedName("Dhuhr")
    val dhuhr: String,
    @SerializedName("Asr")
    val asr: String,
    @SerializedName("Sunset")
    val sunset: String,
    @SerializedName("Maghrib")
    val maghrib: String,
    @SerializedName("Isha")
    val isha: String,
    @SerializedName("Imsak")
    val imsak: String,
    @SerializedName("Midnight")
    val midnight: String,
    @SerializedName("Firstthird")
    val firstthird: String?,
    @SerializedName("Lastthird")
    val lastthird: String?
)

data class DateInfo(
    @SerializedName("readable")
    val readable: String,
    @SerializedName("timestamp")
    val timestamp: String,
    @SerializedName("gregorian")
    val gregorian: GregorianDate,
    @SerializedName("hijri")
    val hijri: HijriDate
)

data class GregorianDate(
    @SerializedName("date")
    val date: String,
    @SerializedName("format")
    val format: String,
    @SerializedName("day")
    val day: String,
    @SerializedName("weekday")
    val weekday: Weekday,
    @SerializedName("month")
    val month: Month,
    @SerializedName("year")
    val year: String
)

data class HijriDate(
    @SerializedName("date")
    val date: String,
    @SerializedName("format")
    val format: String,
    @SerializedName("day")
    val day: String,
    @SerializedName("weekday")
    val weekday: Weekday,
    @SerializedName("month")
    val month: HijriMonth,
    @SerializedName("year")
    val year: String
)

data class Weekday(
    @SerializedName("en")
    val en: String
)

data class Month(
    @SerializedName("number")
    val number: Int,
    @SerializedName("en")
    val en: String
)

data class HijriMonth(
    @SerializedName("number")
    val number: Int,
    @SerializedName("en")
    val en: String,
    @SerializedName("ar")
    val ar: String
)

data class Meta(
    @SerializedName("latitude")
    val latitude: Double,
    @SerializedName("longitude")
    val longitude: Double,
    @SerializedName("timezone")
    val timezone: String
)

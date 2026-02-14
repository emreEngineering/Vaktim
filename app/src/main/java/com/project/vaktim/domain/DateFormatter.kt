package com.project.vaktim.domain

import com.project.vaktim.data.model.GregorianDate
import com.project.vaktim.data.model.HijriDate

class DateFormatter {

    private val turkishMonths = listOf(
        "Ocak", "Subat", "Mart", "Nisan", "Mayis", "Haziran",
        "Temmuz", "Agustos", "Eylul", "Ekim", "Kasim", "Aralik"
    )

    private val hijriMonths = listOf(
        "Muharrem", "Safer", "Rebiulevvel", "Rebiulahir",
        "Cemaziyelevvel", "Cemaziyelahir", "Recep", "Saban",
        "Ramazan", "Sevval", "Zilkade", "Zilhicce"
    )

    fun formatGregorian(date: GregorianDate): String {
        val monthName = turkishMonths.getOrElse(date.month.number - 1) { date.month.en }
        return "${date.day} $monthName ${date.year}"
    }

    fun formatHijri(date: HijriDate): String {
        val monthName = hijriMonths.getOrElse(date.month.number - 1) { date.month.en }
        return "${date.day} $monthName ${date.year}"
    }
}

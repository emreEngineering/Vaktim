package com.project.vaktim.util

object SurahNameResolver {

    private val names = listOf(
        "Fatiha", "Bakara", "Ali Imran", "Nisa", "Maide", "Enam", "Araf", "Enfal",
        "Tevbe", "Yunus", "Hud", "Yusuf", "Rad", "Ibrahim", "Hicr", "Nahl",
        "Isra", "Kehf", "Meryem", "Ta-Ha", "Enbiya", "Hac", "Muminun", "Nur",
        "Furkan", "Suara", "Neml", "Kasas", "Ankebut", "Rum", "Lokman", "Secde",
        "Ahzab", "Sebe", "Fatir", "Yasin", "Saffat", "Sad", "Zumer", "Mumin",
        "Fussilet", "Sura", "Zuhruf", "Duhan", "Casiye", "Ahkaf", "Muhammed", "Fetih",
        "Hucurat", "Kaf", "Zariyat", "Tur", "Necm", "Kamer", "Rahman", "Vakia",
        "Hadid", "Mucadele", "Hasr", "Mumtehine", "Saff", "Cuma", "Munafikun", "Tegabun",
        "Talak", "Tahrim", "Mulk", "Kalem", "Hakka", "Mearic", "Nuh", "Cin",
        "Muzzemmil", "Muddessir", "Kiyame", "Insan", "Murselat", "Nebe", "Naziat", "Abese",
        "Tekvir", "Infitar", "Mutaffifin", "Insikak", "Buruc", "Tarik", "Ala", "Gasiye",
        "Fecr", "Beled", "Sems", "Leyl", "Duha", "Insirah", "Tin", "Alak",
        "Kadr", "Beyyine", "Zilzal", "Adiyat", "Karia", "Tekasur", "Asr", "Humeze",
        "Fil", "Kureys", "Maun", "Kevser", "Kafirun", "Nasr", "Tebbet", "Ihlas",
        "Felak", "Nas"
    )

    fun resolve(number: Int): String {
        val name = names.getOrNull(number - 1) ?: number.toString()
        return "$name Suresi"
    }
}

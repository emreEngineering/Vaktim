package com.project.vaktim.data.model

import com.google.gson.annotations.SerializedName

// Quran API Response
data class QuranResponse(
    @SerializedName("verse")
    val verse: Verse
)

data class Verse(
    @SerializedName("id")
    val id: Int,
    @SerializedName("verse_number")
    val verseNumber: Int,
    @SerializedName("verse_key")
    val verseKey: String,
    @SerializedName("text_uthmani")
    val textUthmani: String? = null,
    @SerializedName("translations")
    val translations: List<Translation>
)

data class Translation(
    @SerializedName("id")
    val id: Int,
    @SerializedName("resource_id")
    val resourceId: Int,
    @SerializedName("text")
    val text: String
)


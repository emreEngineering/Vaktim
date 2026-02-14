package com.project.vaktim.data.repository

import com.project.vaktim.data.api.QuotesApi
import com.project.vaktim.data.api.RetrofitClient
import com.project.vaktim.data.model.DailyQuote
import com.project.vaktim.util.HtmlSanitizer
import com.project.vaktim.util.SurahNameResolver

class QuotesRepository(
    private val quranApi: QuotesApi = RetrofitClient.quotesApi
) : IQuotesRepository {

    override suspend fun getRandomVerse(): Result<DailyQuote> {
        return try {
            val response = quranApi.getRandomVerse()
            val translation = response.verse.translations.firstOrNull()
                ?: return Result.failure(IllegalStateException("Translation not found"))

            val cleanText = HtmlSanitizer.sanitize(translation.text)
            val verseParts = response.verse.verseKey.split(":")
            val surahNumber = verseParts.getOrNull(0)?.toIntOrNull() ?: 0
            val verseNumber = verseParts.getOrNull(1).orEmpty()

            Result.success(
                DailyQuote(
                    text = cleanText,
                    source = "${SurahNameResolver.resolve(surahNumber)}, $verseNumber. Ayet",
                    arabicText = response.verse.textUthmani
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

package com.project.vaktim.data.repository

import com.project.vaktim.data.api.QuotesApi
import com.project.vaktim.data.model.QuranResponse
import com.project.vaktim.data.model.Translation
import com.project.vaktim.data.model.Verse
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class QuotesRepositoryTest {

    @Test
    fun `maps and sanitizes verse response`() = runTest {
        val api = object : QuotesApi {
            override suspend fun getRandomVerse(translations: Int, fields: String): QuranResponse {
                return QuranResponse(
                    verse = Verse(
                        id = 1,
                        verseNumber = 255,
                        verseKey = "2:255",
                        textUthmani = "اللَّهُ لَا إِلَٰهَ إِلَّا هُوَ",
                        translations = listOf(
                            Translation(
                                id = 1,
                                resourceId = 77,
                                text = "Allah &quot;Hayy&quot; ve &lt;b&gt;Kayyum&lt;/b&gt;"
                            )
                        )
                    )
                )
            }
        }
        val repository = QuotesRepository(api)

        val result = repository.getRandomVerse()

        assertTrue(result.isSuccess)
        val quote = result.getOrThrow()
        assertEquals("Allah \"Hayy\" ve Kayyum", quote.text)
        assertEquals("Bakara Suresi, 255. Ayet", quote.source)
    }
}

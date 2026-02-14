package com.project.vaktim.data.api

import com.project.vaktim.data.model.QuranResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface QuotesApi {
    
    @GET("api/v4/verses/random")
    suspend fun getRandomVerse(
        @Query("translations") translations: Int = 77, // Turkish translation
        @Query("fields") fields: String = "text_uthmani" // Arabic text
    ): QuranResponse
}

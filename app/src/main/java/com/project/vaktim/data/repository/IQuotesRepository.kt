package com.project.vaktim.data.repository

import com.project.vaktim.data.model.DailyQuote

/**
 * Interface for quotes/verse data access (DIP — Dependency Inversion Principle).
 * Allows swapping implementations for testing or alternative data sources.
 */
interface IQuotesRepository {
    suspend fun getRandomVerse(): Result<DailyQuote>
}

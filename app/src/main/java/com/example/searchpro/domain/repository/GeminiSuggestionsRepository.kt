package com.example.searchpro.domain.repository

/**
 * Contract for obtaining AI-powered search suggestions and query expansions
 * via Gemini API.
 */
interface GeminiSuggestionsRepository {
    /**
     * Generates intelligent, relevant search suggestions for a user query.
     *
     * @param query The user's input search query
     * @param language The desired response language code (e.g., "es", "en", "fr")
     * @param maxSuggestions Maximum number of suggestions to return (default 4)
     * @return Result containing a list of suggested search terms or failure
     */
    suspend fun getSearchSuggestions(
        query: String,
        language: String = "es",
        maxSuggestions: Int = 4
    ): Result<List<String>>

    /**
     * Generates a concise AI summary of a web page's topic and content.
     */
    suspend fun summarizePage(
        title: String,
        url: String,
        contentSnippet: String = "",
        language: String = "es"
    ): Result<String>

    /**
     * Answers a user question in context of the current browsing session or general query.
     */
    suspend fun askAssistant(
        question: String,
        pageTitle: String? = null,
        pageUrl: String? = null,
        language: String = "es"
    ): Result<String>
}

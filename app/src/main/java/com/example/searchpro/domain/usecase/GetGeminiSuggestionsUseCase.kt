package com.example.searchpro.domain.usecase

import com.example.searchpro.domain.repository.GeminiSuggestionsRepository

/**
 * UseCase to retrieve AI-powered search suggestions for a given user query.
 */
class GetGeminiSuggestionsUseCase(
    private val geminiRepository: GeminiSuggestionsRepository
) {
    suspend operator fun invoke(
        query: String,
        language: String = "es",
        maxSuggestions: Int = 4
    ): Result<List<String>> {
        return geminiRepository.getSearchSuggestions(
            query = query,
            language = language,
            maxSuggestions = maxSuggestions
        )
    }
}

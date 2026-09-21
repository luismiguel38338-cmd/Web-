package com.example.searchpro.presentation.home

import com.example.searchpro.domain.model.SearchFilters
import com.example.searchpro.domain.model.SearchItem

data class HomeUiState(
    val query: String = "",
    val items: List<SearchItem> = emptyList(),
    val favoriteIds: Set<Long> = emptySet(),
    val categories: List<String> = emptyList(),
    val recentQueries: List<String> = emptyList(),
    val geminiSuggestions: List<String> = emptyList(),
    val isGeminiLoading: Boolean = false,
    val filters: SearchFilters = SearchFilters(),
    val isLoading: Boolean = false,
    val hasSearched: Boolean = false,
    val errorMessage: String? = null,
    val showSuggestionsPref: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val currentLanguage: String = "system"
)

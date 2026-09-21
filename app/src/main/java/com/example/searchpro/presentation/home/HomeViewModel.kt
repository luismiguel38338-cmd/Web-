package com.example.searchpro.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.searchpro.data.local.DataStoreManager
import com.example.searchpro.domain.model.DateRangeFilter
import com.example.searchpro.domain.model.SearchFilters
import com.example.searchpro.domain.model.SearchItem
import com.example.searchpro.domain.model.SortOrder
import com.example.searchpro.domain.repository.GeminiSuggestionsRepository
import com.example.searchpro.domain.repository.SearchRepository
import com.example.searchpro.domain.usecase.GetFavoritesUseCase
import com.example.searchpro.domain.usecase.GetGeminiSuggestionsUseCase
import com.example.searchpro.domain.usecase.ManageRecentSearchesUseCase
import com.example.searchpro.domain.usecase.SearchItemsUseCase
import com.example.searchpro.domain.usecase.ToggleFavoriteUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class HomeViewModel(
    private val searchRepository: SearchRepository,
    private val dataStoreManager: DataStoreManager,
    private val geminiRepository: GeminiSuggestionsRepository? = null,
    private val searchItemsUseCase: SearchItemsUseCase = SearchItemsUseCase(searchRepository),
    private val getFavoritesUseCase: GetFavoritesUseCase = GetFavoritesUseCase(searchRepository),
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase = ToggleFavoriteUseCase(searchRepository),
    private val manageRecentSearchesUseCase: ManageRecentSearchesUseCase = ManageRecentSearchesUseCase(searchRepository),
    private val getGeminiSuggestionsUseCase: GetGeminiSuggestionsUseCase? = geminiRepository?.let {
        GetGeminiSuggestionsUseCase(it)
    }
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val queryFlow = MutableStateFlow("")
    private val filtersFlow = MutableStateFlow(SearchFilters())

    init {
        loadInitialData()
        observePreferences()
        observeFavorites()
        observeRecentSearches()
        setupSearchDebounce()
        setupGeminiSuggestionsDebounce()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            try {
                val categories = searchRepository.getCategories()
                _uiState.update { it.copy(categories = categories) }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.localizedMessage) }
            }
        }
    }

    private fun observePreferences() {
        viewModelScope.launch {
            dataStoreManager.suggestionsFlow.collect { enabled ->
                _uiState.update { it.copy(showSuggestionsPref = enabled) }
            }
        }
        viewModelScope.launch {
            dataStoreManager.vibrationFlow.collect { enabled ->
                _uiState.update { it.copy(vibrationEnabled = enabled) }
            }
        }
        viewModelScope.launch {
            dataStoreManager.languageFlow.collect { lang ->
                _uiState.update { it.copy(currentLanguage = lang) }
            }
        }
    }

    private fun observeFavorites() {
        viewModelScope.launch {
            getFavoritesUseCase().collect { favorites ->
                val favIds = favorites.map { it.id }.toSet()
                _uiState.update { it.copy(favoriteIds = favIds) }
            }
        }
    }

    private fun observeRecentSearches() {
        viewModelScope.launch {
            manageRecentSearchesUseCase.getRecentSearches().collect { recent ->
                _uiState.update { it.copy(recentQueries = recent) }
            }
        }
    }

    private fun setupSearchDebounce() {
        viewModelScope.launch {
            combine(
                queryFlow.debounce(300).distinctUntilChanged(),
                filtersFlow
            ) { query, filters ->
                query to filters
            }.flatMapLatest { (query, filters) ->
                val hasQuery = query.isNotBlank()
                val hasActiveFilters = filters.hasActiveFilters

                _uiState.update {
                    it.copy(
                        query = query,
                        filters = filters,
                        isLoading = hasQuery || hasActiveFilters,
                        hasSearched = hasQuery || hasActiveFilters
                    )
                }

                if (!hasQuery && !hasActiveFilters) {
                    // When there is no query and no filters, don't show full result list by default
                    // but keep clean state
                    searchRepository.search("", filters)
                } else {
                    if (hasQuery) {
                        manageRecentSearchesUseCase.saveSearch(query)
                    }
                    searchRepository.search(query, filters)
                }
            }.collect { results ->
                _uiState.update {
                    it.copy(
                        items = results,
                        isLoading = false,
                        errorMessage = null
                    )
                }
            }
        }
    }

    fun onQueryChange(newQuery: String) {
        queryFlow.value = newQuery
        _uiState.update { it.copy(query = newQuery) }
    }

    fun onClearQuery() {
        queryFlow.value = ""
        _uiState.update { it.copy(query = "", hasSearched = it.filters.hasActiveFilters) }
    }

    fun onApplyFilters(newFilters: SearchFilters) {
        filtersFlow.value = newFilters
        _uiState.update { it.copy(filters = newFilters) }
    }

    fun onClearFilters() {
        val cleared = SearchFilters(isEnabled = false)
        filtersFlow.value = cleared
        _uiState.update { it.copy(filters = cleared) }
    }

    fun onRemoveCategoryFilter() {
        val updated = _uiState.value.filters.copy(category = null)
        filtersFlow.value = updated
        _uiState.update { it.copy(filters = updated) }
    }

    fun onRemoveDateFilter() {
        val updated = _uiState.value.filters.copy(dateRange = DateRangeFilter.ALL)
        filtersFlow.value = updated
        _uiState.update { it.copy(filters = updated) }
    }

    fun onRemoveStatusFilter() {
        val updated = _uiState.value.filters.copy(status = null)
        filtersFlow.value = updated
        _uiState.update { it.copy(filters = updated) }
    }

    fun onResetSortOrder() {
        val updated = _uiState.value.filters.copy(sortOrder = SortOrder.RELEVANCE)
        filtersFlow.value = updated
        _uiState.update { it.copy(filters = updated) }
    }

    private fun setupGeminiSuggestionsDebounce() {
        if (getGeminiSuggestionsUseCase == null) return

        viewModelScope.launch {
            queryFlow
                .debounce(450)
                .distinctUntilChanged()
                .collectLatest { query ->
                    val trimmed = query.trim()
                    // Only request suggestions for queries with at least 2 characters
                    // and when suggestion preferences are enabled
                    if (trimmed.length < 2 || !_uiState.value.showSuggestionsPref) {
                        _uiState.update {
                            it.copy(
                                geminiSuggestions = emptyList(),
                                isGeminiLoading = false
                            )
                        }
                        return@collectLatest
                    }

                    _uiState.update { it.copy(isGeminiLoading = true) }

                    val lang = _uiState.value.currentLanguage.ifBlank { "es" }
                    val result = getGeminiSuggestionsUseCase(
                        query = trimmed,
                        language = if (lang == "system") "es" else lang,
                        maxSuggestions = 4
                    )

                    result.onSuccess { suggestions ->
                        _uiState.update {
                            it.copy(
                                geminiSuggestions = suggestions,
                                isGeminiLoading = false
                            )
                        }
                    }.onFailure {
                        _uiState.update {
                            it.copy(
                                geminiSuggestions = emptyList(),
                                isGeminiLoading = false
                            )
                        }
                    }
                }
        }
    }

    fun onSelectCategory(category: String) {
        val currentCategory = _uiState.value.filters.category
        val newCategory = if (currentCategory == category) null else category
        val updated = _uiState.value.filters.copy(
            category = newCategory,
            isEnabled = true
        )
        filtersFlow.value = updated
        _uiState.update { it.copy(filters = updated) }
    }

    fun onToggleFavorite(item: SearchItem) {
        viewModelScope.launch {
            toggleFavoriteUseCase(item)
        }
    }

    class Factory(
        private val repository: SearchRepository,
        private val dataStoreManager: DataStoreManager,
        private val geminiRepository: GeminiSuggestionsRepository? = null
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HomeViewModel(repository, dataStoreManager, geminiRepository) as T
        }
    }
}

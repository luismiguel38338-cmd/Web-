package com.example.searchpro.presentation.recent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.searchpro.domain.model.RecentSearch
import com.example.searchpro.domain.repository.SearchRepository
import com.example.searchpro.domain.usecase.ManageRecentSearchesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RecentSearchesUiState(
    val searches: List<RecentSearch> = emptyList(),
    val isLoading: Boolean = true
)

class RecentSearchesViewModel(
    private val repository: SearchRepository,
    private val manageRecentSearchesUseCase: ManageRecentSearchesUseCase = ManageRecentSearchesUseCase(repository)
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecentSearchesUiState())
    val uiState: StateFlow<RecentSearchesUiState> = _uiState.asStateFlow()

    init {
        observeRecentSearches()
    }

    private fun observeRecentSearches() {
        viewModelScope.launch {
            manageRecentSearchesUseCase.getRecentSearchesDetailed().collect { list ->
                _uiState.update { it.copy(searches = list, isLoading = false) }
            }
        }
    }

    fun onDeleteItem(id: Long) {
        viewModelScope.launch {
            manageRecentSearchesUseCase.deleteSearch(id)
        }
    }

    fun onClearAll() {
        viewModelScope.launch {
            manageRecentSearchesUseCase.clearAll()
        }
    }

    class Factory(private val repository: SearchRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return RecentSearchesViewModel(repository) as T
        }
    }
}

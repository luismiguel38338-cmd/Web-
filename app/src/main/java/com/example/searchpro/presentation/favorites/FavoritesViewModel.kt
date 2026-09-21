package com.example.searchpro.presentation.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.searchpro.domain.model.SearchItem
import com.example.searchpro.domain.repository.SearchRepository
import com.example.searchpro.domain.usecase.GetFavoritesUseCase
import com.example.searchpro.domain.usecase.ToggleFavoriteUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FavoritesUiState(
    val favorites: List<SearchItem> = emptyList(),
    val isLoading: Boolean = true
)

class FavoritesViewModel(
    private val repository: SearchRepository,
    private val getFavoritesUseCase: GetFavoritesUseCase = GetFavoritesUseCase(repository),
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase = ToggleFavoriteUseCase(repository)
) : ViewModel() {

    private val _uiState = MutableStateFlow(FavoritesUiState())
    val uiState: StateFlow<FavoritesUiState> = _uiState.asStateFlow()

    init {
        observeFavorites()
    }

    private fun observeFavorites() {
        viewModelScope.launch {
            getFavoritesUseCase().collect { list ->
                _uiState.update { it.copy(favorites = list, isLoading = false) }
            }
        }
    }

    fun onRemoveFavorite(itemId: Long) {
        viewModelScope.launch {
            val item = _uiState.value.favorites.find { it.id == itemId }
            if (item != null) {
                toggleFavoriteUseCase(item)
            } else {
                repository.removeFavorite(itemId)
            }
        }
    }

    class Factory(private val repository: SearchRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return FavoritesViewModel(repository) as T
        }
    }
}

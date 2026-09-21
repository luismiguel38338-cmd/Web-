package com.example.searchpro.presentation.details

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

data class DetailsUiState(
    val item: SearchItem? = null,
    val isFavorite: Boolean = false,
    val isLoading: Boolean = true,
    val error: String? = null
)

class DetailsViewModel(
    private val itemId: Long,
    private val repository: SearchRepository,
    private val getFavoritesUseCase: GetFavoritesUseCase = GetFavoritesUseCase(repository),
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase = ToggleFavoriteUseCase(repository)
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetailsUiState())
    val uiState: StateFlow<DetailsUiState> = _uiState.asStateFlow()

    init {
        loadItem()
        observeFavoriteStatus()
    }

    private fun loadItem() {
        viewModelScope.launch {
            try {
                val item = repository.getById(itemId)
                _uiState.update { it.copy(item = item, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.localizedMessage, isLoading = false) }
            }
        }
    }

    private fun observeFavoriteStatus() {
        viewModelScope.launch {
            getFavoritesUseCase.isFavorite(itemId).collect { isFav ->
                _uiState.update { it.copy(isFavorite = isFav) }
            }
        }
    }

    fun onToggleFavorite() {
        val item = _uiState.value.item ?: return
        viewModelScope.launch {
            toggleFavoriteUseCase(item)
        }
    }

    class Factory(
        private val itemId: Long,
        private val repository: SearchRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return DetailsViewModel(itemId, repository) as T
        }
    }
}

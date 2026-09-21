package com.example.searchpro.domain.usecase

import com.example.searchpro.domain.model.SearchItem
import com.example.searchpro.domain.repository.SearchRepository

class ToggleFavoriteUseCase(private val repository: SearchRepository) {
    suspend operator fun invoke(item: SearchItem) {
        repository.toggleFavorite(item)
    }
}

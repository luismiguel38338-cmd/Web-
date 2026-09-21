package com.example.searchpro.domain.usecase

import com.example.searchpro.domain.model.SearchItem
import com.example.searchpro.domain.repository.SearchRepository
import kotlinx.coroutines.flow.Flow

class GetFavoritesUseCase(private val repository: SearchRepository) {
    operator fun invoke(): Flow<List<SearchItem>> = repository.getFavorites()
    fun isFavorite(itemId: Long): Flow<Boolean> = repository.isFavorite(itemId)
}

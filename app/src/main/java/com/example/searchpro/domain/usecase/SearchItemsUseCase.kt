package com.example.searchpro.domain.usecase

import com.example.searchpro.domain.model.SearchFilters
import com.example.searchpro.domain.model.SearchItem
import com.example.searchpro.domain.repository.SearchRepository
import kotlinx.coroutines.flow.Flow

class SearchItemsUseCase(private val repository: SearchRepository) {
    operator fun invoke(query: String, filters: SearchFilters): Flow<List<SearchItem>> {
        return repository.search(query = query, filters = filters)
    }
}

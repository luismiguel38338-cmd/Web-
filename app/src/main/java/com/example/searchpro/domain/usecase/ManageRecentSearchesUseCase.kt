package com.example.searchpro.domain.usecase

import com.example.searchpro.domain.model.RecentSearch
import com.example.searchpro.domain.repository.SearchRepository
import kotlinx.coroutines.flow.Flow

class ManageRecentSearchesUseCase(private val repository: SearchRepository) {
    fun getRecentSearches(): Flow<List<String>> = repository.getRecentSearches()
    fun getRecentSearchesDetailed(): Flow<List<RecentSearch>> = repository.getRecentSearchesDetailed()
    suspend fun saveSearch(query: String) = repository.saveSearch(query)
    suspend fun deleteSearch(id: Long) = repository.deleteRecentSearch(id)
    suspend fun deleteSearchByQuery(query: String) = repository.deleteRecentSearchByQuery(query)
    suspend fun clearAll() = repository.clearRecentSearches()
}

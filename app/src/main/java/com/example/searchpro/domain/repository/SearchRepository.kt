package com.example.searchpro.domain.repository

import com.example.searchpro.domain.model.RecentSearch
import com.example.searchpro.domain.model.SearchFilters
import com.example.searchpro.domain.model.SearchItem
import kotlinx.coroutines.flow.Flow

/**
 * Main repository contract for SearchPro operations.
 * Abstracts data access so local Room + Demo data can be swapped
 * with remote APIs or cloud databases seamlessly.
 */
interface SearchRepository {
    fun search(query: String, filters: SearchFilters): Flow<List<SearchItem>>
    suspend fun getById(id: Long): SearchItem?
    fun getRecentSearches(): Flow<List<String>>
    fun getRecentSearchesDetailed(): Flow<List<RecentSearch>>
    fun getFavorites(): Flow<List<SearchItem>>
    fun isFavorite(itemId: Long): Flow<Boolean>
    suspend fun addFavorite(item: SearchItem)
    suspend fun removeFavorite(itemId: Long)
    suspend fun toggleFavorite(item: SearchItem)
    suspend fun saveSearch(query: String)
    suspend fun deleteRecentSearch(id: Long)
    suspend fun deleteRecentSearchByQuery(query: String)
    suspend fun clearRecentSearches()
    suspend fun getCategories(): List<String>
    suspend fun getStatuses(): List<String>
}

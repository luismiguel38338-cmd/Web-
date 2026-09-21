package com.example.searchpro.data.repository

import com.example.searchpro.data.demo.DemoDataProvider
import com.example.searchpro.data.local.FavoriteItemDao
import com.example.searchpro.data.local.RecentSearchDao
import com.example.searchpro.data.local.RecentSearchEntity
import com.example.searchpro.data.local.toDomain
import com.example.searchpro.data.local.toFavoriteEntity
import com.example.searchpro.domain.model.DateRangeFilter
import com.example.searchpro.domain.model.RecentSearch
import com.example.searchpro.domain.model.SearchFilters
import com.example.searchpro.domain.model.SearchItem
import com.example.searchpro.domain.model.SortOrder
import com.example.searchpro.domain.repository.SearchRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.LocalDate

/**
 * Concrete implementation of SearchRepository.
 * Uses local Demo data for search results and Room for persistent favorites & history.
 * Designed to easily incorporate remote Retrofit calls in place of DemoDataProvider.
 */
class SearchRepositoryImpl(
    private val recentSearchDao: RecentSearchDao,
    private val favoriteItemDao: FavoriteItemDao
) : SearchRepository {

    // In-memory cache of demo data that can be reloaded or swapped with API responses
    private val inMemoryDataset: List<SearchItem> = DemoDataProvider.getSampleItems()

    override fun search(query: String, filters: SearchFilters): Flow<List<SearchItem>> = flow {
        val trimmedQuery = query.trim().lowercase()
        val now = LocalDate.now()

        val filtered = inMemoryDataset.filter { item ->
            val matchesQuery = if (trimmedQuery.isEmpty()) {
                // If query is blank, allow item if filters are applied, or return full catalog
                true
            } else {
                val matchesTitle = item.title.lowercase().contains(trimmedQuery)
                val matchesDesc = item.description.lowercase().contains(trimmedQuery)
                val matchesCategory = item.category.lowercase().contains(trimmedQuery)
                val matchesTags = item.tags.any { it.lowercase().contains(trimmedQuery) }
                matchesTitle || matchesDesc || matchesCategory || matchesTags
            }

            if (!matchesQuery) return@filter false

            // Apply filters if enabled
            if (filters.isEnabled) {
                // Category filter
                if (!filters.category.isNullOrBlank() &&
                    !item.category.equals(filters.category, ignoreCase = true)
                ) {
                    return@filter false
                }

                // Status filter
                if (!filters.status.isNullOrBlank() &&
                    !item.status.equals(filters.status, ignoreCase = true)
                ) {
                    return@filter false
                }

                // Date filter
                val matchesDate = when (filters.dateRange) {
                    DateRangeFilter.ALL -> true
                    DateRangeFilter.LAST_7_DAYS -> item.date.isAfter(now.minusDays(8))
                    DateRangeFilter.LAST_30_DAYS -> item.date.isAfter(now.minusDays(31))
                    DateRangeFilter.THIS_YEAR -> item.date.year == now.year
                }
                if (!matchesDate) return@filter false
            }

            true
        }

        // Calculate dynamic ranking score when query is present
        fun calculateScore(item: SearchItem): Double {
            if (trimmedQuery.isEmpty()) return item.relevance
            var score = item.relevance
            val lowerTitle = item.title.lowercase()
            if (lowerTitle == trimmedQuery) score += 10.0
            else if (lowerTitle.startsWith(trimmedQuery)) score += 5.0
            else if (lowerTitle.contains(trimmedQuery)) score += 3.0

            if (item.tags.any { it.lowercase() == trimmedQuery }) score += 4.0
            else if (item.tags.any { it.lowercase().contains(trimmedQuery) }) score += 2.0

            if (item.category.lowercase().contains(trimmedQuery)) score += 2.5
            if (item.description.lowercase().contains(trimmedQuery)) score += 1.0

            return score
        }

        val sorted = when (if (filters.isEnabled) filters.sortOrder else SortOrder.RELEVANCE) {
            SortOrder.RELEVANCE -> filtered.sortedByDescending { calculateScore(it) }
            SortOrder.DATE_NEWEST -> filtered.sortedByDescending { it.date }
            SortOrder.DATE_OLDEST -> filtered.sortedBy { it.date }
            SortOrder.ALPHABETICAL -> filtered.sortedBy { it.title.lowercase() }
        }

        emit(sorted)
    }.flowOn(Dispatchers.Default)

    override suspend fun getById(id: Long): SearchItem? = withContext(Dispatchers.IO) {
        inMemoryDataset.find { it.id == id } ?: favoriteItemDao.getById(id)?.toDomain()
    }

    override fun getRecentSearches(): Flow<List<String>> {
        return recentSearchDao.getRecentSearchQueries()
    }

    override fun getRecentSearchesDetailed(): Flow<List<RecentSearch>> {
        return recentSearchDao.getAllRecentSearches().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getFavorites(): Flow<List<SearchItem>> {
        return favoriteItemDao.getAllFavorites().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun isFavorite(itemId: Long): Flow<Boolean> {
        return favoriteItemDao.isFavorite(itemId)
    }

    override suspend fun addFavorite(item: SearchItem) = withContext(Dispatchers.IO) {
        favoriteItemDao.insertFavorite(item.toFavoriteEntity())
    }

    override suspend fun removeFavorite(itemId: Long) = withContext(Dispatchers.IO) {
        favoriteItemDao.deleteById(itemId)
    }

    override suspend fun toggleFavorite(item: SearchItem) = withContext(Dispatchers.IO) {
        val existing = favoriteItemDao.getById(item.id)
        if (existing != null) {
            favoriteItemDao.deleteById(item.id)
        } else {
            favoriteItemDao.insertFavorite(item.toFavoriteEntity())
        }
    }

    override suspend fun saveSearch(query: String) = withContext(Dispatchers.IO) {
        val cleaned = query.trim()
        if (cleaned.isNotBlank()) {
            recentSearchDao.deleteByQuery(cleaned)
            recentSearchDao.insertRecentSearch(
                RecentSearchEntity(query = cleaned, timestamp = System.currentTimeMillis())
            )
        }
    }

    override suspend fun deleteRecentSearch(id: Long) = withContext(Dispatchers.IO) {
        recentSearchDao.deleteById(id)
    }

    override suspend fun deleteRecentSearchByQuery(query: String) = withContext(Dispatchers.IO) {
        recentSearchDao.deleteByQuery(query)
    }

    override suspend fun clearRecentSearches() = withContext(Dispatchers.IO) {
        recentSearchDao.clearAll()
    }

    override suspend fun getCategories(): List<String> = withContext(Dispatchers.Default) {
        inMemoryDataset.map { it.category }.distinct().sorted()
    }

    override suspend fun getStatuses(): List<String> = withContext(Dispatchers.Default) {
        inMemoryDataset.map { it.status }.distinct().sorted()
    }
}

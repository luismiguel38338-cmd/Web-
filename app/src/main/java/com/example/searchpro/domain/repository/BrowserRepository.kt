package com.example.searchpro.domain.repository

import com.example.searchpro.domain.model.SearchEngine
import com.example.searchpro.domain.model.WebBookmark
import com.example.searchpro.domain.model.WebHistoryItem
import kotlinx.coroutines.flow.Flow

interface BrowserRepository {
    fun getBookmarks(): Flow<List<WebBookmark>>
    fun isBookmarked(url: String): Flow<Boolean>
    suspend fun addBookmark(title: String, url: String, faviconUrl: String? = null)
    suspend fun removeBookmarkByUrl(url: String)
    suspend fun deleteBookmarkById(id: Long)

    fun getHistory(limit: Int = 100): Flow<List<WebHistoryItem>>
    fun searchHistory(query: String): Flow<List<WebHistoryItem>>
    suspend fun recordVisit(title: String, url: String)
    suspend fun deleteHistoryById(id: Long)
    suspend fun clearHistory()

    fun getSearchEngine(): Flow<SearchEngine>
    suspend fun setSearchEngine(engine: SearchEngine)

    fun getDesktopMode(): Flow<Boolean>
    suspend fun setDesktopMode(enabled: Boolean)
}

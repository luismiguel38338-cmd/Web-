package com.example.searchpro.data.repository

import com.example.searchpro.data.local.DataStoreManager
import com.example.searchpro.data.local.WebBookmarkDao
import com.example.searchpro.data.local.WebBookmarkEntity
import com.example.searchpro.data.local.WebHistoryDao
import com.example.searchpro.data.local.WebHistoryEntity
import com.example.searchpro.domain.model.SearchEngine
import com.example.searchpro.domain.model.WebBookmark
import com.example.searchpro.domain.model.WebHistoryItem
import com.example.searchpro.domain.repository.BrowserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class BrowserRepositoryImpl(
    private val bookmarkDao: WebBookmarkDao,
    private val historyDao: WebHistoryDao,
    private val dataStoreManager: DataStoreManager
) : BrowserRepository {

    override fun getBookmarks(): Flow<List<WebBookmark>> {
        return bookmarkDao.getAllBookmarks().map { list ->
            list.map { entity ->
                WebBookmark(
                    id = entity.id,
                    title = entity.title,
                    url = entity.url,
                    faviconUrl = entity.faviconUrl,
                    createdAt = entity.createdAt
                )
            }
        }
    }

    override fun isBookmarked(url: String): Flow<Boolean> {
        return bookmarkDao.isBookmarked(url)
    }

    override suspend fun addBookmark(title: String, url: String, faviconUrl: String?) {
        val safeTitle = if (title.isBlank()) url else title
        bookmarkDao.insertBookmark(
            WebBookmarkEntity(
                title = safeTitle,
                url = url,
                faviconUrl = faviconUrl
            )
        )
    }

    override suspend fun removeBookmarkByUrl(url: String) {
        bookmarkDao.deleteBookmarkByUrl(url)
    }

    override suspend fun deleteBookmarkById(id: Long) {
        bookmarkDao.deleteBookmarkById(id)
    }

    override fun getHistory(limit: Int): Flow<List<WebHistoryItem>> {
        return historyDao.getRecentHistory(limit).map { list ->
            list.map { entity ->
                WebHistoryItem(
                    id = entity.id,
                    title = entity.title,
                    url = entity.url,
                    visitedAt = entity.visitedAt,
                    visitCount = entity.visitCount
                )
            }
        }
    }

    override fun searchHistory(query: String): Flow<List<WebHistoryItem>> {
        return historyDao.searchHistory(query).map { list ->
            list.map { entity ->
                WebHistoryItem(
                    id = entity.id,
                    title = entity.title,
                    url = entity.url,
                    visitedAt = entity.visitedAt,
                    visitCount = entity.visitCount
                )
            }
        }
    }

    override suspend fun recordVisit(title: String, url: String) {
        if (url == "about:blank" || url.isBlank()) return
        val existing = historyDao.getHistoryItemByUrl(url)
        val safeTitle = if (title.isBlank() || title == "about:blank") url else title
        if (existing != null) {
            historyDao.updateVisit(url = url, title = safeTitle, visitedAt = System.currentTimeMillis())
        } else {
            historyDao.insertHistory(
                WebHistoryEntity(
                    title = safeTitle,
                    url = url,
                    visitedAt = System.currentTimeMillis(),
                    visitCount = 1
                )
            )
        }
    }

    override suspend fun deleteHistoryById(id: Long) {
        historyDao.deleteHistoryById(id)
    }

    override suspend fun clearHistory() {
        historyDao.clearAllHistory()
    }

    override fun getSearchEngine(): Flow<SearchEngine> {
        return dataStoreManager.searchEngineFlow.map { name ->
            SearchEngine.fromDisplayName(name)
        }
    }

    override suspend fun setSearchEngine(engine: SearchEngine) {
        dataStoreManager.setSearchEngine(engine.displayName)
    }

    override fun getDesktopMode(): Flow<Boolean> {
        return dataStoreManager.desktopModeFlow
    }

    override suspend fun setDesktopMode(enabled: Boolean) {
        dataStoreManager.setDesktopMode(enabled)
    }
}

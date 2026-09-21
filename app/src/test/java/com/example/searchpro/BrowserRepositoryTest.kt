package com.example.searchpro

import com.example.searchpro.data.local.DataStoreManager
import com.example.searchpro.data.local.WebBookmarkDao
import com.example.searchpro.data.local.WebBookmarkEntity
import com.example.searchpro.data.local.WebHistoryDao
import com.example.searchpro.data.local.WebHistoryEntity
import com.example.searchpro.data.repository.BrowserRepositoryImpl
import com.example.searchpro.domain.model.SearchEngine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class BrowserRepositoryTest {

    private class FakeBookmarkDao : WebBookmarkDao {
        val list = mutableListOf<WebBookmarkEntity>()

        override fun getAllBookmarks() = flowOf(list.toList())
        override fun isBookmarked(url: String) = flowOf(list.any { it.url == url })
        override suspend fun insertBookmark(bookmark: WebBookmarkEntity): Long {
            list.add(bookmark)
            return bookmark.id
        }
        override suspend fun deleteBookmarkByUrl(url: String) {
            list.removeAll { it.url == url }
        }
        override suspend fun deleteBookmarkById(id: Long) {
            list.removeAll { it.id == id }
        }
        override suspend fun clearAllBookmarks() {
            list.clear()
        }
    }

    private class FakeHistoryDao : WebHistoryDao {
        val list = mutableListOf<WebHistoryEntity>()

        override fun getRecentHistory(limit: Int) = flowOf(list.take(limit))
        override suspend fun getHistoryItemByUrl(url: String) = list.find { it.url == url }
        override fun searchHistory(query: String) = flowOf(list.filter { it.title.contains(query) || it.url.contains(query) })
        override suspend fun insertHistory(history: WebHistoryEntity): Long {
            list.add(history)
            return history.id
        }
        override suspend fun updateVisit(url: String, title: String, visitedAt: Long) {
            val idx = list.indexOfFirst { it.url == url }
            if (idx >= 0) {
                val old = list[idx]
                list[idx] = old.copy(title = title, visitedAt = visitedAt, visitCount = old.visitCount + 1)
            }
        }
        override suspend fun deleteHistoryById(id: Long) {
            list.removeAll { it.id == id }
        }
        override suspend fun clearAllHistory() {
            list.clear()
        }
    }

    @Test
    fun `searchEngine builds correct query URLs`() {
        val google = SearchEngine.GOOGLE
        assertEquals(
            "https://www.google.com/search?q=android+compose",
            google.buildSearchUrl("android compose")
        )

        val ddg = SearchEngine.DUCKDUCKGO
        assertEquals(
            "https://duckduckgo.com/?q=kotlin",
            ddg.buildSearchUrl("kotlin")
        )

        val wiki = SearchEngine.WIKIPEDIA
        assertEquals(
            "https://en.wikipedia.org/wiki/Special:Search?search=Android",
            wiki.buildSearchUrl("Android")
        )
    }

    @Test
    fun `bookmarks add and delete functions work as expected`() = runBlocking {
        val fakeBookmarkDao = FakeBookmarkDao()
        val fakeHistoryDao = FakeHistoryDao()
        val context = RuntimeEnvironment.getApplication()
        val dataStoreManager = DataStoreManager(context)

        val repo = BrowserRepositoryImpl(fakeBookmarkDao, fakeHistoryDao, dataStoreManager)

        repo.addBookmark("Google", "https://google.com")
        val bookmarks = repo.getBookmarks().first()

        assertEquals(1, bookmarks.size)
        assertEquals("Google", bookmarks[0].title)
        assertEquals("https://google.com", bookmarks[0].url)

        repo.removeBookmarkByUrl("https://google.com")
        assertTrue(repo.getBookmarks().first().isEmpty())
    }

    @Test
    fun `history records visits properly`() = runBlocking {
        val fakeBookmarkDao = FakeBookmarkDao()
        val fakeHistoryDao = FakeHistoryDao()
        val context = RuntimeEnvironment.getApplication()
        val dataStoreManager = DataStoreManager(context)

        val repo = BrowserRepositoryImpl(fakeBookmarkDao, fakeHistoryDao, dataStoreManager)

        repo.recordVisit("Wikipedia", "https://wikipedia.org")
        val history = repo.getHistory().first()

        assertEquals(1, history.size)
        assertEquals("Wikipedia", history[0].title)

        // Ignore blank / about:blank visits
        repo.recordVisit("About", "about:blank")
        assertEquals(1, repo.getHistory().first().size)
    }
}

package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.searchpro.data.local.SearchDatabase
import com.example.searchpro.data.repository.SearchRepositoryImpl
import com.example.searchpro.domain.model.DateRangeFilter
import com.example.searchpro.domain.model.SearchFilters
import com.example.searchpro.domain.model.SearchItem
import com.example.searchpro.domain.model.SortOrder
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class SearchRepositoryTest {

    private lateinit var database: SearchDatabase
    private lateinit var repository: SearchRepositoryImpl

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, SearchDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = SearchRepositoryImpl(
            recentSearchDao = database.recentSearchDao(),
            favoriteItemDao = database.favoriteItemDao()
        )
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun `search with query is case insensitive and trimmed`() = runTest {
        val results1 = repository.search("   ARCHITECTURE   ", SearchFilters()).first()
        val results2 = repository.search("architecture", SearchFilters()).first()

        assertTrue(results1.isNotEmpty())
        assertEquals(results1.size, results2.size)
        assertEquals(results1.first().id, results2.first().id)
    }

    @Test
    fun `search with category filter returns matching category items`() = runTest {
        val filter = SearchFilters(
            category = "Desarrollo",
            isEnabled = true
        )
        val results = repository.search("", filter).first()

        assertTrue(results.isNotEmpty())
        assertTrue(results.all { it.category.equals("Desarrollo", ignoreCase = true) })
    }

    @Test
    fun `search with status filter returns only matching status`() = runTest {
        val filter = SearchFilters(
            status = "Activo",
            isEnabled = true
        )
        val results = repository.search("", filter).first()

        assertTrue(results.isNotEmpty())
        assertTrue(results.all { it.status.equals("Activo", ignoreCase = true) })
    }

    @Test
    fun `search with alphabetical sort returns ordered items`() = runTest {
        val filter = SearchFilters(
            sortOrder = SortOrder.ALPHABETICAL,
            isEnabled = true
        )
        val results = repository.search("guía", filter).first()

        if (results.size > 1) {
            val titles = results.map { it.title.lowercase() }
            assertEquals(titles.sorted(), titles)
        }
    }

    @Test
    fun `toggle favorite persists and removes favorite correctly`() = runTest {
        val testItem = SearchItem(
            id = 999L,
            title = "Test Item",
            description = "Test description",
            category = "Testing",
            date = LocalDate.now(),
            status = "Activo",
            tags = listOf("test"),
            relevance = 0.95
        )

        // Initially not favorite
        assertFalse(repository.isFavorite(999L).first())

        // Toggle on
        repository.toggleFavorite(testItem)
        assertTrue(repository.isFavorite(999L).first())
        val favorites = repository.getFavorites().first()
        assertTrue(favorites.any { it.id == 999L })

        // Toggle off
        repository.toggleFavorite(testItem)
        assertFalse(repository.isFavorite(999L).first())
    }

    @Test
    fun `recent searches records and clears history`() = runTest {
        repository.saveSearch("Jetpack Compose")
        repository.saveSearch("Kotlin Coroutines")

        val recent = repository.getRecentSearches().first()
        assertEquals(2, recent.size)
        assertEquals("Kotlin Coroutines", recent[0]) // latest first

        repository.clearRecentSearches()
        val emptyRecent = repository.getRecentSearches().first()
        assertTrue(emptyRecent.isEmpty())
    }
}

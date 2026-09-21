package com.example.searchpro

import android.app.Application
import com.example.searchpro.data.local.DataStoreManager
import com.example.searchpro.data.local.SearchDatabase
import com.example.searchpro.data.repository.BrowserRepositoryImpl
import com.example.searchpro.data.repository.GeminiSuggestionsRepositoryImpl
import com.example.searchpro.domain.repository.BrowserRepository
import com.example.searchpro.domain.repository.GeminiSuggestionsRepository

class SearchProApplication : Application() {

    lateinit var database: SearchDatabase
        private set

    lateinit var dataStoreManager: DataStoreManager
        private set

    lateinit var browserRepository: BrowserRepository
        private set

    lateinit var geminiRepository: GeminiSuggestionsRepository
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        database = SearchDatabase.getInstance(this)
        dataStoreManager = DataStoreManager(this)
        browserRepository = BrowserRepositoryImpl(
            bookmarkDao = database.webBookmarkDao(),
            historyDao = database.webHistoryDao(),
            dataStoreManager = dataStoreManager
        )
        geminiRepository = GeminiSuggestionsRepositoryImpl()
    }

    companion object {
        lateinit var instance: SearchProApplication
            private set
    }
}

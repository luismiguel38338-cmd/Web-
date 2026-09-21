package com.example.searchpro.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WebHistoryDao {

    @Query("SELECT * FROM web_history ORDER BY visitedAt DESC LIMIT :limit")
    fun getRecentHistory(limit: Int = 100): Flow<List<WebHistoryEntity>>

    @Query("SELECT * FROM web_history WHERE url = :url LIMIT 1")
    suspend fun getHistoryItemByUrl(url: String): WebHistoryEntity?

    @Query("SELECT * FROM web_history WHERE title LIKE '%' || :query || '%' OR url LIKE '%' || :query || '%' ORDER BY visitedAt DESC")
    fun searchHistory(query: String): Flow<List<WebHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: WebHistoryEntity): Long

    @Query("UPDATE web_history SET visitedAt = :visitedAt, visitCount = visitCount + 1, title = :title WHERE url = :url")
    suspend fun updateVisit(url: String, title: String, visitedAt: Long = System.currentTimeMillis())

    @Query("DELETE FROM web_history WHERE id = :id")
    suspend fun deleteHistoryById(id: Long)

    @Query("DELETE FROM web_history")
    suspend fun clearAllHistory()
}

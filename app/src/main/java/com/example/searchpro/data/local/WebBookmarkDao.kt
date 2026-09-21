package com.example.searchpro.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WebBookmarkDao {

    @Query("SELECT * FROM web_bookmarks ORDER BY createdAt DESC")
    fun getAllBookmarks(): Flow<List<WebBookmarkEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM web_bookmarks WHERE url = :url LIMIT 1)")
    fun isBookmarked(url: String): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: WebBookmarkEntity): Long

    @Query("DELETE FROM web_bookmarks WHERE url = :url")
    suspend fun deleteBookmarkByUrl(url: String)

    @Query("DELETE FROM web_bookmarks WHERE id = :id")
    suspend fun deleteBookmarkById(id: Long)

    @Query("DELETE FROM web_bookmarks")
    suspend fun clearAllBookmarks()
}

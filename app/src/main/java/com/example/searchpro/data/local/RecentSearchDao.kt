package com.example.searchpro.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RecentSearchDao {

    @Query("SELECT DISTINCT query FROM recent_searches ORDER BY timestamp DESC LIMIT 20")
    fun getRecentSearchQueries(): Flow<List<String>>

    @Query("SELECT * FROM recent_searches ORDER BY timestamp DESC LIMIT 50")
    fun getAllRecentSearches(): Flow<List<RecentSearchEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecentSearch(search: RecentSearchEntity)

    @Query("DELETE FROM recent_searches WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM recent_searches WHERE LOWER(query) = LOWER(:query)")
    suspend fun deleteByQuery(query: String)

    @Query("DELETE FROM recent_searches")
    suspend fun clearAll()
}

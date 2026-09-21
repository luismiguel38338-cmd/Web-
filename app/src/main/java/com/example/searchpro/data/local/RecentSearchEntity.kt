package com.example.searchpro.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.searchpro.domain.model.RecentSearch

@Entity(tableName = "recent_searches")
data class RecentSearchEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val query: String,
    val timestamp: Long = System.currentTimeMillis()
)

fun RecentSearchEntity.toDomain(): RecentSearch = RecentSearch(
    id = id,
    query = query,
    timestamp = timestamp
)

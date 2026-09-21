package com.example.searchpro.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "web_history")
data class WebHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val url: String,
    val visitedAt: Long = System.currentTimeMillis(),
    val visitCount: Int = 1
)

package com.example.searchpro.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "web_bookmarks")
data class WebBookmarkEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val url: String,
    val faviconUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

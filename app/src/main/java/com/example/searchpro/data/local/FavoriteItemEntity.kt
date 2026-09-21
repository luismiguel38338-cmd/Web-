package com.example.searchpro.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.searchpro.domain.model.SearchItem
import java.time.LocalDate

@Entity(tableName = "favorites")
data class FavoriteItemEntity(
    @PrimaryKey
    val id: Long,
    val title: String,
    val description: String,
    val category: String,
    val status: String,
    val dateEpochDay: Long,
    val tagsJoined: String,
    val relevance: Double,
    val savedAt: Long = System.currentTimeMillis()
)

fun SearchItem.toFavoriteEntity(): FavoriteItemEntity = FavoriteItemEntity(
    id = id,
    title = title,
    description = description,
    category = category,
    status = status,
    dateEpochDay = date.toEpochDay(),
    tagsJoined = tags.joinToString(separator = "||"),
    relevance = relevance
)

fun FavoriteItemEntity.toDomain(): SearchItem = SearchItem(
    id = id,
    title = title,
    description = description,
    category = category,
    status = status,
    date = LocalDate.ofEpochDay(dateEpochDay),
    tags = if (tagsJoined.isBlank()) emptyList() else tagsJoined.split("||"),
    relevance = relevance
)

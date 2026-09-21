package com.example.searchpro.domain.model

data class RecentSearch(
    val id: Long,
    val query: String,
    val timestamp: Long
)

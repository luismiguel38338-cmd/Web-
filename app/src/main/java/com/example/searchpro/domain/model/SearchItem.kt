package com.example.searchpro.domain.model

import java.time.LocalDate

/**
 * Core domain representation of a searchable item in SearchPro.
 * Designed to easily support local demo data or remote REST API data.
 */
data class SearchItem(
    val id: Long,
    val title: String,
    val description: String,
    val category: String,
    val status: String,
    val date: LocalDate,
    val tags: List<String>,
    val relevance: Double
)

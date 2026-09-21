package com.example.searchpro.domain.model

enum class SortOrder {
    RELEVANCE,
    DATE_NEWEST,
    DATE_OLDEST,
    ALPHABETICAL
}

enum class DateRangeFilter {
    ALL,
    LAST_7_DAYS,
    LAST_30_DAYS,
    THIS_YEAR
}

/**
 * Filter and sorting parameters for advanced multi-criteria searches.
 */
data class SearchFilters(
    val category: String? = null,
    val dateRange: DateRangeFilter = DateRangeFilter.ALL,
    val status: String? = null,
    val sortOrder: SortOrder = SortOrder.RELEVANCE,
    val isEnabled: Boolean = true
) {
    /**
     * Checks if any non-default filter condition is currently active.
     */
    val hasActiveFilters: Boolean
        get() = isEnabled && (!category.isNullOrBlank() ||
                dateRange != DateRangeFilter.ALL ||
                !status.isNullOrBlank() ||
                sortOrder != SortOrder.RELEVANCE)

    val activeCount: Int
        get() {
            if (!isEnabled) return 0
            var count = 0
            if (!category.isNullOrBlank()) count++
            if (dateRange != DateRangeFilter.ALL) count++
            if (!status.isNullOrBlank()) count++
            if (sortOrder != SortOrder.RELEVANCE) count++
            return count
        }
}

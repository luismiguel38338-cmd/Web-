package com.example.searchpro.presentation.search.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.searchpro.domain.model.DateRangeFilter
import com.example.searchpro.domain.model.SearchFilters
import com.example.searchpro.domain.model.SortOrder

@Composable
fun FilterChipsRow(
    filters: SearchFilters,
    onRemoveCategory: () -> Unit,
    onRemoveDateRange: () -> Unit,
    onRemoveStatus: () -> Unit,
    onResetSort: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!filters.hasActiveFilters) return

    val scrollState = rememberScrollState()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(vertical = 4.dp)
            .testTag("active_filter_chips_row"),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Category chip
        if (!filters.category.isNullOrBlank()) {
            FilterChip(
                selected = true,
                onClick = onRemoveCategory,
                label = { Text(text = "${stringResource(R.string.filters_category_label)}: ${filters.category}") },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.filters_clear_button),
                        modifier = Modifier.size(16.dp)
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                modifier = Modifier.testTag("chip_filter_category")
            )
        }

        // Date Range chip
        if (filters.dateRange != DateRangeFilter.ALL) {
            val dateLabel = when (filters.dateRange) {
                DateRangeFilter.ALL -> stringResource(R.string.filters_date_all)
                DateRangeFilter.LAST_7_DAYS -> stringResource(R.string.filters_date_last_7)
                DateRangeFilter.LAST_30_DAYS -> stringResource(R.string.filters_date_last_30)
                DateRangeFilter.THIS_YEAR -> stringResource(R.string.filters_date_this_year)
            }
            FilterChip(
                selected = true,
                onClick = onRemoveDateRange,
                label = { Text(text = dateLabel) },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.filters_clear_button),
                        modifier = Modifier.size(16.dp)
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onTertiaryContainer
                ),
                modifier = Modifier.testTag("chip_filter_date")
            )
        }

        // Status chip
        if (!filters.status.isNullOrBlank()) {
            val statusLabel = when (filters.status.lowercase()) {
                "activo", "active" -> stringResource(R.string.filters_status_active)
                "borrador", "draft" -> stringResource(R.string.filters_status_draft)
                "archivado", "archived" -> stringResource(R.string.filters_status_archived)
                "pendiente", "pending" -> stringResource(R.string.filters_status_pending)
                else -> filters.status
            }
            FilterChip(
                selected = true,
                onClick = onRemoveStatus,
                label = { Text(text = "${stringResource(R.string.filters_status_label)}: $statusLabel") },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.filters_clear_button),
                        modifier = Modifier.size(16.dp)
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                ),
                modifier = Modifier.testTag("chip_filter_status")
            )
        }

        // Sort Order chip
        if (filters.sortOrder != SortOrder.RELEVANCE) {
            val sortLabel = when (filters.sortOrder) {
                SortOrder.RELEVANCE -> stringResource(R.string.filters_sort_relevance)
                SortOrder.DATE_NEWEST -> stringResource(R.string.filters_sort_newest)
                SortOrder.DATE_OLDEST -> stringResource(R.string.filters_sort_oldest)
                SortOrder.ALPHABETICAL -> stringResource(R.string.filters_sort_alphabetical)
            }
            FilterChip(
                selected = true,
                onClick = onResetSort,
                label = { Text(text = "${stringResource(R.string.filters_sort_label)}: $sortLabel") },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.filters_clear_button),
                        modifier = Modifier.size(16.dp)
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    selectedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.testTag("chip_filter_sort")
            )
        }
    }
}

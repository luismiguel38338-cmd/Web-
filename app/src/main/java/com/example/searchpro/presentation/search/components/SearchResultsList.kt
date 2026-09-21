package com.example.searchpro.presentation.search.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.searchpro.domain.model.SearchItem
import com.example.searchpro.ui.components.EmptyStateView

@Composable
fun SearchResultsList(
    items: List<SearchItem>,
    favoriteIds: Set<Long>,
    isLoading: Boolean,
    errorMessage: String?,
    onCardClick: (Long) -> Unit,
    onFavoriteToggle: (SearchItem) -> Unit,
    onResetFilters: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("search_results_container")
    ) {
        when {
            isLoading -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("search_loading_view"),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(44.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.search_loading),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            errorMessage != null -> {
                EmptyStateView(
                    icon = Icons.Default.ErrorOutline,
                    title = stringResource(R.string.search_error_message),
                    description = errorMessage,
                    actionButtonText = stringResource(R.string.see_all),
                    onActionClick = onResetFilters
                )
            }

            items.isEmpty() -> {
                EmptyStateView(
                    icon = Icons.Default.SearchOff,
                    title = stringResource(R.string.search_no_results_title),
                    description = stringResource(R.string.search_no_results_desc),
                    actionButtonText = stringResource(R.string.filters_clear_button),
                    onActionClick = onResetFilters
                )
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("search_results_lazy_column"),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.results_count, items.size),
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.testTag("results_count_text")
                            )
                        }
                    }

                    items(items, key = { it.id }) { searchItem ->
                        SearchItemCard(
                            item = searchItem,
                            isFavorite = favoriteIds.contains(searchItem.id),
                            onCardClick = { onCardClick(searchItem.id) },
                            onFavoriteClick = { onFavoriteToggle(searchItem) }
                        )
                    }
                }
            }
        }
    }
}

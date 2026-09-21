package com.example.searchpro.presentation.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.searchpro.presentation.search.components.FilterChipsRow
import com.example.searchpro.presentation.search.components.SearchBarComponent
import com.example.searchpro.presentation.search.components.SearchResultsList
import com.example.searchpro.presentation.search.voice.rememberVoiceSearchController
import com.example.searchpro.ui.components.AppTopBar
import com.example.searchpro.ui.components.EmptyStateView
import com.example.searchpro.ui.components.rememberHapticHelper
import com.example.searchpro.ui.theme.FavoriteGold

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToSettings: () -> Unit,
    onNavigateToFilters: () -> Unit,
    onNavigateToDetails: (Long) -> Unit,
    onNavigateToRecent: () -> Unit,
    onNavigateToFavorites: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val hapticHelper = rememberHapticHelper(isEnabled = uiState.vibrationEnabled)

    val voiceController = rememberVoiceSearchController(
        onResult = { recognizedQuery ->
            hapticHelper.performClick()
            viewModel.onQueryChange(recognizedQuery)
        },
        currentLanguageCode = uiState.currentLanguage
    )

    Scaffold(
        topBar = {
            AppTopBar(
                title = stringResource(R.string.app_name),
                showSettingsButton = true,
                onSettingsClick = {
                    hapticHelper.performClick()
                    onNavigateToSettings()
                }
            )
        },
        modifier = modifier
            .fillMaxSize()
            .testTag("home_screen")
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .widthIn(max = 720.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Personalized Welcome greeting
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.home_greeting),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.testTag("home_greeting_text")
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Large visible Search Bar
            SearchBarComponent(
                query = uiState.query,
                onQueryChange = { newQuery ->
                    viewModel.onQueryChange(newQuery)
                },
                onClearClick = {
                    hapticHelper.performClick()
                    viewModel.onClearQuery()
                },
                onVoiceSearchClick = {
                    hapticHelper.performClick()
                    voiceController.startListening()
                },
                onFiltersClick = {
                    hapticHelper.performClick()
                    onNavigateToFilters()
                },
                activeFilterCount = uiState.filters.activeCount,
                suggestions = uiState.recentQueries.filter {
                    it.contains(uiState.query, ignoreCase = true) && it != uiState.query
                },
                geminiSuggestions = uiState.geminiSuggestions,
                isGeminiLoading = uiState.isGeminiLoading,
                showSuggestions = uiState.showSuggestionsPref,
                onSuggestionSelected = { selected ->
                    hapticHelper.performClick()
                    viewModel.onQueryChange(selected)
                }
            )

            // Active Filter Chips
            FilterChipsRow(
                filters = uiState.filters,
                onRemoveCategory = {
                    hapticHelper.performClick()
                    viewModel.onRemoveCategoryFilter()
                },
                onRemoveDateRange = {
                    hapticHelper.performClick()
                    viewModel.onRemoveDateFilter()
                },
                onRemoveStatus = {
                    hapticHelper.performClick()
                    viewModel.onRemoveStatusFilter()
                },
                onResetSort = {
                    hapticHelper.performClick()
                    viewModel.onResetSortOrder()
                }
            )

            // Quick Access Cards (Recent Searches, Favorites)
            QuickAccessRow(
                onRecentClick = {
                    hapticHelper.performClick()
                    onNavigateToRecent()
                },
                onFavoritesClick = {
                    hapticHelper.performClick()
                    onNavigateToFavorites()
                },
                favoriteCount = uiState.favoriteIds.size
            )

            // Categories quick filter row
            if (uiState.categories.isNotEmpty()) {
                CategoriesQuickRow(
                    categories = uiState.categories,
                    selectedCategory = uiState.filters.category,
                    onCategoryClick = { cat ->
                        hapticHelper.performClick()
                        viewModel.onSelectCategory(cat)
                    }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Main Content Area: Empty Initial Welcome vs Results List
            if (!uiState.hasSearched && uiState.query.isBlank() && !uiState.filters.hasActiveFilters) {
                EmptyStateView(
                    icon = Icons.Default.Search,
                    title = stringResource(R.string.home_empty_title),
                    description = stringResource(R.string.home_empty_description),
                    modifier = Modifier.weight(1f)
                )
            } else {
                SearchResultsList(
                    items = uiState.items,
                    favoriteIds = uiState.favoriteIds,
                    isLoading = uiState.isLoading,
                    errorMessage = uiState.errorMessage,
                    onCardClick = { itemId ->
                        hapticHelper.performClick()
                        onNavigateToDetails(itemId)
                    },
                    onFavoriteToggle = { item ->
                        hapticHelper.performClick()
                        viewModel.onToggleFavorite(item)
                    },
                    onResetFilters = {
                        hapticHelper.performClick()
                        viewModel.onClearFilters()
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun QuickAccessRow(
    onRecentClick: () -> Unit,
    onFavoritesClick: () -> Unit,
    favoriteCount: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        QuickActionCard(
            title = stringResource(R.string.quick_access_recent),
            icon = Icons.Default.History,
            iconTint = MaterialTheme.colorScheme.primary,
            onClick = onRecentClick,
            modifier = Modifier.weight(1f)
        )

        QuickActionCard(
            title = stringResource(R.string.quick_access_favorites),
            subtitle = if (favoriteCount > 0) "($favoriteCount)" else null,
            icon = Icons.Default.Star,
            iconTint = FavoriteGold,
            onClick = onFavoritesClick,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun QuickActionCard(
    title: String,
    icon: ImageVector,
    iconTint: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        ),
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .testTag("quick_action_${title.lowercase().replace(" ", "_")}")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (subtitle != null) "$title $subtitle" else title,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun CategoriesQuickRow(
    categories: List<String>,
    selectedCategory: String?,
    onCategoryClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        categories.forEach { category ->
            val isSelected = category.equals(selectedCategory, ignoreCase = true)
            FilterChip(
                selected = isSelected,
                onClick = { onCategoryClick(category) },
                label = {
                    Text(
                        text = category,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp)
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Category,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                modifier = Modifier.testTag("quick_category_$category")
            )
        }
    }
}

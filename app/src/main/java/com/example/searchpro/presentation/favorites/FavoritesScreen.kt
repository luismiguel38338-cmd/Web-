package com.example.searchpro.presentation.favorites

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.searchpro.presentation.search.components.SearchItemCard
import com.example.searchpro.ui.components.AppTopBar
import com.example.searchpro.ui.components.EmptyStateView
import com.example.searchpro.ui.components.rememberHapticHelper

@Composable
fun FavoritesScreen(
    viewModel: FavoritesViewModel,
    onNavigateToDetails: (Long) -> Unit,
    onNavigateBack: () -> Unit,
    vibrationEnabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val hapticHelper = rememberHapticHelper(isEnabled = vibrationEnabled)

    Scaffold(
        topBar = {
            AppTopBar(
                title = stringResource(R.string.favorites_title),
                showBackButton = true,
                onBackClick = {
                    hapticHelper.performClick()
                    onNavigateBack()
                }
            )
        },
        modifier = modifier
            .fillMaxSize()
            .testTag("favorites_screen")
    ) { innerPadding ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            uiState.favorites.isEmpty() -> {
                EmptyStateView(
                    icon = Icons.Default.Star,
                    title = stringResource(R.string.favorites_empty_title),
                    description = stringResource(R.string.favorites_empty_desc),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                )
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.favorites, key = { it.id }) { item ->
                        SearchItemCard(
                            item = item,
                            isFavorite = true,
                            onCardClick = {
                                hapticHelper.performClick()
                                onNavigateToDetails(item.id)
                            },
                            onFavoriteClick = {
                                hapticHelper.performClick()
                                viewModel.onRemoveFavorite(item.id)
                            }
                        )
                    }
                }
            }
        }
    }
}

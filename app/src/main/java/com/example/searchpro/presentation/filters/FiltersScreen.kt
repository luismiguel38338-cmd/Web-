package com.example.searchpro.presentation.filters

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.searchpro.domain.model.DateRangeFilter
import com.example.searchpro.domain.model.SearchFilters
import com.example.searchpro.domain.model.SortOrder
import com.example.searchpro.ui.components.AppTopBar
import com.example.searchpro.ui.components.rememberHapticHelper

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FiltersScreen(
    currentFilters: SearchFilters,
    availableCategories: List<String>,
    onApply: (SearchFilters) -> Unit,
    onNavigateBack: () -> Unit,
    vibrationEnabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    val hapticHelper = rememberHapticHelper(isEnabled = vibrationEnabled)

    var selectedCategory by remember { mutableStateOf(currentFilters.category) }
    var selectedDateRange by remember { mutableStateOf(currentFilters.dateRange) }
    var selectedStatus by remember { mutableStateOf(currentFilters.status) }
    var selectedSortOrder by remember { mutableStateOf(currentFilters.sortOrder) }
    var isFiltersEnabled by remember { mutableStateOf(currentFilters.isEnabled) }

    val availableStatuses = listOf("Activo", "Borrador", "Pendiente", "Archivado")
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            AppTopBar(
                title = stringResource(R.string.filters_title),
                showBackButton = true,
                onBackClick = {
                    hapticHelper.performClick()
                    onNavigateBack()
                }
            )
        },
        bottomBar = {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            hapticHelper.performClick()
                            selectedCategory = null
                            selectedDateRange = DateRangeFilter.ALL
                            selectedStatus = null
                            selectedSortOrder = SortOrder.RELEVANCE
                            isFiltersEnabled = false
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("clear_filters_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.RestartAlt,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 6.dp)
                        )
                        Text(text = stringResource(R.string.filters_clear_button))
                    }

                    Button(
                        onClick = {
                            hapticHelper.performClick()
                            val newFilters = SearchFilters(
                                category = selectedCategory,
                                dateRange = selectedDateRange,
                                status = selectedStatus,
                                sortOrder = selectedSortOrder,
                                isEnabled = isFiltersEnabled
                            )
                            onApply(newFilters)
                            onNavigateBack()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("apply_filters_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 6.dp)
                        )
                        Text(text = stringResource(R.string.filters_apply_button))
                    }
                }
            }
        },
        modifier = modifier
            .fillMaxSize()
            .testTag("filters_screen")
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .widthIn(max = 720.dp)
        ) {
            // Master toggle: Activar o desactivar filtros
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.filters_active_toggle),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Switch(
                        checked = isFiltersEnabled,
                        onCheckedChange = {
                            hapticHelper.performClick()
                            isFiltersEnabled = it
                        },
                        modifier = Modifier.testTag("filters_master_switch")
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Section 1: Categoría
            SectionTitle(title = stringResource(R.string.filters_category_label))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                FilterChip(
                    selected = selectedCategory == null,
                    onClick = {
                        hapticHelper.performClick()
                        selectedCategory = null
                        isFiltersEnabled = true
                    },
                    label = { Text(text = stringResource(R.string.filters_category_all)) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.testTag("filter_category_all")
                )

                availableCategories.forEach { cat ->
                    val isSelected = selectedCategory.equals(cat, ignoreCase = true)
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            hapticHelper.performClick()
                            selectedCategory = if (isSelected) null else cat
                            isFiltersEnabled = true
                        },
                        label = { Text(text = cat) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier.testTag("filter_category_$cat")
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(20.dp))

            // Section 2: Fecha o rango de fechas
            SectionTitle(title = stringResource(R.string.filters_date_label))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                DateRangeFilter.values().forEach { range ->
                    val label = when (range) {
                        DateRangeFilter.ALL -> stringResource(R.string.filters_date_all)
                        DateRangeFilter.LAST_7_DAYS -> stringResource(R.string.filters_date_last_7)
                        DateRangeFilter.LAST_30_DAYS -> stringResource(R.string.filters_date_last_30)
                        DateRangeFilter.THIS_YEAR -> stringResource(R.string.filters_date_this_year)
                    }
                    FilterChip(
                        selected = selectedDateRange == range,
                        onClick = {
                            hapticHelper.performClick()
                            selectedDateRange = range
                            isFiltersEnabled = true
                        },
                        label = { Text(text = label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.tertiaryContainer
                        ),
                        modifier = Modifier.testTag("filter_date_${range.name}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(20.dp))

            // Section 3: Estado
            SectionTitle(title = stringResource(R.string.filters_status_label))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                FilterChip(
                    selected = selectedStatus == null,
                    onClick = {
                        hapticHelper.performClick()
                        selectedStatus = null
                        isFiltersEnabled = true
                    },
                    label = { Text(text = stringResource(R.string.filters_status_all)) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer
                    ),
                    modifier = Modifier.testTag("filter_status_all")
                )

                availableStatuses.forEach { status ->
                    val isSelected = selectedStatus.equals(status, ignoreCase = true)
                    val label = when (status.lowercase()) {
                        "activo" -> stringResource(R.string.filters_status_active)
                        "borrador" -> stringResource(R.string.filters_status_draft)
                        "archivado" -> stringResource(R.string.filters_status_archived)
                        "pendiente" -> stringResource(R.string.filters_status_pending)
                        else -> status
                    }
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            hapticHelper.performClick()
                            selectedStatus = if (isSelected) null else status
                            isFiltersEnabled = true
                        },
                        label = { Text(text = label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer
                        ),
                        modifier = Modifier.testTag("filter_status_$status")
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(20.dp))

            // Section 4: Criterio de Ordenamiento
            SectionTitle(title = stringResource(R.string.filters_sort_label))
            Column(modifier = Modifier.fillMaxWidth()) {
                SortOptionRow(
                    label = stringResource(R.string.filters_sort_relevance),
                    selected = selectedSortOrder == SortOrder.RELEVANCE,
                    onClick = {
                        hapticHelper.performClick()
                        selectedSortOrder = SortOrder.RELEVANCE
                    }
                )
                SortOptionRow(
                    label = stringResource(R.string.filters_sort_newest),
                    selected = selectedSortOrder == SortOrder.DATE_NEWEST,
                    onClick = {
                        hapticHelper.performClick()
                        selectedSortOrder = SortOrder.DATE_NEWEST
                        isFiltersEnabled = true
                    }
                )
                SortOptionRow(
                    label = stringResource(R.string.filters_sort_oldest),
                    selected = selectedSortOrder == SortOrder.DATE_OLDEST,
                    onClick = {
                        hapticHelper.performClick()
                        selectedSortOrder = SortOrder.DATE_OLDEST
                        isFiltersEnabled = true
                    }
                )
                SortOptionRow(
                    label = stringResource(R.string.filters_sort_alphabetical),
                    selected = selectedSortOrder == SortOrder.ALPHABETICAL,
                    onClick = {
                        hapticHelper.performClick()
                        selectedSortOrder = SortOrder.ALPHABETICAL
                        isFiltersEnabled = true
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(bottom = 12.dp)
    )
}

@Composable
private fun SortOptionRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.RadioButton
            )
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = null
        )
        Spacer(modifier = Modifier.padding(start = 12.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

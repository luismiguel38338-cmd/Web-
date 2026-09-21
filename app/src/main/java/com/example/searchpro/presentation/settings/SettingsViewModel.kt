package com.example.searchpro.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.searchpro.data.local.DataStoreManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsUiState(
    val language: String = "system",
    val theme: String = "system",
    val suggestionsEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true
)

class SettingsViewModel(
    private val dataStoreManager: DataStoreManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        observeSettings()
    }

    private fun observeSettings() {
        viewModelScope.launch {
            dataStoreManager.languageFlow.collect { lang ->
                _uiState.update { it.copy(language = lang) }
            }
        }
        viewModelScope.launch {
            dataStoreManager.themeFlow.collect { theme ->
                _uiState.update { it.copy(theme = theme) }
            }
        }
        viewModelScope.launch {
            dataStoreManager.suggestionsFlow.collect { suggestions ->
                _uiState.update { it.copy(suggestionsEnabled = suggestions) }
            }
        }
        viewModelScope.launch {
            dataStoreManager.vibrationFlow.collect { vibration ->
                _uiState.update { it.copy(vibrationEnabled = vibration) }
            }
        }
    }

    fun setLanguage(languageCode: String) {
        viewModelScope.launch {
            dataStoreManager.setLanguage(languageCode)
        }
    }

    fun setTheme(themeName: String) {
        viewModelScope.launch {
            dataStoreManager.setTheme(themeName)
        }
    }

    fun setSuggestionsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            dataStoreManager.setSuggestionsEnabled(enabled)
        }
    }

    fun setVibrationEnabled(enabled: Boolean) {
        viewModelScope.launch {
            dataStoreManager.setVibrationEnabled(enabled)
        }
    }

    class Factory(private val dataStoreManager: DataStoreManager) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SettingsViewModel(dataStoreManager) as T
        }
    }
}

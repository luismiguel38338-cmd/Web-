package com.example.searchpro.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "searchpro_preferences")

class DataStoreManager(private val context: Context) {

    companion object {
        val KEY_LANGUAGE = stringPreferencesKey("pref_language")
        val KEY_THEME = stringPreferencesKey("pref_theme")
        val KEY_SEARCH_ENGINE = stringPreferencesKey("pref_search_engine")
        val KEY_DESKTOP_MODE = booleanPreferencesKey("pref_desktop_mode")
        val KEY_SUGGESTIONS = booleanPreferencesKey("pref_suggestions")
        val KEY_VIBRATION = booleanPreferencesKey("pref_vibration")
    }

    val languageFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_LANGUAGE] ?: "system"
    }

    val themeFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_THEME] ?: "system"
    }

    val searchEngineFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_SEARCH_ENGINE] ?: "Google"
    }

    val desktopModeFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_DESKTOP_MODE] ?: false
    }

    val suggestionsFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_SUGGESTIONS] ?: true
    }

    val vibrationFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_VIBRATION] ?: true
    }

    suspend fun setLanguage(languageCode: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_LANGUAGE] = languageCode
        }
    }

    suspend fun setTheme(themeName: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_THEME] = themeName
        }
    }

    suspend fun setSearchEngine(engine: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_SEARCH_ENGINE] = engine
        }
    }

    suspend fun setDesktopMode(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_DESKTOP_MODE] = enabled
        }
    }

    suspend fun setSuggestionsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_SUGGESTIONS] = enabled
        }
    }

    suspend fun setVibrationEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_VIBRATION] = enabled
        }
    }
}

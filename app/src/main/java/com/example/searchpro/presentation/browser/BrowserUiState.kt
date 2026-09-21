package com.example.searchpro.presentation.browser

import com.example.searchpro.domain.model.SearchEngine
import com.example.searchpro.domain.model.WebBookmark
import com.example.searchpro.domain.model.WebHistoryItem
import com.example.searchpro.domain.model.WebTab

data class AiChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sender: String, // "user" or "gemini"
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class BrowserUiState(
    val tabs: List<WebTab> = listOf(WebTab()),
    val activeTabId: String = tabs.first().id,
    val omniboxText: String = "",
    val isOmniboxFocused: Boolean = false,
    val searchEngine: SearchEngine = SearchEngine.GOOGLE,
    val isDesktopMode: Boolean = false,
    val bookmarks: List<WebBookmark> = emptyList(),
    val history: List<WebHistoryItem> = emptyList(),
    val historyFilterQuery: String = "",
    val geminiSuggestions: List<String> = emptyList(),
    val isGeminiSuggestionsLoading: Boolean = false,
    // Sheets & Dialogs
    val isTabsSheetVisible: Boolean = false,
    val isGeminiSheetVisible: Boolean = false,
    val isBookmarksHistoryVisible: Boolean = false,
    val bookmarksHistoryTab: Int = 0, // 0 = Bookmarks, 1 = History
    val isSettingsDialogVisible: Boolean = false,
    // Gemini Assistant
    val pageSummary: String? = null,
    val isPageSummaryLoading: Boolean = false,
    val aiChatMessages: List<AiChatMessage> = emptyList(),
    val isAiChatLoading: Boolean = false,
    // User Settings
    val appLanguage: String = "system",
    val appTheme: String = "system",
    val vibrationEnabled: Boolean = true,
    val suggestionsEnabled: Boolean = true
) {
    val activeTab: WebTab
        get() = tabs.find { it.id == activeTabId } ?: tabs.firstOrNull() ?: WebTab()

    val canGoBack: Boolean
        get() = activeTab.canGoBack

    val canGoForward: Boolean
        get() = activeTab.canGoForward

    val isLoading: Boolean
        get() = activeTab.isLoading

    val progress: Int
        get() = activeTab.progress

    val currentUrl: String
        get() = activeTab.url

    val currentTitle: String
        get() = activeTab.title

    val isCurrentBookmarked: Boolean
        get() = bookmarks.any { it.url == activeTab.url }
}

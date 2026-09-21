package com.example.searchpro.presentation.browser

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.searchpro.data.local.DataStoreManager
import com.example.searchpro.domain.model.SearchEngine
import com.example.searchpro.domain.model.WebBookmark
import com.example.searchpro.domain.model.WebTab
import com.example.searchpro.domain.repository.BrowserRepository
import com.example.searchpro.domain.repository.GeminiSuggestionsRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface BrowserNavCommand {
    data class LoadUrl(val url: String) : BrowserNavCommand
    data object GoBack : BrowserNavCommand
    data object GoForward : BrowserNavCommand
    data object Reload : BrowserNavCommand
    data object Stop : BrowserNavCommand
}

class BrowserViewModel(
    private val browserRepository: BrowserRepository,
    private val geminiRepository: GeminiSuggestionsRepository,
    private val dataStoreManager: DataStoreManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(BrowserUiState())
    val uiState: StateFlow<BrowserUiState> = _uiState.asStateFlow()

    private val _navCommands = MutableSharedFlow<BrowserNavCommand>(extraBufferCapacity = 10)
    val navCommands: SharedFlow<BrowserNavCommand> = _navCommands.asSharedFlow()

    private val omniboxInputFlow = MutableStateFlow("")
    private var suggestionsJob: Job? = null

    init {
        observeDataStore()
        observeBookmarks()
        observeHistory()
        setupSuggestionsDebounce()
    }

    private fun observeDataStore() {
        viewModelScope.launch {
            dataStoreManager.languageFlow.collectLatest { lang ->
                _uiState.update { it.copy(appLanguage = lang) }
            }
        }
        viewModelScope.launch {
            dataStoreManager.themeFlow.collectLatest { theme ->
                _uiState.update { it.copy(appTheme = theme) }
            }
        }
        viewModelScope.launch {
            dataStoreManager.searchEngineFlow.collectLatest { engineName ->
                _uiState.update { it.copy(searchEngine = SearchEngine.fromDisplayName(engineName)) }
            }
        }
        viewModelScope.launch {
            dataStoreManager.desktopModeFlow.collectLatest { desktop ->
                _uiState.update { it.copy(isDesktopMode = desktop) }
            }
        }
        viewModelScope.launch {
            dataStoreManager.suggestionsFlow.collectLatest { enabled ->
                _uiState.update { it.copy(suggestionsEnabled = enabled) }
            }
        }
        viewModelScope.launch {
            dataStoreManager.vibrationFlow.collectLatest { enabled ->
                _uiState.update { it.copy(vibrationEnabled = enabled) }
            }
        }
    }

    private fun observeBookmarks() {
        viewModelScope.launch {
            browserRepository.getBookmarks().collectLatest { bookmarksList ->
                _uiState.update { it.copy(bookmarks = bookmarksList) }
            }
        }
    }

    private fun observeHistory() {
        viewModelScope.launch {
            browserRepository.getHistory(150).collectLatest { historyList ->
                _uiState.update { it.copy(history = historyList) }
            }
        }
    }

    private fun setupSuggestionsDebounce() {
        suggestionsJob = viewModelScope.launch {
            omniboxInputFlow
                .debounce(400)
                .distinctUntilChanged()
                .collectLatest { input ->
                    val trimmed = input.trim()
                    if (trimmed.length < 2 || !_uiState.value.suggestionsEnabled || isDirectUrl(trimmed)) {
                        _uiState.update {
                            it.copy(
                                geminiSuggestions = emptyList(),
                                isGeminiSuggestionsLoading = false
                            )
                        }
                        return@collectLatest
                    }

                    _uiState.update { it.copy(isGeminiSuggestionsLoading = true) }
                    val lang = _uiState.value.appLanguage.ifBlank { "es" }
                    val result = geminiRepository.getSearchSuggestions(
                        query = trimmed,
                        language = if (lang == "system") "es" else lang,
                        maxSuggestions = 5
                    )

                    result.onSuccess { suggestions ->
                        _uiState.update {
                            it.copy(
                                geminiSuggestions = suggestions,
                                isGeminiSuggestionsLoading = false
                            )
                        }
                    }.onFailure {
                        _uiState.update {
                            it.copy(
                                geminiSuggestions = emptyList(),
                                isGeminiSuggestionsLoading = false
                            )
                        }
                    }
                }
        }
    }

    // Omnibox / URL handling
    fun onOmniboxTextChanged(newText: String) {
        _uiState.update { it.copy(omniboxText = newText) }
        omniboxInputFlow.value = newText
    }

    fun onOmniboxFocusChanged(focused: Boolean) {
        _uiState.update { state ->
            state.copy(
                isOmniboxFocused = focused,
                omniboxText = if (focused) {
                    if (state.activeTab.isHomePage) "" else state.activeTab.url
                } else {
                    state.omniboxText
                }
            )
        }
    }

    fun loadUrlOrSearch(input: String) {
        val trimmed = input.trim()
        if (trimmed.isBlank()) return

        val targetUrl = formatUrlOrQuery(trimmed, _uiState.value.searchEngine)
        _uiState.update { state ->
            val updatedTabs = state.tabs.map { tab ->
                if (tab.id == state.activeTabId) {
                    tab.copy(url = targetUrl, title = targetUrl, isLoading = true, progress = 10)
                } else tab
            }
            state.copy(
                tabs = updatedTabs,
                omniboxText = targetUrl,
                isOmniboxFocused = false,
                geminiSuggestions = emptyList()
            )
        }
        _navCommands.tryEmit(BrowserNavCommand.LoadUrl(targetUrl))
    }

    private fun isDirectUrl(input: String): Boolean {
        if (input.startsWith("http://", ignoreCase = true) ||
            input.startsWith("https://", ignoreCase = true) ||
            input.startsWith("about:blank", ignoreCase = true)
        ) {
            return true
        }
        // Match common domain patterns like google.com, example.org/test, etc.
        val domainRegex = Regex("^[a-zA-Z0-9-]+\\.[a-zA-Z]{2,}(/.*)?$")
        return domainRegex.matches(input)
    }

    private fun formatUrlOrQuery(input: String, searchEngine: SearchEngine): String {
        return when {
            input.startsWith("http://", ignoreCase = true) ||
            input.startsWith("https://", ignoreCase = true) -> input
            isDirectUrl(input) -> "https://$input"
            else -> searchEngine.buildSearchUrl(input)
        }
    }

    // Web navigation events from WebView
    fun onPageStarted(url: String) {
        _uiState.update { state ->
            val updatedTabs = state.tabs.map { tab ->
                if (tab.id == state.activeTabId) {
                    tab.copy(url = url, isLoading = true, progress = 15)
                } else tab
            }
            state.copy(
                tabs = updatedTabs,
                omniboxText = if (url == "about:blank") "" else url
            )
        }
    }

    fun onPageFinished(url: String, title: String?, canGoBack: Boolean, canGoForward: Boolean) {
        val cleanTitle = title?.takeIf { it.isNotBlank() && it != "about:blank" } ?: url
        _uiState.update { state ->
            val updatedTabs = state.tabs.map { tab ->
                if (tab.id == state.activeTabId) {
                    tab.copy(
                        url = url,
                        title = cleanTitle,
                        isLoading = false,
                        progress = 100,
                        canGoBack = canGoBack,
                        canGoForward = canGoForward
                    )
                } else tab
            }
            state.copy(
                tabs = updatedTabs,
                omniboxText = if (url == "about:blank") "" else url
            )
        }

        // Record history if not blank
        if (url != "about:blank" && url.isNotBlank()) {
            viewModelScope.launch {
                browserRepository.recordVisit(cleanTitle, url)
            }
        }
    }

    fun onProgressChanged(progress: Int) {
        _uiState.update { state ->
            val updatedTabs = state.tabs.map { tab ->
                if (tab.id == state.activeTabId) {
                    tab.copy(
                        progress = progress,
                        isLoading = progress < 100
                    )
                } else tab
            }
            state.copy(tabs = updatedTabs)
        }
    }

    fun onNavigationStateChanged(canGoBack: Boolean, canGoForward: Boolean) {
        _uiState.update { state ->
            val updatedTabs = state.tabs.map { tab ->
                if (tab.id == state.activeTabId) {
                    tab.copy(canGoBack = canGoBack, canGoForward = canGoForward)
                } else tab
            }
            state.copy(tabs = updatedTabs)
        }
    }

    // Nav Commands
    fun goBack() {
        _navCommands.tryEmit(BrowserNavCommand.GoBack)
    }

    fun goForward() {
        _navCommands.tryEmit(BrowserNavCommand.GoForward)
    }

    fun reload() {
        _navCommands.tryEmit(BrowserNavCommand.Reload)
    }

    fun stop() {
        _navCommands.tryEmit(BrowserNavCommand.Stop)
    }

    fun navigateHome() {
        _uiState.update { state ->
            val updatedTabs = state.tabs.map { tab ->
                if (tab.id == state.activeTabId) {
                    tab.copy(url = "about:blank", title = "Nueva Pestaña", isLoading = false, progress = 0)
                } else tab
            }
            state.copy(tabs = updatedTabs, omniboxText = "", isOmniboxFocused = false)
        }
        _navCommands.tryEmit(BrowserNavCommand.LoadUrl("about:blank"))
    }

    // Tab Management
    fun openNewTab(url: String = "about:blank") {
        val newTab = WebTab(
            url = url,
            title = if (url == "about:blank") "Nueva Pestaña" else url
        )
        _uiState.update { state ->
            state.copy(
                tabs = state.tabs + newTab,
                activeTabId = newTab.id,
                omniboxText = if (url == "about:blank") "" else url,
                isTabsSheetVisible = false
            )
        }
        _navCommands.tryEmit(BrowserNavCommand.LoadUrl(url))
    }

    fun switchTab(tabId: String) {
        val target = _uiState.value.tabs.find { it.id == tabId } ?: return
        _uiState.update { state ->
            state.copy(
                activeTabId = tabId,
                omniboxText = if (target.isHomePage) "" else target.url,
                isTabsSheetVisible = false
            )
        }
        _navCommands.tryEmit(BrowserNavCommand.LoadUrl(target.url))
    }

    fun closeTab(tabId: String) {
        val currentTabs = _uiState.value.tabs
        if (currentTabs.size <= 1) {
            // Keep at least one tab open, reset to blank
            navigateHome()
            return
        }

        val closingIndex = currentTabs.indexOfFirst { it.id == tabId }
        val remaining = currentTabs.filter { it.id != tabId }
        val nextActive = if (_uiState.value.activeTabId == tabId) {
            val newIndex = (closingIndex - 1).coerceAtLeast(0)
            remaining[newIndex]
        } else {
            _uiState.value.activeTab
        }

        _uiState.update { state ->
            state.copy(
                tabs = remaining,
                activeTabId = nextActive.id,
                omniboxText = if (nextActive.isHomePage) "" else nextActive.url
            )
        }
        _navCommands.tryEmit(BrowserNavCommand.LoadUrl(nextActive.url))
    }

    // Bookmarks
    fun toggleBookmarkCurrentPage() {
        val active = _uiState.value.activeTab
        if (active.isHomePage) return

        viewModelScope.launch {
            val isBookmarked = _uiState.value.bookmarks.any { it.url == active.url }
            if (isBookmarked) {
                browserRepository.removeBookmarkByUrl(active.url)
            } else {
                browserRepository.addBookmark(
                    title = active.title,
                    url = active.url
                )
            }
        }
    }

    fun deleteBookmark(id: Long) {
        viewModelScope.launch {
            browserRepository.deleteBookmarkById(id)
        }
    }

    // History
    fun deleteHistoryItem(id: Long) {
        viewModelScope.launch {
            browserRepository.deleteHistoryById(id)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            browserRepository.clearHistory()
        }
    }

    fun onHistoryFilterChanged(query: String) {
        _uiState.update { it.copy(historyFilterQuery = query) }
        viewModelScope.launch {
            if (query.isBlank()) {
                browserRepository.getHistory(150).collectLatest { list ->
                    _uiState.update { it.copy(history = list) }
                }
            } else {
                browserRepository.searchHistory(query).collectLatest { list ->
                    _uiState.update { it.copy(history = list) }
                }
            }
        }
    }

    // Sheets / Modals visibility
    fun setTabsSheetVisible(visible: Boolean) {
        _uiState.update { it.copy(isTabsSheetVisible = visible) }
    }

    fun setGeminiSheetVisible(visible: Boolean) {
        _uiState.update { it.copy(isGeminiSheetVisible = visible) }
    }

    fun setBookmarksHistoryVisible(visible: Boolean, tab: Int = 0) {
        _uiState.update { it.copy(isBookmarksHistoryVisible = visible, bookmarksHistoryTab = tab) }
    }

    fun setSettingsDialogVisible(visible: Boolean) {
        _uiState.update { it.copy(isSettingsDialogVisible = visible) }
    }

    // Settings
    fun setSearchEngine(engine: SearchEngine) {
        viewModelScope.launch {
            browserRepository.setSearchEngine(engine)
        }
    }

    fun toggleDesktopMode() {
        val current = _uiState.value.isDesktopMode
        val next = !current
        viewModelScope.launch {
            browserRepository.setDesktopMode(next)
            _navCommands.tryEmit(BrowserNavCommand.Reload)
        }
    }

    fun setLanguage(lang: String) {
        viewModelScope.launch {
            dataStoreManager.setLanguage(lang)
        }
    }

    fun setTheme(theme: String) {
        viewModelScope.launch {
            dataStoreManager.setTheme(theme)
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

    // Gemini AI Web Assistant
    fun summarizeCurrentPage() {
        val active = _uiState.value.activeTab
        if (active.isHomePage) return

        _uiState.update {
            it.copy(
                isPageSummaryLoading = true,
                pageSummary = null,
                isGeminiSheetVisible = true
            )
        }

        viewModelScope.launch {
            val lang = _uiState.value.appLanguage.ifBlank { "es" }
            val result = geminiRepository.summarizePage(
                title = active.title,
                url = active.url,
                language = if (lang == "system") "es" else lang
            )

            result.onSuccess { summary ->
                _uiState.update {
                    it.copy(
                        pageSummary = summary,
                        isPageSummaryLoading = false
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        pageSummary = "No fue posible generar el resumen. Verifica tu conexión o clave de API.",
                        isPageSummaryLoading = false
                    )
                }
            }
        }
    }

    fun sendAiAssistantMessage(question: String) {
        val trimmed = question.trim()
        if (trimmed.isBlank()) return

        val userMessage = AiChatMessage(sender = "user", message = trimmed)
        val active = _uiState.value.activeTab

        _uiState.update {
            it.copy(
                aiChatMessages = it.aiChatMessages + userMessage,
                isAiChatLoading = true
            )
        }

        viewModelScope.launch {
            val lang = _uiState.value.appLanguage.ifBlank { "es" }
            val result = geminiRepository.askAssistant(
                question = trimmed,
                pageTitle = if (active.isHomePage) null else active.title,
                pageUrl = if (active.isHomePage) null else active.url,
                language = if (lang == "system") "es" else lang
            )

            result.onSuccess { answer ->
                val geminiMsg = AiChatMessage(sender = "gemini", message = answer)
                _uiState.update {
                    it.copy(
                        aiChatMessages = it.aiChatMessages + geminiMsg,
                        isAiChatLoading = false
                    )
                }
            }.onFailure {
                val errorMsg = AiChatMessage(
                    sender = "gemini",
                    message = "No se pudo obtener una respuesta en este momento. Intenta de nuevo."
                )
                _uiState.update {
                    it.copy(
                        aiChatMessages = it.aiChatMessages + errorMsg,
                        isAiChatLoading = false
                    )
                }
            }
        }
    }

    class Factory(
        private val browserRepository: BrowserRepository,
        private val geminiRepository: GeminiSuggestionsRepository,
        private val dataStoreManager: DataStoreManager
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return BrowserViewModel(browserRepository, geminiRepository, dataStoreManager) as T
        }
    }
}

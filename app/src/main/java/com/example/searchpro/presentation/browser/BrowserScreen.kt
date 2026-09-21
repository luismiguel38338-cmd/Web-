package com.example.searchpro.presentation.browser

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import com.example.searchpro.presentation.browser.components.AddressBar
import com.example.searchpro.presentation.browser.components.BookmarksHistorySheet
import com.example.searchpro.presentation.browser.components.BrowserBottomBar
import com.example.searchpro.presentation.browser.components.BrowserSettingsDialog
import com.example.searchpro.presentation.browser.components.GeminiAiAssistantSheet
import com.example.searchpro.presentation.browser.components.SpeedDialHome
import com.example.searchpro.presentation.browser.components.TabsGridSheet
import com.example.searchpro.presentation.browser.components.WebViewContainer

@Composable
fun BrowserScreen(
    viewModel: BrowserViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    // Voice Search Launcher
    val voiceSearchLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenText = result.data
                ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                ?.firstOrNull()
            if (!spokenText.isNullOrBlank()) {
                viewModel.loadUrlOrSearch(spokenText)
            }
        }
    }

    val onLaunchVoiceSearch: () -> Unit = {
        try {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH)
                putExtra(RecognizerIntent.EXTRA_PROMPT, "Habla para buscar...")
            }
            voiceSearchLauncher.launch(intent)
        } catch (e: Exception) {
            // Speech recognition not available on device
        }
    }

    val onShareCurrentPage: () -> Unit = {
        val active = uiState.activeTab
        if (!active.isHomePage) {
            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, "${active.title}\n${active.url}")
                type = "text/plain"
            }
            context.startActivity(Intent.createChooser(sendIntent, "Compartir enlace"))
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("browser_screen_scaffold"),
        topBar = {
            AddressBar(
                uiState = uiState,
                onQueryChanged = viewModel::onOmniboxTextChanged,
                onSubmitQuery = viewModel::loadUrlOrSearch,
                onVoiceClick = onLaunchVoiceSearch,
                onRefreshClick = viewModel::reload,
                onStopClick = viewModel::stop,
                onTabsClick = { viewModel.setTabsSheetVisible(true) },
                onToggleBookmark = viewModel::toggleBookmarkCurrentPage,
                onToggleDesktopMode = viewModel::toggleDesktopMode,
                onBookmarksHistoryClick = { tab -> viewModel.setBookmarksHistoryVisible(true, tab) },
                onSummarizeClick = viewModel::summarizeCurrentPage,
                onShareClick = onShareCurrentPage,
                onSettingsClick = { viewModel.setSettingsDialogVisible(true) }
            )
        },
        bottomBar = {
            BrowserBottomBar(
                uiState = uiState,
                onBackClick = viewModel::goBack,
                onForwardClick = viewModel::goForward,
                onHomeClick = viewModel::navigateHome,
                onTabsClick = { viewModel.setTabsSheetVisible(true) },
                onGeminiClick = { viewModel.setGeminiSheetVisible(true) }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val activeTab = uiState.activeTab

            if (activeTab.isHomePage) {
                SpeedDialHome(
                    uiState = uiState,
                    onNavigateUrl = viewModel::loadUrlOrSearch,
                    onSelectEngine = viewModel::setSearchEngine,
                    onOpenGeminiAssistant = { viewModel.setGeminiSheetVisible(true) },
                    onOpenBookmarks = { viewModel.setBookmarksHistoryVisible(true, 0) },
                    onOpenHistory = { viewModel.setBookmarksHistoryVisible(true, 1) }
                )
            } else {
                WebViewContainer(
                    currentUrl = activeTab.url,
                    isDesktopMode = uiState.isDesktopMode,
                    navCommands = viewModel.navCommands,
                    onPageStarted = viewModel::onPageStarted,
                    onPageFinished = viewModel::onPageFinished,
                    onProgressChanged = viewModel::onProgressChanged,
                    onNavigationStateChanged = viewModel::onNavigationStateChanged
                )
            }
        }
    }

    // Tabs Bottom Sheet
    if (uiState.isTabsSheetVisible) {
        TabsGridSheet(
            tabs = uiState.tabs,
            activeTabId = uiState.activeTabId,
            onSelectTab = viewModel::switchTab,
            onCloseTab = viewModel::closeTab,
            onNewTab = { viewModel.openNewTab() },
            onDismiss = { viewModel.setTabsSheetVisible(false) }
        )
    }

    // Gemini AI Assistant Bottom Sheet
    if (uiState.isGeminiSheetVisible) {
        GeminiAiAssistantSheet(
            uiState = uiState,
            onSummarizePage = viewModel::summarizeCurrentPage,
            onSendMessage = viewModel::sendAiAssistantMessage,
            onDismiss = { viewModel.setGeminiSheetVisible(false) }
        )
    }

    // Bookmarks & History Bottom Sheet
    if (uiState.isBookmarksHistoryVisible) {
        BookmarksHistorySheet(
            initialTab = uiState.bookmarksHistoryTab,
            bookmarks = uiState.bookmarks,
            history = uiState.history,
            historyFilterQuery = uiState.historyFilterQuery,
            onHistoryFilterChanged = viewModel::onHistoryFilterChanged,
            onOpenUrl = viewModel::loadUrlOrSearch,
            onDeleteBookmark = viewModel::deleteBookmark,
            onDeleteHistoryItem = viewModel::deleteHistoryItem,
            onClearAllHistory = viewModel::clearAllHistory,
            onDismiss = { viewModel.setBookmarksHistoryVisible(false) }
        )
    }

    // Settings Dialog
    if (uiState.isSettingsDialogVisible) {
        BrowserSettingsDialog(
            uiState = uiState,
            onSelectEngine = viewModel::setSearchEngine,
            onSelectLanguage = viewModel::setLanguage,
            onSelectTheme = viewModel::setTheme,
            onToggleSuggestions = viewModel::setSuggestionsEnabled,
            onToggleVibration = viewModel::setVibrationEnabled,
            onDismiss = { viewModel.setSettingsDialogVisible(false) }
        )
    }
}

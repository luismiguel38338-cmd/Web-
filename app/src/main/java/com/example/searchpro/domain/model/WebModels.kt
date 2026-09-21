package com.example.searchpro.domain.model

data class WebBookmark(
    val id: Long = 0,
    val title: String,
    val url: String,
    val faviconUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

data class WebHistoryItem(
    val id: Long = 0,
    val title: String,
    val url: String,
    val visitedAt: Long = System.currentTimeMillis(),
    val visitCount: Int = 1
)

data class WebTab(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String = "Nueva Pestaña",
    val url: String = "about:blank",
    val isLoading: Boolean = false,
    val progress: Int = 0,
    val canGoBack: Boolean = false,
    val canGoForward: Boolean = false,
    val isDesktopMode: Boolean = false,
    val isBookmarked: Boolean = false
) {
    val isHomePage: Boolean
        get() = url == "about:blank" || url.isBlank()
}

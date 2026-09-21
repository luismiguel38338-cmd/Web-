package com.example.searchpro.domain.model

enum class SearchEngine(
    val displayName: String,
    val searchUrlPattern: String,
    val homeUrl: String
) {
    GOOGLE(
        displayName = "Google",
        searchUrlPattern = "https://www.google.com/search?q=%s",
        homeUrl = "https://www.google.com"
    ),
    DUCKDUCKGO(
        displayName = "DuckDuckGo",
        searchUrlPattern = "https://duckduckgo.com/?q=%s",
        homeUrl = "https://duckduckgo.com"
    ),
    BING(
        displayName = "Bing",
        searchUrlPattern = "https://www.bing.com/search?q=%s",
        homeUrl = "https://www.bing.com"
    ),
    WIKIPEDIA(
        displayName = "Wikipedia",
        searchUrlPattern = "https://en.wikipedia.org/wiki/Special:Search?search=%s",
        homeUrl = "https://en.wikipedia.org"
    ),
    ECOSIA(
        displayName = "Ecosia",
        searchUrlPattern = "https://www.ecosia.org/search?q=%s",
        homeUrl = "https://www.ecosia.org"
    );

    fun buildSearchUrl(query: String): String {
        val encoded = java.net.URLEncoder.encode(query, "UTF-8")
        return String.format(searchUrlPattern, encoded)
    }

    companion object {
        fun fromDisplayName(name: String): SearchEngine {
            return entries.find { it.displayName.equals(name, ignoreCase = true) } ?: GOOGLE
        }
    }
}

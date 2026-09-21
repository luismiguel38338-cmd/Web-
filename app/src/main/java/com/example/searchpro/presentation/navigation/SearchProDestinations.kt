package com.example.searchpro.presentation.navigation

object SearchProDestinations {
    const val BROWSER = "browser"
    const val WELCOME = "welcome"
    const val HOME = "home"
    const val FILTERS = "filters"
    const val DETAILS = "details/{itemId}"
    const val RECENT_SEARCHES = "recent_searches"
    const val FAVORITES = "favorites"
    const val SETTINGS = "settings"

    fun detailsRoute(itemId: Long): String = "details/$itemId"
}

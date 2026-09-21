package com.example.searchpro.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.searchpro.SearchProApplication
import com.example.searchpro.presentation.browser.BrowserScreen
import com.example.searchpro.presentation.browser.BrowserViewModel

@Composable
fun SearchProNavHost(
    app: SearchProApplication,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    val browserViewModel: BrowserViewModel = viewModel(
        factory = BrowserViewModel.Factory(
            browserRepository = app.browserRepository,
            geminiRepository = app.geminiRepository,
            dataStoreManager = app.dataStoreManager
        )
    )

    NavHost(
        navController = navController,
        startDestination = SearchProDestinations.BROWSER,
        modifier = modifier
    ) {
        composable(SearchProDestinations.BROWSER) {
            BrowserScreen(viewModel = browserViewModel)
        }
    }
}

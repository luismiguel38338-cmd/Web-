package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.searchpro.SearchProApplication
import com.example.searchpro.presentation.browser.BrowserScreen
import com.example.searchpro.presentation.browser.BrowserViewModel
import com.example.searchpro.ui.localization.ProvideAppLocale
import com.example.searchpro.ui.theme.SearchProTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as SearchProApplication

        setContent {
            val appLanguage by app.dataStoreManager.languageFlow.collectAsStateWithLifecycle(initialValue = "system")
            val appTheme by app.dataStoreManager.themeFlow.collectAsStateWithLifecycle(initialValue = "system")

            val systemInDark = isSystemInDarkTheme()
            val isDark = when (appTheme) {
                "light" -> false
                "dark" -> true
                else -> systemInDark
            }

            CompositionLocalProvider(
                LocalActivityResultRegistryOwner provides this@MainActivity
            ) {
                ProvideAppLocale(languageCode = appLanguage) {
                    SearchProTheme(darkTheme = isDark) {
                        Surface(modifier = Modifier.fillMaxSize()) {
                            com.example.searchpro.presentation.navigation.SearchProNavHost(app = app)
                        }
                    }
                }
            }
        }
    }
}


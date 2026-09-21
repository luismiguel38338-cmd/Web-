package com.example

import android.content.Context
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.core.app.ApplicationProvider
import com.example.searchpro.presentation.search.voice.rememberVoiceSearchController
import com.example.searchpro.ui.localization.ProvideAppLocale
import com.example.searchpro.ui.theme.SearchProTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("SearchPro", appName)
  }

  @Test
  fun `voice controller initializes safely inside ProvideAppLocale with custom language`() {
    composeTestRule.setContent {
      ProvideAppLocale(languageCode = "es") {
        SearchProTheme {
          val voiceController = rememberVoiceSearchController(onResult = {})
          assertNotNull(voiceController)
        }
      }
    }
  }
}



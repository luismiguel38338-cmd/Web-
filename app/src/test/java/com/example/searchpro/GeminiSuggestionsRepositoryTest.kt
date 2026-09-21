package com.example.searchpro

import com.example.searchpro.data.remote.gemini.GeminiApiService
import com.example.searchpro.data.remote.gemini.GeminiCandidate
import com.example.searchpro.data.remote.gemini.GeminiContent
import com.example.searchpro.data.remote.gemini.GeminiGenerateContentRequest
import com.example.searchpro.data.remote.gemini.GeminiGenerateContentResponse
import com.example.searchpro.data.remote.gemini.GeminiPart
import com.example.searchpro.data.repository.GeminiSuggestionsRepositoryImpl
import com.example.searchpro.domain.usecase.GetGeminiSuggestionsUseCase
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class GeminiSuggestionsRepositoryTest {

    private class FakeGeminiApiService(
        private val cannedResponse: String
    ) : GeminiApiService {
        var lastCapturedApiKey: String? = null
        var lastCapturedRequest: GeminiGenerateContentRequest? = null

        override suspend fun generateContent(
            apiKey: String,
            request: GeminiGenerateContentRequest
        ): GeminiGenerateContentResponse {
            lastCapturedApiKey = apiKey
            lastCapturedRequest = request
            return GeminiGenerateContentResponse(
                candidates = listOf(
                    GeminiCandidate(
                        content = GeminiContent(
                            parts = listOf(GeminiPart(text = cannedResponse))
                        )
                    )
                )
            )
        }
    }

    @Test
    fun `getSearchSuggestions parses multiline text and filters out query`() = runBlocking {
        val fakeApi = FakeGeminiApiService(
            cannedResponse = "android compose tutorial\n- android architecture components\n• kotlin coroutines\nandroid"
        )
        val repository = GeminiSuggestionsRepositoryImpl(
            apiService = fakeApi,
            apiKeyProvider = { "test-api-key-123" }
        )
        val useCase = GetGeminiSuggestionsUseCase(repository)

        val result = useCase("android", language = "en", maxSuggestions = 3)

        assertTrue(result.isSuccess)
        val suggestions = result.getOrNull().orEmpty()

        assertEquals("test-api-key-123", fakeApi.lastCapturedApiKey)
        assertEquals(3, suggestions.size)
        assertEquals("android compose tutorial", suggestions[0])
        assertEquals("android architecture components", suggestions[1])
        assertEquals("kotlin coroutines", suggestions[2])
    }

    @Test
    fun `getSearchSuggestions returns empty list when query is blank`() = runBlocking {
        val fakeApi = FakeGeminiApiService(cannedResponse = "ignored")
        val repository = GeminiSuggestionsRepositoryImpl(
            apiService = fakeApi,
            apiKeyProvider = { "valid-key" }
        )

        val result = repository.getSearchSuggestions("   ")

        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull().orEmpty().isEmpty())
    }

    @Test
    fun `getSearchSuggestions fails gracefully when api key is not configured`() = runBlocking {
        val fakeApi = FakeGeminiApiService(cannedResponse = "ignored")
        val repository = GeminiSuggestionsRepositoryImpl(
            apiService = fakeApi,
            apiKeyProvider = { "MY_GEMINI_API_KEY" }
        )

        val result = repository.getSearchSuggestions("kotlin")

        assertTrue(result.isFailure)
    }
}

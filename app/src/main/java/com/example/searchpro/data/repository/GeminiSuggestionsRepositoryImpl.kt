package com.example.searchpro.data.repository

import android.util.Log
import com.example.BuildConfig
import com.example.searchpro.data.remote.gemini.GeminiApiService
import com.example.searchpro.data.remote.gemini.GeminiContent
import com.example.searchpro.data.remote.gemini.GeminiGenerateContentRequest
import com.example.searchpro.data.remote.gemini.GeminiGenerationConfig
import com.example.searchpro.data.remote.gemini.GeminiNetworkClient
import com.example.searchpro.data.remote.gemini.GeminiPart
import com.example.searchpro.domain.repository.GeminiSuggestionsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Implementation of [GeminiSuggestionsRepository] calling Google's Gemini API
 * directly using the configured API key from [BuildConfig.GEMINI_API_KEY].
 */
class GeminiSuggestionsRepositoryImpl(
    private val apiService: GeminiApiService = GeminiNetworkClient.apiService,
    private val apiKeyProvider: () -> String = { BuildConfig.GEMINI_API_KEY }
) : GeminiSuggestionsRepository {

    override suspend fun getSearchSuggestions(
        query: String,
        language: String,
        maxSuggestions: Int
    ): Result<List<String>> = withContext(Dispatchers.IO) {
        val trimmedQuery = query.trim()
        if (trimmedQuery.isBlank()) {
            return@withContext Result.success(emptyList())
        }

        val apiKey = apiKeyProvider()
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            Log.w(TAG, "Gemini API key is not configured or using placeholder value.")
            return@withContext Result.failure(
                IllegalStateException("Gemini API key is not configured in Secrets.")
            )
        }

        try {
            val systemInstruction = GeminiContent(
                parts = listOf(
                    GeminiPart(
                        text = "You are a concise search query autocomplete and suggestion engine. " +
                                "Generate up to $maxSuggestions natural, relevant search suggestion queries " +
                                "that a user searching for '${trimmedQuery}' might be looking for in language '${language}'. " +
                                "Return ONLY the suggested terms, one per line. Do NOT include numbers, bullets, quotation marks, or explanations."
                    )
                )
            )

            val promptContent = GeminiContent(
                parts = listOf(
                    GeminiPart(text = "Search suggestions for: $trimmedQuery")
                )
            )

            val request = GeminiGenerateContentRequest(
                contents = listOf(promptContent),
                systemInstruction = systemInstruction,
                generationConfig = GeminiGenerationConfig(
                    temperature = 0.4f,
                    topP = 0.9f,
                    topK = 20
                )
            )

            val response = apiService.generateContent(
                apiKey = apiKey,
                request = request
            )

            val rawText = response.candidates
                ?.firstOrNull()
                ?.content
                ?.parts
                ?.firstOrNull()
                ?.text
                .orEmpty()

            val suggestions = rawText.lines()
                .map { line ->
                    // Clean line of bullets, dashes or numbered prefixes if any
                    line.replace(Regex("^[-*•\\d.]+\\s*"), "")
                        .replace("\"", "")
                        .trim()
                }
                .filter { it.isNotBlank() && !it.equals(trimmedQuery, ignoreCase = true) }
                .distinct()
                .take(maxSuggestions)

            Result.success(suggestions)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching Gemini search suggestions for query '$trimmedQuery'", e)
            Result.failure(e)
        }
    }

    override suspend fun summarizePage(
        title: String,
        url: String,
        contentSnippet: String,
        language: String
    ): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = apiKeyProvider()
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext Result.failure(
                IllegalStateException("Gemini API key is not configured in Secrets.")
            )
        }

        try {
            val systemInstruction = GeminiContent(
                parts = listOf(
                    GeminiPart(
                        text = "You are an intelligent web assistant. Provide a structured, clear summary of " +
                                "the web page based on its title, URL, and snippet in the user's language ($language). " +
                                "Highlight 3 key takeaways or main points. Be concise and helpful."
                    )
                )
            )

            val prompt = buildString {
                appendLine("Page Title: $title")
                appendLine("Page URL: $url")
                if (contentSnippet.isNotBlank()) {
                    appendLine("Page Snippet: $contentSnippet")
                }
                append("Please summarize this page.")
            }

            val request = GeminiGenerateContentRequest(
                contents = listOf(GeminiContent(parts = listOf(GeminiPart(text = prompt)))),
                systemInstruction = systemInstruction,
                generationConfig = GeminiGenerationConfig(
                    temperature = 0.5f,
                    topP = 0.95f
                )
            )

            val response = apiService.generateContent(apiKey = apiKey, request = request)
            val summary = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text.orEmpty()
            if (summary.isNotBlank()) {
                Result.success(summary)
            } else {
                Result.failure(IllegalStateException("No summary generated."))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error generating page summary for $url", e)
            Result.failure(e)
        }
    }

    override suspend fun askAssistant(
        question: String,
        pageTitle: String?,
        pageUrl: String?,
        language: String
    ): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = apiKeyProvider()
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext Result.failure(
                IllegalStateException("Gemini API key is not configured in Secrets.")
            )
        }

        try {
            val systemInstruction = GeminiContent(
                parts = listOf(
                    GeminiPart(
                        text = "You are SearchPro AI, a helpful, precise web search and knowledge assistant. " +
                                "Answer the user's question clearly in $language. If context about the currently " +
                                "viewed webpage is provided, use it to give a more relevant answer."
                    )
                )
            )

            val prompt = buildString {
                if (!pageTitle.isNullOrBlank()) {
                    appendLine("Current page: $pageTitle (${pageUrl.orEmpty()})")
                }
                append(question.trim())
            }

            val request = GeminiGenerateContentRequest(
                contents = listOf(GeminiContent(parts = listOf(GeminiPart(text = prompt)))),
                systemInstruction = systemInstruction,
                generationConfig = GeminiGenerationConfig(
                    temperature = 0.6f,
                    topP = 0.95f
                )
            )

            val response = apiService.generateContent(apiKey = apiKey, request = request)
            val answer = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text.orEmpty()
            if (answer.isNotBlank()) {
                Result.success(answer)
            } else {
                Result.failure(IllegalStateException("Empty assistant response."))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in askAssistant for: $question", e)
            Result.failure(e)
        }
    }

    companion object {
        private const val TAG = "GeminiSuggestionsRepo"
    }
}

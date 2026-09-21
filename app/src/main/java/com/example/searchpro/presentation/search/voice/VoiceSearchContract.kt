package com.example.searchpro.presentation.search.voice

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.widget.Toast
import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultRegistryOwner
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.example.R
import java.util.Locale

/**
 * Interface contract for Voice Search capability.
 * This can be easily replaced or expanded with custom cloud STT models
 * (such as Gemini Live Audio or Whisper REST API).
 */
interface VoiceSearchController {
    val isAvailable: Boolean
    fun startListening()
}

private fun Context.findActivityResultRegistryOwner(): ActivityResultRegistryOwner? {
    var ctx: Context? = this
    while (ctx is ContextWrapper) {
        if (ctx is ActivityResultRegistryOwner) return ctx
        ctx = ctx.baseContext
    }
    return null
}

/**
 * Compose hook providing a ready-to-use VoiceSearchController.
 * Launches the native Android Speech Recognition prompt and returns recognized text.
 */
@Composable
fun rememberVoiceSearchController(
    onResult: (String) -> Unit,
    currentLanguageCode: String = "system"
): VoiceSearchController {
    val context = LocalContext.current
    val registryOwner = LocalActivityResultRegistryOwner.current
        ?: context.findActivityResultRegistryOwner()

    val speechLauncher = if (registryOwner != null) {
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                val spokenText: ArrayList<String>? =
                    result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                val query = spokenText?.firstOrNull()?.trim()
                if (!query.isNullOrBlank()) {
                    onResult(query)
                }
            }
        }
    } else {
        null
    }

    val isRecognitionAvailable = remember(context, speechLauncher) {
        speechLauncher != null && SpeechRecognizer.isRecognitionAvailable(context)
    }

    return remember(speechLauncher, isRecognitionAvailable, currentLanguageCode, context) {
        object : VoiceSearchController {
            override val isAvailable: Boolean = isRecognitionAvailable

            override fun startListening() {
                if (speechLauncher == null) {
                    Toast.makeText(
                        context,
                        context.getString(R.string.voice_not_available),
                        Toast.LENGTH_SHORT
                    ).show()
                    return
                }

                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(
                        RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                    )
                    val locale = when (currentLanguageCode) {
                        "es" -> Locale("es")
                        "en" -> Locale.ENGLISH
                        "fr" -> Locale.FRENCH
                        else -> Locale.getDefault()
                    }
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, locale.toLanguageTag())
                    putExtra(
                        RecognizerIntent.EXTRA_PROMPT,
                        context.getString(R.string.search_voice)
                    )
                }

                try {
                    speechLauncher.launch(intent)
                } catch (e: Exception) {
                    Toast.makeText(
                        context,
                        context.getString(R.string.voice_not_available),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}

package com.example.searchpro.ui.localization

import android.content.Context
import android.content.ContextWrapper
import android.content.res.AssetManager
import android.content.res.Configuration
import android.content.res.Resources
import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.activity.result.ActivityResultRegistryOwner
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import java.util.Locale

class LocalizedContextWrapper(
    base: Context,
    private val configContext: Context
) : ContextWrapper(base) {
    override fun getResources(): Resources = configContext.resources
    override fun getAssets(): AssetManager = configContext.assets
    override fun createConfigurationContext(overrideConfiguration: Configuration): Context {
        return configContext.createConfigurationContext(overrideConfiguration)
    }
}

object LocaleHelper {

    fun getLocaleForCode(languageCode: String): Locale {
        return when (languageCode) {
            "es" -> Locale("es")
            "en" -> Locale("en")
            "fr" -> Locale("fr")
            else -> Locale.getDefault()
        }
    }

    fun applyLocale(context: Context, languageCode: String): Context {
        if (languageCode == "system") {
            return context
        }
        val locale = getLocaleForCode(languageCode)
        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        val configContext = context.createConfigurationContext(config)
        return LocalizedContextWrapper(context, configContext)
    }
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
 * Wraps content with the given language code so all localized resources
 * and stringResource(...) calls immediately reflect the selected language.
 */
@Composable
fun ProvideAppLocale(
    languageCode: String,
    content: @Composable () -> Unit
) {
    val currentContext = LocalContext.current
    val currentConfiguration = LocalConfiguration.current
    val currentRegistryOwner = LocalActivityResultRegistryOwner.current
        ?: currentContext.findActivityResultRegistryOwner()

    val localizedContext = remember(languageCode, currentContext) {
        if (languageCode == "system") {
            currentContext
        } else {
            val locale = LocaleHelper.getLocaleForCode(languageCode)
            val config = Configuration(currentContext.resources.configuration).apply {
                setLocale(locale)
            }
            val configContext = currentContext.createConfigurationContext(config)
            LocalizedContextWrapper(currentContext, configContext)
        }
    }

    val localizedConfiguration = remember(languageCode, currentConfiguration) {
        if (languageCode == "system") {
            currentConfiguration
        } else {
            val locale = LocaleHelper.getLocaleForCode(languageCode)
            Configuration(currentConfiguration).apply {
                setLocale(locale)
            }
        }
    }

    val providers = remember(localizedContext, localizedConfiguration, currentRegistryOwner) {
        val list = mutableListOf<ProvidedValue<*>>(
            LocalContext provides localizedContext,
            LocalConfiguration provides localizedConfiguration
        )
        if (currentRegistryOwner != null) {
            list.add(LocalActivityResultRegistryOwner provides currentRegistryOwner)
        }
        list.toTypedArray()
    }

    CompositionLocalProvider(values = providers, content = content)
}


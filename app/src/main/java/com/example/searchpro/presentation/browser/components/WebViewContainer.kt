package com.example.searchpro.presentation.browser.components

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.viewinterop.AndroidView
import com.example.searchpro.presentation.browser.BrowserNavCommand
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collectLatest

private const val DESKTOP_USER_AGENT =
    "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebViewContainer(
    currentUrl: String,
    isDesktopMode: Boolean,
    navCommands: SharedFlow<BrowserNavCommand>,
    onPageStarted: (String) -> Unit,
    onPageFinished: (url: String, title: String?, canGoBack: Boolean, canGoForward: Boolean) -> Unit,
    onProgressChanged: (Int) -> Unit,
    onNavigationStateChanged: (canGoBack: Boolean, canGoForward: Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var defaultUserAgent by remember { mutableStateOf<String?>(null) }

    // Collect navigation commands from ViewModel
    LaunchedEffect(navCommands) {
        navCommands.collectLatest { command ->
            val webView = webViewRef ?: return@collectLatest
            when (command) {
                is BrowserNavCommand.LoadUrl -> {
                    if (webView.url != command.url) {
                        webView.loadUrl(command.url)
                    }
                }
                BrowserNavCommand.GoBack -> {
                    if (webView.canGoBack()) {
                        webView.goBack()
                    }
                }
                BrowserNavCommand.GoForward -> {
                    if (webView.canGoForward()) {
                        webView.goForward()
                    }
                }
                BrowserNavCommand.Reload -> {
                    webView.reload()
                }
                BrowserNavCommand.Stop -> {
                    webView.stopLoading()
                }
            }
        }
    }

    // React to Desktop Mode toggle
    LaunchedEffect(isDesktopMode) {
        val webView = webViewRef ?: return@LaunchedEffect
        if (defaultUserAgent == null) {
            defaultUserAgent = webView.settings.userAgentString
        }
        webView.settings.userAgentString = if (isDesktopMode) DESKTOP_USER_AGENT else defaultUserAgent
        webView.settings.useWideViewPort = isDesktopMode
        webView.settings.loadWithOverviewMode = isDesktopMode
    }

    // Intercept back presses when WebView can go back
    BackHandler(enabled = webViewRef?.canGoBack() == true) {
        webViewRef?.goBack()
    }

    AndroidView(
        factory = { ctx ->
            WebView(ctx).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )

                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    databaseEnabled = true
                    setSupportZoom(true)
                    builtInZoomControls = true
                    displayZoomControls = false
                    useWideViewPort = isDesktopMode
                    loadWithOverviewMode = isDesktopMode
                    cacheMode = WebSettings.LOAD_DEFAULT
                    mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
                }

                defaultUserAgent = settings.userAgentString

                webViewClient = object : WebViewClient() {
                    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                        super.onPageStarted(view, url, favicon)
                        url?.let { onPageStarted(it) }
                        onNavigationStateChanged(view?.canGoBack() == true, view?.canGoForward() == true)
                    }

                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        val finalUrl = url ?: ""
                        val title = view?.title
                        val canBack = view?.canGoBack() == true
                        val canFwd = view?.canGoForward() == true
                        onPageFinished(finalUrl, title, canBack, canFwd)
                        onNavigationStateChanged(canBack, canFwd)
                    }

                    override fun onReceivedError(
                        view: WebView?,
                        request: WebResourceRequest?,
                        error: android.webkit.WebResourceError?
                    ) {
                        super.onReceivedError(view, request, error)
                        if (request?.isForMainFrame == true) {
                            onNavigationStateChanged(view?.canGoBack() == true, view?.canGoForward() == true)
                        }
                    }

                    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                        val uri = request?.url ?: return false
                        val scheme = uri.scheme?.lowercase()

                        // Handle standard web schemes internally
                        if (scheme == "http" || scheme == "https" || scheme == "about") {
                            return false
                        }

                        // Try launching external schemes (tel, mailto, intent, etc.)
                        return try {
                            val intent = Intent(Intent.ACTION_VIEW, uri)
                            context.startActivity(intent)
                            true
                        } catch (e: Exception) {
                            true
                        }
                    }
                }

                webChromeClient = object : WebChromeClient() {
                    override fun onProgressChanged(view: WebView?, newProgress: Int) {
                        super.onProgressChanged(view, newProgress)
                        onProgressChanged(newProgress)
                        onNavigationStateChanged(view?.canGoBack() == true, view?.canGoForward() == true)
                    }

                    override fun onReceivedTitle(view: WebView?, title: String?) {
                        super.onReceivedTitle(view, title)
                        onNavigationStateChanged(view?.canGoBack() == true, view?.canGoForward() == true)
                    }
                }

                if (currentUrl.isNotBlank() && currentUrl != "about:blank") {
                    loadUrl(currentUrl)
                }

                webViewRef = this
            }
        },
        update = { webView ->
            webViewRef = webView
        },
        modifier = modifier
            .fillMaxSize()
            .testTag("webview_canvas")
    )

    DisposableEffect(Unit) {
        onDispose {
            webViewRef?.stopLoading()
            webViewRef?.destroy()
            webViewRef = null
        }
    }
}

package com.nthcase.sample.ui.screens

import android.annotation.SuppressLint
import android.content.pm.ApplicationInfo
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.nthcase.sample.R
import com.nthcase.sample.TestIds
import com.nthcase.sample.ui.MossgroveTopBar

/** The bundled page. `file:///android_asset/` is served from the APK itself; the WebView never touches the network. */
const val CARE_GUIDE_URL = "file:///android_asset/web/care-guide.html"
const val PAGE_LOADING = "Loading the care guide…"
const val PAGE_LOADED = "Care guide loaded"

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebViewScreen(onBack: () -> Unit) {
    var status by remember { mutableStateOf(PAGE_LOADING) }
    Scaffold(modifier = Modifier.testTag(TestIds.WEBVIEW_SCREEN), topBar = { MossgroveTopBar("Care guide", onBack = onBack) }) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            Text(
                status,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp).testTag(TestIds.WEBVIEW_STATUS),
            )
            AndroidView(
                factory = { context ->
                    WebView(context).apply {
                        // A view id as well as the Compose tag: Appium sees this WebView as com.nthcase.sample:id/webview.
                        id = R.id.webview
                        settings.javaScriptEnabled = true
                        settings.blockNetworkLoads = true
                        settings.allowFileAccess = false
                        settings.allowContentAccess = false
                        // Debug builds let Appium switch into the page's WEBVIEW context; release builds do not.
                        val debuggable = context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0
                        WebView.setWebContentsDebuggingEnabled(debuggable)
                        webViewClient = object : WebViewClient() {
                            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean =
                                !request.url.toString().startsWith("file:///android_asset/web/")

                            override fun onPageFinished(view: WebView, url: String) {
                                status = PAGE_LOADED
                            }
                        }
                        contentDescription = "Care guide page"
                        loadUrl(CARE_GUIDE_URL)
                    }
                },
                modifier = Modifier.fillMaxWidth().weight(1f).testTag(TestIds.WEBVIEW),
            )
        }
    }
}

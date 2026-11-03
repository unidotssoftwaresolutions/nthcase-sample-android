package com.nthcase.sample

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.nthcase.sample.data.AppState
import com.nthcase.sample.data.DeepLinks
import com.nthcase.sample.ui.MossgroveApp

/**
 * The one activity. Compose draws every screen except Settings, which is a classic XML Fragment hosted inside the
 * Compose navigation. It is an AppCompatActivity so the XML screen's Material Components theme and the in-app
 * light/dark choice apply to both toolkits.
 */
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (savedInstanceState == null) takeDeepLink(intent)
        setContent { MossgroveApp() }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        takeDeepLink(intent)
    }

    private fun takeDeepLink(intent: Intent?) {
        DeepLinks.itemId(intent?.data)?.let { AppState.pendingItemId = it }
    }
}

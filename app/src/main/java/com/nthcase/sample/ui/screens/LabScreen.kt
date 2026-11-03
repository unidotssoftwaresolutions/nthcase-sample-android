package com.nthcase.sample.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.nthcase.sample.TestIds
import com.nthcase.sample.ui.Icons
import com.nthcase.sample.ui.MossgroveTopBar
import com.nthcase.sample.ui.Routes

private data class Bench(val id: String, val route: String, val title: String, val body: String)

private val benches = listOf(
    Bench(TestIds.LAB_FORM, Routes.FORM, "Join the club", "A form: text, email, number, password, picker, date, switch, slider, checkbox"),
    Bench(TestIds.LAB_GESTURES, Routes.GESTURES, "Gestures", "Tap, double tap, long press, swipe, drag and drop, pinch, rotate, scroll, shake"),
    Bench(TestIds.LAB_WEBVIEW, Routes.WEBVIEW, "Care guide", "A web page inside the app, bundled with it"),
    Bench(TestIds.LAB_SLOW, Routes.SLOW, "Delivery slots", "Loads for 3 seconds, then shows its content"),
    Bench(TestIds.LAB_ORDERS, Routes.ORDERS, "Orders", "An empty state, and an error state with retry"),
    Bench(TestIds.LAB_GIFT_CARDS, Routes.GIFT_CARDS, "Gift cards", "No test ids at all: find things by their text"),
)

/** The Lab: Mossgrove's second cabinet, one drawer per test bench. It says plainly what each one is for. */
@Composable
fun LabScreen(onOpen: (String) -> Unit) {
    Scaffold(modifier = Modifier.testTag(TestIds.LAB_SCREEN), topBar = { MossgroveTopBar("Lab") }) { padding ->
        Column(Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState())) {
            Text(
                "Screens for testing. Each one exercises verbs that NthCase records and replays.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
            benches.forEach { bench ->
                ListItem(
                    headlineContent = { Text(bench.title) },
                    supportingContent = { Text(bench.body) },
                    trailingContent = { Icon(Icons.Chevron, contentDescription = null) },
                    modifier = Modifier
                        .testTag(bench.id)
                        .clickable(onClickLabel = "Open ${bench.title}") { onOpen(bench.route) },
                )
                HorizontalDivider(Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant)
            }
        }
    }
}

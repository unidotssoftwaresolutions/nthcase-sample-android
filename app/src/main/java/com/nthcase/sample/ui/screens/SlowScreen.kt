package com.nthcase.sample.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.nthcase.sample.TestIds
import com.nthcase.sample.ui.Icons
import com.nthcase.sample.ui.LoadStatus
import com.nthcase.sample.ui.Loading
import com.nthcase.sample.ui.MossgroveTopBar
import com.nthcase.sample.ui.StateMessage
import com.nthcase.sample.ui.rememberLoad

/** The five delivery slots the slow screen shows, in order. Fixed text, so a check can name any of them. */
val DELIVERY_SLOTS = listOf(
    "Monday · 8:00–10:00",
    "Monday · 18:00–20:00",
    "Wednesday · 12:00–14:00",
    "Friday · 8:00–10:00",
    "Saturday · 10:00–12:00",
)

/** Always slow: a 3 s spinner, then the slots. Reload starts the wait again. */
@Composable
fun SlowScreen(onBack: () -> Unit) {
    var round by rememberSaveable { mutableIntStateOf(0) }
    Scaffold(modifier = Modifier.testTag(TestIds.SLOW_SCREEN), topBar = { MossgroveTopBar("Delivery slots", onBack = onBack) }) { padding ->
        key(round) {
            val load = rememberLoad(alwaysSlow = true)
            when (load.status) {
                LoadStatus.Loading -> Loading("Finding delivery slots…", Modifier.padding(padding).testTag(TestIds.SLOW_LOADING))
                LoadStatus.Failed -> StateMessage(
                    icon = Icons.Warning,
                    title = "Slots didn't load",
                    body = "Something went wrong on our side.",
                    actionLabel = "Try again",
                    onAction = load.retry,
                    isError = true,
                    modifier = Modifier.padding(padding),
                )
                LoadStatus.Ready -> Column(
                    Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState()).testTag(TestIds.SLOW_CONTENT),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        "Choose when we bring your order.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp),
                    )
                    DELIVERY_SLOTS.forEachIndexed { index, slot ->
                        ListItem(headlineContent = { Text(slot) }, modifier = Modifier.testTag(TestIds.slowSlot(index + 1)))
                    }
                    OutlinedButton(
                        onClick = { round += 1 },
                        modifier = Modifier.padding(16.dp).fillMaxWidth().testTag(TestIds.SLOW_RELOAD),
                    ) { Text("Reload") }
                }
            }
        }
    }
}

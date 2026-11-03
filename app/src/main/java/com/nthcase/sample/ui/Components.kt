package com.nthcase.sample.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.nthcase.sample.TestIds
import com.nthcase.sample.data.AppState
import com.nthcase.sample.data.Item
import com.nthcase.sample.ui.theme.ItemNumberStyle
import com.nthcase.sample.ui.theme.LocalAisleTints
import kotlinx.coroutines.delay

/**
 * Exposes test tags as Android resource ids, so Appium finds `resource-id="shop_search"`. Set once at the activity's
 * root and again inside every dialog and popup, because each of those is a window of its own.
 */
@OptIn(ExperimentalComposeUiApi::class)
fun Modifier.testIdRoot(): Modifier = semantics { testTagsAsResourceId = true }

/** Tags only when [tagged]: the gift-cards screen reuses these components with no ids at all. */
fun Modifier.idIf(tagged: Boolean, id: String): Modifier = if (tagged) testTag(id) else this

/** The top app bar: the screen's title, and a back arrow on screens below a tab. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MossgroveTopBar(title: String, onBack: (() -> Unit)? = null, tagged: Boolean = true) {
    TopAppBar(
        title = {
            Text(title, modifier = Modifier.idIf(tagged, TestIds.TOP_BAR_TITLE).semantics { heading() })
        },
        navigationIcon = {
            if (onBack != null) {
                IconButton(onClick = onBack, modifier = Modifier.idIf(tagged, TestIds.TOP_BAR_BACK)) {
                    Icon(Icons.Back, contentDescription = "Back")
                }
            }
        },
    )
}

/** "No. 007" in brass monospace: the item's mark on every screen. */
@Composable
fun ItemNumber(item: Item, modifier: Modifier = Modifier) {
    Text(item.number, style = ItemNumberStyle, color = MaterialTheme.colorScheme.tertiary, modifier = modifier)
}

/** The item's drawing on its aisle's tint. */
@Composable
fun GlyphTile(item: Item, size: Dp, modifier: Modifier = Modifier, description: String? = null) {
    val tint = LocalAisleTints.current.of(item.category)
    Box(
        modifier = modifier
            .size(size)
            .background(tint.tile, MaterialTheme.shapes.small),
        contentAlignment = Alignment.Center,
    ) {
        Icon(Art.of(item.glyph), contentDescription = description, tint = tint.ink, modifier = Modifier.size(size * 0.62f))
    }
}

/** The label holder of a drawer front: a thin brass-free frame around the name and the number. */
@Composable
fun LabelHolder(item: Item, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, MaterialTheme.shapes.extraSmall)
            .padding(horizontal = 10.dp, vertical = 6.dp),
    ) {
        Text(item.name, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
        ItemNumber(item)
    }
}

/** A − value + stepper. The value's id and the buttons' ids come from the caller. */
@Composable
fun QuantityStepper(
    value: Int,
    onChange: (Int) -> Unit,
    decreaseId: String,
    valueId: String,
    increaseId: String,
    subject: String,
    min: Int = 1,
    max: Int = 9,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        OutlinedIconButton(Icons.Minus, "Decrease $subject", enabled = value > min, modifier = Modifier.testTag(decreaseId)) {
            onChange(value - 1)
        }
        Text(
            value.toString(),
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .widthIn(min = 40.dp)
                .testTag(valueId)
                .semantics { contentDescription = "Quantity of $subject: $value" },
        )
        OutlinedIconButton(Icons.Plus, "Increase $subject", enabled = value < max, modifier = Modifier.testTag(increaseId)) {
            onChange(value + 1)
        }
    }
}

@Composable
private fun OutlinedIconButton(icon: ImageVector, label: String, enabled: Boolean, modifier: Modifier, onClick: () -> Unit) {
    // disabled loses its ring as well as its colour, so the state is a shape too
    androidx.compose.material3.OutlinedIconButton(
        onClick = onClick,
        enabled = enabled,
        border = if (enabled) IconButtonDefaults.outlinedIconButtonBorder(true) else null,
        modifier = modifier.size(48.dp),
    ) {
        Icon(icon, contentDescription = label)
    }
}

/**
 * A centred message with an icon, a title, a line of help and one action: the shape of every empty and error state.
 * The message is one accessibility node (its id reads the title and the help); the action button stays its own.
 */
@Composable
fun StateMessage(
    icon: ImageVector,
    title: String,
    body: String,
    actionLabel: String?,
    onAction: (() -> Unit)?,
    modifier: Modifier = Modifier,
    actionModifier: Modifier = Modifier,
    isError: Boolean = false,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {}
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(40.dp),
        )
        Spacer(Modifier.height(16.dp))
        Text(title, style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Text(body, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        if (actionLabel != null && onAction != null) {
            Spacer(Modifier.height(24.dp))
            if (isError) Button(onClick = onAction, modifier = actionModifier) { Text(actionLabel) }
            else OutlinedButton(onClick = onAction, modifier = actionModifier) { Text(actionLabel) }
        }
    }
}

/** A spinner with a line under it, for the slow loads. */
@Composable
fun Loading(text: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CircularProgressIndicator()
        Spacer(Modifier.height(16.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

enum class LoadStatus { Loading, Failed, Ready }

/** A load's state and its retry. */
class LoadHandle(val status: LoadStatus, val retry: () -> Unit)

/** How long a slow load takes: inside the spec's 2–5 s, the same on every framework. */
const val SLOW_LOAD_MS = 3000L

/**
 * The local stand-in for a network load (there is no network). It runs each time its screen opens. It waits
 * [SLOW_LOAD_MS] when [alwaysSlow] or Settings › Slow loading is on, and fails its first attempt when Settings › Fail
 * the first load is on; Retry then succeeds. With both off it is ready at once, so normal runs never see a spinner.
 */
@Composable
fun rememberLoad(alwaysSlow: Boolean = false): LoadHandle {
    var attempt by remember { mutableIntStateOf(0) }
    val slow = alwaysSlow || AppState.slowLoading
    val fail = AppState.failFirstLoad
    var status by remember(attempt) {
        mutableStateOf(if (slow) LoadStatus.Loading else if (fail && attempt == 0) LoadStatus.Failed else LoadStatus.Ready)
    }
    LaunchedEffect(attempt) {
        if (status == LoadStatus.Loading) {
            delay(SLOW_LOAD_MS)
            status = if (fail && attempt == 0) LoadStatus.Failed else LoadStatus.Ready
        }
    }
    return remember(status, attempt) { LoadHandle(status) { attempt += 1 } }
}

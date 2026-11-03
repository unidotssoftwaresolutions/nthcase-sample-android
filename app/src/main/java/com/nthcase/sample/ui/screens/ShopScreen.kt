package com.nthcase.sample.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.nthcase.sample.TestIds
import com.nthcase.sample.data.AppState
import com.nthcase.sample.data.Catalog
import com.nthcase.sample.data.Item
import com.nthcase.sample.data.formatPrice
import com.nthcase.sample.ui.GlyphTile
import com.nthcase.sample.ui.Icons
import com.nthcase.sample.ui.LabelHolder
import com.nthcase.sample.ui.LoadStatus
import com.nthcase.sample.ui.Loading
import com.nthcase.sample.ui.LocalSnack
import com.nthcase.sample.ui.SLOW_LOAD_MS
import com.nthcase.sample.ui.StateMessage
import com.nthcase.sample.ui.rememberLoad
import com.nthcase.sample.ui.show
import kotlinx.coroutines.delay

/** How long a pull-to-refresh spins when loading is not slowed. */
const val REFRESH_MS = 800L
const val REFRESHED_MESSAGE = "Catalogue is up to date"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopScreen(onOpenItem: (Int) -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    var refreshing by remember { mutableStateOf(false) }
    val load = rememberLoad()
    val snack = LocalSnack.current
    val results = remember(query) { Catalog.search(query) }

    LaunchedEffect(refreshing) {
        if (refreshing) {
            delay(if (AppState.slowLoading) SLOW_LOAD_MS else REFRESH_MS)
            refreshing = false
            snack.show(REFRESHED_MESSAGE)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag(TestIds.SHOP_SCREEN)
            .statusBarsPadding(),
    ) {
        Column(Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp)) {
            Text("Mossgrove", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.semantics { heading() })
            Spacer(Modifier.padding(top = 12.dp))
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("Search by name or number") },
                leadingIcon = { Icon(Icons.Search, contentDescription = null) },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { query = "" }, modifier = Modifier.testTag(TestIds.SHOP_SEARCH_CLEAR)) {
                            Icon(Icons.Close, contentDescription = "Clear search")
                        }
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(TestIds.SHOP_SEARCH)
                    .semantics { contentDescription = "Search" },
            )
            if (load.status == LoadStatus.Ready) {
                Text(
                    if (query.isBlank()) "${Catalog.items.size} drawers · No. 001–040"
                    else "${results.size} of ${Catalog.items.size} drawers",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .padding(top = 12.dp)
                        .testTag(TestIds.SHOP_COUNT),
                )
            }
        }
        when (load.status) {
            LoadStatus.Loading -> Loading("Opening the drawers…", Modifier.testTag(TestIds.SHOP_LOADING))
            LoadStatus.Failed -> StateMessage(
                icon = Icons.Warning,
                title = "The catalogue didn't load",
                body = "Something went wrong on our side. Your cart is safe.",
                actionLabel = "Try again",
                onAction = load.retry,
                isError = true,
                modifier = Modifier.testTag(TestIds.SHOP_ERROR),
                actionModifier = Modifier.testTag(TestIds.SHOP_RETRY),
            )
            LoadStatus.Ready -> PullToRefreshBox(
                isRefreshing = refreshing,
                onRefresh = { refreshing = true },
                modifier = Modifier.fillMaxSize(),
            ) {
                if (results.isEmpty()) {
                    StateMessage(
                        icon = Icons.Empty,
                        title = "No drawers match \"${query.trim()}\"",
                        body = "Try a shorter word, or an item number like 7.",
                        actionLabel = "Clear search",
                        onAction = { query = "" },
                        modifier = Modifier.testTag(TestIds.SHOP_EMPTY),
                        actionModifier = Modifier.testTag(TestIds.SHOP_EMPTY_ACTION),
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag(TestIds.SHOP_LIST),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(results, key = { it.id }) { item -> DrawerRow(item, onClick = { onOpenItem(item.id) }) }
                    }
                }
            }
        }
    }
}

/** One drawer front: the glyph tile, the label holder, the price. */
@Composable
private fun DrawerRow(item: Item, onClick: () -> Unit) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = Modifier
            .fillMaxWidth()
            .testTag(TestIds.shopItem(item.id))
            .clickable(onClickLabel = "Open ${item.name}", onClick = onClick)
            .semantics(mergeDescendants = true) {},
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            GlyphTile(item, 56.dp)
            Spacer(Modifier.width(12.dp))
            Box(Modifier.weight(1f)) { LabelHolder(item) }
            Spacer(Modifier.width(12.dp))
            Text(formatPrice(item.priceCents), style = MaterialTheme.typography.titleMedium)
        }
    }
}

package com.nthcase.sample.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.nthcase.sample.TestIds
import com.nthcase.sample.data.Feed
import com.nthcase.sample.ui.MossgroveTopBar
import kotlinx.coroutines.delay

/**
 * The Feed (SPEC.md §5, new in spec 1.2): a list that never ends. A LazyColumn of rows; when the third row from the end
 * of what has come shows (any part of it in the list's view, read from the list's own layout), a spinner row comes after
 * the last row, and 1 s later it goes and 20 more rows come in its place. Every visit starts again with rows 1–20.
 */
@Composable
fun FeedScreen(onBack: () -> Unit) {
    var count by remember { mutableIntStateOf(Feed.PAGE) }
    var loading by remember { mutableStateOf(false) }
    val state = rememberLazyListState()
    val lastShown by remember {
        derivedStateOf {
            val info = state.layoutInfo
            Feed.lastShown(info.visibleItemsInfo.map { Triple(it.index, it.offset, it.size) }, info.viewportStartOffset, info.viewportEndOffset, count)
        }
    }
    // the load: the spinner row for 1 s, then the next rows
    LaunchedEffect(loading) {
        if (loading) {
            delay(Feed.LOAD_MS)
            count += Feed.PAGE
            loading = false
        }
    }
    // a load starts when the third row from the end shows, and again at once after one if the new one already shows
    LaunchedEffect(lastShown, count) {
        if (Feed.startsLoad(lastShown, count, loading)) loading = true
    }

    Scaffold(modifier = Modifier.testTag(TestIds.FEED_SCREEN), topBar = { MossgroveTopBar(Feed.TITLE, onBack = onBack) }) { padding ->
        LazyColumn(state = state, modifier = Modifier.padding(padding).fillMaxSize().testTag(TestIds.FEED_LIST)) {
            items(count, key = { it + 1 }) { i -> FeedRow(i + 1) }
            if (loading) {
                item(key = "loading") { LoadingRow() }
            }
        }
    }
}

/** A row: one node with its id and its words, 56 dp high (more with a larger font), and nothing to touch. */
@Composable
private fun FeedRow(n: Int) {
    Column(Modifier.fillMaxWidth().semantics(mergeDescendants = true) {}.testTag(TestIds.feedRow(n))) {
        Row(Modifier.fillMaxWidth().heightIn(min = 56.dp).padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(Feed.row(n), style = MaterialTheme.typography.bodyLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    }
}

/** The spinner row after the last row, while the next 20 come. */
@Composable
private fun LoadingRow() {
    Row(
        Modifier.fillMaxWidth().heightIn(min = 56.dp).semantics(mergeDescendants = true) {}.testTag(TestIds.FEED_LOADING),
        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 3.dp)
        Text(Feed.LOADING, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

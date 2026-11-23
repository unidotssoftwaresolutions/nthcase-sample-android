package com.nthcase.sample.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.scrollBy as scrollByDelta
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.scrollBy
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.nthcase.sample.TestIds
import com.nthcase.sample.data.AppState
import com.nthcase.sample.data.Lobby
import com.nthcase.sample.data.Touch
import com.nthcase.sample.data.TouchLog
import com.nthcase.sample.ui.GlyphTile
import com.nthcase.sample.ui.Icons
import com.nthcase.sample.ui.ItemNumber
import com.nthcase.sample.ui.Art
import com.nthcase.sample.ui.MossgroveTopBar
import com.nthcase.sample.ui.testIdRoot
import com.nthcase.sample.ui.theme.LocalAisleTints
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** What takes the place of the pager, the Top 10 row and the page (SPEC.md §5, Lobby › Panels). */
private sealed interface Panel {
    data object None : Panel

    /** [row] 1–4, or [Lobby.TOP_ROW] for the Top 10 row. */
    data class Detail(val row: Int, val tile: Int) : Panel
    data class Promo(val banner: Int) : Panel
    data object Saved : Panel
}

/** What a quick sheet was opened for: its title, and what its Open opens. */
private data class SheetFor(val title: String, val opens: Panel)

/** Every scroll position of one visit, kept above the panels so that closing a panel finds each scroller where it was. */
private class LobbyScrolls(
    val pager: PagerState,
    val top: LazyListState,
    val page: LazyListState,
    val chips: LazyListState,
    val rows: List<LazyListState>,
    val inner: LazyListState,
    val boardX: androidx.compose.foundation.ScrollState,
    val boardY: androidx.compose.foundation.ScrollState,
)

/**
 * The Lobby, built with Compose (SPEC.md §5, new in 1.1): a test bench for scrolling. The promo pager sits above the
 * page so that it is the first scrollable in the tree. Under it, outside the page, the Top 10 row, which is the first
 * scrollable when "Show the pager" leaves the pager out of the composition. The touch log at the top names every tap,
 * long press and double tap, and nothing that scrolls writes to it.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LobbyScreen(onBack: () -> Unit) {
    val log = remember { TouchLog() }
    var panel by remember { mutableStateOf<Panel>(Panel.None) }
    var sheet by remember { mutableStateOf<SheetFor?>(null) }
    var barSelected by remember { mutableStateOf("Home") }
    var chip by remember { mutableIntStateOf(1) }
    var auto by remember { mutableStateOf(false) }

    // Saved appears late: absent from the tree until the delay has passed. The count starts when the Lobby opens and
    // again when a delay is chosen (the clock's epoch).
    var epoch by remember { mutableIntStateOf(0) }
    val delaySeconds = AppState.lobbySavedDelay
    var savedShown by remember(epoch) { mutableStateOf(delaySeconds == 0) }
    LaunchedEffect(epoch) {
        if (!savedShown) {
            delay(delaySeconds * 1000L)
            savedShown = true
        }
    }

    val scrolls = LobbyScrolls(
        pager = rememberPagerState { Lobby.BANNERS },
        top = rememberLazyListState(),
        page = rememberLazyListState(),
        chips = rememberLazyListState(),
        rows = List(Lobby.ROWS) { rememberLazyListState() },
        inner = rememberLazyListState(),
        boardX = rememberScrollState(),
        boardY = rememberScrollState(),
    )

    val closePanel = { panel = Panel.None }
    BackHandler(enabled = panel is Panel.Detail || panel is Panel.Promo) { closePanel() }

    Scaffold(
        modifier = Modifier.testTag(TestIds.LOBBY_SCREEN),
        topBar = { MossgroveTopBar("Lobby", onBack = { if (panel is Panel.Detail || panel is Panel.Promo) closePanel() else onBack() }) },
        bottomBar = {
            NavigationBar(modifier = Modifier.testTag(TestIds.LOBBY_BAR)) {
                Lobby.barItems.forEachIndexed { i, label ->
                    // Saved is left out of the bar, not hidden: until its delay has passed, the tree has no node for it
                    if (label != "Saved" || savedShown) {
                        NavigationBarItem(
                            selected = barSelected == label,
                            onClick = {
                                log.add(Touch.Tap, label, "bar")
                                barSelected = label
                                panel = if (label == "Saved") Panel.Saved else Panel.None
                            },
                            icon = { Icon(barIcon(label), contentDescription = null) },
                            label = { Text(label) },
                            modifier = Modifier.testTag(TestIds.lobbyBarItems[i]),
                        )
                    }
                }
            }
        },
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            TouchLogBlock(log)
            DelayChoice(
                selected = delaySeconds,
                onChoose = { seconds ->
                    AppState.lobbySavedDelay = seconds
                    epoch += 1
                },
                pagerShown = AppState.lobbyPagerShown,
                onPagerShown = { AppState.lobbyPagerShown = it },
            )
            when (val p = panel) {
                Panel.None -> {
                    // off, the pager and its position line are left out of the composition: no node in the tree, not hidden
                    if (AppState.lobbyPagerShown) {
                        PagerBlock(scrolls.pager, auto, onAuto = { auto = it }, log = log, onPromo = { panel = Panel.Promo(it) }, onSheet = { sheet = it })
                    }
                    TopRow(scrolls.top, log, onDetail = { tile -> panel = Panel.Detail(Lobby.TOP_ROW, tile) }, onSheet = { sheet = it })
                    Page(
                        scrolls = scrolls,
                        log = log,
                        chip = chip,
                        onChip = { chip = it },
                        onDetail = { row, tile -> panel = Panel.Detail(row, tile) },
                        onSheet = { sheet = it },
                        modifier = Modifier.weight(1f),
                    )
                }
                is Panel.Detail -> DetailPanel(p, onBack = closePanel, modifier = Modifier.weight(1f))
                is Panel.Promo -> PromoPanel(p, onBack = closePanel, modifier = Modifier.weight(1f))
                Panel.Saved -> SavedPanel(Modifier.weight(1f))
            }
        }
    }

    sheet?.let { target ->
        QuickSheet(
            target = target,
            onButton = { label ->
                log.add(Touch.Tap, label, "quick sheet, ${target.title}")
                sheet = null
                if (label == "Open") panel = target.opens
            },
            onDismiss = { sheet = null },
        )
    }
}

private fun barIcon(label: String) = when (label) {
    "Home" -> Icons.Shop
    "Live" -> Icons.Eye
    "Saved" -> Icons.Check
    else -> Icons.Cart
}

/** Tap, long press and double tap, each the platform's own; the log line is the only thing the Lobby adds. */
@OptIn(ExperimentalFoundationApi::class)
private fun Modifier.touchable(
    log: TouchLog,
    what: String,
    where: String,
    onTap: (() -> Unit)? = null,
    onLongPress: (() -> Unit)? = null,
): Modifier = combinedClickable(
    onClick = { log.add(Touch.Tap, what, where); onTap?.invoke() },
    onLongClick = { log.add(Touch.LongPress, what, where); onLongPress?.invoke() },
    onDoubleClick = { log.add(Touch.DoubleTap, what, where) },
)

@Composable
private fun TouchLogBlock(log: TouchLog) {
    Surface(color = MaterialTheme.colorScheme.surfaceContainerLow, modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 4.dp), verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f)) {
                Text("Touch log", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    log.text,
                    style = MaterialTheme.typography.bodySmall,
                    minLines = 3,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.testTag(TestIds.LOBBY_TOUCH_LOG),
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(log.countText, style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(end = 8.dp).testTag(TestIds.LOBBY_TOUCH_COUNT))
                TextButton(onClick = { log.reset() }, modifier = Modifier.testTag(TestIds.LOBBY_TOUCH_RESET)) { Text("Reset") }
            }
        }
    }
}

@Composable
private fun DelayChoice(selected: Int, onChoose: (Int) -> Unit, pagerShown: Boolean, onPagerShown: (Boolean) -> Unit) {
    Column(Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                "Saved appears after",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
            )
            Row(
                Modifier
                    .toggleable(value = pagerShown, role = Role.Switch, onValueChange = onPagerShown)
                    .testTag(TestIds.LOBBY_PAGER_SHOWN)
                    .padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Show the pager", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(end = 8.dp))
                Switch(checked = pagerShown, onCheckedChange = null)
            }
        }
        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth().testTag(TestIds.LOBBY_SAVED_DELAY)) {
            Lobby.DELAYS.forEachIndexed { i, seconds ->
                val active = seconds == selected
                SegmentedButton(
                    selected = active,
                    onClick = { onChoose(seconds) },
                    shape = SegmentedButtonDefaults.itemShape(i, Lobby.DELAYS.size),
                    // no check icon: five fit a phone's width without it; the chosen one is bold as well as filled
                    icon = {},
                    label = { Text("$seconds s", fontWeight = if (active) FontWeight.Bold else FontWeight.Normal, maxLines = 1) },
                    modifier = Modifier.testTag(TestIds.lobbySavedDelay(seconds)),
                )
            }
        }
    }
}

@Composable
private fun PagerBlock(
    state: PagerState,
    auto: Boolean,
    onAuto: (Boolean) -> Unit,
    log: TouchLog,
    onPromo: (Int) -> Unit,
    onSheet: (SheetFor) -> Unit,
) {
    LaunchedEffect(auto) {
        while (auto) {
            delay(Lobby.AUTO_ADVANCE_MS)
            state.animateScrollToPage((state.currentPage + 1) % Lobby.BANNERS)
        }
    }
    HorizontalPager(
        state = state,
        contentPadding = PaddingValues(horizontal = 16.dp),
        pageSpacing = 8.dp,
        modifier = Modifier.fillMaxWidth().height(104.dp).padding(top = 4.dp).testTag(TestIds.LOBBY_PAGER),
    ) { index ->
        val n = index + 1
        val title = Lobby.banners[index]
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.shapes.medium)
                .touchable(log, title, "banner $n", onTap = { onPromo(n) }, onLongPress = { onSheet(SheetFor(title, Panel.Promo(n))) })
                .testTag(TestIds.lobbyBanner(n))
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
            Text("A test banner", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
        }
    }
    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
        val position = Lobby.pagerPosition(state.currentPage + 1)
        Row(
            Modifier.weight(1f).semantics(mergeDescendants = true) { contentDescription = position }.testTag(TestIds.LOBBY_PAGER_POSITION),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            repeat(Lobby.BANNERS) { i ->
                val on = i == state.currentPage
                Box(
                    Modifier
                        .padding(end = 6.dp)
                        .size(if (on) 10.dp else 6.dp)
                        .background(if (on) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant, CircleShape),
                )
            }
            Spacer(Modifier.width(6.dp))
            Text(position, style = MaterialTheme.typography.labelMedium)
        }
        Row(
            Modifier
                .toggleable(value = auto, role = Role.Switch, onValueChange = onAuto)
                .testTag(TestIds.LOBBY_PAGER_AUTO)
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Auto-advance", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(end = 8.dp))
            Switch(checked = auto, onCheckedChange = null)
        }
    }
}

/**
 * The Top 10 row (SPEC.md §5, 1.1): fixed, outside the page, under the pager or in its place. Nothing vertical encloses
 * it, so a vertical swipe across it is claimed by nobody: the case of the app where the fault was first seen.
 */
@Composable
private fun TopRow(state: LazyListState, log: TouchLog, onDetail: (Int) -> Unit, onSheet: (SheetFor) -> Unit) {
    val position by remember(state) { derivedStateOf { fullyVisible(state).let { (a, b) -> Lobby.topRowPosition(a, b) } } }
    Text(
        position,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 4.dp)
            .semantics { heading(); contentDescription = position }
            .testTag(TestIds.LOBBY_TOP_ROW_POSITION),
    )
    LazyRow(
        state = state,
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth().testTag(TestIds.LOBBY_TOP_ROW),
    ) {
        items(Lobby.TOP_TILES) { i ->
            val tile = i + 1
            val item = Lobby.topItem(tile)
            val tint = LocalAisleTints.current.of(item.category)
            Column(
                Modifier
                    .width(100.dp)
                    .touchable(
                        log, item.name, Lobby.tileWhere(Lobby.TOP_ROW, tile),
                        onTap = { onDetail(tile) },
                        onLongPress = { onSheet(SheetFor(item.name, Panel.Detail(Lobby.TOP_ROW, tile))) },
                    )
                    .testTag(TestIds.lobbyTopRowTile(tile)),
            ) {
                // a band 100 × 64, lower than a page row's tile, so the page keeps its room under the fixed row
                Box(Modifier.size(100.dp, 64.dp).background(tint.tile, MaterialTheme.shapes.small), contentAlignment = Alignment.Center) {
                    Icon(Art.of(item.glyph), contentDescription = null, tint = tint.ink, modifier = Modifier.size(40.dp))
                }
                Text(
                    item.name,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
    }
}

@Composable
private fun Page(
    scrolls: LobbyScrolls,
    log: TouchLog,
    chip: Int,
    onChip: (Int) -> Unit,
    onDetail: (Int, Int) -> Unit,
    onSheet: (SheetFor) -> Unit,
    modifier: Modifier,
) {
    LazyColumn(state = scrolls.page, modifier = modifier.fillMaxWidth().testTag(TestIds.LOBBY_PAGE)) {
        item { Chips(scrolls.chips, log, chip, onChip) }
        for (row in 1..Lobby.ROWS) {
            item { TileRow(row, scrolls.rows[row - 1], log, onDetail, onSheet) }
        }
        item { Grid(log) }
        item { InnerList(scrolls.inner, log) }
        item { Board(scrolls.boardX, scrolls.boardY, log) }
        item {
            Column(
                Modifier.fillMaxWidth().semantics(mergeDescendants = true) {}.testTag(TestIds.LOBBY_FOOTER).padding(top = 24.dp, bottom = 48.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(Lobby.FOOTER, style = MaterialTheme.typography.titleMedium)
                Text(Lobby.FOOTER_NOTE, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun SectionHeading(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp).semantics { heading() },
    )
}

@Composable
private fun Chips(state: LazyListState, log: TouchLog, selectedChip: Int, onSelect: (Int) -> Unit) {
    LazyRow(
        state = state,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth().testTag(TestIds.LOBBY_CHIPS),
    ) {
        items(Lobby.chips.size) { i ->
            val n = i + 1
            val label = Lobby.chips[i]
            val on = n == selectedChip
            // Material's FilterChip takes no long press, so the chip is drawn here on combinedClickable
            Row(
                modifier = Modifier
                    .heightIn(min = 32.dp)
                    .background(if (on) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surface, MaterialTheme.shapes.small)
                    .border(1.dp, if (on) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.outline, MaterialTheme.shapes.small)
                    .touchable(log, label, "chip $n", onTap = { onSelect(n) })
                    .semantics { selected = on }
                    .testTag(TestIds.lobbyChip(n))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (on) Icon(Icons.Check, contentDescription = null, modifier = Modifier.padding(end = 6.dp).size(16.dp))
                Text(label, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

/** The first and last fully visible items of a row, from 1. */
private fun fullyVisible(state: LazyListState): Pair<Int, Int> {
    val info = state.layoutInfo
    val full = info.visibleItemsInfo.filter { it.offset >= info.viewportStartOffset && it.offset + it.size <= info.viewportEndOffset }
    val shown = full.ifEmpty { info.visibleItemsInfo.take(1) }
    val first = (shown.firstOrNull()?.index ?: 0) + 1
    return first to ((shown.lastOrNull()?.index ?: 0) + 1)
}

@Composable
private fun TileRow(row: Int, state: LazyListState, log: TouchLog, onDetail: (Int, Int) -> Unit, onSheet: (SheetFor) -> Unit) {
    val position by remember(state) { derivedStateOf { fullyVisible(state).let { (a, b) -> Lobby.rowPosition(row, a, b) } } }
    SectionHeading(position, Modifier.semantics { contentDescription = position }.testTag(TestIds.lobbyRowPosition(row)))
    LazyRow(
        state = state,
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth().testTag(TestIds.lobbyRow(row)),
    ) {
        items(Lobby.TILES) { i ->
            val tile = i + 1
            val item = Lobby.tileItem(row, tile)
            Column(
                Modifier
                    .width(100.dp)
                    .touchable(
                        log, item.name, "row $row, tile $tile",
                        onTap = { onDetail(row, tile) },
                        onLongPress = { onSheet(SheetFor(item.name, Panel.Detail(row, tile))) },
                    )
                    .testTag(TestIds.lobbyRowTile(row, tile))
                    .padding(bottom = 4.dp),
            ) {
                GlyphTile(item, 100.dp)
                Text(
                    item.name,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 4.dp),
                )
                ItemNumber(item)
            }
        }
    }
}

@Composable
private fun Grid(log: TouchLog) {
    SectionHeading("Grid")
    Column(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp).testTag(TestIds.LOBBY_GRID),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        for (r in 0 until Lobby.GRID_CELLS / 2) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                for (c in 1..2) {
                    val n = r * 2 + c
                    val item = Lobby.gridItem(n)
                    Row(
                        Modifier
                            .weight(1f)
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, MaterialTheme.shapes.small)
                            .touchable(log, item.name, "grid, cell $n")
                            .testTag(TestIds.lobbyGridCell(n))
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        GlyphTile(item, 40.dp)
                        Text(item.name, style = MaterialTheme.typography.bodySmall, maxLines = 2, modifier = Modifier.padding(start = 8.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun InnerList(state: LazyListState, log: TouchLog) {
    SectionHeading("Inner list")
    LazyColumn(
        state = state,
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
            .padding(horizontal = 16.dp)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, MaterialTheme.shapes.small)
            .testTag(TestIds.LOBBY_INNER_LIST),
    ) {
        items(Lobby.INNER_LINES) { i ->
            val n = i + 1
            Column(Modifier.fillMaxWidth().touchable(log, "Line $n", "inner list").testTag(TestIds.lobbyInnerRow(n))) {
                Text(Lobby.innerLine(n), style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(horizontal = 12.dp, vertical = 14.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            }
        }
    }
}

private val BoardCell = 56.dp

/** ScrollableState.scrollBy, named apart from the semantics action of the same name. */
private suspend fun androidx.compose.foundation.ScrollState.moveBy(delta: Float) {
    scrollByDelta(delta)
}

@Composable
private fun Board(x: androidx.compose.foundation.ScrollState, y: androidx.compose.foundation.ScrollState, log: TouchLog) {
    val cellPx = with(LocalDensity.current) { BoardCell.toPx() }
    val scope = rememberCoroutineScope()
    // the top-left fully visible cell: a cell scrolled partly out is not counted
    val position by remember(x, y, cellPx) {
        derivedStateOf {
            val col = kotlin.math.ceil(x.value / cellPx - 0.001f).toInt() + 1
            val row = kotlin.math.ceil(y.value / cellPx - 0.001f).toInt() + 1
            Lobby.boardPosition(col.coerceIn(1, Lobby.BOARD), row.coerceIn(1, Lobby.BOARD))
        }
    }
    SectionHeading(position, Modifier.semantics { contentDescription = position }.testTag(TestIds.LOBBY_BOARD_POSITION))
    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        // one node that scrolls both ways: both scroll modifiers sit on the same layout
        Box(
            Modifier
                .size(BoardCell * 5)
                .border(1.dp, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.extraSmall)
                .clipToBounds()
                .testTag(TestIds.LOBBY_BOARD)
                // Two scroll modifiers on one node each bring a ScrollBy action, and the node keeps only one of them, so
                // an accessibility scroll would move one axis. This one moves both, and is the one the node keeps.
                .semantics {
                    scrollBy { dx, dy ->
                        scope.launch { x.moveBy(dx) }
                        scope.launch { y.moveBy(dy) }
                        true
                    }
                }
                .horizontalScroll(x)
                .verticalScroll(y),
        ) {
            Column {
                for (row in 1..Lobby.BOARD) {
                    Row {
                        for (col in 1..Lobby.BOARD) {
                            val label = Lobby.boardLabel(col, row)
                            Box(
                                Modifier
                                    .size(BoardCell)
                                    .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
                                    .touchable(log, label, "board")
                                    .testTag(TestIds.lobbyBoardCell(col, row)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(label, style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PanelColumn(id: String, modifier: Modifier, content: @Composable () -> Unit) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {}
            .testTag(id)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
    ) { content() }
}

@Composable
private fun DetailPanel(p: Panel.Detail, onBack: () -> Unit, modifier: Modifier) {
    val item = Lobby.itemAt(p.row, p.tile)
    PanelColumn(TestIds.LOBBY_DETAIL, modifier) {
        GlyphTile(item, 96.dp, description = "Drawing of the ${item.name}")
        Text(item.name, style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
        ItemNumber(item)
        Text(Lobby.detailPlace(p.row, p.tile), style = MaterialTheme.typography.bodyMedium)
        OutlinedButton(onClick = onBack, modifier = Modifier.padding(top = 8.dp).testTag(TestIds.LOBBY_DETAIL_BACK)) { Text(Lobby.BACK_TO_LOBBY) }
    }
}

@Composable
private fun PromoPanel(p: Panel.Promo, onBack: () -> Unit, modifier: Modifier) {
    PanelColumn(TestIds.LOBBY_PROMO, modifier) {
        Text(Lobby.banners[p.banner - 1], style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
        Text(Lobby.pagerPosition(p.banner), style = MaterialTheme.typography.labelLarge)
        Text(Lobby.PROMO_NOTE, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
        OutlinedButton(onClick = onBack, modifier = Modifier.padding(top = 8.dp).testTag(TestIds.LOBBY_PROMO_BACK)) { Text(Lobby.BACK_TO_LOBBY) }
    }
}

@Composable
private fun SavedPanel(modifier: Modifier) {
    PanelColumn(TestIds.LOBBY_SAVED_SCREEN, modifier) {
        Icon(Icons.Check, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(40.dp))
        Text(Lobby.SAVED_EMPTY, style = MaterialTheme.typography.titleLarge)
    }
}

/**
 * The quick sheet: Material 3's ModalBottomSheet, a window of its own. Its content is half the window high and the
 * sheet skips its partly open state, so it opens whole at that height and never grows. The title and the four buttons
 * stay put; under them, More like this (spec 1.2) is the sheet's own LazyColumn, which fills the rest, so its 20 lines
 * run past the sheet's foot and come only by scrolling inside it.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuickSheet(target: SheetFor, onButton: (String) -> Unit, onDismiss: () -> Unit) {
    val state = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val height = (LocalConfiguration.current.screenHeightDp * Lobby.SHEET_HEIGHT_FRACTION).dp
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = state) {
        // the sheet is a window of its own, so the ids are exposed again inside it
        Column(Modifier.testIdRoot().testTag(TestIds.LOBBY_SHEET).fillMaxWidth().height(height).padding(start = 16.dp, end = 16.dp)) {
            Text(
                target.title,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 12.dp).semantics { heading() }.testTag(TestIds.LOBBY_SHEET_TITLE),
            )
            Lobby.sheetButtons.forEachIndexed { i, label ->
                TextButton(
                    onClick = { onButton(label) },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag(TestIds.lobbySheetButtons[i]),
                ) { Text(label, modifier = Modifier.fillMaxWidth()) }
            }
            Text(
                Lobby.MORE_LIKE_THIS,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp).semantics { heading() },
            )
            LazyColumn(Modifier.fillMaxWidth().weight(1f).testTag(TestIds.LOBBY_SHEET_MORE)) {
                items(Lobby.SHEET_MORE_LINES) { i ->
                    val n = i + 1
                    val line = Lobby.sheetMoreLine(target.title, n)
                    // touched as a button is: a tap is logged and closes the sheet
                    Column(Modifier.fillMaxWidth().clickable { onButton(line) }.testTag(TestIds.lobbySheetMoreRow(n))) {
                        Text(
                            line,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.heightIn(min = 48.dp).padding(vertical = 13.dp),
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    }
                }
            }
        }
    }
}

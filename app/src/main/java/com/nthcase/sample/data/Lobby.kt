package com.nthcase.sample.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * The Lobby (SPEC.md §5, new in 1.1): what both of its builds share, the Compose one and the Android Views one. The
 * names, the numbers and the texts are the spec's; the two screens only draw them.
 */
object Lobby {
    const val BANNERS = 5
    const val ROWS = 4
    const val TILES = 20
    const val GRID_CELLS = 12
    const val INNER_LINES = 30
    const val BOARD = 26

    /** The Top 10 row: fixed, outside the page, under the pager (or in its place when the pager is off). */
    const val TOP_TILES = 10

    /** "Saved appears after": the choices, in seconds. 0 is the default; see SPEC.md §5 for why each one. */
    val DELAYS = listOf(0, 3, 6, 12, 20)

    /** Auto-advance moves the pager every 4 s when it is on. */
    const val AUTO_ADVANCE_MS = 4_000L

    val banners = listOf(
        "New in the Kitchen aisle",
        "Warm, low desk lamps",
        "Ferns that like the shade",
        "Candles for long evenings",
        "Totes for market day",
    )

    val chips = listOf("All", "Kitchen", "Desk", "Garden", "Home", "Under $20", "Under $40", "New", "Gifts", "Sets", "Lamps", "Candles")

    /** Tile [m] of row [n] (both from 1) shows item ((n − 1) × 10 + m − 1) mod 40 + 1. */
    fun tileItem(row: Int, tile: Int): Item = Catalog.items[((row - 1) * 10 + tile - 1) % Catalog.items.size]

    /** Tile [m] (from 1) of the Top 10 row shows item m + 30: No. 031 to No. 040. */
    fun topItem(tile: Int): Item = Catalog.items[tile + 29]

    /** Grid cell [n] (from 1) shows item n + 28. */
    fun gridItem(cell: Int): Item = Catalog.items[cell + 27]

    /** "A1" … "Z26": column letter, then row number, both from 1. */
    fun boardLabel(col: Int, row: Int): String = "${'A' + (col - 1)}$row"

    fun pagerPosition(page: Int): String = "Banner $page of $BANNERS"

    /** The first and last fully visible tiles of a row; [first] alone when only one is. */
    fun rowPosition(row: Int, first: Int, last: Int): String =
        if (last <= first) "Row $row: tile $first of $TILES" else "Row $row: tiles $first–$last of $TILES"

    /** "Top 10: tiles 1–3 of 10"; [first] alone when only one is fully visible. */
    fun topRowPosition(first: Int, last: Int): String =
        if (last <= first) "Top 10: tile $first of $TOP_TILES" else "Top 10: tiles $first–$last of $TOP_TILES"

    /** The detail's place line: "Row 3, tile 15", or "Top 10, tile 2" for row 0, the Top 10 row. */
    fun detailPlace(row: Int, tile: Int): String = if (row == TOP_ROW) "Top 10, tile $tile" else "Row $row, tile $tile"

    /** The item a tile shows, for a row from 1 to 4 or [TOP_ROW]. */
    fun itemAt(row: Int, tile: Int): Item = if (row == TOP_ROW) topItem(tile) else tileItem(row, tile)

    /** Where the log says a tile was touched: "row 3, tile 15" or "top row, tile 2". */
    fun tileWhere(row: Int, tile: Int): String = if (row == TOP_ROW) "top row, tile $tile" else "row $row, tile $tile"

    /** The Top 10 row's number where a row is asked for: it is not one of the page's rows 1–4. */
    const val TOP_ROW = 0

    fun boardPosition(col: Int, row: Int): String = "Board: top-left ${boardLabel(col, row)}"

    fun innerLine(n: Int): String = "Line $n of $INNER_LINES"

    const val NOTHING_TOUCHED = "Nothing touched"
    const val SAVED_EMPTY = "Saved: nothing yet"
    const val FOOTER = "End of the lobby"
    const val FOOTER_NOTE = "A test bench: nothing here is for sale."
    const val PROMO_NOTE = "A test banner. Nothing here is for sale."
    const val BACK_TO_LOBBY = "Back to the lobby"
    val barItems = listOf("Home", "Live", "Saved", "Offers")
    val sheetButtons = listOf("Open", "Preview", "Saved", "Rate")
}

/** How a touch came: the words the touch log starts each line with. */
enum class Touch(val words: String) { Tap("Tapped"), LongPress("Long-pressed"), DoubleTap("Double-tapped") }

/**
 * The Lobby's oracle: every tap, long press and double tap on a touchable element, newest first. It shows at most
 * three lines; the count keeps every touch since the last reset. Nothing that scrolls writes here.
 */
class TouchLog {
    var lines by mutableStateOf(emptyList<String>())
        private set
    var count by mutableIntStateOf(0)
        private set

    /** The Views build redraws its log when this is called; Compose reads the state directly. */
    var onChange: (() -> Unit)? = null

    val text: String get() = if (lines.isEmpty()) Lobby.NOTHING_TOUCHED else lines.joinToString("\n")
    val countText: String get() = "Touches: $count"

    fun add(touch: Touch, what: String, where: String) {
        lines = (listOf("${touch.words}: $what ($where)") + lines).take(3)
        count += 1
        onChange?.invoke()
    }

    fun reset() {
        lines = emptyList()
        count = 0
        onChange?.invoke()
    }
}

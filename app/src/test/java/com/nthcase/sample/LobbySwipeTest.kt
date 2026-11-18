package com.nthcase.sample

import android.graphics.Rect
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.isRoot
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onLast
import androidx.compose.ui.test.performTouchInput
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.RecyclerView
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.viewpager2.widget.ViewPager2
import com.nthcase.sample.data.Lobby
import com.nthcase.sample.lobby.LobbyViewsFragment
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/**
 * The touch that started spec 1.1, shown without a device. `UiScrollable.scrollIntoView` swipes the first scrollable in
 * the tree vertically (UiScrollableSwipe has the numbers and where they come from). Here that gesture is injected as
 * MotionEvents through the real tree of each Lobby: across the promo pager, which is the first scrollable; across a
 * tile row inside the page; and, with "Show the pager" off, across the Top 10 row, which is then the first scrollable:
 * a row of tiles that nothing vertical encloses, the kind of row that was first in the app where it happened. Each run records what the touched child
 * received, which scroller (if any) took the drag, and what the touch log says. This is evidence, not a fix, and not a
 * pass/fail of the app: each expected outcome below is what the run showed, pinned so that a library update that
 * changes it is noticed.
 */
@RunWith(AndroidJUnit4::class)
class LobbySwipeTest {
    private val compose = appCompose()

    @get:Rule
    val rules = coldStart(compose)

    private fun out(name: String, lines: List<String>) {
        val dir = File("build/lobby/swipe").apply { mkdirs() }
        File(dir, "$name.txt").writeText(lines.joinToString("\n") + "\n")
    }

    private fun platform(): List<String> {
        val vc = ViewConfiguration.get(compose.activity)
        return listOf(
            "View long-press timeout ${ViewConfiguration.getLongPressTimeout()} ms, tap timeout ${ViewConfiguration.getTapTimeout()} ms, " +
                "double-tap timeout ${ViewConfiguration.getDoubleTapTimeout()} ms, touch slop ${vc.scaledTouchSlop} px, density ${compose.activity.resources.displayMetrics.density}",
        )
    }

    // ── Views ────────────────────────────────────────────────────────────────────────────────────────────────────────

    private val fragment: LobbyViewsFragment
        get() = compose.activity.supportFragmentManager.fragments.filterIsInstance<LobbyViewsFragment>().single()

    private fun <T : View> view(id: String): T = compose.activity.window.decorView.findViewById(fragment.idOf(id))

    /** The view under (x, y) among [candidates], in screen coordinates. */
    private fun under(x: Int, y: Int, candidates: List<View>): View? = candidates.firstOrNull { v ->
        val r = Rect().also(v::getGlobalVisibleRect)
        v.isShown && r.contains(x, y)
    }

    private fun viewsRun(name: String, targetId: String?, stepMs: Long, pagerOff: Boolean = false): List<String> {
        compose.signIn()
        compose.openLab(TestIds.LAB_LOBBY_VIEWS)
        Finger.idle(100)
        if (pagerOff) {
            view<View>(TestIds.LOBBY_PAGER_SHOWN).performClick()
            Finger.idle(100)
        }
        val decor = compose.activity.window.decorView
        val tree = A11yTree.visible(decor)
        val first = tree.first { it.scrollable }
        val target = if (targetId == null) first else tree.first { it.resourceId == "com.nthcase.sample:id/$targetId" }
        val lines = mutableListOf("Lobby (Views)${if (pagerOff) ", Show the pager off" else ""}, swipe across ${target.resourceId ?: target.className} ${target.bounds}, $stepMs ms a step")
        lines += "first scrollable in the tree: ${first.resourceId} ${first.bounds}"
        lines += platform()

        // what the child under the finger receives, and which scroller starts dragging
        val events = mutableListOf<String>()
        // with the pager off, its frame is not in the window, so there is no pager to find
        val pager: ViewPager2? = decor.findViewById(fragment.idOf(TestIds.LOBBY_PAGER))
        val page = view<NestedScrollView>(TestIds.LOBBY_PAGE)
        val rows = (1..Lobby.ROWS).map { view<RecyclerView>(TestIds.lobbyRow(it)) }
        val chips = view<RecyclerView>(TestIds.LOBBY_CHIPS)
        val topRow = view<RecyclerView>(TestIds.LOBBY_TOP_ROW)
        val claimed = linkedSetOf<String>()
        topRow.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) { if (newState == RecyclerView.SCROLL_STATE_DRAGGING) claimed += "lobby_top_row (RecyclerView)" }
        })
        pager?.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageScrollStateChanged(state: Int) { if (state == ViewPager2.SCROLL_STATE_DRAGGING) claimed += "lobby_pager (ViewPager2)" }
        })
        rows.forEachIndexed { i, rv ->
            rv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) { if (newState == RecyclerView.SCROLL_STATE_DRAGGING) claimed += "lobby_row_${i + 1} (RecyclerView)" }
            })
        }
        chips.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) { if (newState == RecyclerView.SCROLL_STATE_DRAGGING) claimed += "lobby_chips (RecyclerView)" }
        })
        var pageFrom = -1
        page.setOnScrollChangeListener { _: NestedScrollView, _, y, _, oldY ->
            if (y != oldY) {
                if (pageFrom < 0) pageFrom = oldY
                claimed.removeAll { it.startsWith("lobby_page") }
                claimed += "lobby_page (NestedScrollView), scrolled from $pageFrom to $y px"
            }
        }

        val swipe = { forward: Boolean, label: String ->
            val points = UiScrollableSwipe.points(target.bounds, forward)
            val (x0, y0) = points.first()
            val touchables = (1..Lobby.BANNERS).mapNotNull { n -> decor.findViewById<View>(fragment.idOf(TestIds.lobbyBanner(n))) } +
                (1..Lobby.TOP_TILES).mapNotNull { m -> decor.findViewById<View>(fragment.idOf(TestIds.lobbyTopRowTile(m))) } +
                (1..Lobby.ROWS).flatMap { r -> (1..Lobby.TILES).mapNotNull { m -> decor.findViewById<View>(fragment.idOf(TestIds.lobbyRowTile(r, m))) } }
            val child = under(x0, y0, touchables)
            events.clear()
            child?.setOnTouchListener { _, ev -> events += MotionEvent.actionToString(ev.actionMasked); false }
            val sheetBefore = fragment.openSheet
            // the touch goes to the window on top, as the input system sends it
            val window = sheetBefore?.window?.decorView ?: decor
            val finger = Finger(window)
            finger.down(x0, y0)
            points.drop(1).dropLast(1).forEach { (x, y) -> Finger.idle(stepMs); finger.move(x, y) }
            Finger.idle(stepMs)
            val (x1, y1) = points.last()
            finger.up(x1, y1)
            Finger.idle(400) // past the double-tap time, so a tap has been counted
            child?.setOnTouchListener(null)
            val cancelled = events.contains("ACTION_CANCEL")
            val moved = if (events.size > 2) events.distinct().joinToString(" → ") else events.joinToString(" → ")
            lines += "$label swipe ${if (forward) "forward (finger up)" else "backward (finger down)"} from ($x0, $y0) to ($x1, $y1)" +
                (if (sheetBefore != null) ", into the quick sheet's window" else "")
            lines += "  child under the finger: ${child?.let { compose.activity.resources.getResourceEntryName(it.id) } ?: "none"}; it received: ${moved.ifEmpty { "nothing" }}" +
                if (cancelled) " (a parent intercepted the gesture)" else ""
            lines += "  scrollers that took the drag: ${claimed.ifEmpty { setOf("none") }.joinToString()}"
            lines += "  touch log: ${fragment.log.text.replace("\n", " | ")} (${fragment.log.countText})"
            lines += "  quick sheet open: ${fragment.openSheet != null}; promo shown: ${view<View>(TestIds.LOBBY_PROMO).visibility == View.VISIBLE}; " +
                "detail shown: ${view<View>(TestIds.LOBBY_DETAIL).visibility == View.VISIBLE}; " +
                (pager?.let { "pager on banner ${it.currentItem + 1}" } ?: "no pager in the window") +
                "; ${view<android.widget.TextView>(TestIds.LOBBY_TOP_ROW_POSITION).text}"
            claimed.clear()
            pageFrom = -1
        }
        // scrollIntoView on a scroller that never scrolls this way: scrollToBeginning's one backward swipe, then one forward
        swipe(false, "1.")
        Finger.idle(263) // the gap between run 00ec77f5's two touches
        swipe(true, "2.")
        out(name, lines)
        return lines
    }

    @Test
    fun views_pager_at_the_floor_speed() {
        val lines = viewsRun("views-pager-5ms", null, UiScrollableSwipe.NOMINAL_STEP_MS)
        assertEquals(true, lines.any { it.contains("touch log: Tapped: New in the Kitchen aisle (banner 1)") })
        assertEquals(true, lines.any { it.contains("it received: ACTION_DOWN → ACTION_MOVE → ACTION_UP") })
    }

    @Test
    fun views_pager_at_the_measured_speed() {
        val lines = viewsRun("views-pager-19ms", null, UiScrollableSwipe.MEASURED_STEP_MS)
        assertEquals(true, lines.any { it.contains("touch log: Long-pressed: New in the Kitchen aisle (banner 1)") })
        assertEquals(true, lines.any { it.contains("quick sheet open: true") })
    }

    @Test
    fun views_tile_row_at_the_floor_speed() {
        val lines = viewsRun("views-row1-5ms", TestIds.lobbyRow(1), UiScrollableSwipe.NOMINAL_STEP_MS)
        assertEquals(true, lines.filter { it.contains("touch log:") }.all { it.contains(Lobby.NOTHING_TOUCHED) })
        assertEquals(true, lines.any { it.contains("(a parent intercepted the gesture)") })
    }

    @Test
    fun views_tile_row_at_the_measured_speed() {
        val lines = viewsRun("views-row1-19ms", TestIds.lobbyRow(1), UiScrollableSwipe.MEASURED_STEP_MS)
        assertEquals(true, lines.filter { it.contains("touch log:") }.all { it.contains(Lobby.NOTHING_TOUCHED) })
        assertEquals(true, lines.any { it.contains("(a parent intercepted the gesture)") })
    }

    @Test
    fun views_top_row_with_the_pager_off_at_the_floor_speed() {
        val lines = viewsRun("views-top-row-pager-off-5ms", null, UiScrollableSwipe.NOMINAL_STEP_MS, pagerOff = true)
        assertEquals(true, lines.any { it.startsWith("first scrollable in the tree: com.nthcase.sample:id/lobby_top_row ") })
        assertEquals(true, lines.any { it.contains("touch log: Tapped: Pocket Notebook (top row, tile 2)") })
        assertEquals(true, lines.any { it.contains("it received: ACTION_DOWN → ACTION_MOVE → ACTION_UP") })
    }

    @Test
    fun views_top_row_with_the_pager_off_at_the_measured_speed() {
        val lines = viewsRun("views-top-row-pager-off-19ms", null, UiScrollableSwipe.MEASURED_STEP_MS, pagerOff = true)
        assertEquals(true, lines.any { it.startsWith("first scrollable in the tree: com.nthcase.sample:id/lobby_top_row ") })
        assertEquals(true, lines.any { it.contains("touch log: Long-pressed: Pocket Notebook (top row, tile 2)") })
        assertEquals(true, lines.any { it.contains("quick sheet open: true") })
    }

    // ── Compose ──────────────────────────────────────────────────────────────────────────────────────────────────────

    private fun composeText(id: String): String =
        compose.onAllNodesWithTag(id).fetchSemanticsNodes().firstOrNull()?.config?.getOrNull(SemanticsProperties.Text)?.joinToString { it.text } ?: "-"

    private fun composeDesc(id: String): String =
        compose.onAllNodesWithTag(id).fetchSemanticsNodes().firstOrNull()?.config?.getOrNull(SemanticsProperties.ContentDescription)?.joinToString() ?: "-"

    private fun composeRun(name: String, targetId: String?, stepMs: Long, pagerOff: Boolean = false): List<String> {
        compose.signIn()
        compose.openLab(TestIds.LAB_LOBBY)
        if (pagerOff) compose.tap(TestIds.LOBBY_PAGER_SHOWN)
        val decor = compose.activity.window.decorView
        val tree = A11yTree.visible(decor)
        val first = tree.first { it.scrollable }
        val target = if (targetId == null) first else tree.first { it.resourceId == targetId }
        val lines = mutableListOf("Lobby (Compose)${if (pagerOff) ", Show the pager off" else ""}, swipe across ${target.resourceId} ${target.bounds}, $stepMs ms a step")
        lines += "first scrollable in the tree: ${first.resourceId} ${first.bounds}"
        lines += platform()
        lines += "Compose reads the same long-press timeout and touch slop from ViewConfiguration (AndroidViewConfiguration)"
        val pageBefore = mutableListOf<String>()
        fun state() = "pager ${composeDesc(TestIds.LOBBY_PAGER_POSITION)}; ${composeDesc(TestIds.LOBBY_TOP_ROW_POSITION)}; ${composeDesc(TestIds.lobbyRowPosition(1))}; " +
            "page shows chips: ${compose.onAllNodesWithTag(TestIds.LOBBY_CHIPS).fetchSemanticsNodes().isNotEmpty()}"
        pageBefore += state()
        lines += "before: ${pageBefore.single()}"

        val swipe = { forward: Boolean, label: String ->
            val points = UiScrollableSwipe.points(target.bounds, forward)
            val sheetOpen = compose.onAllNodesWithTag(TestIds.LOBBY_SHEET).fetchSemanticsNodes().isNotEmpty()
            // the touch goes to the window on top: the sheet's when it is open
            val root = if (sheetOpen) compose.onAllNodes(isRoot()).onLast() else compose.onAllNodes(isRoot()).onFirst()
            val origin = root.fetchSemanticsNode().boundsInWindow.let { Offset(it.left, it.top) }
            fun at(p: Pair<Int, Int>) = Offset(p.first.toFloat(), p.second.toFloat()) - origin
            root.performTouchInput {
                down(at(points.first()))
                points.drop(1).forEach { p -> moveTo(at(p), delayMillis = stepMs) }
                up()
            }
            compose.mainClock.advanceTimeBy(400)
            compose.waitForIdle()
            lines += "$label swipe ${if (forward) "forward (finger up)" else "backward (finger down)"} from ${points.first()} to ${points.last()}" +
                (if (sheetOpen) ", into the quick sheet's window" else "")
            lines += "  touch log: ${composeText(TestIds.LOBBY_TOUCH_LOG).replace("\n", " | ")} (${composeText(TestIds.LOBBY_TOUCH_COUNT)})"
            lines += "  quick sheet open: ${compose.onAllNodesWithTag(TestIds.LOBBY_SHEET).fetchSemanticsNodes().isNotEmpty()}; " +
                "promo shown: ${compose.onAllNodesWithTag(TestIds.LOBBY_PROMO).fetchSemanticsNodes().isNotEmpty()}; " +
                "detail shown: ${compose.onAllNodesWithTag(TestIds.LOBBY_DETAIL).fetchSemanticsNodes().isNotEmpty()}"
            lines += "  after: ${state()}"
        }
        swipe(false, "1.")
        compose.mainClock.advanceTimeBy(263)
        swipe(true, "2.")
        out(name, lines)
        return lines
    }

    @Test
    fun compose_pager_at_the_floor_speed() {
        val lines = composeRun("compose-pager-5ms", null, UiScrollableSwipe.NOMINAL_STEP_MS)
        assertEquals(true, lines.any { it.contains("touch log: Tapped: New in the Kitchen aisle (banner 1)") })
        assertEquals(true, lines.any { it.contains("promo shown: true") })
    }

    @Test
    fun compose_pager_at_the_measured_speed() {
        val lines = composeRun("compose-pager-19ms", null, UiScrollableSwipe.MEASURED_STEP_MS)
        assertEquals(true, lines.any { it.contains("touch log: Long-pressed: New in the Kitchen aisle (banner 1)") })
        assertEquals(true, lines.any { it.contains("quick sheet open: true") })
    }

    @Test
    fun compose_tile_row_at_the_floor_speed() {
        val lines = composeRun("compose-row1-5ms", TestIds.lobbyRow(1), UiScrollableSwipe.NOMINAL_STEP_MS)
        assertEquals(true, lines.filter { it.contains("touch log:") }.all { it.contains(Lobby.NOTHING_TOUCHED) })
    }

    @Test
    fun compose_tile_row_at_the_measured_speed() {
        val lines = composeRun("compose-row1-19ms", TestIds.lobbyRow(1), UiScrollableSwipe.MEASURED_STEP_MS)
        assertEquals(true, lines.filter { it.contains("touch log:") }.all { it.contains(Lobby.NOTHING_TOUCHED) })
    }

    @Test
    fun compose_top_row_with_the_pager_off_at_the_floor_speed() {
        val lines = composeRun("compose-top-row-pager-off-5ms", null, UiScrollableSwipe.NOMINAL_STEP_MS, pagerOff = true)
        assertEquals(true, lines.any { it.startsWith("first scrollable in the tree: lobby_top_row ") })
        assertEquals(true, lines.any { it.contains("touch log: Tapped: Pocket Notebook (top row, tile 2)") })
        assertEquals(true, lines.any { it.contains("detail shown: true") })
    }

    @Test
    fun compose_top_row_with_the_pager_off_at_the_measured_speed() {
        val lines = composeRun("compose-top-row-pager-off-19ms", null, UiScrollableSwipe.MEASURED_STEP_MS, pagerOff = true)
        assertEquals(true, lines.any { it.startsWith("first scrollable in the tree: lobby_top_row ") })
        assertEquals(true, lines.any { it.contains("touch log: Long-pressed: Pocket Notebook (top row, tile 2)") })
        assertEquals(true, lines.any { it.contains("quick sheet open: true") })
    }
}

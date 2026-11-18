package com.nthcase.sample

import android.view.View
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.click
import androidx.compose.ui.test.doubleClick
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.RecyclerView
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.viewpager2.widget.ViewPager2
import com.nthcase.sample.data.AppState
import com.nthcase.sample.data.Lobby
import com.nthcase.sample.lobby.BoardView
import com.nthcase.sample.lobby.LobbyViewsFragment
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.shadows.ShadowDialog
import java.io.File

/** What the tree Appium reads holds, written to app/build/lobby/ so the report can quote it. */
internal fun record(name: String, lines: List<Any>) {
    val dir = File("build/lobby").apply { mkdirs() }
    File(dir, "$name.txt").writeText(lines.joinToString("\n") + "\n")
}

/** The ids every visit of the Lobby shows before anything is scrolled, in either build. */
internal val lobbyIdsAtOpen = listOf(
    TestIds.LOBBY_SCREEN, TestIds.LOBBY_TOUCH_LOG, TestIds.LOBBY_TOUCH_COUNT, TestIds.LOBBY_TOUCH_RESET, TestIds.LOBBY_SAVED_DELAY,
    TestIds.LOBBY_PAGER_SHOWN, TestIds.LOBBY_PAGER, TestIds.lobbyBanner(1), TestIds.LOBBY_PAGER_POSITION, TestIds.LOBBY_PAGER_AUTO,
    TestIds.LOBBY_TOP_ROW_POSITION, TestIds.LOBBY_TOP_ROW, TestIds.lobbyTopRowTile(1), TestIds.lobbyTopRowTile(2), TestIds.LOBBY_PAGE,
    TestIds.LOBBY_CHIPS, TestIds.lobbyChip(1), TestIds.lobbyRowPosition(1), TestIds.lobbyRow(1), TestIds.lobbyRowTile(1, 1),
    TestIds.LOBBY_BAR, TestIds.LOBBY_BAR_HOME, TestIds.LOBBY_BAR_LIVE, TestIds.LOBBY_BAR_SAVED, TestIds.LOBBY_BAR_OFFERS,
) + Lobby.DELAYS.map(TestIds::lobbySavedDelay)

/** The pager's own ids: with "Show the pager" off, none of them may be in the tree. */
internal val pagerIds = listOf(TestIds.LOBBY_PAGER, TestIds.LOBBY_PAGER_POSITION, TestIds.LOBBY_PAGER_AUTO) + (1..Lobby.BANNERS).map(TestIds::lobbyBanner)

/** The scrollables that carry an id, in tree order, without the app's package prefix. */
internal fun namedScrollables(nodes: List<A11yNode>): List<String> =
    nodes.filter { it.scrollable }.mapNotNull { it.resourceId?.substringAfter(":id/") }

/**
 * The Lobby built with Compose (SPEC.md §5, 1.1): its ids in the tree UiAutomator2 reads, the pager first among the
 * scrollables, Saved out of the tree until its delay has passed, and a touch log that names every touch and nothing else.
 */
@RunWith(AndroidJUnit4::class)
class LobbyComposeTest {
    private val compose = appCompose()

    @get:Rule
    val rules = coldStart(compose)

    private fun open() {
        compose.signIn()
        compose.openLab(TestIds.LAB_LOBBY)
    }

    private fun tree() = A11yTree.visible(compose.activity.window.decorView)
    private fun log() = compose.onNodeWithTag(TestIds.LOBBY_TOUCH_LOG).fetchSemanticsNode().config[SemanticsProperties.Text].joinToString { it.text }
    private fun count() = compose.onNodeWithTag(TestIds.LOBBY_TOUCH_COUNT).fetchSemanticsNode().config[SemanticsProperties.Text].joinToString { it.text }

    @Test
    fun the_pager_is_the_first_scrollable_and_every_id_is_a_resource_id() {
        open()
        val nodes = tree()
        record("compose-tree-at-open", nodes)
        val scrollables = nodes.filter { it.scrollable }
        record("compose-scrollables-at-open", scrollables)
        assertEquals("UiScrollable's first scrollable", TestIds.LOBBY_PAGER, scrollables.first().resourceId)
        assertEquals("the Top 10 row comes after the pager, before the page", listOf(TestIds.LOBBY_PAGER, TestIds.LOBBY_TOP_ROW, TestIds.LOBBY_PAGE), namedScrollables(nodes).take(3))
        val ids = nodes.mapNotNull { it.resourceId }.toSet()
        lobbyIdsAtOpen.forEach { assertTrue("$it is a resource-id in the tree", it in ids) }
        assertEquals("Top 10: tiles 1–3 of 10", nodes.single { it.resourceId == TestIds.LOBBY_TOP_ROW_POSITION }.desc)
    }

    @Test
    fun with_the_pager_off_the_top_row_is_the_first_scrollable_and_the_pager_is_not_in_the_tree() {
        open()
        compose.tap(TestIds.LOBBY_PAGER_SHOWN)
        val nodes = tree()
        record("compose-tree-pager-off", nodes)
        val scrollables = nodes.filter { it.scrollable }
        record("compose-scrollables-pager-off", scrollables)
        assertEquals("UiScrollable's first scrollable", TestIds.LOBBY_TOP_ROW, scrollables.first().resourceId)
        val ids = nodes.mapNotNull { it.resourceId }.toSet()
        pagerIds.forEach { id ->
            assertFalse("$id is not in the tree", id in ids)
            assertTrue("$id has no semantics node at all", compose.onAllNodesWithTag(id, useUnmergedTree = true).fetchSemanticsNodes().isEmpty())
        }
        (lobbyIdsAtOpen - pagerIds.toSet()).forEach { assertTrue("$it is still a resource-id in the tree", it in ids) }
        assertFalse(AppState.lobbyPagerShown)
        assertEquals("turning the switch is not a touch", Lobby.NOTHING_TOUCHED, log())

        // kept until the app closes, like the delay: leaving the Lobby and coming back finds the pager still off
        compose.tap(TestIds.TOP_BAR_BACK)
        compose.tap(TestIds.LAB_LOBBY)
        assertEquals(TestIds.LOBBY_TOP_ROW, tree().first { it.scrollable }.resourceId)

        compose.tap(TestIds.LOBBY_PAGER_SHOWN)
        val again = tree()
        record("compose-scrollables-pager-on-again", again.filter { it.scrollable })
        assertEquals(listOf(TestIds.LOBBY_PAGER, TestIds.LOBBY_TOP_ROW, TestIds.LOBBY_PAGE), namedScrollables(again).take(3))
    }

    @Test
    fun a_top_row_tile_takes_a_tap_and_a_long_press() {
        open()
        compose.onNodeWithTag(TestIds.lobbyTopRowTile(2)).performTouchInput { click() }
        compose.mainClock.advanceTimeBy(500)
        assertEquals("Tapped: Pocket Notebook (top row, tile 2)", log())
        val detail = compose.onNodeWithTag(TestIds.LOBBY_DETAIL).fetchSemanticsNode().config[SemanticsProperties.Text].map { it.text }
        assertTrue(detail.toString(), "Top 10, tile 2" in detail && "Pocket Notebook" in detail)
        compose.tap(TestIds.LOBBY_DETAIL_BACK)

        compose.onNodeWithTag(TestIds.lobbyTopRowTile(2)).performTouchInput { longClick() }
        compose.waitForIdle()
        assertTrue(log().startsWith("Long-pressed: Pocket Notebook (top row, tile 2)"))
        val title = compose.onNodeWithTag(TestIds.LOBBY_SHEET_TITLE).fetchSemanticsNode().config[SemanticsProperties.Text].joinToString { it.text }
        assertEquals("Pocket Notebook", title)
        compose.tap(TestIds.LOBBY_SHEET_OPEN)
        assertTrue(log().startsWith("Tapped: Open (quick sheet, Pocket Notebook)"))
        compose.onNodeWithTag(TestIds.LOBBY_DETAIL).assertExists()
        assertEquals("Touches: 3", count())
    }

    @Test
    fun saved_is_out_of_the_tree_until_its_delay_has_passed() {
        open()
        compose.tap(TestIds.lobbySavedDelay(12))
        compose.mainClock.autoAdvance = false
        fun inTree() = tree().any { it.resourceId == TestIds.LOBBY_BAR_SAVED }
        assertFalse("no Saved right after the choice", inTree())
        assertTrue(compose.onAllNodesWithTag(TestIds.LOBBY_BAR_SAVED, useUnmergedTree = true).fetchSemanticsNodes().isEmpty())
        assertEquals(3, tree().count { it.resourceId?.startsWith("lobby_bar_") == true })
        compose.mainClock.advanceTimeBy(11_800)
        assertFalse("no Saved at 11.8 s", inTree())
        compose.mainClock.advanceTimeBy(400)
        assertTrue("Saved at 12.2 s", inTree())
        compose.mainClock.autoAdvance = true
        assertEquals(Lobby.NOTHING_TOUCHED, log())
    }

    @Test
    fun a_tap_a_long_press_and_a_double_tap_on_a_tile_are_logged() {
        open()
        assertEquals(Lobby.NOTHING_TOUCHED, log())
        compose.onNodeWithTag(TestIds.lobbyRowTile(1, 2)).performTouchInput { click() }
        compose.mainClock.advanceTimeBy(500)
        assertEquals("Tapped: Grid Notebook (row 1, tile 2)", log())
        compose.onNodeWithTag(TestIds.LOBBY_DETAIL).assertExists()
        compose.tap(TestIds.LOBBY_DETAIL_BACK)

        compose.onNodeWithTag(TestIds.lobbyRowTile(1, 2)).performTouchInput { longClick() }
        compose.waitForIdle()
        assertTrue(log().startsWith("Long-pressed: Grid Notebook (row 1, tile 2)"))
        compose.onNodeWithTag(TestIds.LOBBY_SHEET).assertExists()
        val sheet = A11yTree.visible(ShadowDialog.getLatestDialog().window!!.decorView)
        record("compose-sheet-tree", sheet)
        TestIds.lobbySheetButtons.plus(TestIds.LOBBY_SHEET_TITLE).forEach { id -> assertTrue("$id in the sheet's window", sheet.any { it.resourceId == id }) }
        compose.tap(TestIds.LOBBY_SHEET_RATE)
        assertTrue(log().startsWith("Tapped: Rate (quick sheet, Grid Notebook)"))

        compose.onNodeWithTag(TestIds.lobbyRowTile(1, 2)).performTouchInput { doubleClick() }
        compose.mainClock.advanceTimeBy(500)
        assertTrue(log().startsWith("Double-tapped: Grid Notebook (row 1, tile 2)"))
        // the tap, the long press, the sheet's Rate and the double tap
        assertEquals("Touches: 4", count())
        compose.tap(TestIds.LOBBY_TOUCH_RESET)
        assertEquals(Lobby.NOTHING_TOUCHED, log())
        assertEquals("Touches: 0", count())
    }

    @Test
    fun scrolling_every_scroller_by_program_logs_nothing() {
        open()
        compose.onNodeWithTag(TestIds.LOBBY_PAGER).performScrollToIndex(3)
        compose.onNodeWithTag(TestIds.LOBBY_PAGE).performScrollToNode(hasTestTag(TestIds.lobbyRow(3)))
        compose.onNodeWithTag(TestIds.lobbyRow(3)).performScrollToIndex(14)
        compose.onNodeWithTag(TestIds.LOBBY_PAGE).performScrollToIndex(0)
        compose.onNodeWithTag(TestIds.LOBBY_CHIPS).performScrollToIndex(11)
        compose.onNodeWithTag(TestIds.LOBBY_PAGE).performScrollToNode(hasTestTag(TestIds.LOBBY_INNER_LIST))
        compose.onNodeWithTag(TestIds.LOBBY_INNER_LIST).performScrollToIndex(29)
        compose.onNodeWithTag(TestIds.LOBBY_PAGE).performScrollToNode(hasTestTag(TestIds.LOBBY_BOARD))
        val cell = with(compose.density) { 56.dp.toPx() }
        compose.onNodeWithTag(TestIds.LOBBY_BOARD).performSemanticsAction(SemanticsActions.ScrollBy) { it(cell * 12, cell * 17) }
        compose.onNodeWithTag(TestIds.LOBBY_PAGE).performScrollToNode(hasTestTag(TestIds.LOBBY_FOOTER))
        compose.waitForIdle()
        val positions = listOf(
            compose.onNodeWithTag(TestIds.LOBBY_PAGER_POSITION).fetchSemanticsNode().config.getOrNull(SemanticsProperties.ContentDescription),
            compose.onNodeWithTag(TestIds.LOBBY_BOARD_POSITION).fetchSemanticsNode().config.getOrNull(SemanticsProperties.ContentDescription),
        )
        record("compose-positions-after-scrolls", positions.map { it.toString() })
        assertEquals(listOf(listOf("Banner 4 of 5"), listOf("Board: top-left M18")), positions)
        assertEquals(Lobby.NOTHING_TOUCHED, log())
        assertEquals("Touches: 0", count())
    }

}

/**
 * The Lobby built with Android Views (Lobby (Views), SPEC.md §5 and §12): the same checks, with touches injected as
 * MotionEvents through the window, and the ids read as view ids.
 */
@RunWith(AndroidJUnit4::class)
class LobbyViewsTest {
    private val compose = appCompose()

    @get:Rule
    val rules = coldStart(compose)

    private val decor: View get() = compose.activity.window.decorView
    private val fragment: LobbyViewsFragment
        get() = compose.activity.supportFragmentManager.fragments.filterIsInstance<LobbyViewsFragment>().single()

    private fun open() {
        compose.signIn()
        compose.openLab(TestIds.LAB_LOBBY_VIEWS)
        Finger.idle(50)
    }

    private fun <T : View> view(id: String): T = decor.findViewById(fragment.idOf(id))
    private fun viewOrNull(id: String): View? = decor.findViewById(fragment.idOf(id))
    private fun centre(v: View): Pair<Int, Int> = IntArray(2).also(v::getLocationOnScreen).let { (it[0] + v.width / 2) to (it[1] + v.height / 2) }
    private fun log() = fragment.log.text
    private fun tree() = A11yTree.visible(decor)

    @Test
    fun the_pager_is_the_first_scrollable_and_every_id_is_a_view_id() {
        open()
        val nodes = tree()
        record("views-tree-at-open", nodes)
        val scrollables = nodes.filter { it.scrollable }
        record("views-scrollables-at-open", scrollables)
        assertEquals("UiScrollable's first scrollable", "com.nthcase.sample:id/${TestIds.LOBBY_PAGER}", scrollables.first().resourceId)
        // between them, ViewPager2's own RecyclerView, which has no id
        assertEquals("the Top 10 row comes after the pager, before the page", listOf(TestIds.LOBBY_PAGER, TestIds.LOBBY_TOP_ROW, TestIds.LOBBY_PAGE), namedScrollables(nodes).take(3))
        val ids = nodes.mapNotNull { it.resourceId }.toSet()
        lobbyIdsAtOpen.forEach { assertTrue("$it is a resource-id in the tree", "com.nthcase.sample:id/$it" in ids) }
        assertEquals("Top 10: tiles 1–3 of 10", nodes.single { it.resourceId == "com.nthcase.sample:id/${TestIds.LOBBY_TOP_ROW_POSITION}" }.desc)
    }

    @Test
    fun with_the_pager_off_the_top_row_is_the_first_scrollable_and_the_pager_is_not_in_the_tree() {
        open()
        view<View>(TestIds.LOBBY_PAGER_SHOWN).performClick()
        Finger.idle(50)
        val nodes = tree()
        record("views-tree-pager-off", nodes)
        val scrollables = nodes.filter { it.scrollable }
        record("views-scrollables-pager-off", scrollables)
        assertEquals("UiScrollable's first scrollable", "com.nthcase.sample:id/${TestIds.LOBBY_TOP_ROW}", scrollables.first().resourceId)
        val ids = nodes.mapNotNull { it.resourceId }.toSet()
        pagerIds.forEach { id ->
            assertFalse("$id is not in the tree", "com.nthcase.sample:id/$id" in ids)
            assertNull("$id is not in the window's views at all (removed, not GONE)", viewOrNull(id))
        }
        // and not in the unfiltered tree either, where a GONE or off-screen view would still be listed
        assertTrue(A11yTree.of(decor).none { it.resourceId?.substringAfter(":id/") in pagerIds })
        (lobbyIdsAtOpen - pagerIds.toSet()).forEach { assertTrue("$it is still a resource-id in the tree", "com.nthcase.sample:id/$it" in ids) }
        assertEquals("turning the switch is not a touch", Lobby.NOTHING_TOUCHED, log())

        // kept until the app closes: a new visit builds the Lobby without the pager
        compose.tap(TestIds.TOP_BAR_BACK)
        compose.tap(TestIds.LAB_LOBBY_VIEWS)
        Finger.idle(50)
        assertEquals("com.nthcase.sample:id/${TestIds.LOBBY_TOP_ROW}", tree().first { it.scrollable }.resourceId)

        view<View>(TestIds.LOBBY_PAGER_SHOWN).performClick()
        Finger.idle(50)
        val again = tree()
        record("views-scrollables-pager-on-again", again.filter { it.scrollable })
        assertEquals(listOf(TestIds.LOBBY_PAGER, TestIds.LOBBY_TOP_ROW, TestIds.LOBBY_PAGE), namedScrollables(again).take(3))
    }

    @Test
    fun a_top_row_tile_takes_a_tap_and_a_long_press() {
        open()
        val finger = Finger(decor)
        val (x, y) = centre(view(TestIds.lobbyTopRowTile(2)))
        finger.tap(x, y)
        Finger.idle(400)
        assertEquals("Tapped: Pocket Notebook (top row, tile 2)", log())
        val detail = view<View>(TestIds.LOBBY_DETAIL)
        assertEquals(View.VISIBLE, detail.visibility)
        assertEquals("Pocket Notebook, No. 032, Top 10, tile 2", detail.contentDescription.toString())
        view<View>(TestIds.LOBBY_DETAIL_BACK).performClick()

        finger.longPress(x, y)
        assertTrue(log().startsWith("Long-pressed: Pocket Notebook (top row, tile 2)"))
        val sheet = fragment.openSheet
        assertNotNull("the quick sheet is open", sheet)
        assertEquals("Pocket Notebook", sheet!!.findViewById<android.widget.TextView>(fragment.idOf(TestIds.LOBBY_SHEET_TITLE))!!.text.toString())
        sheet.findViewById<View>(fragment.idOf(TestIds.LOBBY_SHEET_OPEN))!!.performClick()
        Finger.idle(300)
        assertTrue(log().startsWith("Tapped: Open (quick sheet, Pocket Notebook)"))
        assertEquals(View.VISIBLE, view<View>(TestIds.LOBBY_DETAIL).visibility)
        assertEquals("Touches: 3", fragment.log.countText)
    }

    @Test
    fun saved_is_out_of_the_tree_until_its_delay_has_passed() {
        open()
        view<View>(TestIds.lobbySavedDelay(12)).performClick()
        fun inTree() = tree().any { it.resourceId == "com.nthcase.sample:id/${TestIds.LOBBY_BAR_SAVED}" }
        assertFalse("no Saved right after the choice", inTree())
        assertNull("no Saved view at all", viewOrNull(TestIds.LOBBY_BAR_SAVED))
        Finger.idle(11_800)
        assertFalse("no Saved at 11.8 s", inTree())
        Finger.idle(400)
        assertTrue("Saved at 12.2 s", inTree())
        assertEquals(Lobby.NOTHING_TOUCHED, log())
    }

    @Test
    fun a_tap_a_long_press_and_a_double_tap_on_a_tile_are_logged() {
        open()
        val finger = Finger(decor)
        val (x, y) = centre(view(TestIds.lobbyRowTile(1, 2)))
        finger.tap(x, y)
        Finger.idle(400)
        assertEquals("Tapped: Grid Notebook (row 1, tile 2)", log())
        assertEquals(View.VISIBLE, view<View>(TestIds.LOBBY_DETAIL).visibility)
        view<View>(TestIds.LOBBY_DETAIL_BACK).performClick()

        finger.longPress(x, y)
        assertTrue(log().startsWith("Long-pressed: Grid Notebook (row 1, tile 2)"))
        val sheet = fragment.openSheet
        assertNotNull("the quick sheet is open", sheet)
        Finger.idle(500)
        record("views-sheet-tree-all", A11yTree.of(sheet!!.window!!.decorView))
        val sheetTree = A11yTree.visible(sheet.window!!.decorView)
        record("views-sheet-tree", sheetTree)
        TestIds.lobbySheetButtons.plus(TestIds.LOBBY_SHEET_TITLE).forEach { id ->
            assertTrue("$id in the sheet's window", sheetTree.any { it.resourceId == "com.nthcase.sample:id/$id" })
        }
        sheet.findViewById<View>(fragment.idOf(TestIds.LOBBY_SHEET_RATE))!!.performClick()
        Finger.idle(300)
        assertTrue(log().startsWith("Tapped: Rate (quick sheet, Grid Notebook)"))

        finger.doubleTap(x, y)
        Finger.idle(400)
        assertTrue(log().startsWith("Double-tapped: Grid Notebook (row 1, tile 2)"))
        assertEquals("Touches: 4", fragment.log.countText)
    }

    @Test
    fun scrolling_every_scroller_by_program_logs_nothing() {
        open()
        view<ViewPager2>(TestIds.LOBBY_PAGER).setCurrentItem(3, false)
        view<RecyclerView>(TestIds.LOBBY_CHIPS).scrollToPosition(11)
        (1..Lobby.ROWS).forEach { view<RecyclerView>(TestIds.lobbyRow(it)).scrollToPosition(19) }
        view<RecyclerView>(TestIds.LOBBY_INNER_LIST).scrollToPosition(29)
        val cell = (56 * decor.resources.displayMetrics.density).toInt()
        view<BoardView>(TestIds.LOBBY_BOARD).scrollTo(cell * 12, cell * 17)
        val page = view<NestedScrollView>(TestIds.LOBBY_PAGE)
        page.scrollTo(0, page.getChildAt(0).height)
        Finger.idle(100)
        val positions = listOf(view<View>(TestIds.LOBBY_PAGER_POSITION).contentDescription, view<View>(TestIds.LOBBY_BOARD_POSITION).contentDescription)
        record("views-positions-after-scrolls", positions.map { it.toString() })
        assertEquals(listOf("Banner 4 of 5", "Board: top-left M18"), positions.map { it.toString() })
        assertEquals(Lobby.NOTHING_TOUCHED, log())
        assertEquals("Touches: 0", fragment.log.countText)
        assertTrue("the footer is on screen", tree().any { it.resourceId == "com.nthcase.sample:id/${TestIds.LOBBY_FOOTER}" })
    }

    @Test
    fun the_saved_delay_is_kept_and_the_bar_logs_its_taps() {
        AppState.lobbySavedDelay = 3
        open()
        assertNull(viewOrNull(TestIds.LOBBY_BAR_SAVED))
        Finger.idle(3_100)
        val saved = view<View>(TestIds.LOBBY_BAR_SAVED)
        val (x, y) = centre(saved)
        Finger(decor).tap(x, y)
        Finger.idle(100)
        assertEquals("Tapped: Saved (bar)", log())
        assertEquals(View.VISIBLE, view<View>(TestIds.LOBBY_SAVED_SCREEN).visibility)
    }
}

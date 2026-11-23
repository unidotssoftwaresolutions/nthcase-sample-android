package com.nthcase.sample

import android.os.SystemClock
import android.view.InputDevice
import android.view.MotionEvent
import android.view.View
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeUp
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.nthcase.sample.data.AppState
import com.nthcase.sample.data.Feed
import com.nthcase.sample.data.Lobby
import com.nthcase.sample.lobby.LobbyViewsFragment
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import org.junit.runner.RunWith

/**
 * Spec 1.2 on an emulator or a phone (`./gradlew connectedDebugAndroidTest`): the quick sheet's More like this in both
 * Lobbies, scrolled by the platform's own touch input inside the sheet's window, and the Feed. The JVM tests under
 * src/test prove the same rules with Robolectric; these prove them on a real window manager and a real input pipeline.
 */
@RunWith(AndroidJUnit4::class)
class SheetAndFeedTest {
    private val compose = createAndroidComposeRule<MainActivity>()

    /** A cold start, as every test of the app has: the app's state reset before the activity starts. */
    @get:Rule
    val rules: RuleChain = RuleChain.outerRule(object : TestWatcher() {
        override fun starting(description: Description) = AppState.reset()
        override fun finished(description: Description) = AppState.reset()
    }).around(compose)

    private fun tap(id: String) {
        compose.onNodeWithTag(id).performScrollToIfPossible().performClick()
        compose.waitForIdle()
    }

    private fun androidx.compose.ui.test.SemanticsNodeInteraction.performScrollToIfPossible() =
        try { performScrollTo() } catch (_: AssertionError) { this }

    private fun openLab(bench: String) {
        tap(TestIds.SIGNIN_USE_TEST_ACCOUNT)
        tap(TestIds.SIGNIN_SUBMIT)
        tap(TestIds.NAV_LAB)
        tap(bench)
    }

    private fun text(id: String) = compose.onNodeWithTag(id).fetchSemanticsNode().config[SemanticsProperties.Text].joinToString { it.text }

    @Test
    fun more_like_this_scrolls_inside_the_compose_sheet() {
        openLab(TestIds.LAB_LOBBY)
        compose.onNodeWithTag(TestIds.lobbyTopRowTile(2)).performTouchInput { longClick() }
        compose.waitForIdle()
        compose.onNodeWithTag(TestIds.LOBBY_SHEET_MORE).assertIsDisplayed()
        assertEquals("Like Pocket Notebook · 1 of 20", text(TestIds.lobbySheetMoreRow(1)))
        assertTrue("line 20 past the sheet's foot at open", compose.onAllNodesWithTag(TestIds.lobbySheetMoreRow(20)).fetchSemanticsNodes().isEmpty())
        val sheet = compose.onNodeWithTag(TestIds.LOBBY_SHEET).fetchSemanticsNode().boundsInWindow

        var swipes = 0
        while (compose.onAllNodesWithTag(TestIds.lobbySheetMoreRow(20)).fetchSemanticsNodes().isEmpty() && swipes < 25) {
            compose.onNodeWithTag(TestIds.LOBBY_SHEET_MORE).performTouchInput { swipeUp() }
            compose.waitForIdle()
            swipes++
        }
        compose.onNodeWithTag(TestIds.lobbySheetMoreRow(20)).assertIsDisplayed()
        assertEquals("the sheet did not move", sheet, compose.onNodeWithTag(TestIds.LOBBY_SHEET).fetchSemanticsNode().boundsInWindow)
        assertEquals("the swipes touched nothing", "Long-pressed: Pocket Notebook (top row, tile 2)", text(TestIds.LOBBY_TOUCH_LOG))

        compose.onNodeWithTag(TestIds.lobbySheetMoreRow(20)).performClick()
        compose.waitForIdle()
        assertTrue(text(TestIds.LOBBY_TOUCH_LOG).startsWith("Tapped: Like Pocket Notebook · 20 of 20 (quick sheet, Pocket Notebook)"))
        assertEquals("Touches: 2", text(TestIds.LOBBY_TOUCH_COUNT))
        assertTrue("the sheet closed", compose.onAllNodesWithTag(TestIds.LOBBY_SHEET).fetchSemanticsNodes().isEmpty())
    }

    @Test
    fun more_like_this_scrolls_inside_the_views_sheet() {
        openLab(TestIds.LAB_LOBBY_VIEWS)
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        instrumentation.waitForIdleSync()
        var fragment: LobbyViewsFragment? = null
        compose.runOnUiThread { fragment = compose.activity.supportFragmentManager.fragments.filterIsInstance<LobbyViewsFragment>().single() }
        val lobby = fragment!!
        val tile = centreOnScreen { compose.activity.findViewById<View>(lobby.idOf(TestIds.lobbyTopRowTile(2))) }
        press(tile.first, tile.second, holdMs = 900)
        instrumentation.waitForIdleSync()
        SystemClock.sleep(600)
        val sheet = lobby.openSheet
        assertNotNull("the quick sheet is open", sheet)
        val more = sheet!!.findViewById<RecyclerView>(lobby.idOf(TestIds.LOBBY_SHEET_MORE))!!
        val content = sheet.findViewById<View>(lobby.idOf(TestIds.LOBBY_SHEET))!!
        fun lastWhole(): Int {
            var last = -1
            compose.runOnUiThread { last = (more.layoutManager as LinearLayoutManager).findLastCompletelyVisibleItemPosition() }
            return last
        }
        assertTrue("line 20 past the sheet's foot at open", lastWhole() < Lobby.SHEET_MORE_LINES - 1)
        val sheetAt = onScreen(content)

        var swipes = 0
        while (lastWhole() < Lobby.SHEET_MORE_LINES - 1 && swipes < 25) {
            val box = boxOnScreen(more)
            swipe(box.centerX(), box.top + box.height() * 4 / 5, box.top + box.height() / 5)
            instrumentation.waitForIdleSync()
            SystemClock.sleep(400)
            swipes++
        }
        assertEquals("line 20 whole after $swipes swipes", Lobby.SHEET_MORE_LINES - 1, lastWhole())
        assertEquals("the sheet did not move", sheetAt, onScreen(content))
        assertEquals("the swipes touched nothing", "Long-pressed: Pocket Notebook (top row, tile 2)", lobby.log.text)

        val line = centreOnScreen { sheet.findViewById<View>(lobby.idOf(TestIds.lobbySheetMoreRow(20)))!! }
        press(line.first, line.second, holdMs = 60)
        instrumentation.waitForIdleSync()
        SystemClock.sleep(600)
        assertTrue(lobby.log.text.startsWith("Tapped: Like Pocket Notebook · 20 of 20 (quick sheet, Pocket Notebook)"))
        assertEquals("Touches: 2", lobby.log.countText)
        assertNull("the sheet closed", lobby.openSheet)
    }

    @Test
    fun the_feed_brings_twenty_rows_at_a_time_after_a_spinner_row() {
        openLab(TestIds.LAB_FEED)
        assertEquals("Feed row 1", text(TestIds.feedRow(1)))
        assertTrue(compose.onAllNodesWithTag(TestIds.FEED_LOADING).fetchSemanticsNodes().isEmpty())
        // the clock held: the list's end, the spinner row for its second, then the next rows
        compose.mainClock.autoAdvance = false
        val list = compose.onNodeWithTag(TestIds.FEED_LIST)
        list.performScrollToIndex(19)
        compose.mainClock.advanceTimeBy(32)
        list.performScrollToIndex(20)
        compose.mainClock.advanceTimeBy(32)
        compose.onNodeWithTag(TestIds.FEED_LOADING).assertIsDisplayed()
        assertEquals(Feed.LOADING, text(TestIds.FEED_LOADING))
        compose.mainClock.advanceTimeBy(Feed.LOAD_MS + 100)
        list.performScrollToIndex(20)
        compose.mainClock.advanceTimeBy(32)
        compose.onNodeWithTag(TestIds.feedRow(21)).assertIsDisplayed()
        compose.mainClock.autoAdvance = true

        // then a finger's swipes, through two more loads, past row 60: the Feed never ends. A swipe flings on over many
        // rows, so this reads the last row on screen after each one. A row the list composes ahead of its view
        // (LazyColumn's prefetch) is in the tree before it is on screen, so on screen means inside the list's bounds.
        val rowTag = Regex("^feed_row_(\\d+)$")
        fun lastRowShown(): Int {
            val view = list.fetchSemanticsNode().boundsInRoot
            return compose.onAllNodes(SemanticsMatcher("a Feed row") { rowTag.matches(it.config.getOrNull(SemanticsProperties.TestTag) ?: "") })
                .fetchSemanticsNodes()
                .filter { it.boundsInRoot.top < view.bottom && it.boundsInRoot.bottom > view.top }
                .maxOfOrNull { rowTag.find(it.config[SemanticsProperties.TestTag])!!.groupValues[1].toInt() } ?: 0
        }
        var swipes = 0
        while (lastRowShown() <= 60 && swipes < 40) {
            list.performTouchInput { swipeUp() }
            compose.waitForIdle()
            swipes++
        }
        val last = lastRowShown()
        assertTrue("a row past 60 on screen after $swipes swipes (the last: $last)", last > 60)
        assertTrue("nothing says the Feed ends", compose.onAllNodesWithText("End of the feed", substring = true).fetchSemanticsNodes().isEmpty())
    }

    // ── the platform's own touch input, for the Views sheet ─────────────────────────────────────────────────────────

    private fun boxOnScreen(view: View): android.graphics.Rect {
        val box = android.graphics.Rect()
        compose.runOnUiThread { view.getGlobalVisibleRect(box) }
        val window = IntArray(2)
        compose.runOnUiThread { view.rootView.getLocationOnScreen(window) }
        box.offset(window[0], window[1])
        return box
    }

    private fun onScreen(view: View): List<Int> {
        val at = IntArray(2)
        compose.runOnUiThread { view.getLocationOnScreen(at) }
        return at.toList()
    }

    private fun centreOnScreen(find: () -> View): Pair<Int, Int> {
        var centre = 0 to 0
        compose.runOnUiThread {
            val view = find()
            val at = IntArray(2).also(view::getLocationOnScreen)
            centre = (at[0] + view.width / 2) to (at[1] + view.height / 2)
        }
        return centre
    }

    private fun event(down: Long, action: Int, x: Int, y: Int): MotionEvent =
        MotionEvent.obtain(down, SystemClock.uptimeMillis(), action, x.toFloat(), y.toFloat(), 0).apply { source = InputDevice.SOURCE_TOUCHSCREEN }

    /** A finger down, held, and lifted where it went down. */
    private fun press(x: Int, y: Int, holdMs: Long) {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val down = SystemClock.uptimeMillis()
        instrumentation.sendPointerSync(event(down, MotionEvent.ACTION_DOWN, x, y))
        SystemClock.sleep(holdMs)
        instrumentation.sendPointerSync(event(down, MotionEvent.ACTION_UP, x, y))
    }

    /** A finger drawn from [fromY] to [toY] over about 250 ms, then lifted. */
    private fun swipe(x: Int, fromY: Int, toY: Int) {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val down = SystemClock.uptimeMillis()
        instrumentation.sendPointerSync(event(down, MotionEvent.ACTION_DOWN, x, fromY))
        for (i in 1..10) {
            SystemClock.sleep(25)
            instrumentation.sendPointerSync(event(down, MotionEvent.ACTION_MOVE, x, fromY + (toY - fromY) * i / 10))
        }
        instrumentation.sendPointerSync(event(down, MotionEvent.ACTION_UP, x, toY))
    }
}

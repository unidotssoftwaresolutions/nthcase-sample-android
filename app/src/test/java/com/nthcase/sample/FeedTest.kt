package com.nthcase.sample

import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.performSemanticsAction
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nthcase.sample.data.Feed
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * The Feed (SPEC.md §5, spec 1.2) on the JVM, with the clock held: 20 rows; a load starts when the third row from the end
 * of what has come shows, not a pixel before; its spinner row lasts 1 s; then 20 more rows; and so on, with no end.
 */
@RunWith(AndroidJUnit4::class)
class FeedTest {
    private val compose = appCompose()

    @get:Rule
    val rules = coldStart(compose)

    private fun open() {
        compose.signIn()
        compose.openLab(TestIds.LAB_FEED)
    }

    private val list get() = compose.onNodeWithTag(TestIds.FEED_LIST)

    private fun there(id: String) = compose.onAllNodesWithTag(id).fetchSemanticsNodes().isNotEmpty()

    /** The last row any part of which lies inside the list's bounds: what "shows" means in the spec. */
    private fun lastShown(upTo: Int): Int {
        val view = list.fetchSemanticsNode().boundsInRoot
        return (1..upTo).lastOrNull { n ->
            compose.onAllNodesWithTag(TestIds.feedRow(n)).fetchSemanticsNodes().any { it.boundsInRoot.top < view.bottom && it.boundsInRoot.bottom > view.top }
        } ?: 0
    }

    private fun scrollBy(pixels: Float) = list.performSemanticsAction(SemanticsActions.ScrollBy) { it(0f, pixels) }

    @Test
    fun a_load_starts_when_the_third_row_from_the_end_shows_and_not_before() {
        open()
        assertEquals("Feed row 1", compose.onNodeWithTag(TestIds.feedRow(1)).fetchSemanticsNode().config[SemanticsProperties.Text].joinToString { it.text })
        assertTrue("the list scrolls", A11yTree.visible(compose.activity.window.decorView).single { it.resourceId == TestIds.FEED_LIST }.scrollable)
        assertFalse("no spinner row at open", there(TestIds.FEED_LOADING))
        val view = list.fetchSemanticsNode().boundsInRoot.height
        val row = compose.onNodeWithTag(TestIds.feedRow(1)).fetchSemanticsNode().boundsInRoot.height

        compose.mainClock.autoAdvance = false
        // row 18's top one pixel below the list's end: rows up to 17 show, and nothing loads, however long it waits
        scrollBy(17 * row - view - 1f)
        compose.mainClock.advanceTimeBy(1_500)
        assertEquals(17, lastShown(20))

        // two pixels on: row 18 shows, so the load starts, and its spinner row comes after row 20. Seen at the list's
        // end, it also says that nothing had loaded while row 17 was the last to show: then row 21 would be there.
        scrollBy(2f)
        compose.mainClock.advanceTimeBy(200)
        assertEquals(18, lastShown(20))
        list.performScrollToIndex(20)
        compose.mainClock.advanceTimeBy(32)
        assertTrue("the spinner row after row 20", there(TestIds.FEED_LOADING))
        assertFalse(there(TestIds.feedRow(21)))
        // in the tree Appium reads, a node with an id and the words under it, as a Lobby tile is
        assertTrue(A11yTree.visible(compose.activity.window.decorView).any { it.resourceId == TestIds.FEED_LOADING })
        assertEquals(Feed.LOADING, compose.onNodeWithTag(TestIds.FEED_LOADING).fetchSemanticsNode().config[SemanticsProperties.Text].joinToString { it.text })
        compose.mainClock.advanceTimeBy(600)
        assertTrue("the spinner row still there within its second", there(TestIds.FEED_LOADING))
        assertFalse(there(TestIds.feedRow(21)))
        compose.mainClock.advanceTimeBy(400)
        assertFalse("the spinner row gone after its second", there(TestIds.FEED_LOADING))
        assertTrue("row 21 in the spinner row's place", there(TestIds.feedRow(21)))
        compose.mainClock.autoAdvance = true
    }

    @Test
    fun it_never_ends_and_every_visit_starts_again() {
        open()
        compose.mainClock.autoAdvance = false
        var count = Feed.PAGE
        repeat(5) {
            list.performScrollToIndex(count - 1)
            compose.mainClock.advanceTimeBy(32)
            list.performScrollToIndex(count)
            compose.mainClock.advanceTimeBy(32)
            assertTrue("a spinner row after row $count", there(TestIds.FEED_LOADING))
            compose.mainClock.advanceTimeBy(Feed.LOAD_MS + 50)
            list.performScrollToIndex(count)
            compose.mainClock.advanceTimeBy(32)
            assertTrue("row ${count + 1} after the load", there(TestIds.feedRow(count + 1)))
            count += Feed.PAGE
        }
        assertEquals(120, count)
        assertTrue(compose.onAllNodesWithTag(TestIds.FEED_SCREEN).fetchSemanticsNodes().isNotEmpty())
        compose.mainClock.autoAdvance = true
        assertTrue("nothing says the Feed ends", compose.onAllNodesWithTag(TestIds.FEED_LIST).fetchSemanticsNodes().isNotEmpty() &&
            A11yTree.of(compose.activity.window.decorView).none { it.text?.contains("End of the feed") == true })

        // a new visit starts again at row 1 with 20 rows
        compose.tap(TestIds.TOP_BAR_BACK)
        compose.tap(TestIds.LAB_FEED)
        assertTrue(there(TestIds.feedRow(1)))
        assertFalse(there(TestIds.FEED_LOADING))
        compose.mainClock.autoAdvance = false
        list.performScrollToIndex(19)
        compose.mainClock.advanceTimeBy(32)
        assertEquals("20 rows at the start of a visit", 20, lastShown(25))
        compose.mainClock.autoAdvance = true
    }
}

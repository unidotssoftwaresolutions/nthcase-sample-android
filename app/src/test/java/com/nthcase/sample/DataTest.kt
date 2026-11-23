package com.nthcase.sample

import com.nthcase.sample.data.Cart
import com.nthcase.sample.data.Catalog
import com.nthcase.sample.data.ClubForm
import com.nthcase.sample.data.Category
import com.nthcase.sample.data.Feed
import com.nthcase.sample.data.FormRules
import com.nthcase.sample.data.Lobby
import com.nthcase.sample.data.ShakeDetector
import com.nthcase.sample.data.Touch
import com.nthcase.sample.data.TouchLog
import com.nthcase.sample.data.formatPrice
import com.nthcase.sample.data.itemNumber
import com.nthcase.sample.ui.screens.swipeDirection
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Plain JVM tests of the seeded data and the rules every framework must share (SPEC.md §Data, §Form). */
class DataTest {
    @Test
    fun the_catalogue_is_forty_items_numbered_1_to_40_with_unique_names() {
        assertEquals((1..40).toList(), Catalog.items.map { it.id })
        assertEquals(40, Catalog.items.map { it.name }.toSet().size)
        assertTrue(Catalog.items.all { it.priceCents > 0 && it.description.endsWith(".") })
    }

    @Test
    fun prices_and_numbers_are_written_the_same_way_everywhere() {
        assertEquals("$14.00", formatPrice(1400))
        assertEquals("$9.50", formatPrice(950))
        assertEquals("$0.05", formatPrice(5))
        assertEquals("No. 007", itemNumber(7))
        assertEquals("No. 040", itemNumber(40))
    }

    @Test
    fun search_matches_names_anywhere_and_numbers_exactly() {
        assertEquals(listOf("Brass Desk Lamp", "Clip Reading Lamp", "Floor Lamp", "Bedside Lamp"), Catalog.search("lamp").map { it.name })
        assertEquals(listOf(7), Catalog.search("7").map { it.id })
        assertEquals(listOf(7), Catalog.search("No. 007").map { it.id })
        assertEquals(40, Catalog.search("  ").size)
        assertTrue(Catalog.search("zzz").isEmpty())
    }

    @Test
    fun the_cart_keeps_order_caps_quantities_and_totals() {
        val cart = Cart()
        cart.add(5, 1)
        cart.add(1, 2)
        cart.add(5, 1)
        assertEquals(listOf(5, 1), cart.lines.map { it.itemId })
        assertEquals(2, cart.quantityOf(5))
        assertEquals(4200 * 2 + 1400 * 2, cart.totalCents)
        cart.add(1, 20)
        assertEquals(Cart.MAX_QUANTITY, cart.quantityOf(1))
        cart.setQuantity(1, 0)
        assertEquals(Cart.MIN_QUANTITY, cart.quantityOf(1))
        cart.remove(5)
        assertEquals(1, cart.itemCount)
    }

    @Test
    fun the_form_names_every_invalid_field_and_accepts_a_good_one() {
        assertEquals(setOf("name", "email", "age", "password", "category", "date", "terms"), FormRules.errors(ClubForm()).keys)
        val good = ClubForm("Sam Rivera", "sam@example.test", "34", "garden42x", Category.Garden, "12 Nov 2026", false, 50, true)
        assertTrue(FormRules.errors(good).isEmpty())
        assertEquals(FormRules.AGE, FormRules.errors(good.copy(age = "17"))["age"])
        assertEquals(FormRules.PASSWORD, FormRules.errors(good.copy(password = "longenough"))["password"])
        assertEquals(FormRules.EMAIL, FormRules.errors(good.copy(email = "sam@test"))["email"])
    }

    @Test
    fun three_jolts_within_a_second_are_a_shake() {
        var shakes = 0
        val detector = ShakeDetector { shakes++ }
        val jolt = 3.2f * ShakeDetector.EARTH_GRAVITY
        detector.onReading(jolt, 0f, 0f, 0)
        detector.onReading(0f, 9.8f, 0f, 100) // resting: ignored
        detector.onReading(jolt, 0f, 0f, 300)
        assertEquals(0, shakes)
        detector.onReading(0f, jolt, 0f, 600)
        assertEquals(1, shakes)
        detector.onReading(jolt, 0f, 0f, 5000)
        detector.onReading(jolt, 0f, 0f, 6500) // the first jolt has left the window
        detector.onReading(jolt, 0f, 0f, 6600)
        assertEquals(1, shakes)
    }

    @Test
    fun a_swipe_takes_its_longest_axis() {
        assertEquals("left", swipeDirection(-200f, 30f, 100f))
        assertEquals("right", swipeDirection(200f, -30f, 100f))
        assertEquals("up", swipeDirection(10f, -300f, 100f))
        assertEquals("down", swipeDirection(-10f, 300f, 100f))
        assertNull(swipeDirection(40f, 40f, 100f))
    }

    /** Spec 1.2: More like this reads after the sheet's title, and a tap on a line is logged as a button's is. */
    @Test
    fun more_like_this_reads_after_the_sheets_title() {
        assertEquals(20, Lobby.SHEET_MORE_LINES)
        assertEquals("Like Bedside Lamp · 1 of 20", Lobby.sheetMoreLine("Bedside Lamp", 1))
        assertEquals("Like Warm, low desk lamps · 20 of 20", Lobby.sheetMoreLine(Lobby.banners[1], 20))
        assertEquals("lobby_sheet_more_row_7", TestIds.lobbySheetMoreRow(7))
        val log = TouchLog()
        log.add(Touch.Tap, Lobby.sheetMoreLine("Bedside Lamp", 7), "quick sheet, Bedside Lamp")
        assertEquals("Tapped: Like Bedside Lamp · 7 of 20 (quick sheet, Bedside Lamp)", log.text)
    }

    /** Spec 1.2: the Feed loads when its third row from the end shows, 20 at a time, with no end. */
    @Test
    fun the_feed_loads_when_its_third_row_from_the_end_shows() {
        assertEquals("Feed row 1", Feed.row(1))
        assertEquals("feed_row_61", TestIds.feedRow(61))
        assertEquals(18, Feed.trigger(20))
        assertFalse(Feed.startsLoad(lastShown = 17, count = 20, loading = false))
        assertTrue(Feed.startsLoad(lastShown = 18, count = 20, loading = false))
        assertFalse("no second load while one runs", Feed.startsLoad(lastShown = 20, count = 20, loading = true))
        assertFalse(Feed.startsLoad(lastShown = 37, count = 40, loading = false))
        assertTrue(Feed.startsLoad(lastShown = 38, count = 40, loading = false))
        // items as the list's layout places them: (index, offset, size), a view from 0 to 700
        val rows = (0 until 20).map { Triple(it, it * 56 - 252, 56) }
        assertEquals("row 18's top is at the view's end: not shown", 17, Feed.lastShown(rows, 0, 700, 20))
        val further = (0 until 20).map { Triple(it, it * 56 - 253, 56) }
        assertEquals(18, Feed.lastShown(further, 0, 700, 20))
        // the spinner row after the last row is not a row
        val atTheEnd = (0 until 21).map { Triple(it, it * 56 - 500, 56) }
        assertEquals(20, Feed.lastShown(atTheEnd, 0, 700, 20))
        assertEquals(0, Feed.lastShown(emptyList(), 0, 700, 20))
    }
}

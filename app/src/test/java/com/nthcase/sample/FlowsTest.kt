package com.nthcase.sample

import android.view.View
import android.widget.CompoundButton
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.assertContentDescriptionContains
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.doubleClick
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.pinch
import androidx.compose.ui.test.swipe
import androidx.compose.ui.test.swipeDown
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import androidx.compose.ui.test.swipeUp
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nthcase.sample.data.AppState
import com.nthcase.sample.data.FormRules
import com.nthcase.sample.data.ThemeMode
import com.nthcase.sample.ui.screens.ADDED_MESSAGE
import com.nthcase.sample.ui.screens.GestureWords
import com.nthcase.sample.ui.screens.JOINED_MESSAGE
import com.nthcase.sample.ui.screens.SIGN_IN_WRONG
import com.nthcase.sample.ui.screens.orderPlacedMessage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** The spec's scenarios (the files in nthcase/scenarios), played on the JVM with Robolectric: the same steps, the same checks. */
@RunWith(AndroidJUnit4::class)
class FlowsTest {
    private val compose = appCompose()

    @get:Rule
    val rules = coldStart(compose)

    private fun text(id: String, expected: String) = compose.id(id).assertTextEquals(expected)

    @Test
    fun a_wrong_password_is_refused_and_the_right_one_signs_in() {
        compose.onNodeWithTag(TestIds.SIGNIN_EMAIL).performTextInput(AppState.TEST_EMAIL)
        compose.onNodeWithTag(TestIds.SIGNIN_PASSWORD).performTextInput("letmein1")
        compose.tap(TestIds.SIGNIN_SUBMIT)
        text(TestIds.SIGNIN_ERROR, SIGN_IN_WRONG)
        compose.id(TestIds.SIGNIN_SCREEN).assertIsDisplayed()

        compose.tap(TestIds.SIGNIN_PASSWORD_TOGGLE)
        compose.onNodeWithTag(TestIds.SIGNIN_PASSWORD).assertTextContains("letmein1")
        compose.signIn()
        text(TestIds.SHOP_COUNT, "40 drawers · No. 001–040")
        compose.onNodeWithText("Stoneware Mug").assertIsDisplayed()
    }

    @Test
    fun search_narrows_the_list_and_clears() {
        compose.signIn()
        compose.onNodeWithTag(TestIds.SHOP_SEARCH).performTextInput("lamp")
        compose.waitForIdle()
        text(TestIds.SHOP_COUNT, "4 of 40 drawers")
        compose.id(TestIds.shopItem(5)).assertIsDisplayed()
        compose.id(TestIds.shopItem(1)).assertDoesNotExist()
        compose.tap(TestIds.SHOP_SEARCH_CLEAR)
        text(TestIds.SHOP_COUNT, "40 drawers · No. 001–040")
    }

    @Test
    fun add_two_mugs_change_the_cart_and_place_the_order() {
        compose.signIn()
        compose.tap(TestIds.shopItem(1))
        text(TestIds.ITEM_NUMBER, "No. 001")
        compose.tap(TestIds.ITEM_QTY_INCREASE)
        text(TestIds.ITEM_QTY_VALUE, "2")
        compose.tap(TestIds.ITEM_ADD_TO_CART)
        text(TestIds.SNACKBAR_TEXT, ADDED_MESSAGE)
        compose.tap(TestIds.SNACKBAR_ACTION)
        compose.onNodeWithTag(TestIds.NAV_CART).assertContentDescriptionContains("Cart, 2 items")
        text(TestIds.cartLineQty(1), "2")
        text(TestIds.CART_TOTAL, "$28.00")
        compose.tap(TestIds.cartLineIncrease(1))
        text(TestIds.CART_TOTAL, "$42.00")
        compose.tap(TestIds.CART_CHECKOUT)
        text(TestIds.DIALOG_TITLE, "Place this order?")
        compose.tap(TestIds.DIALOG_DISMISS)
        text(TestIds.CART_TOTAL, "$42.00")
        compose.tap(TestIds.CART_CHECKOUT)
        compose.tap(TestIds.DIALOG_CONFIRM)
        text(TestIds.SNACKBAR_TEXT, orderPlacedMessage(1))
        compose.id(TestIds.CART_EMPTY).assertIsDisplayed()
        compose.tap(TestIds.SNACKBAR_ACTION)
        compose.id(TestIds.ordersRow(1)).assertTextContains("3 items", substring = true)
    }

    @Test
    fun the_form_shows_every_error_then_accepts_valid_input() {
        compose.signIn()
        compose.openLab(TestIds.LAB_FORM)
        compose.tap(TestIds.FORM_SUBMIT)
        text(TestIds.FORM_NAME_ERROR, FormRules.NAME)
        text(TestIds.FORM_TERMS_ERROR, FormRules.TERMS)
        fillValidForm(compose)
        compose.tap(TestIds.FORM_SUBMIT)
        compose.id(TestIds.FORM_RESULT).assertTextContains("Welcome, Sam Rivera.", substring = true)
        compose.id(TestIds.FORM_RESULT).assertTextContains("Garden aisle", substring = true)
        text(TestIds.SNACKBAR_TEXT, JOINED_MESSAGE)
        compose.id(TestIds.FORM_NAME_ERROR).assertDoesNotExist()
    }

    @Test
    fun every_gesture_says_what_it_was() {
        compose.signIn()
        compose.openLab(TestIds.LAB_GESTURES)
        text(TestIds.GESTURES_STATUS, GestureWords.READY)
        text(TestIds.GESTURES_ORIENTATION, "Portrait")

        compose.onNodeWithTag(TestIds.GESTURES_TAP).performClick()
        text(TestIds.GESTURES_STATUS, GestureWords.TAPPED)
        text(TestIds.GESTURES_TAP_COUNT, "Taps: 1")
        compose.onNodeWithTag(TestIds.GESTURES_DOUBLE_TAP).performTouchInput { doubleClick() }
        text(TestIds.GESTURES_STATUS, GestureWords.DOUBLE_TAPPED)
        compose.onNodeWithTag(TestIds.GESTURES_LONG_PRESS).performTouchInput { longClick() }
        text(TestIds.GESTURES_STATUS, GestureWords.LONG_PRESSED)

        val swipe = compose.onNodeWithTag(TestIds.GESTURES_SWIPE)
        swipe.performTouchInput { swipeLeft() }; text(TestIds.GESTURES_STATUS, GestureWords.swiped("left"))
        swipe.performTouchInput { swipeRight() }; text(TestIds.GESTURES_STATUS, GestureWords.swiped("right"))
        swipe.performTouchInput { swipeUp() }; text(TestIds.GESTURES_STATUS, GestureWords.swiped("up"))
        swipe.performTouchInput { swipeDown() }; text(TestIds.GESTURES_STATUS, GestureWords.swiped("down"))

        val source = compose.onNodeWithTag(TestIds.GESTURES_DRAG_SOURCE).fetchSemanticsNode().boundsInRoot
        val target = compose.onNodeWithTag(TestIds.GESTURES_DROP_TARGET).fetchSemanticsNode().boundsInRoot
        compose.onNodeWithTag(TestIds.GESTURES_DRAG_SOURCE).performTouchInput {
            swipe(center, center + (target.center - source.center), durationMillis = 600)
        }
        text(TestIds.GESTURES_STATUS, GestureWords.DROPPED)

        compose.onNodeWithTag(TestIds.GESTURES_PINCH).performScrollTo().performTouchInput {
            pinch(center - Offset(20f, 0f), center - Offset(120f, 0f), center + Offset(20f, 0f), center + Offset(120f, 0f))
        }
        text(TestIds.GESTURES_STATUS, GestureWords.PINCHED_OUT)
        compose.id(TestIds.GESTURES_ZOOM_VALUE).assertTextContains("Zoom ", substring = true)

        compose.onNodeWithTag(TestIds.GESTURES_ROTATE).performTouchInput {
            // two fingers on a 80 px radius, turned a quarter clockwise (screen y points down)
            down(0, center + Offset(-80f, 0f)); down(1, center + Offset(80f, 0f))
            for (step in 1..10) {
                val a = Math.toRadians(9.0 * step)
                val o = Offset((80 * Math.cos(a)).toFloat(), (80 * Math.sin(a)).toFloat())
                moveTo(0, center - o); moveTo(1, center + o)
            }
            up(0); up(1)
        }
        text(TestIds.GESTURES_STATUS, GestureWords.ROTATED_CW)

        compose.onNodeWithTag(TestIds.GESTURES_SCROLL_TARGET).performScrollTo().performClick()
        text(TestIds.GESTURES_STATUS, GestureWords.SCROLLED_TO)
    }

    @Test
    fun the_slow_screen_spins_for_three_seconds_then_shows_its_slots() {
        compose.signIn()
        compose.tap(TestIds.NAV_LAB)
        compose.tapPaused(TestIds.LAB_SLOW)
        compose.mainClock.advanceTimeBy(1_900)
        compose.id(TestIds.SLOW_LOADING).assertIsDisplayed()
        compose.mainClock.advanceTimeBy(200)
        compose.mainClock.autoAdvance = true
        compose.id(TestIds.slowSlot(1)).assertTextContains("Monday · 8:00–10:00", substring = true)
    }

    @Test
    fun a_failed_load_retries_into_the_empty_state() {
        compose.signIn()
        AppState.failFirstLoad = true
        compose.openLab(TestIds.LAB_ORDERS)
        compose.id(TestIds.ORDERS_ERROR).assertIsDisplayed()
        compose.tap(TestIds.ORDERS_RETRY)
        compose.id(TestIds.ORDERS_EMPTY).assertTextContains("No orders yet")
    }

    @Test
    fun the_gift_card_screen_works_by_text_alone() {
        compose.signIn()
        compose.openLab(TestIds.LAB_GIFT_CARDS)
        compose.onNode(hasText("Card code") and hasSetTextAction()).performTextInput("moss-0025")
        compose.onNodeWithText("Check balance").performClick()
        compose.onNodeWithText("Balance: $25.00").assertIsDisplayed()
        // two buttons say "Choose": the second, on the right, is Stone
        compose.onAllNodes(hasText("Choose") and hasClickAction())[1].performClick()
        compose.onNodeWithText("A $25 card with the Stone design").assertIsDisplayed()
    }

    @Test
    fun the_xml_settings_drive_the_app() {
        compose.signIn()
        compose.tap(TestIds.NAV_SETTINGS)
        fun click(id: Int) { compose.runOnUiThread { compose.activity.findViewById<View>(id).performClick() }; compose.waitForIdle() }
        click(R.id.settings_slow_loading)
        assertTrue(AppState.slowLoading)
        click(R.id.settings_fail_loading)
        assertTrue(AppState.failFirstLoad)
        assertTrue(compose.activity.findViewById<CompoundButton>(R.id.settings_fail_loading).isChecked)
        click(R.id.settings_theme_dark)
        assertEquals(ThemeMode.Dark, AppState.themeMode)
        AppState.themeMode = ThemeMode.System
        compose.waitForIdle()
        click(R.id.settings_sign_out)
        compose.id(TestIds.SIGNIN_SCREEN).assertIsDisplayed()
    }

    companion object {
        fun fillValidForm(compose: AppCompose) {
            compose.onNodeWithTag(TestIds.FORM_NAME).performTextInput("Sam Rivera")
            compose.onNodeWithTag(TestIds.FORM_EMAIL).performTextInput("sam@example.test")
            compose.onNodeWithTag(TestIds.FORM_AGE).performTextInput("34")
            compose.onNodeWithTag(TestIds.FORM_PASSWORD).performTextInput("garden42x")
            compose.tap(TestIds.FORM_CATEGORY)
            compose.tap(TestIds.formCategoryOption("garden"))
            compose.tap(TestIds.FORM_DATE)
            // a day reads "Sunday, November 15, 2026": pick the 15th of the month the picker opens on
            compose.onAllNodes(hasText(" 15, ", substring = true) and hasClickAction()).onFirst().performClick()
            compose.tap(TestIds.FORM_DATE_CONFIRM)
            compose.onNodeWithTag(TestIds.FORM_BUDGET).performScrollTo().performSemanticsAction(SemanticsActions.SetProgress) { it(120f) }
            compose.tap(TestIds.FORM_TERMS)
            compose.waitForIdle()
            compose.id(TestIds.FORM_BUDGET_VALUE).assertTextEquals("$120")
            assertTrue(compose.onAllNodesWithTag(TestIds.FORM_DATE_ERROR).fetchSemanticsNodes().isEmpty())
        }
    }
}

private fun hasSetTextAction() = androidx.compose.ui.test.hasSetTextAction()

package com.nthcase.sample

import android.view.View
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nthcase.sample.data.AppState
import com.nthcase.sample.data.Lobby
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Every spec id is present on its screen, in the state the spec names (nthcase/test-ids.json, column "when").
 * Each test puts the app in one or more states and checks the ids the spec lists for them. SpecIdsTest checks that
 * every native spec id is named in this file.
 */
@RunWith(AndroidJUnit4::class)
class ScreenIdsTest {
    private val compose = appCompose()

    @get:Rule
    val rules = coldStart(compose)

    private fun present(vararg ids: String) = ids.forEach { id ->
        // the merged tree is the accessibility tree Appium reads: an id folded into a parent node is not there
        val found = compose.onAllNodesWithTag(id).fetchSemanticsNodes().isNotEmpty()
        assertTrue("$id is on screen, as its own accessibility node", found)
    }

    private fun presentView(vararg ids: String) = ids.forEach { name ->
        val activity = compose.activity
        val resId = activity.resources.getIdentifier(name, "id", activity.packageName)
        assertTrue("$name is an android:id", resId != 0)
        var view: View? = null
        compose.runOnUiThread { view = activity.findViewById(resId) ?: activity.window.decorView.rootView.findViewById(resId) }
        assertNotNull("$name is on screen", view)
    }

    @Test
    fun sign_in() {
        present(TestIds.SIGNIN_SCREEN, TestIds.SIGNIN_EMAIL, TestIds.SIGNIN_PASSWORD, TestIds.SIGNIN_PASSWORD_TOGGLE, TestIds.SIGNIN_SUBMIT, TestIds.SIGNIN_TEST_ACCOUNT, TestIds.SIGNIN_USE_TEST_ACCOUNT)
        compose.tap(TestIds.SIGNIN_SUBMIT)
        present(TestIds.SIGNIN_ERROR)
    }

    @Test
    fun shop_and_chrome() {
        compose.signIn()
        present(TestIds.SHOP_SCREEN, TestIds.SHOP_SEARCH, TestIds.SHOP_COUNT, TestIds.SHOP_LIST, TestIds.shopItem(1))
        present(TestIds.NAV_SHOP, TestIds.NAV_CART, TestIds.NAV_LAB, TestIds.NAV_SETTINGS)
        compose.onNodeWithTag(TestIds.SHOP_LIST).performScrollToNode(hasTestTag(TestIds.shopItem(40)))
        present(TestIds.shopItem(40))
        compose.onNodeWithTag(TestIds.SHOP_SEARCH).performTextInput("zzz")
        compose.waitForIdle()
        present(TestIds.SHOP_SEARCH_CLEAR, TestIds.SHOP_EMPTY, TestIds.SHOP_EMPTY_ACTION)
    }

    @Test
    fun shop_loading_and_error() {
        compose.signIn()
        AppState.slowLoading = true
        compose.tap(TestIds.NAV_LAB)
        compose.tapPaused(TestIds.NAV_SHOP)
        present(TestIds.SHOP_LOADING)
        compose.mainClock.autoAdvance = true
        AppState.slowLoading = false
        AppState.failFirstLoad = true
        compose.tap(TestIds.NAV_LAB)
        compose.tap(TestIds.NAV_SHOP)
        present(TestIds.SHOP_ERROR, TestIds.SHOP_RETRY)
    }

    @Test
    fun item_cart_snackbar_dialog_and_badge() {
        compose.signIn()
        compose.tap(TestIds.shopItem(1))
        present(TestIds.ITEM_SCREEN, TestIds.ITEM_IMAGE, TestIds.ITEM_NUMBER, TestIds.ITEM_NAME, TestIds.ITEM_PRICE, TestIds.ITEM_DESCRIPTION,
            TestIds.ITEM_QTY_DECREASE, TestIds.ITEM_QTY_VALUE, TestIds.ITEM_QTY_INCREASE, TestIds.ITEM_ADD_TO_CART, TestIds.TOP_BAR_TITLE, TestIds.TOP_BAR_BACK)
        compose.tap(TestIds.ITEM_ADD_TO_CART)
        present(TestIds.SNACKBAR, TestIds.SNACKBAR_TEXT, TestIds.SNACKBAR_ACTION)
        compose.tap(TestIds.SNACKBAR_ACTION)
        present(TestIds.CART_SCREEN, TestIds.CART_LIST, TestIds.cartLine(1), TestIds.cartLineDecrease(1), TestIds.cartLineQty(1),
            TestIds.cartLineIncrease(1), TestIds.cartLineRemove(1), TestIds.cartLineTotal(1), TestIds.CART_TOTAL, TestIds.CART_CHECKOUT)
        compose.tap(TestIds.CART_CHECKOUT)
        present(TestIds.DIALOG, TestIds.DIALOG_TITLE, TestIds.DIALOG_TEXT, TestIds.DIALOG_CONFIRM, TestIds.DIALOG_DISMISS)
        compose.tap(TestIds.DIALOG_CONFIRM)
        present(TestIds.CART_EMPTY, TestIds.CART_EMPTY_ACTION)
        compose.tap(TestIds.SNACKBAR_ACTION)
        present(TestIds.ORDERS_SCREEN, TestIds.ORDERS_LIST, TestIds.ordersRow(1))
    }

    @Test
    fun lab() {
        compose.signIn()
        compose.tap(TestIds.NAV_LAB)
        present(TestIds.LAB_SCREEN, TestIds.LAB_FORM, TestIds.LAB_GESTURES, TestIds.LAB_WEBVIEW, TestIds.LAB_SLOW, TestIds.LAB_ORDERS, TestIds.LAB_GIFT_CARDS,
            TestIds.LAB_LOBBY, TestIds.LAB_LOBBY_VIEWS)
    }

    @Test
    fun form() {
        compose.signIn()
        compose.openLab(TestIds.LAB_FORM)
        present(TestIds.FORM_SCREEN, TestIds.FORM_NAME, TestIds.FORM_EMAIL, TestIds.FORM_AGE, TestIds.FORM_PASSWORD, TestIds.FORM_PASSWORD_TOGGLE,
            TestIds.FORM_CATEGORY, TestIds.FORM_DATE, TestIds.FORM_NEWSLETTER, TestIds.FORM_BUDGET, TestIds.FORM_BUDGET_VALUE, TestIds.FORM_TERMS, TestIds.FORM_SUBMIT)
        compose.tap(TestIds.FORM_SUBMIT)
        present(TestIds.FORM_NAME_ERROR, TestIds.FORM_EMAIL_ERROR, TestIds.FORM_AGE_ERROR, TestIds.FORM_PASSWORD_ERROR, TestIds.FORM_CATEGORY_ERROR,
            TestIds.FORM_DATE_ERROR, TestIds.FORM_TERMS_ERROR)
        compose.tap(TestIds.FORM_CATEGORY)
        present(*listOf("kitchen", "desk", "garden", "home").map(TestIds::formCategoryOption).toTypedArray())
        compose.tap(TestIds.formCategoryOption("garden"))
        compose.tap(TestIds.FORM_DATE)
        present(TestIds.FORM_DATE_PICKER, TestIds.FORM_DATE_CONFIRM, TestIds.FORM_DATE_CANCEL)
        compose.tap(TestIds.FORM_DATE_CANCEL)
        FlowsTest.fillValidForm(compose)
        compose.tap(TestIds.FORM_SUBMIT)
        present(TestIds.FORM_RESULT)
    }

    @Test
    fun gestures() {
        compose.signIn()
        compose.openLab(TestIds.LAB_GESTURES)
        present(TestIds.GESTURES_SCREEN, TestIds.GESTURES_STATUS, TestIds.GESTURES_ORIENTATION, TestIds.GESTURES_TAP, TestIds.GESTURES_TAP_COUNT,
            TestIds.GESTURES_DOUBLE_TAP, TestIds.GESTURES_LONG_PRESS, TestIds.GESTURES_SWIPE, TestIds.GESTURES_DRAG_SOURCE, TestIds.GESTURES_DROP_TARGET,
            TestIds.GESTURES_PINCH, TestIds.GESTURES_ZOOM_VALUE, TestIds.GESTURES_ROTATE, TestIds.GESTURES_ROTATION_VALUE, TestIds.GESTURES_SCROLL,
            TestIds.GESTURES_SCROLL_TARGET)
    }

    @Test
    fun webview_and_slow() {
        compose.signIn()
        compose.openLab(TestIds.LAB_WEBVIEW)
        present(TestIds.WEBVIEW_SCREEN, TestIds.WEBVIEW, TestIds.WEBVIEW_STATUS)
        presentView(TestIds.WEBVIEW)
        compose.activity.onBackPressedDispatcher.let { d -> compose.runOnUiThread { d.onBackPressed() } }
        compose.tapPaused(TestIds.LAB_SLOW)
        present(TestIds.SLOW_SCREEN, TestIds.SLOW_LOADING)
        compose.mainClock.advanceTimeBy(2_100)
        compose.mainClock.autoAdvance = true
        compose.waitForIdle()
        present(TestIds.SLOW_CONTENT, TestIds.slowSlot(1), TestIds.slowSlot(5), TestIds.SLOW_RELOAD)
    }

    @Test
    fun orders_states() {
        compose.signIn()
        compose.openLab(TestIds.LAB_ORDERS)
        present(TestIds.ORDERS_SCREEN, TestIds.ORDERS_EMPTY, TestIds.ORDERS_EMPTY_ACTION)
        compose.tap(TestIds.TOP_BAR_BACK)
        AppState.failFirstLoad = true
        compose.tap(TestIds.LAB_ORDERS)
        present(TestIds.ORDERS_ERROR, TestIds.ORDERS_RETRY)
        compose.tap(TestIds.TOP_BAR_BACK)
        AppState.failFirstLoad = false
        AppState.slowLoading = true
        compose.tapPaused(TestIds.LAB_ORDERS)
        present(TestIds.ORDERS_LOADING)
        compose.mainClock.autoAdvance = true
    }

    @Test
    fun deep_link_to_an_unknown_item() {
        compose.signIn()
        AppState.pendingItemId = "999"
        compose.waitForIdle()
        present(TestIds.ITEM_NOT_FOUND, TestIds.ITEM_NOT_FOUND_ACTION)
    }

    @Test
    fun settings_xml_screen() {
        compose.signIn()
        compose.tap(TestIds.NAV_SETTINGS)
        presentView(TestIds.SETTINGS_SCREEN, TestIds.SETTINGS_ACCOUNT_EMAIL, TestIds.SETTINGS_ORDERS, TestIds.SETTINGS_SIGN_OUT, TestIds.SETTINGS_THEME,
            TestIds.SETTINGS_THEME_SYSTEM, TestIds.SETTINGS_THEME_LIGHT, TestIds.SETTINGS_THEME_DARK, TestIds.SETTINGS_NOTIFICATIONS,
            TestIds.SETTINGS_NOTIFICATIONS_STATUS, TestIds.SETTINGS_SEND_NOTIFICATION, TestIds.SETTINGS_SLOW_LOADING, TestIds.SETTINGS_FAIL_LOADING,
            TestIds.SETTINGS_RESET, TestIds.SETTINGS_VERSION)
        compose.runOnUiThread { compose.activity.findViewById<View>(R.id.settings_reset).performClick() }
        compose.waitForIdle()
        org.robolectric.shadows.ShadowLooper.idleMainLooper()
        val dialog = org.robolectric.shadows.ShadowDialog.getLatestDialog()
        assertNotNull("the reset dialog is open", dialog)
        listOf(TestIds.SETTINGS_RESET_CONFIRM, TestIds.SETTINGS_RESET_CANCEL).forEach { name ->
            val resId = compose.activity.resources.getIdentifier(name, "id", compose.activity.packageName)
            assertNotNull("$name is in the dialog", dialog.findViewById<View>(resId))
        }
    }

    /** The Lobby (1.1), Compose: every id in the state its "when" names. LobbyTest reads the same ids in the accessibility tree. */
    @Test
    fun lobby() {
        compose.signIn()
        compose.openLab(TestIds.LAB_LOBBY)
        present(TestIds.LOBBY_SCREEN, TestIds.LOBBY_TOUCH_LOG, TestIds.LOBBY_TOUCH_COUNT, TestIds.LOBBY_TOUCH_RESET, TestIds.LOBBY_SAVED_DELAY,
            *Lobby.DELAYS.map(TestIds::lobbySavedDelay).toTypedArray(), TestIds.LOBBY_PAGER, TestIds.lobbyBanner(1), TestIds.LOBBY_PAGER_POSITION,
            TestIds.LOBBY_PAGER_AUTO, TestIds.LOBBY_PAGE, TestIds.LOBBY_CHIPS, TestIds.lobbyChip(1), TestIds.lobbyRowPosition(1), TestIds.lobbyRow(1),
            TestIds.lobbyRowTile(1, 1), TestIds.LOBBY_BAR, *TestIds.lobbyBarItems.toTypedArray(), TestIds.LOBBY_PAGER_SHOWN,
            TestIds.LOBBY_TOP_ROW_POSITION, TestIds.LOBBY_TOP_ROW, TestIds.lobbyTopRowTile(1))
        compose.onNodeWithTag(TestIds.LOBBY_TOP_ROW).performScrollToIndex(9)
        present(TestIds.lobbyTopRowTile(10))
        fun scrollTo(id: String) = compose.onNodeWithTag(TestIds.LOBBY_PAGE).performScrollToNode(hasTestTag(id))
        scrollTo(TestIds.LOBBY_GRID); present(TestIds.LOBBY_GRID, TestIds.lobbyGridCell(1))
        scrollTo(TestIds.LOBBY_INNER_LIST); present(TestIds.LOBBY_INNER_LIST, TestIds.lobbyInnerRow(1))
        scrollTo(TestIds.LOBBY_BOARD); present(TestIds.LOBBY_BOARD, TestIds.LOBBY_BOARD_POSITION, TestIds.lobbyBoardCell(1, 1))
        scrollTo(TestIds.LOBBY_FOOTER); present(TestIds.LOBBY_FOOTER)
        compose.tap(TestIds.lobbyBanner(1))
        compose.mainClock.advanceTimeBy(400)
        present(TestIds.LOBBY_PROMO, TestIds.LOBBY_PROMO_BACK)
        compose.tap(TestIds.LOBBY_PROMO_BACK)
        compose.onNodeWithTag(TestIds.LOBBY_PAGE).performScrollToIndex(1)
        compose.tap(TestIds.lobbyRowTile(1, 1))
        compose.mainClock.advanceTimeBy(400)
        present(TestIds.LOBBY_DETAIL, TestIds.LOBBY_DETAIL_BACK)
        compose.tap(TestIds.LOBBY_DETAIL_BACK)
        compose.onNodeWithTag(TestIds.lobbyRowTile(1, 1)).performTouchInput { longClick() }
        compose.waitForIdle()
        present(TestIds.LOBBY_SHEET, TestIds.LOBBY_SHEET_TITLE, *TestIds.lobbySheetButtons.toTypedArray())
        compose.tap(TestIds.LOBBY_SHEET_PREVIEW)
        compose.tap(TestIds.LOBBY_BAR_SAVED)
        present(TestIds.LOBBY_SAVED_SCREEN)
    }

    /** The Lobby (1.1), Android Views: every id is a view id on the screen; the sheet's in its own dialog window. */
    @Test
    fun lobby_views() {
        compose.signIn()
        compose.openLab(TestIds.LAB_LOBBY_VIEWS)
        org.robolectric.shadows.ShadowLooper.idleMainLooper()
        presentView(TestIds.LOBBY_SCREEN, TestIds.LOBBY_TOUCH_LOG, TestIds.LOBBY_TOUCH_COUNT, TestIds.LOBBY_TOUCH_RESET, TestIds.LOBBY_SAVED_DELAY,
            *Lobby.DELAYS.map(TestIds::lobbySavedDelay).toTypedArray(), TestIds.LOBBY_PAGER, TestIds.lobbyBanner(1), TestIds.LOBBY_PAGER_POSITION,
            TestIds.LOBBY_PAGER_AUTO, TestIds.LOBBY_PAGE, TestIds.LOBBY_CHIPS, TestIds.lobbyChip(1), TestIds.lobbyRowPosition(1), TestIds.lobbyRow(1),
            TestIds.lobbyRowTile(1, 1), TestIds.LOBBY_GRID, TestIds.lobbyGridCell(1), TestIds.LOBBY_INNER_LIST, TestIds.lobbyInnerRow(1),
            TestIds.LOBBY_BOARD, TestIds.LOBBY_BOARD_POSITION, TestIds.lobbyBoardCell(1, 1), TestIds.LOBBY_FOOTER, TestIds.LOBBY_BAR,
            *TestIds.lobbyBarItems.toTypedArray(), TestIds.LOBBY_DETAIL, TestIds.LOBBY_DETAIL_BACK, TestIds.LOBBY_PROMO, TestIds.LOBBY_PROMO_BACK,
            TestIds.LOBBY_SAVED_SCREEN, TestIds.LOBBY_PAGER_SHOWN, TestIds.LOBBY_TOP_ROW_POSITION, TestIds.LOBBY_TOP_ROW,
            TestIds.lobbyTopRowTile(1))
        val activity = compose.activity
        compose.runOnUiThread {
            activity.findViewById<androidx.recyclerview.widget.RecyclerView>(activity.resources.getIdentifier(TestIds.LOBBY_TOP_ROW, "id", activity.packageName)).scrollToPosition(9)
        }
        org.robolectric.shadows.ShadowLooper.idleMainLooper()
        presentView(TestIds.lobbyTopRowTile(10))
        compose.runOnUiThread { activity.findViewById<View>(activity.resources.getIdentifier(TestIds.lobbyRowTile(1, 1), "id", activity.packageName)).performLongClick() }
        org.robolectric.shadows.ShadowLooper.idleMainLooper()
        val sheet = org.robolectric.shadows.ShadowDialog.getLatestDialog()
        assertNotNull("the quick sheet is open", sheet)
        (listOf(TestIds.LOBBY_SHEET, TestIds.LOBBY_SHEET_TITLE) + TestIds.lobbySheetButtons).forEach { name ->
            assertNotNull("$name is in the sheet", sheet.findViewById<View>(activity.resources.getIdentifier(name, "id", activity.packageName)))
        }
    }

    @Test
    fun gift_cards_has_no_test_ids_at_all() {
        compose.signIn()
        compose.openLab(TestIds.LAB_GIFT_CARDS)
        val tagged = compose.onRoot(useUnmergedTree = true).fetchSemanticsNode().let { root ->
            buildList {
                fun walk(node: androidx.compose.ui.semantics.SemanticsNode) {
                    node.config.getOrNull(SemanticsProperties.TestTag)?.let { add(it) }
                    node.children.forEach(::walk)
                }
                walk(root)
            }
        }
        // The bottom bar is app chrome and is hidden here; the screen itself carries nothing.
        assertEquals(emptyList<String>(), tagged)
        compose.onNodeWithTag(TestIds.TOP_BAR_TITLE).assertDoesNotExist()
        compose.onNodeWithText("Gift cards").assertIsDisplayed()
    }
}

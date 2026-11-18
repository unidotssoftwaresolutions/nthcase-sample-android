package com.nthcase.sample

import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.isRoot
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.click
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTouchInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nthcase.sample.data.AppState
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.shadows.ShadowDialog
import org.robolectric.shadows.ShadowLooper
import java.io.File

/**
 * Draws every screen, light and dark, to one PNG per screen under `app/build/screens/<theme>` on the JVM: Robolectric's native graphics,
 * never a device or an emulator. `./gradlew updateScreenshots` copies them to docs/screenshots for the README.
 */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ScreenPicturesTest {
    private val compose = appCompose()

    @get:Rule
    val rules = coldStart(compose)

    @Test
    fun light() = drawAll("light")

    @Test
    @Config(qualifiers = "+night")
    fun dark() = drawAll("dark")

    private fun drawAll(theme: String) {
        val out = File("build/screens/$theme").apply { mkdirs() }
        fun shot(name: String) {
            compose.waitForIdle()
            save(compose.onAllNodes(isRoot()).onFirst().captureToImage().asAndroidBitmap(), File(out, "$name.png"))
        }
        fun dialogShot(name: String) {
            compose.waitForIdle()
            save(compose.onNode(isDialog()).captureToImage().asAndroidBitmap(), File(out, "$name.png"))
        }

        shot("01-sign-in")
        compose.tap(TestIds.SIGNIN_SUBMIT)
        shot("02-sign-in-error")
        compose.signIn()
        shot("03-shop")
        compose.onNodeWithTag(TestIds.SHOP_SEARCH).performTextInput("lamp")
        shot("04-shop-search")
        compose.tap(TestIds.SHOP_SEARCH_CLEAR)
        compose.onNodeWithTag(TestIds.SHOP_SEARCH).performTextInput("velvet")
        shot("05-shop-no-match")
        compose.tap(TestIds.SHOP_EMPTY_ACTION)

        compose.tap(TestIds.shopItem(5))
        compose.tap(TestIds.ITEM_QTY_INCREASE)
        shot("06-item")
        compose.tap(TestIds.ITEM_ADD_TO_CART)
        shot("07-item-snackbar")
        compose.mainClock.advanceTimeBy(SNACKBAR_GONE_MS)
        compose.tap(TestIds.TOP_BAR_BACK)
        compose.tap(TestIds.shopItem(1))
        compose.tap(TestIds.ITEM_ADD_TO_CART)
        compose.tap(TestIds.SNACKBAR_ACTION)
        shot("08-cart")
        compose.tap(TestIds.CART_CHECKOUT)
        dialogShot("09-cart-dialog")
        compose.tap(TestIds.DIALOG_CONFIRM)
        shot("10-cart-empty")
        compose.mainClock.advanceTimeBy(SNACKBAR_GONE_MS)

        compose.tap(TestIds.NAV_LAB)
        shot("11-lab")
        compose.tap(TestIds.LAB_FORM)
        compose.tap(TestIds.FORM_SUBMIT)
        shot("12-form-errors")
        FlowsTest.fillValidForm(compose)
        compose.tap(TestIds.FORM_SUBMIT)
        shot("13-form-done")
        compose.mainClock.advanceTimeBy(SNACKBAR_GONE_MS)
        compose.tap(TestIds.TOP_BAR_BACK)

        compose.tap(TestIds.LAB_GESTURES)
        compose.tap(TestIds.GESTURES_TAP)
        shot("14-gestures")
        compose.tap(TestIds.TOP_BAR_BACK)

        compose.tap(TestIds.LAB_WEBVIEW)
        shot("15-webview")
        compose.tap(TestIds.TOP_BAR_BACK)

        compose.tapPaused(TestIds.LAB_SLOW)
        save(compose.onAllNodes(isRoot()).onFirst().captureToImage().asAndroidBitmap(), File(out, "16-slow-loading.png"))
        compose.mainClock.advanceTimeBy(2_100)
        compose.mainClock.autoAdvance = true
        shot("17-slow-done")
        compose.tap(TestIds.TOP_BAR_BACK)

        compose.tap(TestIds.LAB_ORDERS)
        shot("18-orders")
        compose.tap(TestIds.TOP_BAR_BACK)
        AppState.cart.clear()
        AppState.orders.clear()
        compose.tap(TestIds.LAB_ORDERS)
        shot("19-orders-empty")
        compose.tap(TestIds.TOP_BAR_BACK)
        AppState.failFirstLoad = true
        compose.tap(TestIds.LAB_ORDERS)
        shot("20-orders-error")
        AppState.failFirstLoad = false
        compose.tap(TestIds.TOP_BAR_BACK)

        compose.tap(TestIds.LAB_GIFT_CARDS)
        // no ids here: the left-hand "Choose" is Fern
        compose.onAllNodes(hasText("Choose") and hasClickAction())[0].performClick()
        shot("21-gift-cards")
        // the gift-cards screen has no ids, so leave it with the system Back
        compose.runOnUiThread { compose.activity.onBackPressedDispatcher.onBackPressed() }

        compose.tap(TestIds.NAV_SETTINGS)
        shot("22-settings")
        val settings = compose.onAllNodes(isRoot()).onFirst().captureToImage().asAndroidBitmap()
        compose.runOnUiThread { compose.activity.findViewById<android.view.View>(R.id.settings_reset).performClick() }
        ShadowLooper.idleMainLooper()
        val dialog = ShadowDialog.getLatestDialog()
        val decor = dialog.window!!.decorView
        ShadowLooper.idleMainLooper()
        // The XML dialog is its own window: draw it centred over the settings screen and a scrim, as a phone shows it.
        val bitmap = settings.copy(Bitmap.Config.ARGB_8888, true)
        Canvas(bitmap).apply {
            drawColor(0x66000000)
            translate((bitmap.width - decor.width) / 2f, (bitmap.height - decor.height) / 2f)
            decor.draw(this)
        }
        save(bitmap, File(out, "23-settings-dialog.png"))
        compose.runOnUiThread { dialog.dismiss() }

        AppState.pendingItemId = "999"
        shot("24-deep-link-unknown")
        compose.tap(TestIds.ITEM_NOT_FOUND_ACTION)

        // the Lobby (spec 1.1), Compose
        compose.openLab(TestIds.LAB_LOBBY)
        shot("25-lobby")
        compose.onNodeWithTag(TestIds.LOBBY_PAGE).performScrollToNode(hasTestTag(TestIds.LOBBY_BOARD))
        compose.onNodeWithTag(TestIds.lobbyBoardCell(3, 4)).performTouchInput { click() }
        compose.mainClock.advanceTimeBy(400)
        shot("26-lobby-board")
        compose.onNodeWithTag(TestIds.LOBBY_PAGE).performScrollToIndex(1)
        val lobby = compose.onAllNodes(isRoot()).onFirst().captureToImage().asAndroidBitmap()
        compose.onNodeWithTag(TestIds.lobbyRowTile(1, 2)).performTouchInput { longClick() }
        compose.waitForIdle()
        // the quick sheet is a window of its own: draw it over the Lobby, as a phone shows it
        ShadowLooper.idleMainLooper()
        val sheetDecor = ShadowDialog.getLatestDialog().window!!.decorView
        val withSheet = lobby.copy(Bitmap.Config.ARGB_8888, true)
        Canvas(withSheet).apply {
            val origin = IntArray(2).also(sheetDecor::getLocationOnScreen)
            translate(origin[0].toFloat(), origin[1].toFloat())
            sheetDecor.draw(this)
        }
        save(withSheet, File(out, "27-lobby-sheet.png"))
        compose.tap(TestIds.LOBBY_SHEET_RATE)
        compose.tap(TestIds.TOP_BAR_BACK)

        // the same Lobby in Android Views
        compose.tap(TestIds.LAB_LOBBY_VIEWS)
        ShadowLooper.idleMainLooper()
        shot("28-lobby-views")
        val activity = compose.activity
        compose.runOnUiThread {
            val page = activity.findViewById<androidx.core.widget.NestedScrollView>(activity.resources.getIdentifier(TestIds.LOBBY_PAGE, "id", activity.packageName))
            val board = activity.findViewById<android.view.View>(activity.resources.getIdentifier(TestIds.LOBBY_BOARD, "id", activity.packageName))
            page.scrollTo(0, (board.parent as android.view.View).top - 120)
        }
        ShadowLooper.idleMainLooper()
        shot("29-lobby-views-board")

        // Show the pager off (1.1): the Top 10 row moves up into the pager's place, in both builds
        compose.runOnUiThread {
            activity.findViewById<androidx.core.widget.NestedScrollView>(activity.resources.getIdentifier(TestIds.LOBBY_PAGE, "id", activity.packageName)).scrollTo(0, 0)
            activity.findViewById<android.view.View>(activity.resources.getIdentifier(TestIds.LOBBY_PAGER_SHOWN, "id", activity.packageName)).performClick()
        }
        ShadowLooper.idleMainLooper()
        shot("31-lobby-views-no-pager")
        compose.tap(TestIds.TOP_BAR_BACK)
        compose.tap(TestIds.LAB_LOBBY)
        shot("30-lobby-no-pager")
        compose.tap(TestIds.LOBBY_PAGER_SHOWN)
    }

    private companion object {
        /** A snackbar stays 10 s (SnackbarDuration.Long); move the clock past it before the next screen. */
        const val SNACKBAR_GONE_MS = 11_000L
    }

    private fun save(bitmap: Bitmap, file: File) {
        file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        assertTrue(file.length() > 0)
    }
}

package com.nthcase.sample

import android.content.Intent
import android.net.Uri
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nthcase.sample.data.DeepLinks
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** `nthcase-sample://item/{id}`: from a cold start it asks for sign-in first, then opens the item. */
@RunWith(AndroidJUnit4::class)
class DeepLinkTest {
    private val compose = createEmptyComposeRule()

    @get:Rule
    val rules = coldStart(compose)

    @Test
    fun the_link_is_parsed_strictly() {
        assertEquals("7", DeepLinks.itemId(Uri.parse("nthcase-sample://item/7")))
        assertEquals("abc", DeepLinks.itemId(Uri.parse("nthcase-sample://item/abc")))
        assertNull(DeepLinks.itemId(Uri.parse("nthcase-sample://cart/7")))
        assertNull(DeepLinks.itemId(Uri.parse("https://item/7")))
        assertNull(DeepLinks.itemId(Uri.parse("nthcase-sample://item/")))
        assertEquals("nthcase-sample://item/12", DeepLinks.forItem(12))
    }

    @Test
    fun a_cold_deep_link_signs_in_then_opens_the_item() {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(DeepLinks.forItem(7)))
            .setClass(ApplicationProvider.getApplicationContext(), MainActivity::class.java)
        ActivityScenario.launch<MainActivity>(intent).use {
            compose.onNodeWithTag(TestIds.SIGNIN_USE_TEST_ACCOUNT).performClick()
            compose.onNodeWithTag(TestIds.SIGNIN_SUBMIT).performClick()
            compose.waitForIdle()
            compose.onNodeWithTag(TestIds.ITEM_NAME, useUnmergedTree = true).assertTextEquals("Wall Clock")
            compose.onNodeWithTag(TestIds.ITEM_NUMBER, useUnmergedTree = true).assertTextEquals("No. 007")
            // Back from a deep-linked item lands on the shop, not out of the app.
            compose.onNodeWithTag(TestIds.TOP_BAR_BACK).performClick()
            compose.waitForIdle()
            compose.onNodeWithTag(TestIds.SHOP_SCREEN).assertExists()
        }
    }
}

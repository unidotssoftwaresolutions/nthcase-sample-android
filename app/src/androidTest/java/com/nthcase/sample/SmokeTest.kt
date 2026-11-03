package com.nthcase.sample

import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * One instrumented smoke test, for an emulator in CI or your own device (`./gradlew connectedDebugAndroidTest`).
 * The JVM tests under src/test cover the same ground with Robolectric and need no device.
 */
@RunWith(AndroidJUnit4::class)
class SmokeTest {
    @get:Rule
    val compose = createAndroidComposeRule<MainActivity>()

    @Test
    fun sign_in_and_open_the_first_drawer() {
        compose.onNodeWithTag(TestIds.SIGNIN_USE_TEST_ACCOUNT).performClick()
        compose.onNodeWithTag(TestIds.SIGNIN_SUBMIT).performClick()
        compose.onNodeWithTag(TestIds.shopItem(1)).performClick()
        compose.onNodeWithTag(TestIds.ITEM_NAME).assertTextEquals("Stoneware Mug")
    }
}

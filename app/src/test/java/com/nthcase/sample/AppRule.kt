package com.nthcase.sample

import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.nthcase.sample.data.AppState
import org.junit.rules.RuleChain
import org.junit.rules.TestRule
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/** Every UI test starts from a cold start: AppState reset before the activity launches, and again after. */
fun coldStart(compose: TestRule): TestRule = RuleChain.outerRule(object : TestWatcher() {
    override fun starting(description: Description) = AppState.reset()
    override fun finished(description: Description) = AppState.reset()
}).around(compose)

typealias AppCompose = AndroidComposeTestRule<ActivityScenarioRule<MainActivity>, MainActivity>

fun appCompose(): AppCompose = createAndroidComposeRule<MainActivity>()

/** A node by its id in the merged tree: the accessibility tree Appium reads, with the text Appium reads. */
fun AppCompose.id(id: String): SemanticsNodeInteraction = onNodeWithTag(id)

fun AppCompose.tap(id: String) {
    onNodeWithTag(id).performScrollToIfPossible().performClick()
    waitForIdle()
}

private fun SemanticsNodeInteraction.performScrollToIfPossible(): SemanticsNodeInteraction =
    try { performScrollTo() } catch (_: AssertionError) { this }

/**
 * Taps, then holds the clock 1 s later: the screen's transition has finished and a 3 s load is still loading. The
 * caller turns `mainClock.autoAdvance` back on.
 */
fun AppCompose.tapPaused(id: String) {
    onNodeWithTag(id).performClick()
    mainClock.autoAdvance = false
    mainClock.advanceTimeBy(1_000)
}

/** Signs in through the sign-in screen, as a person would with the printed test account. */
fun AppCompose.signIn() {
    tap(TestIds.SIGNIN_USE_TEST_ACCOUNT)
    tap(TestIds.SIGNIN_SUBMIT)
}

fun AppCompose.openLab(bench: String) {
    tap(TestIds.NAV_LAB)
    tap(bench)
}

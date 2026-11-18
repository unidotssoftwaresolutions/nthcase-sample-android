package com.nthcase.sample

import android.graphics.Rect
import android.os.Looper
import android.os.SystemClock
import android.view.InputDevice
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.accessibility.AccessibilityNodeInfo
import android.view.accessibility.AccessibilityNodeProvider
import org.robolectric.Shadows.shadowOf
import java.time.Duration

/** One node of the tree UiAutomator2 dumps: the fields Appium's page source shows for it, in tree order. */
data class A11yNode(
    val resourceId: String?,
    val className: String?,
    val scrollable: Boolean,
    val text: String?,
    val desc: String?,
    val visible: Boolean,
    val clickable: Boolean,
    val longClickable: Boolean,
    /** In screen coordinates, as `getBoundsInScreen`; UiScrollable swipes inside these. */
    val bounds: Rect,
    val depth: Int,
) {
    override fun toString() = "${"  ".repeat(depth)}${className?.substringAfterLast('.')} id=$resourceId scrollable=$scrollable visible=$visible text=${text?.take(30)} desc=${desc?.take(30)} $bounds"
}

/**
 * The accessibility tree of a window, built from the real AccessibilityNodeInfos the views and Compose's node provider
 * make, walked depth-first in child order: the order in which UiAutomator's `new UiSelector().scrollable(true)` meets
 * nodes, so the first scrollable here is the one `UiScrollable` would take. Compose's virtual children are followed
 * through the provider; a View inside Compose (a Fragment host) is followed where Compose lists it.
 */
object A11yTree {
    /** `AccessibilityNodeInfo.getChildId(int)`, hidden in the SDK but present in the framework Robolectric runs. */
    private val childId = AccessibilityNodeInfo::class.java.getMethod("getChildId", Int::class.javaPrimitiveType)
    private val viewAccessibilityId = View::class.java.getMethod("getAccessibilityViewId")
    private const val UNDEFINED_ITEM_ID = Int.MAX_VALUE

    fun of(root: View): List<A11yNode> = buildList { walkView(root, this, 0) }

    /** What UiAutomator2 lists by default: only nodes visible to the user. */
    fun visible(root: View): List<A11yNode> = of(root).filter { it.visible }

    private fun node(info: AccessibilityNodeInfo, depth: Int, visible: Boolean = info.isVisibleToUser, resourceId: String? = info.viewIdResourceName) = A11yNode(
        resourceId = resourceId,
        className = info.className?.toString(),
        scrollable = info.isScrollable,
        text = info.text?.toString(),
        desc = info.contentDescription?.toString(),
        visible = visible,
        clickable = info.isClickable,
        longClickable = info.isLongClickable,
        bounds = Rect().also(info::getBoundsInScreen),
        depth = depth,
    )

    private fun walkView(v: View, out: MutableList<A11yNode>, depth: Int) {
        if (v.visibility != View.VISIBLE) return
        val provider = v.accessibilityNodeProvider
        if (provider != null) {
            walkVirtual(v, provider, AccessibilityNodeProvider.HOST_VIEW_ID, out, depth)
            return
        }
        // View.isVisibleToUser also asks that the window be visible, and Robolectric leaves every window at GONE; on a phone
        // it is VISIBLE. So for a View the rest of that rule is applied here: shown, with a visible rect on screen.
        val shown = v.isShown && v.getGlobalVisibleRect(Rect())
        // A View writes its id into the node only when the reading service asks for view ids, as UiAutomator2 does
        // (FLAG_REPORT_VIEW_IDS); then it writes Resources.getResourceName(id) for an id that is not generated.
        // Robolectric's window asks for nothing, so the same name is read here.
        val info = v.createAccessibilityNodeInfo()
        val name = info.viewIdResourceName ?: v.id.takeIf { it != View.NO_ID && (it and 0xFF000000.toInt()) != 0 }
            ?.let { runCatching { v.resources.getResourceName(it) }.getOrNull() }
        out += node(info, depth, visible = shown, resourceId = name)
        if (v is ViewGroup) for (i in 0 until v.childCount) walkView(v.getChildAt(i), out, depth + 1)
    }

    private fun walkVirtual(host: View, provider: AccessibilityNodeProvider, virtualId: Int, out: MutableList<A11yNode>, depth: Int) {
        val info = provider.createAccessibilityNodeInfo(virtualId) ?: return
        out += node(info, depth)
        for (i in 0 until info.childCount) {
            val id = childId.invoke(info, i) as Long
            val virtual = (id shr 32).toInt()
            if (virtual == UNDEFINED_ITEM_ID || virtual == AccessibilityNodeProvider.HOST_VIEW_ID) {
                // a real View listed by the provider (Compose lists a hosted View, a Fragment's root, this way)
                val viewId = id.toInt()
                findByAccessibilityId(host, viewId)?.takeIf { it !== host }?.let { walkView(it, out, depth + 1) }
            } else {
                walkVirtual(host, provider, virtual, out, depth + 1)
            }
        }
    }

    private fun findByAccessibilityId(v: View, id: Int): View? {
        if (viewAccessibilityId.invoke(v) as Int == id) return v
        if (v is ViewGroup) for (i in 0 until v.childCount) findByAccessibilityId(v.getChildAt(i), id)?.let { return it }
        return null
    }
}

/**
 * A finger on a window: MotionEvents dispatched to its root view, as the input system delivers them, with the main
 * looper's clock moved between them so that the views' own timeouts (tap, long press) run as they would.
 */
class Finger(private val root: View) {
    private var downTime = 0L
    private val origin = IntArray(2).also(root::getLocationOnScreen)

    fun down(x: Int, y: Int) {
        downTime = SystemClock.uptimeMillis()
        send(MotionEvent.ACTION_DOWN, x, y)
    }

    fun move(x: Int, y: Int) = send(MotionEvent.ACTION_MOVE, x, y)
    fun up(x: Int, y: Int) = send(MotionEvent.ACTION_UP, x, y)

    /** Screen coordinates in, window coordinates to the root, as the window's ViewRootImpl hands them over. */
    private fun send(action: Int, x: Int, y: Int) {
        val ev = MotionEvent.obtain(downTime, SystemClock.uptimeMillis(), action, (x - origin[0]).toFloat(), (y - origin[1]).toFloat(), 0)
        ev.source = InputDevice.SOURCE_TOUCHSCREEN
        root.dispatchTouchEvent(ev)
        ev.recycle()
    }

    fun tap(x: Int, y: Int) {
        down(x, y); idle(60); up(x, y)
    }

    fun longPress(x: Int, y: Int) {
        down(x, y); idle(800); up(x, y)
    }

    fun doubleTap(x: Int, y: Int) {
        tap(x, y); idle(90); tap(x, y)
    }

    companion object {
        fun idle(ms: Long) = shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(ms))
    }
}

/**
 * The gesture `new UiScrollable(new UiSelector().scrollable(true)).scrollIntoView(…)` makes on a vertical list, read
 * from the bytecode of the UiAutomator2 server Appium installs (appium-uiautomator2-server v10.2.1, androidx.test.uiautomator):
 * - `UiScrollable`: `mIsVerticalList = true` unless told otherwise; `DEFAULT_SWIPE_DEADZONE_PCT = 0.1`;
 *   `SCROLL_STEPS = 55`; `mMaxSearchSwipes = 30`.
 * - `scrollBackward`: from (centreX, top + 10 % of the height) to (centreX, bottom − 10 %): the finger moves down.
 *   `scrollForward` is the reverse. Both inside the node's visible bounds.
 * - `InteractionController.swipe`: one down, then for each step 1 until 55 a move followed by `SystemClock.sleep(5)`,
 *   then one up. No hold before or after (that is only for a drag).
 * - `scrollIntoView`: if the target is not there, `scrollToBeginning` swipes backward until a swipe scrolls nothing,
 *   then it swipes forward until a swipe scrolls nothing. On a scroller that never scrolls vertically that is two
 *   swipes: one backward, one forward.
 *
 * The 5 ms is the floor between moves. On a phone each injected move also waits for the app to take it: run 00ec77f5's
 * two touches lasted about one second each (Samsung, Android 16), about 19 ms a step.
 */
object UiScrollableSwipe {
    const val STEPS = 55
    const val DEAD_ZONE = 0.1
    const val NOMINAL_STEP_MS = 5L
    const val MEASURED_STEP_MS = 19L

    /** The points of one vertical swipe inside [bounds]: [forward] moves the finger up (content down the list). */
    fun points(bounds: Rect, forward: Boolean): List<Pair<Int, Int>> {
        val adjust = (bounds.height() * DEAD_ZONE).toInt()
        val x = bounds.centerX()
        val (fromY, toY) = if (forward) (bounds.bottom - adjust) to (bounds.top + adjust) else (bounds.top + adjust) to (bounds.bottom - adjust)
        val step = (toY - fromY).toDouble() / STEPS
        return buildList {
            add(x to fromY)
            for (i in 1 until STEPS) add(x to fromY + (step * i).toInt())
            add(x to toY)
        }
    }
}

package com.nthcase.sample.lobby

import android.content.Context
import android.os.Bundle
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.view.accessibility.AccessibilityNodeInfo.AccessibilityAction
import android.widget.FrameLayout
import android.widget.OverScroller
import kotlin.math.abs

/**
 * The Lobby's board in Android Views: a fixed window onto one larger child that scrolls both ways. Android has no
 * two-axis scroller of its own (ScrollView is vertical, HorizontalScrollView horizontal), so this is a FrameLayout that
 * scrolls itself with scrollTo and an OverScroller, the way those two do on one axis each.
 *
 * - A drag that starts on the board belongs to the board: it asks its parents not to take it, so the page under it
 *   does not scroll while the board is panned.
 * - Its children keep their own clicks: the board takes the gesture only once a finger has moved past the touch slop,
 *   which sends them a cancel, as every platform scroller does.
 * - It tells accessibility that it scrolls, and in which directions, so UiAutomator2 lists it as scrollable.
 */
class BoardView(context: Context) : FrameLayout(context) {
    private val slop = ViewConfiguration.get(context).scaledTouchSlop
    private val minFling = ViewConfiguration.get(context).scaledMinimumFlingVelocity
    private val maxFling = ViewConfiguration.get(context).scaledMaximumFlingVelocity
    private val scroller = OverScroller(context)
    private var tracker: VelocityTracker? = null
    private var downX = 0f
    private var downY = 0f
    private var lastX = 0f
    private var lastY = 0f
    private var dragging = false

    /** Called after every change of the scroll position, with the new scrollX and scrollY. */
    var onScrolled: ((Int, Int) -> Unit)? = null

    init {
        isFocusable = false
        clipToPadding = true
        isScrollContainer = true
    }

    private val content: View? get() = if (childCount > 0) getChildAt(0) else null
    private val maxX: Int get() = ((content?.width ?: 0) - width).coerceAtLeast(0)
    private val maxY: Int get() = ((content?.height ?: 0) - height).coerceAtLeast(0)

    override fun measureChild(child: View, parentWidthMeasureSpec: Int, parentHeightMeasureSpec: Int) {
        val unspecified = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
        child.measure(unspecified, unspecified)
    }

    override fun measureChildWithMargins(child: View, parentWidthMeasureSpec: Int, widthUsed: Int, parentHeightMeasureSpec: Int, heightUsed: Int) {
        val lp = child.layoutParams as MarginLayoutParams
        child.measure(
            MeasureSpec.makeMeasureSpec(lp.leftMargin + lp.rightMargin, MeasureSpec.UNSPECIFIED),
            MeasureSpec.makeMeasureSpec(lp.topMargin + lp.bottomMargin, MeasureSpec.UNSPECIFIED),
        )
    }

    override fun computeHorizontalScrollRange(): Int = content?.width ?: width
    override fun computeVerticalScrollRange(): Int = content?.height ?: height
    override fun computeHorizontalScrollExtent(): Int = width
    override fun computeVerticalScrollExtent(): Int = height

    override fun scrollTo(x: Int, y: Int) {
        super.scrollTo(x.coerceIn(0, maxX), y.coerceIn(0, maxY))
    }

    override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
        super.onScrollChanged(l, t, oldl, oldt)
        onScrolled?.invoke(l, t)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        when (ev.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                downX = ev.x; downY = ev.y; lastX = ev.x; lastY = ev.y
                dragging = !scroller.isFinished
                scroller.forceFinished(true)
                parent?.requestDisallowInterceptTouchEvent(true)
            }
            MotionEvent.ACTION_MOVE -> {
                if (!dragging && (abs(ev.x - downX) > slop || abs(ev.y - downY) > slop)) {
                    dragging = true
                    lastX = ev.x; lastY = ev.y
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> dragging = false
        }
        return dragging
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        val t = tracker ?: VelocityTracker.obtain().also { tracker = it }
        t.addMovement(ev)
        when (ev.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                downX = ev.x; downY = ev.y; lastX = ev.x; lastY = ev.y
                parent?.requestDisallowInterceptTouchEvent(true)
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                if (!dragging && (abs(ev.x - downX) > slop || abs(ev.y - downY) > slop)) dragging = true
                if (dragging) {
                    scrollBy((lastX - ev.x).toInt(), (lastY - ev.y).toInt())
                    lastX = ev.x; lastY = ev.y
                }
            }
            MotionEvent.ACTION_UP -> {
                if (dragging) {
                    t.computeCurrentVelocity(1000, maxFling.toFloat())
                    val vx = -t.xVelocity.toInt()
                    val vy = -t.yVelocity.toInt()
                    if (abs(vx) > minFling || abs(vy) > minFling) {
                        scroller.fling(scrollX, scrollY, vx, vy, 0, maxX, 0, maxY)
                        postInvalidateOnAnimation()
                    }
                }
                endDrag()
            }
            MotionEvent.ACTION_CANCEL -> endDrag()
        }
        return true
    }

    private fun endDrag() {
        dragging = false
        tracker?.recycle()
        tracker = null
    }

    override fun computeScroll() {
        if (scroller.computeScrollOffset()) {
            scrollTo(scroller.currX, scroller.currY)
            postInvalidateOnAnimation()
        }
    }

    /** Scrolls by one window in a direction; what the accessibility scroll actions do. */
    fun page(dx: Int, dy: Int): Boolean {
        val before = scrollX to scrollY
        scrollTo(scrollX + dx * width, scrollY + dy * height)
        return before != (scrollX to scrollY)
    }

    override fun getAccessibilityClassName(): CharSequence = "android.widget.ScrollView"

    override fun onInitializeAccessibilityNodeInfo(info: AccessibilityNodeInfo) {
        super.onInitializeAccessibilityNodeInfo(info)
        val canX = maxX > 0
        val canY = maxY > 0
        info.isScrollable = canX || canY
        if (scrollX > 0 || scrollY > 0) info.addAction(AccessibilityAction.ACTION_SCROLL_BACKWARD)
        if (scrollX < maxX || scrollY < maxY) info.addAction(AccessibilityAction.ACTION_SCROLL_FORWARD)
        if (scrollY > 0) info.addAction(AccessibilityAction.ACTION_SCROLL_UP)
        if (scrollY < maxY) info.addAction(AccessibilityAction.ACTION_SCROLL_DOWN)
        if (scrollX > 0) info.addAction(AccessibilityAction.ACTION_SCROLL_LEFT)
        if (scrollX < maxX) info.addAction(AccessibilityAction.ACTION_SCROLL_RIGHT)
    }

    override fun onInitializeAccessibilityEvent(event: AccessibilityEvent) {
        super.onInitializeAccessibilityEvent(event)
        event.isScrollable = maxX > 0 || maxY > 0
        event.scrollX = scrollX
        event.scrollY = scrollY
        event.maxScrollX = maxX
        event.maxScrollY = maxY
    }

    override fun performAccessibilityAction(action: Int, arguments: Bundle?): Boolean = when (action) {
        AccessibilityNodeInfo.ACTION_SCROLL_FORWARD -> page(0, 1) || page(1, 0)
        AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD -> page(0, -1) || page(-1, 0)
        AccessibilityAction.ACTION_SCROLL_DOWN.id -> page(0, 1)
        AccessibilityAction.ACTION_SCROLL_UP.id -> page(0, -1)
        AccessibilityAction.ACTION_SCROLL_RIGHT.id -> page(1, 0)
        AccessibilityAction.ACTION_SCROLL_LEFT.id -> page(-1, 0)
        else -> super.performAccessibilityAction(action, arguments)
    }

    override fun generateDefaultLayoutParams(): LayoutParams = LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
}

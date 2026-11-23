package com.nthcase.sample.data

/**
 * The Feed (SPEC.md §5, new in spec 1.2): a list that never ends. It opens with 20 rows. When the third row from the end
 * of what has come shows, a spinner row comes after the last row; 1 s later it goes and 20 more rows come. The screen
 * only draws this.
 */
object Feed {
    /** Rows at the start, and rows each load brings. */
    const val PAGE = 20

    /** How long the spinner row shows before the next rows come. */
    const val LOAD_MS = 1_000L

    const val TITLE = "Feed"
    const val LAB_LINE = "A list that never ends, 20 rows at a time"
    const val LOADING = "Loading more rows…"

    /** "Feed row 7". */
    fun row(n: Int): String = "Feed row $n"

    /** The third row from the end of [count] rows, the one whose showing starts a load. */
    fun trigger(count: Int): Int = count - 2

    /** Whether a load starts: rows up to [lastShown] show (from 1), [count] have come, and none runs. */
    fun startsLoad(lastShown: Int, count: Int, loading: Boolean): Boolean = !loading && lastShown >= trigger(count)

    /**
     * The last row (from 1) any part of which is inside the list's view, from the items its layout placed: each as its
     * index (from 0), its offset from the view's start and its size. Rows are the first [count] items; the spinner row
     * after them is not a row. 0 when none shows.
     */
    fun lastShown(items: List<Triple<Int, Int, Int>>, viewStart: Int, viewEnd: Int, count: Int): Int =
        items.filter { (index, offset, size) -> index < count && offset < viewEnd && offset + size > viewStart }
            .maxOfOrNull { it.first + 1 } ?: 0
}

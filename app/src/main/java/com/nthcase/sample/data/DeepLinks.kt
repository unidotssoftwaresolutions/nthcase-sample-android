package com.nthcase.sample.data

import android.net.Uri

/** `nthcase-sample://item/{id}`: the one deep link every Mossgrove app answers (SPEC.md §Deep link). */
object DeepLinks {
    const val SCHEME = "nthcase-sample"
    const val ITEM_HOST = "item"

    /** The item id as written in the link (it may be unknown or not a number; the detail screen says so), or null. */
    fun itemId(uri: Uri?): String? {
        if (uri == null || uri.scheme != SCHEME || uri.host != ITEM_HOST) return null
        return uri.pathSegments.singleOrNull()?.takeIf { it.isNotBlank() }
    }

    fun forItem(id: Int): String = "$SCHEME://$ITEM_HOST/$id"
}

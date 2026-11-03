package com.nthcase.sample.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp
import com.nthcase.sample.data.Glyph

// Generated from art/icons/*.svg and art/glyphs/*.svg, the shared drawings of every Mossgrove app.
// Each is one stroked path; the tint comes from the theme at the call site.

private fun stroked(name: String, size: Float, stroke: Float, path: String): ImageVector =
    ImageVector.Builder(name, size.dp, size.dp, size, size).addPath(
        pathData = addPathNodes(path),
        stroke = SolidColor(Color.Black),
        strokeLineWidth = stroke,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
    ).build()

/** Interface icons, 24 units. */
object Icons {
    val Search: ImageVector by lazy { stroked("search", 24f, 2f, "M10.5 4a6.5 6.5 0 1 1 0 13a6.5 6.5 0 1 1 0-13z M15.5 15.5L20 20") }
    val Close: ImageVector by lazy { stroked("close", 24f, 2f, "M6 6l12 12 M18 6L6 18") }
    val Back: ImageVector by lazy { stroked("back", 24f, 2f, "M20 12H5 M11 5l-7 7 7 7") }
    val Plus: ImageVector by lazy { stroked("plus", 24f, 2f, "M12 5v14 M5 12h14") }
    val Minus: ImageVector by lazy { stroked("minus", 24f, 2f, "M5 12h14") }
    val Cart: ImageVector by lazy { stroked("cart", 24f, 2f, "M3 5h2.5l2 10h10.5l2-7H7 M9.5 19.5h0.01 M16.5 19.5h0.01") }
    val Shop: ImageVector by lazy { stroked("shop", 24f, 2f, "M4 4h16v16H4z M4 12h16 M10 8h4 M10 16h4") }
    val Lab: ImageVector by lazy { stroked("lab", 24f, 2f, "M9 3h6 M10 3v6l-5 9a2 2 0 0 0 2 3h10a2 2 0 0 0 2-3l-5-9V3 M7.5 14h9") }
    val Settings: ImageVector by lazy { stroked("settings", 24f, 2f, "M4 7h9 M17 7h3 M15 4.5v5 M4 17h3 M11 17h9 M9 14.5v5") }
    val Eye: ImageVector by lazy { stroked("eye", 24f, 2f, "M2 12s3.5-7 10-7 10 7 10 7-3.5 7-10 7S2 12 2 12z M12 9a3 3 0 1 1 0 6a3 3 0 1 1 0-6z") }
    val EyeOff: ImageVector by lazy { stroked("eye_off", 24f, 2f, "M2 12s3.5-7 10-7 10 7 10 7-3.5 7-10 7S2 12 2 12z M12 9a3 3 0 1 1 0 6a3 3 0 1 1 0-6z M4 4l16 16") }
    val Trash: ImageVector by lazy { stroked("trash", 24f, 2f, "M4 7h16 M9 7V4h6v3 M6 7l1 13h10l1-13") }
    val Chevron: ImageVector by lazy { stroked("chevron", 24f, 2f, "M9 6l6 6-6 6") }
    val Calendar: ImageVector by lazy { stroked("calendar", 24f, 2f, "M4 6h16v14H4z M4 10h16 M8 3v5 M16 3v5") }
    val Warning: ImageVector by lazy { stroked("warning", 24f, 2f, "M12 3l10 18H2z M12 10v5 M12 18v0.01") }
    val Empty: ImageVector by lazy { stroked("empty", 24f, 2f, "M3 13l3-8h12l3 8v6H3z M3 13h5l1 3h6l1-3h5") }
    val Check: ImageVector by lazy { stroked("check", 24f, 2f, "M5 12l5 5 9-10") }
}

/** Product drawings, 48 units. */
object Art {
    private val mug: ImageVector by lazy { stroked("mug", 48f, 2.5f, "M10 16h22v18a6 6 0 0 1-6 6H16a6 6 0 0 1-6-6z M32 20h3a5 5 0 0 1 0 10h-3 M17 7v5 M24 7v5") }
    private val notebook: ImageVector by lazy { stroked("notebook", 48f, 2.5f, "M12 6h22a2 2 0 0 1 2 2v32a2 2 0 0 1-2 2H12z M17 6v36 M22 14h9 M22 20h9 M22 26h9") }
    private val plant: ImageVector by lazy { stroked("plant", 48f, 2.5f, "M15 30h18l-3 12H18z M24 30V17 M24 23c-6 0-9-4-9-9 5 0 9 3 9 9z M24 19c0-6 4-10 10-10 0 6-4 10-10 10z") }
    private val candle: ImageVector by lazy { stroked("candle", 48f, 2.5f, "M17 20h14v22H17z M24 6c3 4 4 6 4 8a4 4 0 0 1-8 0c0-2 1-4 4-8z M24 18v2") }
    private val lamp: ImageVector by lazy { stroked("lamp", 48f, 2.5f, "M14 20l5-13h10l5 13z M24 20v16 M16 42h16l-2-6H18z") }
    private val bottle: ImageVector by lazy { stroked("bottle", 48f, 2.5f, "M20 4h8v6l3 4v26a4 4 0 0 1-4 4h-6a4 4 0 0 1-4-4V14l3-4z M17 22h14") }
    private val clock: ImageVector by lazy { stroked("clock", 48f, 2.5f, "M42 24a18 18 0 1 1-36 0a18 18 0 1 1 36 0z M24 13v11l7 5") }
    private val tote: ImageVector by lazy { stroked("tote", 48f, 2.5f, "M10 18h28l-3 24H13z M18 18v-4a6 6 0 0 1 12 0v4") }
    private val pen: ImageVector by lazy { stroked("pen", 48f, 2.5f, "M30 6l12 12-22 22H8V28z M26 10l12 12 M8 40l6-6") }
    private val bowl: ImageVector by lazy { stroked("bowl", 48f, 2.5f, "M6 22h36a18 16 0 0 1-36 0z M18 41h12") }

    fun of(glyph: Glyph): ImageVector = when (glyph) {
        Glyph.Mug -> mug
        Glyph.Notebook -> notebook
        Glyph.Plant -> plant
        Glyph.Candle -> candle
        Glyph.Lamp -> lamp
        Glyph.Bottle -> bottle
        Glyph.Clock -> clock
        Glyph.Tote -> tote
        Glyph.Pen -> pen
        Glyph.Bowl -> bowl
    }
}

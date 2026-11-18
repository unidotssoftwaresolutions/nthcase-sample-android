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
    val Search: ImageVector by lazy { stroked("search", 24f, 2f, ArtPaths.SEARCH) }
    val Close: ImageVector by lazy { stroked("close", 24f, 2f, ArtPaths.CLOSE) }
    val Back: ImageVector by lazy { stroked("back", 24f, 2f, ArtPaths.BACK) }
    val Plus: ImageVector by lazy { stroked("plus", 24f, 2f, ArtPaths.PLUS) }
    val Minus: ImageVector by lazy { stroked("minus", 24f, 2f, ArtPaths.MINUS) }
    val Cart: ImageVector by lazy { stroked("cart", 24f, 2f, ArtPaths.CART) }
    val Shop: ImageVector by lazy { stroked("shop", 24f, 2f, ArtPaths.SHOP) }
    val Lab: ImageVector by lazy { stroked("lab", 24f, 2f, ArtPaths.LAB) }
    val Settings: ImageVector by lazy { stroked("settings", 24f, 2f, ArtPaths.SETTINGS) }
    val Eye: ImageVector by lazy { stroked("eye", 24f, 2f, ArtPaths.EYE) }
    val EyeOff: ImageVector by lazy { stroked("eye_off", 24f, 2f, ArtPaths.EYE_OFF) }
    val Trash: ImageVector by lazy { stroked("trash", 24f, 2f, ArtPaths.TRASH) }
    val Chevron: ImageVector by lazy { stroked("chevron", 24f, 2f, ArtPaths.CHEVRON) }
    val Calendar: ImageVector by lazy { stroked("calendar", 24f, 2f, ArtPaths.CALENDAR) }
    val Warning: ImageVector by lazy { stroked("warning", 24f, 2f, ArtPaths.WARNING) }
    val Empty: ImageVector by lazy { stroked("empty", 24f, 2f, ArtPaths.EMPTY) }
    val Check: ImageVector by lazy { stroked("check", 24f, 2f, ArtPaths.CHECK) }
}

/** Product drawings, 48 units. */
object Art {
    private val mug: ImageVector by lazy { stroked("mug", 48f, 2.5f, ArtPaths.MUG) }
    private val notebook: ImageVector by lazy { stroked("notebook", 48f, 2.5f, ArtPaths.NOTEBOOK) }
    private val plant: ImageVector by lazy { stroked("plant", 48f, 2.5f, ArtPaths.PLANT) }
    private val candle: ImageVector by lazy { stroked("candle", 48f, 2.5f, ArtPaths.CANDLE) }
    private val lamp: ImageVector by lazy { stroked("lamp", 48f, 2.5f, ArtPaths.LAMP) }
    private val bottle: ImageVector by lazy { stroked("bottle", 48f, 2.5f, ArtPaths.BOTTLE) }
    private val clock: ImageVector by lazy { stroked("clock", 48f, 2.5f, ArtPaths.CLOCK) }
    private val tote: ImageVector by lazy { stroked("tote", 48f, 2.5f, ArtPaths.TOTE) }
    private val pen: ImageVector by lazy { stroked("pen", 48f, 2.5f, ArtPaths.PEN) }
    private val bowl: ImageVector by lazy { stroked("bowl", 48f, 2.5f, ArtPaths.BOWL) }

    /** The drawing's SVG path, for the Views build. */
    fun pathOf(glyph: Glyph): String = when (glyph) {
        Glyph.Mug -> ArtPaths.MUG
        Glyph.Notebook -> ArtPaths.NOTEBOOK
        Glyph.Plant -> ArtPaths.PLANT
        Glyph.Candle -> ArtPaths.CANDLE
        Glyph.Lamp -> ArtPaths.LAMP
        Glyph.Bottle -> ArtPaths.BOTTLE
        Glyph.Clock -> ArtPaths.CLOCK
        Glyph.Tote -> ArtPaths.TOTE
        Glyph.Pen -> ArtPaths.PEN
        Glyph.Bowl -> ArtPaths.BOWL
    }

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

/** The path of every shared drawing, as written in art/ (SVG path data). Compose draws them through [Icons] and [Art];
 * the Android Views Lobby draws the same strings with GlyphDrawable. */
object ArtPaths {
    const val SEARCH = "M10.5 4a6.5 6.5 0 1 1 0 13a6.5 6.5 0 1 1 0-13z M15.5 15.5L20 20"
    const val CLOSE = "M6 6l12 12 M18 6L6 18"
    const val BACK = "M20 12H5 M11 5l-7 7 7 7"
    const val PLUS = "M12 5v14 M5 12h14"
    const val MINUS = "M5 12h14"
    const val CART = "M3 5h2.5l2 10h10.5l2-7H7 M9.5 19.5h0.01 M16.5 19.5h0.01"
    const val SHOP = "M4 4h16v16H4z M4 12h16 M10 8h4 M10 16h4"
    const val LAB = "M9 3h6 M10 3v6l-5 9a2 2 0 0 0 2 3h10a2 2 0 0 0 2-3l-5-9V3 M7.5 14h9"
    const val SETTINGS = "M4 7h9 M17 7h3 M15 4.5v5 M4 17h3 M11 17h9 M9 14.5v5"
    const val EYE = "M2 12s3.5-7 10-7 10 7 10 7-3.5 7-10 7S2 12 2 12z M12 9a3 3 0 1 1 0 6a3 3 0 1 1 0-6z"
    const val EYE_OFF = "M2 12s3.5-7 10-7 10 7 10 7-3.5 7-10 7S2 12 2 12z M12 9a3 3 0 1 1 0 6a3 3 0 1 1 0-6z M4 4l16 16"
    const val TRASH = "M4 7h16 M9 7V4h6v3 M6 7l1 13h10l1-13"
    const val CHEVRON = "M9 6l6 6-6 6"
    const val CALENDAR = "M4 6h16v14H4z M4 10h16 M8 3v5 M16 3v5"
    const val WARNING = "M12 3l10 18H2z M12 10v5 M12 18v0.01"
    const val EMPTY = "M3 13l3-8h12l3 8v6H3z M3 13h5l1 3h6l1-3h5"
    const val CHECK = "M5 12l5 5 9-10"
    const val MUG = "M10 16h22v18a6 6 0 0 1-6 6H16a6 6 0 0 1-6-6z M32 20h3a5 5 0 0 1 0 10h-3 M17 7v5 M24 7v5"
    const val NOTEBOOK = "M12 6h22a2 2 0 0 1 2 2v32a2 2 0 0 1-2 2H12z M17 6v36 M22 14h9 M22 20h9 M22 26h9"
    const val PLANT = "M15 30h18l-3 12H18z M24 30V17 M24 23c-6 0-9-4-9-9 5 0 9 3 9 9z M24 19c0-6 4-10 10-10 0 6-4 10-10 10z"
    const val CANDLE = "M17 20h14v22H17z M24 6c3 4 4 6 4 8a4 4 0 0 1-8 0c0-2 1-4 4-8z M24 18v2"
    const val LAMP = "M14 20l5-13h10l5 13z M24 20v16 M16 42h16l-2-6H18z"
    const val BOTTLE = "M20 4h8v6l3 4v26a4 4 0 0 1-4 4h-6a4 4 0 0 1-4-4V14l3-4z M17 22h14"
    const val CLOCK = "M42 24a18 18 0 1 1-36 0a18 18 0 1 1 36 0z M24 13v11l7 5"
    const val TOTE = "M10 18h28l-3 24H13z M18 18v-4a6 6 0 0 1 12 0v4"
    const val PEN = "M30 6l12 12-22 22H8V28z M26 10l12 12 M8 40l6-6"
    const val BOWL = "M6 22h36a18 16 0 0 1-36 0z M18 41h12"
}

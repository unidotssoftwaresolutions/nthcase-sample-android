package com.nthcase.sample.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nthcase.sample.data.Category

// The Mossgrove palette (SPEC.md §Design): cabinet-enamel green, cool card-stock neutrals, one brass accent kept for
// item numbers, Add to cart and the cart count, never for decoration. The same hex values are in res/values*/colors.xml for the XML settings screen.
private val Light = lightColorScheme(
    primary = Color(0xFF2E5E55), onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFCBE7DF), onPrimaryContainer = Color(0xFF0B2B25),
    secondary = Color(0xFF4F615C), onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFD2E5DF), onSecondaryContainer = Color(0xFF0C1F1B),
    tertiary = Color(0xFF7A5A1C), onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFCDFA6), onTertiaryContainer = Color(0xFF271900),
    error = Color(0xFFBA1A1A), onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6), onErrorContainer = Color(0xFF410002),
    background = Color(0xFFF7F9F7), onBackground = Color(0xFF181C1B),
    surface = Color(0xFFF7F9F7), onSurface = Color(0xFF181C1B),
    surfaceVariant = Color(0xFFDBE5E1), onSurfaceVariant = Color(0xFF3F4946),
    outline = Color(0xFF6F7976), outlineVariant = Color(0xFFBFC9C5),
    surfaceContainerLowest = Color(0xFFFFFFFF), surfaceContainerLow = Color(0xFFF1F4F2),
    surfaceContainer = Color(0xFFEBEFEC), surfaceContainerHigh = Color(0xFFE5E9E6),
    surfaceContainerHighest = Color(0xFFE0E3E1),
    inverseSurface = Color(0xFF2D3130), inverseOnSurface = Color(0xFFEFF1EF), inversePrimary = Color(0xFF95D1C2),
)

private val Dark = darkColorScheme(
    primary = Color(0xFF95D1C2), onPrimary = Color(0xFF003730),
    primaryContainer = Color(0xFF15493F), onPrimaryContainer = Color(0xFFB0EEDD),
    secondary = Color(0xFFB6CBC4), onSecondary = Color(0xFF21352F),
    secondaryContainer = Color(0xFF374B45), onSecondaryContainer = Color(0xFFD2E7E0),
    tertiary = Color(0xFFEEC06E), onTertiary = Color(0xFF412D00),
    tertiaryContainer = Color(0xFF5D4200), onTertiaryContainer = Color(0xFFFFDEA5),
    error = Color(0xFFFFB4AB), onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A), onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF101413), onBackground = Color(0xFFE0E3E1),
    surface = Color(0xFF101413), onSurface = Color(0xFFE0E3E1),
    surfaceVariant = Color(0xFF3F4946), onSurfaceVariant = Color(0xFFBFC9C5),
    outline = Color(0xFF899390), outlineVariant = Color(0xFF3F4946),
    surfaceContainerLowest = Color(0xFF0B0F0E), surfaceContainerLow = Color(0xFF181C1B),
    surfaceContainer = Color(0xFF1C2120), surfaceContainerHigh = Color(0xFF272B2A),
    surfaceContainerHighest = Color(0xFF313635),
    inverseSurface = Color(0xFFE0E3E1), inverseOnSurface = Color(0xFF2D3130), inversePrimary = Color(0xFF2E5E55),
)

/** One tint pair per aisle: the tile behind an item's glyph, and the glyph's ink. */
@Immutable
data class AisleTint(val tile: Color, val ink: Color)

@Immutable
data class AisleTints(val kitchen: AisleTint, val desk: AisleTint, val garden: AisleTint, val home: AisleTint) {
    fun of(category: Category): AisleTint = when (category) {
        Category.Kitchen -> kitchen
        Category.Desk -> desk
        Category.Garden -> garden
        Category.Home -> home
    }
}

private val LightAisles = AisleTints(
    kitchen = AisleTint(Color(0xFFDDEBE5), Color(0xFF2E5E55)),
    desk = AisleTint(Color(0xFFE2E7EE), Color(0xFF3F5466)),
    garden = AisleTint(Color(0xFFDCEBD3), Color(0xFF3E6A2E)),
    home = AisleTint(Color(0xFFE8E4DE), Color(0xFF5B5148)),
)

private val DarkAisles = AisleTints(
    kitchen = AisleTint(Color(0xFF23332E), Color(0xFF95D1C2)),
    desk = AisleTint(Color(0xFF222A33), Color(0xFFB4C5D8)),
    garden = AisleTint(Color(0xFF263322), Color(0xFFA8D493)),
    home = AisleTint(Color(0xFF302D29), Color(0xFFD3C7BA)),
)

val LocalAisleTints = staticCompositionLocalOf { LightAisles }

/** Item numbers ("No. 007") are set in the system monospace face, so the digits line up in a list. */
val ItemNumberStyle = TextStyle(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Medium, fontSize = 13.sp, letterSpacing = 0.5.sp)

private val MossgroveShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp), // label holders, chips
    small = RoundedCornerShape(8.dp), // glyph tiles, chips (Material text fields keep their own 4 dp)
    medium = RoundedCornerShape(12.dp), // drawer fronts (cards)
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(28.dp), // dialogs and sheets
)

@Composable
fun MossgroveTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    // Dynamic colour is off on purpose: the five Mossgrove apps share one palette, and screenshots must not depend on
    // the phone's wallpaper.
    androidx.compose.runtime.CompositionLocalProvider(LocalAisleTints provides if (darkTheme) DarkAisles else LightAisles) {
        MaterialTheme(
            colorScheme = if (darkTheme) Dark else Light,
            typography = Typography(),
            shapes = MossgroveShapes,
            content = content,
        )
    }
}

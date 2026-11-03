package com.nthcase.sample

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/** The app draws exactly the shared drawings: every path in art/icons and art/glyphs appears verbatim in ui/Art.kt. */
class ArtTest {
    @Test
    fun the_kotlin_art_is_the_shared_svg_art() {
        val kotlin = File("src/main/java/com/nthcase/sample/ui/Art.kt").readText()
        val svgs = (File("../art/icons").listFiles()!! + File("../art/glyphs").listFiles()!!).filter { it.extension == "svg" }
        assertEquals(27, svgs.size)
        svgs.forEach { svg ->
            val path = Regex("<path d=\"([^\"]+)\"").find(svg.readText())!!.groupValues[1]
            assertTrue("${svg.name} is drawn by Art.kt", kotlin.contains("\"${svg.nameWithoutExtension}\"") && kotlin.contains("\"$path\""))
        }
    }
}

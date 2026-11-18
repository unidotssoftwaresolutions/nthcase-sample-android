package com.nthcase.sample

import com.nthcase.sample.data.Lobby
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.lang.reflect.Modifier

/**
 * The app's id list is the spec's id list: every id in nthcase/test-ids.json is in TestIds or in the bundled web page,
 * and nothing else is. A template like `shop_item_{id}` matches a TestIds function of one argument.
 */
class SpecIdsTest {
    private val specIds: Set<String> by lazy {
        val json = File("../nthcase/test-ids.json").readText()
        assertTrue("the spec copy says spec 1.1", json.contains("\"spec\": \"1.1\""))
        Regex("\"id\": \"([^\"]+)\"").findAll(json).map { it.groupValues[1] }.toSet()
    }

    /** Constants as written; functions as templates, by calling them with a marker and putting the placeholder back. */
    private val appIds: Set<String> by lazy {
        val constants = TestIds::class.java.declaredFields
            .filter { Modifier.isStatic(it.modifiers) && it.type == String::class.java }
            .map { it.get(null) as String }
        val templates = TestIds::class.java.declaredMethods
            .filter { Modifier.isPublic(it.modifiers) && it.parameterCount in 1..2 && it.returnType == String::class.java }
            .map { method ->
                when {
                    method.parameterCount == 2 && method.name == "lobbyBoardCell" -> (method.invoke(TestIds, 1, 987654) as String).replace("a987654", "{col}{row}")
                    method.parameterCount == 2 -> (method.invoke(TestIds, 987654, 876543) as String).replace("987654", "{n}").replace("876543", "{m}")
                    method.parameterTypes[0] == String::class.java -> method.invoke(TestIds, "{key}") as String
                    method.name == "lobbySavedDelay" -> (method.invoke(TestIds, 987654) as String).replace("987654", "{s}")
                    method.name == "lobbyTopRowTile" -> (method.invoke(TestIds, 987654) as String).replace("987654", "{m}")
                    else -> (method.invoke(TestIds, 987654) as String).replace(
                        "987654",
                        if (method.name.startsWith("slow") || method.name.startsWith("orders") || method.name.startsWith("lobby")) "{n}" else "{id}",
                    )
                }
            }
        (constants + templates).toSet()
    }

    private val webIds: Set<String> by lazy {
        val html = File("src/main/assets/web/care-guide.html").readText()
        val ids = Regex("\\sid=\"([^\"]+)\"").findAll(html).map { it.groupValues[1] }.toSet()
        val testIds = Regex("data-testid=\"([^\"]+)\"").findAll(html).map { it.groupValues[1] }.toSet()
        assertEquals("every web element's id and data-testid agree", testIds, ids)
        testIds
    }

    @Test
    fun every_spec_id_is_in_the_app_and_the_app_has_no_others() {
        assertEquals(specIds.filterNot { it.startsWith("web_") }.toSortedSet(), appIds.toSortedSet())
        assertEquals(specIds.filter { it.startsWith("web_") }.toSortedSet(), webIds.toSortedSet())
    }

    @Test
    fun every_id_has_the_shape_every_framework_accepts() {
        val shape = Regex("^[a-z][a-z0-9_]*$")
        specIds.forEach { id -> assertTrue(id, shape.matches(id.replace(Regex("\\{(id|n|key|m|s|col|row)\\}"), "x"))) }
    }

    /** Every native id is checked on its screen by ScreenIdsTest: each one is named there, as a constant or a template. */
    @Test
    fun every_native_spec_id_is_checked_on_its_screen() {
        val source = File("src/test/java/com/nthcase/sample/ScreenIdsTest.kt").readText()
        val byField = TestIds::class.java.declaredFields.filter { Modifier.isStatic(it.modifiers) && it.type == String::class.java }
            .associate { it.name to it.get(null) as String }
        val named = Regex("TestIds\\.([A-Z_]+)").findAll(source).mapNotNull { byField[it.groupValues[1]] }.toMutableSet()
        val templates = mapOf(
            "shopItem" to "shop_item_{id}", "cartLine" to "cart_line_{id}", "cartLineDecrease" to "cart_line_decrease_{id}",
            "cartLineQty" to "cart_line_qty_{id}", "cartLineIncrease" to "cart_line_increase_{id}", "cartLineRemove" to "cart_line_remove_{id}",
            "cartLineTotal" to "cart_line_total_{id}", "formCategoryOption" to "form_category_option_{key}", "slowSlot" to "slow_slot_{n}",
            "ordersRow" to "orders_row_{n}",
            "lobbySavedDelay" to "lobby_saved_delay_{s}", "lobbyBanner" to "lobby_banner_{n}", "lobbyChip" to "lobby_chip_{n}",
            "lobbyRow" to "lobby_row_{n}", "lobbyRowPosition" to "lobby_row_{n}_position", "lobbyRowTile" to "lobby_row_{n}_tile_{m}",
            "lobbyTopRowTile" to "lobby_top_row_tile_{m}",
            "lobbyGridCell" to "lobby_grid_cell_{n}", "lobbyInnerRow" to "lobby_inner_row_{n}", "lobbyBoardCell" to "lobby_board_cell_{col}{row}",
        )
        Regex("TestIds(?:::|\\.)([a-z][A-Za-z]+)").findAll(source).forEach { m -> templates[m.groupValues[1]]?.let(named::add) }
        if ("TestIds.lobbySheetButtons" in source) named += TestIds.lobbySheetButtons
        if ("TestIds.lobbyBarItems" in source) named += TestIds.lobbyBarItems
        assertEquals(specIds.filterNot { it.startsWith("web_") }.toSortedSet(), named.toSortedSet())
    }

    /** The Views Lobby's view ids are exactly the spec's Lobby ids, each template expanded (res/values/lobby_ids.xml). */
    @Test
    fun the_views_lobby_declares_every_lobby_id_as_an_android_id() {
        val declared = Regex("<item name=\"([a-z0-9_]+)\" type=\"id\" />").findAll(File("src/main/res/values/lobby_ids.xml").readText())
            .map { it.groupValues[1] }.toList()
        assertEquals("no id declared twice", declared.size, declared.toSet().size)
        val expanded = specIds.filter { it.startsWith("lobby_") }.flatMap { id ->
            when (id) {
                "lobby_saved_delay_{s}" -> Lobby.DELAYS.map { "lobby_saved_delay_$it" }
                "lobby_banner_{n}" -> (1..Lobby.BANNERS).map(TestIds::lobbyBanner)
                "lobby_chip_{n}" -> (1..Lobby.chips.size).map(TestIds::lobbyChip)
                "lobby_row_{n}" -> (1..Lobby.ROWS).map(TestIds::lobbyRow)
                "lobby_row_{n}_position" -> (1..Lobby.ROWS).map(TestIds::lobbyRowPosition)
                "lobby_row_{n}_tile_{m}" -> (1..Lobby.ROWS).flatMap { n -> (1..Lobby.TILES).map { m -> TestIds.lobbyRowTile(n, m) } }
                "lobby_top_row_tile_{m}" -> (1..Lobby.TOP_TILES).map(TestIds::lobbyTopRowTile)
                "lobby_grid_cell_{n}" -> (1..Lobby.GRID_CELLS).map(TestIds::lobbyGridCell)
                "lobby_inner_row_{n}" -> (1..Lobby.INNER_LINES).map(TestIds::lobbyInnerRow)
                "lobby_board_cell_{col}{row}" -> (1..Lobby.BOARD).flatMap { r -> (1..Lobby.BOARD).map { c -> TestIds.lobbyBoardCell(c, r) } }
                else -> listOf(id)
            }
        }
        assertEquals(expanded.toSortedSet(), declared.toSortedSet())
    }

    @Test
    fun the_xml_screen_declares_its_spec_ids_as_android_ids() {
        val xml = File("src/main/res/layout/fragment_settings.xml").readText() + File("src/main/res/layout/dialog_reset.xml").readText()
        val declared = Regex("android:id=\"@\\+id/([a-z0-9_]+)\"").findAll(xml).map { it.groupValues[1] }.toSet()
        assertEquals(specIds.filter { it.startsWith("settings_") }.toSortedSet(), declared.toSortedSet())
    }
}

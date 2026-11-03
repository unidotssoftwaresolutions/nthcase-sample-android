package com.nthcase.sample

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
        assertTrue("the spec copy says spec 1.0", json.contains("\"spec\": \"1.0\""))
        Regex("\"id\": \"([^\"]+)\"").findAll(json).map { it.groupValues[1] }.toSet()
    }

    /** Constants as written; functions as templates, by calling them with a marker and putting the placeholder back. */
    private val appIds: Set<String> by lazy {
        val constants = TestIds::class.java.declaredFields
            .filter { Modifier.isStatic(it.modifiers) && it.type == String::class.java }
            .map { it.get(null) as String }
        val templates = TestIds::class.java.declaredMethods
            .filter { Modifier.isPublic(it.modifiers) && it.parameterCount == 1 && it.returnType == String::class.java }
            .map { method ->
                if (method.parameterTypes[0] == String::class.java) method.invoke(TestIds, "{key}") as String
                else (method.invoke(TestIds, 987654) as String).replace("987654", if (method.name.startsWith("slow") || method.name.startsWith("orders")) "{n}" else "{id}")
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
        specIds.forEach { id -> assertTrue(id, shape.matches(id.replace(Regex("\\{(id|n|key)\\}"), "x"))) }
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
        )
        Regex("TestIds(?:::|\\.)([a-z][A-Za-z]+)").findAll(source).forEach { m -> templates[m.groupValues[1]]?.let(named::add) }
        assertEquals(specIds.filterNot { it.startsWith("web_") }.toSortedSet(), named.toSortedSet())
    }

    @Test
    fun the_xml_screen_declares_its_spec_ids_as_android_ids() {
        val xml = File("src/main/res/layout/fragment_settings.xml").readText() + File("src/main/res/layout/dialog_reset.xml").readText()
        val declared = Regex("android:id=\"@\\+id/([a-z0-9_]+)\"").findAll(xml).map { it.groupValues[1] }.toSet()
        assertEquals(specIds.filter { it.startsWith("settings_") }.toSortedSet(), declared.toSortedSet())
    }
}

package com.nthcase.sample.data

import java.util.Locale

/** The four aisles of the shop. The key is what the form's picker stores; the label is what it shows. */
enum class Category(val key: String, val label: String) {
    Kitchen("kitchen", "Kitchen"),
    Desk("desk", "Desk"),
    Garden("garden", "Garden"),
    Home("home", "Home"),
}

/** Which original line drawing an item wears. Every glyph is drawn for this app (see Art.kt and SPEC.md §Art). */
enum class Glyph { Mug, Notebook, Plant, Candle, Lamp, Bottle, Clock, Tote, Pen, Bowl }

data class Item(
    val id: Int,
    val name: String,
    val category: Category,
    val glyph: Glyph,
    val priceCents: Int,
    val description: String,
) {
    /** "No. 007": the item number every screen shows. */
    val number: String get() = itemNumber(id)
}

fun itemNumber(id: Int): String = String.format(Locale.US, "No. %03d", id)

/** Fixed US-dollar prices, so every run and every framework shows the same text. */
fun formatPrice(cents: Int): String = String.format(Locale.US, "$%d.%02d", cents / 100, cents % 100)

/**
 * The seeded catalogue: forty invented items, identical in every Mossgrove app (SPEC.md §Data). Nothing is fetched;
 * the order here is the order of the list.
 */
object Catalog {
    val items: List<Item> = listOf(
        Item(1, "Stoneware Mug", Category.Kitchen, Glyph.Mug, 1400, "A heavy, speckled mug that keeps tea warm through a long call."),
        Item(2, "Grid Notebook", Category.Desk, Glyph.Notebook, 950, "Ninety-six pages of 5 mm grid, stitched to lie flat."),
        Item(3, "Desk Fern", Category.Garden, Glyph.Plant, 1800, "A small fern in a glazed pot. Likes shade and a weekly drink."),
        Item(4, "Cedar Candle", Category.Home, Glyph.Candle, 1600, "Soy wax with a dry cedar scent. Burns for about forty hours."),
        Item(5, "Brass Desk Lamp", Category.Desk, Glyph.Lamp, 4200, "A warm, low lamp with a brass shade and a fabric cable."),
        Item(6, "Steel Water Bottle", Category.Kitchen, Glyph.Bottle, 2400, "Double-walled steel. Cold for a day, hot for half of one."),
        Item(7, "Wall Clock", Category.Home, Glyph.Clock, 3500, "A quiet sweep movement behind a plain white face."),
        Item(8, "Canvas Tote", Category.Home, Glyph.Tote, 2200, "Heavy canvas with long handles and one inside pocket."),
        Item(9, "Fineliner Pen Set", Category.Desk, Glyph.Pen, 1200, "Six fineliners in muted inks, 0.4 mm tips."),
        Item(10, "Serving Bowl", Category.Kitchen, Glyph.Bowl, 2800, "A wide stoneware bowl for salads, fruit or bread."),
        Item(11, "Espresso Cup Pair", Category.Kitchen, Glyph.Mug, 1900, "Two small cups with thick walls and saucers to match."),
        Item(12, "Dot Journal", Category.Desk, Glyph.Notebook, 1100, "Dotted pages, numbered, with two ribbon markers."),
        Item(13, "Succulent Trio", Category.Garden, Glyph.Plant, 2100, "Three hardy succulents that forgive a missed watering."),
        Item(14, "Fig Leaf Candle", Category.Home, Glyph.Candle, 1750, "Green and milky. Smells like a fig tree after rain."),
        Item(15, "Clip Reading Lamp", Category.Desk, Glyph.Lamp, 2900, "Clips to a shelf or a bed frame. Three brightness steps."),
        Item(16, "Glass Carafe", Category.Kitchen, Glyph.Bottle, 2600, "A litre of water on the table, with a cup for a lid."),
        Item(17, "Alarm Clock", Category.Home, Glyph.Clock, 2300, "A silent tick and a gentle rising alarm."),
        Item(18, "Market Bag", Category.Home, Glyph.Tote, 1500, "Folds into its own pocket. Holds a week of vegetables."),
        Item(19, "Fountain Pen", Category.Desk, Glyph.Pen, 3800, "A steel nib, a converter, and a box of blue cartridges."),
        Item(20, "Salad Bowl", Category.Kitchen, Glyph.Bowl, 3200, "Oiled wood, turned from one piece."),
        Item(21, "Travel Mug", Category.Kitchen, Glyph.Mug, 1800, "Leak-proof lid, fits a car's cup holder."),
        Item(22, "Sketchbook", Category.Desk, Glyph.Notebook, 1350, "Heavy blank paper that takes ink and light washes."),
        Item(23, "Herb Planter", Category.Garden, Glyph.Plant, 2700, "A long planter for a sunny sill. Seeds not included."),
        Item(24, "Beeswax Taper Set", Category.Home, Glyph.Candle, 1450, "Four hand-dipped tapers that burn clean and slow."),
        Item(25, "Floor Lamp", Category.Home, Glyph.Lamp, 8900, "A tall arc of steel with a linen shade."),
        Item(26, "Oil Bottle", Category.Kitchen, Glyph.Bottle, 1250, "A pourer that stops dripping when you stop pouring."),
        Item(27, "Desk Clock", Category.Desk, Glyph.Clock, 3100, "A small square clock with a date window."),
        Item(28, "Laptop Tote", Category.Desk, Glyph.Tote, 3600, "A padded sleeve with handles for a 14-inch laptop."),
        Item(29, "Brush Pen", Category.Desk, Glyph.Pen, 750, "A soft tip for lettering and quick sketches."),
        Item(30, "Noodle Bowl", Category.Kitchen, Glyph.Bowl, 1600, "Deep enough for broth, with a rest for chopsticks."),
        Item(31, "Tea Mug", Category.Kitchen, Glyph.Mug, 1500, "A tall mug with a steel infuser that sits inside."),
        Item(32, "Pocket Notebook", Category.Desk, Glyph.Notebook, 600, "Fits a back pocket. Ruled, sixty-four pages."),
        Item(33, "Hanging Planter", Category.Garden, Glyph.Plant, 2450, "A glazed pot on a cotton rope, for trailing plants."),
        Item(34, "Pine Candle", Category.Home, Glyph.Candle, 1600, "Resin and needles. Best on a cold evening."),
        Item(35, "Bedside Lamp", Category.Home, Glyph.Lamp, 4500, "A touch dimmer and a shade that does not glare."),
        Item(36, "Vacuum Flask", Category.Kitchen, Glyph.Bottle, 2950, "Keeps soup hot from breakfast until lunch."),
        Item(37, "Kitchen Timer", Category.Kitchen, Glyph.Clock, 1950, "A wind-up timer with a bell you can hear upstairs."),
        Item(38, "Beach Tote", Category.Home, Glyph.Tote, 2500, "Striped cotton that shakes clean of sand."),
        Item(39, "Pencil Set", Category.Desk, Glyph.Pen, 850, "Twelve graphite pencils, 4H to 6B."),
        Item(40, "Fruit Bowl", Category.Kitchen, Glyph.Bowl, 3400, "A wire bowl that lets fruit breathe."),
    )

    private val byId = items.associateBy { it.id }

    fun find(id: Int): Item? = byId[id]

    /**
     * Search by name (case-insensitive, anywhere in the name) or by number: "7", "007" and "No. 007" all find item 7.
     * A blank query matches everything.
     */
    fun search(query: String): List<Item> {
        val q = query.trim()
        if (q.isEmpty()) return items
        val digits = q.removePrefix("No.").removePrefix("no.").trim()
        val number = if (digits.isNotEmpty() && digits.all { it.isDigit() }) digits.toIntOrNull() else null
        return items.filter { it.name.contains(q, ignoreCase = true) || (number != null && it.id == number) }
    }
}

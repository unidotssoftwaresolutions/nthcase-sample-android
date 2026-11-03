package com.nthcase.sample.data

import androidx.compose.runtime.mutableStateListOf

data class CartLine(val itemId: Int, val quantity: Int) {
    val item: Item get() = Catalog.find(itemId)!!
    val totalCents: Int get() = item.priceCents * quantity
}

data class Order(val number: Int, val lines: List<CartLine>) {
    val itemCount: Int get() = lines.sumOf { it.quantity }
    val totalCents: Int get() = lines.sumOf { it.totalCents }
}

/** The cart: one line per item, in the order items were first added. Quantities stay within 1–9. */
class Cart {
    val lines = mutableStateListOf<CartLine>()

    val itemCount: Int get() = lines.sumOf { it.quantity }
    val totalCents: Int get() = lines.sumOf { it.totalCents }

    fun quantityOf(itemId: Int): Int = lines.firstOrNull { it.itemId == itemId }?.quantity ?: 0

    fun add(itemId: Int, quantity: Int) {
        require(Catalog.find(itemId) != null) { "No item $itemId" }
        val index = lines.indexOfFirst { it.itemId == itemId }
        if (index < 0) lines.add(CartLine(itemId, quantity.coerceIn(MIN_QUANTITY, MAX_QUANTITY)))
        else lines[index] = lines[index].copy(quantity = (lines[index].quantity + quantity).coerceIn(MIN_QUANTITY, MAX_QUANTITY))
    }

    fun setQuantity(itemId: Int, quantity: Int) {
        val index = lines.indexOfFirst { it.itemId == itemId }
        if (index >= 0) lines[index] = lines[index].copy(quantity = quantity.coerceIn(MIN_QUANTITY, MAX_QUANTITY))
    }

    fun remove(itemId: Int) {
        lines.removeAll { it.itemId == itemId }
    }

    fun clear() = lines.clear()

    companion object {
        const val MIN_QUANTITY = 1
        const val MAX_QUANTITY = 9
    }
}

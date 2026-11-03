package com.nthcase.sample.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow

enum class ThemeMode { System, Light, Dark }

/** Requests the XML settings screen sends to the Compose navigation. */
sealed interface AppEvent {
    data object OpenOrders : AppEvent
    data object SignedOut : AppEvent
}

/**
 * Everything the app remembers, in memory only. A cold start is always signed out with an empty cart and no orders,
 * so every run begins from the same screen (SPEC.md §Determinism).
 */
object AppState {
    const val TEST_EMAIL = "tester@mossgrove.test"
    const val TEST_PASSWORD = "Mossgrove123"

    var signedInEmail by mutableStateOf<String?>(null)
    val cart = Cart()
    val orders = mutableStateListOf<Order>()

    /** Settings › Testing: every list load waits 3 s first. */
    var slowLoading by mutableStateOf(false)

    /** Settings › Testing: the first load of each list fails; Retry then succeeds. */
    var failFirstLoad by mutableStateOf(false)

    /** Settings › Notifications: post an "order placed" notification when allowed. */
    var orderUpdates by mutableStateOf(false)

    var themeMode by mutableStateOf(ThemeMode.System)

    /** A deep link that arrived before sign-in; opened right after it. */
    var pendingItemId by mutableStateOf<String?>(null)

    val events = MutableSharedFlow<AppEvent>(extraBufferCapacity = 8, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    val signedIn: Boolean get() = signedInEmail != null

    /** The one check of the sign-in screen. Email case and surrounding spaces do not matter; the password's case does. */
    fun credentialsMatch(email: String, password: String): Boolean =
        email.trim().equals(TEST_EMAIL, ignoreCase = true) && password == TEST_PASSWORD

    fun signIn(email: String) {
        signedInEmail = email.trim().lowercase()
    }

    fun signOut() {
        signedInEmail = null
        cart.clear()
        orders.clear()
    }

    fun placeOrder(): Order {
        val order = Order(orders.size + 1, cart.lines.toList())
        orders.add(0, order)
        cart.clear()
        return order
    }

    /** Settings › Reset: empties the cart and the order history, and keeps you signed in. */
    fun resetData() {
        cart.clear()
        orders.clear()
    }

    /** Back to a cold start. Tests call it between cases. */
    fun reset() {
        signOut()
        slowLoading = false
        failFirstLoad = false
        orderUpdates = false
        themeMode = ThemeMode.System
        pendingItemId = null
    }
}

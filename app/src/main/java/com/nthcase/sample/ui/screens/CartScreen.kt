package com.nthcase.sample.ui.screens

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.nthcase.sample.Notifications
import com.nthcase.sample.TestIds
import com.nthcase.sample.data.AppState
import com.nthcase.sample.data.CartLine
import com.nthcase.sample.data.formatPrice
import com.nthcase.sample.ui.GlyphTile
import com.nthcase.sample.ui.Icons
import com.nthcase.sample.ui.ItemNumber
import com.nthcase.sample.ui.LocalSnack
import com.nthcase.sample.ui.MossgroveTopBar
import com.nthcase.sample.ui.QuantityStepper
import com.nthcase.sample.ui.StateMessage
import com.nthcase.sample.ui.testIdRoot

fun orderPlacedMessage(number: Int) = "Order $number placed"

@Composable
fun CartScreen(onBrowse: () -> Unit, onViewOrders: () -> Unit) {
    val cart = AppState.cart
    val snack = LocalSnack.current
    val context = LocalContext.current
    var confirming by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.testTag(TestIds.CART_SCREEN),
        topBar = { MossgroveTopBar("Cart") },
        bottomBar = {
            if (cart.lines.isNotEmpty()) {
                Surface(tonalElevation = 2.dp) {
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Total", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                            Text(formatPrice(cart.totalCents), style = MaterialTheme.typography.titleLarge, modifier = Modifier.testTag(TestIds.CART_TOTAL))
                        }
                        Spacer(Modifier.height(12.dp))
                        Button(
                            onClick = { confirming = true },
                            modifier = Modifier.fillMaxWidth().height(48.dp).testTag(TestIds.CART_CHECKOUT),
                        ) { Text("Check out") }
                    }
                }
            }
        },
    ) { padding ->
        if (cart.lines.isEmpty()) {
            StateMessage(
                icon = Icons.Cart,
                title = "Your cart is empty",
                body = "Open a drawer in the shop and add something.",
                actionLabel = "Browse the shop",
                onAction = onBrowse,
                modifier = Modifier.padding(padding).fillMaxSize().testTag(TestIds.CART_EMPTY),
                actionModifier = Modifier.testTag(TestIds.CART_EMPTY_ACTION),
            )
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding).fillMaxSize().testTag(TestIds.CART_LIST),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(cart.lines, key = { it.itemId }) { line -> CartLineRow(line) }
            }
        }
    }

    if (confirming) {
        AlertDialog(
            onDismissRequest = { confirming = false },
            modifier = Modifier.testIdRoot().testTag(TestIds.DIALOG),
            title = { Text("Place this order?", modifier = Modifier.testTag(TestIds.DIALOG_TITLE)) },
            text = {
                Text(
                    "${cart.itemCount} ${if (cart.itemCount == 1) "item" else "items"} · ${formatPrice(cart.totalCents)}. " +
                        "This is a sample shop: nothing is charged or shipped.",
                    modifier = Modifier.testTag(TestIds.DIALOG_TEXT),
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    confirming = false
                    val order = AppState.placeOrder()
                    notifyPlaced(context, order.number)
                    snack.show(orderPlacedMessage(order.number), "View orders", onViewOrders)
                }, modifier = Modifier.testTag(TestIds.DIALOG_CONFIRM)) { Text("Place order") }
            },
            dismissButton = {
                TextButton(onClick = { confirming = false }, modifier = Modifier.testTag(TestIds.DIALOG_DISMISS)) { Text("Cancel") }
            },
        )
    }
}

private fun notifyPlaced(context: Context, number: Int) {
    if (AppState.orderUpdates) Notifications.post(context, "Order $number placed", "We're packing it now.")
}

@Composable
private fun CartLineRow(line: CartLine) {
    val item = line.item
    Column(Modifier.fillMaxWidth().testTag(TestIds.cartLine(item.id))) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            GlyphTile(item, 48.dp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(item.name, style = MaterialTheme.typography.titleMedium)
                ItemNumber(item)
            }
            Text(
                formatPrice(line.totalCents),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.testTag(TestIds.cartLineTotal(item.id)),
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
            Text("${formatPrice(item.priceCents)} each", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
            QuantityStepper(
                value = line.quantity,
                onChange = { AppState.cart.setQuantity(item.id, it) },
                decreaseId = TestIds.cartLineDecrease(item.id),
                valueId = TestIds.cartLineQty(item.id),
                increaseId = TestIds.cartLineIncrease(item.id),
                subject = item.name,
            )
            IconButton(onClick = { AppState.cart.remove(item.id) }, modifier = Modifier.testTag(TestIds.cartLineRemove(item.id))) {
                Icon(Icons.Trash, contentDescription = "Remove ${item.name}")
            }
        }
        HorizontalDivider(Modifier.padding(top = 8.dp), color = MaterialTheme.colorScheme.outlineVariant)
    }
}

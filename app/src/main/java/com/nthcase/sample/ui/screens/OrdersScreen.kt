package com.nthcase.sample.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.nthcase.sample.TestIds
import com.nthcase.sample.data.AppState
import com.nthcase.sample.data.formatPrice
import com.nthcase.sample.ui.Icons
import com.nthcase.sample.ui.LoadStatus
import com.nthcase.sample.ui.Loading
import com.nthcase.sample.ui.MossgroveTopBar
import com.nthcase.sample.ui.StateMessage
import com.nthcase.sample.ui.rememberLoad

/** Order history: the empty state and, with Settings › Fail the first load, the error state with retry. */
@Composable
fun OrdersScreen(onBack: () -> Unit, onShop: () -> Unit) {
    val load = rememberLoad()
    Scaffold(modifier = Modifier.testTag(TestIds.ORDERS_SCREEN), topBar = { MossgroveTopBar("Orders", onBack = onBack) }) { padding ->
        val inner = Modifier.padding(padding).fillMaxSize()
        when {
            load.status == LoadStatus.Loading -> Loading("Fetching your orders…", inner.testTag(TestIds.ORDERS_LOADING))
            load.status == LoadStatus.Failed -> StateMessage(
                icon = Icons.Warning,
                title = "Orders didn't load",
                body = "Something went wrong on our side. Try again in a moment.",
                actionLabel = "Try again",
                onAction = load.retry,
                isError = true,
                modifier = inner.testTag(TestIds.ORDERS_ERROR),
                actionModifier = Modifier.testTag(TestIds.ORDERS_RETRY),
            )
            AppState.orders.isEmpty() -> StateMessage(
                icon = Icons.Empty,
                title = "No orders yet",
                body = "Orders you place appear here.",
                actionLabel = "Start shopping",
                onAction = onShop,
                modifier = inner.testTag(TestIds.ORDERS_EMPTY),
                actionModifier = Modifier.testTag(TestIds.ORDERS_EMPTY_ACTION),
            )
            else -> LazyColumn(inner.testTag(TestIds.ORDERS_LIST)) {
                items(AppState.orders, key = { it.number }) { order ->
                    ListItem(
                        headlineContent = { Text("Order ${order.number}") },
                        supportingContent = {
                            Text("${order.itemCount} ${if (order.itemCount == 1) "item" else "items"} · " + order.lines.joinToString { it.item.name })
                        },
                        trailingContent = { Text(formatPrice(order.totalCents), style = MaterialTheme.typography.titleMedium) },
                        modifier = Modifier.testTag(TestIds.ordersRow(order.number)),
                    )
                }
            }
        }
    }
}

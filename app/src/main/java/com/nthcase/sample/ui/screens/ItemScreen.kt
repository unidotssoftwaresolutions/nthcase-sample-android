package com.nthcase.sample.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.border
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.nthcase.sample.TestIds
import com.nthcase.sample.data.AppState
import com.nthcase.sample.data.Catalog
import com.nthcase.sample.data.formatPrice
import com.nthcase.sample.ui.GlyphTile
import com.nthcase.sample.ui.Icons
import com.nthcase.sample.ui.ItemNumber
import com.nthcase.sample.ui.LocalSnack
import com.nthcase.sample.ui.MossgroveTopBar
import com.nthcase.sample.ui.QuantityStepper
import com.nthcase.sample.ui.StateMessage

const val ADDED_MESSAGE = "Added to cart"

/** The detail page. [rawId] is the route's (or the deep link's) id as written, so an unknown id can say so. */
@Composable
fun ItemScreen(rawId: String, onBack: () -> Unit, onViewCart: () -> Unit) {
    val item = rawId.toIntOrNull()?.let { Catalog.find(it) }
    val snack = LocalSnack.current
    var quantity by rememberSaveable(rawId) { mutableIntStateOf(1) }

    Scaffold(
        modifier = Modifier.testTag(TestIds.ITEM_SCREEN),
        topBar = { MossgroveTopBar(item?.name ?: "Not found", onBack = onBack) },
    ) { padding ->
        if (item == null) {
            StateMessage(
                icon = Icons.Empty,
                title = "No drawer has the number \"$rawId\"",
                body = "Items are numbered 1 to ${Catalog.items.size}.",
                actionLabel = "Back to the shop",
                onAction = onBack,
                modifier = Modifier
                    .padding(padding)
                    .testTag(TestIds.ITEM_NOT_FOUND),
                actionModifier = Modifier.testTag(TestIds.ITEM_NOT_FOUND_ACTION),
            )
            return@Scaffold
        }
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(Modifier.widthIn(max = 560.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                GlyphTile(
                    item,
                    size = 220.dp,
                    description = "Drawing of the ${item.name}",
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .testTag(TestIds.ITEM_IMAGE),
                )
                Spacer(Modifier.height(4.dp))
                // the drawer's label holder, at the scale of the page: the name, then its number
                Column(
                    Modifier
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, MaterialTheme.shapes.extraSmall)
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                ) {
                    Text(item.name, style = MaterialTheme.typography.headlineSmall, modifier = Modifier.testTag(TestIds.ITEM_NAME))
                    ItemNumber(item, Modifier.testTag(TestIds.ITEM_NUMBER))
                }
                Text(formatPrice(item.priceCents), style = MaterialTheme.typography.titleLarge, modifier = Modifier.testTag(TestIds.ITEM_PRICE))
                Text(
                    item.description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.testTag(TestIds.ITEM_DESCRIPTION),
                )
                Text("${item.category.label} aisle", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Quantity", style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                    QuantityStepper(
                        value = quantity,
                        onChange = { quantity = it },
                        decreaseId = TestIds.ITEM_QTY_DECREASE,
                        valueId = TestIds.ITEM_QTY_VALUE,
                        increaseId = TestIds.ITEM_QTY_INCREASE,
                        subject = item.name,
                    )
                }
                Button(
                    onClick = {
                        AppState.cart.add(item.id, quantity)
                        snack.show(ADDED_MESSAGE, "View cart", onViewCart)
                    },
                    // brass, like the item number and the cart count it fills: the one live action of the page
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiary,
                        contentColor = MaterialTheme.colorScheme.onTertiary,
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag(TestIds.ITEM_ADD_TO_CART),
                ) { Text("Add to cart · ${formatPrice(item.priceCents * quantity)}") }
            }
        }
    }
}

package com.nthcase.sample.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import com.nthcase.sample.data.Glyph
import com.nthcase.sample.ui.Art
import com.nthcase.sample.ui.Icons
import com.nthcase.sample.ui.theme.LocalAisleTints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nthcase.sample.ui.MossgroveTopBar

/** The one gift-card code that has a balance. Any other code is unknown. */
const val GIFT_CARD_CODE = "MOSS-0025"
const val GIFT_CARD_BALANCE = "Balance: $25.00"
const val GIFT_CARD_UNKNOWN = "We don't recognise that code"

/**
 * The screen with no test ids at all (SPEC.md §The screen without ids). Nothing here carries a test tag, including
 * its top bar, so NthCase must find each control by its visible text, its label and its position. Two buttons share
 * the text "Choose": only their position tells them apart.
 */
@Composable
fun GiftCardsScreen(onBack: () -> Unit) {
    var code by rememberSaveable { mutableStateOf("") }
    var result by rememberSaveable { mutableStateOf<String?>(null) }
    var amount by rememberSaveable { mutableStateOf(25) }
    var design by rememberSaveable { mutableStateOf<String?>(null) }

    Scaffold(topBar = { MossgroveTopBar("Gift cards", onBack = onBack, tagged = false) }) { padding ->
        Column(
            Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text("Check a balance", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = code,
                onValueChange = { code = it.uppercase(); result = null },
                label = { Text("Card code") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Button(
                onClick = { result = if (code.trim() == GIFT_CARD_CODE) GIFT_CARD_BALANCE else GIFT_CARD_UNKNOWN },
                modifier = Modifier.fillMaxWidth().height(48.dp),
            ) { Text("Check balance") }
            result?.let { Text(it, style = MaterialTheme.typography.titleMedium) }

            Text("Buy a card", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(10, 25, 50).forEach { value ->
                    FilterChip(
                        selected = amount == value,
                        onClick = { amount = value },
                        label = { Text("$$value") },
                        // selected is a mark as well as a tint
                        leadingIcon = if (amount == value) ({ Icon(Icons.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }) else null,
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                DesignCard("Fern", "Green leaves", design == "Fern", Modifier.weight(1f)) { design = "Fern" }
                DesignCard("Stone", "Grey pebbles", design == "Stone", Modifier.weight(1f)) { design = "Stone" }
            }
            Text(
                design?.let { "A $$amount card with the $it design" } ?: "Choose a design",
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}

/** A gift-card design: a picture of it, its name, and Choose. The chosen one has a check and a primary border. */
@Composable
private fun DesignCard(title: String, body: String, chosen: Boolean, modifier: Modifier, onChoose: () -> Unit) {
    val tints = LocalAisleTints.current
    Card(
        modifier,
        border = if (chosen) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            val tint = if (title == "Fern") tints.garden else tints.home
            Box(
                Modifier.fillMaxWidth().height(72.dp).background(tint.tile, MaterialTheme.shapes.small),
                contentAlignment = Alignment.Center,
            ) {
                if (title == "Fern") {
                    Icon(Art.of(Glyph.Plant), contentDescription = null, tint = tint.ink, modifier = Modifier.size(44.dp))
                } else {
                    // three pebbles, drawn for this card
                    Canvas(Modifier.size(width = 64.dp, height = 36.dp)) {
                        val ink = tint.ink
                        val stroke = Stroke(width = 2.5.dp.toPx())
                        drawOval(ink, Offset(0f, size.height * 0.35f), Size(size.width * 0.42f, size.height * 0.6f), style = stroke)
                        drawOval(ink, Offset(size.width * 0.36f, 0f), Size(size.width * 0.34f, size.height * 0.5f), style = stroke)
                        drawOval(ink, Offset(size.width * 0.56f, size.height * 0.45f), Size(size.width * 0.44f, size.height * 0.52f), style = stroke)
                    }
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(title, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                if (chosen) Icon(Icons.Check, contentDescription = "Chosen", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
            }
            Text(body, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            OutlinedButton(onClick = onChoose, modifier = Modifier.fillMaxWidth()) { Text("Choose") }
        }
    }
}

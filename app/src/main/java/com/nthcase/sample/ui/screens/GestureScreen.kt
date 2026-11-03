package com.nthcase.sample.ui.screens

import android.content.Context
import android.content.res.Configuration
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateRotation
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.onLongClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.nthcase.sample.TestIds
import com.nthcase.sample.data.ShakeDetector
import com.nthcase.sample.ui.Icons
import com.nthcase.sample.ui.MossgroveTopBar
import com.nthcase.sample.ui.theme.ItemNumberStyle
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

/** The status words of the gestures screen. They are the spec's oracle: the same words on every framework. */
object GestureWords {
    const val READY = "Ready"
    const val TAPPED = "Tapped"
    const val DOUBLE_TAPPED = "Double-tapped"
    const val LONG_PRESSED = "Long-pressed"
    fun swiped(direction: String) = "Swiped $direction"
    const val DROPPED = "Dropped on the drawer"
    const val DROP_MISSED = "Dropped outside the drawer"
    const val PINCHED_OUT = "Pinched out"
    const val PINCHED_IN = "Pinched in"
    const val ROTATED_CW = "Rotated clockwise"
    const val ROTATED_CCW = "Rotated counter-clockwise"
    const val SCROLLED_TO = "Found the bottom drawer"
    const val SHAKEN = "Shaken"
}

/** A swipe's direction, from its total travel; null when it travelled less than [minPx]. */
fun swipeDirection(dx: Float, dy: Float, minPx: Float): String? = when {
    maxOf(abs(dx), abs(dy)) < minPx -> null
    abs(dx) >= abs(dy) -> if (dx > 0) "right" else "left"
    else -> if (dy > 0) "down" else "up"
}

@Composable
fun GestureScreen(onBack: () -> Unit) {
    var status by rememberSaveable { mutableStateOf(GestureWords.READY) }
    var taps by rememberSaveable { mutableIntStateOf(0) }
    var zoom by rememberSaveable { mutableFloatStateOf(1f) }
    var rotation by rememberSaveable { mutableFloatStateOf(0f) }
    val landscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

    ShakeListener { status = GestureWords.SHAKEN }

    Scaffold(modifier = Modifier.testTag(TestIds.GESTURES_SCREEN), topBar = { MossgroveTopBar("Gestures", onBack = onBack) }) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            // Pinned above the scrolling benches, so the status is on screen after any gesture.
            Surface(color = MaterialTheme.colorScheme.secondaryContainer, modifier = Modifier.fillMaxWidth()) {
                Row(Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        status,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier
                            .weight(1f)
                            .testTag(TestIds.GESTURES_STATUS)
                            .semantics { liveRegion = LiveRegionMode.Polite },
                    )
                    Text(
                        if (landscape) "Landscape" else "Portrait",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.testTag(TestIds.GESTURES_ORIENTATION),
                    )
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .testTag(TestIds.GESTURES_SCROLL)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Pad(
                        "Tap", "Taps: $taps", Modifier.weight(1f).testTag(TestIds.GESTURES_TAP)
                            .clickable(onClickLabel = "Tap") { taps += 1; status = GestureWords.TAPPED },
                        countId = TestIds.GESTURES_TAP_COUNT,
                    )
                    Pad(
                        "Double tap", "Twice, quickly", Modifier.weight(1f).testTag(TestIds.GESTURES_DOUBLE_TAP)
                            .semantics {
                                customActions = listOf(CustomAccessibilityAction("Double tap") { status = GestureWords.DOUBLE_TAPPED; true })
                            }
                            .pointerInput(Unit) { detectTapGestures(onDoubleTap = { status = GestureWords.DOUBLE_TAPPED }) },
                    )
                    Pad(
                        "Long press", "Hold for a moment", Modifier.weight(1f).testTag(TestIds.GESTURES_LONG_PRESS)
                            .semantics { onLongClick(label = "Long press") { status = GestureWords.LONG_PRESSED; true } }
                            .pointerInput(Unit) { detectTapGestures(onLongPress = { status = GestureWords.LONG_PRESSED }) },
                    )
                }

                SwipePad { status = GestureWords.swiped(it) }

                DragAndDrop { dropped -> status = if (dropped) GestureWords.DROPPED else GestureWords.DROP_MISSED }

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    TwoFingerPad(
                        title = "Pinch",
                        value = String.format(Locale.US, "Zoom %.1f×", zoom),
                        valueId = TestIds.GESTURES_ZOOM_VALUE,
                        modifier = Modifier.weight(1f).testTag(TestIds.GESTURES_PINCH),
                        contentScale = zoom,
                        onGestureEnd = { z, _ ->
                            if (z > 1.05f || z < 0.95f) {
                                zoom = (zoom * z).coerceIn(0.5f, 3f)
                                status = if (z > 1f) GestureWords.PINCHED_OUT else GestureWords.PINCHED_IN
                            }
                        },
                    )
                    TwoFingerPad(
                        title = "Rotate",
                        value = "Rotation ${rotation.roundToInt()}°",
                        valueId = TestIds.GESTURES_ROTATION_VALUE,
                        modifier = Modifier.weight(1f).testTag(TestIds.GESTURES_ROTATE),
                        contentRotation = rotation,
                        onGestureEnd = { _, r ->
                            if (abs(r) > 10f) {
                                rotation = ((rotation + r) % 360f)
                                status = if (r > 0) GestureWords.ROTATED_CW else GestureWords.ROTATED_CCW
                            }
                        },
                    )
                }

                Text(
                    "Scroll to the bottom drawer. It is below the shelves.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                repeat(12) { shelf ->
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .background(MaterialTheme.colorScheme.surfaceContainer, MaterialTheme.shapes.medium)
                            .padding(16.dp),
                    ) { Text("Shelf ${shelf + 1}", color = MaterialTheme.colorScheme.onSurfaceVariant) }
                }
                Button(
                    onClick = { status = GestureWords.SCROLLED_TO },
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag(TestIds.GESTURES_SCROLL_TARGET),
                ) { Text("Open the bottom drawer") }
            }
        }
    }
}

@Composable
private fun Pad(title: String, hint: String, modifier: Modifier, countId: String? = null) {
    Box(
        modifier = modifier
            .height(104.dp)
            .background(MaterialTheme.colorScheme.surfaceContainerLow, MaterialTheme.shapes.medium)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, MaterialTheme.shapes.medium)
            .padding(12.dp),
    ) {
        Column {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(4.dp))
            Text(
                hint,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                // its own node, so the count keeps its id inside the clickable pad
                modifier = if (countId != null) Modifier.testTag(countId).semantics(mergeDescendants = true) {} else Modifier,
            )
        }
    }
}

@Composable
private fun SwipePad(onSwipe: (String) -> Unit) {
    val minPx = with(LocalDensity.current) { 48.dp.toPx() }
    var travel by remember { mutableStateOf(Offset.Zero) }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .background(MaterialTheme.colorScheme.surfaceContainerLow, MaterialTheme.shapes.medium)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, MaterialTheme.shapes.medium)
            .testTag(TestIds.GESTURES_SWIPE)
            .semantics {
                contentDescription = "Swipe pad"
                customActions = listOf("left", "right", "up", "down").map { d ->
                    CustomAccessibilityAction("Swipe $d") { onSwipe(d); true }
                }
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { travel = Offset.Zero },
                    onDragEnd = { swipeDirection(travel.x, travel.y, minPx)?.let(onSwipe) },
                ) { change, amount ->
                    change.consume()
                    travel += amount
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Swipe", style = MaterialTheme.typography.titleSmall)
            Text("Left, right, up or down", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun DragAndDrop(onDrop: (Boolean) -> Unit) {
    var offset by remember { mutableStateOf(Offset.Zero) }
    var sourceBounds by remember { mutableStateOf(Rect.Zero) }
    var targetBounds by remember { mutableStateOf(Rect.Zero) }
    var dropped by rememberSaveable { mutableStateOf(false) }
    val drop by rememberUpdatedState(onDrop)

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Drag the label onto the drawer", style = MaterialTheme.typography.titleSmall)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            Box(
                modifier = Modifier
                    .offset { IntOffset(offset.x.roundToInt(), offset.y.roundToInt()) }
                    .onGloballyPositioned { sourceBounds = it.boundsInRoot() }
                    .size(width = 120.dp, height = 56.dp)
                    .background(MaterialTheme.colorScheme.tertiaryContainer, MaterialTheme.shapes.extraSmall)
                    .border(1.dp, MaterialTheme.colorScheme.tertiary, MaterialTheme.shapes.extraSmall)
                    .testTag(TestIds.GESTURES_DRAG_SOURCE)
                    .semantics {
                        contentDescription = "Label No. 007"
                        customActions = listOf(CustomAccessibilityAction("Drop on the drawer") { dropped = true; drop(true); true })
                    }
                    .pointerInput(Unit) {
                        var start = Rect.Zero
                        detectDragGestures(
                            onDragStart = { start = sourceBounds },
                            onDragEnd = {
                                val hit = targetBounds.contains(start.center + offset)
                                if (hit) dropped = true
                                drop(hit)
                                offset = Offset.Zero
                            },
                            onDragCancel = { offset = Offset.Zero },
                        ) { change, amount ->
                            change.consume()
                            offset += amount
                        }
                    },
                contentAlignment = Alignment.Center,
            ) { Text("No. 007", style = ItemNumberStyle, color = MaterialTheme.colorScheme.onTertiaryContainer) }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(96.dp)
                    .onGloballyPositioned { targetBounds = it.boundsInRoot() }
                    .background(
                        if (dropped) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLow,
                        MaterialTheme.shapes.medium,
                    )
                    .border(2.dp, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.medium)
                    .testTag(TestIds.GESTURES_DROP_TARGET)
                    .semantics { contentDescription = if (dropped) "Drawer, labelled" else "Drawer, no label" },
                contentAlignment = Alignment.Center,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (dropped) Icon(Icons.Check, contentDescription = null, modifier = Modifier.size(18.dp).padding(end = 4.dp))
                    Text(if (dropped) "Labelled" else "Drawer")
                }
            }
        }
    }
}

/**
 * A pad that reads a two-finger gesture from first touch to last lift, then reports its total zoom and rotation
 * (degrees, clockwise positive) once.
 */
@Composable
private fun TwoFingerPad(
    title: String,
    value: String,
    valueId: String,
    modifier: Modifier,
    contentScale: Float = 1f,
    contentRotation: Float = 0f,
    onGestureEnd: (zoom: Float, rotation: Float) -> Unit,
) {
    val end by rememberUpdatedState(onGestureEnd)
    Box(
        modifier = modifier
            .height(160.dp)
            .background(MaterialTheme.colorScheme.surfaceContainerLow, MaterialTheme.shapes.medium)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, MaterialTheme.shapes.medium)
            .semantics { contentDescription = "$title pad" }
            .pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false)
                    var zoom = 1f
                    var rotation = 0f
                    do {
                        val event = awaitPointerEvent()
                        if (event.changes.count { it.pressed } >= 2) {
                            zoom *= event.calculateZoom()
                            rotation += event.calculateRotation()
                            event.changes.forEach { if (it.positionChanged()) it.consume() }
                        }
                    } while (event.changes.any { it.pressed })
                    end(zoom, rotation)
                }
            }
            .padding(12.dp),
    ) {
        Text(title, style = MaterialTheme.typography.titleSmall)
        Box(
            Modifier
                .align(Alignment.Center)
                .size(48.dp)
                .graphicsLayer { scaleX = contentScale; scaleY = contentScale; rotationZ = contentRotation }
                .background(MaterialTheme.colorScheme.primary, MaterialTheme.shapes.extraSmall),
        )
        Text(value, style = MaterialTheme.typography.bodySmall, modifier = Modifier.align(Alignment.BottomStart).testTag(valueId))
    }
}

/** Listens to the accelerometer while the screen is shown. */
@Composable
private fun ShakeListener(onShake: () -> Unit) {
    val context = LocalContext.current
    val shake by rememberUpdatedState(onShake)
    DisposableEffect(context) {
        val sensors = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        val accelerometer = sensors?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val detector = ShakeDetector { shake() }
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                detector.onReading(event.values[0], event.values[1], event.values[2], event.timestamp / 1_000_000)
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
        }
        if (accelerometer != null) sensors.registerListener(listener, accelerometer, SensorManager.SENSOR_DELAY_GAME)
        onDispose { sensors?.unregisterListener(listener) }
    }
}

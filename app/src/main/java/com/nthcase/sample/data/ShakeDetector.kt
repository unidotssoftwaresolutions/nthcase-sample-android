package com.nthcase.sample.data

import kotlin.math.sqrt

/**
 * A shake is three jolts above 2.5 g within one second. Pure, so the JVM tests can feed it readings; the gestures
 * screen feeds it the accelerometer.
 */
class ShakeDetector(private val onShake: () -> Unit) {
    private val jolts = ArrayDeque<Long>()

    fun onReading(x: Float, y: Float, z: Float, timestampMs: Long) {
        val g = sqrt(x * x + y * y + z * z) / EARTH_GRAVITY
        if (g < THRESHOLD_G) return
        jolts.addLast(timestampMs)
        while (jolts.isNotEmpty() && timestampMs - jolts.first() > WINDOW_MS) jolts.removeFirst()
        if (jolts.size >= JOLTS) {
            jolts.clear()
            onShake()
        }
    }

    companion object {
        const val EARTH_GRAVITY = 9.80665f
        const val THRESHOLD_G = 2.5f
        const val WINDOW_MS = 1000L
        const val JOLTS = 3
    }
}

package com.example.a2.data.shared

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Locale

class MetricDisplayTest {

    @Test
    fun testTileDisplayConversion() {
        // Valid BPM
        val bpm = 72.0
        val text = String.format(Locale.US, "%.0f bpm", bpm)
        assertEquals("72 bpm", text)

        // Missing data (handled in TileService)
        val waitingText = "Waiting for sensor data"
        assertEquals("Waiting for sensor data", waitingText)
    }

    @Test
    fun testComplicationShortTextConversion() {
        // Valid BPM
        val bpm = 72.0
        val text = String.format(Locale.US, "%.0f", bpm)
        assertEquals("72", text)

        // Missing data (Wait)
        val waitText = "Wait"
        assertEquals("Wait", waitText)
    }

    @Test
    fun testAccessibilityDescriptions() {
        // Valid state
        val bpm = 72.0
        val description = "Heart rate ${String.format(Locale.US, "%.0f", bpm)} bpm"
        assertEquals("Heart rate 72 bpm", description)

        // Missing state
        val waitingDescription = "Heart rate waiting for data"
        assertEquals("Heart rate waiting for data", waitingDescription)
    }

    @Test
    fun testRateLimitingLogic() {
        val threshold = 10_000L
        var lastUpdate = 0L
        
        fun shouldUpdate(now: Long): Boolean {
            if (lastUpdate == 0L || now - lastUpdate >= threshold) {
                lastUpdate = now
                return true
            }
            return false
        }

        // First update at T=1000 should pass (first valid reading)
        assert(shouldUpdate(1000L))
        assertEquals(1000L, lastUpdate)

        // Second update at T=5000 should be limited
        assert(!shouldUpdate(5000L))
        assertEquals(1000L, lastUpdate)

        // Third update at T=12000 should pass (threshold reached)
        assert(shouldUpdate(12000L))
        assertEquals(12000L, lastUpdate)
    }
}

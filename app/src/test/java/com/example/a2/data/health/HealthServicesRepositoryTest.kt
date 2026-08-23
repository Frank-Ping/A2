package com.example.a2.data.health

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HealthServicesRepositoryTest {

    @Test
    fun testPermissionDeniedState() {
        val repo = FakeHealthServicesRepository()
        repo.hasPermission = false
        repo.start()
        assertTrue(repo.heartRateState.value is HeartRateState.PermissionRequired)
    }

    @Test
    fun testUnsupportedHardwareState() {
        val repo = FakeHealthServicesRepository()
        repo.isSupported = false
        repo.start()
        assertTrue(repo.heartRateState.value is HeartRateState.Unsupported)
    }

    @Test
    fun testValidReadingAndTransitions() {
        val repo = FakeHealthServicesRepository()
        repo.start()
        assertTrue(repo.heartRateState.value is HeartRateState.Measuring)

        repo.emitHeartRate(75.0, 1000L)
        val state = repo.heartRateState.value
        assertTrue(state is HeartRateState.Active)
        assertEquals(75.0, (state as HeartRateState.Active).bpm, 0.1)
    }

    @Test
    fun testSensorUnavailableState() {
        val repo = FakeHealthServicesRepository()
        repo.start()
        repo.setAvailability(false)
        assertTrue(repo.heartRateState.value is HeartRateState.Unavailable)
        
        repo.setAvailability(true)
        // Should return to measuring or stay active if data arrived, 
        // in fake it returns to measuring if not active.
        assertTrue(repo.heartRateState.value is HeartRateState.Measuring)
    }

    @Test
    fun testBalancedStartStop() {
        val repo = FakeHealthServicesRepository()
        repo.start()
        assertTrue(repo.isStarted)
        
        repo.stop()
        assertEquals(HeartRateState.Waiting, repo.heartRateState.value)
        assertTrue(!repo.isStarted)
    }
}

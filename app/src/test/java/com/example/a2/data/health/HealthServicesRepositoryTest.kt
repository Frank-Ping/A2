package com.example.a2.data.health

import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HealthServicesRepositoryTest {

    @Test
    fun testPermissionDeniedState() = runBlocking {
        val repo = FakeHealthServicesRepository()
        repo.hasPermission = false
        repo.start()
        assertTrue(repo.heartRateState.value is HeartRateState.PermissionRequired)
    }

    @Test
    fun testUnsupportedHardwareState() = runBlocking {
        val repo = FakeHealthServicesRepository()
        repo.isSupported = false
        repo.start()
        assertTrue(repo.heartRateState.value is HeartRateState.Unsupported)
    }

    @Test
    fun testValidReadingAndTransitions() = runBlocking {
        val repo = FakeHealthServicesRepository()
        repo.start()
        assertTrue(repo.heartRateState.value is HeartRateState.Measuring)

        repo.emitHeartRate(75.0, 1000L)
        val state = repo.heartRateState.value
        assertTrue(state is HeartRateState.Active)
        assertEquals(75.0, (state as HeartRateState.Active).bpm, 0.1)
    }

    @Test
    fun testSensorUnavailableState() = runBlocking {
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
    fun testPermissionDeniedProducesPermissionRequired() {
        val repo = FakeHealthServicesRepository()
        repo.onPermissionDenied()
        assertEquals(HeartRateState.PermissionRequired, repo.heartRateState.value)
    }

    @Test
    fun testLifecycleResumeMeasurement() = runBlocking {
        val repo = FakeHealthServicesRepository()
        
        // Initial start
        repo.start()
        assertTrue(repo.isStarted)
        assertEquals(HeartRateState.Measuring, repo.heartRateState.value)
        
        // Stop
        repo.stop()
        assertTrue(!repo.isStarted)
        assertEquals(HeartRateState.Waiting, repo.heartRateState.value)
        
        // Restart
        repo.start()
        assertTrue(repo.isStarted)
        assertEquals(HeartRateState.Measuring, repo.heartRateState.value)
    }

    @Test
    fun testDuplicateStartCalls() = runBlocking {
        val repo = FakeHealthServicesRepository()
        repo.start()
        assertEquals(1, repo.registrationCount)
        assertTrue(repo.isStarted)
        
        // Subsequent calls should be ignored or not change state if already started
        repo.start()
        assertEquals(1, repo.registrationCount)
        assertTrue(repo.isStarted)
    }

    @Test
    fun testInterruptedStartRegistration() = runBlocking {
        val repo = FakeHealthServicesRepository()
        repo.startDelayMs = 1000L // Simulate network/system delay
        
        val job = launch {
            repo.start()
        }
        
        // Stop immediately while start is pending
        repo.stop()
        job.cancel()
        
        assertEquals(0, repo.registrationCount)
        assertEquals(HeartRateState.Waiting, repo.heartRateState.value)
    }
}

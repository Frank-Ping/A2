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
        
        repo.start()
        assertTrue(repo.isStarted)
        repo.stop()
        assertTrue(!repo.isStarted)
        repo.start()
        assertTrue(repo.isStarted)
    }

    @Test
    fun testDuplicateStartCalls() = runBlocking {
        val repo = FakeHealthServicesRepository()
        repo.start()
        assertEquals(1, repo.registrationCount)
        repo.start()
        assertEquals(1, repo.registrationCount)
    }

    @Test
    fun testInterruptedStartRegistration() = runBlocking {
        val repo = FakeHealthServicesRepository()
        repo.startDelayMs = 500L
        
        val job = launch {
            repo.start()
        }
        
        repo.stop()
        job.cancel()
        
        assertEquals(0, repo.registrationCount)
        assertEquals(HeartRateState.Waiting, repo.heartRateState.value)
    }

    @Test
    fun testInvalidSamplesAreIgnored() = runBlocking {
        val repo = FakeHealthServicesRepository()
        repo.start()
        
        // Negative BPM
        repo.emitHeartRate(-1.0, 1000L)
        assertTrue(repo.heartRateState.value !is HeartRateState.Active)

        // NaN BPM
        repo.emitHeartRate(Double.NaN, 1000L)
        assertTrue(repo.heartRateState.value !is HeartRateState.Active)

        // Infinity BPM
        repo.emitHeartRate(Double.POSITIVE_INFINITY, 1000L)
        assertTrue(repo.heartRateState.value !is HeartRateState.Active)
    }

    @Test
    fun testValidSample88TriggersActive() = runBlocking {
        val repo = FakeHealthServicesRepository()
        repo.start()
        
        repo.emitHeartRate(88.0, 1000L)
        val state = repo.heartRateState.value
        assertTrue(state is HeartRateState.Active)
        assertEquals(88.0, (state as HeartRateState.Active).bpm, 0.1)
    }
}

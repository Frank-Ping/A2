package com.example.a2.data.sensor

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SensorRepositoryTest {

    @Test
    fun testFakeStateTransitions() {
        val repo = FakeSensorRepository()
        
        // Initial state
        assertEquals("Waiting", repo.accelerometer.value.status)
        assertNull(repo.accelerometer.value.x)

        // Emit data
        repo.emitAccelerometer(1.0f, -2.0f, 0.0001f)
        
        assertEquals("Active", repo.accelerometer.value.status)
        assertEquals(1.0f, repo.accelerometer.value.x!!, 0.001f)
        assertEquals(-2.0f, repo.accelerometer.value.y!!, 0.001f)
        assertEquals(0.0001f, repo.accelerometer.value.z!!, 0.001f)
    }

    @Test
    fun testReplacingOldSample() {
        val repo = FakeSensorRepository()
        
        repo.emitAccelerometer(1.0f, 1.0f, 1.0f)
        val firstTimestamp = repo.accelerometer.value.timestamp
        
        // Ensure some time passes if needed, but here we just check if it updates
        Thread.sleep(10) 
        
        repo.emitAccelerometer(2.0f, 2.0f, 2.0f)
        assertEquals(2.0f, repo.accelerometer.value.x!!, 0.001f)
        assert(repo.accelerometer.value.timestamp > firstTimestamp)
    }

    @Test
    fun testStartStopBalance() {
        val repo = FakeSensorRepository()
        
        repo.start()
        repo.start()
        assertEquals(2, repo.startCalled) // Fake shows literal calls
        
        repo.stop()
        assertEquals(1, repo.stopCalled)
    }
}

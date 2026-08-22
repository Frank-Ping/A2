package com.example.a2.data.sensor

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class FakeSensorRepository : SensorRepository {
    private val _accelerometer = MutableStateFlow(SensorReading(1, "Accel", unit = "m/s²", status = "Waiting"))
    private val _gyroscope = MutableStateFlow(SensorReading(2, "Gyro", unit = "rad/s", status = "Waiting"))
    private val _magneticField = MutableStateFlow(SensorReading(3, "Mag", unit = "µT", status = "Waiting"))

    override val accelerometer: StateFlow<SensorReading> = _accelerometer.asStateFlow()
    override val gyroscope: StateFlow<SensorReading> = _gyroscope.asStateFlow()
    override val magneticField: StateFlow<SensorReading> = _magneticField.asStateFlow()

    var startCalled = 0
    var stopCalled = 0

    override fun start() {
        startCalled++
    }

    override fun stop() {
        stopCalled++
    }

    fun emitAccelerometer(x: Float, y: Float, z: Float) {
        _accelerometer.update { it.copy(x = x, y = y, z = z, status = "Active", timestamp = System.currentTimeMillis()) }
    }

    fun emitGyroscope(x: Float, y: Float, z: Float) {
        _gyroscope.update { it.copy(x = x, y = y, z = z, status = "Active", timestamp = System.currentTimeMillis()) }
    }

    fun emitMagneticField(x: Float, y: Float, z: Float) {
        _magneticField.update { it.copy(x = x, y = y, z = z, status = "Active", timestamp = System.currentTimeMillis()) }
    }
}

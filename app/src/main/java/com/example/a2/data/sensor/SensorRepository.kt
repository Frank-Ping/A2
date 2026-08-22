package com.example.a2.data.sensor

import kotlinx.coroutines.flow.StateFlow

interface SensorRepository {
    val accelerometer: StateFlow<SensorReading>
    val gyroscope: StateFlow<SensorReading>
    val magneticField: StateFlow<SensorReading>

    fun start()
    fun stop()
}

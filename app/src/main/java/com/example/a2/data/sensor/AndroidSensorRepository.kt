package com.example.a2.data.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.example.a2.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class AndroidSensorRepository(private val context: Context) : SensorRepository, SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    private val accelSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val gyroSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
    private val magSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

    private val _accelerometer = MutableStateFlow(createInitialReading(Sensor.TYPE_ACCELEROMETER, context.getString(R.string.accelerometer), context.getString(R.string.unit_accel), accelSensor != null))
    private val _gyroscope = MutableStateFlow(createInitialReading(Sensor.TYPE_GYROSCOPE, context.getString(R.string.gyroscope), context.getString(R.string.unit_gyro), gyroSensor != null))
    private val _magneticField = MutableStateFlow(createInitialReading(Sensor.TYPE_MAGNETIC_FIELD, context.getString(R.string.magnetic_field), context.getString(R.string.unit_mag), magSensor != null))

    override val accelerometer: StateFlow<SensorReading> = _accelerometer.asStateFlow()
    override val gyroscope: StateFlow<SensorReading> = _gyroscope.asStateFlow()
    override val magneticField: StateFlow<SensorReading> = _magneticField.asStateFlow()

    private var isStarted = false

    override fun start() {
        if (isStarted) return
        isStarted = true
        accelSensor?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL) }
        gyroSensor?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL) }
        magSensor?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL) }
    }

    override fun stop() {
        if (!isStarted) return
        isStarted = false
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        val values = event?.values ?: return
        val now = System.currentTimeMillis()
        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> _accelerometer.update { it.copy(x = values[0], y = values[1], z = values[2], status = context.getString(R.string.status_active), timestamp = now) }
            Sensor.TYPE_GYROSCOPE -> _gyroscope.update { it.copy(x = values[0], y = values[1], z = values[2], status = context.getString(R.string.status_active), timestamp = now) }
            Sensor.TYPE_MAGNETIC_FIELD -> _magneticField.update { it.copy(x = values[0], y = values[1], z = values[2], status = context.getString(R.string.status_active), timestamp = now) }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not used
    }

    private fun createInitialReading(id: Int, name: String, unit: String, available: Boolean): SensorReading {
        return SensorReading(
            id = id,
            name = name,
            unit = unit,
            status = context.getString(if (available) R.string.waiting_sensor else R.string.status_unavailable)
        )
    }
}

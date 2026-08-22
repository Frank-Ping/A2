package com.example.a2.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.SurfaceTransformation
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.lazy.rememberTransformationSpec
import androidx.wear.compose.material3.lazy.transformedHeight
import com.example.a2.R
import com.example.a2.data.sensor.AndroidSensorRepository
import com.example.a2.data.sensor.SensorReading
import com.example.a2.data.sensor.SensorRepository
import com.example.a2.presentation.theme.A2Theme
import java.util.Locale

class SensorActivity : ComponentActivity() {

    private lateinit var sensorRepository: SensorRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sensorRepository = AndroidSensorRepository(applicationContext)

        setContent {
            val accel by sensorRepository.accelerometer.collectAsState()
            val gyro by sensorRepository.gyroscope.collectAsState()
            val mag by sensorRepository.magneticField.collectAsState()

            SensorScreen(
                readings = listOf(accel, gyro, mag),
                onBack = { finish() }
            )
        }
    }

    override fun onStart() {
        super.onStart()
        sensorRepository.start()
    }

    override fun onStop() {
        super.onStop()
        sensorRepository.stop()
    }
}

@Composable
fun SensorScreen(
    readings: List<SensorReading>,
    onBack: () -> Unit
) {
    A2Theme {
        AppScaffold {
            val listState = rememberTransformingLazyColumnState()
            val transformationSpec = rememberTransformationSpec()
            ScreenScaffold(scrollState = listState) { contentPadding ->
                TransformingLazyColumn(
                    contentPadding = contentPadding,
                    state = listState,
                    modifier = Modifier.testTag("SensorList")
                ) {
                    item {
                        ListHeader(
                            modifier = Modifier
                                .fillMaxWidth()
                                .transformedHeight(this, transformationSpec)
                                .testTag("SensorHeading"),
                            transformation = SurfaceTransformation(transformationSpec),
                        ) {
                            Text(
                                text = stringResource(R.string.sensor_heading),
                                style = MaterialTheme.typography.titleMedium,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    readings.forEach { reading ->
                        item {
                            SensorSection(reading)
                        }
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }

                    item {
                        Button(
                            onClick = onBack,
                            modifier = Modifier
                                .fillMaxWidth()
                                .transformedHeight(this, transformationSpec)
                                .testTag("BackButton"),
                            transformation = SurfaceTransformation(transformationSpec),
                        ) {
                            Text(stringResource(R.string.back))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SensorSection(reading: SensorReading) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = reading.name,
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.labelMedium,
            textAlign = TextAlign.Center
        )
        reading.x?.let {
            SensorValue(label = "X", value = it, unit = reading.unit)
        }
        reading.y?.let {
            SensorValue(label = "Y", value = it, unit = reading.unit)
        }
        reading.z?.let {
            SensorValue(label = "Z", value = it, unit = reading.unit)
        }
        Text(
            text = reading.status,
            modifier = Modifier
                .fillMaxWidth()
                .testTag(if (reading.id == android.hardware.Sensor.TYPE_ACCELEROMETER) "SensorStatusText" else "SensorStatusText_${reading.id}"),
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            color = if (reading.status == stringResource(R.string.status_active)) 
                MaterialTheme.colorScheme.primary 
            else 
                MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun SensorValue(label: String, value: Float, unit: String) {
    Text(
        text = String.format(Locale.US, "%s: %.2f %s", label, value, unit),
        modifier = Modifier.fillMaxWidth(),
        style = MaterialTheme.typography.bodyMedium,
        textAlign = TextAlign.Center
    )
}

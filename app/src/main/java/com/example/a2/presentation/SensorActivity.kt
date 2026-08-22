package com.example.a2.presentation

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
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
import com.example.a2.presentation.theme.A2Theme
import java.util.Locale

class SensorActivity : ComponentActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null

    private var xValue by mutableStateOf<Float?>(null)
    private var yValue by mutableStateOf<Float?>(null)
    private var zValue by mutableStateOf<Float?>(null)
    private var statusText by mutableStateOf("")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        statusText = if (accelerometer == null) {
            getString(R.string.status_unavailable)
        } else {
            getString(R.string.waiting_sensor)
        }

        setContent {
            SensorScreen(
                x = xValue,
                y = yValue,
                z = zValue,
                status = statusText,
                onBack = { finish() }
            )
        }
    }

    override fun onStart() {
        super.onStart()
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onStop() {
        super.onStop()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            xValue = event.values[0]
            yValue = event.values[1]
            zValue = event.values[2]
            statusText = getString(R.string.status_active)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not used
    }
}

@Composable
fun SensorScreen(
    x: Float?,
    y: Float?,
    z: Float?,
    status: String,
    onBack: () -> Unit
) {
    A2Theme {
        AppScaffold {
            val listState = rememberTransformingLazyColumnState()
            val transformationSpec = rememberTransformationSpec()
            ScreenScaffold(scrollState = listState) { contentPadding ->
                TransformingLazyColumn(
                    contentPadding = contentPadding,
                    state = listState
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
                    item {
                        Text(
                            text = stringResource(R.string.accelerometer),
                            modifier = Modifier.fillMaxWidth(),
                            style = MaterialTheme.typography.labelMedium,
                            textAlign = TextAlign.Center
                        )
                    }
                    item {
                        Text(
                            text = x?.let { stringResource(R.string.label_x, String.format(Locale.US, "%.2f", it)) } ?: "",
                            modifier = Modifier.fillMaxWidth(),
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                    }
                    item {
                        Text(
                            text = y?.let { stringResource(R.string.label_y, String.format(Locale.US, "%.2f", it)) } ?: "",
                            modifier = Modifier.fillMaxWidth(),
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                    }
                    item {
                        Text(
                            text = z?.let { stringResource(R.string.label_z, String.format(Locale.US, "%.2f", it)) } ?: "",
                            modifier = Modifier.fillMaxWidth(),
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                    }
                    item {
                        Text(
                            text = status,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("SensorStatusText"),
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center
                        )
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

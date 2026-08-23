package com.example.a2.presentation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.ListHeaderDefaults
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.ScreenScaffoldDefaults
import androidx.wear.compose.material3.ScrollIndicator
import androidx.wear.compose.material3.SurfaceTransformation
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.TimeText
import androidx.wear.compose.material3.lazy.rememberTransformationSpec
import androidx.wear.compose.material3.lazy.transformedHeight
import com.example.a2.R
import com.example.a2.data.health.HealthServicesRepository
import com.example.a2.data.health.HeartRateState
import com.example.a2.data.sensor.AndroidSensorRepository
import com.example.a2.data.sensor.SensorReading
import com.example.a2.data.sensor.SensorRepository
import com.example.a2.presentation.theme.A2Theme
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.Locale

class SensorActivity : ComponentActivity() {

    private lateinit var sensorRepository: SensorRepository
    private lateinit var healthRepository: HealthServicesRepository
    private var heartRateJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sensorRepository = AndroidSensorRepository(applicationContext)
        healthRepository = HealthServicesRepository(applicationContext)

        setContent {
            val accel by sensorRepository.accelerometer.collectAsState()
            val gyro by sensorRepository.gyroscope.collectAsState()
            val mag by sensorRepository.magneticField.collectAsState()
            val hrState by healthRepository.heartRateState.collectAsState()

            SensorScreen(
                readings = listOf(accel, gyro, mag),
                hrState = hrState,
                onRequestPermission = { requestHeartRatePermission() }
            )
        }
    }

    // ComponentActivity includes the ActivityResult API support needed; Fragment is not required.
    @android.annotation.SuppressLint("InvalidFragmentVersionForActivityResult")
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startHeartRateMonitoring()
        } else {
            healthRepository.onPermissionDenied()
        }
    }

    private fun requestHeartRatePermission() {
        val permission = if (Build.VERSION.SDK_INT >= 36) {
            "android.permission.health.READ_HEART_RATE"
        } else {
            Manifest.permission.BODY_SENSORS
        }
        permissionLauncher.launch(permission)
    }

    private fun startHeartRateMonitoring() {
        if (heartRateJob?.isActive == true) return
        heartRateJob = lifecycleScope.launch {
            healthRepository.start()
        }
    }

    override fun onStart() {
        super.onStart()
        sensorRepository.start()
        
        val permission = if (Build.VERSION.SDK_INT >= 36) {
            "android.permission.health.READ_HEART_RATE"
        } else {
            Manifest.permission.BODY_SENSORS
        }

        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            startHeartRateMonitoring()
        } else {
            healthRepository.onPermissionDenied()
        }
    }

    override fun onStop() {
        super.onStop()
        sensorRepository.stop()
        heartRateJob?.cancel()
        healthRepository.stop()
    }
}

@Composable
fun SensorScreen(
    readings: List<SensorReading>,
    hrState: HeartRateState,
    onRequestPermission: () -> Unit
) {
    LaunchedEffect(Unit) {
        if (hrState is HeartRateState.PermissionRequired || hrState is HeartRateState.Waiting) {
            onRequestPermission()
        }
    }

    A2Theme {
        AppScaffold(timeText = { TimeText() }) {
            val listState = rememberTransformingLazyColumnState()
            val transformationSpec = rememberTransformationSpec()
            ScreenScaffold(
                scrollIndicator = { ScrollIndicator(listState) },
                contentPadding = ScreenScaffoldDefaults.contentPadding
            ) { scaffoldPadding ->
                TransformingLazyColumn(
                    contentPadding = scaffoldPadding,
                    state = listState,
                    modifier = Modifier
                        .testTag("SensorList")
                        .graphicsLayer { alpha = 0.99f }
                        .drawWithContent {
                            drawContent()
                            drawRect(
                                brush = Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Color.Black),
                                    startY = 0f,
                                    endY = 40.dp.toPx()
                                ),
                                blendMode = BlendMode.DstIn
                            )
                        }
                ) {
                    item {
                        ListHeader(
                            modifier = Modifier
                                .minimumVerticalContentPadding(
                                    ListHeaderDefaults.minimumTopListContentPadding,
                                    ListHeaderDefaults.minimumBottomListContentPadding
                                )
                                .fillMaxWidth()
                                .transformedHeight(this, transformationSpec)
                                .testTag("SensorHeading"),
                            transformation = SurfaceTransformation(transformationSpec),
                        ) {
                            Text(
                                text = stringResource(R.string.sensor_heading),
                                style = MaterialTheme.typography.titleMedium,
                                textAlign = TextAlign.Center,
                                color = Color(0xFF8A2BE2)
                            )
                        }
                    }

                    item {
                        Column {
                            HeartRateSection(hrState)
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }

                    readings.forEach { reading ->
                        item {
                            Column {
                                SensorSection(reading)
                                Spacer(modifier = Modifier.height(16.dp))
                            }
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
            style = MaterialTheme.typography.titleSmall,
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
            style = MaterialTheme.typography.labelSmall,
            textAlign = TextAlign.Center,
            color = if (reading.status == stringResource(R.string.status_active)) 
                Color.Green 
            else 
                Color.Red
        )
    }
}

@Composable
fun HeartRateSection(state: HeartRateState) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.heart_rate),
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.titleSmall,
            textAlign = TextAlign.Center
        )
        
        val statusText = when (state) {
            is HeartRateState.Active -> String.format(Locale.US, "%.1f %s", state.bpm, stringResource(R.string.unit_bpm))
            HeartRateState.Measuring -> stringResource(R.string.status_measuring)
            HeartRateState.PermissionRequired -> stringResource(R.string.status_permission_required)
            HeartRateState.Unsupported -> stringResource(R.string.status_unsupported)
            HeartRateState.Unavailable -> stringResource(R.string.status_unavailable)
            HeartRateState.Waiting -> stringResource(R.string.waiting_sensor)
            is HeartRateState.Error -> state.message
        }

        Text(
            text = statusText,
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            color = Color(0xFFFFA500)
        )
    }
}

@Composable
fun SensorValue(label: String, value: Float, unit: String) {
    Text(
        text = String.format(Locale.US, "%s: %.2f %s", label, value, unit),
        modifier = Modifier.fillMaxWidth(),
        style = MaterialTheme.typography.bodySmall,
        textAlign = TextAlign.Center
    )
}

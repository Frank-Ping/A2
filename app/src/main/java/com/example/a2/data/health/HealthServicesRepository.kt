package com.example.a2.data.health

import android.content.ComponentName
import android.content.Context
import androidx.concurrent.futures.await
import androidx.health.services.client.HealthServices
import androidx.health.services.client.MeasureCallback
import androidx.health.services.client.data.Availability
import androidx.health.services.client.data.DataPointContainer
import androidx.health.services.client.data.DataType
import androidx.health.services.client.data.DataTypeAvailability
import androidx.health.services.client.data.DeltaDataType
import androidx.wear.watchface.complications.datasource.ComplicationDataSourceUpdateRequester
import androidx.wear.tiles.TileService
import com.example.a2.data.shared.LatestMetricStore
import com.example.a2.wear.complication.MetricComplicationService
import com.example.a2.wear.tile.MetricTileService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.coroutines.cancellation.CancellationException

sealed class HeartRateState {
    object PermissionRequired : HeartRateState()
    object Unsupported : HeartRateState()
    object Waiting : HeartRateState()
    object Measuring : HeartRateState()
    object Unavailable : HeartRateState()
    data class Active(val bpm: Double) : HeartRateState()
    data class Error(val message: String) : HeartRateState()
}

class HealthServicesRepository(private val context: Context) {
    private val measureClient = HealthServices.getClient(context.applicationContext).measureClient
    private val store = LatestMetricStore(context.applicationContext)
    
    private val _heartRateState = MutableStateFlow<HeartRateState>(HeartRateState.Waiting)
    val heartRateState: StateFlow<HeartRateState> = _heartRateState.asStateFlow()

    private var isRegistered = false
    private val registerMutex = Mutex()

    private val callback = object : MeasureCallback {
        override fun onAvailabilityChanged(dataType: DeltaDataType<*, *>, availability: Availability) {
            if (availability is DataTypeAvailability) {
                when (availability) {
                    DataTypeAvailability.AVAILABLE -> {
                        if (_heartRateState.value !is HeartRateState.Active) {
                            _heartRateState.update { HeartRateState.Measuring }
                        }
                    }
                    DataTypeAvailability.ACQUIRING -> {
                        _heartRateState.update { HeartRateState.Measuring }
                    }
                    DataTypeAvailability.UNAVAILABLE,
                    DataTypeAvailability.UNAVAILABLE_DEVICE_OFF_BODY -> {
                        _heartRateState.update { HeartRateState.Unavailable }
                    }
                }
            }
        }

        override fun onDataReceived(data: DataPointContainer) {
            val heartRateSamples = data.getData(DataType.HEART_RATE_BPM)
            val newestSample = heartRateSamples.maxByOrNull { it.timeDurationFromBoot }
            newestSample?.let { sample ->
                _heartRateState.update { HeartRateState.Active(sample.value) }
                store.saveHeartRate(sample.value, "bpm", "Active")
                updateExternalInterfaces()
            }
        }
    }

    private fun updateExternalInterfaces() {
        TileService.getUpdater(context)
            .requestUpdate(MetricTileService::class.java)
        
        ComplicationDataSourceUpdateRequester.create(
            context,
            ComponentName(context, MetricComplicationService::class.java)
        ).requestUpdateAll()
    }

    suspend fun start() {
        if (isRegistered) return
        registerMutex.withLock {
            if (isRegistered) return
            try {
                val capabilities = measureClient.getCapabilitiesAsync().await()
                if (DataType.HEART_RATE_BPM !in capabilities.supportedDataTypesMeasure) {
                    _heartRateState.update { HeartRateState.Unsupported }
                    return
                }
                
                measureClient.registerMeasureCallback(DataType.HEART_RATE_BPM, callback)
                isRegistered = true
                if (_heartRateState.value !is HeartRateState.Active) {
                    _heartRateState.update { HeartRateState.Measuring }
                }
            } catch (e: SecurityException) {
                _heartRateState.update { HeartRateState.PermissionRequired }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _heartRateState.update { HeartRateState.Error(e.message ?: "Unknown error") }
            }
        }
    }

    fun stop() {
        if (!isRegistered) return
        measureClient.unregisterMeasureCallbackAsync(DataType.HEART_RATE_BPM, callback)
        isRegistered = false
        _heartRateState.update { HeartRateState.Waiting }
    }

    fun onPermissionDenied() {
        _heartRateState.update { HeartRateState.PermissionRequired }
    }
}

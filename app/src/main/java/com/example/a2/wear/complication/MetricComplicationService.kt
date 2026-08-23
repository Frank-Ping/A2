package com.example.a2.wear.complication

import android.app.PendingIntent
import android.content.Intent
import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.data.PlainComplicationText
import androidx.wear.watchface.complications.data.ShortTextComplicationData
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import androidx.wear.watchface.complications.datasource.ComplicationDataSourceService
import com.example.a2.data.shared.LatestMetricStore
import com.example.a2.presentation.SensorActivity
import java.util.Locale

class MetricComplicationService : ComplicationDataSourceService() {
    override fun getPreviewData(type: ComplicationType): ComplicationData? {
        if (type != ComplicationType.SHORT_TEXT) return null
        return ShortTextComplicationData.Builder(
            text = PlainComplicationText.Builder("72").build(),
            contentDescription = PlainComplicationText.Builder("Heart Rate").build()
        )
        .setTitle(PlainComplicationText.Builder("bpm").build())
        .build()
    }

    override fun onComplicationRequest(
        request: ComplicationRequest,
        listener: ComplicationRequestListener
    ) {
        val store = LatestMetricStore(this)
        val bpm = store.getHeartRateBpm()
        
        val text = if (bpm != null) {
            String.format(Locale.US, "%.0f", bpm)
        } else {
            "?"
        }

        val intent = Intent(this, SensorActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val complicationData = ShortTextComplicationData.Builder(
            text = PlainComplicationText.Builder(text).build(),
            contentDescription = PlainComplicationText.Builder("Heart Rate").build()
        )
        .setTitle(PlainComplicationText.Builder("bpm").build())
        .setTapAction(pendingIntent)
        .build()

        listener.onComplicationData(complicationData)
    }
}

package com.example.a2.wear.tile

import androidx.wear.protolayout.ActionBuilders
import androidx.wear.protolayout.DimensionBuilders
import androidx.wear.protolayout.ColorBuilders
import androidx.wear.protolayout.LayoutElementBuilders
import androidx.wear.protolayout.ModifiersBuilders
import androidx.wear.protolayout.ResourceBuilders
import androidx.wear.protolayout.TimelineBuilders
import androidx.wear.protolayout.material.Text
import androidx.wear.protolayout.material.Typography
import androidx.wear.protolayout.material.layouts.PrimaryLayout
import androidx.wear.tiles.RequestBuilders
import androidx.wear.tiles.TileBuilders
import androidx.wear.tiles.TileService
import com.example.a2.R
import com.example.a2.data.shared.LatestMetricStore
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import java.util.Locale

class MetricTileService : TileService() {
    override fun onTileRequest(requestParams: RequestBuilders.TileRequest): ListenableFuture<TileBuilders.Tile> {
        val store = LatestMetricStore(this)
        val bpm = store.getHeartRateBpm()
        val text = if (bpm != null) {
            String.format(Locale.US, "%.0f bpm", bpm)
        } else {
            getString(R.string.waiting_sensor)
        }

        val clickable = ModifiersBuilders.Clickable.Builder()
            .setOnClick(
                ActionBuilders.LaunchAction.Builder()
                    .setAndroidActivity(
                        ActionBuilders.AndroidActivity.Builder()
                            .setPackageName(packageName)
                            .setClassName("com.example.a2.presentation.SensorActivity")
                            .build()
                    )
                    .build()
            )
            .build()

        val modifiers = ModifiersBuilders.Modifiers.Builder()
            .setClickable(clickable)
            .build()

        return Futures.immediateFuture(
            TileBuilders.Tile.Builder()
                .setResourcesVersion("1")
                .setTileTimeline(
                    TimelineBuilders.Timeline.Builder()
                        .addTimelineEntry(
                            TimelineBuilders.TimelineEntry.Builder()
                                .setLayout(
                                    LayoutElementBuilders.Layout.Builder()
                                        .setRoot(
                                            LayoutElementBuilders.Box.Builder()
                                                .setWidth(DimensionBuilders.expand())
                                                .setHeight(DimensionBuilders.expand())
                                                .addContent(
                                                    PrimaryLayout.Builder(requestParams.deviceConfiguration)
                                                        .setResponsiveContentInsetEnabled(true)
                                                        .setContent(
                                                            Text.Builder(this, text)
                                                                .setTypography(if (bpm != null) Typography.TYPOGRAPHY_TITLE1 else Typography.TYPOGRAPHY_BODY1)
                                                                .setMaxLines(3)
                                                                .setMultilineAlignment(LayoutElementBuilders.TEXT_ALIGN_CENTER)
                                                                .setColor(ColorBuilders.argb(0xFFFFFFFF.toInt()))
                                                                .build()
                                                        )
                                                        .build()
                                                )
                                                .setModifiers(modifiers)
                                                .build()
                                        )
                                        .build()
                                )
                                .build()
                        )
                        .build()
                )
                .build()
        )
    }

    override fun onTileResourcesRequest(requestParams: RequestBuilders.ResourcesRequest): ListenableFuture<ResourceBuilders.Resources> {
        return Futures.immediateFuture(
            ResourceBuilders.Resources.Builder()
                .setVersion("1")
                .build()
        )
    }
}

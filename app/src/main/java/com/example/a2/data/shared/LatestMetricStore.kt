package com.example.a2.data.shared

import android.content.Context
import android.content.SharedPreferences

class LatestMetricStore(private val prefs: SharedPreferences) {
    
    constructor(context: Context) : this(context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE))

    fun saveHeartRate(bpm: Double, unit: String, status: String) {
        prefs.edit()
            .putFloat(KEY_BPM, bpm.toFloat())
            .putString(KEY_UNIT, unit)
            .putString(KEY_STATUS, status)
            .putLong(KEY_TIMESTAMP, System.currentTimeMillis())
            .apply()
    }

    fun getHeartRateBpm(): Float? {
        if (!prefs.contains(KEY_BPM)) return null
        return prefs.getFloat(KEY_BPM, 0f)
    }

    fun getHeartRateStatus(): String? {
        return prefs.getString(KEY_STATUS, null)
    }

    fun getHeartRateTimestamp(): Long {
        return prefs.getLong(KEY_TIMESTAMP, 0L)
    }

    companion object {
        private const val PREFS_NAME = "latest_metrics"
        private const val KEY_BPM = "hr_bpm"
        private const val KEY_UNIT = "hr_unit"
        private const val KEY_STATUS = "hr_status"
        private const val KEY_TIMESTAMP = "hr_timestamp"
    }
}

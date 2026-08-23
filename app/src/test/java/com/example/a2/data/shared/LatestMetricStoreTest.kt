package com.example.a2.data.shared

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.util.concurrent.ConcurrentHashMap

class LatestMetricStoreTest {

    private lateinit var store: LatestMetricStore
    private val fakePrefs = FakeSharedPreferences()

    @Before
    fun setup() {
        store = LatestMetricStore(fakePrefs)
    }

    @Test
    fun testInitialStateIsNull() {
        assertNull(store.getHeartRateBpm())
        assertNull(store.getHeartRateStatus())
        assertEquals(0L, store.getHeartRateTimestamp())
    }

    @Test
    fun testSaveAndRetrieve() {
        val bpm = 72.5
        store.saveHeartRate(bpm, "bpm", "Active")
        
        assertEquals(72.5f, store.getHeartRateBpm()!!, 0.1f)
        assertEquals("Active", store.getHeartRateStatus())
        assert(store.getHeartRateTimestamp() > 0)
    }

    // Minimal fake implementation for testing
    private class FakeSharedPreferences : android.content.SharedPreferences {
        private val data = ConcurrentHashMap<String, Any?>()

        override fun getAll(): Map<String, *> = data
        override fun getString(key: String, defValue: String?): String? = data[key] as? String ?: defValue
        override fun getStringSet(key: String, defValues: Set<String>?): Set<String>? = data[key] as? Set<String> ?: defValues
        override fun getInt(key: String, defValue: Int): Int = data[key] as? Int ?: defValue
        override fun getLong(key: String, defValue: Long): Long = data[key] as? Long ?: defValue
        override fun getFloat(key: String, defValue: Float): Float = data[key] as? Float ?: defValue
        override fun getBoolean(key: String, defValue: Boolean): Boolean = data[key] as? Boolean ?: defValue
        override fun contains(key: String): Boolean = data.containsKey(key)
        override fun registerOnSharedPreferenceChangeListener(listener: android.content.SharedPreferences.OnSharedPreferenceChangeListener?) {}
        override fun unregisterOnSharedPreferenceChangeListener(listener: android.content.SharedPreferences.OnSharedPreferenceChangeListener?) {}

        override fun edit(): android.content.SharedPreferences.Editor = FakeEditor(data)

        private class FakeEditor(private val data: MutableMap<String, Any?>) : android.content.SharedPreferences.Editor {
            private val temp = mutableMapOf<String, Any?>()
            override fun putString(key: String, value: String?): android.content.SharedPreferences.Editor { temp[key] = value; return this }
            override fun putStringSet(key: String, values: Set<String>?): android.content.SharedPreferences.Editor { temp[key] = values; return this }
            override fun putInt(key: String, value: Int): android.content.SharedPreferences.Editor { temp[key] = value; return this }
            override fun putLong(key: String, value: Long): android.content.SharedPreferences.Editor { temp[key] = value; return this }
            override fun putFloat(key: String, value: Float): android.content.SharedPreferences.Editor { temp[key] = value; return this }
            override fun putBoolean(key: String, value: Boolean): android.content.SharedPreferences.Editor { temp[key] = value; return this }
            override fun remove(key: String): android.content.SharedPreferences.Editor { temp[key] = null; return this }
            override fun clear(): android.content.SharedPreferences.Editor { data.clear(); return this }
            override fun commit(): Boolean { apply(); return true }
            override fun apply() {
                temp.forEach { (k, v) -> if (v == null) data.remove(k) else data[k] = v }
            }
        }
    }
}

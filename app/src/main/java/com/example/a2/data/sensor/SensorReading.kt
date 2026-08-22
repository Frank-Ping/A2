package com.example.a2.data.sensor

data class SensorReading(
    val id: Int,
    val name: String,
    val x: Float? = null,
    val y: Float? = null,
    val z: Float? = null,
    val unit: String,
    val status: String,
    val timestamp: Long = System.currentTimeMillis()
)

package com.example.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wifi_sessions")
data class WifiSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sessionId: String,
    val timestamp: Long,
    val ssid: String,
    val bssid: String,
    val rssi: Int,
    val frequencyMhz: Int,
    val linkSpeedMbps: Int,
    val qualityLevel: String,
    val status: String,
    val dataSourceType: String,
    val userLabel: String? = null
)

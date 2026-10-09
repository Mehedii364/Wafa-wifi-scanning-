package com.example.core.common

import kotlin.math.max
import kotlin.math.min

enum class SignalQualityLevel {
    EXCELLENT,
    GOOD,
    FAIR,
    POOR,
    UNUSABLE,
    UNKNOWN
}

enum class WifiBand(val displayName: String) {
    BAND_2_4_GHZ("2.4 GHz"),
    BAND_5_GHZ("5 GHz"),
    BAND_6_GHZ("6 GHz (Wi-Fi 6E/7)"),
    BAND_60_GHZ("60 GHz (WiGig)"),
    BAND_UNKNOWN("Unknown Band")
}

data class SignalClassification(
    val level: SignalQualityLevel,
    val scorePercentage: Int,
    val description: String
)

object WifiUtils {

    /**
     * Accurately maps Wi-Fi frequency (MHz) to channel number according to IEEE 802.11 specs.
     */
    fun frequencyToChannel(frequencyMhz: Int): Int? {
        return when {
            // 2.4 GHz band (Channels 1 to 13: 2412 MHz + (channel - 1) * 5)
            frequencyMhz in 2412..2472 -> ((frequencyMhz - 2412) / 5) + 1
            // 2.4 GHz Channel 14 (Japan)
            frequencyMhz == 2484 -> 14
            // 5 GHz band (5170 MHz to 5825 MHz)
            frequencyMhz in 5170..5835 -> (frequencyMhz - 5000) / 5
            // 6 GHz band (Wi-Fi 6E / Wi-Fi 7: 5955 MHz to 7115 MHz)
            frequencyMhz in 5955..7115 -> ((frequencyMhz - 5950) / 5)
            else -> null
        }
    }

    /**
     * Determines Wi-Fi frequency band.
     */
    fun frequencyToBand(frequencyMhz: Int): WifiBand {
        return when {
            frequencyMhz in 2400..2500 -> WifiBand.BAND_2_4_GHZ
            frequencyMhz in 4900..5895 -> WifiBand.BAND_5_GHZ
            frequencyMhz in 5925..7125 -> WifiBand.BAND_6_GHZ
            frequencyMhz in 58320..70200 -> WifiBand.BAND_60_GHZ
            else -> WifiBand.BAND_UNKNOWN
        }
    }

    /**
     * Classifies measured RSSI into a standard 5-tier quality rating.
     */
    fun classifyRssi(rssi: Int): SignalClassification {
        return when {
            rssi == 0 || rssi < -110 || rssi > 0 -> SignalClassification(
                level = SignalQualityLevel.UNKNOWN,
                scorePercentage = 0,
                description = "Unavailable / Out of Range"
            )
            rssi >= -50 -> SignalClassification(
                level = SignalQualityLevel.EXCELLENT,
                scorePercentage = min(100, 90 + ((rssi + 50) * 2)),
                description = "Optimal signal. Ideal for high-bandwidth & low-latency tasks."
            )
            rssi >= -65 -> SignalClassification(
                level = SignalQualityLevel.GOOD,
                scorePercentage = 70 + ((rssi + 65) * 20 / 15),
                description = "Strong signal. Suitable for HD streaming, video calls, gaming."
            )
            rssi >= -75 -> SignalClassification(
                level = SignalQualityLevel.FAIR,
                scorePercentage = 45 + ((rssi + 75) * 25 / 10),
                description = "Moderate signal. Minor packet drops possible in congested areas."
            )
            rssi >= -85 -> SignalClassification(
                level = SignalQualityLevel.POOR,
                scorePercentage = 20 + ((rssi + 85) * 25 / 10),
                description = "Weak signal. High packet loss, sluggish browsing."
            )
            else -> SignalClassification(
                level = SignalQualityLevel.UNUSABLE,
                scorePercentage = max(5, 5 + ((rssi + 100) * 15 / 15)),
                description = "Severely degraded. Disconnection probable."
            )
        }
    }

    /**
     * Privacy preservation: masks sensitive SSID and BSSID identifiers.
     */
    fun maskSsid(ssid: String?): String {
        if (ssid.isNullOrBlank() || ssid == "<unknown ssid>") return "[Hidden Network]"
        val clean = ssid.removeSurrounding("\"")
        if (clean.length <= 3) return "${clean.take(1)}***"
        return "${clean.take(2)}***${clean.takeLast(2)}"
    }

    fun maskBssid(bssid: String?): String {
        if (bssid.isNullOrBlank()) return "[Masked MAC]"
        val parts = bssid.split(":")
        if (parts.size != 6) return "[Masked MAC]"
        return "${parts[0]}:${parts[1]}:**:**:**:${parts[5]}"
    }

    /**
     * Converts dBm to qualitative color hex code for dark obsidian themes.
     */
    fun getRssiColorHex(rssi: Int): Long {
        return when {
            rssi >= -55 -> 0xFF00E5FF // Cyan
            rssi >= -68 -> 0xFF10B981 // Emerald Green
            rssi >= -78 -> 0xFFF59E0B // Amber
            rssi >= -88 -> 0xFFEF4444 // Crimson
            else -> 0xFF6B7280        // Slate / Unavailable
        }
    }
}

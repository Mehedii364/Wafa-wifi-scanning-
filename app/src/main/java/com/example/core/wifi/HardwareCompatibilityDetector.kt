package com.example.core.wifi

import android.content.Context
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.os.Build
import androidx.core.content.ContextCompat

enum class CompatibilitySupportLevel {
    SUPPORTED,
    SUPPORTED_WITH_RESTRICTIONS,
    EXPERIMENTAL,
    REQUIRES_EXTERNAL_HARDWARE,
    UNSUPPORTED,
    NOT_YET_VERIFIED
}

data class FeatureCompatibility(
    val featureName: String,
    val supportLevel: CompatibilitySupportLevel,
    val details: String,
    val requirementNotes: String
)

data class DeviceHardwareProfile(
    val manufacturer: String,
    val model: String,
    val deviceCodeName: String,
    val isOppoReno13F: Boolean,
    val androidVersion: String,
    val sdkInt: Int,
    val hasWifiHardware: Boolean,
    val is5GhzSupported: Boolean,
    val isP2pSupported: Boolean,
    val isRttSupported: Boolean,
    val isCsiSupportedNatively: Boolean,
    val features: List<FeatureCompatibility>
)

object HardwareCompatibilityDetector {

    fun detect(context: Context): DeviceHardwareProfile {
        val pm = context.packageManager
        val wm = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager

        val manufacturer = Build.MANUFACTURER
        val model = Build.MODEL
        val device = Build.DEVICE

        // Check if device matches or is compatible with OPPO Reno13 F 4G (CPH2701)
        val isOppoReno = manufacturer.equals("OPPO", ignoreCase = true) &&
                (model.contains("CPH2701", ignoreCase = true) || model.contains("Reno13", ignoreCase = true))

        val hasWifi = pm.hasSystemFeature(PackageManager.FEATURE_WIFI)
        val is5Ghz = wm?.is5GHzBandSupported == true
        val isP2p = pm.hasSystemFeature(PackageManager.FEATURE_WIFI_DIRECT)
        val isRtt = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            pm.hasSystemFeature(PackageManager.FEATURE_WIFI_RTT)
        } else false

        // CSI is NOT exposed in standard Android HAL / AOSP without root/Nexmon/patches or external ESP32
        val isCsiNative = false

        val featureList = listOf(
            FeatureCompatibility(
                featureName = "Wi-Fi RSSI Monitoring",
                supportLevel = if (hasWifi) CompatibilitySupportLevel.SUPPORTED else CompatibilitySupportLevel.UNSUPPORTED,
                details = "Periodic polling via WifiManager connection info and NetworkCapabilities.",
                requirementNotes = "ACCESS_WIFI_STATE permission."
            ),
            FeatureCompatibility(
                featureName = "Nearby Wi-Fi AP Scanning",
                supportLevel = if (hasWifi) CompatibilitySupportLevel.SUPPORTED_WITH_RESTRICTIONS else CompatibilitySupportLevel.UNSUPPORTED,
                details = "Subject to OS scan throttling (4 scans per 2 minutes in foreground).",
                requirementNotes = "Requires ACCESS_FINE_LOCATION and NEARBY_WIFI_DEVICES on Android 13+."
            ),
            FeatureCompatibility(
                featureName = "5 GHz Dual-Band Spectrum",
                supportLevel = if (is5Ghz) CompatibilitySupportLevel.SUPPORTED else CompatibilitySupportLevel.UNSUPPORTED,
                details = "Channels 36 to 165 (5.1 GHz - 5.8 GHz).",
                requirementNotes = "Device RF hardware capability."
            ),
            FeatureCompatibility(
                featureName = "Wi-Fi RTT (802.11mc Fine Time Measurement)",
                supportLevel = if (isRtt) CompatibilitySupportLevel.SUPPORTED else CompatibilitySupportLevel.UNSUPPORTED,
                details = "Nanosecond round-trip time for sub-meter indoor ranging.",
                requirementNotes = "Hardware RTT chip & compatible 802.11mc Access Point."
            ),
            FeatureCompatibility(
                featureName = "Experimental RF Amplitude Variance",
                supportLevel = CompatibilitySupportLevel.EXPERIMENTAL,
                details = "Statistical baseline & sliding window fluctuation index.",
                requirementNotes = "Subject to environmental multipath, device mobility, and AP power control."
            ),
            FeatureCompatibility(
                featureName = "Raw 802.11 Channel State Information (CSI)",
                supportLevel = CompatibilitySupportLevel.REQUIRES_EXTERNAL_HARDWARE,
                details = "Subcarrier-level amplitude & phase matrices are blocked by Android HAL.",
                requirementNotes = "Requires external ESP32-S3 Wi-Fi sniffer, Nexmon-patched Broadcom/Cypress chip, or Atheros Linux adapter via USB OTG."
            ),
            FeatureCompatibility(
                featureName = "Room Database Local Persistence",
                supportLevel = CompatibilitySupportLevel.SUPPORTED,
                details = "SQLite SQLite3 on-device encrypted sandbox with no remote leaks.",
                requirementNotes = "Fully offline compatible."
            ),
            FeatureCompatibility(
                featureName = "Data Export (CSV & JSON)",
                supportLevel = CompatibilitySupportLevel.SUPPORTED,
                details = "Standard Android Storage Access Framework & share sheet.",
                requirementNotes = "No external server required."
            )
        )

        return DeviceHardwareProfile(
            manufacturer = manufacturer,
            model = model,
            deviceCodeName = device,
            isOppoReno13F = isOppoReno,
            androidVersion = Build.VERSION.RELEASE ?: "Unknown",
            sdkInt = Build.VERSION.SDK_INT,
            hasWifiHardware = hasWifi,
            is5GhzSupported = is5Ghz,
            isP2pSupported = isP2p,
            isRttSupported = isRtt,
            isCsiSupportedNatively = isCsiNative,
            features = featureList
        )
    }
}

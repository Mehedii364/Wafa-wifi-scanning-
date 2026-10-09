package com.example.core.wifi

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.ScanResult
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.os.SystemClock
import com.example.core.common.WifiBand
import com.example.core.common.WifiUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.net.InetAddress
import java.nio.ByteBuffer
import java.nio.ByteOrder

data class CurrentWifiState(
    val isConnected: Boolean = false,
    val isWifiEnabled: Boolean = false,
    val ssid: String = "<Not Connected>",
    val bssid: String = "--:--:--:--:--:--",
    val rssi: Int = 0,
    val frequencyMhz: Int = 0,
    val linkSpeedMbps: Int = 0,
    val ipAddress: String = "0.0.0.0",
    val timestamp: Long = 0L
)

data class ScannedNetwork(
    val ssid: String,
    val bssid: String,
    val rssi: Int,
    val frequencyMhz: Int,
    val channel: Int?,
    val band: WifiBand,
    val capabilities: String,
    val timestamp: Long
)

class WifiMonitor(private val context: Context) {

    private val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
    private val connectivityManager = context.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager

    private val _currentConnection = MutableStateFlow(CurrentWifiState())
    val currentConnection: StateFlow<CurrentWifiState> = _currentConnection.asStateFlow()

    private val _scanResults = MutableStateFlow<List<ScannedNetwork>>(emptyList())
    val scanResults: StateFlow<List<ScannedNetwork>> = _scanResults.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _lastScanTime = MutableStateFlow(0L)
    val lastScanTime: StateFlow<Long> = _lastScanTime.asStateFlow()

    private val _scanMessage = MutableStateFlow<String?>(null)
    val scanMessage: StateFlow<String?> = _scanMessage.asStateFlow()

    private val _isMonitoring = MutableStateFlow(false)
    val isMonitoring: StateFlow<Boolean> = _isMonitoring.asStateFlow()

    private var monitorJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO)
    private var lastScanTriggerTime = 0L

    private val scanReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == WifiManager.SCAN_RESULTS_AVAILABLE_ACTION) {
                _isScanning.value = false
                val success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false)
                readScanResults(success)
            }
        }
    }

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            refreshCurrentConnection()
        }

        override fun onLost(network: Network) {
            refreshCurrentConnection()
        }

        override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
            refreshCurrentConnection()
        }
    }

    init {
        try {
            val filter = IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
            context.registerReceiver(scanReceiver, filter)
        } catch (_: Exception) {}

        try {
            val request = NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .build()
            connectivityManager?.registerNetworkCallback(request, networkCallback)
        } catch (_: Exception) {}

        refreshCurrentConnection()
    }

    @SuppressLint("MissingPermission")
    fun refreshCurrentConnection() {
        val wm = wifiManager ?: run {
            _currentConnection.value = CurrentWifiState()
            return
        }

        val isEnabled = try { wm.isWifiEnabled } catch (_: Exception) { false }

        var wifiInfo: WifiInfo? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val activeNetwork = connectivityManager?.activeNetwork
            val capabilities = connectivityManager?.getNetworkCapabilities(activeNetwork)
            if (capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true) {
                wifiInfo = capabilities.transportInfo as? WifiInfo
            }
        }

        if (wifiInfo == null) {
            @Suppress("DEPRECATION")
            wifiInfo = try { wm.connectionInfo } catch (_: Exception) { null }
        }

        if (wifiInfo != null && (wifiInfo.networkId != -1 || wifiInfo.rssi > -127)) {
            val rawSsid = wifiInfo.ssid ?: "<unknown ssid>"
            val cleanSsid = if (rawSsid == "<unknown ssid>" || rawSsid.isBlank()) "Connected Wi-Fi" else rawSsid.removeSurrounding("\"")
            val bssid = wifiInfo.bssid ?: "--:--:--:--:--:--"
            val rssi = wifiInfo.rssi
            val freq = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) wifiInfo.frequency else 0
            val speed = wifiInfo.linkSpeed
            val ip = formatIp(wifiInfo.ipAddress)

            _currentConnection.value = CurrentWifiState(
                isConnected = true,
                isWifiEnabled = isEnabled,
                ssid = cleanSsid,
                bssid = bssid,
                rssi = rssi,
                frequencyMhz = freq,
                linkSpeedMbps = speed,
                ipAddress = ip,
                timestamp = System.currentTimeMillis()
            )
        } else {
            _currentConnection.value = CurrentWifiState(
                isConnected = false,
                isWifiEnabled = isEnabled,
                ssid = "<Not Connected>",
                bssid = "--:--:--:--:--:--",
                rssi = 0,
                frequencyMhz = 0,
                linkSpeedMbps = 0,
                ipAddress = "0.0.0.0",
                timestamp = System.currentTimeMillis()
            )
        }
    }

    @SuppressLint("MissingPermission")
    fun triggerScan(): Boolean {
        val wm = wifiManager ?: return false
        val now = SystemClock.elapsedRealtime()

        // Scan throttling check (Android restricts foreground apps to 4 scans per 2 minutes)
        if (now - lastScanTriggerTime < 10000) {
            val waitSec = (10000 - (now - lastScanTriggerTime)) / 1000
            _scanMessage.value = "Scan rate-limited by Android OS. Please wait ${waitSec}s."
            return false
        }

        lastScanTriggerTime = now
        _isScanning.value = true
        _scanMessage.value = null

        @Suppress("DEPRECATION")
        val success = try {
            wm.startScan()
        } catch (e: Exception) {
            _isScanning.value = false
            _scanMessage.value = "Scan request failed: ${e.localizedMessage}"
            false
        }

        if (!success) {
            _isScanning.value = false
            _scanMessage.value = "Wi-Fi scan throttled or unavailable on device. Loading cached scan."
            readScanResults(false)
        }
        return success
    }

    @SuppressLint("MissingPermission")
    fun readScanResults(isUpdated: Boolean) {
        val wm = wifiManager ?: return
        val rawResults: List<ScanResult> = try {
            wm.scanResults ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }

        val mapped = rawResults.map { result ->
            val channel = WifiUtils.frequencyToChannel(result.frequency)
            val band = WifiUtils.frequencyToBand(result.frequency)
            val cleanSsid = if (result.SSID.isNullOrBlank()) "<Hidden SSID>" else result.SSID
            ScannedNetwork(
                ssid = cleanSsid,
                bssid = result.BSSID ?: "00:00:00:00:00:00",
                rssi = result.level,
                frequencyMhz = result.frequency,
                channel = channel,
                band = band,
                capabilities = result.capabilities ?: "Open",
                timestamp = System.currentTimeMillis()
            )
        }.sortedByDescending { it.rssi }

        _scanResults.value = mapped
        _lastScanTime.value = System.currentTimeMillis()
        if (!isUpdated && mapped.isNotEmpty()) {
            _scanMessage.value = "Displaying cached scan results (Android throttled real-time update)."
        }
    }

    fun startPeriodicMonitoring(intervalSeconds: Int, onTick: (CurrentWifiState) -> Unit) {
        monitorJob?.cancel()
        _isMonitoring.value = true
        monitorJob = scope.launch {
            while (isActive) {
                refreshCurrentConnection()
                val current = _currentConnection.value
                onTick(current)
                delay(intervalSeconds * 1000L)
            }
        }
    }

    fun stopMonitoring() {
        monitorJob?.cancel()
        monitorJob = null
        _isMonitoring.value = false
    }

    private fun formatIp(ip: Int): String {
        if (ip == 0) return "0.0.0.0"
        val bytes = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(ip).array()
        return try {
            InetAddress.getByAddress(bytes).hostAddress ?: "0.0.0.0"
        } catch (_: Exception) {
            "0.0.0.0"
        }
    }

    fun cleanup() {
        stopMonitoring()
        try {
            context.unregisterReceiver(scanReceiver)
        } catch (_: Exception) {}
        try {
            connectivityManager?.unregisterNetworkCallback(networkCallback)
        } catch (_: Exception) {}
    }
}

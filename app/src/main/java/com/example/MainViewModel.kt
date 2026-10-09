package com.example

import android.app.Application
import android.content.Context
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.common.AppLanguage
import com.example.core.common.EvidenceQuality
import com.example.core.common.ExperimentalSensingEngine
import com.example.core.common.LocalizationManager
import com.example.core.common.SensingState
import com.example.core.common.SensingStatus
import com.example.core.common.WifiUtils
import com.example.core.csi.CsiHardwareManager
import com.example.core.database.WafaDatabase
import com.example.core.database.WifiSessionEntity
import com.example.core.database.WifiSessionRepository
import com.example.core.database.WifiSessionRepositoryImpl
import com.example.core.export.DataExporter
import com.example.core.wifi.CurrentWifiState
import com.example.core.wifi.DeviceHardwareProfile
import com.example.core.wifi.HardwareCompatibilityDetector
import com.example.core.wifi.ScannedNetwork
import com.example.core.wifi.WifiMonitor
import com.example.ui.theme.AppThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

enum class NavScreen {
    DASHBOARD,
    ANALYZER,
    SCANNER,
    SENSING,
    CSI,
    HISTORY,
    COMPATIBILITY,
    DIAGNOSTICS,
    EXPORT,
    PRIVACY,
    SETTINGS,
    ABOUT
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = WafaDatabase.getInstance(application)
    val repository: WifiSessionRepository = WifiSessionRepositoryImpl(db.wifiSessionDao())
    val wifiMonitor = WifiMonitor(application)
    val csiManager = CsiHardwareManager()
    val sensingEngine = ExperimentalSensingEngine()

    // Navigation
    private val _currentScreen = MutableStateFlow(NavScreen.DASHBOARD)
    val currentScreen: StateFlow<NavScreen> = _currentScreen.asStateFlow()

    // Preferences & Settings
    private val _themeMode = MutableStateFlow(AppThemeMode.OBSIDIAN)
    val themeMode: StateFlow<AppThemeMode> = _themeMode.asStateFlow()

    private val _language = MutableStateFlow(AppLanguage.BENGALI)
    val language: StateFlow<AppLanguage> = _language.asStateFlow()

    private val _maskPii = MutableStateFlow(true)
    val maskPii: StateFlow<Boolean> = _maskPii.asStateFlow()

    private val _monitoringIntervalSec = MutableStateFlow(3)
    val monitoringIntervalSec: StateFlow<Int> = _monitoringIntervalSec.asStateFlow()

    private val _batterySaverMode = MutableStateFlow(false)
    val batterySaverMode: StateFlow<Boolean> = _batterySaverMode.asStateFlow()

    private val _hapticsEnabled = MutableStateFlow(true)
    val hapticsEnabled: StateFlow<Boolean> = _hapticsEnabled.asStateFlow()

    // Live Signal Trend Buffer (for charts)
    private val _signalTrend = MutableStateFlow<List<Int>>(emptyList())
    val signalTrend: StateFlow<List<Int>> = _signalTrend.asStateFlow()

    // Experimental Sensing State
    private val _sensingState = MutableStateFlow(
        SensingState(
            status = SensingStatus.INSUFFICIENT_DATA,
            baselineRssiMean = 0.0,
            baselineStdDev = 0.0,
            currentRssiMean = 0.0,
            currentStdDev = 0.0,
            deviationFromBaseline = 0.0,
            fluctuationIndex = 0.0,
            sampleCount = 0,
            evidenceQuality = EvidenceQuality.LOW,
            statusMessage = "Awaiting initial signal samples for baseline calibration..."
        )
    )
    val sensingState: StateFlow<SensingState> = _sensingState.asStateFlow()

    // Active session tracking
    private var activeSessionId: String = UUID.randomUUID().toString()
    private val _sessionStartTime = MutableStateFlow(0L)
    val sessionStartTime: StateFlow<Long> = _sessionStartTime.asStateFlow()

    // Hardware Profile
    val hardwareProfile: DeviceHardwareProfile = HardwareCompatibilityDetector.detect(application)

    // Room Database sessions flow
    val allSessions = repository.getAllSessions().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val sessionCount = repository.getSessionCount().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    init {
        // Initial connection check
        wifiMonitor.refreshCurrentConnection()
    }

    fun navigateTo(screen: NavScreen) {
        _currentScreen.value = screen
    }

    fun setThemeMode(mode: AppThemeMode) {
        _themeMode.value = mode
    }

    fun setLanguage(lang: AppLanguage) {
        _language.value = lang
    }

    fun toggleMaskPii() {
        _maskPii.value = !_maskPii.value
    }

    fun setMonitoringInterval(sec: Int) {
        _monitoringIntervalSec.value = sec.coerceIn(1, 30)
        if (wifiMonitor.isMonitoring.value) {
            startMonitoring()
        }
    }

    fun toggleBatterySaver() {
        _batterySaverMode.value = !_batterySaverMode.value
        if (_batterySaverMode.value) {
            _monitoringIntervalSec.value = 10
        } else {
            _monitoringIntervalSec.value = 3
        }
    }

    fun toggleHaptics() {
        _hapticsEnabled.value = !_hapticsEnabled.value
    }

    fun startMonitoring() {
        activeSessionId = UUID.randomUUID().toString()
        _sessionStartTime.value = System.currentTimeMillis()

        wifiMonitor.startPeriodicMonitoring(_monitoringIntervalSec.value) { current ->
            onNewMeasurement(current)
        }
    }

    fun stopMonitoring() {
        wifiMonitor.stopMonitoring()
    }

    private fun onNewMeasurement(current: CurrentWifiState) {
        if (!current.isConnected || current.rssi == 0) return

        // Update live sparkline buffer (keep last 40 points)
        val currentList = _signalTrend.value.toMutableList()
        currentList.add(current.rssi)
        if (currentList.size > 40) currentList.removeAt(0)
        _signalTrend.value = currentList

        // Feed experimental sensing engine
        val sensingRes = sensingEngine.addSample(current.rssi)
        _sensingState.value = sensingRes

        // Persist to Room database asynchronously
        viewModelScope.launch {
            val entity = WifiSessionEntity(
                sessionId = activeSessionId,
                timestamp = System.currentTimeMillis(),
                ssid = current.ssid,
                bssid = current.bssid,
                rssi = current.rssi,
                frequencyMhz = current.frequencyMhz,
                linkSpeedMbps = current.linkSpeedMbps,
                qualityLevel = WifiUtils.classifyRssi(current.rssi).level.name,
                status = if (current.isConnected) "CONNECTED" else "DISCONNECTED",
                dataSourceType = "NATIVE_ANDROID_WIFI_API"
            )
            repository.insertSession(entity)
        }
    }

    fun scanNetworks() {
        wifiMonitor.triggerScan()
    }

    fun resetSensingBaseline() {
        sensingEngine.resetBaseline()
        _sensingState.value = SensingState(
            status = SensingStatus.INSUFFICIENT_DATA,
            baselineRssiMean = 0.0,
            baselineStdDev = 0.0,
            currentRssiMean = 0.0,
            currentStdDev = 0.0,
            deviationFromBaseline = 0.0,
            fluctuationIndex = 0.0,
            sampleCount = 0,
            evidenceQuality = EvidenceQuality.LOW,
            statusMessage = "Baseline reset. Collecting initial samples..."
        )
    }

    fun deleteSession(session: WifiSessionEntity) {
        viewModelScope.launch {
            repository.deleteSession(session)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearAll()
            _signalTrend.value = emptyList()
        }
    }

    fun exportData(context: Context, format: String) {
        val sessions = allSessions.value
        val mask = _maskPii.value
        if (format.equals("CSV", ignoreCase = true)) {
            val content = DataExporter.toCsv(sessions, mask)
            DataExporter.shareExportFile(context, "wafa_wifi_sessions_${System.currentTimeMillis()}.csv", content, "text/csv")
        } else {
            val content = DataExporter.toJson(sessions, mask)
            DataExporter.shareExportFile(context, "wafa_wifi_sessions_${System.currentTimeMillis()}.json", content, "application/json")
        }
    }

    fun getString(key: String): String {
        return LocalizationManager.getString(_language.value, key)
    }

    override fun onCleared() {
        super.onCleared()
        wifiMonitor.cleanup()
    }
}

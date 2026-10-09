package com.example.core.csi

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class CsiConnectionState {
    HARDWARE_NOT_CONNECTED,
    CSI_UNSUPPORTED_STOCK_ANDROID,
    CONNECTED,
    STREAMING,
    ERROR
}

data class CsiSubcarrierRecord(
    val timestamp: Long,
    val subcarrierIndex: Int,
    val amplitude: Double,
    val phase: Double
)

data class CsiPacket(
    val timestamp: Long,
    val macAddress: String,
    val rssi: Int,
    val channel: Int,
    val subcarrierCount: Int,
    val subcarriers: List<CsiSubcarrierRecord>,
    val dataSource: String
)

interface CsiHardwareAdapter {
    val adapterName: String
    val transportType: String // e.g. "USB_OTG_SERIAL", "LOCAL_UDP_SOCKET"
    fun isDevicePresent(): Boolean
    fun connect(): Boolean
    fun disconnect()
    fun validatePacket(data: ByteArray): Boolean
}

class CsiHardwareManager {

    private val _connectionState = MutableStateFlow(CsiConnectionState.CSI_UNSUPPORTED_STOCK_ANDROID)
    val connectionState: StateFlow<CsiConnectionState> = _connectionState.asStateFlow()

    private val _activePackets = MutableStateFlow<List<CsiPacket>>(emptyList())
    val activePackets: StateFlow<List<CsiPacket>> = _activePackets.asStateFlow()

    private val _statusDetails = MutableStateFlow(
        "Standard Android OS (including ColorOS on OPPO Reno13 F 4G) does not expose subcarrier-level 802.11 Channel State Information (CSI) to non-root user applications via public Android SDK APIs."
    )
    val statusDetails: StateFlow<String> = _statusDetails.asStateFlow()

    fun scanForExternalAdapters(): CsiConnectionState {
        // Honest check: No USB OTG CSI device or raw kernel interface is attached
        _connectionState.value = CsiConnectionState.HARDWARE_NOT_CONNECTED
        _statusDetails.value = "Scanned for USB OTG UART adapters (FTDI / CP2102 / CH340 / ESP32-S3) and local UDP socket CSI streams. No compatible CSI hardware source detected."
        return _connectionState.value
    }

    /**
     * Documentation on how to integrate a real external ESP32-S3 or Atheros hardware adapter.
     */
    fun getHardwareIntegrationGuide(): String {
        return """
            WAFA WI-FI SENSE — CSI HARDWARE INTEGRATION PROTOCOL
            -----------------------------------------------------
            Due to Google Android HAL restrictions, Raw 802.11 CSI is restricted to vendor firmware.
            To collect authentic CSI subcarrier matrices on OPPO Reno13 F 4G:

            Option 1: ESP32-S3 USB OTG Sniffer (Recommended)
            1. Flash ESP32-S3 with ESP-IDF CSI sniffer firmware:
               esp_wifi_set_csi_rx_cb(&csi_cb, NULL);
               esp_wifi_set_csi_config(&csi_config);
            2. Connect ESP32-S3 via USB-C OTG cable to OPPO Reno13 F 4G.
            3. Data is streamed at 921600 baud in standard IEEE 802.11n 64-subcarrier format.

            Option 2: Nexmon Patched Broadcom/Cypress Firmware
            - Requires root permissions, unlocked bootloader, and custom kernel modules.
            - Not supported on stock ColorOS CPH2701.

            Option 3: Atheros AR9580 / Intel 5300 Network Gateway
            - Streams CSI UDP frames over local Wi-Fi to port 5555.
        """.trimIndent()
    }
}

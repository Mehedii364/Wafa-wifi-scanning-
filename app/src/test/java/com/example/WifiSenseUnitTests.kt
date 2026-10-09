package com.example

import com.example.core.common.AppLanguage
import com.example.core.common.EvidenceQuality
import com.example.core.common.ExperimentalSensingEngine
import com.example.core.common.LocalizationManager
import com.example.core.common.SensingStatus
import com.example.core.common.SignalQualityLevel
import com.example.core.common.WifiBand
import com.example.core.common.WifiUtils
import com.example.core.database.WifiSessionEntity
import com.example.core.export.DataExporter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WifiSenseUnitTests {

    @Test
    fun testFrequencyToChannel_24Ghz() {
        assertEquals(1, WifiUtils.frequencyToChannel(2412))
        assertEquals(6, WifiUtils.frequencyToChannel(2437))
        assertEquals(11, WifiUtils.frequencyToChannel(2462))
        assertEquals(13, WifiUtils.frequencyToChannel(2472))
        assertEquals(14, WifiUtils.frequencyToChannel(2484))
    }

    @Test
    fun testFrequencyToChannel_5Ghz() {
        assertEquals(36, WifiUtils.frequencyToChannel(5180))
        assertEquals(40, WifiUtils.frequencyToChannel(5200))
        assertEquals(149, WifiUtils.frequencyToChannel(5745))
        assertEquals(161, WifiUtils.frequencyToChannel(5805))
    }

    @Test
    fun testFrequencyToChannel_Invalid() {
        assertNull(WifiUtils.frequencyToChannel(999))
        assertNull(WifiUtils.frequencyToChannel(3000))
    }

    @Test
    fun testFrequencyToBand() {
        assertEquals(WifiBand.BAND_2_4_GHZ, WifiUtils.frequencyToBand(2437))
        assertEquals(WifiBand.BAND_5_GHZ, WifiUtils.frequencyToBand(5240))
        assertEquals(WifiBand.BAND_6_GHZ, WifiUtils.frequencyToBand(6125))
        assertEquals(WifiBand.BAND_UNKNOWN, WifiUtils.frequencyToBand(100))
    }

    @Test
    fun testRssiClassification() {
        assertEquals(SignalQualityLevel.EXCELLENT, WifiUtils.classifyRssi(-45).level)
        assertEquals(SignalQualityLevel.GOOD, WifiUtils.classifyRssi(-58).level)
        assertEquals(SignalQualityLevel.FAIR, WifiUtils.classifyRssi(-72).level)
        assertEquals(SignalQualityLevel.POOR, WifiUtils.classifyRssi(-82).level)
        assertEquals(SignalQualityLevel.UNUSABLE, WifiUtils.classifyRssi(-95).level)
        assertEquals(SignalQualityLevel.UNKNOWN, WifiUtils.classifyRssi(0).level)
    }

    @Test
    fun testPrivacyMasking() {
        val maskedSsid = WifiUtils.maskSsid("HomeNetwork_5G")
        assertTrue(maskedSsid.contains("***"))
        assertFalse(maskedSsid == "HomeNetwork_5G")

        val maskedBssid = WifiUtils.maskBssid("11:22:33:44:55:66")
        assertEquals("11:22:**:**:**:66", maskedBssid)
    }

    @Test
    fun testExperimentalSensingEngine_BaselineAndFluctuation() {
        val engine = ExperimentalSensingEngine(windowSize = 5, sensitivity = 1.5)

        // Feed first 4 samples: should be INSUFFICIENT_DATA
        for (i in 1..4) {
            val state = engine.addSample(-60)
            assertEquals(SensingStatus.INSUFFICIENT_DATA, state.status)
        }

        // 5th sample should calibrate baseline
        val baseState = engine.addSample(-60)
        assertTrue(engine.isCalibrated())
        assertEquals(-60.0, baseState.baselineRssiMean, 0.01)

        // Feed steady samples: should be SIGNAL_STABLE
        engine.addSample(-60)
        engine.addSample(-60)
        val stableState = engine.addSample(-60)
        assertEquals(SensingStatus.SIGNAL_STABLE, stableState.status)

        // Feed large fluctuation: should detect variation
        engine.addSample(-40)
        engine.addSample(-42)
        val varState = engine.addSample(-38)
        assertEquals(SensingStatus.SIGNAL_VARIATION_OBSERVED, varState.status)
        assertTrue(varState.fluctuationIndex > 0)
    }

    @Test
    fun testDataExporter_CsvAndJsonFormat() {
        val sampleList = listOf(
            WifiSessionEntity(
                id = 1,
                sessionId = "test-session-uuid",
                timestamp = 1700000000000L,
                ssid = "WafaNetwork",
                bssid = "aa:bb:cc:dd:ee:ff",
                rssi = -55,
                frequencyMhz = 5180,
                linkSpeedMbps = 433,
                qualityLevel = "GOOD",
                status = "CONNECTED",
                dataSourceType = "NATIVE_ANDROID_WIFI_API",
                userLabel = "Living Room"
            )
        )

        // Unmasked CSV
        val csv = DataExporter.toCsv(sampleList, maskIdentifiers = false)
        assertTrue(csv.contains("ID,SessionID"))
        assertTrue(csv.contains("WafaNetwork"))
        assertTrue(csv.contains("aa:bb:cc:dd:ee:ff"))
        assertTrue(csv.contains("5180"))

        // Masked CSV
        val maskedCsv = DataExporter.toCsv(sampleList, maskIdentifiers = true)
        assertTrue(maskedCsv.contains("***"))
        assertFalse(maskedCsv.contains("WafaNetwork"))

        // JSON
        val json = DataExporter.toJson(sampleList, maskIdentifiers = false)
        assertTrue(json.contains("\"records\""))
        assertTrue(json.contains("\"WafaNetwork\""))
        assertTrue(json.contains("\"rssi\": -55"))
    }

    @Test
    fun testLocalizationManager() {
        val bnHome = LocalizationManager.getString(AppLanguage.BENGALI, "home")
        val enHome = LocalizationManager.getString(AppLanguage.ENGLISH, "home")
        val arHome = LocalizationManager.getString(AppLanguage.ARABIC, "home")

        assertEquals("হোম ড্যাশবোর্ড", bnHome)
        assertEquals("Home Dashboard", enHome)
        assertEquals("لوحة التحكم", arHome)
    }
}

package com.example.core.common

import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

enum class SensingStatus {
    INSUFFICIENT_DATA,
    SIGNAL_STABLE,
    SIGNAL_VARIATION_OBSERVED,
    INCONCLUSIVE_RESULT
}

enum class EvidenceQuality {
    HIGH,
    MODERATE,
    LOW
}

data class SensingState(
    val status: SensingStatus,
    val baselineRssiMean: Double,
    val baselineStdDev: Double,
    val currentRssiMean: Double,
    val currentStdDev: Double,
    val deviationFromBaseline: Double,
    val fluctuationIndex: Double, // Normalized 0.0 to 10.0 scale
    val sampleCount: Int,
    val evidenceQuality: EvidenceQuality,
    val statusMessage: String
)

class ExperimentalSensingEngine(
    private var windowSize: Int = 15,
    private var sensitivity: Double = 1.5 // 0.5 (sensitive) to 3.0 (coarse)
) {
    private val baselineSamples = mutableListOf<Int>()
    private val windowSamples = mutableListOf<Int>()
    private var isBaselineCalibrated = false
    private var baselineMean = 0.0
    private var baselineStdDev = 0.0

    fun updateConfig(newWindowSize: Int, newSensitivity: Double) {
        this.windowSize = newWindowSize.coerceIn(5, 50)
        this.sensitivity = newSensitivity.coerceIn(0.5, 3.5)
    }

    fun addSample(rssi: Int): SensingState {
        // Collect baseline first
        if (!isBaselineCalibrated) {
            baselineSamples.add(rssi)
            if (baselineSamples.size >= windowSize) {
                calculateBaseline()
            } else {
                return SensingState(
                    status = SensingStatus.INSUFFICIENT_DATA,
                    baselineRssiMean = 0.0,
                    baselineStdDev = 0.0,
                    currentRssiMean = rssi.toDouble(),
                    currentStdDev = 0.0,
                    deviationFromBaseline = 0.0,
                    fluctuationIndex = 0.0,
                    sampleCount = baselineSamples.size,
                    evidenceQuality = EvidenceQuality.LOW,
                    statusMessage = "Calibrating baseline (${baselineSamples.size}/$windowSize samples collected)..."
                )
            }
        }

        // Add to active sliding window
        windowSamples.add(rssi)
        if (windowSamples.size > windowSize) {
            windowSamples.removeAt(0)
        }

        if (windowSamples.size < 3) {
            return SensingState(
                status = SensingStatus.INSUFFICIENT_DATA,
                baselineRssiMean = baselineMean,
                baselineStdDev = baselineStdDev,
                currentRssiMean = rssi.toDouble(),
                currentStdDev = 0.0,
                deviationFromBaseline = 0.0,
                fluctuationIndex = 0.0,
                sampleCount = windowSamples.size,
                evidenceQuality = EvidenceQuality.LOW,
                statusMessage = "Gathering window measurements..."
            )
        }

        val curMean = windowSamples.average()
        val curStdDev = calculateStdDev(windowSamples, curMean)
        val devFromBase = abs(curMean - baselineMean)

        // Threshold calculation
        val dynamicThreshold = sensitivity * maxOf(baselineStdDev, 1.2)

        // Evidence quality heuristic: based on base signal level and variance
        val evidenceQuality = when {
            curMean < -82.0 -> EvidenceQuality.LOW // High path loss noise
            baselineStdDev > 4.0 -> EvidenceQuality.LOW // Noisy baseline
            baselineStdDev > 2.0 -> EvidenceQuality.MODERATE
            else -> EvidenceQuality.HIGH
        }

        // Normalized fluctuation index (0.0 to 10.0)
        val rawFluct = (devFromBase + curStdDev) * 1.5
        val fluctIndex = rawFluct.coerceIn(0.0, 10.0)

        val status: SensingStatus
        val msg: String

        if (curMean < -85.0) {
            status = SensingStatus.INCONCLUSIVE_RESULT
            msg = "Signal is too faint (below -85 dBm). Environmental noise dominates readings."
        } else if (devFromBase > dynamicThreshold || curStdDev > (baselineStdDev * 2.2)) {
            status = SensingStatus.SIGNAL_VARIATION_OBSERVED
            msg = "RF signal amplitude fluctuation exceeds baseline threshold."
        } else {
            status = SensingStatus.SIGNAL_STABLE
            msg = "RF propagation path exhibits steady amplitude consistency."
        }

        return SensingState(
            status = status,
            baselineRssiMean = baselineMean,
            baselineStdDev = baselineStdDev,
            currentRssiMean = curMean,
            currentStdDev = curStdDev,
            deviationFromBaseline = devFromBase,
            fluctuationIndex = fluctIndex,
            sampleCount = windowSamples.size,
            evidenceQuality = evidenceQuality,
            statusMessage = msg
        )
    }

    private fun calculateBaseline() {
        baselineMean = baselineSamples.average()
        baselineStdDev = calculateStdDev(baselineSamples, baselineMean)
        isBaselineCalibrated = true
    }

    private fun calculateStdDev(samples: List<Int>, mean: Double): Double {
        if (samples.size < 2) return 0.0
        val sumSquares = samples.sumOf { (it - mean).pow(2) }
        return sqrt(sumSquares / (samples.size - 1))
    }

    fun resetBaseline() {
        baselineSamples.clear()
        windowSamples.clear()
        isBaselineCalibrated = false
        baselineMean = 0.0
        baselineStdDev = 0.0
    }

    fun isCalibrated(): Boolean = isBaselineCalibrated
}

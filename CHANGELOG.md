# 📜 CHANGELOG — Wafa Wi-Fi Sense Premium

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.0] - 2026-10-09

### Added
- **Core Architecture:**
  - Complete native Android project built with Kotlin 2.2, Jetpack Compose, Material 3, and Gradle Kotlin DSL.
  - Package ID configured as `site.wafazone.wifi.sense`.
  - Targeted specifically for OPPO Reno13 F 4G (Model CPH2701) running Android 14/15.
- **Home Dashboard:**
  - Real-time connection status with color-coded vitality indicators.
  - Connected SSID/BSSID with instant PII masking.
  - RSSI (dBm), PHY Link Speed, Frequency, Channel, and Band display.
  - Real-time Canvas-rendered sparkline signal trend chart.
- **Live Wi-Fi Analyzer:**
  - Continuous signal amplitude monitoring with configurable interval (1s–10s).
  - Stale reading indicators and OS scan throttling warnings.
  - Educational technical distinction between RSSI, PHY rate, and ISP speed.
- **Network Scanner:**
  - Live discovery of nearby 2.4 GHz, 5 GHz, and 6 GHz Access Points.
  - IEEE 802.11 compliant Frequency-to-Channel calculation algorithm.
  - Rotating radar visualization during active scan sweeps.
- **Experimental RF Sensing:**
  - Baseline calibration engine ($\mu_0$, $\sigma_0$) and sliding observation window.
  - Transparent rule-based status classifications: `Signal Stable`, `Signal Variation Observed`, `Insufficient Data`, `Inconclusive Result`.
  - Prominent ethical notice rejecting false claims of human presence detection.
- **Advanced CSI Hardware Mode:**
  - Honest capability detector confirming that stock Android HAL does not expose subcarrier CSI matrices.
  - Hardware adapter probe and integration documentation for ESP32-S3 USB-OTG sniffer.
- **Hardware Compatibility Center:**
  - Automated device audit for OPPO Reno13 F 4G and compatible Android runtimes.
  - Feature-by-feature support table.
- **Local Persistence & Export:**
  - Room SQLite database integration with KSP for offline session logging.
  - Full CSV and JSON data export via Storage Access Framework and Android Share Sheet.
- **Privacy & Diagnostics:**
  - Privacy Center with 100% on-device data storage guarantee.
  - Diagnostics screen verifying system services, permissions, and database health.
- **Localization & Theming:**
  - Trilingual support: Bengali (বাংলা - Primary), English, and Arabic (العربية).
  - Obsidian Dark, Pure Black AMOLED, Light, and System themes.
- **CI/CD & DevOps:**
  - GitHub Actions automated build workflow (`.github/workflows/android-build.yml`).
  - Unit tests covering RSSI classification, frequency mapping, sensing algorithms, and data export.

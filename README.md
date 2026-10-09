# 🚀 Wafa Wi-Fi Sense Premium

[![Android Build & APK Generation](https://github.com/Mehedii364/WafaWiFiSense/actions/workflows/android-build.yml/badge.svg)](https://github.com/Mehedii364/WafaWiFiSense/actions/workflows/android-build.yml)
[![Platform](https://img.shields.io/badge/Platform-Android_14_--_16-00E5FF.svg)](https://developer.android.com)
[![Device](https://img.shields.io/badge/Primary_Device-OPPO_Reno13_F_4G_(CPH2701)-9D4EDD.svg)](https://oppo.com)
[![Language](https://img.shields.io/badge/Language-Kotlin_2.2-7C3AED.svg)](https://kotlinlang.org)
[![UI](https://img.shields.io/badge/UI-Jetpack_Compose_Material_3-10B981.svg)](https://developer.android.com/jetpack/compose)

> **Tagline:** *Analyze. Understand. Connect*  
> **Developer Credit:** **Developed by Mehedi364**  
> **Lead Engineer:** **Md. Mehedi Hasan**  
> **Package Identifier:** `site.wafazone.wifi.sense`  
> **Repository:** `Mehedii364/WafaWiFiSense`

---

## 📖 Overview

**Wafa Wi-Fi Sense Premium** is a native, production-ready Android wireless diagnostic and telemetry tool designed specifically for the **OPPO Reno13 F 4G (Model CPH2701)** and compatible Android devices. Built using modern Kotlin, Jetpack Compose, Material 3, and Room SQLite persistence, the application provides authentic RF measurements, network analysis, sliding-window experimental variance tracking, hardware compatibility auditing, and offline data exports.

### 🛡️ Core Engineering Commitments
1. **Authentic Data Only:** Uses real Android `WifiManager`, `ConnectivityManager`, and `ScanResult` APIs. No fake networks, no fabricated RSSI values, and no synthetic CSI arrays.
2. **Scientific Honesty:** Ordinary RSSI amplitude fluctuation reflects electromagnetic environmental changes (reflections, multipath, device motion, interference). It is **NOT** presented as calibrated proof of human presence.
3. **Local-First Privacy:** 100% of collected telemetry resides in an encrypted on-device SQLite database. No background tracking, no third-party analytics SDKs, and zero cloud uploads without consent.
4. **Honest CSI Hardware Detection:** The standard Android Hardware Abstraction Layer (HAL) on unmodified ColorOS / stock Android does not expose raw 802.11 subcarrier Channel State Information. The app provides a transparent hardware probe and integration specifications for external USB-OTG ESP32-S3 / Nexmon adapters.

---

## ✨ Features & Modules

### 1. 🏠 Home Dashboard
- Real-time connection status with color-coded vitality indicators.
- Connected network SSID and BSSID with instantaneous PII masking toggle.
- Precise RSSI (dBm), PHY Link Speed (Mbps), Frequency (MHz), Channel, and Band.
- 5-tier signal quality classification (Excellent, Good, Fair, Poor, Unusable).
- Real-time sparkline waveform tracking amplitude trajectory.
- Target device audit confirmation (OPPO Reno13 F 4G).

### 2. 📊 Live Wi-Fi Analyzer
- Interactive high-resolution RSSI waveform with dBm guide grids.
- Configurable monitoring intervals (1s, 2s, 3s, 5s, 10s) with rate-limiting warnings.
- Explicit session controls (Start / Stop) with automated Room database logging.
- Stale-data detection when OS throttling delays measurements.
- Clear technical documentation explaining differences between RSSI, PHY Link Speed, and ISP throughput.

### 3. 📡 Network Scanner
- Discovers real nearby 2.4 GHz, 5 GHz, and 6 GHz Access Points.
- Displays SSID, BSSID, Signal Level, Security Suite (WPA2/WPA3), Channel, and Band.
- Frequency-to-Channel algorithmic derivation compliant with IEEE 802.11 specifications.
- Frequency band filters (All, 2.4 GHz, 5 GHz).
- Android scan throttling compliance (4 scans per 2-minute foreground window).

### 4. 🔬 Experimental RF Sensing
- Baseline calibration window capturing steady-state RF amplitude mean ($\mu_0$) and standard deviation ($\sigma_0$).
- Sliding observation window computing real-time deviation ($|\mu_w - \mu_0|$) and fluctuation index.
- Transparent rule-based status classifications:
  - `Signal Stable`
  - `Signal Variation Observed`
  - `Insufficient Data`
  - `Inconclusive Result`
- Evidence quality indicators (High, Moderate, Low) based on signal-to-noise ratio.
- Prominent ethical notice rejecting false human presence detection claims.

### 5. 🔌 Advanced CSI Hardware Mode
- Honest capability probe: confirms that stock Android HAL does not expose raw 802.11 CSI.
- External hardware probe for USB-OTG UART adapters (FTDI, CP2102, ESP32-S3).
- Complete firmware integration guide for flashing ESP-IDF CSI sniffer firmware at 921600 baud.
- Zero fake CSI matrices.

### 6. 📱 Hardware Compatibility Center
- Automatic hardware audit: Manufacturer, Model, Codename, Android OS version, SDK level.
- Feature-by-feature support matrix:
  - Wi-Fi RSSI Monitoring (Supported)
  - Nearby AP Scanning (Supported with Restrictions)
  - 5 GHz Dual-Band (Supported)
  - Wi-Fi RTT / 802.11mc (Hardware Dependent)
  - Experimental RF Variance (Experimental)
  - Raw 802.11 CSI (Requires External Hardware)
  - Room Local Database (Supported)
  - Data Export (Supported)

### 7. 🗄️ Signal History & Export Center
- Local Room SQLite database logging timestamped sessions.
- Full-text search and filter by SSID and user labels.
- Storage Access Framework (SAF) CSV and JSON exports with privacy masking.
- Individual session deletion and total history purge.

### 8. 🩺 Diagnostics Center
- Automated health checks for Wi-Fi service, Location permissions, Nearby devices permissions, scan subsystem, and Room database.
- Actionable bilingual troubleshooting instructions in Bengali and English.

### 9. 🔒 Privacy Center
- User-controlled PII masking for SSIDs and BSSIDs.
- Local storage footprint counter.
- Ethical RF sensing commitment declaration.

### 10. ⚙️ Settings & Customization
- **Four Interface Themes:** Obsidian AMOLED (Default), Pure Black AMOLED, Light, System Dynamic.
- **Trilingual Localization:** Bengali (বাংলা - Primary), English, Arabic (العربية) with instant in-app switching.
- Configurable monitoring interval and battery-conscious mode (10s polling).
- Haptic feedback toggles.

---

## 🛠️ Build & Installation Instructions

### Prerequisites
- Android Studio Ladybug / Meerkat (or newer)
- JDK 17 (Eclipse Temurin or OpenJDK)
- Android SDK Platform 34 / 35 / 36

### Build via Command Line
To build the debug APK locally:

```bash
# Make gradlew executable
chmod +x gradlew

# Run automated unit tests
./gradlew testDebugUnitTest

# Assemble the real debug APK
./gradlew assembleDebug
```

### APK Output Locations
Upon successful compilation, the verified APK will be generated at:
- `app/build/outputs/apk/debug/app-debug.apk`
- `.build-outputs/app-debug.apk`
- `APK_DOWNLOAD/app-debug.apk`

---

## 🤖 Automated CI/CD (GitHub Actions)

The repository includes a production-grade automated CI/CD pipeline defined in `.github/workflows/android-build.yml`.

### Workflow Stages:
1. **Environment Setup:** Provisions Ubuntu runner with JDK 17 and Android SDK.
2. **Quality Verification:** Executes all JVM unit tests (`testDebugUnitTest`).
3. **Compilation:** Executes `./gradlew assembleDebug --stacktrace`.
4. **Validation:** Checks APK existence, verifies size > 100 KB, and runs `unzip -t` to test archive integrity.
5. **SHA256 Checksum:** Verifies identical SHA256 hashes between `.build-outputs/` and `APK_DOWNLOAD/`.
6. **Artifact Upload:** Publishes `app-debug-apk` via GitHub Actions Artifacts.
7. **Code Browser Mirroring:** Commits the verified APK to the repository under `APK_DOWNLOAD/app-debug.apk` for instant download from the GitHub file tree.

---

## 🔐 Required Permissions & Android Manifest

| Permission | Type | Justification |
| :--- | :--- | :--- |
| `ACCESS_WIFI_STATE` | Install-time | Query current connection, RSSI, and link speed. |
| `CHANGE_WIFI_STATE` | Install-time | Trigger nearby Wi-Fi AP scanning. |
| `ACCESS_NETWORK_STATE`| Install-time | Monitor network connectivity transitions. |
| `ACCESS_FINE_LOCATION`| Runtime | Required by Android to read SSIDs and scan results. |
| `NEARBY_WIFI_DEVICES` | Runtime (API 33+)| Scan for nearby Wi-Fi devices on Android 13+. |
| `POST_NOTIFICATIONS`  | Runtime (API 33+)| Notify user during background monitoring sessions. |
| `VIBRATE`            | Install-time | Haptic feedback on UI interactions. |

---

## 📄 License & Credits

- **Application:** Wafa Wi-Fi Sense Premium
- **Developer Credit:** Developed by Mehedi364
- **Lead Developer:** Md. Mehedi Hasan
- **License:** MIT License

# 🛡️ Privacy Policy — Wafa Wi-Fi Sense Premium

Last Updated: October 9, 2026  
Developer Credit: Developed by Mehedi364 (Md. Mehedi Hasan)  
Package ID: `site.wafazone.wifi.sense`

## 1. Local-First Data Philosophy
Wafa Wi-Fi Sense Premium is designed from the ground up to respect user privacy and adhere to the highest standards of data security:
- **No Cloud Telemetry:** All network observations, signal strength records, frequencies, and session logs are stored strictly inside the Android application sandbox using Room SQLite.
- **Zero Third-Party Trackers:** The application does not contain advertising SDKs, behavioral analytics, or hidden data harvesters.
- **No Account Required:** All features operate 100% offline without creating an account or logging into an external service.

## 2. Information Collected Locally
The application inspects wireless parameters provided by the Android OS:
- **Wi-Fi Signal Information:** RSSI (dBm), link speed, frequency, band, and channel.
- **Network Identifiers:** SSIDs and BSSIDs of connected and nearby Access Points.
- **User Settings:** Theme preference, language preference, and monitoring intervals.

By default, the application provides an active **PII Masking Mode** that obfuscates SSIDs and BSSIDs (e.g. `My***iFi` and `aa:bb:**:**:**:ff`) on screen and in exported files.

## 3. Permissions Used
- `ACCESS_FINE_LOCATION` and `NEARBY_WIFI_DEVICES`: Android requires these permissions for applications to scan for Wi-Fi access points and read connected network identifiers. Wafa Wi-Fi Sense never records or transmits your physical geographic GPS coordinates.
- `ACCESS_WIFI_STATE` & `ACCESS_NETWORK_STATE`: Used solely to query current Wi-Fi status and link metrics.

## 4. User Rights & Data Control
- **Export:** You can export your session records to open standard CSV or JSON formats at any time using Android's Storage Access Framework.
- **Deletion:** You can permanently purge individual records or wipe the entire local database from the **Signal History** or **Privacy Center** screens.

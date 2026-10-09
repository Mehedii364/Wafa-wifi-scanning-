package com.example.core.export

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.example.core.common.WifiUtils
import com.example.core.database.WifiSessionEntity
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DataExporter {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US)

    fun toCsv(sessions: List<WifiSessionEntity>, maskIdentifiers: Boolean): String {
        val sb = StringBuilder()
        sb.append("ID,SessionID,Timestamp,ISO_Time,SSID,BSSID,RSSI_dBm,Frequency_MHz,Channel,Band,LinkSpeed_Mbps,Quality,Status,DataSource,UserLabel\n")

        for (s in sessions) {
            val displaySsid = if (maskIdentifiers) WifiUtils.maskSsid(s.ssid) else s.ssid.replace("\"", "\"\"")
            val displayBssid = if (maskIdentifiers) WifiUtils.maskBssid(s.bssid) else s.bssid
            val channel = WifiUtils.frequencyToChannel(s.frequencyMhz)?.toString() ?: "N/A"
            val band = WifiUtils.frequencyToBand(s.frequencyMhz).displayName
            val isoTime = dateFormat.format(Date(s.timestamp))

            sb.append("${s.id},")
            sb.append("\"${s.sessionId}\",")
            sb.append("${s.timestamp},")
            sb.append("\"$isoTime\",")
            sb.append("\"$displaySsid\",")
            sb.append("\"$displayBssid\",")
            sb.append("${s.rssi},")
            sb.append("${s.frequencyMhz},")
            sb.append("$channel,")
            sb.append("\"$band\",")
            sb.append("${s.linkSpeedMbps},")
            sb.append("\"${s.qualityLevel}\",")
            sb.append("\"${s.status}\",")
            sb.append("\"${s.dataSourceType}\",")
            sb.append("\"${(s.userLabel ?: "").replace("\"", "\"\"")}\"\n")
        }
        return sb.toString()
    }

    fun toJson(sessions: List<WifiSessionEntity>, maskIdentifiers: Boolean): String {
        val sb = StringBuilder()
        sb.append("{\n")
        sb.append("  \"exportedAt\": \"${dateFormat.format(Date())}\",\n")
        sb.append("  \"app\": \"Wafa Wi-Fi Sense Premium\",\n")
        sb.append("  \"developer\": \"Developed by Mehedi364 (Md. Mehedi Hasan)\",\n")
        sb.append("  \"recordCount\": ${sessions.size},\n")
        sb.append("  \"records\": [\n")

        for (i in sessions.indices) {
            val s = sessions[i]
            val displaySsid = if (maskIdentifiers) WifiUtils.maskSsid(s.ssid) else escapeJson(s.ssid)
            val displayBssid = if (maskIdentifiers) WifiUtils.maskBssid(s.bssid) else escapeJson(s.bssid)
            val channel = WifiUtils.frequencyToChannel(s.frequencyMhz)
            val band = WifiUtils.frequencyToBand(s.frequencyMhz).displayName
            val isoTime = dateFormat.format(Date(s.timestamp))

            sb.append("    {\n")
            sb.append("      \"id\": ${s.id},\n")
            sb.append("      \"sessionId\": \"${escapeJson(s.sessionId)}\",\n")
            sb.append("      \"timestamp\": ${s.timestamp},\n")
            sb.append("      \"isoTime\": \"$isoTime\",\n")
            sb.append("      \"ssid\": \"$displaySsid\",\n")
            sb.append("      \"bssid\": \"$displayBssid\",\n")
            sb.append("      \"rssi\": ${s.rssi},\n")
            sb.append("      \"frequencyMhz\": ${s.frequencyMhz},\n")
            sb.append("      \"channel\": ${channel ?: "null"},\n")
            sb.append("      \"band\": \"$band\",\n")
            sb.append("      \"linkSpeedMbps\": ${s.linkSpeedMbps},\n")
            sb.append("      \"qualityLevel\": \"${escapeJson(s.qualityLevel)}\",\n")
            sb.append("      \"status\": \"${escapeJson(s.status)}\",\n")
            sb.append("      \"dataSourceType\": \"${escapeJson(s.dataSourceType)}\",\n")
            sb.append("      \"userLabel\": ${if (s.userLabel != null) "\"${escapeJson(s.userLabel)}\"" else "null"}\n")
            sb.append("    }")
            if (i < sessions.size - 1) sb.append(",")
            sb.append("\n")
        }

        sb.append("  ]\n")
        sb.append("}")
        return sb.toString()
    }

    private fun escapeJson(str: String): String {
        return str.replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\b", "\\b")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
    }

    fun shareExportFile(context: Context, filename: String, content: String, mimeType: String) {
        val cacheDir = File(context.cacheDir, "exports")
        if (!cacheDir.exists()) cacheDir.mkdirs()
        val file = File(cacheDir, filename)
        FileOutputStream(file).use { it.write(content.toByteArray()) }

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Wafa Wi-Fi Sense - $filename")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share Wi-Fi Analytics"))
    }
}

package com.example.util

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.example.data.DailyLog
import com.example.data.UserSettings
import java.io.File
import java.io.FileOutputStream

object CsvExporter {

    fun generateCsvContent(userSettings: UserSettings?, logs: List<DailyLog>): String {
        val sb = StringBuilder()
        
        // 1. User Settings Section
        sb.append("--- USER SETTINGS ---\n")
        sb.append("Setting,Value\n")
        if (userSettings != null) {
            sb.append("Last Period Start Date,${userSettings.lastPeriodDate}\n")
            sb.append("Average Cycle Length (Days),${userSettings.averageCycleLength}\n")
            sb.append("Average Period Length (Days),${userSettings.averagePeriodLength}\n")
        } else {
            sb.append("Last Period Start Date,Not Configured\n")
            sb.append("Average Cycle Length (Days),28\n")
            sb.append("Average Period Length (Days),5\n")
        }
        
        sb.append("\n") // Blank space separator
        
        // 2. Daily Symptom & Flow Logs Section
        sb.append("--- DAILY FLOW & SYMPTOM LOGS ---\n")
        sb.append("Date,Flow Intensity,Symptoms\n")
        for (log in logs) {
            val flowLabel = when (log.flowIntensity) {
                1 -> "Light"
                2 -> "Medium"
                3 -> "Heavy"
                else -> "None"
            }
            // Enclose symptoms in quotes to properly handle commas
            val cleanSymptoms = log.symptoms.replace("\"", "\"\"")
            sb.append("${log.date},$flowLabel,\"$cleanSymptoms\"\n")
        }
        
        return sb.toString()
    }

    fun exportToDownloads(context: Context, filename: String, csvContent: String): Uri? {
        val resolver = context.contentResolver
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                put(MediaStore.MediaColumns.MIME_TYPE, "text/csv")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            if (uri != null) {
                try {
                    resolver.openOutputStream(uri)?.use { outputStream ->
                        outputStream.write(csvContent.toByteArray())
                    }
                    return uri
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } else {
            // Fallback for Android 9 and lower
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (!downloadsDir.exists()) {
                downloadsDir.mkdirs()
            }
            val file = File(downloadsDir, filename)
            try {
                FileOutputStream(file).use { outputStream ->
                    outputStream.write(csvContent.toByteArray())
                }
                return Uri.fromFile(file)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return null
    }
}

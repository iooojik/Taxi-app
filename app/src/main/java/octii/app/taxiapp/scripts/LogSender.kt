package octii.app.taxiapp.scripts

import android.app.Activity
import android.content.Intent
import android.os.Environment
import android.os.Process
import android.os.StatFs
import android.util.Log
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader


class LogSender {
    fun sendLogs(activity: Activity) {
        log("sendLogs")
        printInfo()
        val pid = Process.myPid()
        try {
            val command = String.format("logcat -d -v -i -e -w threadtime *:*")
            val process = Runtime.getRuntime().exec(command)
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val result = StringBuilder()
            var currentLine: String? = null
            while (reader.readLine().also { currentLine = it } != null) {
                if (currentLine != null && currentLine!!.contains(pid.toString())) {
                    result.append(currentLine)
                    result.append("\n")
                }
            }
            log(result.toString())
            val emailIntent = Intent(Intent.ACTION_SEND)
            emailIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            emailIntent.type = "vnd.android.cursor.item/email"
            emailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf("sbobrov760@gmail.com"))
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Logs")
            emailIntent.putExtra(Intent.EXTRA_TEXT, result.toString())
            activity.startActivity(Intent.createChooser(emailIntent, "Send mail using..."))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun log(txt: String) {
        Log.d("log", txt)
    }

    private fun printInfo() {
        log("externalMemoryAvailable: " + externalMemoryAvailable())
        if (externalMemoryAvailable()) {
            log("getTotalExternalMemorySize: $totalExternalMemorySize")
            log("getAvailableExternalMemorySize: $availableExternalMemorySize")
        }
        log("getTotalInternalMemorySize: $totalInternalMemorySize")
        log("getAvailableInternalMemorySize: $availableInternalMemorySize")
    }

    private fun externalMemoryAvailable(): Boolean {
        return Environment.getExternalStorageState() ==
                Environment.MEDIA_MOUNTED
    }

    private val availableInternalMemorySize: String
        get() {
            val path: File = Environment.getDataDirectory()
            val stat = StatFs(path.path)
            val blockSize = stat.blockSizeLong
            val availableBlocks = stat.availableBlocksLong
            return formatSize(availableBlocks * blockSize)
        }
    private val totalInternalMemorySize: String
        get() {
            val path: File = Environment.getDataDirectory()
            val stat = StatFs(path.path)
            val blockSize = stat.blockSizeLong
            val totalBlocks = stat.blockCountLong
            return formatSize(totalBlocks * blockSize)
        }
    private val availableExternalMemorySize: String
        get() = if (externalMemoryAvailable()) {
            val path: File = Environment.getExternalStorageDirectory()
            val stat = StatFs(path.path)
            val blockSize = stat.blockSizeLong
            val availableBlocks = stat.availableBlocksLong
            formatSize(availableBlocks * blockSize)
        } else {
            "ext not available"
        }
    private val totalExternalMemorySize: String
        get() {
            return if (externalMemoryAvailable()) {
                val path: File = Environment.getExternalStorageDirectory()
                val stat = StatFs(path.path)
                val blockSize = stat.blockSizeLong
                val totalBlocks = stat.blockCountLong
                formatSize(totalBlocks * blockSize)
            } else {
                "ext not available"
            }
        }

    private fun formatSize(size: Long): String {
        var size = size
        var suffix: String? = null
        if (size >= 1024) {
            suffix = "KB"
            size /= 1024
            if (size >= 1024) {
                suffix = "MB"
                size /= 1024
            }
        }
        val resultBuffer = StringBuilder(java.lang.Long.toString(size))
        var commaOffset = resultBuffer.length - 3
        while (commaOffset > 0) {
            resultBuffer.insert(commaOffset, ',')
            commaOffset -= 3
        }
        if (suffix != null) resultBuffer.append(suffix)
        return resultBuffer.toString()
    }
}
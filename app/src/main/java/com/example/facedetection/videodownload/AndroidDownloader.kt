package com.example.facedetection.videodownload

import android.app.DownloadManager
import android.content.Context
import android.os.Environment
import android.webkit.MimeTypeMap
import android.webkit.URLUtil
import android.widget.Toast
import androidx.annotation.UiThread
import androidx.annotation.WorkerThread
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import com.example.facedetection.MainActivity.Global.Companion.appLog
import com.example.facedetection.MainActivity.Global.Companion.dateStr
import com.example.facedetection.MainActivity.Global.Companion.debugFlag
import com.example.facedetection.MainActivity.Global.Companion.dlDir
import com.example.facedetection.MainActivity.Global.Companion.emarthUrl
import com.example.facedetection.MainActivity.Global.Companion.videoDynamicUrl
import com.example.facedetection.MainActivity.Global.Companion.videoStaticUrl
import com.example.facedetection.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class AndroidDownloader(
    private val context: Context
): Downloader, AppCompatActivity() {
    private val downloadManager = context.getSystemService(DownloadManager::class.java)

    @WorkerThread
    private suspend fun getResponseCode(url: String): Int {
        return withContext(Dispatchers.IO) {
            var result = 404
            val con = URL(url).openConnection() as HttpURLConnection
            con.requestMethod = "GET"

            try {
                result = con.responseCode
                result
            } catch (e: java.lang.Exception){
                result
            }
        }
    }

    override fun downloadFile(url: String): Long {
        var nameOfFile = URLUtil.guessFileName(url, null, MimeTypeMap.getFileExtensionFromUrl(url))

        if (getUrlType(url) == 2) {
            nameOfFile = nameOfFile.substring(0,7) + "_" + dateStr + ".mp4"
        }
        //val filename: String = currentUrl.substring(currentUrl.lastIndexOf('/') + 1)
        val request = DownloadManager.Request(url.toUri())
            .setMimeType("video/mp4")
            .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI)
            .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE)
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setTitle(nameOfFile)
            .addRequestHeader("Authorization", "Bearer <token>")
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, nameOfFile)
        return downloadManager.enqueue(request)
    }

    @UiThread
    private fun downloadWithCode(url: String, code: Int): Long {
        var dlResult = -1L

        if (code == 200) {
            val downloader = AndroidDownloader(context)
            dlResult = downloader.downloadFile(url)
            if (debugFlag) {
                Toast.makeText(context, "Download started", Toast.LENGTH_LONG).show()
            }
        } else {
            if (debugFlag) {
                Toast.makeText(context, "Dowonload file isn't available", Toast.LENGTH_LONG).show()
            }
        }
        return dlResult
    }

    fun execDownload(url: String) {
        val dateAndTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH:mm:ss"))
        val dlAsof = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"))
        var nameOfFile = URLUtil.guessFileName(url, null, MimeTypeMap.getFileExtensionFromUrl(url))
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
        val editor = sharedPref.edit()
        var dlResult = -1L
        var execFlag = true
        val videoDynamicFile = "test_vd_" + dateStr + ".mp4"

        if (getUrlType(url) == 2) {
            nameOfFile = nameOfFile.substring(0,7) + "_" + dateStr + ".mp4"
            if(File(dlDir, nameOfFile).exists()){
                execFlag = false
            }
        }

        if (getUrlType(url) == 1) {
            if (File(dlDir, videoDynamicFile).exists()) {
                execFlag = false
            }
        }

        if(execFlag) {
            lifecycleScope.launch {
                val code = getResponseCode(url)
                dlResult = downloadWithCode(url, code)

                if (!debugFlag) {
                    appLog.save(dateAndTime + ": ")
                    appLog.save(code.toString() + ", " + dlResult + ", " + url + "\n")
                }
            }.invokeOnCompletion {
                if (dlResult == -1L) {
                    Toast.makeText(context, "新しい動画ファイルはありません", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context, "新しい動画ファイルのダウンロードが完了しました", Toast.LENGTH_LONG).show()

                    if (getUrlType(url) == 1) {
                        editor.putString("videoDynamicPreference", dlAsof)
                        editor.putString("videoDynamicFilePref", nameOfFile)
                        editor.apply()
                    } else if (getUrlType(url) == 2) {
                        editor.putString("videoStaticPreference", dlAsof)
                        editor.putString("videoStaticFilePref", nameOfFile)
                        editor.apply()
                    } else if (getUrlType(url) == 3) {
                        editor.putString("dlPreference", dlAsof)
                        editor.putString("dlFilePref", nameOfFile)
                        editor.apply()
                    }
                }
            }
        } else {
            Toast.makeText(context, "新しい動画ファイルはありません", Toast.LENGTH_LONG).show()
        }
    }

    private fun getUrlType(url: String): Int{
        var result = 0

        if(url == videoDynamicUrl){
            result = 1
        } else if(url == videoStaticUrl){
            result = 2
        } else if(url == emarthUrl){
            result = 3
        }
        return result
    }
}
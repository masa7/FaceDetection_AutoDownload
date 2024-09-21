package com.example.facedetection.videodownload

import android.app.DownloadManager
import android.content.Context
import android.os.Environment
import android.webkit.MimeTypeMap
import android.webkit.URLUtil
import androidx.core.net.toUri
import com.example.facedetection.MainActivity.Global.Companion.emarthUrl


class AndroidDownloader(
    private val context: Context
): Downloader {

    private val downloadManager = context.getSystemService(DownloadManager::class.java)

    override fun downloadFile(url: String): Long {

        val nameOfFile = URLUtil.guessFileName(
            emarthUrl, null,
            MimeTypeMap.getFileExtensionFromUrl(emarthUrl)
        )

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
}
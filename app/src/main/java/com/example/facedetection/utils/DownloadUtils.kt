package com.example.facedetection.utils

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Environment.DIRECTORY_DOWNLOADS
import android.util.Log


class DownloadUtils(linkName: String)  {
    private var dlContext: Context
    private val dlLink: String
    private var file: FileUtils
    private lateinit var fileUri: String

    init {
        // for data download
        dlContext = SingletonContext.applicationContext()
        dlLink = linkName
        file = FileUtils("downloadTest.txt", "2")
    }

    fun download(): String {
        try {
            // linkにファイルが置かれたURLを指定
            val link = dlLink
            val fileName = link.substring(link.lastIndexOf("/") + 1)
            //val fileName = "sample0003.mp4"

            //DownloadManagerを使用してファイルをダウンロード
            val manager = dlContext.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val request = DownloadManager.Request(Uri.parse(link))
            request.setTitle(fileName)
            request.setDescription("Downloading")
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            request.setDestinationInExternalPublicDir(DIRECTORY_DOWNLOADS, fileName)
            val downloadId = manager.enqueue(request)

            file.saveFile("\n---download profile---\n")
            file.saveFile(link)
            file.saveFile("\n")
            file.saveFile(fileName)
            file.saveFile("\n")
            file.saveFile(request.toString())
            file.saveFile("\n")

            val receiver = object: BroadcastReceiver() {
                //ダウンロード完了後の処理
                override fun onReceive(context: Context, intent: Intent) {
                    val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                    if (id == downloadId) {
                        //intentによりファイルを開く
                        val openFileIntent = Intent(Intent.ACTION_VIEW)
                        val uri = manager.getUriForDownloadedFile(id)
                        file.saveFile("File ID: ")
                        file.saveFile(id.toString())
                        file.saveFile("\n")

                        file.saveFile("URI: ")
                        file.saveFile(uri.toString())
                        file.saveFile("\n")

                        fileUri = uri.toString()

                        openFileIntent.setDataAndType(uri, context.contentResolver.getType(uri))
                        openFileIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        file.saveFile("OpenFile Intent: ")
                        file.saveFile(openFileIntent.toString())
                        file.saveFile("\n")

                        //context.startActivity(openFileIntent)
                        file.saveFile("download step5\n")

                    }
                }
            }
            file.saveFile("download step6\n")

            dlContext.registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
            file.saveFile("download step7\n")

        } catch (e: Exception) {
            Log.d("Error", "Error occurs in file download")
        }

        return fileUri
    }
}
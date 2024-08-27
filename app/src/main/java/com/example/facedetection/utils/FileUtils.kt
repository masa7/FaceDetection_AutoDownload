package com.example.facedetection.utils

import android.content.ContentValues
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException
import java.io.OutputStreamWriter

class FileUtils(fileName: String, StorageType: String) {
    private var file: File
    private var fn: String
    private var storageType: String
    //private var contentResolver: ContentResolver

    init {
        // for data export
        val ctx = SingletonContext.applicationContext()
        fn = fileName
        storageType = StorageType
        file = File(ctx.filesDir, fileName)

        //val ctx2 = SingletonContext.applicationContext()
        //contentResolver = ctx2.contentResolver
    }

    fun save(str: String?){
        if(storageType == "1"){
            saveFile(str)
        }else if(storageType == "2"){
            savePublicFiles(str)
        }
    }

    fun read(): String? {
        var text: String? = null
        if(storageType == "1"){
            text = readFile()
        }else if(storageType == "2"){
            text = readPublicFiles()
        }

        return text
    }

    fun saveFile(str: String?){
        try{
            FileWriter(file, true).use { writer -> writer.write(str) }
        }catch (e: IOException){
            Log.d("Error", "Error occurs in $fn")
        }
    }

    fun readFile():String? {
        var text: String? = null
        try{
            //BufferedReader(FileReader(file)).use { br -> text = br.readLine() }
            BufferedReader(FileReader(file)).use { br ->
                br.lines().forEach {
                    text += "\n"
                    text += it
                }
            }
        }catch (e: IOException){
            e.printStackTrace()
        }
        return text
    }

    fun savePublicFiles(str: String?){
        val fileLoc = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).path + "/"
        val directory = File(fileLoc)
        val filePath = fileLoc + fn

        try {
            FileOutputStream(File(filePath), true).use({ fileOutputStream ->
                OutputStreamWriter(fileOutputStream, "UTF-8").use({ outputStreamWriter ->
                    BufferedWriter(outputStreamWriter).use({ bw ->
                        bw.write(str)
                        bw.flush()
                    })
                })
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun readPublicFiles(): String? {
        var text: String? = null
        var fileCnt = 0
        val fileLoc = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).path + "/"
        val directory = File(fileLoc)

        val files = directory.listFiles()?.filter { it.isFile }
        files?.forEach { file ->
            println(file.name)
            if (fn == file.name) {
                ++fileCnt
            }
        }

        try{
            BufferedReader(FileReader(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).path + "/" + fn)).use { br ->
                br.lines().forEach {
                    text += "\n"
                    text += it
                }
            }
        }catch (e: IOException){
            e.printStackTrace()
        }
        return text
    }

    fun savePublicFiles2(str: String?){
        val collection = if(Build.VERSION.SDK_INT >= 29){
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        }else {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }

        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "test.txt")
            put(MediaStore.Images.Media.MIME_TYPE, "text/plain")
            //put(MediaStore.Images.ImageColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS + "/log/")
            if(Build.VERSION.SDK_INT >= 29){
                put(MediaStore.Images.Media.IS_PENDING, true)
            }
        }

        val ctx = SingletonContext.applicationContext()
        val contentResolver = ctx.contentResolver
        val contentUri = contentResolver.insert(collection, contentValues)

        try{
            contentResolver.openFileDescriptor(contentUri!!, "w", null).use {
                FileOutputStream(it!!.fileDescriptor).use {
                    //output -> output.write(("sample text line").toByteArray())
                        output -> output.write((str)?.toByteArray())
                }
            }

            contentValues.clear()
            if(Build.VERSION.SDK_INT >= 29) {
                contentResolver.update(contentUri, contentValues.apply {
                    put(MediaStore.Images.Media.IS_PENDING, false)
                }, null, null)
            }else{
                contentResolver.update(contentUri, contentValues, null, null)
            }
        } catch (e: FileNotFoundException){
            e.printStackTrace()
        } catch (e: IOException){
            e.printStackTrace()
        }
    }

}
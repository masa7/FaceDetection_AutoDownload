package com.example.facedetection.videodownload

interface Downloader {
    fun downloadFile(url: String): Long
}
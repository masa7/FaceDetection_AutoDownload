package com.example.facedetection

interface Downloader {
    fun downloadFile(url: String): Long
}
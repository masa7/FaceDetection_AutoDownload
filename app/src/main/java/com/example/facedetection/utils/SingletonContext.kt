package com.example.facedetection.utils

import android.app.Application
import android.content.Context

class SingletonContext: Application() {


    init {
        instance = this
    }

    companion object{
        private var instance: SingletonContext? = null

        fun applicationContext() : Context {
            return instance!!.applicationContext
        }
    }
}

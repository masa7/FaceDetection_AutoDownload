package com.example.facedetection

import android.os.Bundle
import android.view.MenuItem
import com.example.facedetection.MainActivity.Global.Companion.abbrFaceDetectionLog
import com.example.facedetection.MainActivity.Global.Companion.dateStr
import com.example.facedetection.MainActivity.Global.Companion.storageType
import com.example.facedetection.databinding.ActivityLogdataBinding
import com.example.facedetection.utils.BaseActivity
import com.example.facedetection.utils.FileUtils

class LogdataActivity : BaseActivity() {
    private val binding by lazy { ActivityLogdataBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        supportActionBar?.setTitle(R.string.logdataPrefTitle)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        getDetectionFile()
    }

    private fun getDetectionFile() {
        val logFile = FileUtils(abbrFaceDetectionLog + dateStr + ".txt", storageType)
        binding.logtext.text = logFile.read()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        var returnVal = true

        if(item.itemId == android.R.id.home){
            finish()
        }else{
            returnVal = super.onOptionsItemSelected(item)
        }
        return returnVal
    }
}
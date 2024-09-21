package com.example.facedetection

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.example.facedetection.MainActivity.Global.Companion.abbrFaceDetectionLog
import com.example.facedetection.MainActivity.Global.Companion.dateStr
import com.example.facedetection.MainActivity.Global.Companion.storageType
import com.example.facedetection.databinding.ActivityLogdataBinding
import com.example.facedetection.utils.FileUtils

class LogdataActivity : AppCompatActivity() {
    private val binding by lazy { ActivityLogdataBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //enableEdgeToEdge()
        //setContentView(R.layout.activity_logdata)
        setContentView(binding.root)

        /*
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        */

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
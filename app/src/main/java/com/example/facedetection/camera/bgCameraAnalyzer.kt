package com.example.facedetection.camera

import android.util.Log
import com.example.facedetection.MainActivity.Global.Companion.abbrFaceDetectionLog
import com.example.facedetection.MainActivity.Global.Companion.dateStr
import com.example.facedetection.MainActivity.Global.Companion.nameOfPlayedVideo
import com.example.facedetection.MainActivity.Global.Companion.storageType
import com.example.facedetection.MainActivity.Global.Companion.uID
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import com.example.facedetection.utils.FileUtils
import kotlin.math.abs

class bgCameraAnalyzer() : bgBaseCameraAnalyzer<List<Face>>() {

    private var intervalSec: Int
    private var prevSec: Int

    // for tracking id
    private var file: FileUtils
    private var idList: MutableList<Int?> = mutableListOf()

    init {
        //val dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
        file = FileUtils(abbrFaceDetectionLog+ dateStr + ".txt", storageType)
        intervalSec = 1
        prevSec = 0
    }

    private val cameraOptions = FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
        //.setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
        //.setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
        .setMinFaceSize(0.10f)
        .enableTracking()
        .build()

    private val detector = FaceDetection.getClient(cameraOptions)

    override fun detectInImage(image: InputImage): Task<List<Face>> {
        return detector.process(image)
    }

    override fun stop() {
        try {
            detector.close()
        } catch (e: Exception) {
            Log.e(TAG, "BackGround stop: $e")
        }
    }

    override fun onSuccess(results: List<Face>, rotationDegree: Int) {
        // for log recording
        val dateAndTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH:mm:ss"))
        val curSec = LocalDateTime.now().second

        if (abs(curSec - prevSec) >= intervalSec) {
            results.forEach {
                // for face tracking
                if (it.trackingId != null) {
                    val id = it.trackingId

                    if (id in idList) {
                        // save data into file
                        file.save(dateAndTime + ", ")
                        file.save(id.toString() + ", ")
                        //file.save(rotationDegree.toString() + ", ")
                        file.save(nameOfPlayedVideo + ", ")
                        file.save(uID)
                        file.save("\n")
                    } else {
                        idList.add(id)
                    }
                }
            }
            prevSec = curSec
        }
    }

    override fun onFailure(e: Exception) {
        Log.e(TAG, "BackGround onFailure : $e")
    }

    companion object {
        private const val  TAG = "bgCameraAnalyzer"
    }

}
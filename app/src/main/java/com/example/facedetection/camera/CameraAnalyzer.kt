package com.example.facedetection.camera

import android.graphics.Rect
import java.time.LocalDateTime
import android.util.Log
import com.example.facedetection.MainActivity.Global.Companion.abbrFaceDetectionLog
import com.example.facedetection.MainActivity.Global.Companion.dateStr
import com.example.facedetection.MainActivity.Global.Companion.storageType
import com.example.facedetection.MainActivity.Global.Companion.uID
import com.example.facedetection.MainActivity.Global.Companion.uEmail
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.example.facedetection.graphic.GraphicOverlay
import com.example.facedetection.graphic.RectangleOverlay
import com.example.facedetection.utils.FileUtils
import java.time.format.DateTimeFormatter
import kotlin.math.abs

class CameraAnalyzer(
    private val overlay: GraphicOverlay<*>,
    private val initialCameraSide: String
) : BaseCameraAnalyzer<List<Face>>() {

    private var cameraSide: String
    private var intervalSec: Int
    private var prevSec: Int
    // for tracking id
    private var file: FileUtils
    private var idList: MutableList<Int?> = mutableListOf()
    // for firestore
    private var fsFlag: Boolean

    init {
        cameraSide = initialCameraSide

        //val dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
        file = FileUtils(abbrFaceDetectionLog + dateStr + ".txt", storageType)
        intervalSec = 1
        prevSec = 0
        fsFlag = false
    }

    override val graphicOverlay: GraphicOverlay<*>
        get() = overlay

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
            Log.e(TAG, "stop: $e")
        }
    }

    override fun onSuccess(results: List<Face>, graphicOverlay: GraphicOverlay<*>, rect: Rect, rotationDegree: Int) {
        // for log recording
        val dateAndTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH:mm:ss"))
        val curSec = LocalDateTime.now().second

        // Initialize firestore db and set collectionPath name
        //val db = Firebase.firestore

        if (abs(curSec - prevSec) >= intervalSec) {
            graphicOverlay.clear()
            results.forEach {
                //val faceGraphic = RectangleOverlay(graphicOverlay, it, rect, cameraSide)
                val faceGraphic = RectangleOverlay(graphicOverlay, it, rect, cameraSide, rotationDegree)

                // for face tracking
                if (it.trackingId != null) {
                    val id = it.trackingId

                    if (id in idList) {
                        // save data into file
                        file.save(dateAndTime + ", ")
                        file.save(id.toString() + ", ")
                        file.save(rotationDegree.toString() + ", ")
                        file.save(uID)
                        file.save("\n")

                        // export data to firestore
//                        if(fsFlag){
//                            val face_collection = db.collection("Faces").document(dateAndTime + "-" + id.toString())
//
//                            val face = hashMapOf(
//                                "faceId" to id,
//                                "captureTime" to dateAndTime,
//                                //"timestamp" to FieldValue.serverTimestamp()
//                            )
//                            face_collection.set(face)
//                        }

                        faceGraphic.setColor("RED")
                    } else {
                        idList.add(id)
                    }
                }
                graphicOverlay.add(faceGraphic)
            }
            graphicOverlay.postInvalidate()
            prevSec = curSec
        }
    }

    override fun onFailure(e: Exception) {
        Log.e(TAG, "onFailure : $e")
    }

    companion object {
        private const val  TAG = "CameraAnalyzer"
    }
}
package com.example.facedetection.camera

import android.content.Context
import android.util.Log
import android.view.OrientationEventListener
import android.view.Surface
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.example.facedetection.graphic.GraphicOverlay
import com.example.facedetection.utils.CameraUtils
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraManager(
    private val context: Context,
    private val previewView: PreviewView,
    private val graphicOverlay: GraphicOverlay<*>,
    private val lifecycleOwner: LifecycleOwner
) {

    private lateinit var preview: Preview
    private lateinit var camera: Camera
    private lateinit var cameraProvider: ProcessCameraProvider
    private var cameraExecutor : ExecutorService = Executors.newSingleThreadExecutor()
    private var imageAnalysis : ImageAnalysis = ImageAnalysis.Builder().setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build()

    companion object {
        private const val TAG: String = "CameraManager"
        var cameraOption : Int = CameraSelector.LENS_FACING_BACK
    }

    fun cameraStart(cameraSide: String) {
        val cameraProcessProvider = ProcessCameraProvider.getInstance(context)
        orientationEventListener.enable()

        if(cameraSide == "1"){
            cameraOption = CameraSelector.LENS_FACING_BACK
        }else{
            cameraOption = CameraSelector.LENS_FACING_FRONT
        }

        cameraProcessProvider.addListener({
            cameraProvider = cameraProcessProvider.get()
            preview = Preview.Builder().build()
            imageAnalysis.setAnalyzer(cameraExecutor, CameraAnalyzer(graphicOverlay, cameraSide))

            /*
            imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, CameraAnalyzer(graphicOverlay, cameraSide))
                }
            */

            val cameraSelector = CameraSelector.Builder()
                .requireLensFacing(cameraOption)
                .build()

            setCameraConfig(cameraProvider, cameraSelector)
        }, ContextCompat.getMainExecutor(context)
        )
    }

    val orientationEventListener = object : OrientationEventListener(context) {
        override fun onOrientationChanged(orientation : Int) {
            val rotation: Int
            when (orientation) {
                in 45..134 -> rotation = Surface.ROTATION_270
                in 135..224 -> rotation = Surface.ROTATION_180
                in 225..314 -> rotation = Surface.ROTATION_90
                else -> rotation = Surface.ROTATION_0
            }
            imageAnalysis?.targetRotation = rotation
        }
    }

    private fun setCameraConfig(
        cameraProvider: ProcessCameraProvider,
        cameraSelector: CameraSelector
    ) {
        try {
            cameraProvider.unbindAll()
            camera = cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageAnalysis
            )
            preview.setSurfaceProvider(previewView.surfaceProvider)
        } catch (e:Exception) {
            Log.e(TAG, "setCameraConfig: $e")
        }
    }

    fun changeCamera() {
        var cameraSide: String

        cameraProvider.unbindAll()
        cameraOption = if (cameraOption == CameraSelector.LENS_FACING_BACK) CameraSelector.LENS_FACING_FRONT
        else CameraSelector.LENS_FACING_BACK

        CameraUtils.toggleSelector()

        if(cameraOption == CameraSelector.LENS_FACING_BACK){
            cameraSide = "1"
        }else{
            cameraSide = "2"
        }

        cameraStart(cameraSide)
    }

    fun cameraStop() {
        cameraProvider.unbindAll()
        orientationEventListener.disable()
    }
}
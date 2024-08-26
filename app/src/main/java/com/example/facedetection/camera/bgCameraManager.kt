package com.example.facedetection.camera

import android.content.Context
import android.util.Log
import android.view.OrientationEventListener
import android.view.Surface
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.example.facedetection.utils.CameraUtils
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class bgCameraManager(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner
) {

    private lateinit var camera: Camera
    private lateinit var cameraProvider: ProcessCameraProvider
    private var cameraExecutor : ExecutorService = Executors.newSingleThreadExecutor()
    private var imageAnalysis : ImageAnalysis = ImageAnalysis.Builder().setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build()

    companion object {
        private const val TAG: String = "bgCameraManager"
        var bgCameraOption : Int = CameraSelector.LENS_FACING_BACK
    }

    fun cameraStart(cameraSide: String) {
        val cameraProcessProvider = ProcessCameraProvider.getInstance(context)
        bgOrientationEventListener.enable()

        if(cameraSide == "1"){
            bgCameraOption = CameraSelector.LENS_FACING_BACK
        }else{
            bgCameraOption = CameraSelector.LENS_FACING_FRONT
        }

        cameraProcessProvider.addListener({
            cameraProvider = cameraProcessProvider.get()
            imageAnalysis.setAnalyzer(cameraExecutor, bgCameraAnalyzer())

            /*
            imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, bgCameraAnalyzer())
                }
            */

            val cameraSelector = CameraSelector.Builder()
                .requireLensFacing(bgCameraOption)
                .build()

            setCameraConfig(cameraProvider, cameraSelector)
        }, ContextCompat.getMainExecutor(context)
        )
    }

    val bgOrientationEventListener = object : OrientationEventListener(context) {
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
                imageAnalysis
            )
        } catch (e:Exception) {
            Log.e(TAG, "BackGround setCameraConfig: $e")
        }
    }

    fun changeCamera() {
        var cameraSide: String

        cameraProvider.unbindAll()
        bgCameraOption = if (bgCameraOption == CameraSelector.LENS_FACING_BACK) CameraSelector.LENS_FACING_FRONT
        else CameraSelector.LENS_FACING_BACK
        CameraUtils.toggleSelector()
        if(bgCameraOption == CameraSelector.LENS_FACING_BACK){
            cameraSide = "1"
        }else{
            cameraSide = "2"
        }
        cameraStart(cameraSide)
    }

    fun cameraStop() {
        cameraProvider.unbindAll()
        bgOrientationEventListener.disable()
    }

    fun setCameraSide(cameraSide: String){
        cameraProvider.unbindAll()
        if(cameraSide == "1"){
            bgCameraOption = CameraSelector.LENS_FACING_BACK
        }else{
            bgCameraOption = CameraSelector.LENS_FACING_FRONT
        }
        //CameraUtils.setSelector(cameraSide)
        cameraStart(cameraSide)
    }
}
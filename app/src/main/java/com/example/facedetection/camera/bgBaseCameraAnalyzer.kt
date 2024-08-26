package com.example.facedetection.camera

import android.annotation.SuppressLint
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face

abstract class bgBaseCameraAnalyzer<T: List<Face>> : ImageAnalysis.Analyzer {

    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image

        mediaImage?.let { image ->
            detectInImage(
                InputImage.fromMediaImage(
                    image,
                    imageProxy.imageInfo.rotationDegrees
                )
            )
                .addOnSuccessListener { results ->
                    onSuccess(results, imageProxy.imageInfo.rotationDegrees)
                    imageProxy.close()
                }
                .addOnFailureListener {
                    onFailure(it)
                    imageProxy.close()
                }
        }
    }

    protected abstract fun detectInImage(image: InputImage) : Task<T>

    abstract fun stop()

    protected abstract fun onSuccess(
        results: List<Face>,
        rotationDegree: Int
    )

    protected abstract fun onFailure(e: Exception)
}
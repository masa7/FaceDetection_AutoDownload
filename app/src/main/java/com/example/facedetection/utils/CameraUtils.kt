package com.example.facedetection.utils

import android.content.res.Configuration
import android.graphics.Rect
import android.graphics.RectF
import androidx.camera.core.CameraSelector
//import androidx.compose.ui.focus.DefaultFocusProperties.left
//import androidx.compose.ui.focus.DefaultFocusProperties.right
import com.example.facedetection.graphic.GraphicOverlay
import kotlin.math.ceil

object CameraUtils {

    private var mScale: Float? = null
    private var mOffsetX: Float? = null
    private var mOffsetY: Float? = null
    private var cameraSelector: Int = CameraSelector.LENS_FACING_FRONT

    fun calculateRect(
        overlay: GraphicOverlay<*>,
        height: Float,
        width: Float,
        boundingBoxT: Rect,
        cameraSide: String,
        rotationDegree: Int
    ) : RectF {

        // for land scape
        fun isLandScapeMode(): Boolean {
            //return overlay.context.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
            return when (rotationDegree) {
                0 -> true       //   0 rotation: landscape
                180 -> true     // 180 rotation: inverse landscape
                270 -> false    // 270 rotation: inverse portrait
                else -> false   //  90 rotation: portrait
            }
        }

        fun whenLandScapeModeWidth(): Float {
            return when (isLandScapeMode()) {
                true -> width
                false -> height
            }
        }

        fun whenLandScapeModeHeight(): Float {
            return when (isLandScapeMode()) {
                true -> height
                false -> width
            }
        }

        val scaleX = overlay.width.toFloat() / whenLandScapeModeWidth()
        val scaleY = overlay.height.toFloat() / whenLandScapeModeHeight()
        //val scale = scaleX.coerceAtLeast(scaleY)

        val scale: Float
        when (rotationDegree) {
            0 -> scale = scaleY.coerceAtLeast(scaleX)      //   0 rotation: landscape
            180 -> scale = scaleY.coerceAtLeast(scaleX)    // 180 rotation: inverse landscape
            270 -> scale = scaleX.coerceAtLeast(scaleY)    // 270 rotation: inverse portrait
            else -> scale = scaleX.coerceAtLeast(scaleY)   //  90 rotation: portrait
        }
        this.mScale = scale

        // Calculate offset (we need to center the overlay on the target)
        val offsetX = (overlay.width.toFloat() - ceil(whenLandScapeModeWidth() * scale)) / 2.0f
        val offsetY = (overlay.height.toFloat() - ceil(whenLandScapeModeHeight() * scale)) / 2.0f

        this.mOffsetX = offsetX
        this.mOffsetY = offsetY
        /*
        val mappedBox = RectF().apply { // back camera
            left = boundingBoxT.right * scale + offsetX
            right = boundingBoxT.left * scale + offsetX
            top = boundingBoxT.top * scale + offsetY
            bottom = boundingBoxT.bottom * scale + offsetY
        }
        */
        val centerX = overlay.width.toFloat()/2
        val centerY = overlay.height.toFloat()/2
        val leftX = boundingBoxT.right * scale + offsetX - centerX
        val rightX = boundingBoxT.left * scale + offsetX - centerX
        val topY = boundingBoxT.top * scale + offsetY - centerY
        val bottomY = boundingBoxT.bottom * scale + offsetY - centerY

        val mappedBox = RectF().apply { // back camera
            when (rotationDegree) {
                0 -> {
                    left = centerX - topY
                    right = centerX - bottomY
                    top = centerY + rightX
                    bottom = centerY + leftX
                }

                180 -> {
                    left = centerX + topY
                    right = centerX + bottomY
                    top = centerY - rightX
                    bottom = centerY - leftX
                }

                270 -> {
                    left = centerX - leftX
                    right = centerX - rightX
                    top = centerY - bottomY
                    bottom = centerY - topY
                }

                else -> { // rotation = 90
                    left = centerX + leftX
                    right = centerX + rightX
                    top = centerY + bottomY
                    bottom = centerY + topY
                }
            }
        }

        if(cameraSide=="2"){ // front camera
            val centerX = overlay.width.toFloat()/2
            mappedBox.apply {
                left = centerX + (centerX - left)
                right = centerX - (right - centerX)
            }
        }

        return mappedBox
    }

    fun toggleSelector() {
        cameraSelector = if (cameraSelector == CameraSelector.LENS_FACING_BACK) CameraSelector.LENS_FACING_FRONT
        else CameraSelector.LENS_FACING_BACK
    }
}
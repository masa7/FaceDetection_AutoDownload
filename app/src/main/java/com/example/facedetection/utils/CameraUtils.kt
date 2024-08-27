package com.example.facedetection.utils

import android.graphics.Rect
import android.graphics.RectF
import androidx.camera.core.CameraSelector
import com.example.facedetection.MainActivity
import com.example.facedetection.graphic.GraphicOverlay
import kotlin.math.abs
import kotlin.math.ceil

object CameraUtils {

    private var cameraSelector: Int = CameraSelector.LENS_FACING_FRONT

    fun calculateRect(
        overlay: GraphicOverlay<*>,
        height: Float,
        width: Float,
        boundingBoxT: Rect,
        cameraSide: String,
        rotationDegree: Int
    ) : RectF {

        val centerX = getOverlayX(width, height, overlay.width.toFloat(), overlay.height.toFloat(),(boundingBoxT.right.toFloat() + boundingBoxT.left.toFloat())/2, (boundingBoxT.top.toFloat() + boundingBoxT.bottom.toFloat())/2, rotationDegree, cameraSide)
        val centerY = getOverlayY(width, height, overlay.width.toFloat(), overlay.height.toFloat(),(boundingBoxT.right.toFloat() + boundingBoxT.left.toFloat())/2, (boundingBoxT.top.toFloat() + boundingBoxT.bottom.toFloat())/2, rotationDegree, cameraSide)
        val scaleXY = getScale(width, height, overlay.width.toFloat(), overlay.height.toFloat())
        val sideLen = getSideLength(boundingBoxT, scaleXY)

        val leftX = centerX - sideLen
        val rightX = centerX + sideLen
        val topY = centerY - sideLen
        val bottomY = centerY + sideLen

        val mappedBox = RectF().apply {
            left = leftX
            right = rightX
            top = topY
            bottom = bottomY
        }

        //screenCheck("screenLog.txt", width.toFloat(), height.toFloat(), overlay.width.toFloat(), overlay.height.toFloat(), boundingBoxT, cameraSide, rotationDegree)
        return mappedBox
    }

    fun getScale(imageW:Float, imageH:Float, overlayW:Float, overlayH:Float):Float{
        //val listScale = listOf(overlayW / imageW, overlayW / imageH, overlayH / imageW, overlayH / imageH)
        //return listScale.min()

        return (overlayW / imageW + overlayW / imageH + overlayH / imageW + overlayH / imageH) / 4
    }

    fun getSideLength(box: Rect, scale: Float): Float{
        return (abs(box.right - box.left) + abs(box.bottom - box.top)) * scale / 4
    }

    fun getOverlayX(imageW:Float, imageH:Float, overlayW: Float, overlayH:Float, imageX:Float, imageY:Float, rotationDegree:Int, cameraSide:String):Float {
        val ratioX = imageX /  getMaxX(imageW, imageH, rotationDegree)
        val ratioY = imageY /  getMaxY(imageW, imageH, rotationDegree)

        var x = when(rotationDegree){
            0 -> ceil((1 - ratioY) * overlayW)
            180 -> ceil(ratioY * overlayW)
            270 -> ceil((1 - ratioX) * overlayW)
            else -> ceil(ratioX * overlayW)
        }

        val x2 = when(rotationDegree){
            0 -> ceil((1 - ratioY) * overlayW)
            180 -> ceil((ratioY) * overlayW)
            270 -> ceil((1 - ratioX) * overlayW)
            else -> ceil(ratioX * overlayW)
        }

        if(cameraSide == "2") {x = x2}

        return x
    }

    fun getOverlayY(imageW:Float, imageH:Float, overlayW: Float, overlayH:Float, imageX:Float, imageY:Float, rotationDegree:Int, cameraSide: String):Float {
        val ratioX = imageX /  getMaxX(imageW, imageH, rotationDegree)
        val ratioY = imageY /  getMaxY(imageW, imageH, rotationDegree)

        var y = when(rotationDegree){
            0 -> ceil(ratioX * overlayH)
            180 -> ceil((1 - ratioX) * overlayH)
            270 -> ceil((1 - ratioY) * overlayH)
            else -> ceil(ratioY * overlayH)
        }

        val y2 = when(rotationDegree){
            0 -> ceil((1 - ratioX) * overlayH)
            180 -> ceil((ratioX) * overlayH)
            270 -> ceil((ratioY) * overlayH)
            else -> ceil((1 - ratioY) * overlayH)
        }

        if(cameraSide == "2") {y = y2}

        return y
    }

    fun getMaxX(imageW:Float, imageH: Float, rotationDegree:Int):Float {
        return when(rotationDegree){
            0 -> imageW
            180 -> imageW
            270 -> imageH
            else -> imageH
        }
    }

    fun getMaxY(imageW:Float, imageH: Float, rotationDegree:Int):Float {
        return when(rotationDegree){
            0 -> imageH
            180 -> imageH
            270 -> imageW
            else -> imageW
        }
    }

    fun screenCheck(fn:String, w: Float, h: Float, overlayW: Float, overlayH: Float, box: Rect, cameraSide: String, rotationDegree: Int){
        val file = FileUtils(fn, MainActivity.Global.storageType)

        file.save(cameraSide)
        file.save(", " + rotationDegree.toString())
        file.save(", " + w.toString() + ", " + h.toString())
        file.save(", " + overlayW.toString() + ", " + overlayH.toString())
        file.save(", " + box.left.toString() + ", " + box.right.toString())
        file.save(", " + box.top.toString() + ", " + box.bottom.toString())
        file.save("\n")
    }

    fun toggleSelector() {
        cameraSelector = if (cameraSelector == CameraSelector.LENS_FACING_BACK) CameraSelector.LENS_FACING_FRONT
        else CameraSelector.LENS_FACING_BACK
    }
}
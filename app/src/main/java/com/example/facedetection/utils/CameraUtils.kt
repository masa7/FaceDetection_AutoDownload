package com.example.facedetection.utils

import android.graphics.Rect
import android.graphics.RectF
import androidx.camera.core.CameraSelector
import androidx.compose.ui.input.key.Key.Companion.W
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

        val centerX = getOverlayX1(width, height, overlay.width.toFloat(), overlay.height.toFloat(),(boundingBoxT.right.toFloat() + boundingBoxT.left.toFloat())/2, (boundingBoxT.top.toFloat() + boundingBoxT.bottom.toFloat())/2, rotationDegree, cameraSide)
        val centerY = getOverlayY1(width, height, overlay.width.toFloat(), overlay.height.toFloat(),(boundingBoxT.right.toFloat() + boundingBoxT.left.toFloat())/2, (boundingBoxT.top.toFloat() + boundingBoxT.bottom.toFloat())/2, rotationDegree, cameraSide)
        val scaleXY = getScale(width, height, overlay.width.toFloat(), overlay.height.toFloat(), 0)
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

    fun getScale(imageW:Float, imageH:Float, overlayW:Float, overlayH:Float, mode:Int):Float{
        val retScale: Float
        val listScale = listOf(overlayW / imageW, overlayW / imageH, overlayH / imageW, overlayH / imageH)
        val scaleMin = listScale.min()
        val scaleY = overlayH / imageW
        val scaleX = overlayW / imageH
        val scaleAvg = (overlayW / imageW + overlayW / imageH + overlayH / imageW + overlayH / imageH) / 4

        if(mode==1){
            retScale = scaleY
        }else if(mode==2){
            retScale = scaleX
        }else if(mode==3){
            retScale = scaleMin
        }else {
            retScale = scaleAvg
        }

        return retScale
    }

    fun getSideLength(box: Rect, scale: Float): Float{
        return (abs(box.right - box.left) + abs(box.bottom - box.top)) * scale / 4
    }

    fun getOverlayX(imageW:Float, imageH:Float, overlayW: Float, overlayH:Float, imageX:Float, imageY:Float, rotationDegree:Int, cameraSide:String):Float {
        val ratioX = imageX /  getMaxX(imageW, imageH, rotationDegree)
        val ratioY = imageY /  getMaxY(imageW, imageH, rotationDegree)

        var x = when (rotationDegree) {
            0 -> ceil((1 - ratioY) * overlayW)      // mobile: landscape
            180 -> ceil(ratioY * overlayW)          // mobile: reverse landscape
            270 -> ceil((1 - ratioX) * overlayW)    // mobile: reverse portrait
            else -> ceil(ratioX * overlayW)         // mobile: portrait
        }

        val x2 = when (rotationDegree) {
            0 -> ceil((1 - ratioY) * overlayW)
            180 -> ceil((ratioY) * overlayW)
            270 -> ceil((1 - ratioX) * overlayW)
            else -> ceil(ratioX * overlayW)
        }

        if(cameraSide == "2") {x = x2}

        return x
    }

    fun getOverlayX1(imageW:Float, imageH:Float, overlayW: Float, overlayH:Float, imageX:Float, imageY:Float, rotationDegree:Int, cameraSide:String):Float {
        val scaleW = overlayW / imageH
        val scaleH = overlayH / imageW

        val scale = scaleH.coerceAtLeast(scaleW)
        val offsetX = (overlayW - ceil(imageH * scale)) / 2.0f

        var x = when (rotationDegree) {
            0 -> ceil(overlayW - (imageY * scale + offsetX))    // mobile: landscape
            180 -> ceil(imageY * scale + offsetX)               // mobile: reverse landscape
            270 -> ceil(overlayW - (imageX * scale + offsetX))  // mobile: reverse portrait
            else -> ceil(imageX * scale + offsetX)              // mobile: portrait
        }

        val x2 = when (rotationDegree) {
            0 -> ceil(overlayW - (imageY * scale + offsetX))
            180 -> ceil(imageY * scale + offsetX)
            270 -> ceil(overlayW - (imageX * scale + offsetX))
            else -> ceil(imageX * scale + offsetX)
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

    fun getOverlayY1(imageW:Float, imageH:Float, overlayW: Float, overlayH:Float, imageX:Float, imageY:Float, rotationDegree:Int, cameraSide: String):Float {
        val scaleW = overlayW / imageH
        val scaleH = overlayH / imageW

        val scale = scaleH.coerceAtLeast(scaleW)
        val offsetY = (overlayH - ceil(imageW * scale)) / 2.0f

        var y = when(rotationDegree){
            0 -> ceil(imageX * scale + offsetY)
            180 -> ceil(overlayH - (imageX * scale + offsetY))
            270 -> ceil(overlayH - (imageY * scale + offsetY))
            else -> ceil(imageY * scale + offsetY)
        }

        val y2 = when(rotationDegree){
            0 -> ceil(overlayH - (imageX * scale + offsetY))
            180 -> ceil(imageX * scale + offsetY)
            270 -> ceil(imageY * scale + offsetY)
            else -> ceil(overlayH - (imageY * scale + offsetY))
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
        val file = FileUtils(fn, "1")

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
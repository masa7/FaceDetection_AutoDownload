package com.example.facedetection.graphic

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import com.google.mlkit.vision.face.Face
import com.example.facedetection.utils.CameraUtils


class RectangleOverlay(
    private val graphicOverlay: GraphicOverlay<*>,
    private val face: Face,
    private val rect: Rect,
    private val cameraSide: String,
    private val rotationDegree: Int
) : GraphicOverlay.Graphic(graphicOverlay) {

    private val boxPaint : Paint = Paint()

    init {
        // for face detection
        boxPaint.color = Color.GREEN
        boxPaint.style = Paint.Style.STROKE
        boxPaint.strokeWidth = 3.0f
    }

    override fun draw(canvas: Canvas) {
        val rect = CameraUtils.calculateRect(
            graphicOverlay,
            rect.height().toFloat(),
            rect.width().toFloat(),
            face.boundingBox,
            cameraSide,
            rotationDegree
        )
        canvas.drawRect(rect, boxPaint)
    }

    fun setColor(color: String){
        if(color == "RED"){
            boxPaint.color = Color.RED
        }else if(color == "BLUE"){
            boxPaint.color = Color.BLUE
        }else if(color == "YELLOW"){
            boxPaint.color = Color.YELLOW
        }
    }
}
package com.example.visionapp.onnxmodels.processing.Helpers

import android.graphics.Bitmap
import com.example.visionapp.onnxmodels.processing.DetectionResult

object DetectionBitmapHelper {

    fun drawDetectionsOnBitmap(bitmap: Bitmap, detections: List<DetectionResult>): Bitmap {
        val result = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = android.graphics.Canvas(result)
        val paint = android.graphics.Paint().apply {
            style = android.graphics.Paint.Style.STROKE
            color = android.graphics.Color.RED
            strokeWidth = 3f
        }
        val textPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.YELLOW
            textSize = 32f
        }

        val imgWidth = bitmap.width.toFloat()
        val imgHeight = bitmap.height.toFloat()

        for (det in detections) {
            val (x, y, w, h) = det.box
            val left = (x - w / 2)
            val top = (y - h / 2)
            val right = (x + w / 2)
            val bottom = (y + h / 2)

            canvas.drawRect(left, top, right, bottom, paint)
            canvas.drawText("Cls ${det.classId}: ${"%.2f".format(det.confidence)}", left, top - 10, textPaint)
        }

        return result
    }
}
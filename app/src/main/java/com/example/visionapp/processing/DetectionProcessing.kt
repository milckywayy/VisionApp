package com.example.visionapp.processing

import android.graphics.Bitmap
import android.util.Log
import com.example.visionapp.CameraConfig.DEPTH_RESOLUTION
import com.example.visionapp.CameraConfig.DETECTION_RESOLUTION
import com.example.visionapp.ModelsConfig
import com.example.visionapp.onnxmodels.processing.DetectionResult
import com.example.visionapp.utils.scaleBitmap

object DetectionProcessing {

    fun processDetectionsByBoxSize(detections: List<DetectionResult>): List<DetectionResult> {
        val sortedDetections = detections
            .sortedByDescending { calculateBoxArea(it.box) }
            .toMutableList()
        return processSortedDetection(sortedDetections)
    }

    fun processDetectionsByAverageDepth(detections: List<DetectionResult>, depthMap: Bitmap): List<DetectionResult> {
        val sortedDetections = detections
            .sortedByDescending { calculateDepth(it.box, depthMap) }
            .toMutableList()
        return processSortedDetection(sortedDetections)
    }

    fun processDetectionsByBoxSizeDebug(detections: List<DetectionResult>, originalPhoto: Bitmap): Bitmap {
        val sortedDetections = detections
            .sortedByDescending { calculateBoxArea(it.box) }
            .toMutableList()
        return drawBoundinBoxes(originalPhoto,processSortedDetection(sortedDetections))
    }

    fun processDetectionsByAverageDepthDebug(detections: List<DetectionResult>, depthMap: Bitmap, originalPhoto: Bitmap): Bitmap {
        val sortedDetections = detections
            .sortedByDescending { calculateDepth(it.box, depthMap) }
            .toMutableList()
        return drawBoundinBoxes(originalPhoto,processSortedDetection(sortedDetections))
    }

    private fun drawBoundinBoxes(bitmap: Bitmap, detections: List<DetectionResult>): Bitmap {
        val result = scaleBitmap(bitmap, DETECTION_RESOLUTION)
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
    
    private fun calculateBoxArea(box: FloatArray): Float {
        val width = box[2]
        val height = box[3]
        return width * height
    }

    private fun calculateDepth(box: FloatArray, depthMap: Bitmap): Float {
        val x = box[0]
        val y = box[1]
        val w = box[2]
        val h = box[3]

        val left = ((x - w / 2) * depthMap.width).toInt()
        val top = ((y - h / 2) * depthMap.height).toInt()
        val right = ((x + w / 2) * depthMap.width).toInt()
        val bottom = ((y + h / 2) * depthMap.height).toInt()

        val clampedLeft = left.coerceIn(0, depthMap.width - 1)
        val clampedTop = top.coerceIn(0, depthMap.height - 1)
        val clampedRight = right.coerceIn(0, depthMap.width - 1)
        val clampedBottom = bottom.coerceIn(0, depthMap.height - 1)

        val width = clampedRight - clampedLeft + 1
        val height = clampedBottom - clampedTop + 1

        if (width <= 0 || height <= 0) return 0f

        val pixels = IntArray(width * height)
        depthMap.getPixels(pixels, 0, width, clampedLeft, clampedTop, width, height)

        var totalDepth = 0f
        for (pixel in pixels) {
            val depth = (pixel shr 16 and 0xFF) / 255f
            totalDepth += depth
        }

        return totalDepth / pixels.size
    }

    private fun processSortedDetection(sortedDetections: MutableList<DetectionResult>): List<DetectionResult> {
        val result = mutableListOf<DetectionResult>()
        val addedGroups = mutableSetOf<String>()

        for (detection in sortedDetections) {
            val group = getClassGroup(detection.classId)
            if (group !in addedGroups) {
                result.add(detection)
                addedGroups.add(group)
            }
        }

        return result
    }


    private fun getClassGroup(classId: Int): String {
        return ModelsConfig.DETECTION_DISTANCE_ESTIMATION_GROUPS[classId]
            ?.toString()
            ?: throw IllegalArgumentException("No class group mapping found for classId: $classId")
    }
}

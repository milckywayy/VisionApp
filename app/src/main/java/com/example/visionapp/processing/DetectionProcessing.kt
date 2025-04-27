package com.example.visionapp.processing

import android.graphics.Bitmap
import com.example.visionapp.CameraConfig.DEPTH_RESOLUTION
import com.example.visionapp.CameraConfig.DETECTION_RESOLUTION
import com.example.visionapp.ModelsConfig
import com.example.visionapp.onnxmodels.processing.DetectionResult

object DetectionProcessing {

    fun processDetectionsByBoxSize(detections: List<DetectionResult>): List<DetectionResult> {
        val sortedDetections = detections
            .sortedByDescending { calculateBoxArea(it.box) }
            .toMutableList()
        return processSortedDetection(sortedDetections)
    }

    private fun calculateBoxArea(box: FloatArray): Float {
        val width = box[2]
        val height = box[3]
        return width * height
    }

    fun processDetectionsByAverageDepth(detections: List<DetectionResult>, depthMap: Bitmap): List<DetectionResult> {
        val sortedDetections = detections
            .sortedByDescending { calculateDepth(it.box, depthMap) }
            .toMutableList()
        return processSortedDetection(sortedDetections)
    }

    private fun calculateDepth(box: FloatArray, depthMap: Bitmap): Float {
        val resizedBox = resizeBox(box[0],box[1],box[2],box[3], DEPTH_RESOLUTION.width.toFloat() / DETECTION_RESOLUTION.width)
        val (center_x,center_y,w,h) = resizedBox
        val left = (center_x - w / 2).toInt()
        val top = (center_y - h / 2).toInt()
        val right = (center_x + w / 2).toInt()
        val bottom = (center_y + h / 2).toInt()

        val clampedLeft = left.coerceIn(0, depthMap.width - 1)
        val clampedTop = top.coerceIn(0, depthMap.height - 1)
        val clampedRight = right.coerceIn(0, depthMap.width - 1)
        val clampedBottom = bottom.coerceIn(0, depthMap.height - 1)

        var totalDepth = 0f
        var pixelCount = 0

        for (y in clampedTop until clampedBottom) {
            for (x in clampedLeft until clampedRight) {
                val pixelColor = depthMap.getPixel(x, y)

                val depth = (pixelColor shr 16 and 0xFF) / 255f

                totalDepth += depth
                pixelCount++
            }
        }

        return if (pixelCount == 0) 0f else totalDepth / pixelCount
    }

    fun resizeBox(x: Float, y: Float, width: Float, height: Float, scaleFactor: Float): FloatArray {
        val newWidth = width * scaleFactor
        val newHeight = height * scaleFactor

        val newX = x - (newWidth - width) / 2
        val newY = y - (newHeight - height) / 2

        return floatArrayOf(newX, newY, newWidth, newHeight)
    }

    private fun processSortedDetection(sortedDetections: MutableList<DetectionResult>): List<DetectionResult> {
        val result = mutableListOf<DetectionResult>()

        while (sortedDetections.isNotEmpty()) {
            val best = sortedDetections.removeAt(0)
            result.add(best)

            val iterator = sortedDetections.iterator()
            while (iterator.hasNext()) {
                val det = iterator.next()
                if (getClassGroup(best.classId) == getClassGroup(det.classId)) {
                    iterator.remove()
                }
            }
        }

        return result
    }

    private fun getClassGroup(classId: Int): String {
        return ModelsConfig.DETECTION_DISTANCE_ESTIMATION_GROUPS[classId]
            ?: throw IllegalArgumentException("No class group mapping found for classId: $classId")
    }
}
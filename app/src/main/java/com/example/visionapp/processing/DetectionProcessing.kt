package com.example.visionapp.processing

import android.graphics.Bitmap
import android.util.Log
import com.example.visionapp.CameraConfig
import com.example.visionapp.CameraConfig.DEPTH_RESOLUTION
import com.example.visionapp.CameraConfig.DETECTION_RESOLUTION
import com.example.visionapp.Mappings
import com.example.visionapp.onnxmodels.processing.DetectionResult
import com.example.visionapp.utils.scaleBitmap

object DetectionProcessing {

    fun processDetectionsByBoxSize(detections: List<DetectionResult>, segmentationBitmap: Bitmap): List<DetectionResult> {
        val sortedDetections = detections
            .sortedByDescending { calculateBoxArea(it.box) }
            .toMutableList()
        return processSortedDetection(sortedDetections, segmentationBitmap)
    }

    fun processDetectionsByAverageDepth(detections: List<DetectionResult>, segmentationBitmap: Bitmap, depthMap: Bitmap): List<DetectionResult> {
        val sortedDetections = detections
            .sortedByDescending { calculateDepth(it.box, depthMap) }
            .toMutableList()
        return processSortedDetection(sortedDetections, segmentationBitmap)
    }

    fun processDetectionsByBoxSizeDebug(detections: List<DetectionResult>, segmentationBitmap: Bitmap, originalPhoto: Bitmap): Bitmap {
        val sortedDetections = detections
            .sortedByDescending { calculateBoxArea(it.box) }
            .toMutableList()
        return drawBoundinBoxes(originalPhoto,processSortedDetection(sortedDetections, segmentationBitmap))
    }

    fun processDetectionsByAverageDepthDebug(detections: List<DetectionResult>, segmentationBitmap: Bitmap, depthMap: Bitmap, originalPhoto: Bitmap): Bitmap {
        val sortedDetections = detections
            .sortedByDescending { calculateDepth(it.box, depthMap) }
            .toMutableList()
        return drawBoundinBoxes(originalPhoto,processSortedDetection(sortedDetections, segmentationBitmap))
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

        val scaleX = depthMap.width.toFloat() / DEPTH_RESOLUTION.width
        val scaleY = depthMap.height.toFloat() / DETECTION_RESOLUTION.height

        val left = ((x - w / 2) * scaleX).toInt().coerceIn(0, depthMap.width - 1)
        val top = ((y - h / 2) * scaleY).toInt().coerceIn(0, depthMap.height - 1)
        val right = ((x + w / 2) * scaleX).toInt().coerceIn(0, depthMap.width - 1)
        val bottom = ((y + h / 2) * scaleY).toInt().coerceIn(0, depthMap.height - 1)

        val width = right - left + 1
        val height = bottom - top + 1

        if (width <= 0 || height <= 0) return 0f

        val pixels = IntArray(width * height)
        depthMap.getPixels(pixels, 0, width, left, top, width, height)

        var totalDepth = 0f
        for (pixel in pixels) {
            val depth = (pixel shr 16 and 0xFF) / 255f
            totalDepth += depth
        }

        return totalDepth / pixels.size
    }

    private fun processSortedDetection(sortedDetections: MutableList<DetectionResult>, segmentationBitmap: Bitmap): List<DetectionResult> {
        val result = mutableListOf<DetectionResult>()
        val addedGroups = mutableSetOf<String>()

        for (detection in sortedDetections) {
            val group = getClassGroup(detection.classId)
            if (group !in addedGroups) {
                result.add(detection)
                addedGroups.add(group)
                if ( detection.classId == Mappings.DetectionZebraId && isZebraSegmentationOverlap(detection, segmentationBitmap)) {
                    result.remove(detection)
                }
            }
        }

        return result
    }


    private fun getClassGroup(classId: Int): String {
        return Mappings.DETECTION_DISTANCE_ESTIMATION_GROUPS[classId]
            ?.toString()
            ?: throw IllegalArgumentException("No class group mapping found for classId: $classId")
    }


    private fun isZebraSegmentationOverlap(detection: DetectionResult, segmentationBitmap: Bitmap): Boolean {
        val (x, y, w, h) = detection.box

        val scaleX = segmentationBitmap.width.toFloat() / DETECTION_RESOLUTION.width
        val scaleY = segmentationBitmap.height.toFloat() / DETECTION_RESOLUTION.height

        val left = ((x - w / 2) * scaleX).toInt().coerceIn(0, segmentationBitmap.width - 1)
        val top = ((y - h / 2) * scaleY).toInt().coerceIn(0, segmentationBitmap.height - 1)
        val right = ((x + w / 2) * scaleX).toInt().coerceIn(0, segmentationBitmap.width - 1)
        val bottom = ((y + h / 2) * scaleY).toInt().coerceIn(0, segmentationBitmap.height - 1)

        val width = right - left + 1
        val height = bottom - top + 1

        if (width <= 0 || height <= 0) return false

        val pixels = IntArray(width * height)
        segmentationBitmap.getPixels(pixels, 0, width, left, top, width, height)

        val totalPixels = pixels.size

        var zebraPixels = 0
        for (pixel in pixels) {
            val classId = (pixel shr 16) and 0xFF
            if (classId == Mappings.SegmentationZebraId) {
                zebraPixels++
            }
            if (zebraPixels >= totalPixels / 3) {
                return true
            }
        }

        return zebraPixels >= totalPixels / 2
    }

}

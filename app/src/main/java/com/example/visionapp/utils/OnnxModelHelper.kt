package com.example.visionapp.utils

import android.graphics.Bitmap
import ai.onnxruntime.*
import java.nio.FloatBuffer

class OnnxModelHelper(private val ortEnv: OrtEnvironment) {

    data class DetectionResult(
        val classId: Int,
        val confidence: Float,
        val box: FloatArray
    )

    fun createTensor(bitmap: Bitmap): OnnxTensor {
        return OnnxTensor.createTensor(
            ortEnv,
            FloatBuffer.wrap(bitmapToFloatArray(bitmap)),
            longArrayOf(1, 3, bitmap.height.toLong(), bitmap.width.toLong())
        )
    }

    private fun bitmapToFloatArray(bitmap: Bitmap): FloatArray {
        val width = bitmap.width
        val height = bitmap.height
        val pixels = width * height
        val floatArray = FloatArray(3 * pixels)

        val pixelData = IntArray(pixels)
        bitmap.getPixels(pixelData, 0, width, 0, 0, width, height)

        for (i in 0 until pixels) {
            val pixel = pixelData[i]
            floatArray[i] = (pixel shr 16 and 0xFF) / 255f
            floatArray[i + pixels] = (pixel shr 8 and 0xFF) / 255f
            floatArray[i + 2 * pixels] = (pixel and 0xFF) / 255f
        }

        return floatArray
    }

    fun postprocess(output: Array<Array<FloatArray>>, numClasses: Int = 7, confThreshold: Float = 0.25f): List<DetectionResult> {
        val detections = mutableListOf<DetectionResult>()
        val data = output[0]

        val numDetections = data[0].size
        for (i in 0 until numDetections) {
            val x = data[0][i]
            val y = data[1][i]
            val w = data[2][i]
            val h = data[3][i]

            val classConfs = FloatArray(numClasses) { clsIdx ->
                data[4 + clsIdx][i]
            }

            val best = classConfs.withIndex().maxByOrNull { it.value } ?: continue
            val classId = best.index
            val score = best.value

            if (score >= confThreshold) {
                detections.add(
                    DetectionResult(
                        classId = classId,
                        confidence = score,
                        box = floatArrayOf(x, y, w, h)
                    )
                )
            }
        }

        return detections
    }

    fun nms(
        detections: List<DetectionResult>,
        iouThreshold: Float = 0.5f
    ): List<DetectionResult> {
        val result = mutableListOf<DetectionResult>()
        val sorted = detections.sortedByDescending { it.confidence }.toMutableList()

        while (sorted.isNotEmpty()) {
            val best = sorted.removeAt(0)
            result.add(best)

            val iterator = sorted.iterator()
            while (iterator.hasNext()) {
                val det = iterator.next()
                if (iou(best.box, det.box) > iouThreshold) {
                    iterator.remove()
                }
            }
        }

        return result
    }

    private fun iou(boxA: FloatArray, boxB: FloatArray): Float {
        val (xA, yA, wA, hA) = boxA
        val (xB, yB, wB, hB) = boxB

        val leftA = xA - wA / 2
        val topA = yA - hA / 2
        val rightA = xA + wA / 2
        val bottomA = yA + hA / 2

        val leftB = xB - wB / 2
        val topB = yB - hB / 2
        val rightB = xB + wB / 2
        val bottomB = yB + hB / 2

        val interLeft = maxOf(leftA, leftB)
        val interTop = maxOf(topA, topB)
        val interRight = minOf(rightA, rightB)
        val interBottom = minOf(bottomA, bottomB)

        val interWidth = maxOf(0f, interRight - interLeft)
        val interHeight = maxOf(0f, interBottom - interTop)
        val interArea = interWidth * interHeight

        val areaA = wA * hA
        val areaB = wB * hB
        val unionArea = areaA + areaB - interArea

        return if (unionArea > 0f) interArea / unionArea else 0f
    }

}

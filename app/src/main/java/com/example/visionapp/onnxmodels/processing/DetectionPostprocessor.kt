package com.example.visionapp.onnxmodels.processing

import android.graphics.Bitmap
import com.example.visionapp.Mappings
import com.example.visionapp.ModelsConfig
import com.example.visionapp.onnxmodels.processing.Helpers.DetectionBitmapHelper

class DetectionPostprocessor : IPostprocessor<FloatArray, List<DetectionResult>> {

    override fun postprocess(modelOutput: Array<FloatArray>): List<DetectionResult> {
        val detections = mutableListOf<DetectionResult>()
        val numDetections = modelOutput[0].size

        for (i in 0 until numDetections) {
            val x = modelOutput[0][i]
            val y = modelOutput[1][i]
            val w = modelOutput[2][i]
            val h = modelOutput[3][i]

            val classConfs = FloatArray(ModelsConfig.DET_MODEL_NUM_OF_CLASSES) { clsIdx ->
                modelOutput[4 + clsIdx][i]
            }

            val best = classConfs.withIndex().maxByOrNull { it.value } ?: continue
            val classId = best.index
            val score = best.value

            if (score >= ModelsConfig.DET_MODEL_CONFIDENCE_THRESHOLD) {
                detections.add(
                    DetectionResult(
                        classId = classId,
                        confidence = score,
                        box = floatArrayOf(x, y, w, h)
                    )
                )
            }
        }
        val nmsDetections = nms(detections, ModelsConfig.DET_MODEL_IOU_THRESHOLD)
        return nmsDetections
    }

    override fun postprocessDebug(modelOutput: Array<FloatArray>, inputBitmap: Bitmap?): Bitmap? {
        val detections = postprocess(modelOutput)
        if (inputBitmap != null) {
            val imageWithBoxes = DetectionBitmapHelper.drawDetectionsOnBitmap(inputBitmap, detections)
            return imageWithBoxes
        }
        return null
    }

    private fun nms(
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
                if (getClassGroup(best.classId) == getClassGroup(det.classId)) {
                    if (iou(best.box, det.box) > iouThreshold) {
                        iterator.remove()
                    }
                }
            }
        }

        return result
    }

    private fun getClassGroup(classId: Int): String {
        return Mappings.DETECTION_NMS_GROUPS[classId]
            ?.toString()
            ?: throw IllegalArgumentException("No class group mapping found for classId: $classId")
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
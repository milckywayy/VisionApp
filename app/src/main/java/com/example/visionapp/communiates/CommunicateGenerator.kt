package com.example.visionapp.communiates

import com.example.visionapp.Mappings
import com.example.visionapp.onnxmodels.processing.DetectionResult

object CommunicateGenerator {
    fun generateCommunicatesFromDetection(detections: List<DetectionResult>) {
        for (detection in detections) {
            val type = getDetectionCommunicateType(detection.classId)
            if (type != null) {
                CommunicateQueue.add(Communicate(type))
            }
        }
    }

    fun generateCommunicatesFromTriangle(communicateClass: Int) {
        val type = getTriangleCommunicateType(communicateClass)
        if (type != null) {
            CommunicateQueue.add(Communicate(type))
        }
    }

    private fun getDetectionCommunicateType(classId: Int): CommunicateType? {
        return Mappings.DETECTION_COMMUNICATE_CLASSES[classId]
    }

    private fun getTriangleCommunicateType(classId: Int): CommunicateType? {
        return Mappings.TRIANGLE_COMMUNICATE_CLASSES[classId]
    }
}

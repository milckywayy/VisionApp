package com.example.visionapp.communiates

import com.example.visionapp.Mappings
import com.example.visionapp.model.SceneAnalysisResult
import com.example.visionapp.onnxmodels.processing.DetectionResult

object CommunicateGenerator {
    fun generateCommunicatesFromDetection(detections: List<DetectionResult>) {
        val foundTypes = mutableListOf<CommunicateType>()
        for (detection in detections) {
            val type = getDetectionCommunicateType(detection.classId)
            if (type != null) {
                foundTypes.add(type)
                if(CommunicateQueue.previousCommunicateStateMap[type] == true){
                    CommunicateQueue.add(Communicate(type))
                    CommunicateQueue.previousCommunicateStateMap[type] = false;
                } else {
                    CommunicateQueue.previousCommunicateStateMap[type] = true;
                }

            }
        }
        val categoriesToReset = setOf(
            CommunicateCategory.LIGHTS,
            CommunicateCategory.SIGNS,
            CommunicateCategory.CROSSING
        )

        for (type in CommunicateType.entries) {
            if (type.category in categoriesToReset && type !in foundTypes) {
                CommunicateQueue.previousCommunicateStateMap[type] = false
            }
        }
    }

    fun generateCommunicatesFromTriangle(communicateClass: SceneAnalysisResult) {
        val type = getTriangleCommunicateType(communicateClass)
        if (type != null) {
            if(CommunicateQueue.previousCommunicateStateMap[type] == true){
                CommunicateQueue.add(Communicate(type))
                CommunicateQueue.previousCommunicateStateMap[type] = false;
            } else {
                CommunicateQueue.previousCommunicateStateMap[type] = true;
            }
        }
        for (forType in CommunicateType.entries) {
            if (forType.category == CommunicateCategory.PASSAGE && forType != type ) {
                CommunicateQueue.previousCommunicateStateMap[forType] = false
            }
        }
    }

    private fun getDetectionCommunicateType(classId: Int): CommunicateType? {
        return Mappings.DETECTION_COMMUNICATE_CLASSES[classId]
    }

    private fun getTriangleCommunicateType(sceneAnalysisResult: SceneAnalysisResult): CommunicateType? {
        return Mappings.TRIANGLE_COMMUNICATE_CLASSES[sceneAnalysisResult.id]
    }
}

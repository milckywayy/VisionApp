package com.example.visionapp.onnxmodels.processing

data class DetectionResult(
    val classId: Int,
    val confidence: Float,
    val box: FloatArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DetectionResult

        if (classId != other.classId) return false
        if (confidence != other.confidence) return false
        if (!box.contentEquals(other.box)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = classId
        result = 31 * result + confidence.hashCode()
        result = 31 * result + box.contentHashCode()
        return result
    }
}
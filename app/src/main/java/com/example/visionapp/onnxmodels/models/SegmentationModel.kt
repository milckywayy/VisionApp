package com.example.visionapp.onnxmodels.models

import ai.onnxruntime.OnnxTensor
import android.util.Size

class SegmentationModel(resolution: Size) :
    OnnxModel<IntArray>(resolution) {

    override fun runInference(inputTensor: OnnxTensor): Array<IntArray> {
        val results = session.run(mapOf(session.inputNames.first() to inputTensor))
        val outputTensor = results[0].value as Array<Array<IntArray>>
        return outputTensor[0]
    }
}
package com.example.visionapp.onnxmodels.models

import ai.onnxruntime.OnnxTensor
import android.util.Size

class DepthModel(resolution: Size) :
    OnnxModel<FloatArray>(resolution) {

    override fun runInference(inputTensor: OnnxTensor): Array<FloatArray> {
        val results = session.run(mapOf(session.inputNames.first() to inputTensor))
        val outputTensor = results[0].value as Array<Array<FloatArray>>
        return outputTensor[0]
    }
}
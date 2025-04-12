package com.example.visionapp.onnxmodels.models

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtException
import ai.onnxruntime.OrtSession
import android.util.Size

abstract class OnnxModel<T>(resolution: Size) {
    var resolution: Size
    protected lateinit var session: OrtSession
    protected var env: OrtEnvironment = OrtEnvironment.getEnvironment()

    init {
        this.resolution = resolution
    }

    abstract fun runInference(inputTensor: OnnxTensor): Array<T>

    fun initModel(modelBytes: ByteArray){
        try {
            val sessionOptions = OrtSession.SessionOptions()
            this.session = env.createSession(modelBytes, sessionOptions)
        } catch (e: OrtException) {
            e.printStackTrace()
        }
    }

    fun closeSession() {
        session.close()
    }

    fun closeAll() {
        session.close()
        env.close()
    }
}
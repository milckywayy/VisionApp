package com.example.visionapp.onnxmodels

import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtException
import ai.onnxruntime.OrtSession

abstract class OnnxModel<T>(modelBytes: ByteArray, width: Int, height: Int) {
    protected lateinit var session: OrtSession
    protected var env: OrtEnvironment = OrtEnvironment.getEnvironment()
    protected var width: Int = 0
    protected var height: Int = 0

    init {
        try {
            val sessionOptions = OrtSession.SessionOptions()
            this.session = env.createSession(modelBytes, sessionOptions)
            this.width = width
            this.height = height
        } catch (e: OrtException) {
            e.printStackTrace()
        }
    }

    abstract fun runInference(inputArray: FloatArray): Array<T>


    fun closeSession() {
        session.close()
    }

    fun closeAll() {
        session.close()
        env.close()
    }
}
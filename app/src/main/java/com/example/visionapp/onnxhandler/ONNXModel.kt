package com.example.visionapp.onnxhandler

import ai.onnxruntime.*
import java.nio.FloatBuffer

class ONNXModel {
    private lateinit var session: OrtSession
    private lateinit var env: OrtEnvironment

    fun initModel(modelBytes: ByteArray) {
        try {
            env = OrtEnvironment.getEnvironment()
            val sessionOptions = OrtSession.SessionOptions()
            session = env.createSession(modelBytes, sessionOptions)
        } catch (e: OrtException) {
            e.printStackTrace()
        }
    }

fun runInference(inputArray: FloatArray, inputName: String = "input", outputName: String = "output"): Array<IntArray> {
    return try {
        val shape = longArrayOf(1, 3, 1024, 512)
        val inputTensor = OnnxTensor.createTensor(env, FloatBuffer.wrap(inputArray), shape)

        val results = session.run(mapOf(inputName to inputTensor))
        val outputTensor = results[0].value as Array<Array<IntArray>>

        outputTensor[0]
    } catch (e: OrtException) {
        e.printStackTrace()
        Array(0) { IntArray(0) }
    }
}

    fun close() {
        session.close()
        env.close()
    }
}

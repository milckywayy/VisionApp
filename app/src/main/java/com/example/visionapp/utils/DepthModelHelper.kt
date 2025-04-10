package com.example.visionapp.utils

import ai.onnxruntime.*
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Matrix
import android.util.Log
import java.nio.FloatBuffer
import kotlin.system.measureTimeMillis

class DepthModelHelper(
    private val ortEnv: OrtEnvironment,
    modelBytes: ByteArray
) {
    private val ortSession: OrtSession = ortEnv.createSession(modelBytes)
    private val inputName = ortSession.inputNames.iterator().next()
    private val inputDim = 280


    fun predictDepth(inputBitmap: Bitmap): Bitmap {
        val resizedBitmap = Bitmap.createScaledBitmap(inputBitmap, inputDim, inputDim, true)

        val floatArray = bitmapToFloatArray(resizedBitmap)
        val floatBuffer = FloatBuffer.wrap(floatArray)


        val tensor = OnnxTensor.createTensor(
            ortEnv,
            floatBuffer,
            longArrayOf(1, 3, inputDim.toLong(), inputDim.toLong())
        )


        lateinit var output: OnnxTensor
        val inferenceTime = measureTimeMillis {
            output = ortSession.run(mapOf(inputName to tensor))[0] as OnnxTensor
        }

        Log.d("VisionApp", "ONNX depth model inference took ${inferenceTime}ms")

        val outputArray = (output.value as Array<Array<FloatArray>>)[0]


        val depthBitmap = Bitmap.createBitmap(inputDim, inputDim, Bitmap.Config.ARGB_8888)
        for (y in 0 until inputDim) {
            for (x in 0 until inputDim) {
                val depthValue = (outputArray[y][x] * 255f).toInt().coerceIn(0, 255)
                val gray = Color.rgb(depthValue, depthValue, depthValue)
                depthBitmap.setPixel(x, y, gray)
            }
        }

        return Bitmap.createScaledBitmap(depthBitmap, inputBitmap.width, inputBitmap.height, true)
    }

    private fun bitmapToFloatArray(bitmap: Bitmap): FloatArray {
        val width = bitmap.width
        val height = bitmap.height
        val floatValues = FloatArray(3 * width * height)

        for (y in 0 until height) {
            for (x in 0 until width) {
                val pixel = bitmap.getPixel(x, y)
                val r = Color.red(pixel) / 255.0f
                val g = Color.green(pixel) / 255.0f
                val b = Color.blue(pixel) / 255.0f

                val index = y * width + x
                floatValues[index] = r
                floatValues[width * height + index] = g
                floatValues[2 * width * height + index] = b
            }
        }

        return floatValues
    }
}

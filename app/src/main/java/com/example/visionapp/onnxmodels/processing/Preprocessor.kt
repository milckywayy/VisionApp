package com.example.visionapp.onnxmodels.processing

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import android.graphics.Bitmap
import java.nio.FloatBuffer

object Preprocessor {

    fun createTensor(bitmap: Bitmap): OnnxTensor{
        return OnnxTensor.createTensor(
            OrtEnvironment.getEnvironment(),
            FloatBuffer.wrap(bitmapToFloatArray(bitmap)),
            longArrayOf(1, 3, bitmap.height.toLong(), bitmap.width.toLong())
        )
    }


    private fun bitmapToFloatArray(bitmap: Bitmap): FloatArray {
        val width = bitmap.width
        val height = bitmap.height
        val pixels = width * height
        val floatArray = FloatArray(3 * pixels)

        val pixelData = IntArray(pixels)
        bitmap.getPixels(pixelData, 0, width, 0, 0, width, height)

        for (i in 0 until pixels) {
            val pixel = pixelData[i]
            floatArray[i] = (pixel shr 16 and 0xFF) / 255f // R
            floatArray[i + pixels] = (pixel shr 8 and 0xFF) / 255f // G
            floatArray[i + 2 * pixels] = (pixel and 0xFF) / 255f // B
        }

        return floatArray
    }
}
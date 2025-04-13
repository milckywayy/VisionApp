package com.example.visionapp.onnxmodels.processing

import android.graphics.Bitmap
import android.graphics.Color
import androidx.core.graphics.createBitmap
import androidx.core.graphics.set


class DepthPostprocessor : IPostprocessor<FloatArray, Bitmap?> {

    override fun postprocess(modelOutput: Array<FloatArray>): Bitmap? {
        return createBitmap(modelOutput)
    }

    override fun postprocessDebug(modelOutput: Array<FloatArray>, inputBitmap: Bitmap?): Bitmap? {
        return createBitmap(modelOutput) // placeholder
    }

    private fun createBitmap(array: Array<FloatArray>): Bitmap? {
        if(array == null || array.isEmpty()){
            return null
        }
        val width = array[0].size
        val height = array.size
        val depthBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        for (x in 0 until width) {
            for (y in 0 until height) {
                val depthValue = (array[x][y] * 255f).toInt().coerceIn(0, 255)
                val gray = Color.rgb(depthValue, depthValue, depthValue)
                depthBitmap.setPixel(x, y, gray)
            }
        }

        return depthBitmap
    }
}
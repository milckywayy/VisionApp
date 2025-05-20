package com.example.visionapp.onnxmodels.processing

import android.graphics.Bitmap
import android.graphics.Color
import com.example.visionapp.Mappings
import com.example.visionapp.onnxmodels.processing.Helpers.SegmentationBitmapHelper

class SegmentationPostprocessor : IPostprocessor<IntArray, Bitmap?> {

    override fun postprocess(modelOutput: Array<IntArray>): Bitmap? {
        return createOutputBitmap(modelOutput)
    }

    override fun postprocessDebug(modelOutput: Array<IntArray>, inputBitmap: Bitmap?): Bitmap? {
        if(inputBitmap != null){
            return addColouredMaskToOriginalImage(modelOutput, inputBitmap)
        }
        else{
            return createOutputBitmap(modelOutput, createColoured = true)
        }
    }

    private fun createOutputBitmap(array: Array<IntArray>, createColoured: Boolean = false): Bitmap? {
        if (array.isEmpty()) return null
        val width = array[0].size
        val height = array.size
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val pixels = IntArray(width * height)

        for (y in 0 until height) {
            val row = array[y]
            for (x in 0 until width) {
                val index = y * width + x
                val value = row[x]
                val color: Int
                if (createColoured) {
                    color = getColorFromClassId(value)
                } else {
                    color = Color.rgb(value, value, value)
                }
                pixels[index] = color
            }
        }

        bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
        return bitmap
    }

    private fun addColouredMaskToOriginalImage(modelOutput: Array<IntArray>, inputBitmap: Bitmap): Bitmap {
        val width = inputBitmap.width
        val height = inputBitmap.height
        val origPixels = IntArray(width * height)
        inputBitmap.getPixels(origPixels, 0, width, 0, 0, width, height)

        val resultPixels = IntArray(width * height)

        for (y in 0 until height) {
            val row = modelOutput[y]
            for (x in 0 until width) {
                val index = y * width + x
                val origColor = origPixels[index]
                val overlayColor = Mappings.SEGMENTATION_COLOR_MAP[row[x]] ?: intArrayOf(0, 0, 0, 192)
                resultPixels[index] = SegmentationBitmapHelper.blendColors(origColor, overlayColor)
            }
        }

        val resultBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        resultBitmap.setPixels(resultPixels, 0, width, 0, 0, width, height)
        return resultBitmap
    }

    private fun getColorFromClassId(classId: Int): Int {
        val c = Mappings.SEGMENTATION_COLOR_MAP[classId] ?: intArrayOf(0, 0, 0, 192)
        return Color.argb(c[3], c[0], c[1], c[2])
    }
}
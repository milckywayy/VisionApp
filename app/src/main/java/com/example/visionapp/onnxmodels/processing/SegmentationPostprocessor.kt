package com.example.visionapp.onnxmodels.processing

import android.graphics.Bitmap
import android.graphics.Color
import androidx.core.graphics.createBitmap
import androidx.core.graphics.set

class SegmentationPostprocessor : IPostprocessor<IntArray, Array<IntArray>> {

    private val colorMap = mapOf(
        0   to intArrayOf(0,    0,      0,      192),   // road,        black
        1   to intArrayOf(168,  16,     243,    192),   // sidewalk,    purple
        2   to intArrayOf(250,  250,    55,     192),   // wall,        yellow
        3   to intArrayOf(250,  50,     83,     192),   // obstacle,    red
        4   to intArrayOf(0,    255,    0,      192),   // grass,       light-green
        5   to intArrayOf(51,   221,    255,    192),   // sky,         light-blue
        6   to intArrayOf(245,  147,    49,     192),   // person,      orange
        7   to intArrayOf(65,   65,     232,    192),   // vehicle,     dark-blue
        8   to intArrayOf(2,    100,    27,     192),   // vegetation,  dark-green
        9   to intArrayOf(37,   219,    188,    192),   // bike_path,   cyan
        10  to intArrayOf(255,  253,    208,    192),   // zebra,       beige
        11  to intArrayOf(204,  51,     102,    192)    // train,       claret
    )

    override fun postprocess(modelOutput: Array<IntArray>): Array<IntArray> {
        return modelOutput
    }

    override fun postprocessDebug(modelOutput: Array<IntArray>, inputBitmap: Bitmap?): Bitmap? {
        if(inputBitmap != null){
            return addColouredMaskToOriginalImage(modelOutput, inputBitmap)
        }
        else{
            return createColouredBitmap(modelOutput)
        }
    }

    private fun createColouredBitmap(array: Array<IntArray>): Bitmap? {
        if (array.isEmpty()) return null
        val width = array[0].size
        val height = array.size
        val bitmap = createBitmap(width, height, Bitmap.Config.ARGB_8888)

        for (y in 0 until height) {
            for (x in 0 until width) {
                bitmap[x, y] = getColorFromClassId(array[y][x])
            }
        }
        return bitmap
    }

    private fun addColouredMaskToOriginalImage(modelOutput: Array<IntArray>, inputBitmap: Bitmap): Bitmap {
        val width = inputBitmap.width
        val height = inputBitmap.height
        val resultBitmap = inputBitmap.copy(Bitmap.Config.ARGB_8888, true)

        for (y in 0 until height) {
            for (x in 0 until width) {
                val overlayColor = colorMap[modelOutput[y][x]] ?: intArrayOf(0, 0, 0, 192)
                val origColor = inputBitmap.getPixel(x, y)
                resultBitmap.setPixel(x, y, blendColors(origColor, overlayColor))
            }
        }
        return resultBitmap
    }

    private fun getColorFromClassId(classId: Int): Int {
        val c = colorMap[classId] ?: intArrayOf(0, 0, 0, 192)
        return Color.argb(c[3], c[0], c[1], c[2])
    }

    private fun blendColors(baseColor: Int, overlay: IntArray): Int {
        val alphaFactor = overlay[3] / 255f
        val r = ((1 - alphaFactor) * Color.red(baseColor) + alphaFactor * overlay[0]).toInt()
        val g = ((1 - alphaFactor) * Color.green(baseColor) + alphaFactor * overlay[1]).toInt()
        val b = ((1 - alphaFactor) * Color.blue(baseColor) + alphaFactor * overlay[2]).toInt()
        return Color.rgb(r, g, b)
    }
}
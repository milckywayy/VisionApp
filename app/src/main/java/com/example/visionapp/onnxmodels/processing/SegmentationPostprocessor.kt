package com.example.visionapp.onnxmodels.processing

import android.graphics.Bitmap
import android.graphics.Color
import androidx.core.graphics.createBitmap
import androidx.core.graphics.set

class SegmentationPostprocessor : IPostprocessor<IntArray, Bitmap?> {

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

    override fun postprocess(modelOutput: Array<IntArray>): Bitmap? {
        return createColouredBitmap(modelOutput)
    }

    private fun createColouredBitmap(array: Array<IntArray>): Bitmap? {
        if(array == null || array.isEmpty()){
            return null
        }
        val width = array[0].size
        val height = array.size

        val bitmap = createBitmap(width, height)

        for (y in 0 until height) {
            for (x in 0 until width) {
                val pixelValue = array[y][x]
                val color = colorMap[pixelValue] ?: intArrayOf(0, 0, 0, 192)
                val pixelColor = Color.argb(color[3], color[0], color[1], color[2])
                bitmap[x, y] = pixelColor
            }
        }

        return bitmap
    }
}
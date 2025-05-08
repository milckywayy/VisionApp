package com.example.visionapp.model

import android.graphics.Bitmap
import android.graphics.Color
import kotlin.math.max
import kotlin.math.min

object MedianFilter {

    fun applyMedianFilter(input: Bitmap, kernelSize: Int = 7): Bitmap {
        val width = input.width
        val height = input.height
        val radius = kernelSize / 2

        val inputPixels = IntArray(width * height)
        input.getPixels(inputPixels, 0, width, 0, 0, width, height)

        val outputBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val outputPixels = IntArray(width * height)

        val windowSize = kernelSize * kernelSize
        val rChannel = IntArray(windowSize)
        val gChannel = IntArray(windowSize)
        val bChannel = IntArray(windowSize)

        for (y in 0 until height) {
            for (x in 0 until width) {
                var count = 0

                for (dy in -radius..radius) {
                    for (dx in -radius..radius) {
                        val px = min(max(x + dx, 0), width - 1)
                        val py = min(max(y + dy, 0), height - 1)

                        val pixel = inputPixels[py * width + px]
                        rChannel[count] = Color.red(pixel)
                        gChannel[count] = Color.green(pixel)
                        bChannel[count] = Color.blue(pixel)
                        count++
                    }
                }

                rChannel.sort(0, count)
                gChannel.sort(0, count)
                bChannel.sort(0, count)

                val medianColor = Color.rgb(
                    rChannel[count / 2],
                    gChannel[count / 2],
                    bChannel[count / 2]
                )
                outputPixels[y * width + x] = medianColor
            }
        }

        outputBitmap.setPixels(outputPixels, 0, width, 0, 0, width, height)
        return outputBitmap
    }
}

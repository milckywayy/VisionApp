package com.example.visionapp.model

import android.graphics.Bitmap
import android.graphics.Color

object ModeFilter {

    fun applyModeFilter(input: Bitmap, kernelSize: Int = 7): Bitmap {
        val width = input.width
        val height = input.height
        val radius = kernelSize / 2

        val inputPixels = IntArray(width * height)
        input.getPixels(inputPixels, 0, width, 0, 0, width, height)

        val outputPixels = IntArray(width * height)

        for (y in 0 until height) {
            val colorCount = mutableMapOf<Int, Int>()

            for (dy in -radius..radius) {
                val py = y + dy
                if (py in 0 until height) {
                    for (dx in -radius..radius) {
                        val px = 0 + dx
                        if (px in 0 until width) {
                            val color = inputPixels[py * width + px]
                            colorCount[color] = colorCount.getOrDefault(color, 0) + 1
                        }
                    }
                }
            }

            for (x in 0 until width) {
                if (x > 0) {
                    for (dy in -radius..radius) {
                        val py = y + dy
                        if (py in 0 until height) {
                            val leftX = x - radius - 1
                            if (leftX in 0 until width) {
                                val leftColor = inputPixels[py * width + leftX]
                                colorCount[leftColor] = colorCount.getOrDefault(leftColor, 1) - 1
                                if (colorCount[leftColor] == 0) {
                                    colorCount.remove(leftColor)
                                }
                            }
                            val rightX = x + radius
                            if (rightX in 0 until width) {
                                val rightColor = inputPixels[py * width + rightX]
                                colorCount[rightColor] = colorCount.getOrDefault(rightColor, 0) + 1
                            }
                        }
                    }
                }

                val modeColor = colorCount.maxByOrNull { it.value }?.key ?: Color.BLACK
                outputPixels[y * width + x] = modeColor
            }
        }

        val outputBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        outputBitmap.setPixels(outputPixels, 0, width, 0, 0, width, height)
        return outputBitmap
    }
}

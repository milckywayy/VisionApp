package com.example.visionapp.model

import android.graphics.Bitmap
import android.graphics.Color

class TriangleMethod(
    depthBitmap: Bitmap,
    segmentationBitmap: Bitmap
) {
    val resultBitmap: Bitmap = combineBitmaps(depthBitmap, segmentationBitmap)

    companion object {
        private val nonValidClasses = mapOf(
            0 to "road",
            3 to "obstacle",
            7 to "vehicle",
            2 to "wall",
            8 to "vegetation",
            11 to "tram"
        )
    }

    private fun combineBitmaps(depthBitmap: Bitmap, segmentationBitmap: Bitmap): Bitmap {
        val scaledDepth = Bitmap.createScaledBitmap(depthBitmap, 512, 1024, true)
        val width = scaledDepth.width
        val height = scaledDepth.height

        val scaledSource = Bitmap.createScaledBitmap(segmentationBitmap, width, height, true)
        val outputBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        for (y in 0 until height) {
            for (x in 0 until width) {
                val grayPixel = scaledDepth.getPixel(x, y)
                val grayValue = Color.red(grayPixel)

                if (grayValue > 200) {
                    val colorPixel = scaledSource.getPixel(x, y)
                    outputBitmap.setPixel(x, y, colorPixel)
                } else {
                    outputBitmap.setPixel(x, y, Color.WHITE)
                }
            }
        }

        return outputBitmap
    }

    fun analyzeScene(): Int {
        val image = resultBitmap

        val line1 = findPixelsOnLine(-4.139394, 1400.1, 91..177, 668..1024)
        val line2 = findPixelsOnLine(4.139394, -718.685, 336..421, 668..1024)
        //val line3 = findPixelsOnLine(0.0, 668.0, 215..298, 668..669)

        val leftAndCrossing = checkLeftAndCrossing(image, line1, line2)

        if (leftAndCrossing["crossing"]!!.isEmpty()) {
            val right = checkRight(image, line2)
            val inFront = checkInFront(image)

            return when {
                inFront.isEmpty() && leftAndCrossing["left"]!!.isNotEmpty() && right.isNotEmpty() -> 1
                inFront.isEmpty() && leftAndCrossing["left"]!!.isNotEmpty() && right.isEmpty() -> 2
                inFront.isEmpty() && leftAndCrossing["left"]!!.isEmpty() && right.isNotEmpty() -> 3
                inFront.isEmpty() -> 0
                leftAndCrossing["left"]!!.isEmpty() && right.isEmpty() -> 4
                else -> 5
            }
        } else {
            return 5
        }
    }

    private fun findPixelsOnLine(
        m: Double,
        b: Double,
        xRange: IntRange,
        yRange: IntRange,
        tolerance: Int = 5
    ): List<Pair<Int, Int>> {
        val pixels = mutableListOf<Pair<Int, Int>>()
        for (x in xRange) {
            for (y in yRange) {
                val yOnLine = m * x + b
                if (Math.abs(y - yOnLine) <= tolerance) {
                    pixels.add(Pair(x, y))
                }
            }
        }
        return pixels
    }

    private fun checkLeftAndCrossing(
        image: Bitmap,
        line1: List<Pair<Int, Int>>,
        line2: List<Pair<Int, Int>>
    ): Map<String, MutableSet<String>> {
        val line2Cords = mutableMapOf<Int, Int>()
        for (p in line2) {
            line2Cords[p.second] = p.first
        }

        val result = mutableMapOf(
            "left" to mutableSetOf<String>(),
            "crossing" to mutableSetOf<String>()
        )

        for (point in line1) {
            val thing = Color.red(image.getPixel(point.first, point.second))
            if (thing in nonValidClasses) {
                val className = nonValidClasses[thing]!!
                var x1 = point.first
                val maxX = line2Cords[point.second] ?: continue
                while (x1 < maxX) {
                    x1++
                    if (x1 >= image.width) break
                    val nextColor = image.getPixel(x1, point.second)
                    if (nextColor == thing) continue else break
                }
                if (x1 >= maxX) {
                    result["crossing"]!!.add(className)
                } else {
                    result["left"]!!.add(className)
                }
            }
        }

        return result
    }

    private fun checkRight(image: Bitmap, line2: List<Pair<Int, Int>>): Set<String> {
        val found = mutableSetOf<String>()
        for (p in line2) {
            val thing = Color.red(image.getPixel(p.first, p.second))
            if (thing in nonValidClasses) {
                found.add(nonValidClasses[thing]!!)
            }
        }
        return found
    }

    private fun checkInFront(image: Bitmap): Set<String> {
        val p1 = Pair(214, 1024)
        val p2 = Pair(214, 683)
        val p3 = Pair(298, 1024)

        val found = mutableSetOf<String>()
        for (y in p2.second until p1.second) {
            for (x in p2.first until p3.first) {
                val thing = Color.red(image.getPixel(x, y))
                if (thing in nonValidClasses) {
                    found.add(nonValidClasses[thing]!!)
                }
            }
        }
        return found
    }
}

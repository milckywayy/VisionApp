package com.example.visionapp.model

import android.graphics.Bitmap
import android.graphics.Color
import com.example.visionapp.CameraConfig.SEGMENTATION_RESOLUTION
import com.example.visionapp.TriangleConfig

class TriangleMethod(
    depthBitmap: Bitmap,
    segmentationBitmap: Bitmap,
    private val depthThreshold: Int = 200
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
    /*
    0: 'brak',
    1: 'Wąskie przejście',
    2: 'Przesuń się do prawej',
    3: 'Przesuń się do lewej',
    4: 'Uwaga przeszkoda. Przesuń się gdzieś.',
    5: 'Zawróć',*/

    private fun combineBitmaps(depthBitmap: Bitmap, segmentationBitmap: Bitmap): Bitmap {
        val width = SEGMENTATION_RESOLUTION.width
        val height = SEGMENTATION_RESOLUTION.height
        val scaledDepth = Bitmap.createScaledBitmap(depthBitmap, width, height, true)
        val scaledSource = Bitmap.createScaledBitmap(segmentationBitmap, width, height, true)
        val outputBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        val depthPixels = IntArray(width * height)
        val sourcePixels = IntArray(width * height)
        val outputPixels = IntArray(width * height)

        scaledDepth.getPixels(depthPixels, 0, width, 0, 0, width, height)
        scaledSource.getPixels(sourcePixels, 0, width, 0, 0, width, height)

        for (i in depthPixels.indices) {
            val grayValue = Color.red(depthPixels[i])
            outputPixels[i] = if (grayValue > depthThreshold) sourcePixels[i] else Color.WHITE
        }
        outputBitmap.setPixels(outputPixels, 0, width, 0, 0, width, height)

        val final = ModeFilter.applyModeFilter(outputBitmap, 7)


         return final
    }

    fun analyzeScene(): Int {
        val image = resultBitmap

        val width = SEGMENTATION_RESOLUTION.width
        val height = SEGMENTATION_RESOLUTION.height
        val leftLine = findPixelsOnLine(TriangleConfig.LINE_1_a, TriangleConfig.LINE_1_b, (width/6).toInt()..(width/6*2).toInt(), (height/6*4).toInt()..(height-1).toInt())
        val rightLine = findPixelsOnLine(TriangleConfig.LINE_2_a, TriangleConfig.LINE_2_b, (width/6*4).toInt()..(width/6*5).toInt(), (height/6*4).toInt()..(height-1).toInt())
        //val line3 = findPixelsOnLine(0.0, 668.0, 215..298, 668..669)

        val imagePixels = IntArray(image.width * image.height)
        image.getPixels(imagePixels, 0, image.width, 0, 0, image.width, image.height)

        val (hasLeft, hasCrossing) = checkLeftAndCrossing(imagePixels, image.width, leftLine, rightLine)

        if (!hasCrossing) {
            val hasFront = checkInFront(imagePixels, image.width,image.height)
            val hasRight = checkRight(imagePixels, image.width, rightLine)

            return when {
                !hasFront && hasLeft && hasRight -> 1
                !hasFront && hasLeft && !hasRight -> 2
                !hasFront && !hasLeft && hasRight -> 3
                !hasFront -> 0
                !hasLeft && !hasRight -> 4
                else -> 5
            }
        } else {
            return 5
        }
    }

    private fun findPixelsOnLine(
        a: Double,
        b: Double,
        xRange: IntRange,
        yRange: IntRange,
        tolerance: Int = 5
    ): List<Pair<Int, Int>> {
        val pixels = mutableListOf<Pair<Int, Int>>()
        for (x in xRange) {
            val yOnLine = a * x + b
            val yStart = (yOnLine - tolerance).toInt()
            val yEnd = (yOnLine + tolerance).toInt()
            for (y in yStart..yEnd) {
                if (y in yRange) {
                    pixels.add(Pair(x, y))
                }
            }
        }
        return pixels
    }

    private fun checkLeftAndCrossing(
        pixels: IntArray,
        width: Int,
        leftLine: List<Pair<Int, Int>>,
        rightLine: List<Pair<Int, Int>>
    ): Pair<Boolean, Boolean>  {
        val line2Cords = mutableMapOf<Int, Int>()
        for (p in rightLine) {
            line2Cords[p.second] = p.first
        }

        var hasLeft = false
        var hasCrossing = false

        for (point in leftLine) {
            val (x, y) = point
            val classId = Color.red(pixels[y * width + x])
            if (classId in nonValidClasses) {
                var x1 = point.first
                val maxX = line2Cords[point.second] ?: continue
                while (x1 < maxX) {
                    x1++
                    if (x1 >= width) break
                    val nextColor = Color.red(pixels[y * width + x1])
                    if (nextColor == classId)
                        continue
                    else
                        break
                }
                if (x1 >= maxX) {
                    hasCrossing = true
                } else {
                    hasLeft = true
                }
            }
        }

        return Pair(hasLeft, hasCrossing)
    }

    private fun checkRight(pixels: IntArray, width: Int, line2: List<Pair<Int, Int>>): Boolean {
        for ((x,y) in line2) {
            val classId = Color.red(pixels[y * width + x])
            if (classId in nonValidClasses) {
                return true
            }
        }
        return false
    }

    private fun checkInFront(pixels: IntArray, width: Int, height: Int,
                             startXRatio: Double = 0.41,
                             endXRatio: Double = 0.58,
                             startYRatio: Double = 2.0 / 3.0): Boolean {
        /*
           top left point-->|\
                            | \
                            |  \
                            |   \
                            |    \
        bottom left point-->|_____\ <-- bottom right point
         */
        val topLeft = Pair((width*startXRatio).toInt(), height-1)
        val bottomLeft = Pair((width*startXRatio).toInt(),  (height * startYRatio).toInt())
        val bottomRight = Pair((width*endXRatio).toInt(), height-1)

        for (y in bottomLeft.second until topLeft.second) {
            for (x in bottomLeft.first until bottomRight.first) {
                val classId = Color.red(pixels[y * width + x])
                if (classId in nonValidClasses) {
                    return true
                }
            }
        }

        return false
    }
}


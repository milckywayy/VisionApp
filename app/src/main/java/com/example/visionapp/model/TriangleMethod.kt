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
            3 to "obstacle",
            7 to "vehicle",
            2 to "wall",
            8 to "vegetation",
            11 to "tram"
        )
        private val specialClasses = mapOf(
            0 to "road",
            6 to "person",
            9 to "bike_path"
        )
    }
    /*
    0: 'brak',
    1: 'Wąskie przejście',
    2: 'Przesuń się do prawej',
    3: 'Przesuń się do lewej',
    4: 'Uwaga przeszkoda. Przesuń się gdzieś.',
    5: 'Zawróć',
    6: 'Uwaga, wchodzisz na...'*/
    data class CheckResult(
        val hasForbidden: Boolean,
        val hasSpecial: Boolean,
        val specialClassNames: Set<String> = emptySet()
    )

    private fun combineBitmaps(depthBitmap: Bitmap, segmentationBitmap: Bitmap): Bitmap {
        val width = SEGMENTATION_RESOLUTION.width
        val height = SEGMENTATION_RESOLUTION.height
        val scaledDepth = Bitmap.createScaledBitmap(depthBitmap, width, height, true)
        val outputBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        val depthPixels = IntArray(width * height)
        val sourcePixels = IntArray(width * height)
        val outputPixels = IntArray(width * height)

        scaledDepth.getPixels(depthPixels, 0, width, 0, 0, width, height)
        segmentationBitmap.getPixels(sourcePixels, 0, width, 0, 0, width, height)

        for (i in depthPixels.indices) {
            val grayValue = Color.red(depthPixels[i])
            outputPixels[i] = if (grayValue > depthThreshold) sourcePixels[i] else Color.WHITE
        }
        outputBitmap.setPixels(outputPixels, 0, width, 0, 0, width, height)

        val final = ModeFilter.applyModeFilter(outputBitmap, 7)


        return final
    }

    fun analyzeScene(): SceneAnalysisResult {
        val image = resultBitmap

        val width = SEGMENTATION_RESOLUTION.width
        val height = SEGMENTATION_RESOLUTION.height
        val leftLine = findPixelsOnLine(TriangleConfig.LINE_1_a, TriangleConfig.LINE_1_b, (width / 6)..(width / 6 * 2), (height / 6 * 4)..(height - 1))
        val rightLine = findPixelsOnLine(TriangleConfig.LINE_2_a, TriangleConfig.LINE_2_b, (width / 6 * 4)..(width / 6 * 5), (height / 6 * 4)..(height - 1))

        val imagePixels = IntArray(image.width * image.height)
        image.getPixels(imagePixels, 0, image.width, 0, 0, image.width, image.height)

        val (leftCheck, crossingSet) = checkLeftAndCrossing(imagePixels, image.width, leftLine, rightLine)

        if (!crossingSet) {
            val rightCheck = checkRight(imagePixels, image.width, rightLine)
            val frontSet = checkInFront(imagePixels, image.width, image.height)

            if (!leftCheck.hasForbidden && leftCheck.hasSpecial) {
                return when {
                    "road" in leftCheck.specialClassNames -> SceneAnalysisResult.WARNING_ROAD
                    "bike_path" in leftCheck.specialClassNames -> SceneAnalysisResult.WARNING_BIKE_PATH
                    "person" in leftCheck.specialClassNames -> SceneAnalysisResult.WARNING_PERSON
                    else -> SceneAnalysisResult.NO_OBSTACLE // fallback
                }
            }

            if (!rightCheck.hasForbidden && rightCheck.hasSpecial) {
                return when {
                    "road" in rightCheck.specialClassNames -> SceneAnalysisResult.WARNING_ROAD
                    "bike_path" in rightCheck.specialClassNames -> SceneAnalysisResult.WARNING_BIKE_PATH
                    "person" in rightCheck.specialClassNames -> SceneAnalysisResult.WARNING_PERSON
                    else -> SceneAnalysisResult.NO_OBSTACLE
                }
            }

            if (!frontSet) {
                return when {
                    leftCheck.hasForbidden && rightCheck.hasForbidden -> SceneAnalysisResult.NARROW_PASSAGE
                    leftCheck.hasForbidden && !rightCheck.hasForbidden -> SceneAnalysisResult.MOVE_RIGHT
                    !leftCheck.hasForbidden && rightCheck.hasForbidden -> SceneAnalysisResult.MOVE_LEFT
                    else -> SceneAnalysisResult.NO_OBSTACLE
                }
            } else {
                return when {
                    leftCheck.hasForbidden && !rightCheck.hasForbidden -> SceneAnalysisResult.MOVE_RIGHT
                    !leftCheck.hasForbidden && rightCheck.hasForbidden -> SceneAnalysisResult.MOVE_LEFT
                    !leftCheck.hasForbidden && !rightCheck.hasForbidden -> SceneAnalysisResult.OBSTACLE_FRONT
                    else -> SceneAnalysisResult.TURN_AROUND
                }
            }
        } else {
            return SceneAnalysisResult.TURN_AROUND
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
    ): Pair<CheckResult, Boolean> {
        val line2Cords = mutableMapOf<Int, Int>()
        for (p in rightLine) {
            line2Cords[p.second] = p.first
        }

        var hasLeft = false
        var hasCrossing = false
        val specialClassesFound = mutableSetOf<String>()

        for ((x, y) in leftLine) {
            val classId = Color.red(pixels[y * width + x])
            when {
                classId in nonValidClasses -> {
                    var x1 = x
                    val maxX = line2Cords[y] ?: continue
                    while (x1 < maxX) {
                        x1++
                        if (x1 >= width) break
                        val nextColor = Color.red(pixels[y * width + x1])
                        if (nextColor == classId) continue
                        else break
                    }
                    if (x1 >= maxX) hasCrossing = true else hasLeft = true
                }
                classId in specialClasses -> {
                    specialClassesFound.add(specialClasses[classId]!!)
                }
            }
        }

        return Pair(CheckResult(hasLeft, specialClassesFound.isNotEmpty(), specialClassesFound), hasCrossing)
    }


    private fun checkRight(pixels: IntArray, width: Int, line2: List<Pair<Int, Int>>): CheckResult {
        val specialClassesFound = mutableSetOf<String>()

        for ((x, y) in line2) {
            val classId = Color.red(pixels[y * width + x])
            when {
                classId in nonValidClasses -> return CheckResult(true, false)
                classId in specialClasses -> specialClassesFound.add(specialClasses[classId]!!)
            }
        }

        return CheckResult(false, specialClassesFound.isNotEmpty(), specialClassesFound)
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
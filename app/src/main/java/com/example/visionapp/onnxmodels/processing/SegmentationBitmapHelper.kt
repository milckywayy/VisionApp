package com.example.visionapp.onnxmodels.processing

import android.graphics.Bitmap
import android.graphics.Color
import com.example.visionapp.Mappings

object SegmentationBitmapHelper {

    fun overlayColoredMaskOnImage(grayscaleMask: Bitmap, inputImage: Bitmap): Bitmap {
        val width = inputImage.width
        val height = inputImage.height

        val resizedMask = if (grayscaleMask.width != width || grayscaleMask.height != height) {
            Bitmap.createScaledBitmap(grayscaleMask, width, height, false)
        } else {
            grayscaleMask
        }

        val resultBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        val imagePixels = IntArray(width * height)
        val maskPixels = IntArray(width * height)
        val outputPixels = IntArray(width * height)

        inputImage.getPixels(imagePixels, 0, width, 0, 0, width, height)
        resizedMask.getPixels(maskPixels, 0, width, 0, 0, width, height)

        for (i in imagePixels.indices) {
            val baseColor = imagePixels[i]
            val gray = Color.red(maskPixels[i]) // grayscale: R == G == B
            val overlayColor = Mappings.SEGMENTATION_COLOR_MAP[gray] ?: intArrayOf(0, 0, 0, 0)
            outputPixels[i] = blendColors(baseColor, overlayColor)
        }

        resultBitmap.setPixels(outputPixels, 0, width, 0, 0, width, height)
        return resultBitmap
    }


    fun blendColors(baseColor: Int, overlay: IntArray): Int {
        val alpha = overlay[3]
        if (alpha == 0) return baseColor

        val alphaFactor = alpha / 255f

        val r = ((1 - alphaFactor) * Color.red(baseColor) + alphaFactor * overlay[0]).toInt()
        val g = ((1 - alphaFactor) * Color.green(baseColor) + alphaFactor * overlay[1]).toInt()
        val b = ((1 - alphaFactor) * Color.blue(baseColor) + alphaFactor * overlay[2]).toInt()

        return Color.rgb(r, g, b)
    }

}
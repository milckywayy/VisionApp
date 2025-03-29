package com.example.visionapp.utils

import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Size

fun rotateBitmap(source: Bitmap, angle: Float): Bitmap {
    val matrix = Matrix().apply { postRotate(angle) }
    return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
}

fun scaleBitmap(source: Bitmap, size: Size): Bitmap {
    return Bitmap.createScaledBitmap(source, size.width, size.height, false)
}
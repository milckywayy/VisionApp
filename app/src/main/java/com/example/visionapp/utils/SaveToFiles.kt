package com.example.visionapp.utils

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.provider.MediaStore
import com.example.visionapp.ModelsConfig
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


object SaveToFiles {

    fun saveBitmapToGalleryWithName(context: android.content.Context, bitmap: Bitmap, filename: String) {
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "$filename.png")
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/VisionApp")
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }

        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        uri?.let {
            resolver.openOutputStream(it).use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream!!)
            }
            contentValues.clear()
            contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
            resolver.update(uri, contentValues, null, null)
        }
    }


    var currentIndex = 0
    fun saveBitmapsToFiles(context: Context, originalBitmap: Bitmap, segmentedImage: Bitmap, detectionImage: Bitmap, depthImage: Bitmap) {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        currentIndex++
        if ( ModelsConfig.SAVE_TO_FILES ) {
            if (originalBitmap != null) {
                saveBitmapToGalleryWithName(context, originalBitmap, "image_${currentIndex}_original_$timestamp")
            }
            if (segmentedImage != null) {
                saveBitmapToGalleryWithName(context, segmentedImage, "image_${currentIndex}_segmentation_$timestamp")
            }
            if (detectionImage != null) {
                saveBitmapToGalleryWithName(context, detectionImage, "image_${currentIndex}_detection_$timestamp")
            }
            if (depthImage != null) {
                saveBitmapToGalleryWithName(context, depthImage, "image_${currentIndex}_depth_$timestamp")
            }
        }
    }
}
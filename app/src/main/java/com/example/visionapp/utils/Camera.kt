package com.example.visionapp.utils

import android.content.Context
import android.graphics.Bitmap
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.example.visionapp.CameraConfig

fun startCameraWithAnalyzer(
    context: Context,
    lifecycleOwner: LifecycleOwner,
    onFrame: (Bitmap) -> Unit,
    onProviderReady: (ProcessCameraProvider) -> Unit
) {
    val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

    cameraProviderFuture.addListener({
        val cameraProvider = cameraProviderFuture.get()
        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

        val imageAnalysis = ImageAnalysis.Builder()
            .setTargetResolution(CameraConfig.DEFAULT_RESOLUTION)
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()

        imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(context)) { image ->
            val bitmap = rotateBitmap(image.toBitmap(), 90F)
            image.close()
            onFrame(bitmap)
        }

        cameraProvider.unbindAll()
        cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, imageAnalysis)

        onProviderReady(cameraProvider)

    }, ContextCompat.getMainExecutor(context))
}


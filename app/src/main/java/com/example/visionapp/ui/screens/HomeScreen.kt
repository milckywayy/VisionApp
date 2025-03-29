package com.example.visionapp.ui.screens

import android.Manifest
import android.graphics.Bitmap
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.NavController
import com.example.visionapp.ui.common.TextButton
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.visionapp.utils.startCamera
import com.example.visionapp.utils.imageProxyToBitmap
import com.example.visionapp.CameraConfig
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun HomeScreen(navController: NavController) {
    val context = LocalContext.current
    val lifecycleOwner = context as LifecycleOwner

    var hasCameraPermission by remember { mutableStateOf(false) }
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var isCapturing by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val bitmapBuffer = remember { mutableStateListOf<Bitmap>() }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
        if (!isGranted) {
            Toast.makeText(context, "Brak uprawnień do aparatu!", Toast.LENGTH_SHORT).show()
        } else {
            startCamera(context, lifecycleOwner) { capture -> imageCapture = capture }
        }
    }

    LaunchedEffect(Unit) {
        requestPermissionLauncher.launch(Manifest.permission.CAMERA)
    }

    fun addToBuffer(bitmap: Bitmap) {
        if (bitmapBuffer.size == CameraConfig.MAX_BUFFER_SIZE) {
            bitmapBuffer.removeAt(0).recycle()
        }
        bitmapBuffer.add(bitmap)
    }

    fun capturePhoto() {
        if (!hasCameraPermission || imageCapture == null) return

        imageCapture?.takePicture(
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {
                    var bitmap = imageProxyToBitmap(image)
                    image.close()

                    coroutineScope.launch(Dispatchers.Default) {
                        val resized = withContext(Dispatchers.Default) {
                            listOf(
                                Bitmap.createScaledBitmap(
                                    bitmap,
                                    CameraConfig.SEGMENTATION_RESOLUTION.width,
                                    CameraConfig.SEGMENTATION_RESOLUTION.height,
                                    false
                                ),
                                Bitmap.createScaledBitmap(
                                    bitmap,
                                    CameraConfig.DETECTION_RESOLUTION.width,
                                    CameraConfig.DETECTION_RESOLUTION.height,
                                    false
                                ),
                                Bitmap.createScaledBitmap(
                                    bitmap,
                                    CameraConfig.DEPTH_RESOLUTION.width,
                                    CameraConfig.DEPTH_RESOLUTION.height,
                                    false
                                )
                            )
                        }
                    }

                    Log.d("XXXX", "Image captured")
                    // addToBuffer(depthBitmap)
                }

                override fun onError(exception: ImageCaptureException) {
                    Toast.makeText(context, "Błąd: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    fun startCapturing() {
        if (!hasCameraPermission) {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            return
        }

        isCapturing = true
        coroutineScope.launch {
            while (isCapturing) {
                capturePhoto()
                delay(CameraConfig.CAPTURE_DELAY_MS)
            }
        }
    }

    fun stopCapturing() {
        isCapturing = false
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        bitmapBuffer.lastOrNull()?.let { bitmap ->
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "Zrobione zdjęcie",
                modifier = Modifier.size(300.dp)
            )
        }

        TextButton(
            text = if (!isCapturing) "Start" else "Stop",
            onClick = { if (isCapturing) stopCapturing() else startCapturing() }
        )
    }
}

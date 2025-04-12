package com.example.visionapp.ui.screens

import android.Manifest
import android.graphics.Bitmap
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.NavController
import com.example.visionapp.ui.common.TextButton
import com.example.visionapp.CameraConfig
import androidx.camera.lifecycle.ProcessCameraProvider
import com.example.visionapp.ModelsConfig
import com.example.visionapp.onnxmodels.ModelPredictor
import com.example.visionapp.onnxmodels.models.DetectionModel
import com.example.visionapp.onnxmodels.models.SegmentationModel
import com.example.visionapp.onnxmodels.processing.DetectionPostprocessor
import com.example.visionapp.onnxmodels.processing.DetectionResult
import com.example.visionapp.onnxmodels.processing.SegmentationPostprocessor
import com.example.visionapp.utils.startCameraWithAnalyzer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(navController: NavController) {
    val context = LocalContext.current
    val lifecycleOwner = context as LifecycleOwner
    val coroutineScope = rememberCoroutineScope()

    var hasCameraPermission by remember { mutableStateOf(false) }
    var isCapturing by remember { mutableStateOf(false) }
    var cameraProvider by remember { mutableStateOf<ProcessCameraProvider?>(null) }
    val bitmapBuffer = remember { mutableStateListOf<Bitmap>() }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
        if (!isGranted) {
            Toast.makeText(context, "Missing camera permission", Toast.LENGTH_SHORT).show()
        }
    }

    val segModel = remember { SegmentationModel(CameraConfig.SEGMENTATION_RESOLUTION) }
    val segPostprocessor = remember { SegmentationPostprocessor() }
    val segModelPredictor = remember { ModelPredictor<IntArray, Array<IntArray>>(segModel, segPostprocessor) }
    val detModel = remember { DetectionModel(CameraConfig.DETECTION_RESOLUTION) }
    val detPostprocessor = remember { DetectionPostprocessor() }
    val detModelPredictor = remember { ModelPredictor<FloatArray, List<DetectionResult>>(detModel, detPostprocessor) }


    LaunchedEffect(Unit) {
        requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        val segModelBytes = context.assets.open(ModelsConfig.SEG_MODEL_PATH).readBytes()
        segModel.initModel(segModelBytes)
        val detModelBytes = context.assets.open(ModelsConfig.DET_MODEL_PATH).readBytes()
        detModel.initModel(detModelBytes)
    }

    fun addBitmapToBuffer(bitmap: Bitmap) {
        if (bitmapBuffer.size == CameraConfig.MAX_BUFFER_SIZE) {
            bitmapBuffer.removeAt(0).recycle()
        }
        bitmapBuffer.add(bitmap)
    }

    fun processImage(bitmap: Bitmap) {
        val segmentedImage = segModelPredictor.makePredictionsDebug(bitmap)
        val detectionImage = detModelPredictor.makePredictionsDebug(bitmap)

        if (segmentedImage != null) {
            addBitmapToBuffer(segmentedImage)
        }
        /*if (detectionImage != null) {
            addBitmapToBuffer(detectionImage)
        }*/
    }

    fun startCapturing() {
        if (!hasCameraPermission) {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            return
        }

        var lastProcessedTime = 0L
        isCapturing = true

        startCameraWithAnalyzer(
            context = context,
            lifecycleOwner = lifecycleOwner,
            onFrame = { bitmap ->
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastProcessedTime >= CameraConfig.CAPTURE_DELAY_MS) {
                    lastProcessedTime = currentTime

                    coroutineScope.launch(Dispatchers.Default) {
                        processImage(bitmap)
                    }
                } else {
                    bitmap.recycle()
                }
            },
            onProviderReady = { provider ->
                cameraProvider = provider
            }
        )
    }

    fun stopCapturing() {
        bitmapBuffer.forEach { it.recycle() }
        bitmapBuffer.clear()
        isCapturing = false
        cameraProvider?.unbindAll()
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        bitmapBuffer.lastOrNull()?.let { bitmap ->
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "Photo taken",
                modifier = Modifier.size(300.dp)
            )
        }

        TextButton(
            text = if (!isCapturing) "Start" else "Stop",
            onClick = { if (isCapturing) stopCapturing() else startCapturing() }
        )
    }
}

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
import com.example.visionapp.communiates.CommunicateGenerator
import com.example.visionapp.model.TriangleMethod
import com.example.visionapp.onnxmodels.ModelPredictor
import com.example.visionapp.onnxmodels.models.DepthModel
import com.example.visionapp.onnxmodels.models.DetectionModel
import com.example.visionapp.onnxmodels.models.SegmentationModel
import com.example.visionapp.onnxmodels.processing.DepthPostprocessor
import com.example.visionapp.onnxmodels.processing.DetectionPostprocessor
import com.example.visionapp.onnxmodels.processing.DetectionResult
import com.example.visionapp.onnxmodels.processing.SegmentationPostprocessor
import com.example.visionapp.utils.startCameraWithAnalyzer
import com.example.visionapp.onnxmodels.ModelType
import com.example.visionapp.onnxmodels.processing.Helpers.DetectionBitmapHelper
import com.example.visionapp.onnxmodels.processing.Helpers.SegmentationBitmapHelper
import com.example.visionapp.processing.DetectionProcessing
import com.example.visionapp.ui.common.DropdownMenuControl
import com.example.visionapp.utils.TextToSpeechManager
import com.example.visionapp.utils.scaleBitmap
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
    var isProcessing by remember { mutableStateOf(false) }
    val imageStack = remember { mutableStateListOf<Bitmap>() }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
        if (!isGranted) {
            Toast.makeText(context, "Missing camera permission", Toast.LENGTH_SHORT).show()
        }
    }
    var selectedModelDebug by remember { mutableStateOf<ModelType?>(null) }

    val segModel = remember { SegmentationModel(CameraConfig.SEGMENTATION_RESOLUTION) }
    val segPostprocessor = remember { SegmentationPostprocessor() }
    val segModelPredictor = remember { ModelPredictor<IntArray, Bitmap?>(segModel, segPostprocessor) }
    val detModel = remember { DetectionModel(CameraConfig.DETECTION_RESOLUTION) }
    val detPostprocessor = remember { DetectionPostprocessor() }
    val detModelPredictor = remember { ModelPredictor<FloatArray, List<DetectionResult>>(detModel, detPostprocessor) }
    val depthModel = remember { DepthModel(CameraConfig.DEPTH_RESOLUTION) }
    val depthPostprocessor = remember { DepthPostprocessor() }
    val depthModelPredictor = remember { ModelPredictor<FloatArray, Bitmap?>(depthModel, depthPostprocessor) }

    val tts = remember { TextToSpeechManager(context) }

    LaunchedEffect(Unit) {
        requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        val segModelBytes = context.assets.open(ModelsConfig.SEG_MODEL_PATH).readBytes()
        segModel.initModel(segModelBytes)
        val detModelBytes = context.assets.open(ModelsConfig.DET_MODEL_PATH).readBytes()
        detModel.initModel(detModelBytes)
        val depthModelBytes = context.assets.open(ModelsConfig.DEPTH_MODEL_PATH).readBytes()
        depthModel.initModel(depthModelBytes)
    }

    fun addBitmapToBuffer(bitmap: Bitmap) {
        if (bitmapBuffer.size == CameraConfig.MAX_BUFFER_SIZE) {
            bitmapBuffer.removeAt(0).recycle()
        }
        bitmapBuffer.add(bitmap)
    }

    fun processImage(bitmap: Bitmap) {
        val segmentedImage = segModelPredictor.makePredictions(bitmap)
        val detectionResults = detModelPredictor.makePredictions(bitmap)
        val depthImage = depthModelPredictor.makePredictions(bitmap)

        if (segmentedImage != null && depthImage != null){
            val processedDetectionResults = DetectionProcessing.processDetectionsByBoxSize(detectionResults, segmentedImage)
            CommunicateGenerator.generateCommunicatesFromDetection(processedDetectionResults)
            val triangle = TriangleMethod(depthImage, segmentedImage)
            CommunicateGenerator.generateCommunicatesFromTriangle(triangle.analyzeScene())
        }

        if(ModelsConfig.VISUAL_DEBUG_MODE){
            when (selectedModelDebug){
                ModelType.DEPTH -> depthImage?.let { addBitmapToBuffer(it) }
                ModelType.DETECTION -> {
                    val scaledImageBitmap = scaleBitmap(bitmap, CameraConfig.DETECTION_RESOLUTION)
                    val imageWithBoxes = DetectionBitmapHelper.drawDetectionsOnBitmap(scaledImageBitmap, detectionResults)
                    addBitmapToBuffer(imageWithBoxes)
                }
                ModelType.SEGMENTATION -> segmentedImage?.let{
                    val scaledImageBitmap = scaleBitmap(bitmap, CameraConfig.SEGMENTATION_RESOLUTION)
                    val imageWithOverlay = SegmentationBitmapHelper.overlayColoredMaskOnImage(segmentedImage, scaledImageBitmap)
                    addBitmapToBuffer(imageWithOverlay)
                }
                else -> {}
            }

        }
    }

    fun processImageDebug(bitmap: Bitmap) {
        val segmentedImage = segModelPredictor.makePredictionsDebug(bitmap)
        val detectionImage = detModelPredictor.makePredictionsDebug(bitmap)
        val depthImage = depthModelPredictor.makePredictionsDebug(bitmap)

        when (selectedModelDebug){
            ModelType.DEPTH -> depthImage?.let { addBitmapToBuffer(it) }
            ModelType.DETECTION -> detectionImage?.let { addBitmapToBuffer(it) }
            ModelType.SEGMENTATION -> segmentedImage?.let { addBitmapToBuffer(it) }
            else -> segmentedImage?.let { addBitmapToBuffer(it) }
        }
    }

    fun analyzeNextImage() {
        if (isProcessing || imageStack.isEmpty()) return

        isProcessing = true
        val bitmapToProcess = imageStack.removeAt(imageStack.lastIndex)

        coroutineScope.launch(Dispatchers.Default) {
            try {
                if (ModelsConfig.VISUAL_DEBUG_ONLY_MODE) {
                    processImageDebug(bitmapToProcess)
                } else {
                    processImage(bitmapToProcess)
                }
            } finally {
                isProcessing = false
            }
        }
    }

    fun startCapturing() {
        if (!hasCameraPermission) {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            return
        }
        isCapturing = true

        startCameraWithAnalyzer(
            context = context,
            lifecycleOwner = lifecycleOwner,
            onFrame = { bitmap ->
                imageStack.forEach { it.recycle() }
                imageStack.clear()
                imageStack.add(bitmap)
                analyzeNextImage()
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

        imageStack.forEach { it.recycle() }
        imageStack.clear()
        isProcessing = false

        cameraProvider?.unbindAll()
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if(ModelsConfig.VISUAL_DEBUG_ONLY_MODE || ModelsConfig.VISUAL_DEBUG_MODE) {
            DropdownMenuControl(
                items = ModelType.entries,
                selectedItem = selectedModelDebug,
                initialText = "Select which output to display",
                onItemSelected = { selectedModelDebug = it },
                labelProvider = { it.label }
            )
        }

        bitmapBuffer.lastOrNull()?.let { bitmap ->
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "Photo taken",
                modifier = Modifier.size(500.dp)
            )
        }

        TextButton(
            text = if (!isCapturing) "Start" else "Stop",
            onClick = { if (isCapturing) stopCapturing() else startCapturing() }
        )
    }
}

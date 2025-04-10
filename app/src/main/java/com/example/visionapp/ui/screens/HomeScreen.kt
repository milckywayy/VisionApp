package com.example.visionapp.ui.screens

import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import android.Manifest
import android.content.res.AssetManager
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
import com.example.visionapp.utils.DepthModelHelper
import com.example.visionapp.utils.OnnxModelHelper
import com.example.visionapp.utils.scaleBitmap
import com.example.visionapp.utils.startCameraWithAnalyzer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun HomeScreen(navController: NavController) {
    val context = LocalContext.current
    val lifecycleOwner = context as LifecycleOwner
    val coroutineScope = rememberCoroutineScope()

    var hasCameraPermission by remember { mutableStateOf(false) }
    var isCapturing by remember { mutableStateOf(false) }
    var cameraProvider by remember { mutableStateOf<ProcessCameraProvider?>(null) }
    val bitmapBuffer = remember { mutableStateListOf<Bitmap>() }

    val ortEnv = OrtEnvironment.getEnvironment()
    val detectionHelper = remember { mutableStateOf<OnnxModelHelper?>(null) }
    val detectionSession = remember { mutableStateOf<OrtSession?>(null) }

    val depthHelper = remember { mutableStateOf<DepthModelHelper?>(null) }

    fun initializeModel(assetManager: AssetManager) {
        coroutineScope.launch(Dispatchers.IO) {
            val helper = OnnxModelHelper(ortEnv)
            val detectionBytes = assetManager.open("detekcja_ir9.onnx").use { it.readBytes() }
            val depthBytes = assetManager.open("vits_280_quantized.onnx").use { it.readBytes() }

            val session = ortEnv.createSession(detectionBytes)
            val depthModel = DepthModelHelper(ortEnv, depthBytes)

            detectionHelper.value = helper
            detectionSession.value = session
            depthHelper.value = depthModel
        }
    }



    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
        if (!isGranted) {
            Toast.makeText(context, "Brak uprawnień do aparatu!", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        requestPermissionLauncher.launch(Manifest.permission.CAMERA)
    }

    LaunchedEffect(Unit) {
        requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        initializeModel(context.assets) // <-- DODAJ TO
    }


    fun addBitmapToBuffer(bitmap: Bitmap) {
        if (bitmapBuffer.size == CameraConfig.MAX_BUFFER_SIZE) {
            bitmapBuffer.removeAt(0).recycle()
        }
        bitmapBuffer.add(bitmap)
    }

    fun drawDetectionsOnBitmap(bitmap: Bitmap, detections: List<OnnxModelHelper.DetectionResult>): Bitmap {
        val result = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = android.graphics.Canvas(result)
        val paint = android.graphics.Paint().apply {
            style = android.graphics.Paint.Style.STROKE
            color = android.graphics.Color.RED
            strokeWidth = 3f
        }
        val textPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.YELLOW
            textSize = 32f
        }

        val imgWidth = bitmap.width.toFloat()
        val imgHeight = bitmap.height.toFloat()

        for (det in detections) {
            val (x, y, w, h) = det.box
            val left = (x - w / 2) * imgWidth
            val top = (y - h / 2) * imgHeight
            val right = (x + w / 2) * imgWidth
            val bottom = (y + h / 2) * imgHeight

            canvas.drawRect(left, top, right, bottom, paint)
            canvas.drawText("Cls ${det.classId}: ${"%.2f".format(det.confidence)}", left, top - 10, textPaint)
        }

        return result
    }

    fun processImage(bitmap: Bitmap) {
        val detectionImage = scaleBitmap(bitmap, CameraConfig.DETECTION_RESOLUTION)
        val depthImage = scaleBitmap(bitmap, CameraConfig.DEPTH_RESOLUTION)

        coroutineScope.launch(Dispatchers.Default) {
            val helper = detectionHelper.value ?: return@launch
            val session = detectionSession.value ?: return@launch
            val depthModel = depthHelper.value ?: return@launch

            val inputTensor = helper.createTensor(detectionImage)
            val output = session.run(mapOf(session.inputNames.first() to inputTensor))
            val rawDetections = helper.postprocess(output[0].value as Array<Array<FloatArray>>)
            val detections = helper.nms(rawDetections)
            val imageWithBoxes = drawDetectionsOnBitmap(detectionImage, detections)

            val depthBitmap = depthModel.predictDepth(depthImage)

            withContext(Dispatchers.Main) {
                addBitmapToBuffer(depthBitmap)
            }
        }
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

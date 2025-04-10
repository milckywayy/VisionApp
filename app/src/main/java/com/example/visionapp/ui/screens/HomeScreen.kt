package com.example.visionapp.ui.screens

import android.Manifest
import android.graphics.Bitmap
import android.graphics.Color
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
import com.example.visionapp.utils.scaleBitmap
import com.example.visionapp.utils.startCameraWithAnalyzer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.example.visionapp.onnxhandler.ONNXModel

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
            Toast.makeText(context, "Brak uprawnień do aparatu!", Toast.LENGTH_SHORT).show()
        }
    }

    val segModel = remember { ONNXModel() }


    LaunchedEffect(Unit) {
        requestPermissionLauncher.launch(Manifest.permission.CAMERA)

        val modelBytes = context.assets.open("pp_liteseg_2.onnx").readBytes()
        segModel.initModel(modelBytes)
    }

    fun addBitmapToBuffer(bitmap: Bitmap) {
        if (bitmapBuffer.size == CameraConfig.MAX_BUFFER_SIZE) {
            bitmapBuffer.removeAt(0).recycle()
        }
        bitmapBuffer.add(bitmap)
    }

    fun bitmapToFloatArray(bitmap: Bitmap): FloatArray {
        val width = bitmap.width
        val height = bitmap.height
        val pixels = width * height
        val floatArray = FloatArray(3 * pixels)

        val pixelData = IntArray(pixels)
        bitmap.getPixels(pixelData, 0, width, 0, 0, width, height)

        for (i in 0 until pixels) {
            val pixel = pixelData[i]
            floatArray[i] = (pixel shr 16 and 0xFF) / 255f // R
            floatArray[i + pixels] = (pixel shr 8 and 0xFF) / 255f // G
            floatArray[i + 2 * pixels] = (pixel and 0xFF) / 255f // B
        }

        return floatArray
    }


    fun mapArrayToBitmap(array: Array<IntArray>): Bitmap {
        val width = 512
        val height = 1024

        val colorMap = mapOf(
            0 to intArrayOf(0, 0, 0, 192), // road, black
            1 to intArrayOf(168, 16, 243, 192), // sidewalk, purple
            2 to intArrayOf(250, 250, 55, 192), // wall, yellow
            3 to intArrayOf(250, 50, 83, 192), // obstacle, red
            4 to intArrayOf(0, 255, 0, 192), // grass, light-green
            5 to intArrayOf(51, 221, 255, 192), // sky, light-blue
            6 to intArrayOf(245, 147, 49, 192), // person, orange
            7 to intArrayOf(65, 65, 232, 192), // vehicle, dark-blue
            8 to intArrayOf(2, 100, 27, 192), // vegetation, dark-green
            9 to intArrayOf(37, 219, 188, 192), // bike_path, cyan?
            10 to intArrayOf(255, 253, 208, 192), // zebra, beige
            11 to intArrayOf(204, 51, 102, 192) // train, claret
        )


        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        for (y in 0 until height) {
            for (x in 0 until width) {
                val pixelValue = array[y][x]
                val color = colorMap[pixelValue] ?: intArrayOf(0, 0, 0, 192)
                val pixelColor = Color.argb(color[3].toInt(), color[0].toInt(), color[1].toInt(), color[2].toInt())
                bitmap.setPixel(x, y, pixelColor)
            }
        }

        return bitmap
    }


    fun processImage(bitmap: Bitmap) {
        val segmentationImage = scaleBitmap(bitmap, CameraConfig.SEGMENTATION_RESOLUTION)
        val detectionImage = scaleBitmap(bitmap, CameraConfig.DETECTION_RESOLUTION)
        val depthImage = scaleBitmap(bitmap, CameraConfig.DEPTH_RESOLUTION)

        val input = bitmapToFloatArray(segmentationImage)

        val output = segModel.runInference(
            inputArray = input,
            inputName = "x",
            outputName = "output"
        )

        val outputBitmap = mapArrayToBitmap(output)
        addBitmapToBuffer(outputBitmap)
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

package com.example.visionapp

import android.util.Size
import java.util.Locale


object LocalizationConfig {
    val DEFAULT_TTS_LANGUAGE: Locale = Locale.ENGLISH
}

object CameraConfig {
    val DEFAULT_RESOLUTION = Size(640, 1024)
    val SEGMENTATION_RESOLUTION = Size(512, 1024)
    val DETECTION_RESOLUTION = Size(640, 640)
    val DEPTH_RESOLUTION = Size(280 , 280)
    const val MAX_BUFFER_SIZE = 5
    const val CAPTURE_DELAY_MS = 1500L
}

object ModelsConfig {
    const val SEG_MODEL_PATH = "pp_liteseg.onnx"
    const val DET_MODEL_PATH = "detection.onnx"
    const val DEPTH_MODEL_PATH = "vits_280.onnx"
    const val DET_MODEL_NUM_OF_CLASSES = 7
    const val DET_MODEL_CONFIDENCE_THRESHOLD = 0.25f
    const val DET_MODEL_IOU_THRESHOLD = 0.5f
    const val VISUAL_DEBUG_MODE = true
}

object CommunicateConfig{
    const val CLEANUP_CHECK_INTERVAL_MS = 2_000L
    const val COMMUNICATE_MAX_AGE_MS = 10_000L
}
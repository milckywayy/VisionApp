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
    const val DET_MODEL_NUM_OF_CLASSES = 13
    const val DET_MODEL_CONFIDENCE_THRESHOLD = 0.25f
    const val DET_MODEL_IOU_THRESHOLD = 0.5f

    val NMS_CLASSES_GROUPS = mapOf( //to discuss
        0 to "green-light",
        1 to "sign",
        2 to "sign",
        3 to "red-light",
        4 to "sign",
        5 to "sign",
        6 to "zebra",
        7 to "green-light",
        8 to "red-light",
        9 to "green-light",
        10 to "red-light",
        11 to "sign",
        12 to "sign"
    )

    val DETECTION_DISTANCE_ESTIMATION_GROUPS = mapOf(
        0 to "pedestrian-light",
        1 to "pedestrians-lane-sign",
        2 to "pedestrians-lane-sign",
        3 to "pedestrian-light",
        4 to "pedestrians-lane-sign",
        5 to "no-pedestrians",
        6 to "crosswalk",
        7 to "bike-green-light",
        8 to "bike-red-light",
        9 to "car-green-light",
        10 to "car-red-light",
        11 to "pedestrians-lane-sign",
        12 to "pedestrians-lane-sign"
    )
}
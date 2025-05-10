package com.example.visionapp

import android.util.Size
import com.example.visionapp.communiates.CommunicateType
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
}

object ModelsConfig {
    const val SEG_MODEL_PATH = "pp_liteseg.onnx"
    const val DET_MODEL_PATH = "detection.onnx"
    const val DEPTH_MODEL_PATH = "vits_280.onnx"
    const val DET_MODEL_NUM_OF_CLASSES = 13
    const val DET_MODEL_CONFIDENCE_THRESHOLD = 0.25f
    const val DET_MODEL_IOU_THRESHOLD = 0.5f
    const val VISUAL_DEBUG_MODE = false

    enum class DetectionNmsGroup {
        GREEN_LIGHT,
        SIGN,
        RED_LIGHT,
        ZEBRA
    }

    enum class DetectionDistanceGroup {
        PEDESTRIAN_LIGHT,
        PEDESTRIANS_LANE_SIGN,
        NO_PEDESTRIANS,
        CROSSWALK,
        BIKE_GREEN_LIGHT,
        BIKE_RED_LIGHT,
        CAR_GREEN_LIGHT,
        CAR_RED_LIGHT
    }

    val DETECTION_NMS_GROUPS = mapOf(
        0 to DetectionNmsGroup.GREEN_LIGHT,
        1 to DetectionNmsGroup.SIGN,
        2 to DetectionNmsGroup.SIGN,
        3 to DetectionNmsGroup.RED_LIGHT,
        4 to DetectionNmsGroup.SIGN,
        5 to DetectionNmsGroup.SIGN,
        6 to DetectionNmsGroup.ZEBRA,
        7 to DetectionNmsGroup.GREEN_LIGHT,
        8 to DetectionNmsGroup.RED_LIGHT,
        9 to DetectionNmsGroup.GREEN_LIGHT,
        10 to DetectionNmsGroup.RED_LIGHT,
        11 to DetectionNmsGroup.SIGN,
        12 to DetectionNmsGroup.SIGN
    )

    val DETECTION_DISTANCE_ESTIMATION_GROUPS = mapOf(
        0 to DetectionDistanceGroup.PEDESTRIAN_LIGHT,
        1 to DetectionDistanceGroup.PEDESTRIANS_LANE_SIGN,
        2 to DetectionDistanceGroup.PEDESTRIANS_LANE_SIGN,
        3 to DetectionDistanceGroup.PEDESTRIAN_LIGHT,
        4 to DetectionDistanceGroup.PEDESTRIANS_LANE_SIGN,
        5 to DetectionDistanceGroup.NO_PEDESTRIANS,
        6 to DetectionDistanceGroup.CROSSWALK,
        7 to DetectionDistanceGroup.BIKE_GREEN_LIGHT,
        8 to DetectionDistanceGroup.BIKE_RED_LIGHT,
        9 to DetectionDistanceGroup.CAR_GREEN_LIGHT,
        10 to DetectionDistanceGroup.CAR_RED_LIGHT,
        11 to DetectionDistanceGroup.PEDESTRIANS_LANE_SIGN,
        12 to DetectionDistanceGroup.PEDESTRIANS_LANE_SIGN
    )

    val DETECTION_COMMUNICATE_CLASSES = mapOf(
        0 to CommunicateType.GREEN_LIGHT,
        1 to CommunicateType.PEDESTRIANS_TO_THE_LEFT,
        2 to CommunicateType.PEDESTRIANS_TO_THE_RIGHT,
        3 to CommunicateType.RED_LIGHT,
        4 to CommunicateType.COMMON_AREA,
        5 to CommunicateType.NO_PEDESTRIANS,
        6 to CommunicateType.CROSSING
    )

    val TRIANGLE_COMMUNICATE_CLASSES = mapOf(
        1 to CommunicateType.NARROW_PASSAGE,
        2 to CommunicateType.MOVE_RIGHT,
        3 to CommunicateType.MOVE_LEFT,
        4 to CommunicateType.OBSTACLE,
        5 to CommunicateType.NO_PASSAGE
    )
}

object CommunicateConfig{
    const val CLEANUP_CHECK_INTERVAL_MS = 2_000L
    const val COMMUNICATE_MAX_AGE_MS = 10_000L
}

object TriangleConfig {
    const val LINE_1_a = -4.139394
    const val LINE_1_b = 1400.1
    const val LINE_2_a = 4.139394
    const val LINE_2_b =-718.685
}
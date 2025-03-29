package com.example.visionapp

import android.util.Size


object CameraConfig {
    val DEFAULT_RESOLUTION = Size(640, 1024)
    val SEGMENTATION_RESOLUTION = Size(512, 1024)
    val DETECTION_RESOLUTION = Size(640, 640)
    val DEPTH_RESOLUTION = Size(210 , 210)
    const val MAX_BUFFER_SIZE = 5
    const val CAPTURE_DELAY_MS = 50L
}
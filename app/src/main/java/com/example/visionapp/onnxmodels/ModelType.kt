package com.example.visionapp.onnxmodels

enum class ModelType(val label: String) {
    DEPTH("Depth"),
    DETECTION("Detection"),
    SEGMENTATION("Segmentation");

    override fun toString(): String = label
}
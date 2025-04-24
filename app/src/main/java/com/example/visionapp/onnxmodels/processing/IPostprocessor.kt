package com.example.visionapp.onnxmodels.processing

import android.graphics.Bitmap

interface IPostprocessor<TParam,TReturn> {
    fun postprocess(modelOutput: Array<TParam>): TReturn
    fun postprocessDebug(modelOutput: Array<TParam>, inputBitmap: Bitmap? = null): Bitmap?
}
package com.example.visionapp.onnxmodels.processing

interface IPostprocessor<TParam,TReturn> {
    fun postprocess(modelOutput: Array<TParam>): TReturn
}
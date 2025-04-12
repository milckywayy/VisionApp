package com.example.visionapp.onnxmodels

interface IPostprocessor<TParam,TReturn> {
    fun postprocess(modelOutput: Array<TParam>): TReturn
}
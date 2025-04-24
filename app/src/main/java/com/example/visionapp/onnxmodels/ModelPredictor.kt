package com.example.visionapp.onnxmodels

import android.graphics.Bitmap
import com.example.visionapp.onnxmodels.models.OnnxModel
import com.example.visionapp.onnxmodels.processing.IPostprocessor
import com.example.visionapp.onnxmodels.processing.Preprocessor
import com.example.visionapp.utils.scaleBitmap

class ModelPredictor<TParam, TReturn>(model: OnnxModel<TParam>, postprocessor: IPostprocessor<TParam, TReturn>) {
    protected val model: OnnxModel<TParam>
    protected val postprocessor: IPostprocessor<TParam, TReturn>

    init{
        this.model = model
        this.postprocessor = postprocessor
    }

    fun makePredictions(inputBitmap: Bitmap) : TReturn{
        val image = scaleBitmap(inputBitmap, model.resolution)
        val modelPreds = makePredictionsWithModel(image)
        val finalPreds = postprocessor.postprocess(modelPreds)
        return finalPreds
    }

    fun makePredictionsDebug(inputBitmap: Bitmap): Bitmap?{
        val image = scaleBitmap(inputBitmap, model.resolution)
        val modelPreds = makePredictionsWithModel(image)
        val finalPreds = postprocessor.postprocessDebug(modelPreds, image)
        return finalPreds
    }

    private fun makePredictionsWithModel(image: Bitmap): Array<TParam>{
        val inputTensor = Preprocessor.createTensor(image)
        val modelPreds = model.runInference(inputTensor)
        return modelPreds
    }
}
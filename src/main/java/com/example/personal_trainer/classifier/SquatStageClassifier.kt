package com.example.personal_trainer.classifier

import android.content.Context
import android.util.Log
import androidx.camera.core.ImageProxy
import com.example.personal_trainer.ml.SquatClassifierModel
import com.example.personal_trainer.utils.ApplicationUtils


class SquatStageClassifier : AbstractStageClassifier {

    private val squatModelClassifier: SquatClassifierModel

    constructor(context: Context) : super(ApplicationUtils.squat) {
        squatModelClassifier = context?.let { SquatClassifierModel.newInstance(it) }!!
    }

    override fun classifyExerciseStage(imageProxy: ImageProxy): String {

        Log.d("squatClassifier", "Starting the classification of the squat for current frame...")
        val inputFeature = transformImageFrameInTensorInput(imageProxy)

        Log.d("squatClassifier", "Processing the input feature...")
        val outputs: SquatClassifierModel.Outputs = squatModelClassifier.process(inputFeature)

        Log.d("squatClassifier", "Predicting the content of the current frame")

        return getPredictionResultLabel(outputs.outputFeature0AsTensorBuffer)
    }
}
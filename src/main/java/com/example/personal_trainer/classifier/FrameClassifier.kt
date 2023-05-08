package com.example.personal_trainer.classifier

import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import java.nio.ByteBuffer

class FrameClassifier(private val classifier: AbstractStageClassifier) : ImageAnalysis.Analyzer {

    private fun ByteBuffer.toByteArray(): ByteArray {
        rewind()    // Rewind the buffer to zero
        val data = ByteArray(remaining())
        get(data)   // Copy the buffer into a byte array
        return data // Return the byte array
    }
    override fun analyze(image: ImageProxy) {
        Log.d("classifying", "Got a frame")
        Thread.sleep(100)
        classifier.classifyExerciseStage(image)
        image.close()
    }
}
package com.example.personal_trainer.classifier

import android.util.Log
import android.widget.TextView
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.example.personal_trainer.model.RepetitionCounter
import org.w3c.dom.Text
import java.nio.ByteBuffer

class FrameClassifier(private val classifier: AbstractStageClassifier, private val viewToUpdate: TextView) : ImageAnalysis.Analyzer {

    val repCounter = RepetitionCounter()
    private fun ByteBuffer.toByteArray(): ByteArray {
        rewind()    // Rewind the buffer to zero
        val data = ByteArray(remaining())
        get(data)   // Copy the buffer into a byte array
        return data // Return the byte array
    }
    override fun analyze(image: ImageProxy) {

        val frameLabel = classifier.classifyExerciseStage(image)
        Log.d("updateText", frameLabel)
        viewToUpdate.text = frameLabel
        image.close()
    }
}
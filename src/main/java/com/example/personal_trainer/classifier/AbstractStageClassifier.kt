package com.example.personal_trainer.classifier

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.media.Image
import android.util.Log
import androidx.camera.core.ImageProxy
import com.example.personal_trainer.utils.ApplicationUtils
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.nio.ByteBuffer
import java.nio.ByteOrder

abstract class AbstractStageClassifier(private val exerciseName: String) {

    @SuppressLint("UnsafeOptInUsageError")
    fun transformImageFrameInTensorInput(imageProxy: ImageProxy): TensorBuffer {
        val img: Image? = imageProxy.image
        val image: Bitmap? = img?.let { ApplicationUtils.toBitmap(it) }
        val inputFeature0 =
            TensorBuffer.createFixedSize(
                intArrayOf(
                    1,
                    ApplicationUtils.modelImageSize,
                    ApplicationUtils.modelImageSize,
                    3
                ), DataType.FLOAT32
            )
        val byteBuffer =
            ByteBuffer.allocateDirect(4 * ApplicationUtils.modelImageSize * ApplicationUtils.modelImageSize * 3)
        byteBuffer.order(ByteOrder.nativeOrder())
        val intValues = IntArray(ApplicationUtils.modelImageSize * ApplicationUtils.modelImageSize)
        image!!.getPixels(
            intValues,
            0,
            ApplicationUtils.modelImageSize,
            0,
            0,
            ApplicationUtils.modelImageSize,
            ApplicationUtils.modelImageSize
        )
        var pixel = 0
        for (i in 0 until ApplicationUtils.modelImageSize) {
            for (j in 0 until ApplicationUtils.modelImageSize) {
                val `val` = intValues[pixel++] // RGB
                byteBuffer.putFloat((`val` shr 16 and 0xFF) * (1f / 255))
                byteBuffer.putFloat((`val` shr 8 and 0xFF) * (1f / 255))
                byteBuffer.putFloat((`val` and 0xFF) * (1f / 255))
            }
        }
        inputFeature0.loadBuffer(byteBuffer)

        return inputFeature0;
    }

     abstract fun classifyExerciseStage(imageProxy: ImageProxy): String ;

    fun getPredictionResultLabel(
        outputFeature: TensorBuffer
    ): String {
        val labels = ApplicationUtils.exerciseList[exerciseName]
        val probabilities = outputFeature.floatArray
        var maxIndex = 0
        var maxValue = probabilities[0]
        Log.d("probability", "Up:"  + probabilities[0]);
        Log.d("probability","Down" +  probabilities[1]);

        probabilities.withIndex().forEach {
            if (it.value > maxValue) {
                maxValue = it.value
                maxIndex = it.index
            }
        }

        return labels!!.get(maxIndex)
    }
}
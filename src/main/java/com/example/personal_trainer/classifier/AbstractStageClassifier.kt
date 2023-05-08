package com.example.personal_trainer.classifier

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.media.Image
import android.util.Log
import androidx.camera.core.ImageProxy
import com.example.personal_trainer.ApplicationUtils
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.nio.ByteBuffer
import java.nio.ByteOrder

open class AbstractStageClassifier {

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

    open fun classifyExerciseStage(imageProxy: ImageProxy): String {
        TODO("Not yet implemented")
    }

    var count = 0;
    fun getPredictionResultLabel(
        labels: List<String>,
        outputFeature: TensorBuffer
    ): String {
        val probabilities = outputFeature.floatArray
        var maxIndex = 0
        var maxValue = probabilities[0]
        probabilities.withIndex().forEach {
            if (it.value > maxValue) {
                maxValue = it.value
                maxIndex = it.index
            }
        }
        Log.d("label", labels[maxIndex])
        if(labels[maxIndex] == "endPosition"){
            count++
            Log.d("label","" + count )
        }
        return labels[maxIndex]
    }
}
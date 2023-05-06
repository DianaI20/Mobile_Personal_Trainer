package com.example.personal_trainer

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.media.Image
import android.util.Log
import androidx.camera.core.ImageProxy
import com.example.personal_trainer.ml.PersonalTrainerModel
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.nio.ByteBuffer
import java.nio.ByteOrder


class Classifier {
    private var context: Context? = null
    var tflite: Interpreter? = null
    val ASSOCIATED_AXIS_LABELS = "labels.txt"
    val associatedAxisLabels: List<String>? = listOf("down", "up")
    val model: PersonalTrainerModel
    constructor(context: Context) {
         model = context?.let { PersonalTrainerModel.newInstance(it) }!!
    }

    @SuppressLint("UnsafeOptInUsageError")
    fun classify(imageProxy: ImageProxy): String? {

        val img: Image? = imageProxy.image
        val image: Bitmap? = img?.let { Utils.toBitmap(it) }
        val imageSize = 224
        // Creates inputs for reference.
        // Creates inputs for reference.
        val inputFeature0 =
            TensorBuffer.createFixedSize(intArrayOf(1, 224, 224, 3), DataType.FLOAT32)
        val byteBuffer = ByteBuffer.allocateDirect(4 * imageSize * imageSize * 3)
        byteBuffer.order(ByteOrder.nativeOrder())
        val intValues = IntArray(imageSize * imageSize)
        image!!.getPixels(intValues, 0, 224, 0, 0, 224, 224)
        var pixel = 0
        //iterate over each pixel and extract R, G, and B values. Add those values individually to the byte buffer.
        //iterate over each pixel and extract R, G, and B values. Add those values individually to the byte buffer.
        for (i in 0 until imageSize) {
            for (j in 0 until imageSize) {
                val `val` = intValues[pixel++] // RGB
                byteBuffer.putFloat((`val` shr 16 and 0xFF) * (1f / 255))
                byteBuffer.putFloat((`val` shr 8 and 0xFF) * (1f / 255))
                byteBuffer.putFloat((`val` and 0xFF) * (1f / 255))
            }
        }
        inputFeature0.loadBuffer(byteBuffer)
        Log.d("inputFeature", "")
        // Runs model inference and gets result.
        val outputs: PersonalTrainerModel.Outputs = model.process(inputFeature0)
        val outputFeature0 = outputs.outputFeature0AsTensorBuffer
        val confidences = outputFeature0.floatArray
        // find the index of the class with the biggest confidence.
        // find the index of the class with the biggest confidence.
        val maxPos = 0
        val maxConfidence = 0f

        Log.d("confidence", "up with probability  " + confidences[0])
        Log.d("confidence", "down with probability  " + confidences[1])


// Runs model inference and gets result.

//        val img: Image? = image.image
//        val bitmap: Bitmap? = img?.let { Utils.toBitmap(it) }
//        val rotation: Int = Utils.getImageRotation(image)
//        val width = bitmap!!.width
//        val height = bitmap!!.height
//        val size = if (height > width) width else height
//        val imageProcessor: ImageProcessor = ImageProcessor.Builder()
//            .add(ResizeWithCropOrPadOp(size, size))
//            .add(ResizeOp(224, 224, ResizeOp.ResizeMethod.BILINEAR))
//            .build()
//        var tensorImage = TensorImage(DataType.FLOAT32)
//        tensorImage.load(bitmap)
//        tensorImage = imageProcessor.process(tensorImage)
//        val probabilityBuffer = TensorBuffer.createFixedSize(intArrayOf(1, 1001), DataType.UINT8)
//        if (null != tflite) {
//            tflite!!.run(tensorImage.buffer, probabilityBuffer.buffer)
//        }
//        val probabilityProcessor = TensorProcessor.Builder().add(NormalizeOp(0f, 255f)).build()
//        var result = " "
//        if (null != associatedAxisLabels) {
//            // Map of labels and their corresponding probability
//            probabilityProcessor.process(probabilityBuffer)
////            val labels = TensorLabel(
////                associatedAxisLabels!!,
////
////            )
//            Log.d("probability","Index 0 with confidence: " + probabilityBuffer.floatArray[0])
//            Log.d("probability","Index 1 with confidence: " + probabilityBuffer.floatArray[1])
//            // Create a map to access the result based on label
//      //      val floatMap = labels.mapWithFloatValue
//        //    result = Utils.writeResults(floatMap)
//        }
//        return result
        return ""
    }
}
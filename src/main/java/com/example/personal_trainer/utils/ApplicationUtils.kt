package com.example.personal_trainer.utils

import android.content.Context
import android.graphics.*
import android.media.Image
import android.util.Log
import com.example.personal_trainer.R
import com.example.personal_trainer.classifier.AbstractStageClassifier
import com.example.personal_trainer.classifier.SquatStageClassifier
import java.io.ByteArrayOutputStream

object ApplicationUtils {
    val squatStages = listOf<String>("up", "down")
    val pushupStages = listOf<String>("up", "down")
    val exerciseList = hashMapOf(
        "Squat" to squatStages,
        "Pushup" to pushupStages
    )
    val stages = listOf(squatStages)
    val modelImageSize = 224
    val noOfSecondsBeforeExercise = 5.0

    fun findDrawableResourceId(context: Context, name: String): Int {
        val uri = "@drawable/$name"
        var resourceId: Int = context.resources.getIdentifier(
            uri, "drawable",
            context.packageName
        )
        if (resourceId == 0) {
            Log.d("findDrawableResourceId", "Did not find resource for image with name: $name")
            resourceId = R.drawable.default_icon

        }

        return resourceId
    }

    fun toBitmap(image: Image): Bitmap {
        val planes = image.planes
        val yBuffer = planes[0].buffer
        val uBuffer = planes[1].buffer
        val vBuffer = planes[2].buffer
        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()
        val nv21 = ByteArray(ySize + uSize + vSize)
        //U and V are swapped
        yBuffer[nv21, 0, ySize]
        vBuffer[nv21, ySize, vSize]
        uBuffer[nv21, ySize + vSize, uSize]
        val yuvImage = YuvImage(nv21, ImageFormat.NV21, image.width, image.height, null)
        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, yuvImage.width, yuvImage.height), 100, out)
        val imageBytes = out.toByteArray()
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    }

    fun initializeClassifier(
        exerciseCode: Int,
        applicationContext: Context
    ): AbstractStageClassifier {
        return when (exerciseCode) {
            0 -> {
                Log.d("selection", "Squat was selected to be identified")
                SquatStageClassifier(applicationContext)
            }
            1 -> {
                Log.d("selection", "Push up was selected to be identified")
                SquatStageClassifier(applicationContext);
            }
            else -> { // Note the block
                // Do nothing

                throw ExceptionInInitializerError(
                    "Error initializing the classifier"
                )
            }
        }
    }

    fun convertMilisIntoText(milis: Long): String {
        val seconds = milis / 1000
        return "0$seconds"
    }
}
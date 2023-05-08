package com.example.personal_trainer.service

import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.util.Log
import com.example.personal_trainer.CameraActivity

class ModelSelectorHelper(private val context: Context, private var position: Int) {

    fun loadModelAndGoToCameraActivity() {
        Log.d("modelSelector", "Trying to initialize the model...")

        when (position) {
            0 -> loadSquatModelAndGoToCameraActivity()
            1 -> Log.d("selection", "Push up was selected to be identified")
            else -> { // Note the block
                Log.d("modelSelector", "Invalid position:$position")
            }
        }
    }

    private fun loadSquatModelAndGoToCameraActivity() {
        Log.d("modelSelector", "Loading squat model...")
        val intent = Intent(context, CameraActivity::class.java)
        intent.putExtra("exercise", position)
        intent.addFlags(FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}
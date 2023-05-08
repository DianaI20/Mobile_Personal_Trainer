package com.example.personal_trainer.adapter

import android.content.Context
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.personal_trainer.R
import com.example.personal_trainer.model.Exercise
import com.example.personal_trainer.service.ModelSelectorHelper

// Holds the views for adding it to image and text
class ExerciseViewHolder(itemView: View, context: Context) :
    RecyclerView.ViewHolder(itemView) {

    private var imageView: ImageView
    private var textView: TextView

    init {
        imageView = itemView.findViewById(R.id.imageview)
        textView = itemView.findViewById(R.id.textView)

        itemView.setOnClickListener {
            Log.d("modelSelector", "Clicked on exercise...")
            Log.d("modelSelector", "Position $absoluteAdapterPosition")
            var modelSelector = ModelSelectorHelper(context, absoluteAdapterPosition)
            modelSelector.loadModelAndGoToCameraActivity()
        }
    }

    fun bindData(data: Exercise) {
        imageView.setImageResource(data.imageResourceId)
        textView.text = data.name
    }
}
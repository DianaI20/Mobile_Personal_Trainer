package com.example.personal_trainer.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.personal_trainer.R
import com.example.personal_trainer.model.Exercise

class ExerciseViewAdapter (private val itemList: List<Exercise>, private val context: Context) : RecyclerView.Adapter<ExerciseViewHolder>() {
    // create new views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExerciseViewHolder {
        // inflates the card_view_design view
        // that is used to hold list item
        val view = LayoutInflater.from(parent.context).inflate(R.layout.card_view_design, parent, false)
        return ExerciseViewHolder(view, context)
    }

    // binds the list items to a view
    override fun onBindViewHolder(holder: ExerciseViewHolder, position: Int) {

        val data = itemList[position]
        holder.bindData(data)
    }

    // return the number of the items in the list
    override fun getItemCount(): Int {
        return itemList.size
    }

}

package com.example.personal_trainer

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.personal_trainer.adapter.ExerciseViewAdapter
import com.example.personal_trainer.databinding.ActivityExerciseMenuBinding
import com.example.personal_trainer.model.Exercise


class ExerciseRecyclerViewList : AppCompatActivity() {

    private lateinit var viewBinding: ActivityExerciseMenuBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityExerciseMenuBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        viewBinding.recyclerview.layoutManager = LinearLayoutManager(this)
        val adapter = ExerciseViewAdapter(getExerciseListItems(), applicationContext)
        viewBinding.recyclerview.adapter = adapter
    }

    private fun getExerciseListItems(): ArrayList<Exercise> {
        val exerciseListMenu = ArrayList<Exercise>()

        for (i in ApplicationUtils.exerciseList) {
            val imageName = (i + "_icon").toLowerCase()
            Log.d("imageName", "" + imageName)
            val image = ApplicationUtils.findDrawableResourceId(applicationContext, imageName)
            exerciseListMenu.add(
                Exercise(
                    i,
                    "TODO",
                    image
                )
            )
        }

        return exerciseListMenu
    }
}
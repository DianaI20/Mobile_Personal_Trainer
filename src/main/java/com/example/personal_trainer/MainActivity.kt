package com.example.personal_trainer

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.personal_trainer.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var viewBinding : ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        val cameraButton: Button = viewBinding.cameraButton
        cameraButton.setOnClickListener {
            val intent = Intent(this, ExerciseRecyclerViewList::class.java)
            startActivity(intent);
        }
    }
}

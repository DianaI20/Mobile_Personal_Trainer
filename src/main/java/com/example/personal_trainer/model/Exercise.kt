package com.example.personal_trainer.model

data class Exercise(
    var name: String,
    var descritpion: String,
    var imageResourceId: Int,
    var stages: List<String>? = null
)

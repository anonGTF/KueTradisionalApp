package com.bimo.kuetradisionalapp.model

data class RecipeData(
    val ingredients: List<String>,
    val resource: String,
    val steps: List<String>,
    val youtube_id: String
)
package com.bimo.kuetradisionalapp.model

data class RecipeResponse(
    val `data`: RecipeData,
    val meta: Meta,
    val pagination: Pagination,
    val version: Version
)
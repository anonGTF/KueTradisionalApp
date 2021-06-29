package com.bimo.kuetradisionalapp.data.remote

import com.bimo.kuetradisionalapp.model.KueTradisionalResponse
import com.bimo.kuetradisionalapp.model.RecipeResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface KueTradisionalApi {

    @GET("{label_kue}")
    suspend fun getKue(
        @Path("label_kue") labelKue: String
    ): Response<KueTradisionalResponse>

    @GET("recipe/{label_kue}")
    suspend fun getKueRecipe(
        @Path("label_kue") labelKue: String
    ): Response<RecipeResponse>
}
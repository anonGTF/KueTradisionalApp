package com.bimo.kuetradisionalapp.data

import com.bimo.kuetradisionalapp.data.remote.KueTradisionalApi
import com.bimo.kuetradisionalapp.model.KueTradisionalResponse
import com.bimo.kuetradisionalapp.model.RecipeResponse
import retrofit2.Response
import javax.inject.Inject

class KueTradisionalRepository(
    private val api: KueTradisionalApi
) {

    suspend fun getKue(labelKue: String): Response<KueTradisionalResponse> = api.getKue(labelKue)

    suspend fun getKueRecipe(labelKue: String): Response<RecipeResponse> = api.getKueRecipe(labelKue)

}
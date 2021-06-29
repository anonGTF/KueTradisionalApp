package com.bimo.kuetradisionalapp.detail

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bimo.kuetradisionalapp.data.KueTradisionalRepository
import com.bimo.kuetradisionalapp.model.RecipeData
import com.bimo.kuetradisionalapp.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.lang.Exception
import javax.inject.Inject

class DetailViewModel(
    private val repository: KueTradisionalRepository
) : ViewModel() {

    val data: MutableLiveData<Resource<RecipeData>> = MutableLiveData()

    fun getRecipe(title: String) = viewModelScope.launch {
        data.postValue(Resource.Loading())
        try {
            val response = repository.getKueRecipe(title)
            data.postValue(Resource.Success(response.body()!!.data))
        }
        catch (e: Exception) {
            data.postValue(Resource.Error(e.localizedMessage ?: "error"))
        }
    }

}
package com.bimo.kuetradisionalapp.result

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bimo.kuetradisionalapp.data.KueTradisionalRepository
import com.bimo.kuetradisionalapp.model.KueTradisionalData
import com.bimo.kuetradisionalapp.util.Classifier
import com.bimo.kuetradisionalapp.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

class ResultViewModel(
    private val repository: KueTradisionalRepository
): ViewModel() {

    val data: MutableLiveData<Resource<KueTradisionalData>> = MutableLiveData()

    fun detect(classifier: Classifier, bitmap: Bitmap, sensorOrientation: Int) = viewModelScope.launch {
        data.postValue(Resource.Loading())
        try {
            val results: List<Classifier.Recognition> =
                classifier.recognizeImage(bitmap, sensorOrientation)
            if (results.isNotEmpty()) {
                val recognition: Classifier.Recognition = results[0]
                val response = recognition.title?.let { repository.getKue(it) }
                Log.d("coba", "detect: $response")
                Log.d("coba", "detect: $recognition")
                Log.d("coba", "detect: $sensorOrientation")
                data.postValue(Resource.Success(response?.body()!!.data))
            }
        } catch (e: IOException) {
            data.postValue(Resource.Error(e.localizedMessage!!))
            Log.e("classifier", e.localizedMessage!!)
        }
    }

}
package com.bimo.kuetradisionalapp.util

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.bimo.kuetradisionalapp.data.KueTradisionalRepository
import com.bimo.kuetradisionalapp.data.remote.KueTradisionalApi
import com.bimo.kuetradisionalapp.detail.DetailViewModel
import com.bimo.kuetradisionalapp.result.ResultViewModel

class ViewModelProviderFactory(
    val api: KueTradisionalApi
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(DetailViewModel::class.java) -> {
                DetailViewModel(KueTradisionalRepository(api)) as T
            }
            modelClass.isAssignableFrom(ResultViewModel::class.java) -> {
                ResultViewModel(KueTradisionalRepository(api)) as T
            }
            else -> throw Throwable("Unknown ViewModel Type")
        }
    }
}
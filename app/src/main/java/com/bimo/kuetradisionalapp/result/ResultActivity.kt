package com.bimo.kuetradisionalapp.result

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import com.bimo.kuetradisionalapp.data.remote.RetrofitInstance
import com.bimo.kuetradisionalapp.data.remote.RetrofitInstance.Companion.api
import com.bimo.kuetradisionalapp.databinding.ActivityResultBinding
import com.bimo.kuetradisionalapp.detail.DetailActivity
import com.bimo.kuetradisionalapp.detail.DetailActivity.Companion.EXTRA_KUE_NAME
import com.bimo.kuetradisionalapp.detail.DetailViewModel
import com.bimo.kuetradisionalapp.model.KueTradisionalData
import com.bimo.kuetradisionalapp.util.Classifier
import com.bimo.kuetradisionalapp.util.Resource
import com.bimo.kuetradisionalapp.util.ViewModelProviderFactory
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import java.io.File

class ResultActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_FILE_PATH = "extra_file_path"
        const val SENSOR_ORIENTATION = "extra_sensor"
    }

    private lateinit var binding: ActivityResultBinding
    private lateinit var viewModel: ResultViewModel
    private lateinit var bitmap: Bitmap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewModel = ViewModelProvider(this, ViewModelProviderFactory(api)).get(ResultViewModel::class.java)

        val extras = intent.extras
        extras?.let {
            val filePath = extras.getString(EXTRA_FILE_PATH)
            val sensorOrientation = extras.getInt(SENSOR_ORIENTATION)
            if (filePath != null) {
                val file = File(filePath)
                if (file.exists()) {
                    Log.d("coba", "onCreate: $file")
                    bitmap = BitmapFactory.decodeFile(file.absolutePath)
                    val classifier = Classifier.create(this, Classifier.Device.CPU, -1)
                    viewModel.detect(classifier, bitmap, sensorOrientation)
                }
            }
        }

        viewModel.data.observe(this, { response ->
            when (response) {
                is Resource.Success -> {
                    populateItem(response.data)
                }
                is Resource.Error -> {
                    handleErrorState(response.message ?: "error")
                }
                is Resource.Loading -> {
                    handleLoadingState()
                }
            }
        })
    }

    private fun handleLoadingState() {
        binding.cvProgress.visibility = View.VISIBLE
    }

    private fun handleErrorState(msg: String) {
        binding.error.tvErrorMessage.text = msg
        binding.error.cvError.visibility = View.VISIBLE
    }

    private fun populateItem(data: KueTradisionalData?) {
        binding.cvProgress.visibility = View.INVISIBLE
        binding.error.cvError.visibility = View.INVISIBLE
        with(binding) {
            tvNama.text = data?.name
            tvDeskripsi.text = data?.description
            Glide.with(this@ResultActivity)
                .load(bitmap)
                .into(imgInput)
            btnDetail.setOnClickListener {
                intent = Intent(this@ResultActivity, DetailActivity::class.java)
                intent.putExtra(EXTRA_KUE_NAME, data?.name)
                startActivity(intent)
            }
        }
    }
}
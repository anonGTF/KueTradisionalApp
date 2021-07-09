package com.bimo.kuetradisionalapp.result

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.bimo.kuetradisionalapp.data.remote.RetrofitInstance.Companion.api
import com.bimo.kuetradisionalapp.databinding.ActivityResultBinding
import com.bimo.kuetradisionalapp.detail.DetailActivity
import com.bimo.kuetradisionalapp.model.KueTradisionalData
import com.bimo.kuetradisionalapp.util.Classifier
import com.bimo.kuetradisionalapp.util.NO_INTERNET_ERROR
import com.bimo.kuetradisionalapp.util.Resource
import com.bimo.kuetradisionalapp.util.ViewModelProviderFactory
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
                Log.d("coba", "onCreate: $file")
                bitmap = BitmapFactory.decodeStream(this.openFileInput("myImage"))
                val classifier = Classifier.create(this, Classifier.Device.CPU, -1)
                viewModel.detect(classifier, bitmap, sensorOrientation)
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

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.title = "Hasil Deteksi"
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun handleLoadingState() {
        binding.cvProgress.visibility = View.VISIBLE
    }

    private fun handleErrorState(msg: String) {
        Log.d("coba", "handleErrorState: $msg")
        binding.cvProgress.visibility = View.INVISIBLE
        when (msg) {
            NO_INTERNET_ERROR -> {
                binding.noInternet.cvNoInternet.visibility = View.VISIBLE
                binding.result.itemResult.visibility = View.INVISIBLE
            }
            "not found" -> {
                binding.notFound.cvNotFound.visibility = View.VISIBLE
                binding.result.itemResult.visibility = View.INVISIBLE
            }
            else -> {
                binding.error.tvErrorMessage.text = msg
                binding.error.cvError.visibility = View.VISIBLE
            }
        }
    }

    private fun populateItem(data: KueTradisionalData?) {
        binding.cvProgress.visibility = View.INVISIBLE
        binding.error.cvError.visibility = View.INVISIBLE
        with(binding) {
            result.tvNama.text = data?.name
            result.tvDeskripsi.text = data?.description
            result.imgInput.setImageBitmap(bitmap)
            result.btnDetail.setOnClickListener {
                val intent = Intent(this@ResultActivity, DetailActivity::class.java)
                intent.putExtra(DetailActivity.EXTRA_KUE_NAME, data?.name)
                startActivity(intent)
            }
        }
    }
}
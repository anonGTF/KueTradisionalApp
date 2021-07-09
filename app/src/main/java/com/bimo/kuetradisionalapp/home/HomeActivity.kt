package com.bimo.kuetradisionalapp.home

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.bimo.kuetradisionalapp.databinding.ActivityHomeBinding
import com.bimo.kuetradisionalapp.result.ResultActivity
import java.io.ByteArrayOutputStream

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnCTA.setOnClickListener {
            openImageChooser()
        }

        supportActionBar?.title = "Deteksi Kue Tradisional"
    }


    private fun openImageChooser() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, 100)
    }

    fun createImageFromBitmap(bitmap: Bitmap): String? {
        var fileName: String? = "myImage" //no .png or .jpg needed
        try {
            val bytes = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
            val fo = openFileOutput(fileName, Context.MODE_PRIVATE)
            fo.write(bytes.toByteArray())
            // remember close file output
            fo.close()
        } catch (e: Exception) {
            e.printStackTrace()
            fileName = null
        }
        return fileName
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            val orientasi = this.resources.configuration.orientation
            val screenOrientation: Int = when (orientasi) {
                Configuration.ORIENTATION_UNDEFINED -> {
                    180
                }
                Configuration.ORIENTATION_PORTRAIT -> {
                    90
                }
                else -> 0
            }
            Log.d("coba", "onActivityResult: $orientasi")
            Log.d("coba", "onActivityResult: $screenOrientation")
            val returnValue = data?.getExtras()?.get("data") as Bitmap
            Log.d("coba", "onActivityResult: ${returnValue}")
            intent = Intent(this@HomeActivity, ResultActivity::class.java)
            intent.putExtra(ResultActivity.EXTRA_FILE_PATH, createImageFromBitmap(returnValue))
            intent.putExtra(ResultActivity.SENSOR_ORIENTATION, screenOrientation)
            startActivity(intent)
        }
    }
}
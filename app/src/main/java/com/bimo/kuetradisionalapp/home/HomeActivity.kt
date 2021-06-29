package com.bimo.kuetradisionalapp.home

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Surface
import android.widget.Toast
import com.bimo.kuetradisionalapp.databinding.ActivityHomeBinding
import com.bimo.kuetradisionalapp.result.ResultActivity
import com.fxn.pix.Options
import com.fxn.pix.Pix
import com.fxn.utility.PermUtil

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnCTA.setOnClickListener {
            openImageChooser()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PermUtil.REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Pix.start(this@HomeActivity,
                        Options.init().setRequestCode(100).setMode(Options.Mode.Picture))
                } else {
                    Toast.makeText(this@HomeActivity,
                        "Approve permissions to open Pix ImagePicker", Toast.LENGTH_LONG).show()
                }
                return
            }
        }
    }


    private fun openImageChooser() {
        Pix.start(this@HomeActivity,
            Options.init().setRequestCode(100).setMode(Options.Mode.Picture))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            val screenOrientation: Int = when (this.baseContext.display!!.rotation) {
                Surface.ROTATION_270 -> {
                    270
                }
                Surface.ROTATION_180 -> {
                    180
                }
                Surface.ROTATION_90 -> {
                    90
                }
                else -> 0
            }
            val returnValue: ArrayList<String> = data?.getStringArrayListExtra(Pix.IMAGE_RESULTS) as ArrayList<String>
            Log.d("coba", "onActivityResult: ${returnValue[0]}")
            intent = Intent(this@HomeActivity, ResultActivity::class.java)
            intent.putExtra(ResultActivity.EXTRA_FILE_PATH, returnValue[0])
            intent.putExtra(ResultActivity.SENSOR_ORIENTATION, screenOrientation)
            startActivity(intent)
        }
    }
}
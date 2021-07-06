package com.bimo.kuetradisionalapp.detail

import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.*
import com.bimo.kuetradisionalapp.data.remote.RetrofitInstance.Companion.api
import com.bimo.kuetradisionalapp.databinding.ActivityDetailBinding
import com.bimo.kuetradisionalapp.model.RecipeData
import com.bimo.kuetradisionalapp.util.OrderedListSpan
import com.bimo.kuetradisionalapp.util.Resource
import com.bimo.kuetradisionalapp.util.ViewModelProviderFactory
import com.bimo.kuetradisionalapp.util.YOUTUBE_API_KEY
import com.google.android.youtube.player.YouTubeBaseActivity
import com.google.android.youtube.player.YouTubeInitializationResult
import com.google.android.youtube.player.YouTubePlayer
import java.util.*

class DetailActivity : YouTubeBaseActivity(), LifecycleOwner {

    companion object {
        const val EXTRA_KUE_NAME = "kue_name"
        const val WIDTH = 70
    }

    private lateinit var binding: ActivityDetailBinding
    private lateinit var viewModel: DetailViewModel
    private lateinit var title: String
    private lateinit var lifecycleRegistry: LifecycleRegistry

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleRegistry = LifecycleRegistry(this)
        lifecycleRegistry.markState(Lifecycle.State.CREATED)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewModel = ViewModelProvider(ViewModelStore(), ViewModelProviderFactory(api)).get(DetailViewModel::class.java)

        val extras = intent.extras
        extras?.let {
            title = extras.getString(EXTRA_KUE_NAME).toString()
            Log.d("coba", "onCreate: $title")
            viewModel.getRecipe(title.replace(" ", "_").toLowerCase(Locale.ROOT))
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

    public override fun onStart() {
        super.onStart()
        lifecycleRegistry.markState(Lifecycle.State.STARTED)
    }

    override fun getLifecycle(): Lifecycle {
        return lifecycleRegistry
    }

    private fun populateItem(data: RecipeData?) {
        val bahanBuilder = SpannableStringBuilder()
        data?.ingredients?.forEachIndexed { index, item ->
            bahanBuilder.append(
                "$item\n\n",
                OrderedListSpan(WIDTH, "${index + 1}."),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        val prosedurBuilder = SpannableStringBuilder()
        data?.steps?.forEachIndexed { index, item ->
            prosedurBuilder.append(
                "$item\n\n",
                OrderedListSpan(WIDTH, "${index + 1}."),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        with(binding) {
            error.cvError.visibility = View.INVISIBLE
            progressBar.visibility = View.INVISIBLE

            playVideo(data?.youtube_id)
            tvNamaKueTradisional.text = title
            tvDetailAlatBahan.text = bahanBuilder
            tvDetailProsedur.text = prosedurBuilder
        }
    }

    private fun playVideo(youtubeId: String?) {
        try {
            binding.ytPlayer.initialize(YOUTUBE_API_KEY,
                object : YouTubePlayer.OnInitializedListener {
                    override fun onInitializationSuccess(
                        provider: YouTubePlayer.Provider,
                        youTubePlayer: YouTubePlayer, b: Boolean
                    ) {
                        youTubePlayer.loadVideo(youtubeId)
                    }

                    override fun onInitializationFailure(
                        provider: YouTubePlayer.Provider,
                        youTubeInitializationResult: YouTubeInitializationResult
                    ) {
                        Log.e("youtube_failure", youTubeInitializationResult.toString())
                        handleErrorState(youTubeInitializationResult.toString())
                    }
                })
        } catch (e: Exception) {
            Log.e("youtube", e.message!!)
            handleErrorState(e.localizedMessage ?: "error")
        }
    }

    private fun handleErrorState(msg: String) {
        binding.error.tvErrorMessage.text = msg
        binding.error.cvError.visibility = View.VISIBLE
    }

    private fun handleLoadingState() {
        binding.progressBar.visibility = View.VISIBLE
    }
}
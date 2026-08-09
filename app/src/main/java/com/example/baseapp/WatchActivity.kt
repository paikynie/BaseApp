package com.example.baseapp

import android.content.pm.ActivityInfo
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.example.baseapp.repository.AnimeRepository
import kotlinx.coroutines.launch

class WatchActivity : AppCompatActivity() {

    private lateinit var playerView: PlayerView
    private lateinit var tvEpsTitleWatch: TextView
    private lateinit var serverContainer: LinearLayout
    private lateinit var progressBarWatch: ProgressBar
    private lateinit var btnPrev: Button
    private lateinit var btnNext: Button
    private lateinit var btnFullscreen: Button
    private var exoPlayer: ExoPlayer? = null
    private val repository = AnimeRepository()
    
    private var slugList: ArrayList<String> = arrayListOf()
    private var currentIndex: Int = 0
    private var isFullscreen = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_watch)
        
        supportActionBar?.hide()

        playerView = findViewById(R.id.playerView)
        tvEpsTitleWatch = findViewById(R.id.tvEpsTitleWatch)
        serverContainer = findViewById(R.id.serverContainer)
        progressBarWatch = findViewById(R.id.progressBarWatch)
        btnPrev = findViewById(R.id.btnPrev)
        btnNext = findViewById(R.id.btnNext)
        btnFullscreen = findViewById(R.id.btnFullscreen)

        slugList = intent.getStringArrayListExtra("slugList") ?: arrayListOf()
        currentIndex = intent.getIntExtra("currentIndex", 0)

        setupNavigationButtons()
        setupFullscreenButton()
        
        loadCurrentEpisode()
    }

    private fun setupFullscreenButton() {
        btnFullscreen.setOnClickListener {
            if (isFullscreen) {
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                btnFullscreen.text = "Fullscreen"
                playerView.layoutParams.height = (250 * resources.displayMetrics.density).toInt()
            } else {
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                btnFullscreen.text = "Exit Fullscreen"
                playerView.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
            }
            isFullscreen = !isFullscreen
        }
    }

    private fun setupNavigationButtons() {
        btnPrev.isEnabled = currentIndex > 0
        btnNext.isEnabled = currentIndex < slugList.size - 1

        btnPrev.setOnClickListener {
            if (currentIndex > 0) {
                currentIndex--
                exoPlayer?.stop()
                exoPlayer?.clearMediaItems()
                loadCurrentEpisode()
            }
        }

        btnNext.setOnClickListener {
            if (currentIndex < slugList.size - 1) {
                currentIndex++
                exoPlayer?.stop()
                exoPlayer?.clearMediaItems()
                loadCurrentEpisode()
            }
        }
    }

    private fun loadCurrentEpisode() {
        if (slugList.isEmpty()) return
        
        setupNavigationButtons()
        val slug = slugList[currentIndex]
        
        progressBarWatch.visibility = View.VISIBLE
        tvEpsTitleWatch.text = "Loading..."
        serverContainer.removeAllViews()

        lifecycleScope.launch {
            try {
                val response = repository.getStreamingVideo(slug)
                progressBarWatch.visibility = View.GONE
                
                if (response.isSuccessful) {
                    val result = response.body()?.asJsonObject?.getAsJsonObject("result")
                    tvEpsTitleWatch.text = result?.get("title")?.asString ?: "Unknown Episode"
                    
                    val streams = result?.getAsJsonArray("streams")
                    if (streams != null && streams.size() > 0) {
                        val firstStream = streams.get(0).asJsonObject
                        // Load without seeking (start from 0)
                        playVideo(firstStream.get("url").asString, 0L)
                        
                        for (i in 0 until streams.size()) {
                            val streamObj = streams.get(i).asJsonObject
                            val quality = streamObj.get("quality")?.asString ?: "Auto"
                            val url = streamObj.get("url").asString
                            
                            val btn = Button(this@WatchActivity)
                            btn.text = quality
                            
                            // Highlight the first button initially
                            if (i == 0) {
                                btn.setBackgroundColor(Color.parseColor("#4CAF50"))
                            } else {
                                btn.setBackgroundColor(Color.parseColor("#757575"))
                            }
                            
                            btn.setOnClickListener {
                                // Update button colors
                                for (j in 0 until serverContainer.childCount) {
                                    serverContainer.getChildAt(j).setBackgroundColor(Color.parseColor("#757575"))
                                }
                                btn.setBackgroundColor(Color.parseColor("#4CAF50"))
                                
                                // Save current position and switch quality
                                val currentPos = exoPlayer?.currentPosition ?: 0L
                                playVideo(url, currentPos)
                            }
                            serverContainer.addView(btn)
                        }
                    } else {
                        Toast.makeText(this@WatchActivity, "Stream tidak tersedia", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                progressBarWatch.visibility = View.GONE
                Toast.makeText(this@WatchActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun playVideo(url: String, seekPosition: Long) {
        val playWhenReady = exoPlayer?.playWhenReady ?: true

        if (exoPlayer == null) {
            exoPlayer = ExoPlayer.Builder(this).build()
            playerView.player = exoPlayer
        }
        
        val mediaItem = MediaItem.fromUri(url)
        exoPlayer?.setMediaItem(mediaItem)
        exoPlayer?.prepare()
        
        if (seekPosition > 0L) {
            exoPlayer?.seekTo(seekPosition)
        }
        
        exoPlayer?.playWhenReady = playWhenReady
    }

    override fun onDestroy() {
        super.onDestroy()
        exoPlayer?.release()
        exoPlayer = null
    }
}
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
import androidx.media3.common.Player
import com.example.baseapp.repository.AnimeRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class WatchActivity : AppCompatActivity() {

    private lateinit var playerView: PlayerView
    private lateinit var tvEpsTitleWatch: TextView
    private lateinit var serverContainer: LinearLayout
    private lateinit var progressBarWatch: ProgressBar
    private lateinit var btnPrev: Button
    private lateinit var btnNext: Button
    private lateinit var btnFullscreen: Button
    private lateinit var autoNextOverlay: LinearLayout
    private lateinit var tvAutoNextCountdown: TextView
    private lateinit var btnCancelAutoNext: Button
    private var autoNextJob: Job? = null
    private var exoPlayer: ExoPlayer? = null
    private val repository = AnimeRepository()
    private lateinit var prefs: android.content.SharedPreferences
    private var progressJob: Job? = null
    
    private var slugList: ArrayList<String> = arrayListOf()
    private var currentIndex: Int = 0
    private var isFullscreen = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_watch)
        
        prefs = getSharedPreferences("VideoProgress", android.content.Context.MODE_PRIVATE)
        
        supportActionBar?.hide()

        playerView = findViewById(R.id.playerView)
        tvEpsTitleWatch = findViewById(R.id.tvEpsTitleWatch)
        serverContainer = findViewById(R.id.serverContainer)
        progressBarWatch = findViewById(R.id.progressBarWatch)
        btnPrev = findViewById(R.id.btnPrev)
        btnNext = findViewById(R.id.btnNext)
        btnFullscreen = findViewById(R.id.btnFullscreen)
        autoNextOverlay = findViewById(R.id.autoNextOverlay)
        tvAutoNextCountdown = findViewById(R.id.tvAutoNextCountdown)
        btnCancelAutoNext = findViewById(R.id.btnCancelAutoNext)

        btnCancelAutoNext.setOnClickListener {
            cancelAutoNext()
        }

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
        // Karena API me-return episode dari yang TERBARU ke TERLAMA (Descending)
        // Index 0 = Episode Terbaru (misal Ep 6)
        // Index 1 = Episode Lama (misal Ep 5)
        
        // PREV (Episode lebih lama) = index BERTAMBAH
        btnPrev.isEnabled = currentIndex < slugList.size - 1
        
        // NEXT (Episode lebih baru) = index BERKURANG
        btnNext.isEnabled = currentIndex > 0

        btnPrev.setOnClickListener {
            if (currentIndex < slugList.size - 1) {
                saveProgress()
                currentIndex++
                exoPlayer?.stop()
                exoPlayer?.clearMediaItems()
                loadCurrentEpisode()
            }
        }

        btnNext.setOnClickListener {
            if (currentIndex > 0) {
                saveProgress()
                currentIndex--
                exoPlayer?.stop()
                exoPlayer?.clearMediaItems()
                loadCurrentEpisode()
            }
        }
    }

    private fun loadCurrentEpisode() {
        if (slugList.isEmpty()) return
        
        cancelAutoNext()
        setupNavigationButtons()
        val slug = slugList[currentIndex]
        val savedPos = prefs.getLong(slug, 0L)
        
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
                        // Load with saved position
                        playVideo(firstStream.get("url").asString, savedPos)
                        
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
            
            exoPlayer?.addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (playbackState == Player.STATE_ENDED) {
                        handleVideoEnd()
                    }
                }
            })
        }
        
        val mediaItem = MediaItem.fromUri(url)
        exoPlayer?.setMediaItem(mediaItem)
        exoPlayer?.prepare()
        
        if (seekPosition > 0L) {
            exoPlayer?.seekTo(seekPosition)
        }
        
        exoPlayer?.playWhenReady = playWhenReady
        startProgressSaver()
    }
    
    private fun handleVideoEnd() {
        if (currentIndex <= 0) return
        
        autoNextOverlay.visibility = View.VISIBLE
        
        autoNextJob = lifecycleScope.launch {
            for (i in 5 downTo 1) {
                tvAutoNextCountdown.text = "Episode selanjutnya dalam $i detik..."
                delay(1000)
            }
            autoNextOverlay.visibility = View.GONE
            currentIndex--
            exoPlayer?.stop()
            exoPlayer?.clearMediaItems()
            loadCurrentEpisode()
        }
    }

    private fun cancelAutoNext() {
        autoNextJob?.cancel()
        autoNextOverlay.visibility = View.GONE
    }
    
    private fun startProgressSaver() {
        progressJob?.cancel()
        progressJob = lifecycleScope.launch {
            while (true) {
                delay(5000)
                saveProgress()
            }
        }
    }

    private fun saveProgress() {
        val player = exoPlayer ?: return
        if (slugList.isEmpty()) return
        val currentSlug = slugList[currentIndex]
        
        val currentPosition = player.currentPosition
        val duration = player.duration
        
        if (duration > 0) {
            val percentage = currentPosition.toFloat() / duration.toFloat()
            if (percentage >= 0.95f) {
                prefs.edit().remove(currentSlug).remove(currentSlug + "_duration").apply()
            } else {
                prefs.edit()
                    .putLong(currentSlug, currentPosition)
                    .putLong(currentSlug + "_duration", duration)
                    .apply()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        saveProgress()
        exoPlayer?.pause()
    }

    override fun onDestroy() {
        cancelAutoNext()
        progressJob?.cancel()
        super.onDestroy()
        exoPlayer?.release()
        exoPlayer = null
    }
}
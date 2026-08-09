package com.example.baseapp

import android.os.Bundle
import android.view.View
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
    private var exoPlayer: ExoPlayer? = null
    private val repository = AnimeRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_watch)
        
        supportActionBar?.hide()

        playerView = findViewById(R.id.playerView)
        tvEpsTitleWatch = findViewById(R.id.tvEpsTitleWatch)
        serverContainer = findViewById(R.id.serverContainer)
        progressBarWatch = findViewById(R.id.progressBarWatch)

        val slug = intent.getStringExtra("slug") ?: ""
        if (slug.isNotEmpty()) {
            fetchStream(slug)
        } else {
            Toast.makeText(this, "Episode tidak valid", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun fetchStream(slug: String) {
        lifecycleScope.launch {
            try {
                val response = repository.getStreamingVideo(slug)
                progressBarWatch.visibility = View.GONE
                
                if (response.isSuccessful) {
                    val result = response.body()?.asJsonObject?.getAsJsonObject("result")
                    tvEpsTitleWatch.text = result?.get("title")?.asString ?: "Unknown Episode"
                    
                    val streams = result?.getAsJsonArray("streams")
                    if (streams != null && streams.size() > 0) {
                        // Play stream pertama
                        val firstStream = streams.get(0).asJsonObject
                        playVideo(firstStream.get("url").asString)
                        
                        // Buat tombol server
                        for (i in 0 until streams.size()) {
                            val streamObj = streams.get(i).asJsonObject
                            val quality = streamObj.get("quality")?.asString ?: "Auto"
                            val url = streamObj.get("url").asString
                            
                            val btn = Button(this@WatchActivity)
                            btn.text = quality
                            btn.setOnClickListener {
                                playVideo(url)
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

    private fun playVideo(url: String) {
        if (exoPlayer == null) {
            exoPlayer = ExoPlayer.Builder(this).build()
            playerView.player = exoPlayer
        }
        val mediaItem = MediaItem.fromUri(url)
        exoPlayer?.setMediaItem(mediaItem)
        exoPlayer?.prepare()
        exoPlayer?.play()
    }

    override fun onDestroy() {
        super.onDestroy()
        exoPlayer?.release()
    }
}
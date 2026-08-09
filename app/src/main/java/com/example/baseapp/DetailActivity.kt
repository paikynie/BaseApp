package com.example.baseapp

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.baseapp.adapter.EpisodeAdapter
import com.example.baseapp.repository.AnimeRepository
import kotlinx.coroutines.launch

class DetailActivity : AppCompatActivity() {

    private lateinit var imgDetailPoster: ImageView
    private lateinit var tvDetailTitle: TextView
    private lateinit var tvSynopsis: TextView
    private lateinit var rvEpisodes: RecyclerView
    private lateinit var progressBarDetail: ProgressBar
    private val repository = AnimeRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Detail Anime"

        imgDetailPoster = findViewById(R.id.imgDetailPoster)
        tvDetailTitle = findViewById(R.id.tvDetailTitle)
        tvSynopsis = findViewById(R.id.tvSynopsis)
        rvEpisodes = findViewById(R.id.rvEpisodes)
        progressBarDetail = findViewById(R.id.progressBarDetail)
        
        rvEpisodes.layoutManager = LinearLayoutManager(this)

        val slug = intent.getStringExtra("slug") ?: ""
        if (slug.isNotEmpty()) {
            fetchDetail(slug)
        } else {
            Toast.makeText(this, "Anime tidak ditemukan", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun fetchDetail(slug: String) {
        lifecycleScope.launch {
            try {
                val response = repository.getAnimeDetail(slug)
                progressBarDetail.visibility = View.GONE
                
                if (response.isSuccessful) {
                    val result = response.body()?.asJsonObject?.getAsJsonObject("result")
                    
                    val title = result?.get("title")?.asString ?: "No Title"
                    val synopsis = result?.get("synopsis")?.asString ?: "Tidak ada sinopsis."
                    var image = result?.get("image")?.asString ?: ""
                    
                    tvDetailTitle.text = title
                    tvSynopsis.text = synopsis
                    
                    if (image.startsWith("/")) {
                        image = "https://x6.sokuja.uk$image"
                    }
                    
                    if (image.isNotEmpty()) {
                        Glide.with(this@DetailActivity).load(image).centerCrop().into(imgDetailPoster)
                    }
                    
                    val episodes = result?.getAsJsonArray("episodes")
                    if (episodes != null) {
                        rvEpisodes.adapter = EpisodeAdapter(episodes)
                    }
                } else {
                    Toast.makeText(this@DetailActivity, "Gagal memuat detail", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                progressBarDetail.visibility = View.GONE
                Toast.makeText(this@DetailActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
package com.example.baseapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.baseapp.R
import com.example.baseapp.model.Anime

class AnimeAdapter(private val animeList: List<Anime>) : RecyclerView.Adapter<AnimeAdapter.AnimeViewHolder>() {

    class AnimeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgPoster: ImageView = view.findViewById(R.id.imgPoster)
        val tvTitle: TextView = view.findViewById(R.id.tvTitle)
        val tvEpisode: TextView = view.findViewById(R.id.tvEpisode)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnimeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_anime, parent, false)
        return AnimeViewHolder(view)
    }

    override fun onBindViewHolder(holder: AnimeViewHolder, position: Int) {
        val anime = animeList[position]
        holder.tvTitle.text = anime.title ?: "No Title"
        holder.tvEpisode.text = "Episode " + (anime.currentEpisode ?: "N/A")
        
        var url = anime.imageUrl ?: ""
        if (url.startsWith("/")) {
            // Gambar ternyata disimpan di server sokuja, bukan di api.pailynie.eu.cc
            url = "https://x6.sokuja.uk$url"
        }
        
        if (url.isNotEmpty()) {
            Glide.with(holder.itemView.context)
                .load(url)
                .centerCrop()
                .into(holder.imgPoster)
        }

        // Membuat Card bisa dipencet (sementara memunculkan Toast)
        holder.itemView.setOnClickListener {
            android.widget.Toast.makeText(
                holder.itemView.context, 
                "Membuka: ${anime.title}", 
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun getItemCount() = animeList.size
}
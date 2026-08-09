package com.example.baseapp.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.baseapp.R
import com.example.baseapp.WatchActivity

class EpisodeAdapter(private val episodes: com.google.gson.JsonArray) : RecyclerView.Adapter<EpisodeAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvEpsTitle: TextView = view.findViewById(R.id.tvEpsTitle)
        val tvEpsDate: TextView = view.findViewById(R.id.tvEpsDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_episode, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val eps = episodes.get(position).asJsonObject
        val title = eps.get("title")?.asString ?: "Unknown"
        val slug = eps.get("slug")?.asString ?: ""
        val date = eps.get("date")?.asString ?: ""

        holder.tvEpsTitle.text = title
        holder.tvEpsDate.text = date
        
        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, WatchActivity::class.java)
            intent.putExtra("slug", slug)
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount() = episodes.size()
}
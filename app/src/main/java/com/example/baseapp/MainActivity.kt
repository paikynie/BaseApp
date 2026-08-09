package com.example.baseapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.baseapp.adapter.AnimeAdapter
import com.example.baseapp.viewmodel.AnimeViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: AnimeViewModel
    private lateinit var adapter: AnimeAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvError: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.recyclerView)
        progressBar = findViewById(R.id.progressBar)
        tvError = findViewById(R.id.tvError)

        recyclerView.layoutManager = LinearLayoutManager(this)

        viewModel = ViewModelProvider(this)[AnimeViewModel::class.java]

        viewModel.animeList.observe(this) { list ->
            progressBar.visibility = View.GONE
            if (list.isNullOrEmpty()) {
                tvError.visibility = View.VISIBLE
                tvError.text = "Data kosong / Struktur API berbeda"
            } else {
                tvError.visibility = View.GONE
                adapter = AnimeAdapter(list)
                recyclerView.adapter = adapter
            }
        }

        viewModel.error.observe(this) { errorMsg ->
            progressBar.visibility = View.GONE
            tvError.visibility = View.VISIBLE
            tvError.text = errorMsg
        }

        // Fetch data
        progressBar.visibility = View.VISIBLE
        viewModel.fetchLatestAnime()
    }
}

package com.example.baseapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.baseapp.adapter.AnimeAdapter
import com.example.baseapp.viewmodel.AnimeViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: AnimeViewModel
    private lateinit var adapter: AnimeAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvError: TextView
    private lateinit var etSearch: EditText
    private lateinit var btnClearSearch: ImageButton
    private var searchJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.recyclerView)
        progressBar = findViewById(R.id.progressBar)
        tvError = findViewById(R.id.tvError)

        recyclerView.layoutManager = LinearLayoutManager(this)

        viewModel = ViewModelProvider(this)[AnimeViewModel::class.java]
        etSearch = findViewById(R.id.etSearch)
        btnClearSearch = findViewById(R.id.btnClearSearch)

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().trim()
                if (query.isNotEmpty()) {
                    btnClearSearch.visibility = View.VISIBLE
                    searchJob?.cancel()
                    searchJob = lifecycleScope.launch {
                        delay(500)
                        progressBar.visibility = View.VISIBLE
                        recyclerView.visibility = View.GONE
                        viewModel.searchAnime(query)
                    }
                } else {
                    btnClearSearch.visibility = View.GONE
                    searchJob?.cancel()
                    progressBar.visibility = View.VISIBLE
                    recyclerView.visibility = View.GONE
                    viewModel.fetchOngoingAnime()
                }
            }
        })

        btnClearSearch.setOnClickListener {
            etSearch.text.clear()
        }

        viewModel.animeList.observe(this) { list ->
            progressBar.visibility = View.GONE
            if (list.isNullOrEmpty()) {
                tvError.visibility = View.VISIBLE
                tvError.text = "Anime tidak ditemukan"
                recyclerView.visibility = View.GONE
            } else {
                tvError.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
                adapter = AnimeAdapter(list)
                recyclerView.adapter = adapter
            }
        }

        viewModel.error.observe(this) { errorMsg ->
            progressBar.visibility = View.GONE
            tvError.visibility = View.VISIBLE
            tvError.text = errorMsg
        }

        val tvHeader = findViewById<TextView>(R.id.tvHeader)
        tvHeader.text = "BaseApp"

        // Fetch data
        progressBar.visibility = View.VISIBLE
        viewModel.fetchOngoingAnime()
    }
}

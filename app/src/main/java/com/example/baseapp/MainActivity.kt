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
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import org.json.JSONObject
import java.io.File
import java.net.URL
import kotlin.concurrent.thread
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
    
    private var downloadId: Long = -1L
    private val downloadReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            if (id == downloadId) {
                installApk()
                try {
                    unregisterReceiver(this)
                } catch(e: Exception){}
            }
        }
    }

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

        // Fetch data
        progressBar.visibility = View.VISIBLE
        viewModel.fetchOngoingAnime()
        
        checkForUpdates()
    }

    private fun checkForUpdates() {
        thread {
            try {
                val url = URL("https://api.github.com/repos/paikynie/BaseApp/releases/latest")
                val connection = url.openConnection()
                connection.setRequestProperty("Accept", "application/vnd.github.v3+json")
                val response = connection.getInputStream().bufferedReader().readText()
                val jsonObject = JSONObject(response)
                
                val latestVersionTag = jsonObject.getString("tag_name")
                val pInfo = packageManager.getPackageInfo(packageName, 0)
                val currentVersion = "v" + pInfo.versionName
                
                if (latestVersionTag != currentVersion) {
                    val assets = jsonObject.getJSONArray("assets")
                    var downloadUrl = ""
                    for (i in 0 until assets.length()) {
                        val asset = assets.getJSONObject(i)
                        if (asset.getString("name").endsWith(".apk")) {
                            downloadUrl = asset.getString("browser_download_url")
                            break
                        }
                    }
                    
                    if (downloadUrl.isNotEmpty()) {
                        runOnUiThread {
                            showUpdateDialog(latestVersionTag, downloadUrl)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun showUpdateDialog(latestVersion: String, downloadUrl: String) {
        if (isFinishing || isDestroyed) return
        AlertDialog.Builder(this)
            .setTitle("Update Tersedia")
            .setMessage("Update tersedia ($latestVersion). Update sekarang?")
            .setPositiveButton("Update") { _, _ ->
                startDownload(downloadUrl)
            }
            .setNegativeButton("Nanti", null)
            .show()
    }

    private fun startDownload(url: String) {
        val request = DownloadManager.Request(Uri.parse(url))
        request.setTitle("Painime Update")
        request.setDescription("Downloading latest version...")
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        request.setDestinationInExternalFilesDir(this, Environment.DIRECTORY_DOWNLOADS, "painime_update.apk")
        
        val manager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        downloadId = manager.enqueue(request)
        
        Toast.makeText(this, "Mendownload update...", Toast.LENGTH_SHORT).show()
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(downloadReceiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE), Context.RECEIVER_EXPORTED)
        } else {
            registerReceiver(downloadReceiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
        }
    }

    private fun installApk() {
        val file = File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "painime_update.apk")
        if (file.exists()) {
            val uri = FileProvider.getUriForFile(this, "$packageName.fileprovider", file)
            val installIntent = Intent(Intent.ACTION_VIEW)
            installIntent.setDataAndType(uri, "application/vnd.android.package-archive")
            installIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
            startActivity(installIntent)
        }
    }
}

package com.example.baseapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.baseapp.model.Anime
import com.example.baseapp.repository.AnimeRepository
import kotlinx.coroutines.launch

class AnimeViewModel : ViewModel() {
    private val repository = AnimeRepository()

    private val _animeList = MutableLiveData<List<Anime>>()
    val animeList: LiveData<List<Anime>> get() = _animeList

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    fun fetchOngoingAnime() {
        viewModelScope.launch {
            try {
                val response = repository.getOngoingAnime()
                if (response.isSuccessful) {
                    val result = response.body()?.result
                    val list = result?.animeList ?: result?.episodes ?: emptyList()
                    _animeList.postValue(list)
                } else {
                    _error.postValue("Error: ${response.code()}")
                }
            } catch (e: Exception) {
                _error.postValue(e.message ?: "Unknown error")
            }
        }
    }
}
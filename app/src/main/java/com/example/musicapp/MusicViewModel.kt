package com.example.musicapp

import android.app.Application
import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.audiofx.LoudnessEnhancer
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class MusicViewModel(
    application: Application,
    private val songApi: SongApi,
    private val dataStoreManager: DataStoreManager
): AndroidViewModel(application) {

    var premiereSongs by mutableStateOf<List<Song>>(emptyList())
        private set

    var topArtists by mutableStateOf<List<ArtistDB>>(emptyList())
        private set

    var songsByGenre by mutableStateOf<List<Song>>(emptyList())
        private set

    var isPlaying by mutableStateOf(false)
        private set

    var currentSong by mutableStateOf<Song?>(null)
        private set

    var searchResults by mutableStateOf<List<Song>>(emptyList())
        private set

    var favoriteSongs by mutableStateOf<List<Song>>(emptyList())
        private set

    var isFavoritesLoading by mutableStateOf(false)
        private set

    var favoritesError by mutableStateOf<String?>(null)
        private set

    var userEmail by mutableStateOf("")
        private set

    var currentlyPlayingId by mutableStateOf<String?>(null)

    private var mediaPlayer: MediaPlayer? = null

    val nickname = mutableStateOf("Гость")

    var playbackProgress = mutableStateOf(0f)
        private set

    private var progressJob: Job? = null

    private val _isAuthorized = MutableStateFlow(true)
    val isAuthorized: StateFlow<Boolean> = _isAuthorized

    private var loudnessEnhancer: LoudnessEnhancer? = null


    private fun getAllSongs(): List<Song> {
        return premiereSongs + songsByGenre + searchResults + favoriteSongs
    }

    private fun startProgressUpdates() {
        progressJob?.cancel()
        progressJob = viewModelScope.launch {
            while (true) {
                mediaPlayer?.let {
                    if (it.isPlaying && it.duration > 0) {
                        playbackProgress.value = it.currentPosition.toFloat() / it.duration
                    }
                }
                delay(500)
            }
        }
    }

    private fun stopProgressUpdates() {
        progressJob?.cancel()
    }

    fun seekToPercent(percent: Float) {
        mediaPlayer?.let {
            val position = (it.duration * percent).toInt()
            it.seekTo(position)
        }
    }

    fun togglePlayPause() {
        currentSong?.let { song ->
            onPlayPauseClick(getApplication(), song)
        }
    }

    fun playNextSong() {
        val allSongs = getAllSongs().distinctBy { it.id }
        val currentIndex = allSongs.indexOfFirst { it.id == currentSong?.id }
        if (currentIndex != -1 && currentIndex < allSongs.lastIndex) {
            val nextSong = allSongs[currentIndex + 1]
            onPlayPauseClick(getApplication(), nextSong)
        }
    }

    fun playPreviousSong() {
        val allSongs = getAllSongs().distinctBy { it.id }
        val currentIndex = allSongs.indexOfFirst { it.id == currentSong?.id }
        if (currentIndex > 0) {
            val previousSong = allSongs[currentIndex - 1]
            onPlayPauseClick(getApplication(), previousSong)
        }
    }

    fun loadNickname() {
        viewModelScope.launch {
            dataStoreManager.userNickname.collect { storedNickname ->
                nickname.value = storedNickname ?: "Гость"
            }
        }
    }

    init {
        viewModelScope.launch {
            dataStoreManager.userEmail.first()?.let { email ->
                if (email.isNotBlank()) {
                    userEmail = email
                    Log.d("MusicViewModel", "userEmail = $userEmail")
                    loadFavorites(email)
                }
            }
            fetchPremiereSongs()
            fetchTopArtists()
        }
    }

    private suspend fun loadFavorites(email: String) {
        Log.d("MusicViewModel", "userEmail = $userEmail")
        try {
            favoriteSongs = ApiClient.songApi.getFavorites(email)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun isFavorite(song: Song): Boolean {
        return favoriteSongs.any { it.id == song.id }
    }

    fun toggleFavorite(song: Song) {
        viewModelScope.launch {
            if (favoriteSongs.any { it.id == song.id }) {
                songApi.removeFromFavorites(FavoriteReceiveRemote(userEmail, song.id))
                favoriteSongs = favoriteSongs.filterNot { it.id == song.id }
            } else {
                songApi.addToFavorites(FavoriteReceiveRemote(userEmail, song.id))
                favoriteSongs = favoriteSongs + song
            }
        }
    }

    fun searchSongs(query: String) {
        viewModelScope.launch {
            try {
                searchResults = ApiClient.songApi.searchSongs(query)
            } catch (e: Exception) {
                Log.e("MusicViewModel", "Error searching songs: ${e.message}")
                searchResults = emptyList()
            }
        }
    }

    private fun fetchPremiereSongs() {
        viewModelScope.launch {
            try {
                premiereSongs = ApiClient.songApi.getPremiereSongs()
            } catch (e: Exception) {
                Log.e("MusicViewModel", "Error fetching songs: ${e.message}")
            }
        }
    }

    private fun fetchTopArtists() {
        viewModelScope.launch {
            try {
                topArtists = ApiClient.songApi.getTopArtists()
            } catch (e: Exception) {
                Log.e("MusicViewModel", "Error fetching artists: ${e.message}")
            }
        }
    }

    fun fetchSongsByGenre(genre: String) {
        viewModelScope.launch {
            try {
                songsByGenre = ApiClient.songApi.getSongsByGenre(genre)
            } catch (e: Exception) {
                Log.e("MusicViewModel", "Ошибка при загрузке песен жанра $genre: ${e.message}")
            }
        }
    }

    fun onPlayPauseClick(context: Context, song: Song) {
        if (currentlyPlayingId == song.id) {
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.pause()
                isPlaying = false
                stopProgressUpdates()
            } else {
                mediaPlayer?.start()
                isPlaying = true
                startProgressUpdates()
            }
        } else {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                setDataSource(song.filePath)
                prepareAsync()
                setOnPreparedListener {
                    start()
                    loudnessEnhancer = LoudnessEnhancer(audioSessionId).apply {
                        setTargetGain(1000)
                        enabled = true
                    }
                    this@MusicViewModel.isPlaying = true
                    playbackProgress.value = 0f
                    startProgressUpdates()
                }
            }
            currentlyPlayingId = song.id
            currentSong = song
        }
    }

    fun logout(onLoggedOut: () -> Unit) {
        viewModelScope.launch {
            dataStoreManager.clear()
            nickname.value = "Гость"
            _isAuthorized.value = false
            onLoggedOut()
        }
    }

    override fun onCleared() {
        super.onCleared()
        mediaPlayer?.release()
        mediaPlayer = null
        stopProgressUpdates()
        isPlaying = false
    }
}
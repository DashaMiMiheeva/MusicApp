package com.example.musicapp

import androidx.compose.ui.graphics.Color

data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val email: String,
    val password: String,
    val nickname: String
)

data class AuthResponse(
    val token: String,
    val nickname: String
)

data class Song(
    val id: String,
    val title: String,
    val artist: String,
    val filePath: String,
    val coverUrl: String,
    val genre: String,
    val popularity: Int,
    val isPremiere: Boolean,
    val coverArtist: String
)

data class ArtistDB(
    val artist: String,
    val coverArtist: String
)

data class GenreItem(
    val name: String,
    val color: Color
)

data class FavoriteReceiveRemote(
    val userEmail: String,
    val songId: String
)
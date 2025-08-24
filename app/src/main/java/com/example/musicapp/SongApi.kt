package com.example.musicapp

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface SongApi {
    @GET("/songs/premiere")
    suspend fun getPremiereSongs(): List<Song>

    @GET("/songs/top-artists")
    suspend fun getTopArtists(): List<ArtistDB>

    @GET("songs/by-genre/{genre}")
    suspend fun getSongsByGenre(@Path("genre", encoded = true) genre: String): List<Song>

    @GET("/songs/search")
    suspend fun searchSongs(@Query("query") query: String): List<Song>

    @POST("/songs/favorites/add")
    suspend fun addToFavorites(@Body favorite: FavoriteReceiveRemote): Response<Unit>

    @POST("/songs/favorites/remove")
    suspend fun removeFromFavorites(@Body favorite: FavoriteReceiveRemote): Response<Unit>

    @GET("/songs/favorites")
    suspend fun getFavorites(@Query("email") email: String): List<Song>

}
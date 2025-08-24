package com.example.musicapp

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("register")
    fun register(@Body request: RegisterRequest): Call<AuthResponse>

    @POST("login")
    fun login(@Body request: LoginRequest): Call<AuthResponse>
}
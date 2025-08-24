package com.example.musicapp

sealed class Screen(val route: String) {
    object Welcome : Screen("welcome")
    object SignIn : Screen("sign_in")
    object SignUp : Screen("sign_up")
    object Home : Screen("home")
}
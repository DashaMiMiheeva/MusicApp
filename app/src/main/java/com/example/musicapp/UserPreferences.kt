package com.example.musicapp

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("user_prefs")

class DataStoreManager(private val context: Context) {

    companion object {
        val EMAIL_KEY = stringPreferencesKey("email")
        val NICKNAME_KEY = stringPreferencesKey("nickname")
    }

    suspend fun saveUser(email: String, nickname: String) {
        context.dataStore.edit { preferences ->
            preferences[EMAIL_KEY] = email
            preferences[NICKNAME_KEY] = nickname
        }
    }

    val userEmail: Flow<String?> = context.dataStore.data
        .map { preferences -> preferences[EMAIL_KEY] }

    val userNickname: Flow<String?> = context.dataStore.data
        .map { preferences -> preferences[NICKNAME_KEY] }

    suspend fun clear() {
        context.dataStore.edit { it.clear() }
    }
}

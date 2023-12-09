package com.dicoding.storyapp.data.di

import android.content.Context
import com.dicoding.storyapp.repo.UserRepository
import com.dicoding.storyapp.data.api.ApiConfig
import com.dicoding.storyapp.data.pref.UserPreference
import com.dicoding.storyapp.data.pref.dataStore
import com.dicoding.storyapp.database.StoryDataBase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking


object Injection {
    fun provideRepository(context: Context): UserRepository {
        val dataBase = StoryDataBase.getDatabase(context)
        val pref = UserPreference.getInstance(context.dataStore)
        val user = runBlocking { pref.getSession().first() }
        val apiService = ApiConfig.getApiService(user.token)
        return UserRepository.getInstance(dataBase, apiService, pref)
    }
}
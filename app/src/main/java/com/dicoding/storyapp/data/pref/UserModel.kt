package com.dicoding.storyapp.data.pref

data class UserModel(
    val name: String,
    val token: String,
    val userId: String,
    val isLogin: Boolean = false
)
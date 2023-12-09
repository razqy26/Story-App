package com.dicoding.storyapp.view.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.dicoding.storyapp.repo.UserRepository
import com.dicoding.storyapp.data.response.LoginResponse
import com.dicoding.storyapp.data.pref.UserModel

class LoginViewModel(private var repository: UserRepository) : ViewModel() {

    val isLoading: LiveData<Boolean>
        get() = repository.isLoading

    val loginResponse: LiveData<LoginResponse>
        get() = repository.loginResponse

    fun login(email: String, password: String) {
        repository.login(email, password)
    }

    suspend fun saveSession(user: UserModel) {
        repository.saveSession(user)
    }

}
package com.dicoding.storyapp.view.register

import androidx.lifecycle.ViewModel
import com.dicoding.storyapp.repo.UserRepository
import com.dicoding.storyapp.data.response.RegisterResponse


class RegisterViewModel(private var repository: UserRepository) : ViewModel() {
    suspend fun register(name: String, email: String, password: String): RegisterResponse {
        return repository.register(name, email, password)
    }
}

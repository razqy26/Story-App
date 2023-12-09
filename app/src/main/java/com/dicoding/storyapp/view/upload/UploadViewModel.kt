package com.dicoding.storyapp.view.upload

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.dicoding.storyapp.repo.UserRepository
import okhttp3.MultipartBody
import okhttp3.RequestBody

class UploadViewModel(private val repository: UserRepository) : ViewModel() {

    var isLoading: LiveData<Boolean> = repository.isLoading

    fun uploadStory(
        file: MultipartBody.Part,
        description: RequestBody
    ) {
        repository.uploadImage(file, description)
    }
}
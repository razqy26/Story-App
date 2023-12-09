package com.dicoding.storyapp.repo

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.liveData
import com.dicoding.storyapp.data.Result
import com.dicoding.storyapp.data.StoryPagingSource
import com.dicoding.storyapp.data.StoryRemoteMediator
import com.dicoding.storyapp.data.api.ApiService
import com.dicoding.storyapp.data.response.LoginResponse
import com.dicoding.storyapp.data.response.RegisterResponse
import com.dicoding.storyapp.data.pref.UserModel
import com.dicoding.storyapp.data.pref.UserPreference
import com.dicoding.storyapp.data.response.ErrorResponse
import com.dicoding.storyapp.data.response.ListStoryItem
import com.dicoding.storyapp.data.response.StoryResponse
import com.dicoding.storyapp.data.response.UploadResponse
import com.dicoding.storyapp.database.StoryDataBase
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.HttpException
import retrofit2.Response
import java.io.File

class UserRepository private constructor(
    private val dataBase: StoryDataBase,
    private val apiService: ApiService,
    private val userPreference: UserPreference,
) {

    private var _loginResponse = MutableLiveData<LoginResponse>()
    var loginResponse: MutableLiveData<LoginResponse> = _loginResponse

    var _isLoading = MutableLiveData<Boolean>()
    var isLoading: LiveData<Boolean> = _isLoading

    private var _list = MutableLiveData<List<ListStoryItem>>()
    var list: MutableLiveData<List<ListStoryItem>> = _list


    fun getStory() {
        _isLoading.value = true
        val client = apiService.getStories()
        client.enqueue(object : Callback<StoryResponse> {
            override fun onResponse(
                call: Call<StoryResponse>,
                response: Response<StoryResponse>
            ) {
                if (response.isSuccessful) {
                    _isLoading.value = false
                    val newStories = response.body()?.listStory
                    val currentList = _list.value.orEmpty()
                    val updatedList = mutableListOf<ListStoryItem>()

                    if (newStories != null) {
                        updatedList.addAll(newStories)
                    }

                    updatedList.addAll(currentList)

                    _list.value = updatedList
                }
            }

            override fun onFailure(call: Call<StoryResponse>, t: Throwable) {
                _isLoading.value = false
                Log.e("StoryRepository", "error: ${t.message}")
            }
        })
    }


    suspend fun saveSession(user: UserModel) {
        userPreference.saveSession(user)
    }

    fun getSession(): Flow<UserModel> {
        return userPreference.getSession()
    }

    suspend fun logout() {
        userPreference.logout()
    }
    suspend fun register(name: String, email: String, password: String): RegisterResponse {
        return apiService.register(name, email, password)
    }

    fun login(email: String, password: String) {
        _isLoading.value = true
        val client = apiService.login(email, password)
        client.enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.isSuccessful) {
                    _isLoading.value = false
                    _loginResponse.value = response.body()
                } else {
                    val errorMessage = extractErrorMessage(response)
                    Log.e("StoryRepository", errorMessage)
                    _isLoading.value = false
                    _loginResponse.value = LoginResponse(error = true, message = errorMessage)
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                val errorMessage = "Login failed: ${t.message}"
                Log.e("StoryRepository", errorMessage)
                _isLoading.value = false
                _loginResponse.value = LoginResponse(error = true, message = errorMessage)
            }
        })
    }

    private fun extractErrorMessage(response: Response<*>): String {
        return try {
            val errorObject = Gson().fromJson(response.errorBody()?.charStream(), ErrorResponse::class.java)
            errorObject.message ?: "Login failed: ${response.message()}"
        } catch (e: Exception) {
            "Login failed: ${response.message()}"
        }
    }


    fun uploadImage(file: MultipartBody.Part, description: RequestBody) {
        _isLoading.value = true
        val client = apiService.uploadImage(file, description)
        client.enqueue(object : Callback<UploadResponse> {
            override fun onResponse(
                call: Call<UploadResponse>,
                response: Response<UploadResponse>
            ) {
                if (response.isSuccessful) {
                    _isLoading.value = false
                }
            }

            override fun onFailure(call: Call<UploadResponse>, t: Throwable) {
                _isLoading.value = false
                Log.e("StoryRepository", "error: ${t.message}")
            }
        })
    }

    fun getStoriesWithLocation(): LiveData<Result<List<ListStoryItem>>> = liveData {
        emit(Result.Loading)
        try {
            val response = apiService.getStoriesWithLocation()
            emit(Result.Success(response.listStory))
        } catch (e: HttpException) {
            val jsonInString = e.response()?.errorBody()?.string()
            val errorBody = Gson().fromJson(jsonInString, ErrorResponse::class.java)
            val errorMessage = errorBody.message
            Result.Error(errorMessage.toString())
        }
    }

    @OptIn(ExperimentalPagingApi::class)
    fun getStories(): LiveData<PagingData<ListStoryItem>> {
        return Pager(
            config = PagingConfig(
                pageSize = 5
            ),
            remoteMediator = StoryRemoteMediator(dataBase, apiService),
            pagingSourceFactory = {
//                StoryPagingSource(apiService)
                dataBase.storyDao().getAllStory()
            }
        ).liveData
    }

    companion object {
        @Volatile
        private var instance: UserRepository? = null
        fun getInstance(
            dataBase: StoryDataBase,
            apiService: ApiService,
            userPreference: UserPreference
        ): UserRepository = UserRepository(dataBase, apiService, userPreference)

        fun clearInstance() {
            instance = null
        }
    }
}
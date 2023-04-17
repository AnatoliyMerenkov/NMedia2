package ru.netology.nmedia.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response
import ru.netology.nmedia.api.PostApi
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.dto.Token
import ru.netology.nmedia.error.ApiError

class AuthViewModel : ViewModel() {
    val authState = AppAuth.getInstance()
        .data
        .asLiveData(Dispatchers.Default)
    val token = MutableLiveData<Token?>(null)


    val authenticated: Boolean
        get() = authState.value != null

    fun logIn(login: String, password: String) {
        viewModelScope.launch {
            val response = withContext(Dispatchers.Default) {
                PostApi.service.login(login, password)
            }
            val body = checkResponse(response)
            token.postValue(body)
        }
    }

    fun register(login: String, password: String, name: String) {
        viewModelScope.launch {
            val response = withContext(Dispatchers.Default) {
                PostApi.service.register(login, password, name)
            }
            val body = checkResponse(response)
            token.postValue(body)
        }
    }

    private fun <T> checkResponse(response: Response<T>): T {
        if (!response.isSuccessful) throw ApiError(response.code(), response.message())
        return response.body() ?: throw RuntimeException("body is null")
    }

    fun singIn(id: Long, token: String) {
        AppAuth.getInstance().saveUser(id, token)
    }

    fun logout() {
        AppAuth.getInstance().clear()
    }
}
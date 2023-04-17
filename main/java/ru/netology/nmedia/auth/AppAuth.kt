package ru.netology.nmedia.auth

import android.content.Context
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AppAuth private constructor(context: Context) {
    private val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
    private val _data = MutableStateFlow<AuthState?>(null)
    val data: StateFlow<AuthState?> = _data.asStateFlow()

    init {
        val token = prefs.getString(TOKEN_KEY, null)
        val id = prefs.getLong(ID_KEY, 0L)

        if (token == null || id == 0L) {
            prefs.edit { clear() }
        } else {
            _data.value = AuthState(token, id)
        }
    }

    @Synchronized
    fun saveUser(id: Long, token: String) {
        _data.value = AuthState(token, id)
        prefs.edit {
            putString(TOKEN_KEY, token)
            putLong(ID_KEY, id)
        }
    }

    @Synchronized
    fun clear() {
        _data.value = null
        prefs.edit { clear() }
    }


    companion object {
        private const val TOKEN_KEY = "TOKEN_KEY"
        private const val ID_KEY = "ID_KEY"
        private var INSTANCE: AppAuth? = null

        fun getInstance() = requireNotNull(INSTANCE) {
            "You must call initApp(context: Context) first"
        }

        fun initApp(context: Context) {
            INSTANCE = AppAuth(context)
        }
    }
}
package ru.netology.nmedia.reposirory

import ru.netology.nmedia.dto.Post

interface PostRepository {
    fun getAllAsync(callback: Callback<List<Post>>)
    fun getByIdAsync(id: Long, callback: Callback<Post>)
    fun likeByIdAsync(id: Long, callback: Callback<Unit>)
    fun removeByIdAsync(id: Long, callback: Callback<Unit>)
    fun saveAsync(post: Post, callback: Callback<Unit>)

    interface Callback<T> {
        fun onSuccess(result: T)
        fun onError(e: Exception)
    }
}
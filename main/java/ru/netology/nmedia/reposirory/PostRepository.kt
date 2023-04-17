package ru.netology.nmedia.reposirory

import androidx.lifecycle.LiveData
import kotlinx.coroutines.flow.Flow
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.model.PhotoModel

interface PostRepository {
    val posts: Flow<List<Post>>
    fun getNewer(id: Long): Flow<Int>
    suspend fun showNewerPosts()
    suspend fun getAll()
    suspend fun getById(id: Long): Post?
    suspend fun likeById(id: Long)
    suspend fun removeById(id: Long)
    suspend fun save(post: Post)
    suspend fun saveWithAttachment(post: Post, photo: PhotoModel)
}
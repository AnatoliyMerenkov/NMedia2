package ru.netology.nmedia.reposirory

import android.util.Log
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Response
import ru.netology.nmedia.BuildConfig.BASE_URL
import ru.netology.nmedia.api.PostApi
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dto.Attachment
import ru.netology.nmedia.dto.Media
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.entity.*
import ru.netology.nmedia.enumeration.AttachmentType
import ru.netology.nmedia.error.*
import ru.netology.nmedia.model.PhotoModel
import java.io.IOException

class PostRepositoryImpl(private val postDao: PostDao) : PostRepository {


    override val posts: Flow<List<Post>> = postDao.getAll()
        .map(List<PostEntity>::toDto)
        .flowOn(Dispatchers.Default)

    override fun getNewer(id: Long): Flow<Int> = flow {
        while (true) {
            try {
                delay(10_000)
                val response = PostApi.service.getNewer(id)
                val body = checkResponse(response)
                    .toEntity()
                    .map { it.copy(isVisible = false) }
                postDao.insert(body)
                emit(body.size)
            } catch (e: CancellationException) {
                throw e
            } catch (e: IOException) {
                //
            }
        }
    }

    override suspend fun showNewerPosts() {
        postDao.showNewerPosts()
    }

    override suspend fun getAll() {
        try {
            val response = PostApi.service.getAll()
            val body = checkResponse(response)
            postDao.insert(body.map(PostEntity::fromDto))
        } catch (e: IOException) {
            throw NetworkError()
        } catch (e: Exception) {
            throw UnknownError()
        }
    }

    override suspend fun likeById(id: Long) {
        val post = getById(id)
        Log.d("MyLog", post.toString())
        postDao.likeById(id)
        if (post.likedByMe) {
            makeRequestDislikeById(id)
        } else {
            makeRequestLikeById(id)
        }
    }

    private suspend fun makeRequestLikeById(id: Long) {
        try {
            val response = PostApi.service.likeById(id)
            val body = checkResponse(response)
            postDao.insert(PostEntity.fromDto(body))
        } catch (e: IOException) {
            throw NetworkError()
        } catch (e: Exception) {
            throw UnknownError()
        }
    }

    private suspend fun makeRequestDislikeById(id: Long) {
        try {
            val response = PostApi.service.dislikeById(id)
            val body = checkResponse(response)
            postDao.insert(PostEntity.fromDto(body))
        } catch (e: IOException) {
            throw NetworkError()
        } catch (e: Exception) {
            throw UnknownError()
        }
    }

    override suspend fun removeById(id: Long) {
        try {
            postDao.removeById(id)
            val response = PostApi.service.removeById(id)
            checkResponse(response)
        } catch (e: IOException) {
            throw NetworkError()
        } catch (e: Exception) {
            throw UnknownError()
        }
    }

    override suspend fun getById(id: Long): Post {
        val response = PostApi.service.getById(id)
        //todo это временный вариант, надо как то запрашивать пост из локальной копии
        return checkResponse(response)
    }

    override suspend fun save(post: Post) {
        try {
            val newPostId = postDao.insert(PostEntity.fromDto(post))
            val response = PostApi.service.save(post)
            val body = checkResponse(response)
            postDao.removeById(newPostId)
            postDao.save(PostEntity.fromDto(body))
        } catch (e: IOException) {
            throw NetworkError()
        } catch (e: Exception) {
            throw UnknownError()
        }
    }

    override suspend fun saveWithAttachment(post: Post, photo: PhotoModel) {
        try {
            val media = upload(photo)
            val newPostId = postDao.insert(PostEntity.fromDto(post))
            val response = PostApi.service.save(
                post.copy(
                    attachment = Attachment(
                        media.id,
                        null,
                        AttachmentType.IMAGE
                    )
                )
            )
            val body = checkResponse(response)
            postDao.removeById(newPostId)
            postDao.save(PostEntity.fromDto(body))
        } catch (e: IOException) {
            throw NetworkError()
        } catch (e: Exception) {
            throw UnknownError()
        }
    }

    private suspend fun upload(photo: PhotoModel): Media {
        val media = MultipartBody.Part.createFormData(
            "file",
            photo.file?.name,
            requireNotNull(photo.file?.asRequestBody())
        )

        val response = PostApi.service.upload(media)
        return checkResponse(response)
    }

    private fun <T> checkResponse(response: Response<T>): T {
        if (!response.isSuccessful) throw ApiError(response.code(), response.message())
        return response.body() ?: throw RuntimeException("body is null")
    }

    companion object {
        fun getAvatarUrl(avatarURL: String) = "${BASE_URL}/avatars/$avatarURL"
        fun getImageUrl(imageURL: String) = "${BASE_URL}/media/$imageURL"
    }
}
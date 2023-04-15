package ru.netology.nmedia.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ru.netology.nmedia.util.SingleLiveEvent
import ru.netology.nmedia.activity.feed.FeedModel
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.reposirory.*

private var empty = Post(
    id = 0,
    author = "",
    content = "",
    likedByMe = false,
)

class PostViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: PostRepository = PostRepositoryImpl()
    private val _data = MutableLiveData(FeedModel())
    private val _postCreated = SingleLiveEvent<Unit>()

    val data: LiveData<FeedModel>
        get() = _data
    val postCreated: LiveData<Unit>
        get() = _postCreated
    val edited = MutableLiveData(empty)

    init {
        loadPost()
    }

    fun changeContentAndSave(content: String) {
        changeContent(content)
        save()
    }

    private fun changeContent(content: String) {
        val text = content.trim()
        edited.value?.let {
            if (it.content != text) {
                edited.value = it.copy(
                    content = text
                )
            }
        }
    }

    private fun save() {
        edited.value?.let {
            repository.saveAsync(it, object : PostRepository.Callback<Unit> {
                override fun onSuccess(result: Unit) {
                    _postCreated.postValue(Unit)
                    clearEdited()
                }

                override fun onError(e: Exception) {
                    throw Exception("Save Error")
                }
            })
        }
    }

    fun edit(post: Post) {
        edited.postValue(post)
    }

    fun clearEdited() {
        edited.postValue(empty)
    }

    fun likeById(id: Long) {
        val old = _data.value?.posts.orEmpty()

        repository.likeByIdAsync(id, object : PostRepository.Callback<Unit> {
            override fun onSuccess(result: Unit) {
                _data.postValue(
                    _data.value?.copy(posts = _data.value?.posts.orEmpty()
                        .map {
                            if (it.id != id) it else {
                                if (it.likedByMe) {
                                    it.copy(likedByMe = !it.likedByMe, likes = it.likes - 1)
                                } else {
                                    it.copy(likedByMe = !it.likedByMe, likes = it.likes + 1)
                                }
                            }
                        }
                    )
                )
            }

            override fun onError(e: Exception) {
                _data.postValue(_data.value?.copy(posts = old))
            }
        })
    }

    fun removeById(id: Long) {
        val old = _data.value?.posts.orEmpty()

        repository.removeByIdAsync(id, object : PostRepository.Callback<Unit> {
            override fun onSuccess(result: Unit) {
                _data.postValue(
                    _data.value?.copy(posts = _data.value?.posts.orEmpty()
                        .filter { it.id != id }
                    )
                )
            }

            override fun onError(e: Exception) {
                _data.postValue(_data.value?.copy(posts = old))
            }
        })
    }

    fun loadPost() {
        _data.value = FeedModel(loading = true)
        repository.getAllAsync(object : PostRepository.Callback<List<Post>> {
            override fun onSuccess(result: List<Post>) {
                _data.postValue(FeedModel(posts = result, empty = result.isEmpty()))
            }

            override fun onError(e: Exception) {
                _data.postValue(FeedModel(error = true))
            }
        })
    }
}
package ru.netology.nmedia.viewmodel

import android.app.Application
import android.net.Uri
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.model.FeedModel
import ru.netology.nmedia.model.FeedModelState
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.model.PhotoModel
import ru.netology.nmedia.reposirory.PostRepository
import ru.netology.nmedia.reposirory.PostRepositoryImpl
import ru.netology.nmedia.util.DialogManager
import ru.netology.nmedia.util.SingleLiveEvent
import java.io.File

private var empty = Post(
    id = 0,
    authorId = 0L,
    localId = 0,
    author = "",
    content = "",
    likedByMe = false,
)

class PostViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: PostRepository =
        PostRepositoryImpl(AppDb.getInstance(application).postDao())

    val data: LiveData<FeedModel> = AppAuth.getInstance().data.flatMapLatest {
        repository.posts
            .map { posts ->
                FeedModel(
                    posts = posts.map { post ->
                        post.copy(ownedByMe = post.authorId == it?.id)
                    },
                    empty = posts.isEmpty(),
                )
            }

    }.asLiveData(Dispatchers.Default)


    private val _state = MutableLiveData<FeedModelState>()
    val state: LiveData<FeedModelState>
        get() = _state

    val newerCount: LiveData<Int> = data.switchMap {
        repository.getNewer(it.posts.firstOrNull()?.id ?: 0L)
            .asLiveData(Dispatchers.Default)
    }

    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit>
        get() = _postCreated

    private val _postsShowed = SingleLiveEvent<Unit>()
    val postsShowed: LiveData<Unit>
        get() = _postsShowed

    private val _photo = MutableLiveData<PhotoModel?>(null)
    val photo: LiveData<PhotoModel?>
        get() = _photo

    val edited = MutableLiveData(empty)

    val draftContent = MutableLiveData("")

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
        edited.value?.let { post ->
            _postCreated.value = Unit
            viewModelScope.launch {
                try {
                    photo.value?.let {
                        repository.saveWithAttachment(post, it)
                        clearPhoto()
                    } ?: repository.save(post)

                    _state.value = FeedModelState.Idle
                } catch (e: Exception) {
                    _state.value = FeedModelState.Error
                }
            }
        }
        clearEdited()
    }

    fun edit(post: Post) {
        edited.value = post
    }

    fun clearEdited() {
        edited.value = empty
    }

    private fun clearPhoto() {
        savePhoto(null, null)
    }

    fun likeById(id: Long) = viewModelScope.launch {
        try {
            repository.likeById(id)
        } catch (e: Exception) {
            e.printStackTrace()
            _state.value = FeedModelState.Error
        }
    }

    fun removeById(id: Long) = viewModelScope.launch {
        try {
            repository.removeById(id)
        } catch (e: Exception) {
            _state.value = FeedModelState.Error
        }
    }

    fun loadPost() = viewModelScope.launch {
        try {
            _state.value = FeedModelState.Loading
            repository.getAll()
            _state.value = FeedModelState.Idle
        } catch (e: Exception) {
            _state.value = FeedModelState.Error
        }
    }

    fun refresh() = viewModelScope.launch {
        try {
            _state.value = FeedModelState.Refreshing
            repository.getAll()
            _state.value = FeedModelState.Idle
        } catch (e: Exception) {
            _state.value = FeedModelState.Error
        }
    }

    fun clearDraft() {
        draftContent.value = ""
    }

    fun showNewerPosts() {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.Default) {
                    repository.showNewerPosts()
                }
                _postsShowed.value = Unit
            } catch (e: Exception) {
                _state.value = FeedModelState.Error
            }
        }
    }

    fun savePhoto(uri: Uri?, toFile: File?) {
        _photo.value = PhotoModel(uri, toFile)
    }

    private fun isLogin() = AppAuth.getInstance().data.value != null

    fun checkLogin(fragment: Fragment): Boolean =
        if (isLogin()) {
            true
        } else {
            DialogManager.errorAuthDialog(fragment)
            false
        }
}
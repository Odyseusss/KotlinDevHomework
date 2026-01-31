package com.mynewproject.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.mynewproject.db.AppDb
import com.mynewproject.dto.Post
import com.mynewproject.model.FeedModel
import com.mynewproject.model.FeedModelState
import com.mynewproject.repository.PostRepository
import com.mynewproject.repository.PostRepositoryNetwork
import com.mynewproject.util.SingleLiveEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

private val empty = Post(
    id = 0,
    author = "",
    content = "",
    published = "",
    likes = 0,
    likedByMe = false,
    authorAvatar = "",
)

class PostViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: PostRepository = PostRepositoryNetwork(
        AppDb.getInstance(application).postDao()
    )

    private val _state = MutableLiveData(FeedModelState())
    val state: LiveData<FeedModelState>
        get() = _state
    private val scope = MainScope()
    val data: LiveData<FeedModel> =
        repository.data.map { list: List<Post> -> FeedModel(list, list.isEmpty()) }
            .catch { it.printStackTrace() }
            .asLiveData(
                Dispatchers.Default
            )
    val newerCount = data.switchMap {
        repository.getNewer(
            data.value?.posts?.firstOrNull()?.id ?: 0
        ).asLiveData()
    }

    val edited = MutableLiveData(empty)
    private val _postCreated = SingleLiveEvent<Unit>()
    private val _errorMessage = SingleLiveEvent<String>()
    val errorMessage: LiveData<String>
        get() = _errorMessage
    val postCreated: LiveData<Unit>
        get() = _postCreated

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _state.postValue(FeedModelState(loading = true))
            try {
                repository.getAllAsync()

                _state.value = FeedModelState()
            } catch (_: Exception) {
                _state.value = FeedModelState(error = true)
            }
        }
    }

    fun save() {
        viewModelScope.launch {
            try {
                edited.value?.let {
                    repository.saveAsync(it)
                    _postCreated.value = Unit
                    edited.value = empty
                }
            } catch (_: Exception) {
                _errorMessage.value = "Ошибка"
            }
        }
    }

    fun edit(post: Post) {
        edited.postValue(post)
    }

    fun shareById(id: Long) {
        viewModelScope.launch {
            repository.shareByIdAsync(id)
        }
    }

    fun likeById(post: Post) {
        viewModelScope.launch {
            try {
                repository.likeByIdAsync(post)
            } catch (_: Exception) {
                _errorMessage.value = "Ошибка"
            }
        }
    }

    fun removeById(id: Long) {
        viewModelScope.launch {
            try {
                repository.removeByIdAsync(id)
            } catch (_: Exception) {
                _errorMessage.value = "Ошибка"
            }
        }
    }

    fun changeContent(content: String) {
        edited.value = edited.value?.copy(content = content.trim())
    }

    fun cancelEdit() {
        edited.value = empty
    }

    fun refresh() {
        viewModelScope.launch {
            _state.postValue(FeedModelState(refreshing = true))
            try {
                repository.getAllAsync()

                _state.value = FeedModelState()
            } catch (_: Exception) {
                _state.value = FeedModelState(error = true)
            }
        }
    }

    fun showNewPosts() {
        viewModelScope.launch {
            repository.showAll()
        }
    }
}
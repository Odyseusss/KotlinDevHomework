package com.mynewproject.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.mynewproject.dto.Post
import com.mynewproject.model.FeedModel
import com.mynewproject.repository.GetAllCallBack
import com.mynewproject.repository.PostCallback
import com.mynewproject.repository.PostRepository
import com.mynewproject.repository.PostRepositoryNetwork
import com.mynewproject.repository.SaveCallback
import com.mynewproject.util.SingleLiveEvent

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
    private val repository: PostRepository = PostRepositoryNetwork()
    private val _data: MutableLiveData<FeedModel> = MutableLiveData(FeedModel())
    val data: LiveData<FeedModel>
        get() = _data
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

    fun save() {
        val post = edited.value ?: return
        repository.saveAsync(post, object : SaveCallback {
            override fun onSuccess(post: Post) {
                _postCreated.postValue(Unit)
                edited.postValue(empty)
                load()
            }

            override fun onError(e: Exception) {
                _data.value = FeedModel(error = true)
                _errorMessage.value = "Failed to save post"
            }

        })
    }

    fun load() {
        _data.postValue(FeedModel(loading = true))
        repository.getAllAsync(object : GetAllCallBack {
            override fun onSuccess(posts: List<Post>) {
                _data.value = FeedModel(posts = posts, empty = posts.isEmpty())
            }

            override fun onError(e: Throwable) {
                _data.value = FeedModel(error = true)
                _errorMessage.value = e.message ?: "Unknown error"
            }
        })
    }

    fun edit(post: Post) {
        edited.postValue(post)
    }

    fun shareById(id: Long) {
        repository.shareByIdAsync(id, object : PostCallback {
            override fun onSuccess() = load()
            override fun onError(e: Exception) {
                _errorMessage.value = "Failed to share post"
            }
        })
    }

    fun likeById(post: Post) {
        repository.likeByIdAsync(post, object : PostCallback {
            override fun onSuccess() = load()
            override fun onError(e: Exception) {
                _errorMessage.value = "Failed to like post"
            }
        })
    }

    fun removeById(id: Long) {
        repository.removeByIdAsync(id, object : PostCallback {
            override fun onSuccess() = load()
            override fun onError(e: Exception) {
                _errorMessage.value = "Failed to remove post"
            }
        })
    }

    fun changeContent(content: String) {
        edited.value = edited.value?.copy(content = content.trim())
    }

    fun cancelEdit() {
        edited.value = empty
    }
}
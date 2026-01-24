package com.mynewproject.repository

import com.mynewproject.dto.Post

interface PostRepository {
    fun getAll(): List<Post>
    fun shareById(id: Long)
    fun likeById(post: Post)
    fun removeById(id: Long)
    fun save(post: Post)

    fun getAllAsync(callback: GetAllCallBack)
    fun shareByIdAsync(id: Long, callback: PostCallback)
    fun likeByIdAsync(post: Post, callback: PostCallback)
    fun removeByIdAsync(id: Long, callback: PostCallback)
    fun saveAsync(post: Post, callback: SaveCallback)
}

interface GetAllCallBack {
    fun onSuccess(posts: List<Post>)
    fun onError(e: Throwable)
}

interface SaveCallback {
    fun onSuccess(post: Post)
    fun onError(e: Exception)
}

interface PostCallback {
    fun onSuccess()
    fun onError(e: Exception)
}
package com.mynewproject.repository

import androidx.lifecycle.LiveData
import com.mynewproject.dto.Post

interface PostRepository {
    val data: LiveData<List<Post>>
    suspend fun getAllAsync()
    suspend fun shareByIdAsync(id: Long)
    suspend fun likeByIdAsync(post: Post): Post
    suspend fun removeByIdAsync(id: Long)
    suspend fun saveAsync(post: Post): Post
}
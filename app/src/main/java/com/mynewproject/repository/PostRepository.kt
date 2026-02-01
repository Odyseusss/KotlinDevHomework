package com.mynewproject.repository

import com.mynewproject.dto.Post
import kotlinx.coroutines.flow.Flow

interface PostRepository {
    val data: Flow<List<Post>>
    suspend fun getAllAsync()
    suspend fun shareByIdAsync(id: Long)
    suspend fun likeByIdAsync(post: Post): Post
    suspend fun removeByIdAsync(id: Long)
    suspend fun saveAsync(post: Post): Post
    fun getNewer(id: Long): Flow<Int>
    suspend fun showAll()
}
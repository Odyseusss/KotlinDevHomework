package com.mynewproject.repository

import com.mynewproject.api.PostApi
import com.mynewproject.dao.PostDao
import com.mynewproject.dto.Post
import com.mynewproject.entity.PostEntity
import com.mynewproject.error.ApiError
import com.mynewproject.error.AppError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlin.collections.map


class PostRepositoryNetwork(private val dao: PostDao) : PostRepository {

    override val data = dao.getAll().map { it -> it.map { it.toDto() } }.flowOn(Dispatchers.Default)

    override suspend fun getAllAsync() {
        val posts: List<Post> = PostApi.service.getAll()
        dao.insert(posts.map(PostEntity::fromDto))
    }

    override suspend fun shareByIdAsync(id: Long) {
        dao.shareById(id)
    }

    override suspend fun likeByIdAsync(post: Post): Post {
        dao.likeById(post.id)
        return try {
            if (post.likedByMe) {
                PostApi.service.dislikeById(post.id)
            } else {
                PostApi.service.likeById(post.id)
            }
        } catch (e: Exception) {
            dao.likeById(post.id)
            throw e
        }
    }

    override suspend fun removeByIdAsync(id: Long) {
        val post = dao.getById(id)
        dao.removeById(id)
        try {
            PostApi.service.deleteById(id)
        } catch (e: Exception) {
            dao.insert(post)
            throw e
        }

    }

    override suspend fun saveAsync(post: Post): Post {
        val saved = PostApi.service.save(post)
        dao.save(PostEntity.fromDto(saved))
        return saved
    }

    override fun getNewer(id: Long): Flow<Int> = flow {
        while (true) {
            delay(10_000L)
            val response = PostApi.service.getNewer(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val body = response.body() ?: emptyList()

            if (body.isNotEmpty()) {
                dao.insert(body.map(PostEntity::fromDto))
            }
            emit(body.size)
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun showAll() {
        dao.showAll()
    }
}
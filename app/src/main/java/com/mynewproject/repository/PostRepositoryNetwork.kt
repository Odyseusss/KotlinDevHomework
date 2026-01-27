package com.mynewproject.repository


import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.mynewproject.api.PostApi
import com.mynewproject.dao.PostDao
import com.mynewproject.dto.Post
import com.mynewproject.entity.PostEntity


class PostRepositoryNetwork(private val dao: PostDao) : PostRepository {

    override val data: LiveData<List<Post>> = dao.getAll().map {
        it.map(PostEntity::toDto)
    }

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
}
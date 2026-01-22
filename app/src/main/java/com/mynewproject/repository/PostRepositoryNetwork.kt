package com.mynewproject.repository


import com.mynewproject.api.PostApi
import com.mynewproject.dto.Post
import retrofit2.Callback
import retrofit2.Call
import retrofit2.Response


class PostRepositoryNetwork() : PostRepository {

    override fun getAllAsync(callback: GetAllCallBack) {
        PostApi.service.getAll()
            .enqueue(object : Callback<List<Post>> {
                override fun onResponse(
                    call: Call<List<Post>>,
                    response: Response<List<Post>>
                ) {
                    if (!response.isSuccessful) {
                        callback.onError(RuntimeException("Server error: ${response.code()}"))
                        return
                    }

                    callback.onSuccess(response.body().orEmpty())
                }

                override fun onFailure(
                    call: Call<List<Post>>,
                    throwable: Throwable
                ) {
                    callback.onError(throwable)
                }

            })
    }

    override fun getAll(): List<Post> {
        return PostApi.service.getAll()
            .execute()
            .body()
            .orEmpty()
    }

    override fun likeById(post: Post) {
        PostApi.service.likeById(post.id)
            .execute()
    }

    override fun likeByIdAsync(post: Post, callback: PostCallback) {
        val call = if (!post.likedByMe) {
            PostApi.service.likeById(post.id)
        } else {
            PostApi.service.dislikeById(post.id)
        }
        call.enqueue(object : Callback<Post> {
            override fun onResponse(call: Call<Post>, response: Response<Post>) {
                callback.onSuccess()
            }

            override fun onFailure(call: Call<Post>, t: Throwable) {
                callback.onError(t as Exception)
            }
        })
    }

    override fun shareByIdAsync(id: Long, callback: PostCallback) {
        callback.onSuccess()
    }

    override fun shareById(id: Long) {
    }

    override fun removeByIdAsync(id: Long, callback: PostCallback) {
        PostApi.service.deleteById(id)
            .enqueue((object : Callback<Unit> {
                override fun onFailure(call: Call<Unit>, t: Throwable) {
                    callback.onError(t as Exception)
                }

                override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                    callback.onSuccess()
                }
            }))
    }

    override fun removeById(id: Long) {
        PostApi.service.deleteById(id)
            .execute()
    }

    override fun saveAsync(post: Post, callback: SaveCallback) {
        PostApi.service.save(post)
            .enqueue(object : Callback<Post> {
                override fun onResponse(call: Call<Post>, response: Response<Post>) {
                    callback.onSuccess(response.body()!!)
                }

                override fun onFailure(call: Call<Post>, t: Throwable) {
                    callback.onError(t as Exception)
                }
            })
    }

    override fun save(post: Post) {
        PostApi.service.save(post)
            .execute()
    }
}
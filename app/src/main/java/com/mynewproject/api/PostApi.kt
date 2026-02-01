package com.mynewproject.api

import com.mynewproject.BuildConfig
import com.mynewproject.dto.Post
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import java.util.concurrent.TimeUnit


private val client = OkHttpClient.Builder()
    .connectTimeout(30, TimeUnit.SECONDS)
    .apply {
        if (BuildConfig.DEBUG) {
            addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
        }
    }
    .build()

private const val BASE_URL = "${BuildConfig.BASE_URL}api/slow/"

private val retrofit = Retrofit.Builder()
    .client(client)
    .addConverterFactory(GsonConverterFactory.create())
    .baseUrl(BASE_URL)
    .build()

interface PostApi {
    @GET("posts")
    suspend fun getAll(): List<Post>

    @POST("posts")
    suspend fun save(@Body post: Post): Post

    @DELETE("posts/{id}")
    suspend fun deleteById(@Path("id") id: Long)

    @POST("posts/{id}/likes")
    suspend fun likeById(@Path("id") id: Long): Post

    @DELETE("posts/{id}/likes")
    suspend fun dislikeById(@Path("id") id: Long): Post

    @GET("posts/{id}/newer")
    suspend fun getNewer(@Path("id") id: Long): Response<List<Post>>

    companion object {
        val service: PostApi by lazy {
            retrofit.create(PostApi::class.java)
        }
    }
}
package com.mynewproject.dto

data class Post(
    val id: Long,
    val author: String,
    val published: String,
    val content: String,
    val authorAvatar: String?,
    val likes: Int = 0,
    val likedByMe: Boolean = false,
    val shares: Int = 0,
    val sharedByMe: Boolean = false,
    val video: String? = null,
)
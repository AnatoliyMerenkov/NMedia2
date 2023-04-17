package ru.netology.nmedia.dto

data class Post(
    val id: Long,
    val author: String,
    val content: String,
    val published: String,
    val likedByMe: Boolean = false,
    val like: Int = 0,
    val share: Int = 0,
    val view: Int = 0,
)

package ru.netology.nmedia.dto

import ru.netology.nmedia.enumeration.AttachmentType

data class Post(
    val id: Long,
    val author: String,
    val authorAvatar: String = "",
    val content: String,
    val likedByMe: Boolean = false,
    val likes: Int = 0,
    val published: Long = 0,
    val video: String? = null,
    val sharedByMe: Boolean = false,
    val share: Int = 0,
    val view: Int = 0,
    val attachment: Attachment? = null,
)

data class Attachment(
    val url: String,
    val description: String?,
    val type: AttachmentType,
)
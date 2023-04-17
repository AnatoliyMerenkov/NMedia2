package ru.netology.nmedia.activity

import ru.netology.nmedia.dto.Post

interface OnInteractionListener {
    fun onLike(post: Post)
    fun onShare(post: Post)
    fun onRemove(post: Post)
    fun onEdit(post: Post)
    fun onCancelEdit(post: Post)
    fun onPlayVideo(post: Post)
    fun onToPost(post: Post)
    fun onToImage(post: Post)
}
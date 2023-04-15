package ru.netology.nmedia.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.netology.nmedia.dto.Post


@Entity
data class PostEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val author: String,
    val authorAvatar: String,
    val content: String,
    val published: Long,
    val video: String? = null,
    val likedByMe: Boolean = false,
    val sharedByMe: Boolean = false,
    val likes: Int = 0,
    val share: Int = 0,
    val view: Int = 0,
) {
    fun toDto() = Post(
        this.id,
        this.author,
        this.content,
        this.authorAvatar,
        this.likedByMe,
        this.likes,
        this.published,
        this.video,
        this.sharedByMe,
        this.share,
        this.view
    )

    companion object {
        fun fromDto(post: Post) = PostEntity(
            post.id,
            post.author,
            post.authorAvatar,
            post.content,
            post.published,
            post.video,
            post.likedByMe,
            post.sharedByMe,
            post.likes,
            post.share,
            post.view
        )
    }
}

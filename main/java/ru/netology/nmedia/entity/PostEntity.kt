package ru.netology.nmedia.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.netology.nmedia.dto.Attachment
import ru.netology.nmedia.dto.Post


@Entity
data class PostEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val authorId: Long,
    val localId: Long,
    val author: String,
    val authorAvatar: String,
    val content: String,
    val likedByMe: Boolean = false,
    val likes: Int = 0,
    val published: Long,
    val video: String? = null,
    val sharedByMe: Boolean = false,
    val share: Int = 0,
    val view: Int = 0,
    @Embedded
    val attachment: Attachment? = null,
    val isVisible: Boolean = true,
) {
    fun toDto() = Post(
        this.id,
        this.authorId,
        this.localId,
        this.author,
        this.authorAvatar,
        this.content,
        this.likedByMe,
        this.likes,
        this.published,
        this.video,
        this.sharedByMe,
        this.share,
        this.view,
        this.attachment
    )

    companion object {
        fun fromDto(post: Post) = PostEntity(
            post.id,
            post.authorId,
            post.localId,
            post.author,
            post.authorAvatar,
            post.content,
            post.likedByMe,
            post.likes,
            post.published,
            post.video,
            post.sharedByMe,
            post.share,
            post.view,
            post.attachment
        )
    }
}

fun List<PostEntity>.toDto(): List<Post> = map(PostEntity::toDto)
fun List<Post>.toEntity(): List<PostEntity> = map(PostEntity::fromDto)
package ru.netology.nmedia.adapter

import android.os.Build
import android.view.View
import android.widget.PopupMenu
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import ru.netology.nmedia.activity.OnInteractionListener
import ru.netology.nmedia.util.Formatter
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.CardPostBinding
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.reposirory.PostRepositoryImpl

class PostViewHolder(
    private val binding: CardPostBinding,
    private val onInteractionListener: OnInteractionListener
) : RecyclerView.ViewHolder(binding.root) {

    @RequiresApi(Build.VERSION_CODES.O)
    fun bind(post: Post) {
        binding.apply {
            author.text = post.author
            published.text = Formatter.dateMilliToPostDateFormat(post.published)
            content.text = post.content
            like.isChecked = post.likedByMe
            like.text = Formatter.numberToShortFormat(post.likes)
            share.isChecked = post.sharedByMe
            //share.text = Formatter.numberToShortFormat(post.share)
            viewCount.text = Formatter.numberToShortFormat(post.view)
            video.isVisible = !post.video.isNullOrBlank()

            Glide.with(binding.avatar)
                .load(PostRepositoryImpl.getAvatarUrl(post.authorAvatar))
                .transform(RoundedCorners(30))
                .placeholder(R.drawable.ic_update_avatar)
                .error(R.drawable.ic_error_avatar)
                .timeout(10_000)
                .into(binding.avatar)

            val attachment = post.attachment
            if (attachment != null) {
                image.visibility = View.VISIBLE
                Glide.with(image)
                    .load(PostRepositoryImpl.getImageUrl(attachment.url))
                    .placeholder(R.drawable.ic_update_avatar)
                    .error(R.drawable.ic_error_avatar)
                    .timeout(10_000)
                    .into(image)
            } else {
                image.visibility = View.GONE
            }

            like.setOnClickListener {
                onInteractionListener.onLike(post)
            }

            share.setOnClickListener {
                onInteractionListener.onShare(post)
            }

            video.setOnClickListener {
                onInteractionListener.onPlayVideo(post)
            }

            cardPost.setOnClickListener {
                onInteractionListener.onToPost(post)
            }

            menu.isVisible = post.ownedByMe

            menu.setOnClickListener {
                PopupMenu(it.context, it).apply {
                    inflate(R.menu.options_post)
                    setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.remove -> {
                                onInteractionListener.onRemove(post)
                                true
                            }
                            R.id.edit -> {
                                onInteractionListener.onEdit(post)
                                true
                            }
                            else -> false
                        }
                    }
                }.show()
            }

            image.setOnClickListener {
                onInteractionListener.onToImage(post)
            }
        }
    }
}
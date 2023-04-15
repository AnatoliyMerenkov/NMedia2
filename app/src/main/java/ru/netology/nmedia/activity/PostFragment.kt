package ru.netology.nmedia.activity

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import ru.netology.nmedia.R
import ru.netology.nmedia.viewmodel.PostViewModel
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import ru.netology.nmedia.activity.NewPostFragment.Companion.textArg
import ru.netology.nmedia.databinding.FragmentPostBinding
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.reposirory.PostRepositoryImpl
import ru.netology.nmedia.util.Formatter
import ru.netology.nmedia.util.StringArg

class PostFragment : Fragment() {

    companion object {
        var Bundle.textId: String? by StringArg
    }

    private val viewModel: PostViewModel by viewModels(
        ownerProducer = ::requireParentFragment
    )

    lateinit var post: Post
    private lateinit var binding: FragmentPostBinding
    private var id: Long? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPostBinding.inflate(
            inflater,
            container,
            false
        )

        id = arguments?.textId?.toLong()

        setListeners()
        subscribe()
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun subscribe() {
        viewModel.data.observe(viewLifecycleOwner) {
            val requestPost = it.posts.find { post -> post.id == id }
            if (requestPost != null) {
                post = requestPost
                updateView()
            } else {
                findNavController().navigateUp()
            }
        }

        viewModel.edited.observe(viewLifecycleOwner) {
            if (it.id == 0L) {
                return@observe
            }
            findNavController().navigate(
                R.id.action_postFragment_to_newPostFragment,
                Bundle().apply { textArg = it.content }
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateView() {
        binding.apply {
            author.text = post.author
            published.text = Formatter.dateMilliToPostDateFormat(post.published)
            content.text = post.content
            like.text = Formatter.numberToShortFormat(post.likes)
            like.isChecked = post.likedByMe == true
            share.text = Formatter.numberToShortFormat(post.share)
            viewCount.text = Formatter.numberToShortFormat(post.view)
            video.visibility = if (post.video.isNullOrBlank()) View.GONE else View.VISIBLE

            Glide.with(avatar)
                .load(PostRepositoryImpl.getAvatarUrl(post.authorAvatar))
                .transform(RoundedCorners(30))
                .placeholder(R.drawable.ic_update_avatar)
                .error(R.drawable.ic_error_avatar)
                .timeout(10_000)
                .into(avatar)

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
        }
    }

    private fun setListeners() {
        binding.apply {

            like.setOnClickListener {
                viewModel.likeById(post.id)
            }

            share.setOnClickListener {
                val intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, post.content)
                    type = "text/plain"
                }
                val shareIntent =
                    Intent.createChooser(intent, getString(R.string.chooser_share_post))
                startActivity(shareIntent)
            }

            menu.setOnClickListener {
                PopupMenu(it.context, it).apply {
                    inflate(R.menu.options_post)
                    setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.remove -> {
                                viewModel.removeById(post.id)
                                true
                            }
                            R.id.edit -> {
                                viewModel.edit(post)
                                true
                            }
                            else -> false
                        }
                    }
                }.show()
            }

            video.setOnClickListener {
                if (post.video.isNullOrBlank()) return@setOnClickListener
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(post.video))
                startActivity(intent)
            }
        }
    }
}
package ru.netology.nmedia.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import ru.netology.nmedia.R
import ru.netology.nmedia.adapter.PostsAdapter
import ru.netology.nmedia.databinding.FragmentFeedBinding
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.viewmodel.PostViewModel
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import ru.netology.nmedia.activity.ImageFragment.Companion.imageUrlArg
import ru.netology.nmedia.activity.NewPostFragment.Companion.idEditedPostArg
import ru.netology.nmedia.activity.PostFragment.Companion.id
import ru.netology.nmedia.model.FeedModelState

class FeedFragment : Fragment() {

    private val viewModel: PostViewModel by viewModels(
        ownerProducer = ::requireParentFragment
    )
    private lateinit var binding: FragmentFeedBinding
    private lateinit var adapter: PostsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFeedBinding.inflate(
            inflater,
            container,
            false
        )

        adapter = PostsAdapter(object : OnInteractionListener {
            override fun onLike(post: Post) {
                if (viewModel.checkLogin(this@FeedFragment)) {
                    viewModel.likeById(post.id)
                }
            }

            override fun onShare(post: Post) {
                val intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, post.content)
                    type = "text/plain"
                }
                val shareIntent =
                    Intent.createChooser(intent, getString(R.string.chooser_share_post))
                startActivity(shareIntent)
            }

            override fun onRemove(post: Post) {
                viewModel.removeById(post.id)
            }

            override fun onEdit(post: Post) {
                viewModel.edit(post)
            }

            override fun onCancelEdit(post: Post) {
                viewModel.clearEdited()
            }

            override fun onPlayVideo(post: Post) {
                if (post.video.isNullOrBlank()) return
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(post.video))
                startActivity(intent)
            }

            override fun onToPost(post: Post) {
                findNavController().navigate(
                    R.id.action_feedFragment_to_postFragment,
                    Bundle().apply { id = post.id })
            }

            override fun onToImage(post: Post) {
                post.attachment?.let {
                    findNavController().navigate(
                        R.id.action_feedFragment_to_imageFragment,
                        Bundle().apply { imageUrlArg = it.url }
                    )
                }
            }
        })
        binding.list.adapter = adapter
        subscribe()
        setListeners()

        return binding.root
    }

    private fun subscribe() {
        viewModel.data.observe(viewLifecycleOwner) { state ->
            adapter.submitList(state.posts) {
            }
            binding.emptyText.isVisible = state.empty
        }

        viewModel.state.observe(viewLifecycleOwner) { state ->
            binding.swipeRefresh.isRefreshing = state is FeedModelState.Refreshing
            binding.progress.isVisible = state is FeedModelState.Loading
            if (state is FeedModelState.Error) {
                Snackbar.make(binding.root, R.string.error_loading, Snackbar.LENGTH_SHORT)
                    .setAction(R.string.retry_loading) {
                        viewModel.loadPost()
                    }
                    .show()
            }
        }

        viewModel.edited.observe(viewLifecycleOwner) { post ->
            if (post.id == 0L) {
                return@observe
            }
            findNavController().navigate(
                R.id.action_feedFragment_to_newPostFragment,
                Bundle().apply { idEditedPostArg = post.id }
            )
        }

        viewModel.newerCount.observe(viewLifecycleOwner) { count ->
            if (count > 0) {
                val contentNewPost = "${getString(R.string.new_posts)} ($count)"
                binding.toNewPosts.text = contentNewPost
                binding.toNewPosts.visibility = View.VISIBLE
            }
        }

        viewModel.postsShowed.observe(viewLifecycleOwner) {
            binding.list.smoothScrollToPosition(0)
        }
    }

    private fun setListeners() {
        binding.add.setOnClickListener {
            if (viewModel.checkLogin(this)) {
                findNavController().navigate(R.id.action_feedFragment_to_newPostFragment)
            }
        }

        binding.toNewPosts.setOnClickListener {
            viewModel.showNewerPosts()
            it.visibility = View.GONE
        }

        binding.swipeRefresh.setOnRefreshListener {
            viewModel.refresh()
        }
    }
}
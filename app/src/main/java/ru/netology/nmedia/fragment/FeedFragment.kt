package ru.netology.nmedia.fragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import ru.netology.nmedia.R
import ru.netology.nmedia.adapter.PostActionListener
import ru.netology.nmedia.adapter.PostAdapter
import ru.netology.nmedia.databinding.FragmentFeedBinding
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.fragment.NewPostOrEditPostFragment.Companion.textArg
import ru.netology.nmedia.fragment.ShowImageFragment.Companion.showImage
import ru.netology.nmedia.fragment.ShowPostFragment.Companion.showOnePost
import ru.netology.nmedia.viewmodel.PostViewModel

class FeedFragment : Fragment(R.layout.fragment_feed) {

    private val viewModel by viewModels<PostViewModel>(
        ownerProducer = ::requireParentFragment
    )

    private var _binding: FragmentFeedBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return super.onCreateView(inflater, container, savedInstanceState)?.also {
            _binding = FragmentFeedBinding.bind(it)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = PostAdapter(
            object : PostActionListener {
                override fun edit(post: Post) {
                    viewModel.edit(post)
                    findNavController().navigate(
                        R.id.to_newPostOrEditPostFragment,
                        Bundle().apply { textArg = post.content })
                }

                override fun remove(post: Post) {
                    viewModel.remove(post.id)
                }

                override fun like(post: Post) {
                    viewModel.like(post.id)
                }

                override fun share(post: Post) {
                    val intent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, post.content)
                        type = "text/plain"
                    }

                    val shareIntent =
                        Intent.createChooser(intent, getString(R.string.chooser_share_post))
                    startActivity(shareIntent)
                }

                override fun video(post: Post) {
                    val intentVideo = Intent(Intent.ACTION_VIEW, Uri.parse(post.video))
                    startActivity(intentVideo)
                }

                override fun showPost(post: Post) {
                    findNavController().navigate(
                        R.id.action_feedFragment_to_showPostFragment,
                        Bundle().apply { showOnePost = post.id })
                }

                override fun showImage(post: Post) {
                    findNavController().navigate(
                        R.id.action_feedFragment_to_showImageFragment,
                        Bundle().apply { showImage = post.attachment?.url }
                    )
                }
            }
        )

        binding.container.adapter = adapter
        viewModel.data.observe(viewLifecycleOwner) { state ->
            adapter.submitList(state.posts)
            binding.apply {
                emptyText.isVisible = state.empty
            }
        }

        viewModel.dataState.observe(viewLifecycleOwner) { state ->
            binding.apply {
                swipeRefresh.isRefreshing = state.loading
                progress.isVisible = state.loading
                swipeRefresh.isRefreshing = state.refreshing
                if (state.error) {
                    Snackbar.make(binding.root, R.string.error_loading, Snackbar.LENGTH_INDEFINITE)
                        .setAction(R.string.retry_loading) { viewModel.retry() }
                        .show()
                }
            }
        }

        viewModel.newerCount.observe(viewLifecycleOwner) { state ->
            if (state > 0) binding.buttonFreshPosts.visibility = View.VISIBLE
            println(state)
        }

        with(binding) {
            fab.setOnClickListener {
                findNavController().navigate(R.id.to_newPostOrEditPostFragment)
            }

            swipeRefresh.setOnRefreshListener {
                viewModel.refresh()
            }

            buttonFreshPosts.setOnClickListener {
                lifecycleScope.launch {
                    viewModel.getNewPosts()
                    viewModel.loadPosts()
                    container.smoothScrollToPosition(0)
                }
                it.visibility = View.GONE
            }
        }
    }

    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }
}
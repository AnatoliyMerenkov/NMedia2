package ru.netology.nmedia.activity

import android.app.Activity
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toFile
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.github.dhaval2404.imagepicker.ImagePicker
import ru.netology.nmedia.R
import ru.netology.nmedia.activity.ImageFragment.Companion.imageUrlArg
import ru.netology.nmedia.databinding.FragmentNewPostBinding
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.util.AndroidUtils
import ru.netology.nmedia.util.LongArg
import ru.netology.nmedia.util.StringArg
import ru.netology.nmedia.viewmodel.PostViewModel

class NewPostFragment : Fragment() {

    private val viewModel: PostViewModel by viewModels(
        ownerProducer = ::requireParentFragment
    )
    private lateinit var binding: FragmentNewPostBinding
    private var id: Long? = null
    lateinit var post: Post

    private val imageLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            when (it.resultCode) {
                ImagePicker.RESULT_ERROR -> {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.photo_error),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                Activity.RESULT_OK -> {
                    val uri = it.data?.data
                    viewModel.savePhoto(uri, uri?.toFile())
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNewPostBinding.inflate(
            inflater,
            container,
            false
        )

        setContent()
        addMenu()
        addBackPressedAction()
        subscribe()
        setListeners()
        binding.content.requestFocus()

        return binding.root
    }

    private fun addBackPressedAction() {
        val callbackNoEdit = object : OnBackPressedCallback(viewModel.edited.value?.id != 0L) {
            override fun handleOnBackPressed() {
                viewModel.clearEdited()
                viewModel.clearDraft()
                findNavController().navigateUp()
            }
        }
        val callbackWithDraft = object : OnBackPressedCallback(viewModel.edited.value?.id == 0L) {
            override fun handleOnBackPressed() {
                if (binding.content.toString().trim().isNotBlank()) {
                    viewModel.draftContent.value = binding.content.text.toString()
                }
                findNavController().navigateUp()
            }
        }
        val backPressedDispatcher = requireActivity().onBackPressedDispatcher
        backPressedDispatcher.addCallback(viewLifecycleOwner, callbackNoEdit)
        backPressedDispatcher.addCallback(viewLifecycleOwner, callbackWithDraft)
    }

    private fun addMenu() {
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onMenuItemSelected(menuItem: MenuItem): Boolean =
                if (menuItem.itemId == R.id.save) {
                    val content = binding.content.text.toString()
                    if (content.isNotBlank()) {
                        if (viewModel.checkLogin(this@NewPostFragment)) {
                            viewModel.changeContentAndSave(content)
                        }
                    } else {
                        viewModel.clearEdited()
                    }
                    viewModel.clearDraft()
                    AndroidUtils.hideKeyboard(requireView())
                    true
                } else {
                    false
                }

            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.new_post_menu, menu)
            }
        }, viewLifecycleOwner)
    }

    private fun setContent() {
        if (arguments?.idEditedPostArg == null) {
            if (viewModel.draftContent.value.toString().isNotBlank()) {
                viewModel.draftContent.value.let(binding.content::setText)
            }
        } else {
            id = arguments?.idEditedPostArg
            val requestPost = viewModel.data.value?.posts?.find { post -> post.id == id }
            requestPost?.let { post ->
                with(binding) {
                    content.setText(post.content)
                    post.attachment?.let {
                        //todo необходимо выяснить, как выгрузить картинку из сервера в photo
//                        ImagePicker.Builder(this)
//                            .cameraOnly()
//                            .maxResultSize(2048, 2048)
//                            .createIntent(imageLauncher::launch)
//                          Glide.with(preview)
//                            .load(PostRepositoryImpl.getImageUrl(post.attachment.url))
//                            .placeholder(R.drawable.ic_update_avatar)
//                            .error(R.drawable.ic_error_avatar)
//                            .timeout(10_000)
//                            .into(preview)
//                        val uri = it.url
//                        viewModel.savePhoto(uri, uri?.toFile())
                    }
                    if (post.attachment != null) {


                    } else {
                        preview.visibility = View.GONE
                    }
                }
            }
        }
    }

    private fun subscribe() {
        viewModel.postCreated.observe(viewLifecycleOwner) {
            viewModel.loadPost()
            findNavController().navigateUp()
        }

        viewModel.photo.observe(viewLifecycleOwner) {
            binding.imageGroup.isVisible = it?.uri != null
            binding.preview.setImageURI(it?.uri)
        }
    }

    private fun setListeners() {
        binding.takePhoto.setOnClickListener {
            ImagePicker.Builder(this)
                .cameraOnly()
                .maxResultSize(2048, 2048)
                .createIntent(imageLauncher::launch)
        }

        binding.gallery.setOnClickListener {
            ImagePicker.Builder(this)
                .galleryOnly()
                .maxResultSize(2048, 2048)
                .createIntent(imageLauncher::launch)
        }

        binding.clear.setOnClickListener {
            viewModel.savePhoto(null, null)
        }

        binding.preview.setOnClickListener {
            post.attachment?.let {
                findNavController().navigate(
                    R.id.action_newPostFragment_to_imageFragment,
                    Bundle().apply { imageUrlArg = it.url }
                )
            }
        }
    }

    companion object {
        var Bundle.idEditedPostArg: Long by LongArg
        var Bundle.textArg: String? by StringArg
    }
}
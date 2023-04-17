package ru.netology.nmedia.activity

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.FragmentImageBinding
import ru.netology.nmedia.reposirory.PostRepositoryImpl
import ru.netology.nmedia.util.StringArg

class ImageFragment : Fragment() {

    private lateinit var binding: FragmentImageBinding
    private lateinit var imageUrl: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentImageBinding.inflate(inflater, container, false)
        setImage()

        return binding.root
    }

    private fun setImage() {
        if (!arguments?.imageUrlArg.isNullOrEmpty()) {
            arguments?.imageUrlArg?.let {
                imageUrl = it
                with(binding) {
                    Glide.with(image)
                        .load(PostRepositoryImpl.getImageUrl(imageUrl))
                        .placeholder(R.drawable.ic_update_avatar)
                        .error(R.drawable.ic_error_avatar)
                        .timeout(10_000)
                        .into(image)
                }
            }
        }
    }

    companion object {
        var Bundle.imageUrlArg: String? by StringArg
    }
}
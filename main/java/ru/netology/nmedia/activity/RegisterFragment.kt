package ru.netology.nmedia.activity

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.FragmentRegisterBinding
import ru.netology.nmedia.util.DialogManager
import ru.netology.nmedia.viewmodel.AuthViewModel
import kotlin.math.log

class RegisterFragment : Fragment() {

    private lateinit var binding: FragmentRegisterBinding
    private val viewModel by viewModels<AuthViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRegisterBinding.inflate(inflater, container, false)

        setListeners()
        subscribe()
        return binding.root
    }

    private fun setListeners() {
        with(binding) {
            registerButton.setOnClickListener {
                if (isPasswordsDifferent()) {
                    DialogManager.differentPasswordsDialog(this@RegisterFragment)
                    return@setOnClickListener
                }
                val name = name.text.toString()
                val login = login.text.toString()
                val password = password.text.toString()

                viewModel.register(login, password, name)
            }
        }
    }

    private fun isPasswordsDifferent() = with(binding) {
        password.text.toString() != rePassword.text.toString()
    }

    private fun subscribe() {
        viewModel.token.observe(viewLifecycleOwner) {
            it?.let {
                viewModel.singIn(it.id, it.token)
                findNavController().navigateUp()
            }
        }
    }
}
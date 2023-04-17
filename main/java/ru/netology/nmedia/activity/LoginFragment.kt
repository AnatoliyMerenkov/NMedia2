package ru.netology.nmedia.activity

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import ru.netology.nmedia.databinding.FragmentLoginBinding
import ru.netology.nmedia.viewmodel.AuthViewModel

class LoginFragment : Fragment() {

    private lateinit var binding: FragmentLoginBinding
    private val viewModel by viewModels<AuthViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLoginBinding.inflate(inflater, container, false)

        setListeners()
        subscribe()
        return binding.root
    }

    private fun setListeners() {
        with(binding) {
            loginButton.setOnClickListener {
                val login = login.text.toString()
                val password = password.text.toString()
                viewModel.logIn(login, password)
            }
        }
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
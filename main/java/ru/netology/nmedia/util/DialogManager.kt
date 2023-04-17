package ru.netology.nmedia.util

import android.app.AlertDialog
import android.content.Context
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import ru.netology.nmedia.R
import ru.netology.nmedia.activity.FeedFragment
import ru.netology.nmedia.activity.NewPostFragment
import ru.netology.nmedia.activity.PostFragment

object DialogManager {
    fun errorDialog(context: Context, e: Exception) {
        val builder = AlertDialog.Builder(context)
        val dialog = builder.create()
        dialog.setTitle("Error")
        val massage = makeErrorMessage(e)
        dialog.setMessage(massage)
        dialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK") { _, _ ->
            dialog.dismiss()
        }
        dialog.show()
    }

    fun differentPasswordsDialog(fragment: Fragment) {
        val builder = AlertDialog.Builder(fragment.requireContext())
        val dialog = builder.create()
        dialog.setTitle("Error")
        dialog.setMessage("Passwords are different")
        dialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK") { _, _ ->
            dialog.dismiss()
        }
        dialog.show()
    }

    fun errorAuthDialog(fragment: Fragment) {
        val builder = AlertDialog.Builder(fragment.requireContext())
        val dialog = builder.create()
        dialog.setTitle("Auth error")
        dialog.setMessage("Sign In to yur account")
        dialog.setButton(AlertDialog.BUTTON_POSITIVE, "Sign in") { _, _ ->
            when (fragment) {
                is FeedFragment -> fragment.findNavController().navigate(R.id.action_feedFragment_to_loginFragment)
                is PostFragment -> fragment.findNavController().navigate(R.id.action_postFragment_to_loginFragment)
                is NewPostFragment -> fragment.findNavController().navigate(R.id.action_newPostFragment_to_loginFragment)
            }
        }
        dialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Sign up") { _, _ ->
            when (fragment) {
                is FeedFragment -> fragment.findNavController().navigate(R.id.action_feedFragment_to_registerFragment)
                is PostFragment -> fragment.findNavController().navigate(R.id.action_postFragment_to_registerFragment)
                is NewPostFragment -> fragment.findNavController().navigate(R.id.action_newPostFragment_to_registerFragment)
            }
        }
        dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel") { _, _ ->
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun makeErrorMessage(e: Exception): String {
        return e.message.toString()
    }
}
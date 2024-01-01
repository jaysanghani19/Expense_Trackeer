package com.jay.expensetracker.signinactivity.forgotpassword

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.jay.expensetracker.DatabaseClass
import com.jay.expensetracker.R
import com.jay.expensetracker.customlayout.ProgressBarDialog
import com.jay.expensetracker.databinding.ForgotPasswordFragmentBinding


class ForgotPassword : Fragment() {
    private val progressBarDialog = ProgressBarDialog()
    private lateinit var binding: ForgotPasswordFragmentBinding

    @SuppressLint("ResourceAsColor")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {

        binding =
            DataBindingUtil.inflate(inflater, R.layout.forgot_password_fragment, container, false)
        binding.cardview.setCardBackgroundColor(R.color.black)

        binding.tvLogin.setOnClickListener {
            clickListenerLogin()
        }
        binding.btnSetPassword.setOnClickListener {
            progressBarDialog.show(childFragmentManager, "ProgressBar")
            clickListenerButtonSetPassword()
        }
        return binding.root
    }

    //    Setting setOnClickListener functions
    private fun clickListenerLogin() {
        findNavController().navigate(ForgotPasswordDirections.actionForgotPasswordToSignIn())
    }

    private fun clickListenerButtonSetPassword() {
        val email = binding.email.text.toString()
        if (!DatabaseClass.isInternetAvailable(requireContext())) {
            Toast.makeText(requireContext(), "Connect to the Internet", Toast.LENGTH_LONG).show()
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.email.error = "Enter valid Email Address"
            binding.email.requestFocus()
            progressBarDialog.dismiss()
        } else {
            resetPassword()
        }

    }

    //        Using this function we'll send reset password link
//        Sending reset password mail using FirebaseAuth
    private fun resetPassword() {
//        Essential data for Firebase Auth
        val mail = binding.email.text.toString()
//        Getting instance of FirebaseAuth from UserDatabase class
        val auth = DatabaseClass.getInstanceOfFirebaseAuth()

//        Sending the reset password link
//        if the user is registered than it'll send the link to the email
//        otherwise it'll toast message that email address is incorrect
        auth.sendPasswordResetEmail(mail).addOnCompleteListener {
            if (it.isSuccessful) {
                progressBarDialog.dismiss()
                Toast.makeText(
                    requireContext(),
                    "Reset Password link sent successfully",
                    Toast.LENGTH_SHORT
                ).show()
                findNavController().navigate(ForgotPasswordDirections.actionForgotPasswordToSignIn())
            } else {
                try {
                    throw it.exception!!
                } catch (e: FirebaseAuthInvalidUserException) {
                    binding.email.error = "Invalid Email"
                    Toast.makeText(
                        requireContext(),
                        "Email isn't Registered",
                        Toast.LENGTH_LONG
                    ).show()
                    binding.email.requestFocus()
                } catch (e: Exception) {
                    Toast.makeText(
                        requireContext(),
                        "Try Again\nThere's error on our side",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                progressBarDialog.dismiss()
            }
        }
    }

}
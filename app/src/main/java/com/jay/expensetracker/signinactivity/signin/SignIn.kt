package com.jay.expensetracker.signinactivity.signin

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.jay.expensetracker.DatabaseClass
import com.jay.expensetracker.MainActivity
import com.jay.expensetracker.R
import com.jay.expensetracker.customlayout.ProgressBarDialog
import com.jay.expensetracker.databinding.SignInFragmentBinding

class SignIn : Fragment() {

    private lateinit var binding: SignInFragmentBinding

    private val progressBarDialog = ProgressBarDialog()

    @SuppressLint("ResourceAsColor")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?,
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.sign_in_fragment, container, false)

        binding.cardview.setCardBackgroundColor(R.color.black)
//        binding.btnLogin.setBackgroundResource(R.drawable.button)

        binding.signUp.setOnClickListener {
            clickListenerOfSignUp()
        }
        binding.tvForgotPassword.setOnClickListener {
            clickListenerOfForgotPassword()
        }

        binding.btnLogin.setOnClickListener {
            progressBarDialog.show(childFragmentManager, "ProgressBar")
            clickListenerOfLogin()
        }
        return binding.root
    }

    //        Creating Functions for setOnClickListener
    private fun clickListenerOfForgotPassword() {
        findNavController().navigate(SignInDirections.actionSignInToForgotPassword())
    }

    private fun clickListenerOfSignUp() {
        findNavController().navigate(SignInDirections.actionSignInToSignUp())
    }

    private fun clickListenerOfLogin() {
//        Checking that all data field has been filled or not
//        if not than it'll show the error other wise it'll log in
        if (!DatabaseClass.isInternetAvailable(requireContext())) {
            Toast.makeText(requireContext(), "Connect To the Internet", Toast.LENGTH_LONG).show()
        } else if (TextUtils.isEmpty(binding.email.text.toString())) {
            binding.email.error = "Enter the Email"
            binding.email.requestFocus()
            progressBarDialog.dismiss()
        } else if (TextUtils.isEmpty(binding.password.text.toString())) {
            binding.password.error = "Enter the password"
            binding.password.requestFocus()
            progressBarDialog.dismiss()
        } else {
            userLogin()
        }
    }

    //            Creating function for log in using firebase
    private fun userLogin() {

//        Essential data for Login
        val email = binding.email.text.toString()
        val password = binding.password.text.toString()

//        Getting instance of the FirebaseAuth for login
        val auth = DatabaseClass.getInstanceOfFirebaseAuth()


//    log in using Firebase Authentication
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener {

//            Checking that log in was successful or not
//            if log in is successful than we've redirect to the DashboardActivty
//            else we've toast the message that password or email is incorrect

            if (it.isSuccessful) {
//                binding.progressCircular.visibility = View.GONE
//                Checking that user has verified the email
//                if user have verified their email than they'll redirect to the DashboardActivity
//                else they'll be can't log in

                if (auth.currentUser?.isEmailVerified == true) {
                    Toast.makeText(requireContext(), "Log in Successfully", Toast.LENGTH_SHORT)
                        .show()
                    progressBarDialog.dismiss()
                    val intent = Intent(requireContext(), MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)

                } else {
                    progressBarDialog.dismiss()
                    Toast.makeText(
                        requireContext(),
                        "Email hasn't verified /n Please verify E-mail/n we've send the mail for verification",
                        Toast.LENGTH_SHORT
                    ).show()
                    auth.currentUser?.sendEmailVerification()
                }

            } else {
                try {
                    throw it.exception!!
                } catch (e: FirebaseAuthInvalidUserException) {
                    binding.email.error = "Invalid E-mail. Register your self"
                    Toast.makeText(
                        requireContext(),
                        "Email isn't registered. \n Register your self",
                        Toast.LENGTH_SHORT
                    ).show()
                    val runnable = Runnable {
                        findNavController().navigate(SignInDirections.actionSignInToSignUp())
                    }
                    @Suppress("DEPRECATION") val handler = Handler()
                    handler.postDelayed(runnable, 2000)
                } catch (e: FirebaseAuthInvalidCredentialsException) {
                    binding.password.error = "Incorrect Password"
                    binding.password.requestFocus()
                } catch (e: Exception) {
                    Toast.makeText(
                        requireContext(),
                        "There's Error on our side\nTry Again",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                progressBarDialog.dismiss()
            }
        }
    }
}
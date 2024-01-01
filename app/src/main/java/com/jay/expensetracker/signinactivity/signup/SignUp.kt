package com.jay.expensetracker.signinactivity.signup

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.database.DatabaseReference
import com.jay.expensetracker.DatabaseClass
import com.jay.expensetracker.R
import com.jay.expensetracker.customlayout.ProgressBarDialog
import com.jay.expensetracker.databinding.SignUpFragmentBinding
import com.jay.expensetracker.dataclass.NoOfEntriesDataClass
import com.jay.expensetracker.dataclass.UserDataClass
import kotlin.properties.Delegates

class SignUp : Fragment() {

    // progress bar varible

    private val progressBarDialog = ProgressBarDialog()

    //    Reference of the userDatabase
    private lateinit var userDatabase: DatabaseReference

    //    Reference of EntriesDatabase
    private lateinit var entriesDatabase: DatabaseReference

    //    Reference of the Firebase Auth
    private lateinit var authenticator: FirebaseAuth

    //    DataBinding of the SignUp fragment
    private lateinit var binding: SignUpFragmentBinding

    //    user has checked male or female
//    for that we'll store the id of that
    private var checkedId by Delegates.notNull<Int>()

    //    in case the Firebase Database's instance wasn't created we'll manually create using the options
    private val options = FirebaseOptions.Builder()
        .setDatabaseUrl(/* databaseUrl = */ "https://console.firebase.google.com/u/0/project/expense-tracker-2164c/database/expense-tracker-2164c-default-rtdb/data/~2F")
        .setApplicationId("1:881278095168:android:5aa3a8e47bf5b67a5134a5")
        .setApiKey("AIzaSyAuMxZHxSldQOiNkFSfe8UH68Qp2nqv5mc")
        .build()

    companion object {
        const val MALE = "Male"
        const val FEMALE = "Female"
        const val TRANSGENDER = "Transgender"
    }

    @SuppressLint("ResourceAsColor")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.sign_up_fragment, container, false)

        binding.cardview.setCardBackgroundColor(R.color.black)

//        Creating the Firebase App in case it wasn't created automatically
        if (FirebaseApp.getApps(requireContext()).isEmpty()) {
            FirebaseApp.initializeApp(requireContext(), options)
        }

//        ClickListener of the views
        binding.signin.setOnClickListener {
            clickListenerOfSignIn()
        }

        binding.buttonSubmit.setOnClickListener {
            progressBarDialog.show(childFragmentManager, "ProgressBar")
            clickListenerOfButtonSubmit()
        }
        binding.buttonReset.setOnClickListener {
            clickListenerButtonReset()
        }
        return binding.root
    }

//    Functions for clickListener of views

    private fun clickListenerButtonReset() {
        binding.firstName.text = null
        binding.lastName.text = null
        binding.email.text = null
        binding.password.text = null
        binding.radioGroup.clearCheck()
    }

    private fun clickListenerOfSignIn() {
        findNavController().navigate(SignUpDirections.actionSignUpToSignIn())
    }

    private fun clickListenerOfButtonSubmit() {

        checkedId = binding.radioGroup.checkedRadioButtonId

//        Checking that all data field has been filled or not
//        if not filled than it'll show the error other wise it'll create user using FirebaseAuth
        if (!DatabaseClass.isInternetAvailable(requireContext())) {
            Toast.makeText(requireContext(), "Connect to the Internet", Toast.LENGTH_SHORT).show()
        } else if (TextUtils.isEmpty(binding.firstName.text.toString())) {
            binding.firstName.error = "First Name is required"
            binding.firstName.requestFocus()
            progressBarDialog.dismiss()
        } else if (TextUtils.isEmpty(binding.lastName.text.toString())) {
            binding.lastName.error = "Last Name is required"
            binding.lastName.requestFocus()
            progressBarDialog.dismiss()
        } else if (!Patterns.EMAIL_ADDRESS.matcher(binding.email.text.toString()).matches()) {
            binding.email.error = "Enter valid E-mail is required"
            binding.email.requestFocus()
            progressBarDialog.dismiss()
        } else if (binding.mobileNo.text.toString().length != 10) {
            binding.mobileNo.error = "Enter Full Mobile Number"
            binding.mobileNo.requestFocus()
            progressBarDialog.dismiss()
        } else if (TextUtils.isEmpty(binding.password.text.toString())) {
            binding.password.error = "Password is required"
            binding.password.requestFocus()
            progressBarDialog.dismiss()
        } else if (binding.password.text.toString().length < 8) {
            binding.password.error = "Password length should be more than 8"
            binding.password.requestFocus()
            progressBarDialog.dismiss()
        } else if (checkedId == -1) {
            Toast.makeText(requireContext(), "Select the Gender", Toast.LENGTH_SHORT).show()
            progressBarDialog.dismiss()

        } else {
            userRegister()
        }
    }

    //        Register the user using Firebase authentication
    private fun userRegister() {

//        Essential data for registration
        val firstName = binding.firstName.text.toString()
        val lastName = binding.lastName.text.toString()
        val email = binding.email.text.toString()
        val password = binding.password.text.toString()
        val mobileNo = binding.mobileNo.text.toString()
        val gender: String = when (checkedId) {
            R.id.male_radio_button -> {
                MALE
            }

            R.id.female_radio_button -> {
                FEMALE
            }

            else -> TRANSGENDER
        }

        val entriesData = NoOfEntriesDataClass(0)

//        Firebase Database Instance with reference From UserDatabase
        this.userDatabase =
            DatabaseClass.getInstanceOfUserDatabase()
                .getReference(DatabaseClass.USERDATA_DATABASE_PATH)

//        Getting instance of the FirebaseAuth for Registration from UserDatabase Class
        this.authenticator = DatabaseClass.getInstanceOfFirebaseAuth()

//        Registering the user using firebase authentication
        this.authenticator.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { it ->

//            if Registering is successful than we've redirect to the log in page for log in
//            Else we've toast message that there's some error during the registering

                if (it.isSuccessful) {

                    val uid = authenticator.currentUser?.uid.toString()
                    //        setting users data in userData class
                    val userData = UserDataClass(
                        uid,
                        firstName,
                        lastName,
                        email,
                        gender,
                        mobileNo
                    )
//                Storing data of user while registering
                    this.userDatabase.child(authenticator.uid.toString()).setValue(userData)
                        .addOnCompleteListener {

//                        if data Inserting is successful Then it'll redirected to signin page
//                        else it'll delete the user and showing the message for user hasn't registered
                            if (it.isSuccessful) {

//                               Reference of the Entries child where all entries will be stored
//                               Storing the number of entries in entries child Right now it's 0
                                val tempUID = authenticator.currentUser?.uid.toString()
                                entriesDatabase =
                                    DatabaseClass.getInstanceOfEntriesDatabase()
                                        .getReference(tempUID)

                                entriesDatabase.child(DatabaseClass.NO_OF_ENTRIES)
                                    .setValue(entriesData).addOnCompleteListener {
                                        if (it.isSuccessful) {
                                            Toast.makeText(
                                                requireContext(),
                                                "User is registered \nWe've send Verification email \nVerify your email address\n Before log in",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            this.authenticator.currentUser?.sendEmailVerification()
                                            progressBarDialog.dismiss()
                                            findNavController().navigate(SignUpDirections.actionSignUpToSignIn())
                                        } else {
//                                        Removing the current user from authentication and removing the child where i've stored the data of the user
                                            authenticator.currentUser?.delete()
                                            userDatabase.child(authenticator.uid.toString())
                                                .removeValue()

                                            Toast.makeText(
                                                requireContext(),
                                                "User hasn't Registered \n Try again",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            progressBarDialog.dismiss()
                                        }
                                    }
                            } else {
//                                Removing the user from authentication
                                authenticator.currentUser?.delete()

                                Toast.makeText(
                                    requireContext(),
                                    "User hasn't Registered \n Try again",
                                    Toast.LENGTH_SHORT
                                ).show()
                                progressBarDialog.dismiss()
                            }
                        }

                } else {
                    authenticator.currentUser?.delete()
                    try {
                        throw it.exception!!
                    } catch (e: FirebaseAuthInvalidCredentialsException) {
                        binding.email.error = "Email is invalid or already in use"
                        binding.email.requestFocus()
                    } catch (e: FirebaseAuthUserCollisionException) {
                        binding.email.error = "Email is already registered"
                        Toast.makeText(
                            requireContext(),
                            "User is registered \nyou can log in",
                            Toast.LENGTH_SHORT
                        ).show()
//                    Navigating SignUp to SignIn Fragment
//                    Creating runnable and handler for delaying 2 second
                        val runnable = Runnable {
                            findNavController().navigate(SignUpDirections.actionSignUpToSignIn())
                        }
                        @Suppress("DEPRECATION") val handler = Handler()
                        handler.postDelayed(runnable, 2000)
                    } catch (e: Exception) {
                        Toast.makeText(
                            requireContext(),
                            "User hasn't been registered try again",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    progressBarDialog.dismiss()
                }
            }
    }
}


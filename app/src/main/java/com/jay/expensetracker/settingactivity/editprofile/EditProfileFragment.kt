package com.jay.expensetracker.settingactivity.editprofile

import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.TextUtils
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.jay.expensetracker.DatabaseClass
import com.jay.expensetracker.R
import com.jay.expensetracker.customlayout.ProgressBarDialog
import com.jay.expensetracker.databinding.EditProfileFragmentBinding
import com.jay.expensetracker.dataclass.UserDataClass
import com.jay.expensetracker.signinactivity.signup.SignUp

class EditProfileFragment : Fragment() {

    private lateinit var binding: EditProfileFragmentBinding

    private lateinit var progressBarDialog: ProgressBarDialog

    private lateinit var userDatabaseRef: DatabaseReference


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = DataBindingUtil.inflate(
            layoutInflater,
            R.layout.edit_profile_fragment,
            container,
            false
        )

        DatabaseClass.setUserUID()
        progressBarDialog = ProgressBarDialog()
        setUserData()
        binding.reloadButton.setOnClickListener {
            clickListenerOfReloadButton()
        }
        binding.editButton.setOnClickListener {
            clickListenerOfEditButton()
        }
        binding.saveButton.setOnClickListener {
            clickListenerOfSaveButton()
        }
        binding.discardButton.setOnClickListener {
            clickListenerOfDiscard()
        }
        binding.backButton.setOnClickListener {
            clickListenerOfBackButton()
        }
        return binding.root
    }

    //    function will navigate back to the settingfragment
    private fun clickListenerOfBackButton() {
        findNavController().navigate(EditProfileFragmentDirections.actionEditProfileFragmentToSettingFragment())
    }

    //  Function will make views InEditable
    private fun clickListenerOfDiscard() {
        makingEditTextInEditable()
    }

    //  Function will make views Editable and visibling RadioButton
    private fun clickListenerOfEditButton() {

//        Selecting the radio button based on previous stored data
        when (binding.defaultGender.text.toString()) {
            SignUp.MALE -> binding.maleRadioButton.isSelected = true
            SignUp.FEMALE -> binding.femaleRadioButton.isSelected = true
            else -> binding.transagenderButton.isSelected = true
        }
        makingEditTextEditable()
    }

    //  Function will set userData
    private fun clickListenerOfReloadButton() {
        setUserData()
    }

    //  Function will check the EditText if every details is okay than it'll update the data
    private fun clickListenerOfSaveButton() {
        val checkedId = binding.gender.checkedRadioButtonId
        if (!DatabaseClass.isInternetAvailable(requireContext())) {
            Toast.makeText(requireContext(), "Connect to the Internet", Toast.LENGTH_LONG).show()
        } else if (TextUtils.isEmpty(binding.firstName.text.toString())) {
            binding.firstName.requestFocus()
            binding.firstName.error = "Enter first name"
        } else if (TextUtils.isEmpty(binding.lastName.text.toString())) {
            binding.lastName.requestFocus()
            binding.lastName.error = "Enter last name"
        } else if (!Patterns.EMAIL_ADDRESS.matcher(binding.email.text.toString()).matches()) {
            binding.email.requestFocus()
            binding.email.error = "Enter email name"
        } else if (binding.mobileNo.text.toString().length != 10) {
            binding.mobileNo.requestFocus()
            binding.mobileNo.error = "Enter full Mobile Number"
        } else if (checkedId == -1) {
            Toast.makeText(requireContext(), "Select Gender", Toast.LENGTH_LONG).show()
        } else {
            updateData(
                binding.firstName.text.toString(),
                binding.lastName.text.toString(),
                binding.email.text.toString(),
                binding.mobileNo.text.toString(),
                checkedId
            )
        }

    }

    //  function will Update the data of user
    private fun updateData(
        firstName: String,
        lastName: String,
        email: String,
        mobileNo: String,
        checkedId: Int,
    ) {
        val gender = when (checkedId) {
            R.id.male_radio_button -> SignUp.MALE
            R.id.female_radio_button -> SignUp.FEMALE
            else -> SignUp.TRANSGENDER
        }
//        Setting map for the update the user data
        val map = HashMap<String, String>()
        map[UserDataClass.FIRST_NAME] = firstName
        map[UserDataClass.LAST_NAME] = lastName
        map[UserDataClass.EMAIL] = email
        map[UserDataClass.MOBILE_NO] = mobileNo
        map[UserDataClass.GENDER] = gender

        userDatabaseRef.updateChildren(map as Map<String, Any>).addOnCompleteListener {
            if (it.isSuccessful) {
                Toast.makeText(requireContext(), "Data Updated Successfully", Toast.LENGTH_SHORT)
                    .show()
                makingEditTextInEditable()
                setUserData()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Data hasn't Updated Try Again",
                    Toast.LENGTH_SHORT
                ).show()
            }

        }
    }

    //        Making EditText View editable
    private fun makingEditTextInEditable() {
//        Making editable Firstname,lastname , email,mobileNo
        binding.firstName.isCursorVisible = false
        binding.firstName.isFocusable = false
        binding.firstName.isLongClickable = false

        binding.lastName.isCursorVisible = false
        binding.lastName.isFocusable = false
        binding.lastName.isLongClickable = false

        binding.mobileNo.isCursorVisible = false
        binding.mobileNo.isFocusable = false
        binding.mobileNo.isLongClickable = false

        binding.email.isCursorVisible = false
        binding.email.isFocusable = false
        binding.email.isLongClickable = false

//        Removing View default gender layout and making visible Radio Group for gender
        binding.genderTextLayout.visibility = View.VISIBLE

//        Making visible save and discard Button
        binding.buttonLayout.visibility = View.GONE
        binding.editGenderLayout.visibility = View.GONE
    }

    //        Making EditText View editable
    private fun makingEditTextEditable() {

//        Making editable Firstname,lastname , email,mobileno
        binding.firstName.isCursorVisible = true
        binding.firstName.isFocusable = true
        binding.firstName.isFocusableInTouchMode = true
        binding.firstName.isLongClickable = true


        binding.lastName.isCursorVisible = true
        binding.lastName.isFocusable = true
        binding.lastName.isFocusableInTouchMode = true
        binding.lastName.isLongClickable = true

        binding.mobileNo.isCursorVisible = true
        binding.mobileNo.isFocusable = true
        binding.mobileNo.isFocusableInTouchMode = true
        binding.mobileNo.isLongClickable = true

        binding.email.isCursorVisible = true
        binding.email.isFocusable = true
        binding.email.isFocusableInTouchMode = true
        binding.email.isLongClickable = true

//        Removing View default gender layout and making visible Radio Group for gender
        binding.genderTextLayout.visibility = View.GONE

//        Making visible save and discard Button and gender radio group
        binding.buttonLayout.visibility = View.VISIBLE
        binding.editGenderLayout.visibility = View.VISIBLE
    }


    //    This function will set the user data on their textview
    private fun setUserData() {
//        Setting User Database reference to
        userDatabaseRef = DatabaseClass.getInstanceOfUserDatabase()
            .getReference(DatabaseClass.pathForUsersRegisteredData())

//        started progress bar
        progressBarDialog.show(childFragmentManager, "Progress Bar")

        //        Creating reference of Registered User Database
        if (DatabaseClass.isInternetAvailable(requireContext())) {
//        Retrieving the data from the reference
            userDatabaseRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val data = snapshot.getValue(UserDataClass::class.java)
//                if data is not null then showing the data on layout
                    if (data != null) {
                        binding.userData.visibility = View.VISIBLE
                        val fullName = "${data.firstName} ${data.lastName}"
                        binding.fullName.text = fullName
                        binding.firstName.text = SpannableStringBuilder(data.firstName)
                        binding.lastName.text = SpannableStringBuilder(data.lastName)
                        binding.email.text = SpannableStringBuilder(data.email)
                        binding.mobileNo.text = SpannableStringBuilder(data.mobileNo)
                        binding.defaultGender.text = SpannableStringBuilder(data.gender)
//                        making other views gone and user Data visible
                        binding.userData.visibility = View.VISIBLE
                        binding.reloadButton.visibility = View.GONE
                        binding.buttonLayout.visibility = View.GONE
                    } else {
                        onFailedSetUserData()
                    }
//                Dismissing progress bar
                    progressBarDialog.dismiss()
                }

                override fun onCancelled(error: DatabaseError) {
                    onFailedSetUserData()
                }

            })
        } else {
//                        Updating UI
            binding.reloadButton.visibility = View.VISIBLE
            binding.userData.visibility = View.GONE
            Toast.makeText(requireContext(), "Connect to the Internet", Toast.LENGTH_LONG).show()
        }
    }

    //                        Updating UI
    private fun onFailedSetUserData() {
        Toast.makeText(requireContext(), "Try Again", Toast.LENGTH_SHORT).show()
        binding.reloadButton.visibility = View.VISIBLE
        binding.userData.visibility = View.GONE
        binding.buttonLayout.visibility = View.GONE
    }
}
package com.jay.expensetracker.dashboardacitivity.profile

import android.content.Context
import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.jay.expensetracker.DatabaseClass
import com.jay.expensetracker.R
import com.jay.expensetracker.customlayout.ProgressBarDialog
import com.jay.expensetracker.dashboardacitivity.dashboard.Dashboard
import com.jay.expensetracker.databinding.ProfileFragmentBinding
import com.jay.expensetracker.dataclass.UserDataClass

class Profile : Fragment() {

    //    DataBinding of ProfileFragmentBinding
    private lateinit var binding: ProfileFragmentBinding

    //    ProgressBarDialog Fragment
    private val progressBarDialog = ProgressBarDialog()

    private lateinit var userDatabaseRef: DatabaseReference

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.profile_fragment, container, false)

        DatabaseClass.setUserUID()
        sharedPreferences = requireActivity().getSharedPreferences(
            Dashboard.SHARED_PREFERENCE_NAME,
            Context.MODE_PRIVATE
        )
        val editor = sharedPreferences.edit()
        val isDataSet = sharedPreferences.getBoolean(Dashboard.USER_DATA_IS_SET, false)
        if (isDataSet) {
            setDataOnTextView()
        } else if (DatabaseClass.isInternetAvailable(requireContext())) {
            //       Setting the user data on the textview respectively
            setUserData(editor)
        } else {
            binding.reloadButton.visibility = View.VISIBLE
        }

        binding.reloadButton.setOnClickListener {
            progressBarDialog.show(childFragmentManager, "ProgressBar")
            clickListenerOfReloadButton(editor)
        }
        return binding.root
    }

    private fun setDataOnTextView() {
        binding.name.text = sharedPreferences.getString(Dashboard.USER_NAME, "")
        binding.userFullname.text = sharedPreferences.getString(Dashboard.USER_NAME, "")
        binding.email.text = sharedPreferences.getString(Dashboard.USER_EMAIL, "")
        binding.mobileNo.text = sharedPreferences.getString(Dashboard.USER_MOBILE_NUMBER, "")
        binding.gender.text = sharedPreferences.getString(Dashboard.USER_GENDER, "")
    }

    //    Setting clickListenerFunction of views
    private fun clickListenerOfReloadButton(editor: Editor) {
        setUserData(editor)
    }

    //    This function will set the user data on their textview
    private fun setUserData(editor: Editor) {
//        started progress bar
        progressBarDialog.show(childFragmentManager, "Progress Bar")
        //        Creating reference of Registered User Database
        userDatabaseRef =
            DatabaseClass.getInstanceOfUserDatabase()
                .getReference(DatabaseClass.pathForUsersRegisteredData())

//        Retrieving the data from the reference
        userDatabaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val data = snapshot.getValue(UserDataClass::class.java)
//                if data is not null then showing the data on layout
                if (data != null) {
                    binding.infoLayout.visibility = View.VISIBLE

                    val name = data.firstName + " " + data.lastName
                    val email = data.email
                    val gender = data.gender
                    val number = data.mobileNo
                    editor.putString(Dashboard.USER_NAME, name)
                    editor.putString(Dashboard.USER_EMAIL, data.email)
                    editor.putString(Dashboard.USER_MOBILE_NUMBER, data.mobileNo)
                    editor.putString(Dashboard.USER_GENDER, data.gender)
                    editor.putBoolean(Dashboard.USER_DATA_IS_SET, true)
                    editor.apply()
                    binding.userFullname.text = name
                    binding.name.text = name
                    binding.email.text = email
                    binding.gender.text = gender
                    binding.mobileNo.text = number
                }
//                Dismissing progress bar
                progressBarDialog.dismiss()
            }

            override fun onCancelled(error: DatabaseError) {
                binding.reloadLayout.visibility = View.VISIBLE
                binding.text.visibility = View.GONE
            }

        })
    }

}
package com.jay.expensetracker.settingactivity.settingfragment

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.jay.expensetracker.DatabaseClass
import com.jay.expensetracker.ImageConvertor
import com.jay.expensetracker.MainActivity
import com.jay.expensetracker.R
import com.jay.expensetracker.customlayout.ConfirmationLayout
import com.jay.expensetracker.customlayout.ProgressBarDialog
import com.jay.expensetracker.dashboardacitivity.DashboardActivity
import com.jay.expensetracker.dashboardacitivity.dashboard.Dashboard
import com.jay.expensetracker.databinding.SettingFragmentBinding
import com.jay.expensetracker.dataclass.BalanceDataClass
import com.jay.expensetracker.dataclass.EntryDataClass
import com.jay.expensetracker.dataclass.NoOfEntriesDataClass
import com.jay.expensetracker.entryroomdatabase.EntryRoomDatabase
import com.jay.expensetracker.entryroomdatabase.EntryRoomDatabaseBuilder
import com.jay.expensetracker.entryroomdatabase.EntryRoomDatabaseDao
import com.jay.expensetracker.signinactivity.SignInAndSignUPActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SettingFragment : Fragment() {
    private lateinit var binding: SettingFragmentBinding

    private lateinit var confirmationDialog: ConfirmationLayout

    private lateinit var auth: FirebaseAuth

    private lateinit var entryDatabaseReference: DatabaseReference

    private lateinit var progressBarDialog: ProgressBarDialog

    private lateinit var viewModel: SettingFragmentViewModel

    private lateinit var dao: EntryRoomDatabaseDao

    companion object {
        const val DELETE_ACCOUNT = "Delete Account"
        const val SIGN_OUT = "Sign Out"
        const val RESTORE_DATA = "Restore Data"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.setting_fragment, container, false)
        init()
        return binding.root
    }

    //  Function will initialize viewModel and includes the clickListener of views
    private fun init() {
        DatabaseClass.setUserUID()
        dao = EntryRoomDatabaseBuilder.getInstance(requireContext()).getEntryDatabaseDao()

        val viewModelFactory = SettingFragmentViewModelFactory(requireActivity().application, dao)
        viewModel = ViewModelProvider(this, viewModelFactory)[SettingFragmentViewModel::class.java]
        progressBarDialog = ProgressBarDialog()
        binding.profileLayout.setOnClickListener {
            clickListenerOfProfile()
        }
//        binding.budgetLayout.setOnClickListener {
//            clickListenerOfBudget()
//
//        }
        binding.restoreLayout.setOnClickListener {
            clickListenerOfRestore()

        }
        binding.deleteAccountLayout.setOnClickListener {
            clickListenerOfDelete()

        }
        binding.signOutLayout.setOnClickListener {
            clickListenerOfSignOut()
        }
        binding.backButton.setOnClickListener {
            val intent = Intent(requireContext(), DashboardActivity::class.java)
            startActivity(intent)

        }
    }

    //    it'll navigate to the BudgettingFragment
//    private fun clickListenerOfBudget() {
//        findNavController().navigate(SettingFragmentDirections.actionSettingFragmentToBudgetingFragment())
//    }

    //    it'll inflate the ConfirmationLayout and invoke restore data if user click yes other wise it'll dismiss the ConfirmationLayout
    private fun clickListenerOfRestore() {
        confirmationDialog = ConfirmationLayout(requireContext(), RESTORE_DATA)
        confirmationDialog.show()
        confirmationDialog.setPositiveButtonClickListener {
            if (DatabaseClass.isInternetAvailable(requireContext())) {
                progressBarDialog.show(childFragmentManager, "ProgressBar")
                restoreData()
            } else {
                Toast.makeText(requireContext(), "Connect to the Internet", Toast.LENGTH_SHORT)
                    .show()
                confirmationDialog.dismiss()
            }
        }

        confirmationDialog.setNegativeButtonClickListener {
            confirmationDialog.dismiss()
        }

    }

    //   it'll get the RoomDatabase data of entries
    private fun restoreData() {
        getOfflineData()
    }

    //   Function will get the Entries and No Of Entries in RoomDatabase and invoke the getDiffrenceOfEntries() with noOfOfflineEntries and offlineEntries
    @SuppressLint("SuspiciousIndentation")
    private fun getOfflineData() {
        lifecycleScope.launch {
            val noOfOfflineEntries = viewModel.noOfOfflineEntries()
            getDifferenceOfEntries(noOfOfflineEntries)
        }
    }

    //   Function will take 1. noOfOfflineEntries and 2. offlineEntries as parameters. 1. No. of rows in entry roomdatabase 2. rows in RoomDatabase
//   Function will fetch the number of offlineEntries in Firebase realtime Database and compare with noOfOfflineEntries
//   if Difference is more than 0 than it'll invoke restoreEntries Function with difference and offlineEntries
//   else it'll toast message that data is up to date and in case database error we'll try again message
    private fun getDifferenceOfEntries(
        noOfOfflineEntries: Int,
    ) {
        entryDatabaseReference =
            DatabaseClass.getInstanceOfUserDatabase()
                .getReference(DatabaseClass.USER_NO_OF_ENTRIES_PATH())


        entryDatabaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val value = snapshot.getValue(NoOfEntriesDataClass::class.java)
                if (value != null) {
                    val differenceOfEntries = value.totalEntries?.minus(noOfOfflineEntries)
                    if (differenceOfEntries == 0) {
                        progressBarDialog.dismiss()
                        confirmationDialog.dismiss()
                        Toast.makeText(requireContext(), "Data is up to Date", Toast.LENGTH_LONG)
                            .show()
                    } else {
                        if (differenceOfEntries != null && differenceOfEntries > 0) {
                            restoreEntries(differenceOfEntries)
                        }
                    }
                } else {
                    Toast.makeText(
                        requireContext(),
                        "There's no Data is Stored",
                        Toast.LENGTH_SHORT
                    ).show()
                    confirmationDialog.dismiss()
                    progressBarDialog.dismiss()
                }

            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    requireContext(),
                    "Try Again after sometime",
                    Toast.LENGTH_SHORT
                ).show()
                confirmationDialog.dismiss()
                progressBarDialog.dismiss()
            }
        })
    }

    //  Function will take 1. difference and 2. offlineEntries as parameters. 1. The diffrence of entries in RoomDatabase and Firebase Realtime Database
//  2. rows of EntryRoomDatabase
//  Function will collect the Entries from Firebase database and store in onlineEntries. after that it'll invoke deleteEntries() and setEntries() Function and dismiss the customlayout and show message of successful transaction
//  in case of exception and onCancelled it'll dismiss customLayout and show the message of try again
    private fun restoreEntries(difference: Int) {
        val entriesReference = DatabaseClass.getInstanceOfUserDatabase()
            .getReference(DatabaseClass.USERS_ENTRYDATABASE_PATH())

        val temp = 1
        var entriesList: List<EntryDataClass>

        val query =
            entriesReference.orderByChild(DatabaseClass.entryNumberChild).startAt(temp.toDouble())
                .endAt(difference.toDouble())

        Log.i("start and end index", "Start $temp End $difference")
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val json = Gson().toJson(snapshot.value)

                try {
                    val listType = object : TypeToken<List<EntryDataClass>>() {}.type
                    entriesList = Gson().fromJson(json, listType)
                    val onlineEntries = ArrayList<EntryRoomDatabase>()

                    var number = 1
                    entriesList.forEach {
                        if (it != null) {
                            val imagePath = it.billImageBase64Code?.let { it1 ->
                                getImagePath(it1)
                            }
                            val temp = it.amount?.let { it2 ->
                                imagePath?.let { it1 ->
                                    it.entryNumber?.let { it3 ->
                                        EntryRoomDatabase(
                                            it3,
                                            it.transactionType,
                                            it.timeStamp,
                                            it2,
                                            it.date,
                                            it.category,
                                            it.note,
                                            it1
                                        )
                                    }
                                }
                            }
                            if (temp != null) {
                                onlineEntries.add(temp)
                                number++
                            }
                        }
                    }
                    lifecycleScope.launch {
                        withContext(Dispatchers.IO) {
                            setEntries(onlineEntries)
                            setData()
                        }
                    }
                    Toast.makeText(
                        requireContext(),
                        "Data Restored Successfully",
                        Toast.LENGTH_LONG
                    ).show()
                    confirmationDialog.dismiss()
                    progressBarDialog.dismiss()
                } catch (e: Exception) {
//                    If there's other error than showing message for try again
                    Log.i("Exception", "onDataChange: $e")
                    progressBarDialog.dismiss()
                    confirmationDialog.dismiss()
                    Toast.makeText(requireContext(), "Try Again", Toast.LENGTH_LONG).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                progressBarDialog.dismiss()
                confirmationDialog.dismiss()
                Toast.makeText(requireContext(), "Try Again", Toast.LENGTH_LONG).show()
            }

        })

    }

    //    This function takes base64Code of image as parameter
//    Function will decode the image and store in phone's storage and return the path of the image that it stored
    private fun getImagePath(base64Code: String?): String? {
        val bitMapOfBillImage = base64Code?.let { ImageConvertor.decodeBase64ToBitmap(it) }
        val imageUri = bitMapOfBillImage?.let {
            ImageConvertor.bitmapToImageUri(
                requireContext(),
                it
            )
        }
        val imageType = imageUri?.let { it1 ->
            ImageConvertor.getImageFormat(
                requireContext(),
                it1
            )
        }
        val imagePath = imageType?.let { it1 ->
            ImageConvertor.getUniqueImagePath(requireContext(), it1)
        }
        if (imageUri != null && imagePath != null) {
            ImageConvertor.handleImage(requireContext(), imageUri, imagePath)
        }
        return imagePath
    }

    //    This Function will take onlineEntries and offlineEntries as parameter. onlineEntries as stored in firebase Database, and OfflineEntries as in it was stored in Room Database
//    This will first insert the onlineEntries and after that it'll insert offlineEntries
    private fun setEntries(
        onlineEntries: ArrayList<EntryRoomDatabase>,
    ) {
        onlineEntries.forEach {
            viewModel.insertEntry(it)
        }
    }

    private fun setData() {
        val databaseRef =
            DatabaseClass.getInstanceOfUserDatabase().getReference(DatabaseClass.USERS_BALANCE_PATH())
        val sharedPreferences = requireActivity().getSharedPreferences(
            Dashboard.SHARED_PREFERENCE_NAME,
            Context.MODE_PRIVATE
        )
        val editor = sharedPreferences.edit()
        databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val data = snapshot.getValue(BalanceDataClass::class.java)
                if (data != null) {
                    data.currentBalance?.let { editor.putFloat(Dashboard.CURRENT_BALANCE, it) }
                    editor.apply()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                setData()
            }

        })
    }
//    it'll inflate the ConfirmationLayout and invoke deleterUser() if user click yes other wise it'll dismiss the ConfirmationLayout

    private fun clickListenerOfDelete() {
        confirmationDialog = ConfirmationLayout(requireContext(), DELETE_ACCOUNT)
        confirmationDialog.show()
        confirmationDialog.setPositiveButtonClickListener {
            if (!DatabaseClass.isInternetAvailable(requireContext())) {
                progressBarDialog.show(childFragmentManager, "ProgressBar")
                deleteAccount()
            } else {
                Toast.makeText(requireContext(), "Connect to the Internet", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        confirmationDialog.setNegativeButtonClickListener {
            confirmationDialog.dismiss()
        }

    }

    //  This function will delete the account that's been logged in
//  and also it'll remove the data of the user. if that's unsuccessful than it'll say contact us for deleting the data
    private fun deleteAccount() {
        auth = DatabaseClass.getInstanceOfFirebaseAuth()
        val path = auth.currentUser?.uid.toString()
        entryDatabaseReference = DatabaseClass.getInstanceOfEntriesDatabase().getReference(path)
        if (DatabaseClass.isInternetAvailable(requireContext())) {
//            Deleting the account of current user
            auth.currentUser?.delete()?.addOnCompleteListener {
                if (it.isSuccessful) {
//                    Removing the data of the user from realtime database
                    deleteAccountSuccessful(entryDatabaseReference)
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Account hasn't Deleted Try Again",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            progressBarDialog.dismiss()
            confirmationDialog.dismiss()
        } else {
            Toast.makeText(requireContext(), "Connect to The Internet", Toast.LENGTH_LONG)
                .show()
            progressBarDialog.dismiss()
            confirmationDialog.dismiss()
        }
    }

    private fun deleteAccountSuccessful(entryDatabaseReference: DatabaseReference) {
        entryDatabaseReference.removeValue()
            .addOnCompleteListener { it2 ->
                if (it2.isSuccessful) {
                    Toast.makeText(
                        requireContext(),
                        "Account Deleted Successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                    lifecycleScope.launch {
                        withContext(Dispatchers.IO) {
                            deleteEntries()
                            deleteAllData()
                        }
                    }
                    val intent = Intent(requireContext(), SignInAndSignUPActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)


                } else {
                    Toast.makeText(
                        requireContext(),
                        "Account is deleted.\n Contact us for Deleting Data",
                        Toast.LENGTH_SHORT
                    ).show()
                    lifecycleScope.launch {
                        withContext(Dispatchers.IO) {
                            deleteEntries()
                            deleteAllData()
                        }
                    }
                    val intent = Intent(requireContext(), SignInAndSignUPActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)

                }
            }
    }

    private fun deleteAllData() {
        val sharedPreferences = requireActivity().getSharedPreferences(
            Dashboard.SHARED_PREFERENCE_NAME,
            Context.MODE_PRIVATE
        )
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()
    }

    private fun deleteEntries() {
        viewModel.deleteAllEntries()
    }
//    it'll inflate the ConfirmationLayout and invoke signOutUser() if user click yes other wise it'll dismiss the ConfirmationLayout

    private fun clickListenerOfSignOut() {
        confirmationDialog = ConfirmationLayout(requireContext(), SIGN_OUT)
        confirmationDialog.show()
        confirmationDialog.setPositiveButtonClickListener {
            progressBarDialog.show(childFragmentManager, "ProgressBar")
            signOutUser()
        }

        confirmationDialog.setNegativeButtonClickListener {
            confirmationDialog.dismiss()
        }

    }

    //    this function will signout current user and redirect to the SignInAndSignUpActivity and if that fails than it'll show the message of try again
    private fun signOutUser() {
        auth = DatabaseClass.getInstanceOfFirebaseAuth()
        if (DatabaseClass.isInternetAvailable(requireContext())) {
            auth.signOut()
            if (auth.currentUser == null) {
                Toast.makeText(requireContext(), "Logged Out Successfully", Toast.LENGTH_SHORT)
                    .show()
                lifecycleScope.launch {
                    withContext(Dispatchers.IO) {
                        deleteEntries()
                        deleteAllData()
                    }
                }
                val intent = Intent(requireContext(), SignInAndSignUPActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)

            } else {
                Toast.makeText(
                    requireContext(),
                    "Sign out Failed.\nTry Again.",
                    Toast.LENGTH_SHORT
                ).show()
            }
            progressBarDialog.dismiss()
            confirmationDialog.dismiss()
        } else {
            Toast.makeText(requireContext(), "Connect to The Internet", Toast.LENGTH_LONG)
                .show()
            progressBarDialog.dismiss()
            confirmationDialog.dismiss()
        }
    }

    //  this function will navigate to the profile fragment
    private fun clickListenerOfProfile() {
        findNavController().navigate(SettingFragmentDirections.actionSettingFragmentToEditProfileFragment())
    }
}
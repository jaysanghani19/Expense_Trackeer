package com.jay.expensetracker.dashboardacitivity.dashboard

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.jay.expensetracker.DatabaseClass
import com.jay.expensetracker.OfflineEntriesRecyclerViewAdapter
import com.jay.expensetracker.R
import com.jay.expensetracker.addactivity.AddActivity
import com.jay.expensetracker.customlayout.EntryShowCustomLayout
import com.jay.expensetracker.customlayout.ProgressBarDialog
import com.jay.expensetracker.dashboardacitivity.profile.Profile
import com.jay.expensetracker.databinding.DashboardFragmentBinding
import com.jay.expensetracker.dataclass.BalanceDataClass
import com.jay.expensetracker.dataclass.EntryDataClass
import com.jay.expensetracker.dataclass.NoOfEntriesDataClass
import com.jay.expensetracker.dataclass.UserDataClass
import com.jay.expensetracker.entryroomdatabase.EntryRoomDatabase
import com.jay.expensetracker.entryroomdatabase.EntryRoomDatabaseBuilder
import com.jay.expensetracker.settingactivity.SettingActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class Dashboard : Fragment(), EntryShowCustomLayout.DialogListener {
    private lateinit var binding: DashboardFragmentBinding

    private lateinit var userDatabaseRef: DatabaseReference

    private lateinit var viewModel: DashboardFragmentViewModel

    private lateinit var sharedPreferences: SharedPreferences

    private lateinit var editor: SharedPreferences.Editor

    private var selectedEntry = EntryRoomDatabase(0, "", "", 0f, "", "", "", "")

    private lateinit var customDialog: EntryShowCustomLayout

    private val progressBarDialog = ProgressBarDialog()

    companion object {
        //        Keys and name for shared preference
        const val USER_NAME = "Full Name Of User"
        const val USER_EMAIL = "Email Of User"
        const val USER_GENDER = "Gender Of User"
        const val USER_MOBILE_NUMBER = "Mobile Number Of User"
        const val USER_DATA_IS_SET = "User Data is Set Or Not"

        const val CURRENT_BALANCE = "Current Balance"

        const val SHARED_PREFERENCE_NAME = "User Basic Data"

        //        Key For AddActivity for bundle
        const val ENTRY_NUMBER = "Entry Number"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.dashboard_fragment, container, false)

        binding.info.setBackgroundResource(R.drawable.circular_10_counter_white_blue)
        // Create an instance of ProgressBarDialog

        //                Initializing Recycler View layout
        binding.entriesRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        DatabaseClass.setUserUID()

        sharedPreferences = requireActivity().getSharedPreferences(
            SHARED_PREFERENCE_NAME,
            Context.MODE_PRIVATE
        )
        editor = sharedPreferences.edit()

        val dao = EntryRoomDatabaseBuilder.getInstance(requireContext()).getEntryDatabaseDao()
        val viewModelFactory = DashboardViewModelProvider(requireActivity().application, dao)
        viewModel =
            ViewModelProvider(this, viewModelFactory)[DashboardFragmentViewModel::class.java]

        val ifUserDataIsStored = sharedPreferences.getBoolean(USER_DATA_IS_SET, false)
        if (ifUserDataIsStored) {
            val fullName = sharedPreferences.getString(USER_NAME, " ")
            binding.name.text = fullName
        } else {
            setUserFullName(editor)
        }
        setBalance()

//        it'll set the full name of user in textview name

//        setting up entriesRecyclerView
        binding.entriesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        viewModel.entries.observe(viewLifecycleOwner) { it ->
            binding.entriesRecyclerView.adapter = OfflineEntriesRecyclerViewAdapter(it) { it1 ->
                clickListenerOnEntry(it1)
                selectedEntry = it1
            }
            binding.transactionsLayout.visibility = View.VISIBLE
            setIncomingOutcomingAmount(it)
        }

        binding.addButton.setOnClickListener {
            clickListenerOfAddButton()
        }

        binding.settingButton.setOnClickListener {
            clickListenerOfSettingButton()
        }
        binding.profileButton.setOnClickListener {
            val fragment = Profile()
            replaceFragment(fragment)
        }
        return binding.root
    }

    //    Methods for EntryShowCustomDialog
    override fun onDeleteButtonClick() {
        deleteEntryInFirebase()
    }

    override fun onEditButtonClick() {
        customDialog.dismiss()
        if (selectedEntry.entryNumber != 0) {
            val intent = Intent(requireContext(), AddActivity::class.java)
            intent.putExtra(ENTRY_NUMBER, selectedEntry.entryNumber)
            startActivity(intent)
        } else {
            Toast.makeText(requireContext(), "Try Again", Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteEntryInFirebase() {
        val databaseRef = DatabaseClass.getInstanceOfUserDatabase()
            .getReference(DatabaseClass.USERS_ENTRYDATABASE_PATH())
            .child(selectedEntry.timeStamp)

        if (DatabaseClass.isInternetAvailable(requireContext())) {
            progressBarDialog.show(childFragmentManager, "ProgressBar")
            databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val data = snapshot.getValue(EntryDataClass::class.java)
                    if (data != null) {
                        databaseRef.removeValue().addOnCompleteListener {
                            if (it.isSuccessful) {
                                viewModel.deleteEntry(selectedEntry)
                                data.entryNumber?.let { it1 ->
                                    refactorEntries(it1)
                                }
                                data.amount?.let { it1 ->
                                    updateOnlineOfflineBalance(
                                        data.transactionType,
                                        it1
                                    )
                                }
                                binding.totalBalanceAmount.text =
                                    sharedPreferences.getFloat(CURRENT_BALANCE, 0f)
                                        .toString()
                            } else {
                                deleteEntryInFirebase()
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    progressBarDialog.dismiss()
                    customDialog.dismiss()
                    Toast.makeText(
                        requireContext(),
                        "Entry hasn't Deleted.\nTry Again",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            })

        } else {
            progressBarDialog.dismiss()
            customDialog.dismiss()
            Toast.makeText(requireContext(), "Connect to the Internet", Toast.LENGTH_SHORT).show()
        }
    }

    private fun refactorEntries(entryNumber: Int) {
        lifecycleScope.launch {
            val timeStamp = viewModel.getTimeStamp(entryNumber)
            var tempEntryNumber = entryNumber
            timeStamp.forEach {
                refactorEntryNumber(it, tempEntryNumber)
                viewModel.updateEntryNumber(tempEntryNumber, it)
                tempEntryNumber++
            }
            reduceEntryNumber()
            withContext(Dispatchers.Main) {
                progressBarDialog.dismiss()
                customDialog.dismiss()
            }
        }
    }

    private fun refactorEntryNumber(timeStamp: String, entryNumber: Int) {
        val map = mapOf(
            EntryDataClass.ENTRY_NUMBER to entryNumber
        )
        val ref = DatabaseClass.getInstanceOfUserDatabase()
            .getReference(DatabaseClass.USERS_ENTRYDATABASE_PATH()).child(timeStamp)
        ref.updateChildren(map).addOnCompleteListener {
            if (!it.isSuccessful) {
                refactorEntryNumber(timeStamp, entryNumber)
            }
        }
    }

    private fun reduceEntryNumber() {
        val ref = DatabaseClass.getInstanceOfUserDatabase()
            .getReference(DatabaseClass.USER_NO_OF_ENTRIES_PATH())
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val data = snapshot.getValue(NoOfEntriesDataClass::class.java)
                    val entryNumber = data?.totalEntries
                    val map = mapOf(NoOfEntriesDataClass.TOTAL_ENTRIES to entryNumber?.minus(1))
                    updateTotalEntries(map)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    private fun updateTotalEntries(map: Map<String, Int?>) {
        val ref = DatabaseClass.getInstanceOfUserDatabase()
            .getReference(DatabaseClass.USER_NO_OF_ENTRIES_PATH())
        ref.updateChildren(map).addOnCompleteListener {
            if (!it.isSuccessful) {
                updateTotalEntries(map)
            }
        }
    }

    private fun updateOnlineOfflineBalance(transactionType: String, amount: Float) {
        val ref =
            DatabaseClass.getInstanceOfUserDatabase().getReference(DatabaseClass.USERS_BALANCE_PATH())
        val offlineBalance = sharedPreferences.getFloat(CURRENT_BALANCE, 0f)
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val data = snapshot.getValue(BalanceDataClass::class.java)
                if (data != null) {
                    val onlineBalance = data.currentBalance
                    if (transactionType == EntryRoomDatabase.CREDIT_TRANSACTION) {
                        val updatedOfflineBalance = offlineBalance - amount
                        val updatedOnlineBalance = onlineBalance?.minus(amount)
                        if (updatedOnlineBalance != null) {
                            updateOnlineBalance(updatedOnlineBalance) {
                                handleUpdateBalance(it, updatedOnlineBalance, updatedOfflineBalance)
                            }
                        }
                    } else {
                        val updatedOfflineBalance = offlineBalance + amount
                        val updatedOnlineBalance = onlineBalance?.plus(amount)
                        if (updatedOnlineBalance != null) {
                            updateOnlineBalance(updatedOnlineBalance) {
                                handleUpdateBalance(it, updatedOnlineBalance, updatedOfflineBalance)
                            }
                        }
                    }

                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    private fun handleUpdateBalance(boolean: Boolean, onlineBalance: Float, offlineBalance: Float) {
        if (!boolean) {
            updateOnlineBalance(onlineBalance) {
                if (!it) handleUpdateBalance(false, onlineBalance, offlineBalance)
            }
            updateOfflineBalance(offlineBalance)
        } else {
            updateOfflineBalance(offlineBalance)
        }
    }

    //    This function will update the Current Balance in Firebase Database
    private fun updateOnlineBalance(updatedBalance: Float, callback: (Boolean) -> Unit) {
        val updatedBalanceMap = mapOf(
            BalanceDataClass.CURRENT_BALANCE to updatedBalance
        )
        val databaseRef =
            DatabaseClass.getInstanceOfUserDatabase().getReference(DatabaseClass.USERS_BALANCE_PATH())
        databaseRef.updateChildren(updatedBalanceMap).addOnCompleteListener { task ->
            callback(task.isSuccessful)
        }
    }

    //  This Function will update balance in SharedPreference
    private fun updateOfflineBalance(updatedBalance: Float) {
        editor.putFloat(CURRENT_BALANCE, updatedBalance)
        editor.apply()
    }

    private fun clickListenerOnEntry(entry: EntryRoomDatabase) {
        customDialog = EntryShowCustomLayout(entry)
        customDialog.show(childFragmentManager, "Entry Dialog")
        customDialog.setDialogListener(this)
    }

    //    This will set the outcoming income and incoming income
    private fun setIncomingOutcomingAmount(entries: List<EntryRoomDatabase>) {
        var incoming = 0f
        var outcoming = 0f

        entries.forEach {
            if (it.transactionType == EntryRoomDatabase.CREDIT_TRANSACTION) {
                incoming += it.amount
            } else outcoming += it.amount
        }
        binding.outcomingIncome.text = "$ $outcoming"
        binding.incomingAmount.text = "$ $incoming"

    }

    private fun setBalance() {
        val curBalance = sharedPreferences.getFloat(CURRENT_BALANCE, 0f)
        binding.totalBalanceAmount.text = "$ $curBalance"
    }

    //    This function will start SettingActivity
    private fun clickListenerOfSettingButton() {
        val intent = Intent(requireContext(), SettingActivity::class.java)
        startActivity(intent)
    }

    //    This function will start AddActivity
    private fun clickListenerOfAddButton() {
        val intent = Intent(requireContext(), AddActivity::class.java)
        startActivity(intent)
    }

    //    This Function will set the full name of the user to textview(name)
    private fun setUserFullName(editor: SharedPreferences.Editor) {
//        Creating Registered User database's reference for retrieving the data
        userDatabaseRef =
            DatabaseClass.getInstanceOfUserDatabase()
                .getReference(DatabaseClass.pathForUsersRegisteredData())

//        Retrieving the data from the reference
        userDatabaseRef.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
//                after retrieving the data we'll display on the textview (name)
                val data = snapshot.getValue(UserDataClass::class.java)
                if (data != null) {
                    val fullName = data.firstName + " " + data.lastName
                    editor.putString(USER_NAME, fullName)
                    editor.putString(USER_EMAIL, data.email)
                    editor.putString(USER_MOBILE_NUMBER, data.mobileNo)
                    editor.putString(USER_GENDER, data.gender)
                    editor.putBoolean(USER_DATA_IS_SET, true)
                    editor.apply()
                    binding.name.text = fullName
                }
                binding.gmMessage.visibility = View.VISIBLE
            }

            override fun onCancelled(error: DatabaseError) {
                if (DatabaseClass.isInternetAvailable(requireContext())) {
                    setUserFullName(editor)
                } else {
                    Toast.makeText(requireContext(), "Connect the Internet", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        })
    }

    private fun replaceFragment(fragment: Fragment) {
        activity?.supportFragmentManager?.beginTransaction()?.apply {
            replace(R.id.frame_layout, fragment)
            addToBackStack(null)
            commit()
        }
    }

}
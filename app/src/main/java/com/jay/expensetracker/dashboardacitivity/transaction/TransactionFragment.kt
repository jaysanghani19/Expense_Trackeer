package com.jay.expensetracker.dashboardacitivity.transaction

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
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
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.jay.expensetracker.DatabaseClass
import com.jay.expensetracker.OfflineEntriesRecyclerViewAdapter
import com.jay.expensetracker.R
import com.jay.expensetracker.addactivity.AddActivity
import com.jay.expensetracker.customlayout.EntryShowCustomLayout
import com.jay.expensetracker.customlayout.ProgressBarDialog
import com.jay.expensetracker.dashboardacitivity.dashboard.Dashboard
import com.jay.expensetracker.databinding.TransactionFragmentBinding
import com.jay.expensetracker.dataclass.BalanceDataClass
import com.jay.expensetracker.dataclass.EntryDataClass
import com.jay.expensetracker.dataclass.NoOfEntriesDataClass
import com.jay.expensetracker.entryroomdatabase.EntryRoomDatabase
import com.jay.expensetracker.entryroomdatabase.EntryRoomDatabaseBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date

class TransactionFragment : Fragment(), EntryShowCustomLayout.DialogListener {
    //   DataBinding for TransactionFragment
    private lateinit var binding: TransactionFragmentBinding

    //    MaterialDatePicker Dialog for choosing DateRange
    private lateinit var datePickerDialog: MaterialDatePicker.Builder<androidx.core.util.Pair<Long, Long>>

    //    ProgressBarDialog for progressBar
    private val progressBarDialog = ProgressBarDialog()

    private lateinit var sharedPreferences: SharedPreferences

    private lateinit var editor: SharedPreferences.Editor

    //    Variable for dates
    private lateinit var fromDate: String
    private lateinit var toDate: String

    private lateinit var viewModel: TransactionFragmentViewModel

    //    DateFormat for formatting dates
    @SuppressLint("SimpleDateFormat")
    private val dateFormat = SimpleDateFormat("yyyy/MM/dd")

    private lateinit var customDialog: EntryShowCustomLayout

    private var selectedEntry = EntryRoomDatabase(0, "", "", 0f, "", "", "", "")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {

        binding = DataBindingUtil.inflate(inflater, R.layout.transaction_fragment, container, false)

        val dao = EntryRoomDatabaseBuilder.getInstance(requireContext()).getEntryDatabaseDao()
        val viewModelFactory =
            TransactionFragmentViewModelFactory(requireActivity().application, dao)
        viewModel =
            ViewModelProvider(this, viewModelFactory)[TransactionFragmentViewModel::class.java]
//        Initializing recyclerView
        binding.transactionsRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        sharedPreferences = requireActivity().getSharedPreferences(
            Dashboard.SHARED_PREFERENCE_NAME,
            Context.MODE_PRIVATE
        )
        editor = sharedPreferences.edit()
//        ClickListener of Views
        binding.dateRangeIcon.setOnClickListener {
            clickListenerOfDateView()
        }
        binding.fromToDate.setOnClickListener {
            clickListenerOfDateView()
        }
        binding.btnApply.setOnClickListener {
            clickListenerOfApplyButton()
        }
        return binding.root
    }

    //    This function will create datePickerDialog and let user choose the dates
    private fun clickListenerOfDateView() {
        datePickerDialog = MaterialDatePicker.Builder.dateRangePicker()
        datePickerDialog.setTitleText("Select Date Range")
        val datePicker = datePickerDialog.build()
        datePicker.addOnPositiveButtonClickListener {
            val fromDate = it.first
            val toDate = it.second

//            Setting dates in textview
            this.fromDate = dateFormat.format(Date(fromDate))
            this.toDate = dateFormat.format(Date(toDate))
            val fromToDate = getString(R.string.from_to_date, this.fromDate, this.toDate)
            binding.fromToDate.text = fromToDate
        }
        datePicker.show(childFragmentManager, "DatePicker")
    }

    //  function will check that user has choosen the date or not
//  if yes than it'll set data on recyclerView else it'll show the message for selecting date
    private fun clickListenerOfApplyButton() {
        progressBarDialog.show(childFragmentManager, "ProgressBar")
        if (binding.fromToDate.text.toString() == getString(R.string.select_range)) {
            Toast.makeText(requireContext(), "Select the Date range", Toast.LENGTH_LONG).show()
            binding.fromToDate.requestFocus()
            progressBarDialog.dismiss()
        } else {
            setDataInRecyclerView()
        }

    }

    private fun clickListenerOnEntry(entry: EntryRoomDatabase) {
        customDialog = EntryShowCustomLayout(entry)
        customDialog.show(childFragmentManager, "Entry Dialog")
        customDialog.setDialogListener(this)
    }

    //    This function will retrieve the Entries between the dates entered by user
//    And After that it'll reflect entries in RecyclerView
//    if there's no entry than it'll show the No Entries
    private fun setDataInRecyclerView() {
        val entries = viewModel.getEntriesBetweenDates(fromDate, toDate)
        entries.observe(viewLifecycleOwner) { it1 ->
            if (it1.isNotEmpty()) {
                binding.noEntries.visibility = View.GONE
                binding.transactionsLayout.visibility = View.VISIBLE
                binding.transactionsRecyclerView.adapter =
                    OfflineEntriesRecyclerViewAdapter(it1) { it2 ->
                        clickListenerOnEntry(it2)
                        selectedEntry = it2
                    }
                progressBarDialog.dismiss()
            } else {
                binding.noEntries.visibility = View.VISIBLE
                progressBarDialog.dismiss()
            }
        }
    }

    //    Methods for EntryShowCustomDialog
    override fun onDeleteButtonClick() {
        Log.i("Delete", "onDeleteButtonClick: ")
        deleteEntryInFirebase()
    }

    override fun onEditButtonClick() {
        customDialog.dismiss()
        Log.i("Delete", "onEdit ")

        if (selectedEntry.entryNumber != 0) {
            val intent = Intent(requireContext(), AddActivity::class.java)
            intent.putExtra(Dashboard.ENTRY_NUMBER, selectedEntry.entryNumber)
            startActivity(intent)
        }
//        else {
//            Toast.makeText(requireContext(), "Try Again", Toast.LENGTH_SHORT).show()
//        }
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
        val offlineBalance = sharedPreferences.getFloat(Dashboard.CURRENT_BALANCE, 0f)
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
        editor.putFloat(Dashboard.CURRENT_BALANCE, updatedBalance)
        editor.apply()
    }
}
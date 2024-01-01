package com.jay.expensetracker.dashboardacitivity.transaction

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.jay.expensetracker.entryroomdatabase.EntryRoomDatabaseDao

class TransactionFragmentViewModelFactory(
    private val application: Application,
    private val dao: EntryRoomDatabaseDao,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        if (modelClass.isAssignableFrom(TransactionFragmentViewModel::class.java)) {
            return TransactionFragmentViewModel(application, dao) as T
        }
        throw IllegalArgumentException("Illegal Class")
    }
}
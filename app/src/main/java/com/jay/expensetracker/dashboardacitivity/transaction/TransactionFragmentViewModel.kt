package com.jay.expensetracker.dashboardacitivity.transaction

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.jay.expensetracker.entryroomdatabase.EntryRoomDatabase
import com.jay.expensetracker.entryroomdatabase.EntryRoomDatabaseDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TransactionFragmentViewModel(
    application: Application,
    private val dao: EntryRoomDatabaseDao,
) : AndroidViewModel(application) {
    //    This function will get rows between fromDate and toDate
    fun getEntriesBetweenDates(fromDate: String, toDate: String) =
        dao.getEntriesBetweenDates(fromDate, toDate)

    fun updateEntryNumber(entryNumber: Int, timeStamp: String) {
        viewModelScope.launch {
            dao.updateEntryNumber(entryNumber, timeStamp)
        }
    }

    suspend fun getTimeStamp(entryNumber: Int): List<String> {
        return withContext(Dispatchers.IO) {
            dao.getTimeStamp(entryNumber)
        }
    }

    fun deleteEntry(entry: EntryRoomDatabase) {
        viewModelScope.launch {
            dao.deleteEntry(entry)
        }
    }
}
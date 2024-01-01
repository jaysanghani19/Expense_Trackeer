package com.jay.expensetracker.dashboardacitivity.dashboard

import android.app.Application
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.jay.expensetracker.entryroomdatabase.EntryRoomDatabase
import com.jay.expensetracker.entryroomdatabase.EntryRoomDatabaseDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date

class DashboardFragmentViewModel(application: Application, val dao: EntryRoomDatabaseDao) :
    AndroidViewModel(application) {

    //    entries is today's entries
    val dateFormat = SimpleDateFormat("yyyy/MM/dd")
    val todayDate = Date()

    val entries = dao.getAllEntry()

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
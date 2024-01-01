package com.jay.expensetracker.settingactivity.settingfragment

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.jay.expensetracker.entryroomdatabase.EntryRoomDatabase
import com.jay.expensetracker.entryroomdatabase.EntryRoomDatabaseDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SettingFragmentViewModel(application: Application, private val dao: EntryRoomDatabaseDao) :
    AndroidViewModel(application) {

    //    This function will invoke dao.insertEntry()
    fun insertEntry(entry: EntryRoomDatabase) =
        viewModelScope.launch {
            dao.insertEntry(entry)
        }


    //    This function will invoke dao.gerRowCount()
    suspend fun noOfOfflineEntries(): Int {
        return withContext(Dispatchers.IO) {
            dao.getRowCount()
        }
    }

    //    This function will invoke dao.deleteAllEntry()
    fun deleteAllEntries() {
        dao.deleteallEntries()
    }

    //    All rows in RoomDatabase
    val entries = dao.getAllEntry()

}
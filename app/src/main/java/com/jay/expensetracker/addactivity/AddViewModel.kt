package com.jay.expensetracker.addactivity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jay.expensetracker.entryroomdatabase.EntryRoomDatabase
import com.jay.expensetracker.entryroomdatabase.EntryRoomDatabaseDao
import kotlinx.coroutines.launch

class AddViewModel(val dao: EntryRoomDatabaseDao) : ViewModel() {
    //  This function will insert the entry
    fun insertData(entry: EntryRoomDatabase) = viewModelScope.launch {
        dao.insertEntry(entry)
    }

    fun getEntryWithEntryNumber(entryNumber: Int): EntryRoomDatabase {
        return dao.getEntryWithEntryNumber(entryNumber)
    }

    fun updateEntry(entry: EntryRoomDatabase) = viewModelScope.launch {
        dao.updateEntry(entry)
    }

    fun deleteEntry(entryNumber: Int) = viewModelScope.launch {
        dao.deleteSpecificEntry(entryNumber)
    }
}
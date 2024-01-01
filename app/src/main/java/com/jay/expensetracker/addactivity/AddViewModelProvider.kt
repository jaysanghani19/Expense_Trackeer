package com.jay.expensetracker.addactivity

import androidx.lifecycle.ViewModelProvider
import com.jay.expensetracker.entryroomdatabase.EntryRoomDatabaseDao

class AddViewModelProvider(val dao: EntryRoomDatabaseDao) : ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddViewModel::class.java)) {
            return AddViewModel(dao) as T
        }
        throw IllegalArgumentException("Illegal Class")
    }
}
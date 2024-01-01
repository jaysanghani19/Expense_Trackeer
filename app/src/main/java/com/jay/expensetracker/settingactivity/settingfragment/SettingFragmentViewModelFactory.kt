package com.jay.expensetracker.settingactivity.settingfragment

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.jay.expensetracker.entryroomdatabase.EntryRoomDatabaseDao

class SettingFragmentViewModelFactory(
    private val application: Application,
    private val dao: EntryRoomDatabaseDao,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingFragmentViewModel::class.java)) {
            return SettingFragmentViewModel(application, dao) as T
        }
        throw IllegalArgumentException("Illegal Class")
    }
}
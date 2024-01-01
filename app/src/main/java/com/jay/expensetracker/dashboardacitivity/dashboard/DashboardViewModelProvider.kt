package com.jay.expensetracker.dashboardacitivity.dashboard

import android.app.Application
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.jay.expensetracker.entryroomdatabase.EntryRoomDatabaseDao

class DashboardViewModelProvider(
    private val application: Application,
    private val dao: EntryRoomDatabaseDao,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DashboardFragmentViewModel::class.java)) {
            return DashboardFragmentViewModel(application, dao) as T
        }
        throw IllegalArgumentException("Illegal Class")
    }
}
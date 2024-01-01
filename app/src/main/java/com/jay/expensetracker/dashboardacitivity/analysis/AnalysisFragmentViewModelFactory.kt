package com.jay.expensetracker.dashboardacitivity.analysis

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.jay.expensetracker.entryroomdatabase.EntryRoomDatabaseDao

class AnalysisFragmentViewModelFactory(
    val application: Application,
    val dao: EntryRoomDatabaseDao,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AnalysisFragmentViewModel::class.java)) {
            return AnalysisFragmentViewModel(application, dao) as T
        }
        throw IllegalArgumentException("Illegal Class")
    }
}
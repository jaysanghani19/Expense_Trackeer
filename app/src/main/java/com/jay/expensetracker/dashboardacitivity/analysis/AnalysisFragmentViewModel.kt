package com.jay.expensetracker.dashboardacitivity.analysis

import android.app.Application
import android.content.Context
import android.widget.ArrayAdapter
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.jay.expensetracker.entryroomdatabase.EntryRoomDatabase
import com.jay.expensetracker.entryroomdatabase.EntryRoomDatabaseDao
import java.time.LocalDate

class AnalysisFragmentViewModel(application: Application, val dao: EntryRoomDatabaseDao) :
    AndroidViewModel(application) {

    private val _entries = MutableLiveData<List<EntryRoomDatabase>>()
    val entries: LiveData<List<EntryRoomDatabase>>
        get() = _entries

    //    Array Adapter for both dropdown menu
    private val _arrayAdapterOfTimeFrame = MutableLiveData<ArrayAdapter<String>>()
    val arrayAdapterOfTimeFrame: LiveData<ArrayAdapter<String>>
        get() = _arrayAdapterOfTimeFrame

    private val _arrayAdapterOfTime = MutableLiveData<ArrayAdapter<String>>()
    val arrayAdapterOfTime: LiveData<ArrayAdapter<String>>
        get() = _arrayAdapterOfTime


    private val timeFrameArray = arrayOf("Monthly", "Yearly", "Custom")
    private val monthlyArray = arrayOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )

    private var selectedTime = ""
    private var selectedMonth = ""
    private var selectedYear = ""


    private val currentYear = LocalDate.now().year

    //    This function will set the arrayAdapter for the timeFrame
    fun setTimeFrame(context: Context) {
        _arrayAdapterOfTimeFrame.value = ArrayAdapter(
            context,
            androidx.transition.R.layout.support_simple_spinner_dropdown_item,
            timeFrameArray
        )
    }

    //    This function will set month or year according to the selectedTimeFrame and set the array for ArrayAdapter
    fun setMonthYear(selectedTimeFrame: String, monthOrYear: String) {
        when (selectedTimeFrame) {
            timeFrameArray[0] -> selectedMonth = monthOrYear
            timeFrameArray[1] -> selectedYear = monthOrYear
            else -> selectedTime = " "
        }
    }

    //    This function will set the array Adapter for timeDropDownMenu according to timeFrame
    fun setTime(context: Context, timeFrame: String) {

        when (timeFrame) {
//                    If User Select Monthly than we'll initializing time dropdown menu with months
            timeFrameArray[0] -> {
//                this@AnalysisFragment.binding.fromToDate.visibility = View.GONE
                _arrayAdapterOfTime.value = ArrayAdapter(
                    context,
                    androidx.transition.R.layout.support_simple_spinner_dropdown_item,
                    monthlyArray
                )
            }
//                    If User Select Years than we'll initializing time dropdown menu with current year and previous 2 years
            timeFrameArray[1] -> {
//                this@AnalysisFragment.binding.fromToDate.visibility = View.GONE
                val yearlyArray = arrayOf(
                    (currentYear - 2).toString(),
                    (currentYear - 1).toString(),
                    currentYear.toString()
                )
                _arrayAdapterOfTime.value = ArrayAdapter(
                    context,
                    androidx.transition.R.layout.support_simple_spinner_dropdown_item,
                    yearlyArray
                )
            }
        }
    }

    //    This will get the entries between the dates
    fun getEntries(fromDate: String, toDate: String) = dao.getEntriesBetweenDates(fromDate, toDate)


}
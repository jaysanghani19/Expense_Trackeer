package com.jay.expensetracker.dashboardacitivity.analysis

import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.android.material.datepicker.MaterialDatePicker
import com.jay.expensetracker.R
import com.jay.expensetracker.customlayout.ProgressBarDialog
import com.jay.expensetracker.databinding.AnalysisFragmentBinding
import com.jay.expensetracker.entryroomdatabase.EntryRoomDatabase
import com.jay.expensetracker.entryroomdatabase.EntryRoomDatabaseBuilder
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.YearMonth
import java.util.Date

class AnalysisFragment : Fragment() {
    private lateinit var binding: AnalysisFragmentBinding

    //    MaterialDatePicker Dialog for choosing DateRange
    private lateinit var datePickerDialog: MaterialDatePicker.Builder<androidx.core.util.Pair<Long, Long>>

    //    ProgressBarDialog for progressBar
    private val progressBarDialog = ProgressBarDialog()

    //    Variable for dates
    private lateinit var fromDate: String
    private lateinit var toDate: String


    private val currentYear = LocalDate.now().year

    private var selectedTimeFrame = ""

    private var selectedTime = ""
    private var selectedMonth = ""
    private var selectedYear = ""

    private lateinit var viewModel: AnalysisFragmentViewModel

    val timeFrameArray = arrayOf("Monthly", "Yearly", "Custom")
    private val monthlyArray = arrayOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )


    //    DateFormat for formatting dates
    private val dateFormat = SimpleDateFormat("yyyy/MM/dd")


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        this.binding =
            DataBindingUtil.inflate(inflater, R.layout.analysis_fragment, container, false)

        val dao = EntryRoomDatabaseBuilder.getInstance(requireContext()).getEntryDatabaseDao()
        val factory = AnalysisFragmentViewModelFactory(requireActivity().application, dao)
        viewModel = ViewModelProvider(
            this,
            factory
        )[AnalysisFragmentViewModel::class.java]

        viewModel.setTimeFrame(requireContext())

        this.fromDate = ""
        this.toDate = ""


        this.binding.timeFrame.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {

//                Setting array adapter for choosing time like months,years
                this@AnalysisFragment.selectedTimeFrame = binding.timeFrame.text.toString()
                viewModel.setTime(requireContext(), selectedTimeFrame)
                viewModel.arrayAdapterOfTime.observe(viewLifecycleOwner) {
                    binding.chooseTime.setAdapter(it)
                }

//                After user select time Frame we'll initialize another dropdown menu for time or Visible TextView
                this@AnalysisFragment.binding.timeFrame.clearFocus()
                when (this@AnalysisFragment.selectedTimeFrame) {
//                    If User Select Monthly than we'll initializing time dropdown menu with months
                    timeFrameArray[0] -> {
                        this@AnalysisFragment.binding.fromToDate.visibility = View.GONE
                        this@AnalysisFragment.binding.chooseTimeLayout.visibility = View.VISIBLE
                        this@AnalysisFragment.binding.chooseTimeLayout.hint = "Month"
                    }
//                    If User Select Years than we'll initializing time dropdown menu with current year and previous 2 years
                    timeFrameArray[1] -> {
                        this@AnalysisFragment.binding.fromToDate.visibility = View.GONE
                        this@AnalysisFragment.binding.chooseTimeLayout.visibility = View.VISIBLE
                        this@AnalysisFragment.binding.chooseTimeLayout.hint = "Year"
                    }
//                    If user select Custom than we'll make fromToDate view visible so user can choose date
                    timeFrameArray[2] -> {
                        this@AnalysisFragment.binding.chooseTimeLayout.visibility = View.GONE
                        this@AnalysisFragment.binding.fromToDate.visibility = View.VISIBLE
                    }
                }
            }

        })

//        If user choose Year or monthly than we'll
        this.binding.chooseTime.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                this@AnalysisFragment.binding.chooseTime.clearFocus()
                viewModel.setMonthYear(selectedTimeFrame, binding.chooseTime.text.toString())
                when (selectedTimeFrame) {
                    timeFrameArray[0] -> selectedMonth = binding.chooseTime.text.toString()
                    timeFrameArray[1] -> selectedYear = binding.chooseTime.text.toString()
                    else -> selectedTime = " "
                }
            }
        })

        this.binding.fromToDate.setOnClickListener {
            clickListenerOfFromToDate()
        }

        this.binding.btnApply.setOnClickListener {
            clickListenerOfApplyButton(selectedTimeFrame)
        }
        viewModel.arrayAdapterOfTimeFrame.observe(viewLifecycleOwner) {
            binding.timeFrame.setAdapter(it)
        }

        return this.binding.root
    }

    //    functions for clickListener of views

    //    This function will create the datePickerDialog and
//    cause of that user can select the dates
    private fun clickListenerOfFromToDate() {
        this.datePickerDialog = MaterialDatePicker.Builder.dateRangePicker()
        this.datePickerDialog.setTitleText("Select Date Range")
        val datePicker = datePickerDialog.build()
        datePicker.addOnPositiveButtonClickListener {
            val fromDate = it.first
            val toDate = it.second

//            Setting dates in textview
            this.fromDate = this.dateFormat.format(Date(fromDate))
            this.toDate = this.dateFormat.format(Date(toDate))
            val fromToDate = getString(R.string.from_to_date, this.fromDate, this.toDate)
            this.binding.fromToDate.text = fromToDate
        }
        datePicker.show(childFragmentManager, "DatePicker")
    }

    //    This function will have timeFrame as parameter
//    and according to that it'll make changes in UI
//    if every details is okay than it'll invoke fetchEntries(timeFrame)
    private fun clickListenerOfApplyButton(timeFrame: String) {
        this.progressBarDialog.show(childFragmentManager, "ProgressBar")
        this.binding.errorMessage.visibility = View.GONE

        when (timeFrame) {
            timeFrameArray[0] -> {
                if (this.binding.chooseTime.text.toString() == "Select") {
                    this.binding.errorMessage.visibility = View.VISIBLE
                    this.binding.errorMessage.text = "Choose Month"
                    this.progressBarDialog.dismiss()
                } else {
                    this.binding.errorMessage.visibility = View.GONE
                    fetchEntries(timeFrame)
                }
            }

            timeFrameArray[1] -> {
                if (this.binding.chooseTime.text.toString() == "Select") {
                    this.binding.errorMessage.visibility = View.VISIBLE
                    this.binding.errorMessage.text = "Choose Year"
                    this.progressBarDialog.dismiss()
                } else {
                    this.binding.errorMessage.visibility = View.GONE
                    fetchEntries(timeFrame)
                }
            }

            else -> {
                if (this.binding.fromToDate.text.toString() == getString(R.string.select_range)) {
                    this.binding.errorMessage.visibility = View.VISIBLE
                    this.binding.errorMessage.setText(R.string.chose_dates)
                    this.progressBarDialog.dismiss()
                } else {
                    this.binding.errorMessage.visibility = View.GONE
                    fetchEntries(timeFrame)
                }
            }
        }
    }

    //  This function will take timeFrame as parameter
//  function will setDates() aftere that it'll fetch entries from the viewModel getEntries Function
//  and finally it'll setDataOnGraph
    private fun fetchEntries(timeFrame: String) {
//        Setting dates for Monthly/Yearly Time frame
        setDates(timeFrame)

        this.fromDate = this.fromDate.replace("-", "/")
        this.toDate = this.toDate.replace("-", "/")

        val entries = viewModel.getEntries(fromDate, toDate)

        entries.observe(viewLifecycleOwner) {
            setDataOnGraph(it, timeFrame)
        }
    }

    //    This function will take the entries between the dates and timeFrame
//    if timeFrame is custom Than it'll create the pieDataSet and show the data on analysisGraph1
//    else it'll create pieDataSet and barDataSet and show the data on analysisGraph1 and analysisGraph2
    private fun setDataOnGraph(entries: List<EntryRoomDatabase>, timeFrame: String) {
//    for analysisGraph1
        val mapForCategories = HashMap<String, Float>()
//    for analysisGraph2
        val mapForDatesOrMonths = HashMap<String, Float>()

        if (entries.isEmpty()) {
            binding.analysisGraph.visibility = View.GONE
            binding.analysisGraph2.visibility = View.GONE
            binding.dragMessage.visibility = View.GONE
            progressBarDialog.dismiss()
            Toast.makeText(requireContext(), "No Data", Toast.LENGTH_LONG).show()
        } else {
            entries.forEach { entry ->
                if (entry.transactionType.equals(EntryRoomDatabase.DEBIT_TRANSACTION)) {
                    mapForCategories.merge(
                        entry.category,
                        entry.amount
                    ) { existingValue, newValue ->
                        existingValue + newValue
                    }

                    when (timeFrame) {
                        timeFrameArray[0] -> mapForDatesOrMonths.merge(
                            entry.date,
                            entry.amount
                        ) { existingValue, newValue ->
                            existingValue + newValue
                        }

                        timeFrameArray[1] -> {
                            val month = getMonthOfDate(entry.date)
                            mapForDatesOrMonths.merge(
                                month.toString(),
                                entry.amount
                            ) { existingValue, newValue ->
                                existingValue + newValue
                            }
                        }
                    }
                }
            }
//      Creating dataset for the analysisGraph1
            val entriesOfGraph = ArrayList<PieEntry>()
            mapForCategories.forEach { (category, amount) ->
                entriesOfGraph.add(PieEntry(amount, category))
            }

            entriesOfGraph.sortByDescending { it.value }

            if (entriesOfGraph.size > 4) {
                val othersSum =
                    entriesOfGraph.subList(3, entriesOfGraph.size).sumOf { it.value.toDouble() }
                        .toFloat()
                entriesOfGraph.subList(3, entriesOfGraph.size).clear()
                entriesOfGraph.add(PieEntry(othersSum, "Others"))
            }

//      customcolors for graph
            val customColors = intArrayOf(
                ContextCompat.getColor(requireContext(), R.color.graph_1),
                ContextCompat.getColor(requireContext(), R.color.graph_2),
                ContextCompat.getColor(requireContext(), R.color.graph_3),
                ContextCompat.getColor(requireContext(), R.color.graph_4),
                ContextCompat.getColor(requireContext(), R.color.graph_5)
            ).toList()

            val chartDataSet = PieDataSet(entriesOfGraph, "Categories")
            chartDataSet.colors = customColors
            val pieChartData = PieData(chartDataSet)

//    if timeframe is custom than it'll show piechart
//    else it'll create dataSet for analysisGraph2 and show the analysisGraph2
            if (selectedTimeFrame == timeFrameArray[2]) {
                setPieChart(pieChartData)
                binding.analysisGraph2.visibility = View.GONE
                binding.dragMessage.visibility = View.GONE
                progressBarDialog.dismiss()
            } else {

                binding.analysisGraph2.visibility = View.VISIBLE
                val barGraphDescription = when (timeFrame) {
                    timeFrameArray[0] -> "Dates"
                    timeFrameArray[1] -> "Months"
                    else -> ""
                }
                val entriesForBarGraph = ArrayList<BarEntry>()
                val labels = ArrayList<String>()

                mapForDatesOrMonths.entries.forEachIndexed { index, (key, value) ->
                    entriesForBarGraph.add(BarEntry(index.toFloat(), value, key))
                    labels.add(key)
                }

                val dataSet = BarDataSet(entriesForBarGraph, barGraphDescription)
                dataSet.colors = customColors
                dataSet.valueTextSize = 12f
                dataSet.valueTextColor = Color.BLACK
                dataSet.barBorderWidth = 1f

                val barGraphData = BarData(dataSet)
                setBarGraph(entriesForBarGraph, labels, barGraphData, barGraphDescription)

                setPieChart(pieChartData)
            }
        }
    }

    //    this function will take data for setting analysisGraph2 and set the UI
    private fun setBarGraph(
        entriesForBarGraph: ArrayList<BarEntry>,
        labels: ArrayList<String>,
        barGraphData: BarData,
        barGraphDescription: String,
    ) {
        binding.analysisGraph2.data = barGraphData

        binding.analysisGraph2.axisLeft.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART)
        binding.analysisGraph2.axisLeft.axisMinimum = 0f
        binding.analysisGraph2.axisLeft.axisMaximum = getMaxValueFromBarEntries(entriesForBarGraph)

        binding.analysisGraph2.axisRight.isEnabled = false

        binding.analysisGraph2.xAxis.position = XAxis.XAxisPosition.BOTTOM
        binding.analysisGraph2.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        binding.analysisGraph2.xAxis.labelCount = labels.size
        binding.analysisGraph2.xAxis.granularity = 1f
        binding.analysisGraph2.xAxis.isGranularityEnabled = true
        binding.analysisGraph2.xAxis.isEnabled = true

        binding.analysisGraph2.description.isEnabled = true
        binding.analysisGraph2.description.text = "Data Across $barGraphDescription"

        binding.analysisGraph2.isDragEnabled = true

        binding.analysisGraph2.setVisibleXRangeMaximum(5f)
        binding.analysisGraph2.setVisibleXRangeMinimum(2f)

        binding.analysisGraph2.axisLeft.setDrawGridLines(false)
        binding.analysisGraph2.axisRight.setDrawGridLines(false)

        binding.analysisGraph2.invalidate()
        binding.dragMessage.visibility = View.VISIBLE

        binding.analysisGraph2.animateXY(2000, 2000)

        progressBarDialog.dismiss()
    }

    //  This function will set the data and set UI of the analysisGraph2
    private fun setPieChart(chartData: PieData) {
        binding.analysisGraph.setUsePercentValues(true)
        binding.analysisGraph.visibility = View.VISIBLE
        binding.analysisGraph.data = chartData
        binding.analysisGraph.description.isEnabled = true
        binding.analysisGraph.description.text = "Data Across Categories"
        binding.analysisGraph.description.textSize = 10f
    }

    //    This function will get the first and last date of the month according to year and month
    private fun getFirstAndLastDateOfMonth(year: Int, month: Int): Pair<String, String> {
        val firstDayOfMonth = LocalDate.of(year, month, 1)
        val lastDayOfMonth = YearMonth.of(year, month).atEndOfMonth()
        val fromDate = firstDayOfMonth.toString()
        val toDate = lastDayOfMonth.toString()
        return Pair(fromDate, toDate)
    }

    //    This function will set the fromData and toDate based on timeFrame
    private fun setDates(timeFrame: String) {
        when (timeFrame) {
            timeFrameArray[0] -> {
                var month = 0
                when (this.selectedMonth) {
                    monthlyArray[0] -> {
                        month = 1
                    }

                    monthlyArray[1] -> {
                        month = 2
                    }

                    monthlyArray[2] -> {
                        month = 3
                    }

                    monthlyArray[3] -> {
                        month = 4
                    }

                    monthlyArray[4] -> {
                        month = 5
                    }

                    monthlyArray[5] -> {
                        month = 6
                    }

                    monthlyArray[6] -> {
                        month = 7
                    }

                    monthlyArray[7] -> {
                        month = 8
                    }

                    monthlyArray[8] -> {
                        month = 9
                    }

                    monthlyArray[9] -> {
                        month = 10
                    }

                    monthlyArray[10] -> {
                        month = 11
                    }

                    monthlyArray[11] -> {
                        month = 12
                    }
                }
                val dates = getFirstAndLastDateOfMonth(currentYear, month)
                this.fromDate = dates.first
                this.toDate = dates.second
            }

            timeFrameArray[1] -> {
                this.fromDate = "01/01/${this.selectedYear}"
                this.toDate = "31/12/${this.selectedYear}"

            }
        }
    }

    //    This function will get the month of the date
    private fun getMonthOfDate(date: String): Int {
        var month: String = date.removeRange(0, 5)
        month = month.removeRange(2, 5)
        return month.toInt()
    }

    //  this function will get the highest value of barEntry
    private fun getMaxValueFromBarEntries(entries: ArrayList<BarEntry>): Float {
        var maxValue = Float.MIN_VALUE // Initialize with the smallest possible float value
        for (entry in entries) {
            val value = entry.y
            if (value > maxValue) {
                maxValue = value
            }
        }
        return maxValue
    }
}

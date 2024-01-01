package com.jay.expensetracker.settingactivity.budgeting

import androidx.fragment.app.Fragment

class BudgetingFragment : Fragment() {

    //    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?,
//    ): View {
//        binding =
//            DataBindingUtil.inflate(layoutInflater, R.layout.budgeting_fragment, container, false)
//
//        budgetDatabaseReference =
//            DatabaseClass.getInstanceOfEntriesDatabase().getReference(DatabaseClass.BUDGET_OF_USER)
//
//        setBudgetData()
//        binding.backButton.setOnClickListener {
//            clickListenerOfBackButton()
//        }
//        binding.editButton.setOnClickListener {
//            clickListenerOfEditButton()
//        }
//        binding.saveButton.setOnClickListener {
//            clickListenerOfSaveButton()
//        }
//        binding.discardButton.setOnClickListener {
//            clickListenerOfDiscardButton()
//        }
//        return binding.root
//    }
//
////    This function will make views InEditable and button invisible
//    private fun clickListenerOfDiscardButton() {
//        makingViewsEditableOrInEditable(false)
//        binding.buttonLayout.visibility = View.GONE
//    }
////  this function will make views Editable and buttons visible
//    private fun clickListenerOfEditButton() {
//        //        Making edit text views editable
//        makingViewsEditableOrInEditable(true)
//
//        binding.buttonLayout.visibility = View.VISIBLE
//    }
//
////    this function will save the data that user changed
//    private fun clickListenerOfSaveButton() {
//        progressBarDialog.show(childFragmentManager, "ProgressBar")
//        val salary = binding.salary.text.toString().toFloat()
//        val transportation = binding.transportation.text.toString().toFloat()
//        val groceries = binding.groceries.text.toString().toFloat()
//        val utilities = binding.utilities.text.toString().toFloat()
//        val insurance = binding.insurance.text.toString().toFloat()
//        val saving = binding.saving.text.toString().toFloat()
//        val entertainment = binding.entertainment.text.toString().toFloat()
//        val miscellaneous = binding.miscellaneous.text.toString().toFloat()
//        if (salary == 0f) {
//            binding.salary.requestFocus()
//            binding.salary.error = "Enter the error"
//            progressBarDialog.dismiss()
//        } else if (groceries + utilities + insurance + saving + entertainment + miscellaneous + transportation != 100f) {
//            binding.groceries.error = "Overall % should be 100%"
//            progressBarDialog.dismiss()
//        } else {
//            val budget = BudgetDataClass(
//                salary, groceries, transportation, utilities,
//                insurance, saving, entertainment, miscellaneous
//            )
//            updateBudgetData(budget)
//        }
//    }
//
////    it'll navigate back to SettingFragment
//    private fun clickListenerOfBackButton() {
//        findNavController().navigate(BudgetingFragmentDirections.actionBudgetingFragmentToSettingFragment())
//    }
//
////    this function will the update data on firebase database
//    private fun updateBudgetData(budget: BudgetDataClass) {
//        val map = hashMapOf<String, Float>(
//            Pair(BudgetDataClass.SALARY, budget.salary),
//            Pair(BudgetDataClass.GROCERIES, budget.groceries),
//            Pair(BudgetDataClass.TRANSPORTATION, budget.transportation),
//            Pair(BudgetDataClass.UTILITIES, budget.utilities),
//            Pair(BudgetDataClass.INSURANCE, budget.insurance),
//            Pair(BudgetDataClass.SAVING, budget.saving),
//            Pair(BudgetDataClass.ENTERTAINMENT, budget.entertainment),
//            Pair(BudgetDataClass.MISCELLANEOUS, budget.miscellaneous)
//        )
//        if (DatabaseClass.isInternetAvailable(requireContext())) {
//            budgetDatabaseReference.updateChildren(map as Map<String, Any>).addOnCompleteListener {
//                if (it.isSuccessful) {
//                    Toast.makeText(
//                        requireContext(),
//                        "Data Updated Successfully",
//                        Toast.LENGTH_SHORT
//                    ).show()
//                    makingViewsEditableOrInEditable(false)
//                    binding.buttonLayout.visibility = View.GONE
//                    progressBarDialog.dismiss()
//                } else {
//                    Toast.makeText(requireContext(), "Try Again", Toast.LENGTH_SHORT).show()
//                    progressBarDialog.dismiss()
//                }
//            }
//        } else {
//            progressBarDialog.dismiss()
//            Toast.makeText(requireContext(), "Connect to the Internet", Toast.LENGTH_SHORT).show()
//        }
//    }
//
//    //    function will receive boolean if it's true than we'll make editable and not then not editable
//    private fun makingViewsEditableOrInEditable(boolean: Boolean) {
//        /* Making EditText View editable or ineditable */
//        binding.salary.isCursorVisible = boolean
//        binding.salary.isFocusableInTouchMode = boolean
//        binding.salary.isFocusable = boolean
//        binding.salary.isLongClickable = boolean
//
//        binding.groceries.isCursorVisible = boolean
//        binding.groceries.isFocusableInTouchMode = boolean
//        binding.groceries.isFocusable = boolean
//        binding.groceries.isLongClickable = boolean
//
//        binding.transportation.isCursorVisible = boolean
//        binding.transportation.isFocusableInTouchMode = boolean
//        binding.transportation.isFocusable = boolean
//        binding.transportation.isLongClickable = boolean
//
//        binding.utilities.isCursorVisible = boolean
//        binding.utilities.isFocusableInTouchMode = boolean
//        binding.utilities.isFocusable = boolean
//        binding.utilities.isLongClickable = boolean
//
//        binding.insurance.isCursorVisible = boolean
//        binding.insurance.isFocusableInTouchMode = boolean
//        binding.insurance.isFocusable = boolean
//        binding.insurance.isLongClickable = boolean
//
//        binding.saving.isCursorVisible = boolean
//        binding.saving.isFocusableInTouchMode = boolean
//        binding.saving.isFocusable = boolean
//        binding.saving.isLongClickable = boolean
//
//        binding.entertainment.isCursorVisible = boolean
//        binding.entertainment.isFocusableInTouchMode = boolean
//        binding.entertainment.isFocusable = boolean
//        binding.entertainment.isLongClickable = boolean
//
//        binding.miscellaneous.isCursorVisible = boolean
//        binding.miscellaneous.isFocusableInTouchMode = boolean
//        binding.miscellaneous.isFocusable = boolean
//        binding.miscellaneous.isLongClickable = boolean
//
//    }
//
////    this function will get data from firebase database and set on the textviews
//    private fun setBudgetData() {
//        budgetDatabaseReference = DatabaseClass.getInstanceOfEntriesDatabase()
//            .getReference(DatabaseClass.USERS_BUDGET_PATH)
//
//        if (DatabaseClass.isInternetAvailable(requireContext())) {
//            budgetDatabaseReference.addValueEventListener(object : ValueEventListener {
//                override fun onDataChange(snapshot: DataSnapshot) {
//                    if (snapshot.exists()) {
//                        val value = snapshot.getValue(BudgetDataClass::class.java)
//                        if (value != null) {
//                            updateUI(value)
//                        } else {
//                            updateBudgetDataIfDataIsNull()
//                        }
//                    } else {
//                        updateBudgetDataIfDataIsNull()
//                    }
//                }
//
//                override fun onCancelled(error: DatabaseError) {
//                    binding.editTextLayout.visibility = View.GONE
//                    binding.reloadButton.visibility = View.VISIBLE
//                    Toast.makeText(requireContext(), "Try Again", Toast.LENGTH_SHORT).show()
//                }
//
//            })
//        } else {
//            Toast.makeText(requireContext(), "Connect to the Internet", Toast.LENGTH_SHORT).show()
//        }
//    }
//
//    private fun updateBudgetDataIfDataIsNull() {
//        val budget = BudgetDataClass(
//            binding.salary.text.toString().toFloat(),
//            binding.groceries.text.toString().toFloat(),
//            binding.transportation.text.toString().toFloat(),
//            binding.utilities.text.toString().toFloat(),
//            binding.insurance.text.toString().toFloat(),
//            binding.saving.text.toString().toFloat(),
//            binding.entertainment.text.toString().toFloat(),
//            binding.miscellaneous.text.toString().toFloat()
//        )
//        budgetDatabaseReference.setValue(budget).addOnCompleteListener {
//            setBudgetData()
//        }
//    }
//
////    this function will the update ui for edittext views
//    fun updateUI(value: BudgetDataClass) {
//        binding.salary.text = SpannableStringBuilder(value.salary.toString())
//        binding.groceries.text =
//            SpannableStringBuilder(value.groceries.toString())
//        binding.transportation.text =
//            SpannableStringBuilder(value.transportation.toString())
//        binding.utilities.text =
//            SpannableStringBuilder(value.utilities.toString())
//        binding.miscellaneous.text =
//            SpannableStringBuilder(value.miscellaneous.toString())
//        binding.saving.text = SpannableStringBuilder(value.saving.toString())
//        binding.entertainment.text =
//            SpannableStringBuilder(value.entertainment.toString())
//        binding.insurance.text =
//            SpannableStringBuilder(value.insurance.toString())
//    }
}
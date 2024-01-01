package com.jay.expensetracker.customlayout

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.jay.expensetracker.R
import com.jay.expensetracker.addactivity.AddActivity
import com.jay.expensetracker.databinding.EntryShowLayoutBinding
import com.jay.expensetracker.entryroomdatabase.EntryRoomDatabase

class EntryShowCustomLayout(private val entry: EntryRoomDatabase) : DialogFragment() {
    private lateinit var binding: EntryShowLayoutBinding

    interface DialogListener {
        fun onEditButtonClick()
        fun onDeleteButtonClick()
    }

    private var dialogListener: DialogListener? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.entry_show_layout, container, false)
        binding.amountTextview.text = getString(R.string.amount, entry.amount.toString())
        binding.noteTextview.text = getString(R.string.get_note, entry.note)
        binding.dateTextview.text = getString(R.string.date, entry.date)
        binding.categoryTextview.text = getString(R.string.category, entry.category)

        val imageOfCategory = when (entry.category) {
            AddActivity.categoryDebit[0] -> R.drawable.groceries
            AddActivity.categoryDebit[1] -> R.drawable.transportations
            AddActivity.categoryDebit[2] -> R.drawable.utilities
            AddActivity.categoryDebit[3], AddActivity.categoryCredit[2] -> R.drawable.insurance
            AddActivity.categoryDebit[4] -> R.drawable.saving
            AddActivity.categoryDebit[5] -> R.drawable.entertainment
            AddActivity.categoryCredit[0] -> R.drawable.salary
            AddActivity.categoryCredit[1] -> R.drawable.bonus
            AddActivity.categoryCredit[3] -> R.drawable.rent
            AddActivity.categoryCredit[4] -> R.drawable.profit
            else -> R.drawable.others
        }
        val transactionType =
            if (entry.transactionType == EntryRoomDatabase.CREDIT_TRANSACTION) getString(R.string.credit)
            else getString(R.string.debit)

        binding.categoryIcon.setImageResource(imageOfCategory)
        binding.creditOrDebit.text = transactionType

        try {
            val bitmap = BitmapFactory.decodeFile(entry.imagePath)
            binding.billImage.setImageBitmap(bitmap)
            binding.billImage.visibility = View.VISIBLE
            binding.billImage.visibility = View.VISIBLE
        } catch (e: Exception) {
            binding.billImage.visibility = View.GONE
            binding.billImage.visibility = View.GONE
            binding.imageDeletedMessage.visibility = View.VISIBLE
        }

        binding.closeButton.setOnClickListener {
            dismiss()
        }
        binding.editButton.setOnClickListener {
            dialogListener?.onEditButtonClick()
        }
        binding.deleteButton.setOnClickListener {
            dialogListener?.onDeleteButtonClick()
        }
        return binding.root
    }

    fun setDialogListener(listener: DialogListener) {
        this.dialogListener = listener
    }

    //    fun clickListenerOfDelete(listener: View.OnClickListener){
//        binding.deleteButton.setOnClickListener(clickListener)
//    }
    override fun onStart() {
        super.onStart()

        // Set the dialog size to cover 70% of the screen
        val width = (resources.displayMetrics.widthPixels * 1)
        val height = (resources.displayMetrics.heightPixels * 0.8).toInt()
        dialog?.window?.setLayout(width, height)
    }
}
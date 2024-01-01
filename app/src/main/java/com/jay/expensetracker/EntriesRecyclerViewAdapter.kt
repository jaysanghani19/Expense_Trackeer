package com.jay.expensetracker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.jay.expensetracker.addactivity.AddActivity
import com.jay.expensetracker.entryroomdatabase.EntryRoomDatabase

class OfflineEntriesRecyclerViewAdapter(
    private val entriesList: List<EntryRoomDatabase>,
    private val clickListener: (EntryRoomDatabase) -> Unit,
) :
    RecyclerView.Adapter<EntriesRecyclerViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): EntriesRecyclerViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val itemsView = layoutInflater.inflate(R.layout.entry_layout, parent, false)
        return EntriesRecyclerViewHolder(itemsView)
    }

    override fun getItemCount(): Int {
        return entriesList.size
    }

    override fun onBindViewHolder(holder: EntriesRecyclerViewHolder, position: Int) {
        holder.bindData(entriesList.elementAt(position), clickListener)
    }


}

class EntriesRecyclerViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
    private val amountView: TextView = view.findViewById(R.id.entry_amount)
    private val noteView: TextView = view.findViewById(R.id.entry_note)
    private val dateView: TextView = view.findViewById(R.id.entry_date)
    private val imageViewOfCategory: ImageView = view.findViewById(R.id.category_image)
    private val imageViewOfTransactionType: ImageView =
        view.findViewById(R.id.transaction_type_image)

    fun bindData(entry: EntryRoomDatabase, clickListener: (EntryRoomDatabase) -> Unit) {
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
        val imageOfTransactionType =
            if (entry.transactionType == EntryRoomDatabase.CREDIT_TRANSACTION) R.drawable.credit
            else R.drawable.debit
        imageViewOfCategory.setImageResource(imageOfCategory)
        imageViewOfTransactionType.setImageResource(imageOfTransactionType)
        amountView.text = entry.amount.toString()
        noteView.text = entry.note
        dateView.text = entry.date

        view.setOnClickListener {
            clickListener(entry)
        }
    }


}
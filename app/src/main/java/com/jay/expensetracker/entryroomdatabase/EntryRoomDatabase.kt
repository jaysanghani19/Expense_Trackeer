package com.jay.expensetracker.entryroomdatabase

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

//  Room database for the entry
@Entity(tableName = "entrydatabase")
data class EntryRoomDatabase(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "entryNumber")
    var entryNumber: Int,
    @ColumnInfo(name = "transactonType")
    val transactionType: String,
    @ColumnInfo(name = "timeStamp")
    val timeStamp: String,
    @ColumnInfo(name = "amount")
    val amount: Float,
    @ColumnInfo(name = "date")
    val date: String,
    @ColumnInfo(name = "category")
    val category: String,
    @ColumnInfo(name = "note")
    val note: String,
    @ColumnInfo(name = "image64Code")
    val imagePath: String,
) {
    companion object {
        const val DEBIT_TRANSACTION = "Debit"
        const val CREDIT_TRANSACTION = "Credit"
    }
}


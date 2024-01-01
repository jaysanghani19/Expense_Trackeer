package com.jay.expensetracker.dataclass

//  DataClass for Entry
data class EntryDataClass(
    val entryNumber: Int? = null,
    val transactionType: String = "",
    val timeStamp: String = "",
    val amount: Float? = null,
    val date: String = "",
    val category: String = "",
    val note: String = "",
    val billImageBase64Code: String? = null,
) {
    companion object {
        const val ENTRY_NUMBER = "entryNumber"
        const val AMOUNT = "amount"
        const val DATE = "date"
        const val CATEGORY = "category"
        const val NOTE = "note"
        const val BILLIMAGEBASE64CODE = "billImageBase64Code"
    }
}


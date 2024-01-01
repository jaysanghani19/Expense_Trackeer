package com.jay.expensetracker.dataclass

//  data Class of the NoOfEntries
data class NoOfEntriesDataClass(var totalEntries: Int? = null) {
    companion object {
        const val TOTAL_ENTRIES = "totalEntries"
    }
}
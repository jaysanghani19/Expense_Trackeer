package com.jay.expensetracker.dataclass

data class BalanceDataClass(val currentBalance: Float? = null) {
    companion object {
        const val CURRENT_BALANCE = "currentBalance"
    }
}

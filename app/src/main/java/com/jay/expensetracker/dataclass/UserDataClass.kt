package com.jay.expensetracker.dataclass

// Creating the UserDataClass for reference of the User Data
data class UserDataClass(
    val uidOfUser: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val gender: String = "",
    val mobileNo: String = "",
) {
    companion object {
        const val FIRST_NAME = "firstName"
        const val LAST_NAME = "lastName"
        const val EMAIL = "email"
        const val GENDER = "gender"
        const val MOBILE_NO = "mobileNo"
    }
}
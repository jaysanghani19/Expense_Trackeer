package com.jay.expensetracker

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class DatabaseClass {
    companion object {

        //       database of User's Data
        private val firebaseAuth = FirebaseAuth.getInstance()

        private val database = FirebaseDatabase.getInstance()

        private var userUID = FirebaseAuth.getInstance().currentUser?.uid.toString()


        //        this reference have the personal data of all user
        //        Reference String of the all user in database
        const val USERDATA_DATABASE_PATH = "Registered User"

        //        Reference String of the particular users personal data who is logged in
        fun pathForUsersRegisteredData():String =
            "$USERDATA_DATABASE_PATH/${this.userUID}"

        fun setUserUID(){
            this.userUID = FirebaseAuth.getInstance().currentUser?.uid.toString()
        }
        //        it'll get instance of the database from that we'll make reference
        fun getInstanceOfUserDatabase(): FirebaseDatabase {
            return database
        }

        //        it'll get the referece of the FirebaseAuth based on that we'll sign in, sign up,Forgot password and sign out
        fun getInstanceOfFirebaseAuth(): FirebaseAuth {
            return firebaseAuth
        }

//        DATABASE OF User it includes (Entries of user , No Of total entries , budget of user)

        const val entryNumberChild = "entryNumber"

        //        1st child
        val USER_DATABASE_PATH: String = userUID

        //        2nd child for Entries in database (Entries of user)
        const val ENTRIES = "Entries"

        //        2nd child for (no of Entries)
        const val NO_OF_ENTRIES = "No of Entries"

        const val BALANCE_OF_USER = "Current Balance"

//        val USERS_BALANCE_PATH = "$USER_DATABASE_PATH/$BALANCE_OF_USER"

        fun USERS_BALANCE_PATH() : String = "$USER_DATABASE_PATH/$BALANCE_OF_USER"
        //        path for the entries of the user
//        val USERS_ENTRYDATABASE_PATH = "$USER_DATABASE_PATH/$ENTRIES"

        fun USERS_ENTRYDATABASE_PATH() : String = "$USER_DATABASE_PATH/$ENTRIES"

        //        path for the no of entries of the user
//        val USER_NO_OF_ENTRIES_PATH = "$USER_DATABASE_PATH/$NO_OF_ENTRIES"
        fun USER_NO_OF_ENTRIES_PATH() : String = "$USER_DATABASE_PATH/$NO_OF_ENTRIES"
        //        It'll get the reference for adding the entry
        //        Under this section we'll add all entries
        fun getInstanceOfEntriesDatabase(): FirebaseDatabase {
            return FirebaseDatabase.getInstance()
        }

        fun isInternetAvailable(context: Context): Boolean {
            val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

            val network = connectivityManager.activeNetwork
            val networkCapabilities = connectivityManager.getNetworkCapabilities(network)

            // Check if the device has internet connectivity
            return networkCapabilities != null &&
                    (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR))
        }
    }

}
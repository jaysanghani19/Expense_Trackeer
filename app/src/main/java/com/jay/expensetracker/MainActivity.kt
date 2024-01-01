package com.jay.expensetracker

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.jay.expensetracker.dashboardacitivity.DashboardActivity

class MainActivity : AppCompatActivity() {

    private lateinit var runnable: Runnable
    private lateinit var handler: Handler
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splash_screen)
        isUserLoggedIn()
    }

    //    We're checking that user has logged in or not
    private fun isUserLoggedIn() {
//    Getting current user using FirebaseAuth
        val user = FirebaseAuth.getInstance().currentUser

//    if user is null then user hasn't logged in so we'll start SignInAndSignUp Activity
        if (user == null) {
            activityToSignInActivity()
        }
//    else Start DashboardActivity
        else {
            if (user.isEmailVerified) {
                activityToDashboardActivity()
            } else {
                activityToSignInActivity()
            }
        }
    }

    private fun activityToDashboardActivity() {
        runnable = Runnable {
            val intent = Intent(
                this@MainActivity,
                DashboardActivity::class.java
            )
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
        @Suppress("DEPRECATION")
        handler = Handler()
        handler.postDelayed(runnable, 1500)
    }

    private fun activityToSignInActivity() {
        runnable = Runnable {
            val intent = Intent(
                this@MainActivity,
                com.jay.expensetracker.signinactivity.SignInAndSignUPActivity::class.java
            )
            startActivity(intent)
            finish()
        }
        @Suppress("DEPRECATION")
        handler = Handler()
        handler.postDelayed(runnable, 1500)
    }

}
package com.jay.expensetracker.dashboardacitivity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.jay.expensetracker.MainActivity
import com.jay.expensetracker.R
import com.jay.expensetracker.dashboardacitivity.analysis.AnalysisFragment
import com.jay.expensetracker.dashboardacitivity.dashboard.Dashboard
import com.jay.expensetracker.dashboardacitivity.profile.Profile
import com.jay.expensetracker.dashboardacitivity.transaction.TransactionFragment
import com.jay.expensetracker.databinding.DashboardActivityBinding

@Suppress("DEPRECATION")
class DashboardActivity : AppCompatActivity() {
    private lateinit var binding: DashboardActivityBinding
    private val dashboard = Dashboard()
    private val analytics = AnalysisFragment()
    private val profile = Profile()
    private val transaction = TransactionFragment()

    //  when view is clicked than it'll replace the fragment
    private val onNavigationItemSelectedListener =
        BottomNavigationView.OnNavigationItemSelectedListener { item ->

            when (item.itemId) {
                R.id.home -> {
                    replaceFragment(dashboard)
                    return@OnNavigationItemSelectedListener true
                }

                R.id.transaction -> {
                    replaceFragment(transaction)
                    return@OnNavigationItemSelectedListener true
                }

                R.id.profile -> {
                    replaceFragment(profile)
                    return@OnNavigationItemSelectedListener true
                }

                R.id.analytics -> {
                    replaceFragment(analytics)
                    return@OnNavigationItemSelectedListener true
                }

                else -> return@OnNavigationItemSelectedListener false
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val uidOfUser = FirebaseAuth.getInstance().currentUser?.uid
        if (uidOfUser == null || uidOfUser == "null") {
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        } else {
            binding = DataBindingUtil.setContentView(this, R.layout.dashboard_activity)
            binding.bottomNavigation.setOnNavigationItemSelectedListener(
                onNavigationItemSelectedListener
            )
            binding.bottomNavigation.menu.findItem(R.id.home).isCheckable = true
        }
    }

    //  This function will replace the fragment based on which button is clicked on menu
    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.frame_layout, fragment)
            .commit()
    }
}
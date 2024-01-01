package com.jay.expensetracker.customlayout

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.jay.expensetracker.R
import com.jay.expensetracker.settingactivity.settingfragment.SettingFragment

class ConfirmationLayout(context: Context, val value: String) : AlertDialog(context) {

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.confirmation_dialog_layout)

        // Find views
        val titleTextView = findViewById<TextView>(R.id.titleTextView)
        val messageTextView = findViewById<TextView>(R.id.messageTextView)

        // setting the title for the message
        titleTextView.text = "Confirmation"
        val message = if (value == SettingFragment.SIGN_OUT)
            "You Really wanna log out"
        else if (value == SettingFragment.RESTORE_DATA)
            "You want to restore the entries"
        else "You Really wanna delete account"

        messageTextView.text = message
    }

    // Method to set a click listener for the positive button
    fun setPositiveButtonClickListener(listener: View.OnClickListener) {
        val positiveButton = findViewById<Button>(R.id.positiveButton)
        positiveButton.setOnClickListener(listener)
    }

    // Method to set a click listener for the negative button
    fun setNegativeButtonClickListener(listener: View.OnClickListener) {
        val negativeButton = findViewById<Button>(R.id.negativeButton)
        negativeButton.setOnClickListener(listener)
    }
}

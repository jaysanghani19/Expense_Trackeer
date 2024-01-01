package com.jay.expensetracker.customlayout

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.jay.expensetracker.R

class ProgressBarDialog : DialogFragment() {
    //    it'll create the progressBar Dialog
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(requireContext(), android.R.style.Theme_Translucent_NoTitleBar)
        dialog.setContentView(R.layout.progress_bar_layout)
        dialog.setCancelable(false)
        return dialog

    }
}
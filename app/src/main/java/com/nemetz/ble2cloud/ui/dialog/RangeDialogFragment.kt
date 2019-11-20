package com.nemetz.ble2cloud.ui.dialog

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment

class RangeDialogFragment(val onClickListener: DialogInterface.OnClickListener) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            builder.setTitle("Pick range")
                .setItems(
                    arrayOf(
                        "Last hour",
                        "Last 3 hour",
                        "Last day",
                        "Custom"
                    ), onClickListener
                )
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

}
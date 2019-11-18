package com.nemetz.ble2cloud.ui.dialog

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment

class DeleteConfirmDialogFragment(
    private val positiveClickListener: DialogInterface.OnClickListener,
    private val negativeClickListener: DialogInterface.OnClickListener
) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            AlertDialog.Builder(it)
                .setTitle("Delete confirmation")
                .setMessage("Are you sure you want to delete this senor?")
                .setPositiveButton("Delete", positiveClickListener)
                .setNegativeButton("Cancel", negativeClickListener)
                .create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}
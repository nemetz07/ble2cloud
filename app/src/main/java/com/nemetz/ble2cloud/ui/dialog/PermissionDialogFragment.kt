package com.nemetz.ble2cloud.ui.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.text.Html
import androidx.fragment.app.DialogFragment
import com.nemetz.ble2cloud.utils.manager.PermissionManager

class PermissionDialogFragment : DialogFragment() {
    private val TAG = "PERMISSION_DIALOG_FRAGMENT"

    private val title = "Location permission needed"
    private val button = "Open settings"
    private val message =
        "The application needs access to location for <b>Bluetooth scanning</b>. You can provide the needed permission in the in the <b>Settings</b>"

    companion object {
        @JvmStatic
        fun newInstance(): PermissionDialogFragment {
            return PermissionDialogFragment()
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(activity)
            .setTitle(title)
            .setMessage(Html.fromHtml(message, Html.FROM_HTML_MODE_COMPACT))
            .setPositiveButton(button) { _: DialogInterface, _: Int ->
                val settingsIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri: Uri = Uri.fromParts("package", activity?.packageName, null)
                settingsIntent.data = uri
                activity?.startActivityForResult(settingsIntent, PermissionManager.OPEN_SETTINGS_FOR_PERMISSION)
            }
            .create()
    }

    override fun onCancel(dialog: DialogInterface) {
        activity?.finish()
    }
}
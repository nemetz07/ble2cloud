package com.nemetz.ble2cloud.ui.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.NumberPicker
import android.widget.Switch
import androidx.fragment.app.DialogFragment
import com.nemetz.ble2cloud.BLE2CloudApplication
import com.nemetz.ble2cloud.R
import com.nemetz.ble2cloud.data.BLESensorValue

class EditSensorDialogFragment(val address: String, val sensorValue: BLESensorValue) :
    DialogFragment() {

    private val min = -100
    private val max = 100
    private val startValue = 0

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val alertDialogBuilder = AlertDialog.Builder(context)
        val dialogView =
            LayoutInflater.from(context).inflate(R.layout.dialog_edit_sensor, null, false)

        val minPicker = dialogView.findViewById<NumberPicker>(R.id.editSensorMinPicker)
        val maxPicker = dialogView.findViewById<NumberPicker>(R.id.editSensorMaxPicker)

        val minSwitch = dialogView.findViewById<Switch>(R.id.editSensorMinSwitch)
        val maxSwitch = dialogView.findViewById<Switch>(R.id.editSensorMaxSwitch)

        minSwitch.apply {
            isChecked = sensorValue.min != null
        }

        maxSwitch.apply {
            isChecked = sensorValue.max != null
        }

        minPicker.apply {
            maxValue = max - min
            setFormatter {
                "${it + min}"
            }
            setOnValueChangedListener { picker, oldVal, newVal ->
                minSwitch.isChecked = true
            }
            value = if (sensorValue.min == null) {
                startValue - min
            } else {
                sensorValue.min!! - min
            }
        }

        maxPicker.apply {
            maxValue = max - min
            setFormatter {
                "${it + min}"
            }
            setOnValueChangedListener { picker, oldVal, newVal ->
                maxSwitch.isChecked = true
            }
            value = if (sensorValue.max == null) {
                startValue - min
            } else {
                sensorValue.max!! - min
            }
        }

        alertDialogBuilder.apply {
            setTitle("Edit")
            setView(dialogView)
            setPositiveButton("Save") { dialogInterface: DialogInterface, i: Int ->
                sensorValue.min = if (minSwitch.isChecked) minPicker.value + min else null
                sensorValue.max = if (maxSwitch.isChecked) maxPicker.value + min else null

                (context.applicationContext as BLE2CloudApplication)
                    .cloudConnector.updateSensorAlert(address, sensorValue)
            }
        }

        return alertDialogBuilder.show()
    }
}
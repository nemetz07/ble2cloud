package com.nemetz.ble2cloud.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class BLESensor(
    val address: String = "",
    val name: String = "-",
    val values: ArrayList<BLESensorValue> = arrayListOf()
) : Parcelable
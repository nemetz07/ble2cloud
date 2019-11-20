package com.nemetz.ble2cloud.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class BLESensorValue(
    var format: BLEDataFormat? = null,
    var min: Int? = null,
    var max: Int? = null,
    var uuid: String = ""
) : Parcelable
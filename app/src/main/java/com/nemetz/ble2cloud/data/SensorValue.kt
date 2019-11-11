package com.nemetz.ble2cloud.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class SensorValue(var format: MyDataFormat? = null,
                  var uuid: String = ""
): Parcelable
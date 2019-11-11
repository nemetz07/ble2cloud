package com.nemetz.ble2cloud.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.RawValue

@Parcelize
data class MySensor(val address: String = "",
                    val name: String = "-",
                    val values: ArrayList<SensorValue> = arrayListOf()
): Parcelable
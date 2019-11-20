package com.nemetz.ble2cloud.data

import android.os.Parcelable
import com.google.firebase.Timestamp
import kotlinx.android.parcel.Parcelize

@Parcelize
data class BLESensor(
    val address: String = "",
    val name: String = "-",
    var createdAt: Timestamp? = null,
    var createdBy: String? = null,
    val values: MutableMap<String, BLESensorValue> = mutableMapOf()
) : Parcelable
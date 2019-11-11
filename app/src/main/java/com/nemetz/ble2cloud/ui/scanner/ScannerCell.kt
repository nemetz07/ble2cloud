package com.nemetz.ble2cloud.ui.scanner

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize


@Parcelize
data class ScannerCell(
    val name: String?,
    val address: String,
    var rssi: Int
) : Parcelable
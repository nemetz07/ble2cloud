package com.nemetz.ble2cloud.ui.deviceBrowser.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.nemetz.ble2cloud.R
import com.nemetz.ble2cloud.ui.deviceBrowser.model.DeviceCell
import kotlin.collections.ArrayList

class DeviceBrowserAdapter(var cellDevices: ArrayList<DeviceCell>) :
    RecyclerView.Adapter<DeviceBrowserAdapter.DeviceBrowserViewHolder>() {

    lateinit var mClickListener: ItemClickListener
    private lateinit var context: Context

    interface ItemClickListener{
        fun onItemClick(view: View, position: Int)
    }

    inner class DeviceBrowserViewHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {
        var nameTextView: TextView = view.findViewById(R.id.device_name_tv)
        var addressTextView: TextView = view.findViewById(R.id.device_address_tv)
        var rssiTextView: TextView = view.findViewById(R.id.device_rssi_tv)
        var icon: ImageView = view.findViewById(R.id.device_icon)
        var legacyTextView: TextView = view.findViewById(R.id.device_connectable)

        init{
            view.setOnClickListener(this)
        }

        override fun onClick(view: View?) {
            mClickListener.onItemClick(view!!, adapterPosition)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceBrowserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.device_cell, parent, false)

        context = parent.context

        return DeviceBrowserViewHolder(view)
    }

    fun getItem(id: Int): DeviceCell {
        return cellDevices[id]
    }

    override fun getItemCount() = cellDevices.size

    override fun onBindViewHolder(holder: DeviceBrowserViewHolder, position: Int) {
        val deviceCell: DeviceCell = cellDevices[position]

        holder.nameTextView.text = deviceCell.device.name ?: "-"
        holder.addressTextView.text = deviceCell.device.address
        holder.rssiTextView.text = "${deviceCell.rssi} dB"
//        holder.icon.src = holder.itemView.context.getString(R.string.icon_mobile)
        holder.legacyTextView.text = if(deviceCell.isConnectable) "Connectable" else ""
    }

    fun setClickListener(itemClickListener: ItemClickListener){
        mClickListener = itemClickListener
    }

}

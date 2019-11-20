package com.nemetz.ble2cloud.ui.scanner

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.nemetz.ble2cloud.R

class ScannerAdapter(val cellSensors: ArrayList<ScannerCell>) :
    RecyclerView.Adapter<ScannerAdapter.ScannerViewHolder>() {

    private lateinit var context: Context

    interface ItemClickListener {
        fun onItemClick(view: View, position: Int)
    }

    lateinit var mClickListener: ItemClickListener
    private lateinit var enabledClickListener: ItemClickListener
    private val disabledClickListener = object : ItemClickListener {
        override fun onItemClick(view: View, position: Int) {}
    }

    inner class ScannerViewHolder(view: View) : RecyclerView.ViewHolder(view),
        View.OnClickListener {
        var nameTextView: TextView = view.findViewById(R.id.sensor_name_tv)
        var addressTextView: TextView = view.findViewById(R.id.sensor_address_tv)
        var rssiTextView: TextView = view.findViewById(R.id.sensor_rssi_tv)
        var icon: ImageView = view.findViewById(R.id.sensor_icon)

        init {
            view.setOnClickListener(this)
        }

        override fun onClick(view: View?) {
            mClickListener.onItemClick(view!!, adapterPosition)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScannerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.scanner_cell, parent, false)

        context = parent.context

        return ScannerViewHolder(view)
    }

    fun getItem(id: Int): ScannerCell {
        return cellSensors[id]
    }

    override fun getItemCount() = cellSensors.size

    override fun onBindViewHolder(holder: ScannerViewHolder, position: Int) {
        val scannerCell: ScannerCell = cellSensors[position]

        holder.nameTextView.text = scannerCell.name
        holder.addressTextView.text = scannerCell.address
        holder.rssiTextView.text = "${scannerCell.rssi} dB"
    }

    fun setClickListener(itemClickListener: ItemClickListener) {
        enabledClickListener = itemClickListener
        enableClickListener()
    }

    fun disableClickListener() {
        mClickListener = disabledClickListener
    }

    fun enableClickListener() {
        mClickListener = enabledClickListener
    }

}

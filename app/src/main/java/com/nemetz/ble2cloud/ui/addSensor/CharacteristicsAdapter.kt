package com.nemetz.ble2cloud.ui.addSensor

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.nemetz.ble2cloud.R
import com.nemetz.ble2cloud.ui.scanner.ScannerAdapter

class CharacteristicsAdapter(val cellCharacteristics: ArrayList<CharacteristicCell>): RecyclerView.Adapter<CharacteristicsAdapter.ViewHolder>() {

    interface ItemClickListener{
        fun onItemClick(view: View, position: Int)
    }

    lateinit var mClickListener: ItemClickListener

    inner class ViewHolder(view: View): RecyclerView.ViewHolder(view), View.OnClickListener {
        var nameTextView = view.findViewById<TextView>(R.id.characteristicCellNameTV)
        var uuidTextView = view.findViewById<TextView>(R.id.characteristicCellUUIDTV)
        var unitTextView = view.findViewById<TextView>(R.id.characteristicCellUnitTV)
        var enableSwitch = view.findViewById<Switch>(R.id.characteristicCellEnableSwitch)

        init {
            view.setOnClickListener(this)
        }

        override fun onClick(view: View?) {
            mClickListener.onItemClick(view!!, adapterPosition)
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.characteristic_cell, parent, false)

        mClickListener = object : ItemClickListener {
            override fun onItemClick(view: View, position: Int) {
                cellCharacteristics[position].enabled = !cellCharacteristics[position].enabled
            }
        }

        return ViewHolder(view)
    }

    override fun getItemCount() = cellCharacteristics.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val characteristicCell = cellCharacteristics[position]

        holder.nameTextView.text = characteristicCell.name
        holder.uuidTextView.text = characteristicCell.uuid
        holder.unitTextView.text = "[${characteristicCell.unit}]"
        holder.enableSwitch.isChecked = characteristicCell.enabled
    }
}
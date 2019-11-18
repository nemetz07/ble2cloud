package com.nemetz.ble2cloud.ui.dataCollection

import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import com.nemetz.ble2cloud.R
import com.nemetz.ble2cloud.SDF_FULL
import com.nemetz.ble2cloud.SDF_TIME
import com.nemetz.ble2cloud.event.DataCollectionAddedEvent
import org.greenrobot.eventbus.EventBus
import java.util.*

class DataCollectionAdapter(val cellData: ArrayList<DataCollectionCell>) :
    RecyclerView.Adapter<DataCollectionAdapter.ViewHolder>(), EventListener<QuerySnapshot> {

    private val TAG = "DATA_COLLECTION_ADAPTER"

    override fun onEvent(querySnapshot: QuerySnapshot?, e: FirebaseFirestoreException?) {
        if (e != null) {
            Log.w(TAG, "onEvent:error", e)
            return
        }

        for (change in querySnapshot!!.documentChanges) {
            when (change.type) {
                DocumentChange.Type.ADDED -> {
                    addData(change)
                    Log.w(TAG, "DATA added")
                }
                else -> Log.w(TAG, "DATA modified")
            }
        }
    }

    private fun addData(change: DocumentChange) {
        val value = change.document["value"].toString()
        val unit = change.document.get("unit") ?: ""
        val name = change.document.get("name") ?: ""
        val sensorName = change.document.get("sensorName")
        val address = change.document.get("address")
        val createdAt: Timestamp? = change.document.getTimestamp("createdAt")
        val createdBy: String? = change.document.getString("createdBy")
        val isLocal = if(createdBy != null) createdBy == FirebaseAuth.getInstance().uid else true
        if (createdAt != null) {
            val date = Date()
            date.time = createdAt.seconds * 1000
            cellData.add(
                change.newIndex, DataCollectionCell(
                    value = "$value $unit",
                    createdAt = SDF_TIME.format(date),
                    name = name.toString(),
                    sensorName = sensorName?.toString(),
                    address = address?.toString(),
                    isLocal = isLocal
                )
            )
            notifyItemInserted(change.newIndex)
            EventBus.getDefault().post(DataCollectionAddedEvent())
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.data_collection_cell, parent, false)

        return ViewHolder(view)
    }

    override fun getItemCount(): Int = cellData.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val cell = cellData[position]

        holder.createdAtTV.text = cell.createdAt
        holder.valueTV.text = cell.value
        holder.nameTV.text = "${cell.name}:"
        holder.addressTV.text = cell.address
        holder.sensorNameTV.text = cell.sensorName
        val icon: Drawable = if (cell.isLocal){
            holder.itemView.resources.getDrawable(R.drawable.ic_smartphone, null)
        } else {
            holder.itemView.resources.getDrawable(R.drawable.ic_globe, null)
        }
        if(cell.isLocal){
            holder.itemView.setBackgroundColor(holder.itemView.resources.getColor(R.color.dataCellLocal, null))
            holder.icon.setImageDrawable(holder.itemView.resources.getDrawable(R.drawable.ic_smartphone, null))
        } else {
            holder.itemView.setBackgroundColor(holder.itemView.resources.getColor(R.color.dataCellOnline, null))
            holder.icon.setImageDrawable(holder.itemView.resources.getDrawable(R.drawable.ic_globe, null))
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var createdAtTV = itemView.findViewById<TextView>(R.id.dataCollectionCellCreatedAtTV)
        var valueTV = itemView.findViewById<TextView>(R.id.dataCollectionCellValueTV)
        var nameTV = itemView.findViewById<TextView>(R.id.dataCollectionCellNameTV)
        var sensorNameTV = itemView.findViewById<TextView>(R.id.dataCollectionCellSensorNameTV)
        var addressTV = itemView.findViewById<TextView>(R.id.dataCollectionCellAddressTV)
        var icon = itemView.findViewById<ImageView>(R.id.dataCollectionCellIcon)
    }

}
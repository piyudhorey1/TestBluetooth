package com.example.testbluetooth.adapter

import android.bluetooth.BluetoothDevice
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.testbluetooth.R

class OtherDevicesAdapter(private var devicesList: List<String>, private val listener: OnItemClickListener) :
    RecyclerView.Adapter<OtherDevicesAdapter.OtherDeviceViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(deviceName: String)
    }

    fun updateData(newDevices: List<String>) {
        devicesList = newDevices
        notifyDataSetChanged()
    }

    inner class OtherDeviceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.deviceName)

        fun bind(deviceName: String, listener: OnItemClickListener) {
            textView.text = deviceName
            itemView.setOnClickListener {
                listener.onItemClick(deviceName)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OtherDeviceViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_other_device,
            parent, false)
        return OtherDeviceViewHolder(view)
    }


    override fun onBindViewHolder(holder: OtherDeviceViewHolder, position: Int) {
        holder.bind(devicesList[position], listener)

    }

    override fun getItemCount(): Int = devicesList.size
}

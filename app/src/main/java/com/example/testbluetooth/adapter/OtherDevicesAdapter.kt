package com.example.testbluetooth.adapter

import android.bluetooth.BluetoothDevice
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.testbluetooth.R

class OtherDevicesAdapter(private val devicesList: List<BluetoothDevice>) :
    RecyclerView.Adapter<OtherDevicesAdapter.OtherDeviceViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OtherDeviceViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_other_device, parent, false)
        return OtherDeviceViewHolder(view)
    }

    override fun onBindViewHolder(holder: OtherDeviceViewHolder, position: Int) {
        val device = devicesList[position]
        holder.deviceName.text = device.name ?: "Unknown Device"
    }

    override fun getItemCount(): Int {
        return devicesList.size
    }

    class OtherDeviceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val deviceName: TextView = itemView.findViewById(R.id.deviceName)
    }
}
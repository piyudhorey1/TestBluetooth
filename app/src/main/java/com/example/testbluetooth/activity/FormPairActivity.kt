package com.example.testbluetooth.activity

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.CompoundButton
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.testbluetooth.R
import com.example.testbluetooth.adapter.OtherDevicesAdapter
import com.example.testbluetooth.databinding.ActivityFormPairBinding

class FormPairActivity : AppCompatActivity() {

    lateinit var binding: ActivityFormPairBinding
    val bluetoothAdapter: BluetoothAdapter? by lazy {
     BluetoothAdapter.getDefaultAdapter()
    }
    private val otherDevicesList = mutableListOf<BluetoothDevice>()
    private val otherDevicesAdapter = OtherDevicesAdapter(otherDevicesList)
    private val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFormPairBinding.inflate(layoutInflater)
        setContentView(binding.root)

        registerReceiver(receiver, filter, RECEIVER_NOT_EXPORTED)

        binding.rcyOtherDevices.layoutManager = LinearLayoutManager(this)
        binding.rcyOtherDevices.adapter = otherDevicesAdapter

        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Device doesn't support bluetooth", Toast.LENGTH_LONG).show()
        }
        binding.toggleBluetooth.setOnCheckedChangeListener { _: CompoundButton, isChecked: Boolean ->
            if (isChecked) {
                enableBluetooth()
            } else {
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) != PackageManager.PERMISSION_GRANTED
                ) {

                    return@setOnCheckedChangeListener
                }
                bluetoothAdapter?.disable()
                otherDevicesList.clear()
            }
        }
    }

    private val bluetoothActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            discoverDevices()
        } else {
            binding.toggleBluetooth.isChecked = false
            Toast.makeText(this, "Bluetooth is required for this app", Toast.LENGTH_SHORT).show()
        }
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (BluetoothDevice.ACTION_FOUND == intent?.action) {
                val device: BluetoothDevice? =
                    intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                device?.let {
                    otherDevicesList.add(it)
                    otherDevicesAdapter.notifyDataSetChanged()
                }
            }
        }
    }

    private fun enableBluetooth() {
        if (bluetoothAdapter?.isEnabled == false) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            bluetoothActivityResultLauncher.launch(enableBtIntent)
        }
        discoverDevices()
    }

    private fun discoverDevices() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
        } else {
            bluetoothAdapter?.startDiscovery()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
    }


}
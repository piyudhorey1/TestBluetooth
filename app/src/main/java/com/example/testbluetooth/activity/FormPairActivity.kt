package com.example.testbluetooth.activity

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.IntentCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.testbluetooth.adapter.OtherDevicesAdapter
import com.example.testbluetooth.databinding.ActivityFormPairBinding
import java.io.IOException

class FormPairActivity : AppCompatActivity(), OtherDevicesAdapter.OnItemClickListener {

    lateinit var binding: ActivityFormPairBinding
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private val pairedDevices = mutableListOf<BluetoothDevice>()
    private val discoveredDevices = mutableListOf<BluetoothDevice>()
    private val pairedDevicesAdapter = OtherDevicesAdapter(pairedDevices.map { it.name }, this)
    private val discoveredDevicesAdapter = OtherDevicesAdapter(discoveredDevices.map { it.name }, this)
    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFormPairBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
         bluetoothAdapter = bluetoothManager.adapter
            ?: return

            binding.rcyPairedDevices.adapter = pairedDevicesAdapter
            binding.rcyPairedDevices.layoutManager = LinearLayoutManager(this)
            binding.rcyOtherDevices.adapter = discoveredDevicesAdapter
            binding.rcyOtherDevices.layoutManager = LinearLayoutManager(this)


            showPairedDevices()

            val filter = IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
            registerReceiver(pairingReceiver, filter)

            startDiscovery()
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun showPairedDevices() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.BLUETOOTH_CONNECT), 1)
            return
        }
        val pairedDevicesList = bluetoothAdapter.bondedDevices.toList()
        pairedDevices.clear()
        pairedDevices.addAll(pairedDevicesList)
        val deviceNames = pairedDevices.map { it.name }
        pairedDevicesAdapter.updateData(deviceNames)

    }


//    @RequiresApi(Build.VERSION_CODES.S)
//    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        if (requestCode == PERMISSION_REQUEST_CODE) {
//            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
//                startDiscovery()
//                showPairedDevices()
//            } else {
//                Toast.makeText(this, "Permissions Denied", Toast.LENGTH_SHORT).show()
//            }
//        }
//    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun startDiscovery() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_SCAN
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.BLUETOOTH_SCAN), 1)
            return
        }
        bluetoothAdapter.startDiscovery()
        val discoveryIntentFilter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        registerReceiver(discoveryReceiver, discoveryIntentFilter)
        binding.otherProgressBar.visibility = View.VISIBLE
    }

    private val discoveryReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        @RequiresApi(Build.VERSION_CODES.S)
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (action == BluetoothDevice.ACTION_FOUND) {
                if (ActivityCompat.checkSelfPermission(
                        this@FormPairActivity,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    ActivityCompat.requestPermissions(
                        this@FormPairActivity,
                        arrayOf(Manifest.permission.BLUETOOTH_CONNECT),
                        1
                    )
                    return
                }
                val device = IntentCompat.getParcelableExtra(
                    intent,
                    BluetoothDevice.EXTRA_DEVICE,
                    BluetoothDevice::class.java
                )
                device?.let {
                    discoveredDevices.add(device)
                    discoveredDevicesAdapter.updateData(listOf(device.name))
                }
            } else if (action == BluetoothAdapter.ACTION_DISCOVERY_FINISHED) {
                Toast.makeText(context, "Discovery finished", Toast.LENGTH_SHORT).show()
                binding.otherProgressBar.visibility = View.GONE
            }
        }
    }


    private val pairingReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        @RequiresApi(Build.VERSION_CODES.S)
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (action == BluetoothDevice.ACTION_BOND_STATE_CHANGED) {
                val device = IntentCompat.getParcelableArrayExtra(
                    intent,
                    BluetoothDevice.EXTRA_DEVICE,
                    BluetoothDevice::class.java
                ) as? Array<BluetoothDevice> ?: emptyArray()

                device.forEach { bluetoothDevice ->
                    if (ActivityCompat.checkSelfPermission(
                            context,
                            Manifest.permission.BLUETOOTH_CONNECT
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        ActivityCompat.requestPermissions(this@FormPairActivity, arrayOf(Manifest.permission.BLUETOOTH_CONNECT),1)
                        return
                    }
                    when (bluetoothDevice.bondState) {
                        BluetoothDevice.BOND_BONDED -> {
                            Toast.makeText(
                                context,
                                "Paired with ${bluetoothDevice.name}",
                                Toast.LENGTH_SHORT
                            ).show()
                            showPairedDevices()
                        }

                        BluetoothDevice.BOND_BONDING -> {
                            Toast.makeText(
                                context,
                                "Pairing with ${bluetoothDevice.name}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        BluetoothDevice.BOND_NONE -> {
                            Toast.makeText(
                                context,
                                "Pairing failed with ${bluetoothDevice.name}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(discoveryReceiver)
            unregisterReceiver(pairingReceiver)
        } catch (e: IllegalArgumentException) {
            Log.e("FormPairActivity", "Receiver not registered", e)
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onItemClick(deviceName: String) {
        val device = pairedDevices.find { if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this@FormPairActivity, arrayOf(Manifest.permission.BLUETOOTH_CONNECT), 1)
            return
        }
            it.name == deviceName } ?: discoveredDevices.find { it.name == deviceName }
        device?.let {
            if (it.bondState == BluetoothDevice.BOND_BONDED) {
                connectToDevice(it)
            } else {
                it.createBond()
                Toast.makeText(this, "${it.name} is not paired", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun connectToDevice(device: BluetoothDevice) {
         if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
             ActivityCompat.requestPermissions(this@FormPairActivity, arrayOf(Manifest.permission.BLUETOOTH_CONNECT), 1)
             return
        }
        val uuid = device.uuids[0].uuid
        val socket: BluetoothSocket?
        try {
            socket = device.createRfcommSocketToServiceRecord(uuid)
            socket.connect()
            Toast.makeText(this, "Connected to ${device.name}", Toast.LENGTH_SHORT).show()
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this, "Connection failed to ${device.name}", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        private const val PERMISSION_REQUEST_CODE = 1
    }
}

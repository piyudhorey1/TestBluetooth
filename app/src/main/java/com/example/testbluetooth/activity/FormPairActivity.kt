package com.example.testbluetooth.activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.CompoundButton
import android.widget.ListView
import android.widget.Switch
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.testbluetooth.R
import com.example.testbluetooth.adapter.OtherDevicesAdapter
import com.example.testbluetooth.databinding.ActivityFormPairBinding

class FormPairActivity : AppCompatActivity(), OtherDevicesAdapter.OnItemClickListener {

    lateinit var binding: ActivityFormPairBinding
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var bluetoothSwitch: Switch
    private val pairedDevices = mutableListOf<String>()
    private val discoveredDevices = mutableListOf<BluetoothDevice>()
    private val pairedDevicesAdapter = OtherDevicesAdapter(discoveredDevices.map { it.name }, this)
    private val discoveredDevicesAdapter = OtherDevicesAdapter(discoveredDevices.map { it.name }, this)

    private val permissionRequestLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions.all { it.value }) {
                enableBluetooth()
            } else {
                Toast.makeText(this, "Permissions Denied", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFormPairBinding.inflate(layoutInflater)
        setContentView(binding.root)

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        binding.rcyPairedDevices.adapter = pairedDevicesAdapter
        binding.rcyPairedDevices.layoutManager = LinearLayoutManager(this)
        binding.rcyOtherDevices.adapter = discoveredDevicesAdapter
        binding.rcyOtherDevices.layoutManager = LinearLayoutManager(this)

        bluetoothSwitch = binding.toggleBluetooth

        bluetoothSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                enableBluetooth()
            } else {
                disableBluetooth()
            }
        }

        showPairedDevices()

        val filter = IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
        registerReceiver(pairingReceiver, filter)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun checkPermissions(): Boolean {
        val permissionsNeeded = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        } else {
            arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_FINE_LOCATION,
            )
     }

        return permissionsNeeded.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestPermissions() {
        val permissionsNeeded = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        } else {
            arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }

        permissionRequestLauncher.launch(permissionsNeeded)
    }

    private fun showPairedDevices() {
        val pairedDevicesList = bluetoothAdapter.bondedDevices.map { it.name }.toMutableList()
        val adapter = OtherDevicesAdapter(pairedDevicesList,this)
        binding.rcyPairedDevices.adapter = adapter
    }

    private fun disableBluetooth() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this@FormPairActivity, arrayOf(Manifest.permission.BLUETOOTH_CONNECT), 1)
            return
        }
        bluetoothAdapter.disable()
    }

    @SuppressLint("NewApi")
    private fun enableBluetooth() {
        if (!bluetoothAdapter.isEnabled) {
            if (checkPermissions()) {
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
            } else {
                requestPermissions()
            }
        } else {
            startDiscovery()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                enableBluetooth()
                showPairedDevices()
            } else {
                Toast.makeText(this, "Permissions Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun startDiscovery() {
        if (checkPermissions()) {
            bluetoothAdapter.startDiscovery()
            val discoveryIntentFilter = IntentFilter(BluetoothDevice.ACTION_FOUND)
            registerReceiver(discoveryReceiver, discoveryIntentFilter)
            binding.otherProgressBar.visibility = View.VISIBLE
        } else {
            requestPermissions()
        }
    }

    private val discoveryReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (action == BluetoothDevice.ACTION_FOUND)
            {
                val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                device?.let {
                    discoveredDevices.add(it)

                    discoveredDevicesAdapter.updateData(discoveredDevices.map { it.name })
                }
            } else if (action == BluetoothAdapter.ACTION_DISCOVERY_FINISHED) {
                Toast.makeText(context, "Discovery finished", Toast.LENGTH_SHORT).show()
                binding.otherProgressBar.visibility = View.GONE
            }
        }
    }

    private val pairingReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (action == BluetoothDevice.ACTION_BOND_STATE_CHANGED) {
                val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                when (device?.bondState) {
                    BluetoothDevice.BOND_BONDED -> {
                        Toast.makeText(context, "Paired with ${device.name}", Toast.LENGTH_SHORT).show()
                        showPairedDevices()
                    }
                    BluetoothDevice.BOND_BONDING -> {
                        Toast.makeText(context, "Pairing with ${device.name}", Toast.LENGTH_SHORT).show()
                    }
                    BluetoothDevice.BOND_NONE -> {
                        Toast.makeText(context, "Pairing failed with ${device.name}", Toast.LENGTH_SHORT).show()
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

    override fun onItemClick(deviceName: String) {
        val device = discoveredDevices.find { it.name == deviceName }
        device?.let {
            it.createBond()
        }
    }

    companion object {
        private const val REQUEST_ENABLE_BT = 1
        private const val PERMISSION_REQUEST_CODE = 1
    }
}

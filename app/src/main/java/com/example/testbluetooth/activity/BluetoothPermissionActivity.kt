package com.example.testbluetooth.activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.widget.Switch
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.testbluetooth.R
import com.example.testbluetooth.databinding.ActivityBluetoothPermissionBinding
import com.example.testbluetooth.databinding.ActivityFormPairBinding

class BluetoothPermissionActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBluetoothPermissionBinding

    @RequiresApi(Build.VERSION_CODES.S)
    private val permissionRequestLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions.all { it.value }) {
                enableBluetoothAndStartActivity()
            } else {
                Toast.makeText(this, "Permissions Denied", Toast.LENGTH_SHORT).show()
                binding.toggleBluetooth.isChecked = false
            }
        }

    private val enableBluetoothLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode
            == Activity.RESULT_OK)
        {
            startFormPairActivity()
        } else {
            Toast.makeText(this, "You need bluetooth permissions to use app", Toast.LENGTH_SHORT).show()
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBluetoothPermissionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toggleBluetooth.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                checkPermissions()
            } else {
                disableBluetooth()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun checkPermissions() {
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

        if (permissionsNeeded.all { ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED }) {
            enableBluetoothAndStartActivity()
        } else {
            permissionRequestLauncher.launch(permissionsNeeded)
        }
    }

    @SuppressLint("SuspiciousIndentation")
    @RequiresApi(Build.VERSION_CODES.S)
    private fun enableBluetoothAndStartActivity() {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val bluetoothAdapter = bluetoothManager.adapter
            ?: return

            if (!bluetoothAdapter.isEnabled) {
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                enableBluetoothLauncher.launch(enableBtIntent)
            } else {
                startFormPairActivity()
            }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun disableBluetooth() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionRequestLauncher.launch(arrayOf(Manifest.permission.BLUETOOTH_CONNECT))
            return
        }
        val intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
        startActivity(intent)
    }

    @RequiresApi(Build.VERSION_CODES.S)
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                enableBluetoothAndStartActivity()
            } else {
                Toast.makeText(this, "Bluetooth enabling failed", Toast.LENGTH_SHORT).show()
                binding.toggleBluetooth.isChecked = false
            }
        }
    }


    private fun startFormPairActivity() {
        val intent = Intent(this, FormPairActivity::class.java)
        startActivity(intent)
        finish()
    }

    companion object {
        private const val REQUEST_ENABLE_BT = 1
    }
}
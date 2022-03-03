package com.alfasreda.mobilecity.services

import android.Manifest
import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.app.Service
import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.alfasreda.mobilecity.R
import java.io.IOException
import java.util.*


const val SCAN_PERIOD: Long = 10000

class BluetoothService : Service() {

    companion object {
        private var listener: Listener? = null
        fun setListener(listener: Listener) {
            this.listener = listener
        }

        private var bluetoothAdapter: BluetoothAdapter? = null
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()

        val bluetoothManager = this.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Устройство не поддерживает блютуз", Toast.LENGTH_LONG).show()
            stopSelf()
        }

        bluetoothAdapter?.let { adapter ->
            initAdapter(adapter)
        }

    }

    @SuppressLint("MissingPermission")
    fun initAdapter(adapter: BluetoothAdapter) {
        if (!adapter.isEnabled) {
            listener?.isBluetoothEnabled(false)
        }
    }


    override fun onDestroy() {
        Toast.makeText(this, "Service is stopped", Toast.LENGTH_LONG).show()

        super.onDestroy()
    }

    interface Listener {
        fun isBluetoothEnabled(flag: Boolean)
        fun notPermission()
    }
    interface IDiscoverListener {
        fun onScanResult(callbackType: Int, result: ScanResult)
        fun onDiscoverBoundedDevices(devices: MutableSet<BluetoothDevice>)
    }








}
@file:Suppress("ObjectLiteralToLambda")

package com.alfasreda.mobilecity.repositories.bt

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import com.alfasreda.mobilecity.models.BtDevice
import com.alfasreda.mobilecity.utils.toUtf8ByteArray
import org.bouncycastle.util.Integers

open class BtRepository {

    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()

    private val bltAdvertiser = bluetoothAdapter?.bluetoothLeAdvertiser

    open val isSupportBluetooth: Boolean
        get() {
            return bluetoothAdapter != null
        }

    open val isEnabledBluetooth: Boolean
        get() {
            return bluetoothAdapter?.isEnabled ?: false
        }

    private val advSettings = AdvertiseSettings.Builder()
        .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
        .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
        .setConnectable(false)
        .build()

    @SuppressLint("MissingPermission")
    open fun startAdvertising(deviceId: String, listener: IBtAdvertisingListener) {

        val sid = (deviceId).substring(2)
        val bytes = sid.toUtf8ByteArray()
        val advData = AdvertiseData.Builder().addManufacturerData(0x4449, bytes).build()

        bltAdvertiser?.startAdvertising(advSettings, advData, object : AdvertiseCallback() {

            override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
                super.onStartSuccess(settingsInEffect)
                listener.onAdvertisingSuccess(settingsInEffect)
            }
            override fun onStartFailure(errorCode: Int) {
                super.onStartFailure(errorCode)
                listener.onAdvertisingError(errorCode)
            }
        })
        val s = 5.toString(2)
        s
    }

    @SuppressLint("MissingPermission")
    open fun startLowEnergyScan(listener: IBtScanListener) {
        bluetoothAdapter?.startLeScan(object : BluetoothAdapter.LeScanCallback {
                override fun onLeScan(p0: BluetoothDevice?, p1: Int, p2: ByteArray?) {
                    val device = BtDevice(p0, p1, p2)
                    listener.onLeScan(device)
                }
            })
    }

    @SuppressLint("MissingPermission")
    open fun stopLowEnergyScan(listener: IBtScanListener?) {
        bluetoothAdapter?.stopLeScan(object : BluetoothAdapter.LeScanCallback {
            override fun onLeScan(p0: BluetoothDevice?, p1: Int, p2: ByteArray?) {
                val device = BtDevice(p0, p1, p2)
                listener?.onLeScan(device)
            }
        })
    }

    interface IBtAdvertisingListener {
        fun onAdvertisingSuccess(settingsInEffect: AdvertiseSettings?)
        fun onAdvertisingError(errorCode: Int)
    }
    interface IBtScanListener {
        fun onLeScan(device: BtDevice)
    }
}
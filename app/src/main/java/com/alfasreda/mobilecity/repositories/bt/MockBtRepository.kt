@file:Suppress("ObjectLiteralToLambda")

package com.alfasreda.mobilecity.repositories.bt

import android.bluetooth.BluetoothAdapter
import com.alfasreda.mobilecity.models.BtDevice
import kotlinx.coroutines.*
import org.robolectric.shadows.ShadowBluetoothDevice

class MockBtRepository : BtRepository() {

    override val bluetoothAdapter: BluetoothAdapter? = super.bluetoothAdapter

    private var isScan = false

    override fun startAdvertising(listener: IBtAdvertisingListener) {
        super.startAdvertising(listener)
    }

    override fun startLowEnergyScan(listener: IBtScanListener) {

        isScan = true
        CoroutineScope(Dispatchers.IO).launch {

            while (isScan) {
                delay(200)
                withContext(Dispatchers.Main) {
                    devices.forEach {
                        listener.onLeScan(it)
                    }
                }
            }
        }
    }

    override fun stopLowEnergyScan(listener: IBtScanListener?) {
        super.stopLowEnergyScan(listener)

        isScan = false
    }

    private val byteArray = arrayOf(
        2,
        1,
        6,
        11,
        9,
        73,
        68,
        48,
        48,
        48,
        48,
        48,
        48,
        48,
        52,
        10,
        -1,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        5,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0
    ).map {
        it.toByte()
    }.toByteArray()

    private val devices = listOf<BtDevice>(
        BtDevice(
            device = ShadowBluetoothDevice.newInstance("24:6F:28:A9:93:3E"),
            rssi = -66,
            bytes = byteArray
        ),
        BtDevice(
            device = ShadowBluetoothDevice.newInstance("24:6F:28:A9:93:4B"),
            rssi = -55,
            bytes = byteArray
        )
    )
}
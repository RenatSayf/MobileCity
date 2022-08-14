@file:Suppress("ObjectLiteralToLambda")

package com.alfasreda.mobilecity.repositories.bt

import com.alfasreda.mobilecity.models.BtDevice
import kotlinx.coroutines.*

class MockBtRepository : BtRepository() {

    override val isSupportBluetooth: Boolean
        get() = true

    override val isEnabledBluetooth: Boolean
        get() = when {
            isSupportBluetooth -> true
            else -> false
        }

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
            rssi = -66,
            bytes = byteArray
        ),
        BtDevice(
            rssi = -55,
            bytes = byteArray
        )
    )
}
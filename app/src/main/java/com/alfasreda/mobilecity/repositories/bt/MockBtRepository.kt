@file:Suppress("ObjectLiteralToLambda")

package com.alfasreda.mobilecity.repositories.bt

import com.alfasreda.mobilecity.models.BtDevice
import com.alfasreda.mobilecity.models.busBytes
import com.alfasreda.mobilecity.models.pharmacyBytes
import com.alfasreda.mobilecity.models.tramBytes
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

    private val devices = listOf<BtDevice>(
        BtDevice(
            rssi = -66,
            bytes = pharmacyBytes
        ).apply {
                description = "Аптека Живика. Улица Челюскинцев, 19"
        },
        BtDevice(
            rssi = -55,
            bytes = pharmacyBytes
        ).apply {
            description = "Остановка трамвая. Ж/д вокзал"
        },
        BtDevice(
            rssi = -59,
            bytes = busBytes
        ).apply {
            description = "Автобус №56"
        },
        BtDevice(
            rssi = -51,
            bytes = tramBytes
        ).apply {
            description = "Трамвай №3"
        }
    )
}
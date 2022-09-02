@file:Suppress("ObjectLiteralToLambda")

package com.alfasreda.mobilecity.repositories.bt

import com.alfasreda.mobilecity.models.BtDevice
import com.alfasreda.mobilecity.models.mockBytes
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

    override fun startAdvertising(deviceId: String, listener: IBtAdvertisingListener) {
        super.startAdvertising(deviceId, listener)

        isScan = true
        devices.first {
            it.id == deviceId
        }.apply {
           this.call()
        }
    }

    override fun startLowEnergyScan(listener: IBtScanListener) {

        isScan = true
        CoroutineScope(Dispatchers.IO).launch {

            while (isScan) {
                withContext(Dispatchers.Main) {
                    devices.forEach { device ->
                        delay(200)
                        listener.onLeScan(
                            device.apply {
                                rssi = -(40..75).random()
                            })
                    }
                }
            }
        }
    }

    override fun stopLowEnergyScan(listener: IBtScanListener?) {
        super.stopLowEnergyScan(listener)

        isScan = false
    }

    private val devices = listOf(
        BtDevice(
            rssi = -66,
            bytes = mockBytes.clone().apply {
                set(7, 48) // device type city-object=48, bus=49, trolleybus=50, tram=51
                set(14, 49) // id = 49..57
                set(24, 5.toByte()) //sign of a call: city-object=53, transport=55
            }
        ).apply {
                description = "Аптека Живика. Улица Челюскинцев, 19"
        },
        BtDevice(
            rssi = -55,
            bytes = mockBytes.clone().apply {
                set(7, 48) // device type city-object=48, bus=49, trolleybus=50, tram=51
                set(14, 50) // id = 49..57
                set(24, 5.toByte()) //sign of a call: city-object=53, transport=55
            }
        ).apply {
            description = "Остановка трамвая. Ж/д вокзал"
        },
        BtDevice(
            rssi = -59,
            bytes = mockBytes.clone().apply {
                set(7, 49) // device type city-object=48, bus=49, trolleybus=50, tram=51
                set(14, 51) // id = 49..57
                set(24, 7.toByte()) //sign of a call: city-object=53, transport=55
                set(32, 53.toByte()) //route number 48..57
                set(33, 54.toByte()) //route number 48..57
            }
        ).apply {
            description = "Автобус №56"
        },
        BtDevice(
            rssi = -51,
            bytes = mockBytes.clone().apply {
                set(7, 51) // device type city-object=48, bus=49, trolleybus=50, tram=51
                set(14, 52) // id = 49..57
                set(24, 7.toByte()) //sign of a call: city-object=53, transport=55
                set(32, 48.toByte()) //route number 48..57
                set(33, 51.toByte()) //route number 48..57
            }
        ).apply {
            description = "Трамвай №3"
        },
        BtDevice(
            rssi = -45,
            bytes = mockBytes.clone().apply {
                set(7, 48.toByte()) // device type city-object=48, bus=49, trolleybus=50, tram=51
                set(14, 53.toByte()) // id = 49..57
                set(24, 5.toByte()) //sign of a call: city-object=53, transport=55
            }
        ).apply {
            description = "Супермаркет Пятёрочка. Челюскинцев, 23"
        },
        BtDevice(
            rssi = -63,
            bytes = mockBytes.clone().apply {
                set(7, 50.toByte()) // device type city-object=48, bus=49, trolleybus=50, tram=51
                set(14, 54.toByte()) // id = 49..57
                set(24, 7.toByte()) //sign of a call: city-object=53, transport=55
                set(32, 49.toByte()) //route number 48..57
                set(33, 50.toByte()) //route number 48..57
            }
        ).apply {
            description = "Троллейбус №12"
        }
    )
}
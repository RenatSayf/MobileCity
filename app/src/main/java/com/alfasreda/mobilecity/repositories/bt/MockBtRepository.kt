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
                                lastUpdateTime = System.currentTimeMillis()
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
            description = "Аптека Живика"
            address = "Улица Челюскинцев, 19"
            setObjectDescription("Аптека Живика\nУлица Челюскинцев, 19")
        },
        BtDevice(
            rssi = -55,
            bytes = mockBytes.clone().apply {
                set(7, 48) // device type city-object=48, bus=49, trolleybus=50, tram=51
                set(14, 50) // id = 49..57
                set(24, 5.toByte()) //sign of a call: city-object=53, transport=55
            }
        ).apply {
            description = "Остановка трамвая"
            address = "Ж/д вокзал"
            setObjectDescription("Остановка трамвая\nЖ/д вокзал")
        },
        BtDevice(
            rssi = -59,
            bytes = mockBytes.clone().apply {
                set(7, 49) // device type city-object=48, bus=49, trolleybus=50, tram=51
                set(14, 51) // id = 49..57
                set(24, 7.toByte()) //sign of a call: city-object=53, transport=55
            }
        ).apply {
            description = "Автобус №56"
            setObjectDescription("56")
        },
        BtDevice(
            rssi = -51,
            bytes = mockBytes.clone().apply {
                set(7, 51) // device type city-object=48, bus=49, trolleybus=50, tram=51
                set(14, 52) // id = 49..57
                set(24, 7.toByte()) //sign of a call: city-object=53, transport=55
            }
        ).apply {
            description = "Трамвай №3"
            setObjectDescription("3")
        },
        BtDevice(
            rssi = -45,
            bytes = mockBytes.clone().apply {
                set(7, 48.toByte()) // device type city-object=48, bus=49, trolleybus=50, tram=51
                set(14, 53.toByte()) // id = 49..57
                set(24, 5.toByte()) //sign of a call: city-object=53, transport=55
            }
        ).apply {
            description = "Супермаркет Пятёрочка"
            address = "Челюскинцев, 23"
            setObjectDescription("Супермаркет Пятёрочка\nЧелюскинцев, 23")
        },
        BtDevice(
            rssi = -63,
            bytes = mockBytes.clone().apply {
                set(7, 50.toByte()) // device type city-object=48, bus=49, trolleybus=50, tram=51
                set(14, 54.toByte()) // id = 49..57
                set(24, 7.toByte()) //sign of a call: city-object=53, transport=55
            }
        ).apply {
            description = "Троллейбус №12"
            setObjectDescription("12")
        }
    )
}
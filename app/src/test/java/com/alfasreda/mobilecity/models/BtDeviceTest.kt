package com.alfasreda.mobilecity.models

import org.junit.Assert
import org.junit.Test

class BtDeviceTest {

    private val btDeviceTransport = BtDevice(
        rssi = -59,
        bytes = mockBytes.clone().apply {
            set(7, 49) // device type city-object=48, bus=49, trolleybus=50, tram=51
            set(14, 51) // id = 49..57
            set(17, 19.toByte())
            set(18, 29.toByte())
            set(19, 236.toByte())
            set(20, 110.toByte())
            set(21, 4.toByte())
            set(22, 118.toByte())
            set(23, 200.toByte())
            set(24, 7.toByte()) //sign of a call: city-object=53, transport=55
            set(32, 53.toByte()) //route number 48..57
            set(33, 54.toByte()) //route number 48..57
        }
    ).apply {
        description = "Автобус №56"
    }

    @Test
    fun isDoorOpen_true() {

        val btDevice = btDeviceTransport.apply {
            bytes = this.bytes?.clone().apply {
                this!![24] = 7.toByte()
            }
        }
        val doorOpen = btDevice.isDoorOpen
        Assert.assertTrue(doorOpen)
    }

    @Test
    fun isDoorOpen_false() {
        val btDevice = btDeviceTransport.apply {
            bytes = this.bytes?.clone().apply {
                this!![24] = 3.toByte()
            }
        }
        val doorOpen = btDevice.isDoorOpen
        Assert.assertTrue(!doorOpen)
    }
}
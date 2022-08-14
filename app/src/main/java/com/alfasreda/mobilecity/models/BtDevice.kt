package com.alfasreda.mobilecity.models

import android.bluetooth.BluetoothDevice

data class BtDevice(
    val device: BluetoothDevice? = null,
    val rssi: Int,
    val bytes: ByteArray?
) {

    var description: String = ""

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BtDevice

        if (device != other.device) return false
        return true
    }

    override fun hashCode(): Int {
        return device.hashCode()
    }
}
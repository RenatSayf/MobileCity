package com.alfasreda.mobilecity.models

import android.bluetooth.BluetoothDevice

data class BtDevice(
    val device: BluetoothDevice?,
    val index: Int,
    val bytes: ByteArray?
) {

    var description: String = ""

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BtDevice

        if (device != other.device) return false
        if (index != other.index) return false
        if (!bytes.contentEquals(other.bytes)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = device.hashCode()
        result = 31 * result + index
        result = 31 * result + bytes.contentHashCode()
        return result
    }
}
@file:Suppress("MoveVariableDeclarationIntoWhen")

package com.alfasreda.mobilecity.models

import android.bluetooth.BluetoothDevice

data class BtDevice(
    val device: BluetoothDevice? = null,
    val rssi: Int,
    val bytes: ByteArray?
) {

    companion object {

        const val UNDEFINED = "UNDEFINED"
        const val CITY_OBJECT = "CITY_OBJECT"
        const val TRANSPORT = "TRANSPORT"
    }

    var description: String = ""

    val type: String
        get() {
            val value = bytes?.get(7)?.toInt()?.toChar() ?: ""
            return when(value) {
                '0' -> CITY_OBJECT
                '1' -> TRANSPORT
                '2' -> TRANSPORT
                '3' -> TRANSPORT
                '4' -> CITY_OBJECT
                else -> UNDEFINED
            }
        }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BtDevice

        if (device != other.device) return false
        if (rssi != other.rssi) return false
        if (!bytes.contentEquals(other.bytes)) return false
        if (description != other.description) return false
        return true
    }

    override fun hashCode(): Int {
        return device.hashCode()
    }
}
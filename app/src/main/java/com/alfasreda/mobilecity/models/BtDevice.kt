@file:Suppress("MoveVariableDeclarationIntoWhen")

package com.alfasreda.mobilecity.models

import android.bluetooth.BluetoothDevice
import kotlin.math.abs

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

    private val macAddress: String
        get() {
            return if (device != null) device.address else "00:00:00:${abs(rssi)}"
        }

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
        other as BtDevice
        return macAddress == other.macAddress
    }

    override fun hashCode(): Int {
        return device?.address.hashCode()
    }
}
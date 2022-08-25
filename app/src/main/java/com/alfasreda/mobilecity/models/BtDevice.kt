@file:Suppress("MoveVariableDeclarationIntoWhen")

package com.alfasreda.mobilecity.models

import android.bluetooth.BluetoothDevice
import com.alfasreda.mobilecity.BuildConfig
import com.alfasreda.mobilecity.utils.DataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs

data class BtDevice(
    val device: BluetoothDevice? = null,
    val rssi: Int,
    var bytes: ByteArray?
) {

    companion object {

        const val UNDEFINED = "UNDEFINED"
        const val CITY_OBJECT = "CITY_OBJECT"
        const val TRANSPORT = "TRANSPORT"
    }

    var description: String = ""

    val id: String
        get() {
            return try {
                bytes?.decodeToString(5, 15, true) ?: UNDEFINED
            } catch (e: Exception) {
                if (BuildConfig.DEBUG) e.printStackTrace()
                UNDEFINED
            }
        }

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

    fun call() {
        when(type) {
            CITY_OBJECT -> {
                bytes?.set(24, 37.toByte())
                CoroutineScope(Dispatchers.Default).launch {
                    delay(30000)
                    bytes?.set(24, 5.toByte())
                }
            }
            TRANSPORT -> {
                bytes?.set(24, 27.toByte())
                CoroutineScope(Dispatchers.Default).launch {
                    delay(30000)
                    bytes?.set(24, 7.toByte())
                }
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        other as BtDevice
        return id == other.id
    }

    override fun hashCode(): Int {
        return device?.address.hashCode()
    }
}
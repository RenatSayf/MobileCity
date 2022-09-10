@file:Suppress("MoveVariableDeclarationIntoWhen")

package com.alfasreda.mobilecity.models

import android.bluetooth.BluetoothDevice
import android.os.Handler
import android.os.Looper
import android.provider.SyncStateContract
import com.alfasreda.mobilecity.BuildConfig
import com.alfasreda.mobilecity.utils.DataSource
import com.alfasreda.mobilecity.utils.injectString
import kotlin.math.abs

data class BtDevice(
    val device: BluetoothDevice? = null,
    var rssi: Int,
    var bytes: ByteArray?,
    var lastUpdateTime: Long = System.currentTimeMillis()
) {

    companion object {

        const val UNDEFINED = "UNDEFINED"
        const val CITY_OBJECT = "CITY_OBJECT"
        const val TRANSPORT = "TRANSPORT"
        const val BUS = "BUS"
        const val TRAM = "TRAM"
        const val TROLLEYBUS = "TROLLEYBUS"
        const val TRAFFIC_LIGHT = "TRAFFIC_LIGHT"
    }

    val isDoorOpen: Boolean
        get() {
            val bitString = bytes?.get(24)?.toString(2)
            return try {
                val bit = bitString?.get(bitString.length - 3)
                bit == '1'
            } catch (e: IndexOutOfBoundsException) {
                false
            }
        }

    var description: String = ""

    val objectDescription: String
        get() {
            return bytes?.copyOfRange(26, bytes?.size ?: 26)?.toString(charset("Windows-1251")) ?: "Неизвестный объект"
        }

    fun setObjectDescription(description: String) {
        if (BuildConfig.DATA_SOURCE == DataSource.LOCALHOST) {
            bytes = bytes?.injectString(description, 26, charset("Windows-1251"))
        } else {
            throw IllegalStateException("********** This function is only used in test mode (LOCALHOST) *******************")
        }
    }

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
                '1' -> BUS
                '2' -> TROLLEYBUS
                '3' -> TRAM
                '4' -> TRAFFIC_LIGHT
                else -> UNDEFINED
            }
        }

    var address: String = ""

    val route: String
        get() {
            return try {
                val string = bytes?.decodeToString(26, 34, true)
                string?.replace("0", "") ?: ""
            } catch (e: Exception) {
                if (BuildConfig.DEBUG) e.printStackTrace()
                ""
            }
        }

    private var handler: Handler = Handler()

    fun call() {
        when (type) {
            CITY_OBJECT -> {
                bytes?.let {
                    it[24] = 37.toByte()
                }
                handler.postDelayed({
                    bytes?.let {
                        it[24] = 5.toByte()
                    }
                }, 30000)
            }
            BUS, TROLLEYBUS, TRAM -> {
                bytes?.let {
                    it[24] = 27.toByte()
                }
                handler.postDelayed({
                    bytes?.let {
                        it[24] = 7.toByte()
                    }
                }, 10000)
            }
        }
    }

    fun isCall(): Boolean = when (type) {
        CITY_OBJECT -> {
            bytes?.get(24) == 37.toByte()
        }
        BUS, TROLLEYBUS, TRAM -> {
            bytes?.get(24) == 27.toByte()
        }
        else -> false
    }


    override fun equals(other: Any?): Boolean {
        other as BtDevice
        return id == other.id
    }

    override fun hashCode(): Int {
        return this.id.hashCode()
    }
}
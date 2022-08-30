@file:Suppress("MoveVariableDeclarationIntoWhen")

package com.alfasreda.mobilecity.models

import android.bluetooth.BluetoothDevice
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.MutableLiveData
import com.alfasreda.mobilecity.BuildConfig
import kotlinx.coroutines.flow.MutableStateFlow
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
        const val BUS = "BUS"
        const val TRAM = "TRAM"
        const val TROLLEYBUS = "TROLLEYBUS"
        const val TRAFFIC_LIGHT = "TRAFFIC_LIGHT"
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

//    private val macAddress: String
//        get() {
//            return if (device != null) device.address else "00:00:00:${abs(rssi)}"
//        }

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

    private var handler: Handler = Handler(Looper.getMainLooper())

    fun call() {
        when {
            type == CITY_OBJECT -> {
                bytes?.let {
                    it[24] = 37.toByte()
                    isCallLiveData.value = true
                }
                handler.postDelayed({
                    bytes?.let {
                        it[24] = 5.toByte()
                        isCallLiveData.value = false
                    }
                }, 30000)
            }
            type == BUS || type == TROLLEYBUS || type == TRAM -> {
                bytes?.let {
                    it[24] = 27.toByte()
                    isCallLiveData.value = true
                }
                handler.postDelayed({
                    bytes?.let {
                        it[24] = 7.toByte()
                        isCallLiveData.value = false
                    }
                }, 10000)
            }
        }
    }

    val isCall: Boolean
        get() {
            return when(type) {
                CITY_OBJECT -> bytes?.get(24) == 37.toByte()
                TRANSPORT -> bytes?.get(24) == 27.toByte()
                else -> false
            }
        }

    val isCallLiveData = MutableLiveData(false).apply {
        when(type) {
            CITY_OBJECT -> value = bytes?.get(24) == 37.toByte()
            TRANSPORT -> value = bytes?.get(24) == 27.toByte()
        }
    }

    val rssiLiveData = MutableStateFlow(0)
    fun updateRSSI(rssi: Int) {
        rssiLiveData.value = rssi
    }

    override fun equals(other: Any?): Boolean {
        other as BtDevice
        return id == other.id
    }

    override fun hashCode(): Int {
        return device?.address.hashCode()
    }
}
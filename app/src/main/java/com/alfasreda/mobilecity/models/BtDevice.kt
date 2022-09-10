@file:Suppress("MoveVariableDeclarationIntoWhen")

package com.alfasreda.mobilecity.models

import android.bluetooth.BluetoothDevice
import android.os.Handler
import android.os.Looper
import com.alfasreda.mobilecity.BuildConfig
import com.alfasreda.mobilecity.utils.DataSource
import com.alfasreda.mobilecity.utils.injectString
import com.alfasreda.mobilecity.utils.toHexList
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
                    it[24] = 27.toByte()
                }
                handler.postDelayed({
                    bytes?.let {
                        it[24] = 5.toByte()
                    }
                }, 30000)
            }
            BUS, TROLLEYBUS, TRAM -> {
                bytes?.let {
                    it[24] = 37.toByte() //TODO уточнить какое значение должно быть для признака "Вызов принят" (27 или 37)
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
            val bitString = bytes?.get(24)?.toString(2)
            try {
                val bit = bitString?.get(bitString.length - 6)
                bit == '1'
            } catch (e: IndexOutOfBoundsException) {
                false
            }
        }
        BUS, TROLLEYBUS, TRAM -> {
            val bitString = bytes?.get(24)?.toString(2)
            try {
                val bit = bitString?.get(bitString.length - 6)
                bit == '1'
            } catch (e: IndexOutOfBoundsException) {
                false
            }
        }
        else -> false
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

    private fun sign(input: Long): Long {
        return if (input < 0x8000000) input else input or 0x7ffffff.inv()
    }

    private fun ltob(input: Byte): Long {
        var r = input.toLong()
        if (r < 0) r += 256
        return r
    }

    fun getCoordinate(): Coordinates? {

        val hexList = this.bytes?.copyOfRange(17, 24)?.toHexList() ?: listOf()

        return try {
            val n0e0 = hexList[0].substringAfterLast("x").toLong(radix = 16)
            val n1 = hexList[1].substringAfterLast("x").toLong(radix = 16)
            val n2 = hexList[2].substringAfterLast("x").toLong(radix = 16)
            val n3 = hexList[3].substringAfterLast("x").toLong(radix = 16)
            val e1 = hexList[4].substringAfterLast("x").toLong(radix = 16)
            val e2 = hexList[5].substringAfterLast("x").toLong(radix = 16)
            val e3 = hexList[6].substringAfterLast("x").toLong(radix = 16)

            //преобразование в long со знаком. n-широта, e-долгота:
            var n: Long = n0e0 shr 4 and 0xf
            var e: Long = n0e0 and 0xf
            n = n * 0x1000000 + ltob(n1.toByte()) * 0x10000 + ltob(n2.toByte()) * 0x100 + ltob(n3.toByte())
            e = e * 0x1000000 + ltob(e1.toByte()) * 0x10000 + ltob(e2.toByte()) * 0x100 + ltob(e3.toByte())
            n = sign(n)
            e = sign(e)

            //преобразование из фиксированной запятой в плавающую:
            var dn = n.toDouble()
            dn /= 0x8000000
            dn *= 180
            var de = e.toDouble()
            de /= 0x8000000
            de *= 180

            //выходные данные:
            //dn - широта, в градусах
            //de - долгота, в градусах
            Coordinates(dn, de)
        } catch (e: IndexOutOfBoundsException) {
            if (BuildConfig.DEBUG) e.printStackTrace()
            null
        }
    }

    override fun equals(other: Any?): Boolean {
        other as BtDevice
        return id == other.id
    }

    override fun hashCode(): Int {
        return this.id.hashCode()
    }
}
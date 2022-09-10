package com.alfasreda.mobilecity.utils

import com.alfasreda.mobilecity.models.mockBytes
import org.junit.Test

class ExtensionsKtTest {

    private val decBytesTransport = mockBytes.clone().apply {
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

    private val hexBytesTransport = mockBytes.clone().apply {
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
    }.toHexList().map {
        it.substringAfter("x")
    }

    @Test
    fun ruStringToByteArray() {
        val ruText = "Аптека Живика"
        val byteArray = stringToByteArray(ruText)
        val charList = byteArray.toCharList()
        val string = byteArray.toString(charset("Windows-1251"))
        string
    }

    @Test
    fun enStringToByteArray() {
        val enText = "Encodes the contents"
        val byteArray = stringToByteArray(enText)
        val charList = byteArray.toCharList()
        val string = byteArray.toString(Charsets.UTF_8)
        string
    }

    @Test
    fun injectStringToByteArray() {
        val ruText = "Аптека Живика\nУлица Челюскинцев, 19"
        val bytes = mockBytes.injectString(ruText, 26, charset("Windows-1251"))
        val string = bytes.copyOfRange(26, bytes.size).toString(charset("Windows-1251"))
        string
    }

    @Test
    fun toHexString() {
        var hexString = ""
        hexBytesTransport.forEach { item ->
            hexString += "$item "
        }
        hexString = hexString.trim()
        hexString
    }

    @Test
    fun toBitList() {
        val bitsList = hexBytesTransport.map {
            it.toInt(radix = 16)
        }.map {
            it.toString(2)
        }
        bitsList
    }
}
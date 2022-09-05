package com.alfasreda.mobilecity.utils

import com.alfasreda.mobilecity.models.mockBytes
import org.junit.Test

class ExtensionsKtTest {

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
}
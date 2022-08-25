package com.alfasreda.mobilecity.utils

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.alfasreda.mobilecity.databinding.ToolBarBinding
import com.google.android.material.snackbar.Snackbar
import java.nio.charset.Charset

fun Context.checkPermission(permission: String): Boolean {
    return ActivityCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
}

fun Fragment.checkPermission(permission: String): Boolean {
    return requireContext().checkPermission(permission)
}

fun View.showSnackBar(message: String, length: Int = Snackbar.LENGTH_LONG) {
    Snackbar.make(this, message, length).show()
}

fun Fragment.showSnackBar(message: String, length: Int = Snackbar.LENGTH_LONG) {
    requireView().showSnackBar(message, length)
}

fun Fragment.setUpToolBar(
    binding: ToolBarBinding,
    navIconResource: Int,
    iconContentDescription: String,
    title: String,
    titleContentDescription: String
) {
    with(binding){
        btnBackNavigation.setImageResource(navIconResource)
        btnBackNavigation.contentDescription = iconContentDescription
        tvToolBarTitle.apply {
            text = title
            contentDescription = titleContentDescription
            requestFocus()
        }
    }
}

fun ByteArray.toHexList(): List<String> {
    val list = this.map {
        String.format("0x%02X", it)
    }
    return list
}

fun ByteArray.toCharList(): List<Char> {
    val list = this.map {
        it.toInt().toChar()
    }
    return list
}

fun String.toUtf8ByteArray(): ByteArray {
    return this.toByteArray(Charsets.UTF_8)
}

fun sign(input: Long): Long {
    return if (input < 0x8000000) input else input or 0x7ffffff.inv()
}

fun ltob(input: Byte): Long {
    var r = input.toLong()
    if (r < 0) r += 256
    return r
}

val Context.appPref: SharedPreferences
    get() {
        return this.getSharedPreferences("APP_PREF", Context.MODE_PRIVATE)
    }
val Fragment.appPref: SharedPreferences
    get() {
        return requireContext().appPref
    }
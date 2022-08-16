package com.alfasreda.mobilecity.utils

import android.content.Context
import android.content.pm.PackageManager
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.alfasreda.mobilecity.databinding.ToolBarBinding
import com.google.android.material.snackbar.Snackbar

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
    iconResource: Int,
    iconContentDescription: String,
    title: String,
    titleContentDescription: String
) {
    with(binding){
        btnBackNavigation.setImageResource(iconResource)
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
package com.alfasreda.mobilecity.ui.main.adapters

import com.alfasreda.mobilecity.models.BtDevice

interface IBtDevicesAdapterListener {
    fun onAdapterPreviousBtnClick(position: Int)
    fun onAdapterNextBtnClick(position: Int)
    fun onAdapterBtnCallClick(device: BtDevice)
    fun onAdapterItemLongClick(description: String)
    fun onAdapterItemAttached(description: String)
    fun onAdapterItemsAdded(count: Int)
    fun onEmptyAdapter()
    fun onSignalReceived(deviceId: String, isCall: Boolean)
}
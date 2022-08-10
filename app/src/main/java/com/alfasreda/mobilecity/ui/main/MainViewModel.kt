@file:Suppress("ObjectLiteralToLambda")

package com.alfasreda.mobilecity.ui.main

import android.Manifest
import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.alfasreda.mobilecity.models.BtDevice
import com.alfasreda.mobilecity.utils.checkPermission
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainViewModel(private val app: Application) : AndroidViewModel(app) {

    sealed class State {
        object NotSupportBT: State()
        object BtIsOn: State()
        object BtIsOff: State()
        object NoScanPermission: State()
        data class ScanSuccess(val data: MutableList<BtDevice>): State()
        data class ScanFailure(val errorCode: Int): State()
    }

    private var _state = MutableStateFlow<State>(State.BtIsOff)
    val state: StateFlow<State> = _state
    fun setState(state: State) {
        _state.value = state
    }

    private val btDevices = mutableListOf<BtDevice>()
    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private val bltAdvertiser = bluetoothAdapter?.bluetoothLeAdvertiser

    private val sid = ("ID00000004").substring(2)
    private val namesSnd = sid.toByte()

    private val advData = AdvertiseData.Builder().addManufacturerData(0x4449, byteArrayOf(namesSnd)).build()

    private val advSettings = AdvertiseSettings.Builder()
        .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
        .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
        .setConnectable(false)
        .build()

    fun startAdvertising() {
        if (!app.checkPermission(Manifest.permission.BLUETOOTH_ADVERTISE)) {
            bltAdvertiser?.startAdvertising(advSettings, advData, object : AdvertiseCallback() {

                override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
                    super.onStartSuccess(settingsInEffect)

                    if (app.checkPermission(Manifest.permission.BLUETOOTH_SCAN)) {
                        viewModelScope.launch {
                            _state.emit(State.NoScanPermission)
                        }
                        return
                    }
                    bluetoothAdapter?.startLeScan(object : BluetoothAdapter.LeScanCallback {
                        override fun onLeScan(p0: BluetoothDevice?, p1: Int, p2: ByteArray?) {
                            val btDevice = BtDevice(p0, p1, p2)
                            btDevices.add(btDevice)
                            viewModelScope.launch {
                                _state.emit(State.ScanSuccess(btDevices))
                            }
                        }
                    })
                }
                override fun onStartFailure(errorCode: Int) {
                    super.onStartFailure(errorCode)

                    viewModelScope.launch {
                        _state.emit(State.ScanFailure(errorCode))
                    }
                }
            })
        }
    }

    fun stopAdvertising() {
        if (app.checkPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
            bluetoothAdapter?.stopLeScan(object : BluetoothAdapter.LeScanCallback {
                override fun onLeScan(p0: BluetoothDevice?, p1: Int, p2: ByteArray?) {

                }
            })
        }
    }

    fun initBluetooth() {
        if (bluetoothAdapter == null) {
            viewModelScope.launch {
                _state.emit(State.NotSupportBT)
            }
        }
        else if (!bluetoothAdapter.isEnabled) {
            viewModelScope.launch {
                _state.emit(State.BtIsOff)
            }
        }
        else {
            viewModelScope.launch {
                _state.emit(State.BtIsOn)
            }
        }
    }


}
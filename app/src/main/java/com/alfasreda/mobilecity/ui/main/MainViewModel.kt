@file:Suppress("ObjectLiteralToLambda")

package com.alfasreda.mobilecity.ui.main

import android.Manifest
import android.annotation.SuppressLint
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

    sealed class BtState {
        object NotSupportBT: BtState()
        object BtIsOn: BtState()
        object BtIsOff: BtState()
        object NoScanPermission: BtState()
        data class NoPermission(val permission: String): BtState()
        data class ScanSuccess(val data: MutableSet<BtDevice>): BtState()
        data class ScanFailure(val errorCode: Int): BtState()
    }

    private var _btState = MutableStateFlow<BtState>(BtState.BtIsOff)
    val btState: StateFlow<BtState> = _btState
    fun setBtState(btState: BtState) {
        _btState.value = btState
    }

    sealed class ScreenState {
        object NothingMode: ScreenState()
        object CityMode: ScreenState()
        object TransportMode: ScreenState()
    }

    private var _screenState = MutableStateFlow<ScreenState>(ScreenState.NothingMode)
    val screenState: StateFlow<ScreenState> = _screenState
    fun setScreenState(state: ScreenState) {
        _screenState.value = state
    }

    private val btDevices = mutableSetOf<BtDevice>()
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

    @SuppressLint("MissingPermission")
    fun startAdvertising() {
        bltAdvertiser?.startAdvertising(advSettings, advData, object : AdvertiseCallback() {

            override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
                super.onStartSuccess(settingsInEffect)

                bluetoothAdapter?.startLeScan(object : BluetoothAdapter.LeScanCallback {
                    override fun onLeScan(p0: BluetoothDevice?, p1: Int, p2: ByteArray?) {
                        val btDevice = BtDevice(p0, p1, p2)
                        btDevices.add(btDevice)
                        viewModelScope.launch {
                            _btState.value = BtState.ScanSuccess(btDevices)
                        }
                    }
                })
            }
            override fun onStartFailure(errorCode: Int) {
                super.onStartFailure(errorCode)

                viewModelScope.launch {
                    _btState.value = BtState.ScanFailure(errorCode)
                }
            }
        })
    }

    fun stopAdvertising() {
        if (app.checkPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
            bluetoothAdapter?.stopLeScan(object : BluetoothAdapter.LeScanCallback {
                override fun onLeScan(p0: BluetoothDevice?, p1: Int, p2: ByteArray?) {
                    p0
                    p1
                    p2
                }
            })
        }
    }

    @SuppressLint("MissingPermission")
    fun startScan() {
        bluetoothAdapter?.startLeScan(
        object : BluetoothAdapter.LeScanCallback {
            override fun onLeScan(p0: BluetoothDevice?, p1: Int, p2: ByteArray?) {
                val btDevice = BtDevice(p0, p1, p2)
                btDevices.add(btDevice)
                viewModelScope.launch {
                    _btState.value = BtState.ScanSuccess(btDevices)
                }
            }
        })
    }

    fun initBluetooth() {
        if (bluetoothAdapter == null) {
            viewModelScope.launch {
                _btState.value = BtState.NotSupportBT
            }
        }
        else if (!bluetoothAdapter.isEnabled) {
            viewModelScope.launch {
                _btState.value = BtState.BtIsOff
            }
        }
        else {
            viewModelScope.launch {
                _btState.value = BtState.BtIsOn
            }
        }
    }


}
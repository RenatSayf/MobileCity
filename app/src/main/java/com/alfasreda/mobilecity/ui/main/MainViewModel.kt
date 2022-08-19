@file:Suppress("ObjectLiteralToLambda")

package com.alfasreda.mobilecity.ui.main

import android.annotation.SuppressLint
import android.bluetooth.le.AdvertiseSettings
import androidx.lifecycle.*
import com.alfasreda.mobilecity.di.BtRepositoryModule
import com.alfasreda.mobilecity.models.BtDevice
import com.alfasreda.mobilecity.repositories.bt.BtRepository
import com.alfasreda.mobilecity.utils.toCharList
import com.alfasreda.mobilecity.utils.toHexList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainViewModel(
    private val btRepository: BtRepository
) : ViewModel() {

    @Suppress("UNCHECKED_CAST")
    class Factory : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass == MainViewModel::class.java)
            return MainViewModel(btRepository = BtRepositoryModule.provide()) as T
        }
    }

    sealed class BtState {
        object Undefined: BtState()
        object NotSupportBT: BtState()
        object BtIsOn: BtState()
        object BtIsOff: BtState()
        object NoScanPermission: BtState()
        data class NoPermission(val permission: String): BtState()
        data class ScanSuccess(val data: MutableSet<BtDevice>): BtState()
        data class ScanFailure(val errorCode: Int): BtState()
    }

    val btDevices = mutableSetOf<BtDevice>()

    private var _btState = MutableLiveData<BtState>(BtState.Undefined)
    val btState: LiveData<BtState> = _btState
    fun setBtState(btState: BtState) {
        _btState.value = btState
    }

    sealed class ScreenState {
        object NothingMode: ScreenState()
        data class CityMode(val devices: Set<BtDevice>): ScreenState()
        data class TransportMode(val devices: Set<BtDevice>): ScreenState()
    }

    private var _screenState = MutableStateFlow<ScreenState>(ScreenState.NothingMode)
    val screenState: StateFlow<ScreenState> = _screenState
    fun setScreenState(state: ScreenState) {
        _screenState.value = state
    }

    @SuppressLint("MissingPermission")
    fun startAdvertising() {

        btRepository.startAdvertising(object : BtRepository.IBtAdvertisingListener {

            override fun onAdvertisingSuccess(settingsInEffect: AdvertiseSettings?) {

                btRepository.startLowEnergyScan(object : BtRepository.IBtScanListener {
                    override fun onLeScan(device: BtDevice) {
                        if (!btDevices.contains(device)) {
                            btDevices.add(device)
                            viewModelScope.launch {
                                _btState.value = BtState.ScanSuccess(btDevices)
                            }
                        }
                    }
                })
            }
            override fun onAdvertisingError(errorCode: Int) {

                btRepository.stopLowEnergyScan(null)
                viewModelScope.launch {
                    _btState.value = BtState.ScanFailure(errorCode)
                }
            }
        })
    }

    @SuppressLint("MissingPermission")
    fun stopBtScan() {

        btRepository.stopLowEnergyScan(object : BtRepository.IBtScanListener {
            override fun onLeScan(device: BtDevice) {

            }
        })
    }

    @SuppressLint("MissingPermission")
    fun startBtScan() {

        btRepository.startLowEnergyScan(object : BtRepository.IBtScanListener {
            override fun onLeScan(device: BtDevice) {

                if (!btDevices.contains(device)) {
                    val bytes = device.bytes
                    val charList = bytes?.toCharList()
                    val hexList = bytes?.toHexList()
                    btDevices.add(device)
                    viewModelScope.launch {
                        _btState.value = BtState.ScanSuccess(btDevices)
                    }
                }
            }
        })
    }

    fun initBluetooth() {
        //val adapter = btRepository.bluetoothAdapter
        if (!btRepository.isSupportBluetooth) {
            viewModelScope.launch {
                _btState.value = BtState.NotSupportBT
            }
        }
        else if (!btRepository.isEnabledBluetooth) {
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

    override fun onCleared() {

        btRepository.stopLowEnergyScan(null)

        super.onCleared()
    }
}
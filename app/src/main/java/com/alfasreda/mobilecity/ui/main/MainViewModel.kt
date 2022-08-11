@file:Suppress("ObjectLiteralToLambda")

package com.alfasreda.mobilecity.ui.main

import android.annotation.SuppressLint
import android.bluetooth.le.AdvertiseSettings
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.alfasreda.mobilecity.di.BtRepositoryModule
import com.alfasreda.mobilecity.models.BtDevice
import com.alfasreda.mobilecity.repositories.bt.BtRepository
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

    @SuppressLint("MissingPermission")
    fun startAdvertising() {

        btRepository.startAdvertising(object : BtRepository.IBtAdvertisingListener {

            override fun onAdvertisingSuccess(settingsInEffect: AdvertiseSettings?) {

                btRepository.startLowEnergyScan(object : BtRepository.IBtScanListener {
                    override fun onLeScan(device: BtDevice) {
                        btDevices.add(device)
                        viewModelScope.launch {
                            _btState.value = BtState.ScanSuccess(btDevices)
                        }
                    }
                })
            }
            override fun onAdvertisingError(errorCode: Int) {

                btRepository.stopLowEnergyScan(object : BtRepository.IBtScanListener {
                    override fun onLeScan(device: BtDevice) {}
                })
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
                btDevices.add(device)
                viewModelScope.launch {
                    _btState.value = BtState.ScanSuccess(btDevices)
                }
            }
        })
    }

    fun initBluetooth() {
        val adapter = btRepository.bluetoothAdapter
        if (adapter == null) {
            viewModelScope.launch {
                _btState.value = BtState.NotSupportBT
            }
        }
        else if (!adapter.isEnabled) {
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
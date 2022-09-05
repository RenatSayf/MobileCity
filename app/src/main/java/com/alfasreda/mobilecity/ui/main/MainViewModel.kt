@file:Suppress("ObjectLiteralToLambda")

package com.alfasreda.mobilecity.ui.main

import android.annotation.SuppressLint
import android.bluetooth.le.AdvertiseSettings
import androidx.lifecycle.*
import com.alfasreda.mobilecity.di.BtRepositoryModule
import com.alfasreda.mobilecity.models.BtDevice
import com.alfasreda.mobilecity.repositories.bt.BtRepository
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
        object NoLocationPermission: BtState()
        object NotSupportBT: BtState()
        object BtIsOn: BtState()
        object BtIsOff: BtState()
        object NoBtPermission: BtState()
        data class PermissionDenied(val permission: String): BtState()
        object StartScan: BtState()
        data class ScanSuccess(val data: MutableSet<BtDevice>): BtState()
        data class ScanFailure(val errorCode: Int): BtState()
        data class UpdateData(val device: BtDevice): BtState()
        object EmptyData: BtState()
    }

    val btDevices = mutableSetOf<BtDevice>()

    private var _btState = MutableLiveData<BtState>(BtState.NoLocationPermission)
    val btState: LiveData<BtState> = _btState
    fun setBtState(btState: BtState) {
        _btState.value = btState
    }

    enum class DisplayMode {
        Grid, List
    }
    sealed class ScreenState {
        object Init: ScreenState()
        data class AllObjectsMode(val mode: DisplayMode): ScreenState()
        data class CityMode(val mode: DisplayMode): ScreenState()
        data class TransportMode(val mode: DisplayMode): ScreenState()
        object Disable: ScreenState()
    }

    private var _screenState = MutableLiveData<ScreenState>(ScreenState.Init)
    val screenState: LiveData<ScreenState> = _screenState
    fun setScreenState(state: ScreenState) {
        _screenState.value = state
    }

    fun setDisplayMode(mode: DisplayMode) {
        when(_screenState.value) {
            is ScreenState.CityMode -> {
                _screenState.value = ScreenState.CityMode(mode)
            }
            is ScreenState.TransportMode -> {
                _screenState.value = ScreenState.TransportMode(mode)
            }
            is ScreenState.AllObjectsMode -> {
                _screenState.value = ScreenState.AllObjectsMode(mode)
            }
            else -> {}
        }
    }

    fun getDisplayMode(): DisplayMode {
        return when (val value = _screenState.value) {
            is ScreenState.CityMode -> value.mode
            is ScreenState.TransportMode -> value.mode
            is ScreenState.AllObjectsMode -> value.mode
            else -> DisplayMode.Grid
        }
    }

    @SuppressLint("MissingPermission")
    fun startAdvertising(deviceId: String) {

        btRepository.startAdvertising(deviceId, object : BtRepository.IBtAdvertisingListener {

            override fun onAdvertisingSuccess(settingsInEffect: AdvertiseSettings?) {

                btRepository.startLowEnergyScan(object : BtRepository.IBtScanListener {
                    override fun onLeScan(device: BtDevice) {
                        btDevices.first {
                            it.id == device.id
                        }.apply {
                            bytes = device.bytes
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
                device
            }
        })
    }

    @SuppressLint("MissingPermission")
    fun startBtScan() {

        _btState.value = BtState.StartScan
        btRepository.startLowEnergyScan(object : BtRepository.IBtScanListener {
            override fun onLeScan(device: BtDevice) {

                if (!btDevices.contains(device)) {
                    btDevices.add(device)
                    viewModelScope.launch {
                        _btState.postValue(BtState.ScanSuccess(btDevices))
                    }
                }
                else {
                    val btDevice = btDevices.first {
                        it.id == device.id
                    }.apply {
                        this.rssi = device.rssi
                    }
                    _btState.value = BtState.UpdateData(btDevice)
                }
            }
        })
    }

    fun initBluetooth() {

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
@file:Suppress("ObjectLiteralToLambda")

package com.alfasreda.mobilecity.ui.main

import android.annotation.SuppressLint
import android.bluetooth.le.AdvertiseSettings
import android.os.CountDownTimer
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
        data class DeviceMissing(val device: BtDevice): BtState()
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
        object Disabled: ScreenState()
    }

    private var _screenState = MutableLiveData<ScreenState>(ScreenState.Disabled)
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

                if (countDownTimer == null) {
                    countDownTimer = createRemovingTimer(::removeNotActiveDevice)
                    countDownTimer?.start()
                }
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
                        this.lastUpdateTime = device.lastUpdateTime
                    }
                    _btState.value = BtState.UpdateData(btDevice)
                }
            }
        })
    }

    private var countDownTimer: CountDownTimer? = null

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

    private fun removeNotActiveDevice() {

        val listToRemove = btDevices.filter {
            System.currentTimeMillis() - it.lastUpdateTime > 5000
        }
        listToRemove.forEach { item ->
            val isRemoved = btDevices.remove(item)
            if (isRemoved) {
                _btState.value = BtState.DeviceMissing(item)
            }
        }
        if (btDevices.isEmpty()) {
            _btState.value = BtState.EmptyData
            countDownTimer = null
        }
        countDownTimer?.start()
    }

    private fun createRemovingTimer(lambda: () -> Unit): CountDownTimer {
        val countDownTimer = object : CountDownTimer(5000, 1000) {
            override fun onTick(millisUntilFinished: Long) {}

            override fun onFinish() {
                lambda.invoke()
            }
        }
        return countDownTimer
    }

    override fun onCleared() {

        btRepository.stopLowEnergyScan(null)
        countDownTimer?.cancel()

        super.onCleared()
    }
}
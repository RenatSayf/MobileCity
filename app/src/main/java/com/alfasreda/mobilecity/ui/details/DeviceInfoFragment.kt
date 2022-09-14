@file:Suppress("MoveVariableDeclarationIntoWhen")

package com.alfasreda.mobilecity.ui.details

import android.annotation.SuppressLint
import android.os.Bundle
import android.speech.tts.UtteranceProgressListener
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.alfasreda.mobilecity.databinding.FragmentDeviceInfoBinding
import com.alfasreda.mobilecity.models.BtDevice
import com.alfasreda.mobilecity.models.enums.TrafficLightState
import com.alfasreda.mobilecity.ui.main.MainViewModel
import com.alfasreda.mobilecity.ui.main.SpeechViewModel

class DeviceInfoFragment : Fragment() {

    companion object {
        const val KEY_ID = "KEY_ID"
    }

    private lateinit var binding: FragmentDeviceInfoBinding

    private val mainVM: MainViewModel by activityViewModels()
    private val speechVM: SpeechViewModel by activityViewModels()
    private val deviceVM: DeviceInfoViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDeviceInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val deviceId = arguments?.getString(KEY_ID)

        mainVM.btState.observe(viewLifecycleOwner) { state ->
            when(state) {
                MainViewModel.BtState.EmptyData -> {

                }
                is MainViewModel.BtState.ScanFailure -> {

                }
                is MainViewModel.BtState.ScanSuccess -> {
                    val btDevice = state.data.find {
                        it.id == deviceId
                    }
                    if (btDevice == null) {
                        val message = "Связь с объектом потеряна. Переходим к списку объектов"
                        speechVM.autoSpeak(message, speakId = "XXXX", listener = object : UtteranceProgressListener() {
                            override fun onStart(utteranceId: String?) {}

                            override fun onDone(utteranceId: String?) {
                                if (utteranceId == "XXXX") {
                                    findNavController().popBackStack()
                                }
                            }
                            override fun onError(utteranceId: String?) {
                                if (utteranceId == "XXXX") {
                                    findNavController().popBackStack()
                                }
                            }
                        })
                    }
                }
                is MainViewModel.BtState.UpdateData -> {
                    handleData(state.device, deviceId)
                }
                else -> {}
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun handleData(device: BtDevice, id: String?) {
        with(binding) {

            if (device.id == id) {
                var objectName = ""
                var contentDescription = ""
                btnCall.visibility = View.VISIBLE

                when(device.type) {
                    BtDevice.BUS -> {
                        objectName = "Автобус\nМаршрут № ${device.objectDescription}"
                    }
                    BtDevice.TROLLEYBUS -> {
                        objectName = "Троллейбус\nМаршрут № ${device.objectDescription}"
                    }
                    BtDevice.TRAM -> {
                        objectName = "Трамвай\nМаршрут № ${device.objectDescription}"
                    }
                }

                when(device.type) {
                    BtDevice.TRAFFIC_LIGHT -> {
                        btnCall.visibility = View.GONE
                        val color = device.trafficLightColor
                        val textColor = when (color) {
                            TrafficLightState.Red -> "Красный"
                            TrafficLightState.Yellow -> "Желтый"
                            TrafficLightState.Green -> "Зеленый"
                            else -> "Неизвестен"
                        }
                        contentDescription = "$objectName. Сигнал $textColor"
                        objectName = "Светофор\n${device.objectDescription}\n$textColor"
                    }
                    BtDevice.CITY_OBJECT -> {
                        btnCall.contentDescription = "Найти вход"
                        btnCall.text = btnCall.contentDescription
                        objectName = device.objectDescription ?: ""
                        contentDescription = objectName
                    }
                    BtDevice.BUS, BtDevice.TROLLEYBUS, BtDevice.TRAM -> {
                        btnCall.contentDescription = "Подать сигнал водителю"
                        btnCall.text = "Вызвать"
                        val doorState = if (device.isDoorOpen) "Дверь открыта" else "Дверь закрыта"
                        contentDescription = "${tvObjectName.text}. $doorState"
                    }
                }

                tvObjectName.text = objectName
                tvObjectName.contentDescription = contentDescription

                tvDeviceId.text = device.id
                tvMacAddress.text = device.macAddress
                tvLatitude.text = device.getCoordinate()?.latitude.toString()
                tvLongitude.text = device.getCoordinate()?.longitude.toString()
                tvRssiValue.text = "${device.rssi} dB"

                btnCall.setOnClickListener {
                    mainVM.startAdvertising(id)
                    speechVM.autoSpeak("Вызываю")
                }

                tvObjectName.setOnLongClickListener {
                    speechVM.speak(it.contentDescription.toString())
                    true
                }

                if (device.isCall() && !deviceVM.isCalled) {
                    deviceVM.isCalled = true
                    speechVM.autoSpeak("Вызов принят")
                }
                if (!device.isCall() && deviceVM.isCalled) {
                    deviceVM.isCalled = false
                }

                tvObjectName.isSelected = device.isCall()
            }
        }
    }

    override fun onResume() {
        super.onResume()

        with(binding) {

            toolBar.setNavigationOnClickListener {
                findNavController().popBackStack()
            }

            btnBack.setOnClickListener {
                findNavController().popBackStack()
            }
        }
    }

}
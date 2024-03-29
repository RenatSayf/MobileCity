@file:Suppress("ObjectLiteralToLambda", "MoveVariableDeclarationIntoWhen")

package com.alfasreda.mobilecity.ui.main

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Bundle
import android.speech.tts.UtteranceProgressListener
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.view.forEach
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.airbnb.paris.extensions.style
import com.alfasreda.mobilecity.BuildConfig
import com.alfasreda.mobilecity.R
import com.alfasreda.mobilecity.databinding.MainFragmentBinding
import com.alfasreda.mobilecity.models.BtDevice
import com.alfasreda.mobilecity.ui.main.adapters.BtDeviceListAdapter
import com.alfasreda.mobilecity.ui.main.adapters.BtDevicePageAdapter
import com.alfasreda.mobilecity.ui.main.adapters.IBtDevicesAdapterListener
import com.alfasreda.mobilecity.ui.splash.KEY_FIRST_RUN
import com.alfasreda.mobilecity.utils.*
import com.fondesa.kpermissions.allGranted
import com.fondesa.kpermissions.extension.permissionsBuilder
import com.fondesa.kpermissions.extension.send


class MainFragment : Fragment(), IBtDevicesAdapterListener {

    companion object {
        const val ARG_SPEECH = "ARG_SPEECH"
    }

    private lateinit var binding: MainFragmentBinding

    private val speechVM: SpeechViewModel by activityViewModels()

    private val mainVM: MainViewModel by activityViewModels(factoryProducer = {
        MainViewModel.Factory()
    })

    private var pageAdapter: BtDevicePageAdapter? = null

    private var listAdapter: BtDeviceListAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = MainFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        appPref.edit().putBoolean(KEY_FIRST_RUN, false).apply()
        arguments?.let { bundle ->
            val res = bundle.getInt(ARG_SPEECH)
            when(res) {
                Speech.INIT_ERROR -> {
                    showSnackBar("Синтез речи не подерживается")
                }
                Speech.LANG_MISSING_DATA -> {
                    showSnackBar("Не установлен языковой пакет. Перейдите в настройки и установите яз. пакет")
                }
                Speech.LANG_NOT_SUPPORTED -> {
                    showSnackBar("Синтез речи для этого языка не поддерживается")
                }
            }
        }

        with(binding) {

            setUpToolBar(
                binding = includeAppBar,
                navIconResource = R.drawable.ic_hamburger_white,
                iconContentDescription = "Перейти в главное меню",
                title = "Главная",
                titleContentDescription = "Это главный экран приложения"
                )

            includeAppBar.btnBackNavigation.setOnClickListener {
                findNavController().navigate(R.id.action_mainFragment_to_menuFragment)
            }

            btnToList.setOnClickListener {
                mainVM.setDisplayMode(MainViewModel.DisplayMode.List)
            }

            btnToPage.setOnClickListener {
                mainVM.setDisplayMode(MainViewModel.DisplayMode.Page)
            }

            btnCityObjects.setOnClickListener {
                speechVM.autoSpeak("Режим городские объекты")
                mainVM.setScreenState(MainViewModel.ScreenState.CityMode(mainVM.getDisplayMode()))
            }

            btnTransport.setOnClickListener {
                speechVM.autoSpeak("Режим транспорт")
                mainVM.setScreenState(MainViewModel.ScreenState.TransportMode(mainVM.getDisplayMode()))
            }

            tvMessage.setOnClickListener {
                if (mainVM.btState.value == MainViewModel.BtState.PermissionDenied(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    mainVM.setBtState(MainViewModel.BtState.NoLocationPermission)
                }
                val message = (it as TextView).text
                speechVM.autoSpeak(message.toString())
            }

            mainVM.btState.observe(viewLifecycleOwner) { state ->

                when (state) {
                    MainViewModel.BtState.BtIsOff -> {
                        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                        startActivityForResult(enableBtIntent, 1111)
                    }
                    MainViewModel.BtState.BtIsOn -> {
                        binding.btnCityObjects.isEnabled = true
                        binding.btnTransport.isEnabled = true
                        mainVM.setScreenState(MainViewModel.ScreenState.NothingMode)
                    }
                    MainViewModel.BtState.NotSupportBT -> {
                        val message = "Устройство не поддерживает блютуз"
                        speechVM.autoSpeak(message)
                        showBtDeviceList(isList = false, isPage = false, isProgress = false, message = message)
                    }
                    MainViewModel.BtState.NoBtPermission -> {
                        val message = "Для работы приложения требуется включение блютуз. Нажмите что бы продолжить."
                        speechVM.autoSpeak(message)
                        showBtDeviceList(isList = false, isPage = false, isProgress = false, message = message)
                    }
                    is MainViewModel.BtState.ScanFailure -> {
                        progressBar.visibility = View.GONE
                        val message = "Ошибка блютуз сканирования. Код ${state.errorCode}"
                        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
                    }
                    is MainViewModel.BtState.ScanSuccess -> {
                        progressBar.visibility = View.GONE
                        val data = state.data.toList()
                        val screenState = mainVM.screenState.value
                        when(screenState) {
                            is MainViewModel.ScreenState.CityMode -> {
                                val filteredData = data.filter {
                                    it.type == BtDevice.CITY_OBJECT
                                }
                                when(screenState.mode) {
                                    MainViewModel.DisplayMode.Page -> {
                                        if (filteredData.isNotEmpty()) {
                                            pageAdapter?.addItems(filteredData)
                                            showBtDeviceList(isList = false, isPage = true, isProgress = false, message = null)
                                        }
                                        else showBtDeviceList(isList = false, isPage = false, isProgress = false, message = getString(R.string.no_visible_objects))
                                    }
                                    MainViewModel.DisplayMode.List -> {
                                        if (filteredData.isNotEmpty()) {
                                            listAdapter?.addItems(filteredData)
                                            showBtDeviceList(isList = true, isPage = false, isProgress = false, message = null)
                                        }
                                        else showBtDeviceList(isList = false, isPage = false, isProgress = false, message = getString(R.string.no_visible_objects))
                                    }
                                }
                            }
                            is MainViewModel.ScreenState.TransportMode -> {
                                val filteredData = data.filter {
                                    it.type == BtDevice.BUS || it.type == BtDevice.TROLLEYBUS || it.type == BtDevice.TRAM
                                }
                                when(screenState.mode) {
                                    MainViewModel.DisplayMode.Page -> {
                                        if (filteredData.isNotEmpty()) {
                                            pageAdapter?.addItems(filteredData)
                                            showBtDeviceList(isList = false, isPage = true, isProgress = false, message = null)
                                        }
                                        else showBtDeviceList(isList = false, isPage = false, isProgress = false, message = getString(R.string.no_visible_objects))
                                    }
                                    MainViewModel.DisplayMode.List -> {
                                        if (filteredData.isNotEmpty()) {
                                            listAdapter?.addItems(filteredData)
                                            showBtDeviceList(isList = true, isPage = false, isProgress = false, message = null)
                                        }
                                        else showBtDeviceList(isList = false, isPage = false, isProgress = false, message = getString(R.string.no_visible_objects))
                                    }
                                }
                            }
                            else -> {}
                        }
                    }
                    MainViewModel.BtState.NoLocationPermission -> {
                        binding.btnCityObjects.isEnabled = false
                        binding.btnTransport.isEnabled = false
                        permissionsBuilder(Manifest.permission.ACCESS_FINE_LOCATION).build().send(){ result ->
                            if (result.allGranted()) {
                                mainVM.initBluetooth()
                            }
                            else {
                                result.forEach {
                                    mainVM.setBtState(MainViewModel.BtState.PermissionDenied(it.permission))
                                }
                            }
                        }
                    }
                    MainViewModel.BtState.StartScan -> {
                        progressBar.visibility = View.VISIBLE
                    }
                    is MainViewModel.BtState.PermissionDenied -> {
                        val message = "Для работы приложения требуется доступ к данным о местоположении устройства."
                        showBtDeviceList(isList = false, isPage = false, isProgress = false, message = message)
                        btnCityObjects.apply {
                            isSelected = false
                            isEnabled = false
                            style(R.style.AppButton)
                        }
                        btnTransport.apply {
                            isSelected = false
                            isEnabled = false
                            style(R.style.AppButton)
                        }
                    }
                    is MainViewModel.BtState.UpdateData -> {
                        val device = state.device
                        RxBus.sendDevice(device)
                    }
                    MainViewModel.BtState.EmptyData -> {
                        val message = getString(R.string.no_visible_objects)
                        showBtDeviceList(isList = false, isPage = false, isProgress = false, message = message)
                        speechVM.autoSpeak(message)
                    }
                }
            }

            mainVM.screenState.observe(viewLifecycleOwner) { state ->
                when(state) {
                    MainViewModel.ScreenState.NothingMode -> {
                        btnCityObjects.style(R.style.AppButton)
                        btnTransport.style(R.style.AppButton)
                        val message = "Выберите режим обнаружения статических объектов или транспорта, нажав на соответствующие кнопки внизу экрана"
                        showBtDeviceList(isList = false, isPage = false, isProgress = false, message = message)
                        speechVM.autoSpeak(message)
                    }
                    is MainViewModel.ScreenState.CityMode -> {
                        when(state.mode) {
                            MainViewModel.DisplayMode.Page -> {
                                pageAdapter = BtDevicePageAdapter(listener = this@MainFragment)
                                vpList.adapter = pageAdapter
                            }
                            MainViewModel.DisplayMode.List -> {
                                listAdapter = BtDeviceListAdapter(listener = this@MainFragment)
                                rvList.adapter = listAdapter
                            }
                        }
                        mainVM.startBtScan()
                        showBtDeviceList(isList = false, isPage = false, isProgress = false, message = "Поиск объектов...")
                        btnCityObjects.apply {
                            isSelected = true
                            style(R.style.AppButtonSelected)
                        }
                        btnTransport.apply {
                            isSelected = false
                            style(R.style.AppButton)
                        }
                        displayDeviceList(state.mode)
                        mainVM.setBtState(MainViewModel.BtState.ScanSuccess(mainVM.btDevices))
                    }
                    is MainViewModel.ScreenState.TransportMode -> {
                        when(state.mode) {
                            MainViewModel.DisplayMode.Page -> {
                                pageAdapter = BtDevicePageAdapter(listener = this@MainFragment)
                                vpList.adapter = pageAdapter
                            }
                            MainViewModel.DisplayMode.List -> {
                                listAdapter = BtDeviceListAdapter(listener = this@MainFragment)
                                rvList.adapter = listAdapter
                            }
                        }
                        mainVM.startBtScan()
                        showBtDeviceList(isList = false, isPage = false, isProgress = false, message = "Поиск объектов...")
                        btnCityObjects.apply {
                            isSelected = false
                            style(R.style.AppButton)
                        }
                        btnTransport.apply {
                            isSelected = true
                            style(R.style.AppButtonSelected)
                        }
                        displayDeviceList(state.mode)
                        mainVM.setBtState(MainViewModel.BtState.ScanSuccess(mainVM.btDevices))
                    }
                    MainViewModel.ScreenState.Init -> {

                    }
                }
            }


        }
    }

    private fun displayDeviceList(mode: MainViewModel.DisplayMode) {
        with(binding) {

            when(mode) {
                MainViewModel.DisplayMode.Page -> {
                    btnToList.visibility = View.VISIBLE
                    btnToPage.visibility = View.GONE
                    vpList.apply {
                        visibility = View.VISIBLE
                    }
                    rvList.visibility = View.GONE
                }
                MainViewModel.DisplayMode.List -> {
                    btnToList.visibility = View.GONE
                    btnToPage.visibility = View.VISIBLE
                    rvList.apply {
                        visibility = View.VISIBLE
                    }
                    vpList.visibility = View.GONE
                }
            }
        }
    }

    private fun showBtDeviceList(
        isList: Boolean,
        isPage: Boolean,
        isProgress: Boolean,
        message: String?
    ) {
        with(binding) {

            if (message.isNullOrEmpty()) {
                tvMessage.visibility = View.GONE
            }
            else tvMessage.apply {
                text = message
                visibility = View.VISIBLE
            }
            if (isList) rvList.visibility = View.VISIBLE else rvList.visibility = View.GONE
            if (isPage) vpList.visibility = View.VISIBLE else vpList.visibility = View.GONE
            if (isProgress) progressBar.visibility = View.VISIBLE else progressBar.visibility = View.GONE
        }
    }

    override fun onResume() {
        super.onResume()

        with(binding) {

            toolBarContainer.forEach { view ->
                view.setOnLongClickListener {
                    val description = it.contentDescription
                    if (!description.isNullOrEmpty()) {
                        speechVM.speak(description.toString())
                    }
                    true
                }
            }
            includeAppBar.root.forEach { view ->
                view.setOnLongClickListener {
                    val description = it.contentDescription
                    if (!description.isNullOrEmpty()) {
                        speechVM.speak(description.toString())
                    }
                    true
                }
            }
            contentLayout.forEach { view ->
                view.setOnLongClickListener {
                    val description = it.contentDescription
                    if (!description.isNullOrEmpty()) {
                        speechVM.speak(description.toString())
                    }
                    true
                }
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                return
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (resultCode) {
            Activity.RESULT_OK -> {
                mainVM.setBtState(MainViewModel.BtState.BtIsOn)
            }
            Activity.RESULT_CANCELED -> {
                mainVM.setBtState(MainViewModel.BtState.NoBtPermission)
            }
        }
    }

    override fun onAdapterPreviousBtnClick(position: Int) {
        try {
            binding.vpList.setCurrentItem(position - 1, false)
        } catch (e: IndexOutOfBoundsException) {
            if (BuildConfig.DEBUG) e.printStackTrace()
        }
    }

    override fun onAdapterNextBtnClick(position: Int) {
        try {
            binding.vpList.setCurrentItem(position + 1, false)
        } catch (e: IndexOutOfBoundsException) {
            if (BuildConfig.DEBUG) e.printStackTrace()
        }
    }

    override fun onAdapterBtnCallClick(device: BtDevice) {
        val id = device.id
        mainVM.startAdvertising(id)
        speechVM.speak("Вызываю", speakId = "XXX", listener = object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {}

            override fun onDone(utteranceId: String?) {
                if (utteranceId == "XXX") {
                    appRingtone()?.play()
                }
            }
            @Deprecated("Deprecated in Java", ReplaceWith(
                "appRingtone()?.play()",
                "com.alfasreda.mobilecity.utils.appRingtone"
            )
            )
            override fun onError(utteranceId: String?) {
                if (utteranceId == "XXX") {
                    appRingtone()?.play()
                }
            }
        })
    }

    override fun onAdapterItemLongClick(description: String) {
        speechVM.speak(description)
    }

    override fun onAdapterItemAttached(description: String) {
        speechVM.autoSpeak(description)
    }

    override fun onAdapterItemsAdded(count: Int) {

        speechVM.autoSpeak("Видимых объектов $count")
    }

    override fun onEmptyAdapter() {
        mainVM.setBtState(MainViewModel.BtState.EmptyData)
    }


}
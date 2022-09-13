@file:Suppress("ObjectLiteralToLambda", "MoveVariableDeclarationIntoWhen")

package com.alfasreda.mobilecity.ui.main

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.view.forEach
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.alfasreda.mobilecity.R
import com.alfasreda.mobilecity.databinding.MainFragmentBinding
import com.alfasreda.mobilecity.models.BtDevice
import com.alfasreda.mobilecity.ui.main.adapters.CityObjectsAdapter
import com.alfasreda.mobilecity.ui.main.adapters.IBtDevicesAdapterListener
import com.alfasreda.mobilecity.ui.splash.KEY_FIRST_RUN
import com.alfasreda.mobilecity.utils.Speech
import com.alfasreda.mobilecity.utils.appPref
import com.alfasreda.mobilecity.utils.setUpToolBar
import com.alfasreda.mobilecity.utils.showSnackBar
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

    private val objectsAdapter: CityObjectsAdapter by lazy {
        CityObjectsAdapter(this)
    }

    private var scanHandler: Handler? = null

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

            rvList.apply {
                adapter = objectsAdapter
                itemAnimator = null
            }

            includeAppBar.btnBackNavigation.setOnClickListener {
                findNavController().navigate(R.id.action_mainFragment_to_menuFragment)
            }

            btnToList.setOnClickListener {
                mainVM.setDisplayMode(MainViewModel.DisplayMode.List)
            }

            btnToPage.setOnClickListener {
                mainVM.setDisplayMode(MainViewModel.DisplayMode.Grid)
            }

            includeRadioGroup.rgFilter.setOnCheckedChangeListener(object : RadioGroup.OnCheckedChangeListener {

                override fun onCheckedChanged(group: RadioGroup?, checkedId: Int) {
                    when (checkedId) {
                        R.id.btn_all -> {
                            mainVM.setScreenState(MainViewModel.ScreenState.AllObjectsMode(mainVM.getDisplayMode()))
                        }
                        R.id.btn_objects -> {
                            mainVM.setScreenState(MainViewModel.ScreenState.CityMode(mainVM.getDisplayMode()))
                        }
                        R.id.btn_transport -> {
                            mainVM.setScreenState(MainViewModel.ScreenState.TransportMode(mainVM.getDisplayMode()))
                        }
                    }
                }
            })

            includeRadioGroup.btnAll.setOnClickListener {
                (it as RadioButton).isChecked
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
                        mainVM.setScreenState(MainViewModel.ScreenState.Disabled)
                        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                        startActivityForResult(enableBtIntent, 1111)
                    }
                    MainViewModel.BtState.BtIsOn -> {

                       with(includeRadioGroup) {
                           rgFilter.visibility = View.VISIBLE
                       }
                        mainVM.setScreenState(MainViewModel.ScreenState.Init)
                        mainVM.startBtScan()
                    }
                    MainViewModel.BtState.NotSupportBT -> {
                        val message = "Устройство не поддерживает блютуз"
                        speechVM.autoSpeak(message)
                        showBtDeviceList(isList = false, isProgress = false, message = message)
                        mainVM.setScreenState(MainViewModel.ScreenState.Disabled)
                    }
                    MainViewModel.BtState.NoBtPermission -> {
                        val message = "Для работы приложения требуется включение блютуз. Нажмите что бы продолжить."
                        speechVM.autoSpeak(message)
                        showBtDeviceList(isList = false, isProgress = false, message = message)
                        mainVM.setScreenState(MainViewModel.ScreenState.Disabled)
                    }
                    is MainViewModel.BtState.ScanFailure -> {
                        progressBar.visibility = View.GONE
                        val message = "Ошибка блютуз сканирования. Код ${state.errorCode}"
                        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
                    }
                    is MainViewModel.BtState.ScanSuccess -> {
                        scanHandler?.removeCallbacksAndMessages(null)
                        val data = state.data.toList()
                        val screenState = mainVM.screenState.value
                        when(screenState) {
                            MainViewModel.ScreenState.Disabled -> {
                                mainVM.setScreenState(MainViewModel.ScreenState.Init)
                            }
                            is MainViewModel.ScreenState.AllObjectsMode -> {
                                when(screenState.mode) {
                                    MainViewModel.DisplayMode.Grid -> {
                                        if (data.isNotEmpty()) {
                                            objectsAdapter.submitList(data)
                                            showBtDeviceList(
                                                isList = true,
                                                isProgress = false,
                                                message = null
                                            )
                                        }
                                        else showBtDeviceList(
                                            isList = false,
                                            isProgress = false,
                                            message = getString(R.string.no_visible_objects)
                                        )
                                    }
                                    MainViewModel.DisplayMode.List -> {
                                        if (data.isNotEmpty()) {
                                            objectsAdapter.submitList(data)
                                            showBtDeviceList(
                                                isList = true,
                                                isProgress = false,
                                                message = null
                                            )
                                        }
                                        else showBtDeviceList(
                                            isList = false,
                                            isProgress = false,
                                            message = getString(R.string.no_visible_objects)
                                        )
                                    }
                                }
                            }
                            is MainViewModel.ScreenState.CityMode -> {
                                val filteredData = data.filter {
                                    it.type == BtDevice.CITY_OBJECT
                                }
                                when(screenState.mode) {
                                    MainViewModel.DisplayMode.Grid -> {
                                        if (filteredData.isNotEmpty()) {
                                            objectsAdapter.submitList(filteredData)
                                            showBtDeviceList(
                                                isList = true,
                                                isProgress = false,
                                                message = null
                                            )
                                        }
                                        else showBtDeviceList(
                                            isList = false,
                                            isProgress = false,
                                            message = getString(R.string.no_visible_objects)
                                        )
                                    }
                                    MainViewModel.DisplayMode.List -> {
                                        if (filteredData.isNotEmpty()) {
                                            objectsAdapter.submitList(filteredData)
                                            showBtDeviceList(
                                                isList = true,
                                                isProgress = false,
                                                message = null
                                            )
                                        }
                                        else showBtDeviceList(
                                            isList = false,
                                            isProgress = false,
                                            message = getString(R.string.no_visible_objects)
                                        )
                                    }
                                }
                            }
                            is MainViewModel.ScreenState.TransportMode -> {
                                val filteredData = data.filter {
                                    it.type == BtDevice.BUS || it.type == BtDevice.TROLLEYBUS || it.type == BtDevice.TRAM
                                }
                                when(screenState.mode) {
                                    MainViewModel.DisplayMode.Grid -> {
                                        if (filteredData.isNotEmpty()) {
                                            objectsAdapter.submitList(filteredData)
                                            showBtDeviceList(
                                                isList = true,
                                                isProgress = false,
                                                message = null
                                            )
                                        }
                                        else showBtDeviceList(
                                            isList = false,
                                            isProgress = false,
                                            message = getString(R.string.no_visible_objects)
                                        )
                                    }
                                    MainViewModel.DisplayMode.List -> {
                                        if (filteredData.isNotEmpty()) {
                                            objectsAdapter.submitList(filteredData)
                                            showBtDeviceList(
                                                isList = true,
                                                isProgress = false,
                                                message = null
                                            )
                                        }
                                        else showBtDeviceList(
                                            isList = false,
                                            isProgress = false,
                                            message = getString(R.string.no_visible_objects)
                                        )
                                    }
                                }
                            }
                            else -> {}
                        }
                    }
                    MainViewModel.BtState.NoLocationPermission -> {
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

                        scanHandler = runScanTimer(mainVM)
                    }
                    is MainViewModel.BtState.PermissionDenied -> {
                        val message = "Для работы приложения требуется доступ к данным о местоположении устройства."
                        showBtDeviceList(isList = false, isProgress = false, message = message)
                    }
                    is MainViewModel.BtState.UpdateData -> {
                        val device = state.device
                        objectsAdapter.updateItem(device)
                    }
                    MainViewModel.BtState.EmptyData -> {
                        val message = getString(R.string.no_visible_objects)
                        showBtDeviceList(isList = false, isProgress = false, message = message)
                        mainVM.setScreenState(MainViewModel.ScreenState.Disabled)
                        speechVM.autoSpeak(message)
                    }
                }
            }

            mainVM.screenState.observe(viewLifecycleOwner) { state ->

                when(state) {
                    MainViewModel.ScreenState.Init -> {
                        showBtDeviceList(
                            isList = false,
                            isProgress = true,
                            message = "Поиск объектов..."
                        )
                        includeRadioGroup.rgFilter.forEach {
                            it.isEnabled = true
                        }
                        includeRadioGroup.btnAll.isChecked = true
                        btnToPage.visibility = View.VISIBLE
                        btnToList.visibility = View.VISIBLE
                        mainVM.setScreenState(MainViewModel.ScreenState.AllObjectsMode(mainVM.getDisplayMode()))
                    }
                    MainViewModel.ScreenState.Disabled -> {
                        includeRadioGroup.rgFilter.forEach {
                            it.isEnabled = false
                        }
                        btnToPage.visibility = View.GONE
                        btnToList.visibility = View.GONE
                    }
                    is MainViewModel.ScreenState.AllObjectsMode -> {
                        speechVM.autoSpeak("Режим любые объекты")
                        displayDeviceList(mainVM.getDisplayMode())
                        mainVM.setBtState(MainViewModel.BtState.ScanSuccess(mainVM.btDevices))
                    }
                    is MainViewModel.ScreenState.CityMode -> {
                        speechVM.autoSpeak("Режим городские объекты")
                        displayDeviceList(mainVM.getDisplayMode())
                        mainVM.setBtState(MainViewModel.BtState.ScanSuccess(mainVM.btDevices))
                    }
                    is MainViewModel.ScreenState.TransportMode -> {
                        speechVM.autoSpeak("Режим транспорт")
                        displayDeviceList(mainVM.getDisplayMode())
                        mainVM.setBtState(MainViewModel.BtState.ScanSuccess(mainVM.btDevices))
                    }
                }
            }


        }
    }

    private fun displayDeviceList(mode: MainViewModel.DisplayMode) {
        with(binding) {

            when(mode) {
                MainViewModel.DisplayMode.Grid -> {
                    btnToList.visibility = View.VISIBLE
                    btnToPage.visibility = View.GONE
                    rvList.layoutManager = GridLayoutManager(requireContext(), 2, GridLayoutManager.VERTICAL, false)
                }
                MainViewModel.DisplayMode.List -> {
                    btnToList.visibility = View.GONE
                    btnToPage.visibility = View.VISIBLE
                    rvList.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
                }
            }
        }
    }

    private fun showBtDeviceList(isList: Boolean, isProgress: Boolean, message: String?) {
        with(binding) {

            if (message.isNullOrEmpty()) {
                tvMessage.visibility = View.GONE
            }
            else tvMessage.apply {
                text = message
                visibility = View.VISIBLE
            }
            if (isList) rvList.visibility = View.VISIBLE else rvList.visibility = View.GONE
            if (isProgress) progressBar.visibility = View.VISIBLE else progressBar.visibility = View.GONE
        }
    }

    private fun runScanTimer(viewModel: MainViewModel): Handler {
        val handler = Handler(Looper.getMainLooper()).apply {
            postDelayed({
                viewModel.setBtState(MainViewModel.BtState.EmptyData)
            }, 5000)
        }
        return handler
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

    }

    override fun onAdapterNextBtnClick(position: Int) {

    }

    override fun onAdapterBtnCallClick(device: BtDevice) {
        val id = device.id
        mainVM.startAdvertising(id)
        speechVM.speak("Вызываю")
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

    override fun onSignalReceived(deviceId: String, isCall: Boolean) {
        if (isCall) speechVM.speak("Вызов принят")
    }


}
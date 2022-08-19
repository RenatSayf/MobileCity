@file:Suppress("ObjectLiteralToLambda", "MoveVariableDeclarationIntoWhen")

package com.alfasreda.mobilecity.ui.main

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.view.forEach
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.airbnb.paris.extensions.style
import com.alfasreda.mobilecity.BuildConfig
import com.alfasreda.mobilecity.R
import com.alfasreda.mobilecity.databinding.MainFragmentBinding
import com.alfasreda.mobilecity.ui.main.adapters.BtDeviceAdapter
import com.alfasreda.mobilecity.utils.DoubleClickListener
import com.alfasreda.mobilecity.utils.Speech
import com.alfasreda.mobilecity.utils.setUpToolBar
import com.alfasreda.mobilecity.utils.showSnackBar
import com.fondesa.kpermissions.allGranted
import com.fondesa.kpermissions.extension.permissionsBuilder
import com.fondesa.kpermissions.extension.send
import kotlinx.coroutines.launch


class MainFragment : Fragment(), BtDeviceAdapter.Listener {

    companion object {
        const val ARG_SPEECH = "ARG_SPEECH"
    }

    private lateinit var binding: MainFragmentBinding

    private val speechVM: SpeechViewModel by activityViewModels()

    private val mainVM: MainViewModel by activityViewModels(factoryProducer = {
        MainViewModel.Factory()
    })

    private val deviceAdapter: BtDeviceAdapter by lazy {
        BtDeviceAdapter(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = MainFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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

        lifecycleScope.launchWhenStarted {
            speechVM.state.collect { state ->
                when(state) {
                    SpeechViewModel.State.InitError -> {

                    }
                    SpeechViewModel.State.LangMissingData -> {

                    }
                    SpeechViewModel.State.LangNotSupported -> {

                    }
                    SpeechViewModel.State.InitSuccess -> {

                    }
                    SpeechViewModel.State.NotActive -> {}
                }
            }
        }

        if (savedInstanceState == null) {
            permissionsBuilder(Manifest.permission.ACCESS_FINE_LOCATION).build().send(){ result ->
                if (result.allGranted()) {
                    mainVM.initBluetooth()
                }
                else {
                    result
                }
            }
        }

        with(binding) {

            setUpToolBar(
                binding = includeAppBar,
                iconResource = R.drawable.ic_main_menu_white,
                iconContentDescription = "Перейти в главное меню",
                title = "Главная",
                titleContentDescription = "Это главный экран приложения"
                )

            includeAppBar.btnBackNavigation.setOnClickListener {
                findNavController().navigate(R.id.action_mainFragment_to_menuFragment)
            }

            btnCityObjects.setOnClickListener {
                //mainVM.startAdvertising()
                //mainVM.startBtScan()
                mainVM.setScreenState(MainViewModel.ScreenState.CityMode(mainVM.btDevices))
            }

            btnTransport.setOnClickListener {
                //mainVM.startAdvertising()
                //mainVM.startBtScan()
                mainVM.setScreenState(MainViewModel.ScreenState.TransportMode(mainVM.btDevices))
            }

            mainVM.btState.observe(viewLifecycleOwner) { state ->

                when (state) {
                    MainViewModel.BtState.BtIsOff -> {
                        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                        startActivityForResult(enableBtIntent, 1111)
                    }
                    MainViewModel.BtState.BtIsOn -> {

                    }
                    MainViewModel.BtState.NotSupportBT -> {
                        val message = "Устройство не поддерживает блютуз"
                        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
                    }
                    MainViewModel.BtState.NoScanPermission -> {
                        val message = "Нет разрешения на блютуз сканирование"
                        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
                    }
                    is MainViewModel.BtState.ScanFailure -> {
                        val message = "Ошибка блютуз сканирования. Код ${state.errorCode}"
                        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
                    }
                    is MainViewModel.BtState.ScanSuccess -> {
                        val data = state.data
                        deviceAdapter.addItems(data)
                        rvList.adapter = deviceAdapter
                    }
                    is MainViewModel.BtState.NoPermission -> {
                        permissionsBuilder(state.permission).build().send() { result ->
                            val granted = result.allGranted()
                            if (granted) {
                                mainVM.startAdvertising()
                            } else {
                                mainVM.stopBtScan()
                            }
                        }
                    }
                    MainViewModel.BtState.Undefined -> {}
                }
            }

            lifecycleScope.launchWhenResumed {
                mainVM.screenState.collect { state ->
                    when(state) {
                        MainViewModel.ScreenState.NothingMode -> {
                            btnCityObjects.style(R.style.AppButton)
                            btnTransport.style(R.style.AppButton)
                        }
                        is MainViewModel.ScreenState.CityMode -> {
                            btnCityObjects.apply {
                                isSelected = true
                                style(R.style.AppButtonSelected)
                            }
                            btnTransport.apply {
                                isSelected = false
                                style(R.style.AppButton)
                            }
                            mainVM.startBtScan()
                            val data = mainVM.btDevices
                            deviceAdapter.addItems(data)
                            rvList.adapter = deviceAdapter
                        }
                        is MainViewModel.ScreenState.TransportMode -> {
                            btnCityObjects.apply {
                                isSelected = false
                                style(R.style.AppButton)
                            }
                            btnTransport.apply {
                                isSelected = true
                                style(R.style.AppButtonSelected)
                            }
                            mainVM.startBtScan()
                            val data = mainVM.btDevices
                            deviceAdapter.addItems(data)
                            rvList.adapter = deviceAdapter
                        }
                    }
                }
            }

        }



    }

    override fun onResume() {
        super.onResume()

        with(binding) {

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

        requestCode
    }

    override fun onAdapterPreviousBtnClick(position: Int) {
        try {
            binding.rvList.setCurrentItem(position - 1, true)
        } catch (e: IndexOutOfBoundsException) {
            if (BuildConfig.DEBUG) e.printStackTrace()
        }
    }

    override fun onAdapterNextBtnClick(position: Int) {
        try {
            binding.rvList.setCurrentItem(position + 1, true)
        } catch (e: IndexOutOfBoundsException) {
            if (BuildConfig.DEBUG) e.printStackTrace()
        }
    }


}
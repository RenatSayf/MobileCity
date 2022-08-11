@file:Suppress("ObjectLiteralToLambda")

package com.alfasreda.mobilecity.ui.main

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.alfasreda.mobilecity.R
import com.alfasreda.mobilecity.databinding.MainFragmentBinding
import com.fondesa.kpermissions.allGranted
import com.fondesa.kpermissions.extension.permissionsBuilder
import com.fondesa.kpermissions.extension.send


class MainFragment : Fragment() {

    private lateinit var binding: MainFragmentBinding

    private val mainVM: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = MainFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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

            btnCityObjects.setOnClickListener {
                //mainVM.startAdvertising()
                mainVM.startScan()
                mainVM.setScreenState(MainViewModel.ScreenState.CityMode)
            }

            btnTransport.setOnClickListener {
                //mainVM.startAdvertising()
                mainVM.startScan()
                mainVM.setScreenState(MainViewModel.ScreenState.TransportMode)
            }

            lifecycleScope.launchWhenResumed {

                mainVM.btState.collect { state ->
                    mainVM.setBtState(state)
                    when(state) {
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
                            data
                        }
                        is MainViewModel.BtState.NoPermission -> {
                            permissionsBuilder(state.permission).build().send(){ result ->
                                val granted = result.allGranted()
                                if (granted) {
                                    mainVM.startAdvertising()
                                }
                                else {
                                    mainVM.stopAdvertising()
                                }
                            }
                        }
                    }
                }
            }

            lifecycleScope.launchWhenResumed {
                mainVM.screenState.collect { state ->
                    when(state) {
                        MainViewModel.ScreenState.NothingMode -> {
                            btnCityObjects.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.blue))
                            btnTransport.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.blue))
                        }
                        MainViewModel.ScreenState.CityMode -> {
                            btnCityObjects.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.light_green))
                            btnTransport.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.blue))
                        }
                        MainViewModel.ScreenState.TransportMode -> {
                            btnCityObjects.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.blue))
                            btnTransport.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.light_green))
                        }
                    }
                }
            }

        }



    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        requestCode
    }









}
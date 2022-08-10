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
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
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

        permissionsBuilder(Manifest.permission.ACCESS_FINE_LOCATION).build().send(){ result ->
            if (result.allGranted()) {
                mainVM.initBluetooth()
            }
            else {
                result
            }
        }

        with(binding) {

            mainVM.startAdvertising()

            lifecycleScope.launchWhenResumed {
                mainVM.state.collect { state ->
                    mainVM.setState(state)
                    when(state) {
                        MainViewModel.State.BtIsOff -> {
                            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                            startActivityForResult(enableBtIntent, 1111)
                        }
                        MainViewModel.State.BtIsOn -> {
                            mainVM.startAdvertising()
                        }
                        MainViewModel.State.NotSupportBT -> {
                            val message = "Устройство не поддерживает блютуз"
                            Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
                        }
                        MainViewModel.State.NoScanPermission -> {
                            val message = "Нет разрешения на блютуз сканирование"
                            Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
                        }
                        is MainViewModel.State.ScanFailure -> {
                            val message = "Ошибка блютуз сканирования. Код ${state.errorCode}"
                            Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
                        }
                        is MainViewModel.State.ScanSuccess -> {
                            val data = state.data
                            data
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
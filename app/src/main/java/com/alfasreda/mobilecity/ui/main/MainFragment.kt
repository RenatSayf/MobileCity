@file:Suppress("ObjectLiteralToLambda")

package com.alfasreda.mobilecity.ui.main

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.alfasreda.mobilecity.R
import com.alfasreda.mobilecity.databinding.MainFragmentBinding
import com.fondesa.kpermissions.allGranted
import com.fondesa.kpermissions.extension.permissionsBuilder
import com.fondesa.kpermissions.extension.send


class MainFragment : Fragment(R.layout.main_fragment) {

    private lateinit var binding: MainFragmentBinding

    private val mainVM: MainViewModel by viewModels()

    private var bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private val bltAdvertiser = bluetoothAdapter?.bluetoothLeAdvertiser

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.main_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = MainFragmentBinding.bind(view)

        val sid = ("ID00000004").substring(2)
        val namesSnd = sid.toByte()

        val advData = AdvertiseData.Builder().addManufacturerData(0x4449, byteArrayOf(namesSnd)).build()

        val advSettings = AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
            .setConnectable(false)
            .build()

        val advCallback = object : AdvertiseCallback() {
            override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
                super.onStartSuccess(settingsInEffect)
                binding.progressAvailableDevices.visibility = View.GONE
                if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) return
                bluetoothAdapter?.startLeScan(object : BluetoothAdapter.LeScanCallback {
                    override fun onLeScan(p0: BluetoothDevice?, p1: Int, p2: ByteArray?) {
                        p0
                        p1
                        p2
                    }
                })
            }
            override fun onStartFailure(errorCode: Int) {
                super.onStartFailure(errorCode)
                binding.progressAvailableDevices.visibility = View.GONE
            }
        }

        permissionsBuilder(Manifest.permission.ACCESS_FINE_LOCATION).build().send(){ result ->
            if (result.allGranted()) {
                initBluetooth(bluetoothAdapter)
            }
        }

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            bluetoothAdapter?.stopLeScan(object : BluetoothAdapter.LeScanCallback {
                override fun onLeScan(p0: BluetoothDevice?, p1: Int, p2: ByteArray?) {}
            })
        }

        with(binding) {
            scanBtn.setOnClickListener {
                binding.progressAvailableDevices.visibility = View.VISIBLE

                if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_ADVERTISE) != PackageManager.PERMISSION_GRANTED) {
                    bltAdvertiser?.startAdvertising(advSettings, advData, advCallback)
                }
            }
        }



    }

    private fun initBluetooth(adapter: BluetoothAdapter?) {
        if (adapter == null) {
            Toast.makeText(requireContext(), "Устройство не поддерживает блютуз", Toast.LENGTH_LONG).show()
            return
        }
        else if (!adapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, 1111)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
    }









}
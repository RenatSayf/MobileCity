package com.alfasreda.mobilecity.ui.main

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.*
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
import com.alfasreda.mobilecity.services.BluetoothService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import quevedo.soares.leandro.androideasyble.BLE


class MainFragment : Fragment(R.layout.main_fragment) {

    private lateinit var binding: MainFragmentBinding

    private val mainVM: MainViewModel by viewModels()

    private var ble: BLE? = null

    private var bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private val bltAdvertiser = bluetoothAdapter?.bluetoothLeAdvertiser

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.main_fragment, container, false)
    }

    //@SuppressLint("MissingPermission")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = MainFragmentBinding.bind(view)

        if (bluetoothAdapter == null) {
            Toast.makeText(requireContext(), "Устройство не поддерживает блютуз", Toast.LENGTH_LONG).show()
            return
        }
        else if (bluetoothAdapter != null && !bluetoothAdapter!!.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, 1111)
        }

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
                if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
                    return
                }
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

        val checkSelfPermission = ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_SCAN)
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
            bluetoothAdapter?.stopLeScan(object : BluetoothAdapter.LeScanCallback {
                override fun onLeScan(p0: BluetoothDevice?, p1: Int, p2: ByteArray?) {
                    p0
                    p1
                    p2
                }
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1111 && resultCode != Activity.RESULT_OK) return
    }











}
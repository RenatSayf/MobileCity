package com.alfasreda.mobilecity.ui.main.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.alfasreda.mobilecity.databinding.ItemToGridBinding
import com.alfasreda.mobilecity.models.BtDevice

class CityObjectsAdapter(
    private val listener: IBtDevicesAdapterListener
) : ListAdapter<BtDevice, CityObjectsAdapter.ViewHolder>(DIFF_CALLBACK) {

    private var positionIndex: Int = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemToGridBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), position, itemCount)
    }

    fun updateItem(device: BtDevice) {
        val btDevice = currentList.firstOrNull {
            it.id == device.id
        }.apply {
            this?.rssi = device.rssi
        }
        if (btDevice != null) {
            val position = currentList.indexOf(btDevice)
            notifyItemChanged(position)
        }
    }


    inner class ViewHolder(private val binding: ItemToGridBinding) : RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(device: BtDevice, position: Int, count: Int) {

            positionIndex = position
            with(binding) {

                when(device.type) {
                    BtDevice.CITY_OBJECT -> {
                        tvObjectType.text = device.description
                        tvAddress.text = device.address
                        btnCall.contentDescription = "Найти вход"
                    }
                    BtDevice.BUS -> {
                        tvObjectType.text = "Автобус"
                        tvAddress.text = "Маршрут № ${device.route}"
                        btnCall.contentDescription = "Подать сигнал водителю"
                    }
                    BtDevice.TROLLEYBUS -> {
                        tvObjectType.text = "Троллейбус"
                        tvAddress.text = "Маршрут № ${device.route}"
                        btnCall.contentDescription = "Подать сигнал водителю"
                    }
                    BtDevice.TRAM -> {
                        tvObjectType.text = "Трамвай"
                        tvAddress.text = "Маршрут № ${device.route}"
                        btnCall.contentDescription = "Подать сигнал водителю"
                    }
                }

                tvRssiValue.text = "${device.rssi} dB"

                layoutItem.contentDescription = "${tvObjectType.text}. ${tvAddress.text}"

                layoutItem.setOnLongClickListener {
                    listener.onAdapterItemLongClick(device.description)
                    true
                }

                btnCall.setOnClickListener {
                    listener.onAdapterBtnCallClick(device)
                }

            }
        }


    }


}
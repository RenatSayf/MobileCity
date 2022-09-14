@file:Suppress("MoveVariableDeclarationIntoWhen")

package com.alfasreda.mobilecity.ui.main.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.alfasreda.mobilecity.R
import com.alfasreda.mobilecity.databinding.ItemToGridBinding
import com.alfasreda.mobilecity.models.BtDevice
import com.alfasreda.mobilecity.models.enums.TrafficLightState

class CityObjectsAdapter(
    private val listener: IBtDevicesAdapterListener
) : ListAdapter<BtDevice, CityObjectsAdapter.ViewHolder>(DIFF_CALLBACK) {

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

        private var calledDeviceId = ""

        @SuppressLint("SetTextI18n")
        fun bind(device: BtDevice, position: Int, count: Int) {

            with(binding) {

                tvAddress.visibility = View.VISIBLE
                btnCall.visibility = View.VISIBLE

                when(device.type) {
                    BtDevice.BUS -> {
                        tvObjectType.text = "Автобус"
                    }
                    BtDevice.TROLLEYBUS -> {
                        tvObjectType.text = "Троллейбус"
                    }
                    BtDevice.TRAM -> {
                        tvObjectType.text = "Трамвай"
                    }
                    BtDevice.TRAFFIC_LIGHT -> {
                        tvObjectType.text = "Светофор"
                    }
                }

                when(device.type) {
                    BtDevice.TRAFFIC_LIGHT -> {
                        val description = device.objectDescription
                        tvAddress.text = description ?: "Неизвестный объект"
                        val color = device.trafficLightColor
                        val textColor = when (color) {
                            TrafficLightState.Red -> "Красный"
                            TrafficLightState.Yellow -> "Желтый"
                            TrafficLightState.Green -> "Зеленый"
                            else -> "Неизвестен"
                        }
                        layoutItem.contentDescription = "${tvObjectType.text}. $description. Сигнал $textColor"
                        btnCall.visibility = View.INVISIBLE
                    }
                    BtDevice.CITY_OBJECT -> {
                        val description = device.objectDescription
                        tvObjectType.text = description ?: "Неизвестный объект"
                        tvAddress.visibility = View.GONE
                        btnCall.contentDescription = "Найти вход"
                        layoutItem.contentDescription = tvObjectType.text
                    }
                    BtDevice.BUS, BtDevice.TROLLEYBUS, BtDevice.TRAM -> {
                        tvAddress.visibility = View.VISIBLE
                        val rout = "Маршрут № ${device.objectDescription}"
                        tvAddress.text = rout
                        btnCall.contentDescription = "Подать сигнал водителю"
                        val doorState = if (device.isDoorOpen) "Дверь открыта" else "Дверь закрыта"
                        layoutItem.contentDescription = "${tvObjectType.text}. ${tvAddress.text}. $doorState"
                    }
                }

                tvRssiValue.text = "${device.rssi} dB"

                if (device.isCall()) {
                    layoutItem.setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.called_bg_color))
                }
                else {
                    layoutItem.setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.white))
                }

                layoutItem.setOnClickListener {
                    listener.onAdapterItemClick(device)
                }

                layoutItem.setOnLongClickListener {
                    val description = layoutItem.contentDescription.toString()
                    listener.onAdapterItemLongClick(description)
                    true
                }

                btnCall.setOnClickListener {
                    listener.onAdapterBtnCallClick(device)
                }

                if (device.isCall() && calledDeviceId.isEmpty()) {
                    calledDeviceId = device.id
                    listener.onSignalReceived(device.id, device.isCall())
                }

                if (!device.isCall() && device.id == calledDeviceId) {
                    calledDeviceId = ""
                }

            }
        }

    }

}
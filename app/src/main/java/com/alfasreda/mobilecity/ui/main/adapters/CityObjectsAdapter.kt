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
import com.alfasreda.mobilecity.utils.appRingtone

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

        @SuppressLint("SetTextI18n")
        fun bind(device: BtDevice, position: Int, count: Int) {

            with(binding) {

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
                }

                when(device.type) {
                    BtDevice.CITY_OBJECT -> {
                        tvObjectType.text = device.objectDescription
                        tvAddress.visibility = View.GONE
                        btnCall.contentDescription = "Найти вход"
                        layoutItem.contentDescription = tvObjectType.text
                    }
                    BtDevice.BUS, BtDevice.TROLLEYBUS, BtDevice.TRAM -> {
                        tvAddress.visibility = View.VISIBLE
                        val rout = "Маршрут № ${device.route}"
                        tvAddress.text = rout
                        btnCall.contentDescription = "Подать сигнал водителю"
                        val doorState = if (device.isDoorOpen) "Дверь открыта" else "Дверь закрыта"
                        layoutItem.contentDescription = "${tvObjectType.text}. ${tvAddress.text}. $doorState"
                    }
                }

                tvRssiValue.text = "${device.rssi} dB"

                if (device.isCall()) {
                    layoutItem.setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.super_light_green))
                }
                else {
                    layoutItem.setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.white))
                    itemView.context.appRingtone()?.stop()
                }

                layoutItem.setOnLongClickListener {
                    val description = layoutItem.contentDescription.toString()
                    listener.onAdapterItemLongClick(description)
                    true
                }

                btnCall.setOnClickListener {
                    listener.onAdapterBtnCallClick(device)
                }

                if (device.isCall()) {
                    listener.onSignalReceived(device)
                }

            }
        }

    }

}
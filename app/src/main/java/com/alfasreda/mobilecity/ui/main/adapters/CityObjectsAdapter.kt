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

    private var devices = mutableListOf<BtDevice>()
    private var positionIndex: Int = -1

    fun addItems(list: List<BtDevice>) {

        if (devices != list) {
            listener.onAdapterItemsAdded(list.size)
        }
        devices = list.toMutableList()
        notifyDataSetChanged()

    }

    fun clear() {
        devices.clear()
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return devices.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemToGridBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(devices[position], position, devices.size)
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
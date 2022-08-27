package com.alfasreda.mobilecity.ui.main.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.alfasreda.mobilecity.R
import com.alfasreda.mobilecity.databinding.ItemObjectToListBinding
import com.alfasreda.mobilecity.databinding.ItemObjectToPageBinding
import com.alfasreda.mobilecity.databinding.ItemTransportToListBinding
import com.alfasreda.mobilecity.databinding.ItemTransportToPageBinding
import com.alfasreda.mobilecity.models.BtDevice
import com.alfasreda.mobilecity.utils.appRingtone


private const val CITY_OBJECT = 0
private const val TRANSPORT = 1

class BtDeviceListAdapter(
    private val lifecycleOwner: LifecycleOwner,
    private val listener: Listener
    ) : ListAdapter<BtDevice, RecyclerView.ViewHolder>(DIFF_CALLBACK) {

    private var devices = mutableListOf<BtDevice>()

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

    override fun getItemViewType(position: Int): Int {
        val objectType = devices[position].type
        return when(objectType) {
            BtDevice.CITY_OBJECT -> CITY_OBJECT
            BtDevice.BUS -> TRANSPORT
            BtDevice.TROLLEYBUS -> TRANSPORT
            BtDevice.TRAM -> TRANSPORT
            else -> -1
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when(viewType) {
            CITY_OBJECT -> {
                val binding = ItemObjectToListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                CityObjectViewHolder(binding)
            }
            TRANSPORT -> {
                val binding = ItemTransportToListBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                TransportViewHolder(binding)
            }
            else -> {
                throw IllegalStateException()
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(holder) {
            is CityObjectViewHolder -> {
                holder.bind(devices[position])
            }
            is TransportViewHolder -> {
                holder.bind(devices[position])
            }
        }
    }

    override fun getItemCount(): Int {
        return devices.size
    }

    inner class CityObjectViewHolder(private val binding: ItemObjectToListBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(device: BtDevice) {
            with(binding) {

                tvObjectName.text = device.description
                tvObjectName.setOnClickListener {

                    listener.onAdapterBtnCallClick(device)
                }
                tvObjectName.setOnLongClickListener {
                    listener.onAdapterItemLongClick(device.description)
                    true
                }
            }
        }
    }

    inner class TransportViewHolder(private val binding: ItemTransportToListBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(device: BtDevice) {
            with(binding) {

                val type = device.type
                when(type) {
                    BtDevice.BUS -> tvTransportType.text = "Автобус"
                    BtDevice.TRAM -> tvTransportType.text = "Трамвай"
                    BtDevice.TROLLEYBUS -> tvTransportType.text = "Троллейбус"
                }
                tvRouteValue.text = device.route

                btnCall.setOnClickListener {
                    listener.onAdapterBtnCallClick(device)
                }
                device.isCallLiveData.observe(lifecycleOwner) {
                    val appRingtone = itemView.context.appRingtone()
                    if (it) {
                        layoutItem.setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.super_light_green))
                    }
                    else {
                        layoutItem.setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.white))
                        appRingtone?.stop()
                    }
                }
            }
        }
    }

    interface Listener {
        fun onAdapterBtnCallClick(device: BtDevice)
        fun onAdapterItemLongClick(description: String)
        fun onAdapterItemsAdded(count: Int)
    }
}
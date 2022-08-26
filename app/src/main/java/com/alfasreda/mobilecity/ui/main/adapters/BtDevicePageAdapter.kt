@file:Suppress("MoveVariableDeclarationIntoWhen")

package com.alfasreda.mobilecity.ui.main.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.alfasreda.mobilecity.R
import com.alfasreda.mobilecity.databinding.ItemBleToPageBinding
import com.alfasreda.mobilecity.databinding.ItemTransportToPageBinding
import com.alfasreda.mobilecity.models.BtDevice

private const val CITY_OBJECT = 0
private const val TRANSPORT = 1

class BtDevicePageAdapter(
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
                val binding = ItemBleToPageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                CityObjectViewHolder(binding)
            }
            TRANSPORT -> {
                val binding = ItemTransportToPageBinding.inflate(
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
                holder.bind(devices[position], position, devices.size)
            }
            is TransportViewHolder -> {
                holder.bind(devices[position], position, devices.size)
            }
        }
    }

    override fun getItemCount(): Int {
        return devices.size
    }

    override fun onViewAttachedToWindow(holder: RecyclerView.ViewHolder) {
        super.onViewAttachedToWindow(holder)

        when(holder) {
            is CityObjectViewHolder -> {
                val text = holder.itemView.findViewById<TextView>(R.id.tv_object_name).text
                listener.onAdapterItemBind(text.toString())
            }
            is TransportViewHolder -> {
                with(holder.itemView) {
                    val typeText = findViewById<TextView>(R.id.tv_transport_type).text
                    val routeTitle = findViewById<TextView>(R.id.tv_route_title).text
                    val routeValue = findViewById<TextView>(R.id.tv_route_value).text
                    val text = "$typeText. $routeTitle $routeValue"
                    listener.onAdapterItemBind(text)
                }
            }
        }
    }



    inner class CityObjectViewHolder(private val binding: ItemBleToPageBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(device: BtDevice, position: Int, count: Int) {

            with(binding) {

                tvObjectName.text = device.description
                val itemCount = "${position + 1} / $count"
                tvObjectsCount.text = itemCount

                btnPrevious.isEnabled = position > 0
                btnNext.isEnabled = position < count - 1

                tvObjectName.setOnClickListener {
                    listener.onAdapterItemOnClick(device)
                }

                tvObjectName.setOnLongClickListener {
                    listener.onAdapterItemLongClick(device.description)
                    true
                }
                btnPrevious.setOnClickListener {
                    listener.onAdapterPreviousBtnClick(position)
                }
                btnNext.setOnClickListener {
                    listener.onAdapterNextBtnClick(position)
                }
            }
        }
    }

    inner class TransportViewHolder(private val binding: ItemTransportToPageBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(device: BtDevice, position: Int, count: Int) {
            with(binding) {

                val itemCount = "${position + 1} / $count"
                tvObjectsCount.text = itemCount

                btnPrevious.isEnabled = position > 0
                btnNext.isEnabled = position < count - 1

                val type = device.type
                when(type) {
                    BtDevice.BUS -> tvTransportType.text = "Автобус"
                    BtDevice.TRAM -> tvTransportType.text = "Трамвай"
                    BtDevice.TROLLEYBUS -> tvTransportType.text = "Троллейбус"
                }
                tvRouteValue.text = device.route

                btnPrevious.setOnClickListener {
                    listener.onAdapterPreviousBtnClick(position)
                }
                btnNext.setOnClickListener {
                    listener.onAdapterNextBtnClick(position)
                }
                btnCall.setOnClickListener {
                    listener.onAdapterItemOnClick(device)
                    layoutItem.setBackgroundColor(ContextCompat.getColor(it.context, R.color.super_light_green))
                }
            }
        }
    }

    interface Listener {
        fun onAdapterPreviousBtnClick(position: Int)
        fun onAdapterNextBtnClick(position: Int)
        fun onAdapterItemOnClick(device: BtDevice)
        fun onAdapterItemLongClick(description: String)
        fun onAdapterItemBind(description: String)
        fun onAdapterItemsAdded(count: Int)
    }
}
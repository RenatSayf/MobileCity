package com.alfasreda.mobilecity.ui.main.adapters

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.alfasreda.mobilecity.BuildConfig
import com.alfasreda.mobilecity.R
import com.alfasreda.mobilecity.databinding.ItemToGridBinding
import com.alfasreda.mobilecity.models.BtDevice
import com.alfasreda.mobilecity.utils.appRingtone

class CityObjectsAdapter(
    private val listener: IBtDevicesAdapterListener
) : ListAdapter<BtDevice, CityObjectsAdapter.ViewHolder>(DIFF_CALLBACK) {

    private val handlerList = mutableListOf<Handler>()

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

    override fun onCurrentListChanged(
        previousList: MutableList<BtDevice>,
        currentList: MutableList<BtDevice>
    ) {
        handlerList.forEach { handler ->
            handler.removeCallbacksAndMessages(null)
        }
        if (handlerList.isNotEmpty()) {
            handlerList.clear()
        }
    }

    fun resetRemovingTimers() {
        handlerList.forEach { handler ->
            handler.removeCallbacksAndMessages(null)
        }
        if (handlerList.isNotEmpty()) {
            handlerList.clear()
        }
    }



    inner class ViewHolder(private val binding: ItemToGridBinding) : RecyclerView.ViewHolder(binding.root) {

        private var handlerToRemoveItem: Handler? = null

        @SuppressLint("SetTextI18n")
        fun bind(device: BtDevice, position: Int, count: Int) {

            with(binding) {

                handlerToRemoveItem?.removeCallbacksAndMessages(null)
                handlerList.remove(handlerToRemoveItem)
                handlerToRemoveItem = null

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

                if (device.isCall()) {
                    layoutItem.setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.super_light_green))
                }
                else {
                    layoutItem.setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.white))
                    itemView.context.appRingtone()?.stop()
                }

                layoutItem.setOnLongClickListener {
                    val description = "${tvObjectType.text}. ${tvAddress.text}"
                    listener.onAdapterItemLongClick(description)
                    true
                }

                btnCall.setOnClickListener {
                    listener.onAdapterBtnCallClick(device)
                }

                handlerToRemoveItem = runTimerToRemoveItem(position, object : IPostDelayedCallback {
                    override fun onDelayed(position: Int) {
                        try {
                            val list = currentList.toMutableList()
                            val removedDevice = list.removeAt(position)
                            this@CityObjectsAdapter.submitList(list)
                            if (list.isEmpty()) {
                                listener.onEmptyAdapter()
                            }
                            listener.onAdapterItemRemoved(removedDevice)
                        } catch (e: IndexOutOfBoundsException) {
                            if (BuildConfig.DEBUG) e.printStackTrace()
                        }
                    }
                })
                handlerToRemoveItem?.let { handler ->
                    handlerList.add(handler)
                }
            }
        }

        private fun runTimerToRemoveItem(position: Int, callback: IPostDelayedCallback): Handler {
            return Handler(Looper.getMainLooper()).apply {
                this.postDelayed({
                    callback.onDelayed(position)
                }, 5000)
            }
        }


    }

    interface IPostDelayedCallback {
        fun onDelayed(position: Int)
    }

}
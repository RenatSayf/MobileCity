@file:Suppress("MoveVariableDeclarationIntoWhen")

package com.alfasreda.mobilecity.ui.main.adapters

import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.alfasreda.mobilecity.BuildConfig
import com.alfasreda.mobilecity.R
import com.alfasreda.mobilecity.databinding.ItemObjectToListBinding
import com.alfasreda.mobilecity.databinding.ItemTransportToListBinding
import com.alfasreda.mobilecity.models.BtDevice
import com.alfasreda.mobilecity.utils.RxBus
import com.alfasreda.mobilecity.utils.appRingtone
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers


private const val CITY_OBJECT = 0
private const val TRANSPORT = 1

class BtDeviceListAdapter(
    private val listener: Listener
    ) : ListAdapter<BtDevice, RecyclerView.ViewHolder>(DIFF_CALLBACK) {

    private var devices = mutableListOf<BtDevice>()

    val composite = CompositeDisposable()

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
            BtDevice.BUS, BtDevice.TROLLEYBUS, BtDevice.TRAM -> TRANSPORT
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

        private var positionIndex: Int = -1

        fun bind(device: BtDevice) {
            with(binding) {

                tvObjectName.apply {
                    text = device.description
                    contentDescription = device.description
                    setOnClickListener {

                        listener.onAdapterBtnCallClick(device)
                    }
                    setOnLongClickListener {
                        listener.onAdapterItemLongClick(device.description)
                        true
                    }
                }
                btnCall.setOnClickListener {
                    listener.onAdapterBtnCallClick(device)
                }

                composite.add(
                    RxBus.toObservable()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe {
                            countDownTimer.start()
                            if (device.id == it.id) {
                                val value = "${it.rssi} dB"
                                tvRssiValue.text = value
                                countDownTimer.cancel()

                                if (it.isCall()) {
                                    layoutItem.setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.super_light_green))
                                }
                                else {
                                    layoutItem.setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.white))
                                    itemView.context.appRingtone()?.stop()
                                }
                            }
                        }
                )
            }
        }

        private val countDownTimer = object : CountDownTimer(5000, 1000) {
            override fun onTick(millisUntilFinished: Long) {}

            override fun onFinish() {
                if (positionIndex >= 0) {
                    try {
                        devices.removeAt(positionIndex)
                        notifyItemRemoved(positionIndex)
                    } catch (e: Exception) {
                        if (BuildConfig.DEBUG) e.printStackTrace()
                    }
                }
            }
        }
    }

    inner class TransportViewHolder(private val binding: ItemTransportToListBinding) : RecyclerView.ViewHolder(binding.root) {

        private var positionIndex: Int = -1

        fun bind(device: BtDevice) {
            with(binding) {

                val type = device.type
                when(type) {
                    BtDevice.BUS -> {
                        tvTransportType.apply {
                            text = "Автобус"; contentDescription = text
                        }
                    }
                    BtDevice.TRAM -> {
                        tvTransportType.apply {
                            text = "Трамвай"; contentDescription = text
                        }
                    }
                    BtDevice.TROLLEYBUS -> {
                        tvTransportType.apply {
                            text = "Троллейбус"; contentDescription = text
                        }
                    }
                }
                tvRouteValue.text = device.route

                layoutItem.apply {
                    contentDescription = "${tvTransportType.contentDescription} ${tvRouteTitle.text} ${tvRouteValue.text}"
                    setOnLongClickListener { layout ->
                        listener.onAdapterItemLongClick(layout.contentDescription.toString())
                        true
                    }
                }

                btnCall.setOnClickListener {
                    listener.onAdapterBtnCallClick(device)
                }

                composite.add(
                    RxBus.toObservable()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe {
                            countDownTimer.start()
                            if (device.id == it.id) {
                                val value = "${it.rssi} dB"
                                tvRssiValue.text = value
                                countDownTimer.cancel()

                                if (it.isCall()) {
                                    layoutItem.setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.super_light_green))
                                }
                                else {
                                    layoutItem.setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.white))
                                    itemView.context.appRingtone()?.stop()
                                }
                            }
                        }
                )

            }
        }

        private val countDownTimer = object : CountDownTimer(5000, 1000) {
            override fun onTick(millisUntilFinished: Long) {}

            override fun onFinish() {
                if (positionIndex >= 0) {
                    try {
                        devices.removeAt(positionIndex)
                        notifyItemRemoved(positionIndex)
                    } catch (e: Exception) {
                        if (BuildConfig.DEBUG) e.printStackTrace()
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
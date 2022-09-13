@file:Suppress("MoveVariableDeclarationIntoWhen")

package com.alfasreda.mobilecity.ui.main.adapters

import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.alfasreda.mobilecity.BuildConfig
import com.alfasreda.mobilecity.R
import com.alfasreda.mobilecity.databinding.ItemObjectToPageBinding
import com.alfasreda.mobilecity.databinding.ItemTransportToPageBinding
import com.alfasreda.mobilecity.models.BtDevice
import com.alfasreda.mobilecity.utils.RxBus
import com.alfasreda.mobilecity.utils.appRingtone
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

private const val CITY_OBJECT = 0
private const val TRANSPORT = 1

class BtDevicePageAdapter(
    private val listener: IBtDevicesAdapterListener
) : ListAdapter<BtDevice, RecyclerView.ViewHolder>(DIFF_CALLBACK) {

    private var devices = mutableListOf<BtDevice>()

    private val composite = CompositeDisposable()
    private var timerToDelete: CountDownTimer? = null

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
                val binding = ItemObjectToPageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
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
                listener.onAdapterItemAttached(text.toString())
            }
            is TransportViewHolder -> {
                with(holder.itemView) {
                    val typeText = findViewById<TextView>(R.id.tv_object_type).text
                    val routeTitle = findViewById<TextView>(R.id.tv_route_title).text
                    val routeValue = findViewById<TextView>(R.id.tv_route_value).text
                    val text = "$typeText. $routeTitle $routeValue"
                    listener.onAdapterItemAttached(text)
                }
            }
        }
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {

        if (!composite.isDisposed) {
            composite.dispose()
            composite.clear()
        }
        timerToDelete?.cancel()
        super.onDetachedFromRecyclerView(recyclerView)
    }

    private fun setTimerToDelete(position: Int = -1): CountDownTimer {

        val timer = object : CountDownTimer(5000, 1000) {
            override fun onTick(millisUntilFinished: Long) {}

            override fun onFinish() {
                if (position >= 0) {
                    try {
                        devices.removeAt(position)
                        notifyItemRemoved(position)
                        if (devices.isEmpty()) {
                            listener.onEmptyAdapter()
                        }
                    } catch (e: Exception) {
                        if (BuildConfig.DEBUG) e.printStackTrace()
                        cancel()
                    }
                }
            }
        }
        return timer
    }



    inner class CityObjectViewHolder(private val binding: ItemObjectToPageBinding) : RecyclerView.ViewHolder(binding.root) {

        private var positionIndex: Int = -1

        fun bind(device: BtDevice, position: Int, count: Int) {

            positionIndex = position
            with(binding) {

                tvObjectName.text = device.description
                val itemCount = "${position + 1} / $count"
                tvObjectsCount.text = itemCount

                btnPrevious.isEnabled = position > 0
                btnNext.isEnabled = position < count - 1

                tvObjectName.setOnLongClickListener {
                    listener.onAdapterItemLongClick(device.description)
                    true
                }
//                btnPrevious.setOnClickListener {
//                    listener.onAdapterPreviousBtnClick(position)
//                }
//                btnNext.setOnClickListener {
//                    listener.onAdapterNextBtnClick(position)
//                }
                btnCall.setOnClickListener {
                    listener.onAdapterBtnCallClick(device)
                }

                timerToDelete = setTimerToDelete(positionIndex)
                composite.add(
                    RxBus.toObservable()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe {

                            timerToDelete?.start()
                            if (device.id == it.id) {
                                val value = "${it.rssi} dB"
                                tvRssiValue.text = value
                                timerToDelete?.cancel()

                                if (it.isCall()) {
                                    layoutItem.setBackgroundColor(
                                        ContextCompat.getColor(
                                            itemView.context,
                                            R.color.super_light_green
                                        )
                                    )
                                } else {
                                    layoutItem.setBackgroundColor(
                                        ContextCompat.getColor(
                                            itemView.context,
                                            R.color.white
                                        )
                                    )
                                    itemView.context.appRingtone()?.stop()
                                }
                            }
                        }
                )

            }
        }

    }

    inner class TransportViewHolder(private val binding: ItemTransportToPageBinding) : RecyclerView.ViewHolder(binding.root) {

        private var positionIndex: Int = -1

        fun bind(device: BtDevice, position: Int, count: Int) {
            with(binding) {

                positionIndex = position
                val itemCount = "${position + 1} / $count"
                tvObjectsCount.text = itemCount

                btnPrevious.isEnabled = position > 0
                btnNext.isEnabled = position < count - 1

                val type = device.type
                when(type) {
                    BtDevice.BUS -> tvObjectType.apply {
                        text = "Автобус"; contentDescription = text
                    }
                    BtDevice.TRAM -> tvObjectType.apply {
                        text = "Трамвай"; contentDescription = text
                    }
                    BtDevice.TROLLEYBUS -> tvObjectType.apply {
                        text = "Троллейбус"; contentDescription = text
                    }
                }
                tvRouteValue.text = device.route

                layoutItem.apply {
                    contentDescription = "${tvObjectType.contentDescription} ${tvRouteTitle.text} ${tvRouteValue.text}"
                    setOnLongClickListener { layout ->
                        listener.onAdapterItemLongClick(layout.contentDescription.toString())
                        true
                    }
                }

//                btnPrevious.setOnClickListener {
//                    listener.onAdapterPreviousBtnClick(position)
//                }
//                btnNext.setOnClickListener {
//                    listener.onAdapterNextBtnClick(position)
//                }
                btnCall.setOnClickListener {
                    listener.onAdapterBtnCallClick(device)
                }

                timerToDelete = setTimerToDelete(positionIndex)
                composite.add(
                    RxBus.toObservable()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe {

                            timerToDelete?.start()
                            if (device.id == it.id) {
                                val value = "${it.rssi} dB"
                                tvRssiValue.text = value
                                timerToDelete?.cancel()

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

    }


}
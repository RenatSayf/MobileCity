package com.alfasreda.mobilecity.ui.main.adapters

import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.alfasreda.mobilecity.BuildConfig
import com.alfasreda.mobilecity.R
import com.alfasreda.mobilecity.databinding.ItemObjectToPageBinding
import com.alfasreda.mobilecity.models.BtDevice
import com.alfasreda.mobilecity.utils.appRingtone
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.cancel
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class CityObjectsPageAdapter(
    private val lifecycleOwner: LifecycleOwner,
    private val listener: Listener
) : ListAdapter<BtDevice, CityObjectsPageAdapter.ViewHolder>(DIFF_CALLBACK) {

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

    override fun getItemCount(): Int {
        return devices.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemObjectToPageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(devices[position], position, devices.size)
    }

    override fun onViewDetachedFromWindow(holder: ViewHolder) {
        holder.rssiObserver = null

        super.onViewDetachedFromWindow(holder)
    }



    inner class ViewHolder(private val binding: ItemObjectToPageBinding) : RecyclerView.ViewHolder(binding.root) {

        private var positionIndex: Int = -1

        var rssiObserver: Observer<Int>? = null

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
                btnPrevious.setOnClickListener {
                    listener.onAdapterPreviousBtnClick(position)
                }
                btnNext.setOnClickListener {
                    listener.onAdapterNextBtnClick(position)
                }
                btnCall.setOnClickListener {
                    listener.onAdapterBtnCallClick(device)
                }

            }
        }

        private var timer: CountDownTimer? = null

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
        fun onAdapterPreviousBtnClick(position: Int)
        fun onAdapterNextBtnClick(position: Int)
        fun onAdapterBtnCallClick(device: BtDevice)
        fun onAdapterItemLongClick(description: String)
        fun onAdapterItemAttached(description: String)
        fun onAdapterItemsAdded(count: Int)
    }
}
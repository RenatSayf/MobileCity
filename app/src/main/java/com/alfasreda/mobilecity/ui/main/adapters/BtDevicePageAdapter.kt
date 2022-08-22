@file:Suppress("MoveVariableDeclarationIntoWhen")

package com.alfasreda.mobilecity.ui.main.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.alfasreda.mobilecity.R
import com.alfasreda.mobilecity.databinding.ItemBleToPageBinding
import com.alfasreda.mobilecity.models.BtDevice


class BtDevicePageAdapter(
    private val listener: Listener
) : ListAdapter<BtDevice, BtDevicePageAdapter.PageViewHolder>(DIFF_CALLBACK) {

    private var devices = mutableListOf<BtDevice>()

    fun addItems(list: List<BtDevice>) {

        if (devices != list) {
            listener.onItemsAdded(list.size)
        }
        devices = list.toMutableList()
        notifyDataSetChanged()
    }

    fun clear() {
        devices.clear()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PageViewHolder {
        val binding = ItemBleToPageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PageViewHolder, position: Int) {
        holder.bind(devices[position], position, devices.size)
    }

    override fun getItemCount(): Int {
        return devices.size
    }

    override fun onViewAttachedToWindow(holder: PageViewHolder) {
        super.onViewAttachedToWindow(holder)

        val text = holder.itemView.findViewById<TextView>(R.id.tv_object_name).text
        listener.onAdapterItemBind(text.toString())
    }

    inner class PageViewHolder(
        private val binding: ItemBleToPageBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(device: BtDevice, position: Int, count: Int) {

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
            }
        }
    }

    interface Listener {
        fun onAdapterPreviousBtnClick(position: Int)
        fun onAdapterNextBtnClick(position: Int)
        fun onAdapterItemLongClick(description: String)
        fun onAdapterItemBind(description: String)
        fun onItemsAdded(count: Int)
    }
}
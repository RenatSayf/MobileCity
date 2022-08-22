package com.alfasreda.mobilecity.ui.main.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.alfasreda.mobilecity.databinding.ItemBleToListBinding
import com.alfasreda.mobilecity.models.BtDevice

class BtDeviceListAdapter(private val listener: Listener) : ListAdapter<BtDevice, BtDeviceListAdapter.ListViewHolder>(DIFF_CALLBACK) {

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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        val binding = ItemBleToListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ListViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        holder.bind(devices[position])
    }

    override fun getItemCount(): Int {
        return devices.size
    }

    inner class ListViewHolder(private val binding: ItemBleToListBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(device: BtDevice) {
            with(binding) {

                tvObjectName.text = device.description
                tvObjectName.setOnClickListener {

                    listener.onListAdapterItemClick(device)
                }
                tvObjectName.setOnLongClickListener {
                    listener.onAdapterItemLongClick(device.description)
                    true
                }
            }
        }
    }

    interface Listener {
        fun onListAdapterItemClick(device: BtDevice)
        fun onAdapterItemLongClick(description: String)
        fun onAdapterItemsAdded(count: Int)
    }
}
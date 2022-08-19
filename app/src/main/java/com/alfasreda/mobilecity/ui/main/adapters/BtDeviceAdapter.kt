@file:Suppress("MoveVariableDeclarationIntoWhen")

package com.alfasreda.mobilecity.ui.main.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.alfasreda.mobilecity.databinding.ItemBleObjectBinding
import com.alfasreda.mobilecity.models.BtDevice


class BtDeviceAdapter(
    private val listener: Listener
) : ListAdapter<BtDevice, BtDeviceAdapter.ObjectViewHolder>(DIFF_CALLBACK) {

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<BtDevice>() {
            override fun areItemsTheSame(oldItem: BtDevice, newItem: BtDevice): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: BtDevice, newItem: BtDevice): Boolean {
                return oldItem == newItem
            }
        }
    }

    private var recyclerView: RecyclerView? = null

    private var devices = mutableListOf<BtDevice>()

    fun addItems(list: Set<BtDevice>) {
        devices = list.toMutableList()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ObjectViewHolder {
        val binding = ItemBleObjectBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ObjectViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ObjectViewHolder, position: Int) {
        holder.bind(devices[position], position, devices.size)
    }

    override fun getItemCount(): Int {
        return devices.size
    }

    inner class ObjectViewHolder(private val binding: ItemBleObjectBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(device: BtDevice, position: Int, count: Int) {

            with(binding) {

                tvObjectName.text = device.description
                val itemCount = "${position + 1} / $count"
                tvObjectsCount.text = itemCount

                btnPrevious.isEnabled = position > 0
                btnNext.isEnabled = position < count - 1

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
    }
}
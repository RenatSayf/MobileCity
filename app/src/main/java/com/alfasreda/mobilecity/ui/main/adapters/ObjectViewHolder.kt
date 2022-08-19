package com.alfasreda.mobilecity.ui.main.adapters

import androidx.recyclerview.widget.RecyclerView
import com.alfasreda.mobilecity.databinding.ItemBleObjectBinding
import com.alfasreda.mobilecity.models.BtDevice

class ObjectViewHolder(private val binding: ItemBleObjectBinding) : RecyclerView.ViewHolder(binding.root) {

    fun bind(device: BtDevice, position: Int, count: Int) {

        with(binding) {

            tvObjectName.text = device.description
            val itemCount = "$position / $count"
            tvObjectsCount.text = itemCount
        }
    }
}
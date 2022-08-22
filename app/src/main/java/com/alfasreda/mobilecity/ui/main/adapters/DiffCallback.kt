package com.alfasreda.mobilecity.ui.main.adapters

import androidx.recyclerview.widget.DiffUtil
import com.alfasreda.mobilecity.models.BtDevice

val DIFF_CALLBACK = object : DiffUtil.ItemCallback<BtDevice>() {
    override fun areItemsTheSame(oldItem: BtDevice, newItem: BtDevice): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: BtDevice, newItem: BtDevice): Boolean {
        return oldItem == newItem
    }
}
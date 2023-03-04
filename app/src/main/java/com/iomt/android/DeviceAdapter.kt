@file:Suppress("MISSING_KDOC_CLASS_ELEMENTS")

package com.iomt.android

import android.bluetooth.BluetoothDevice
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.iomt.android.DeviceAdapter.DeviceHolder

/**
 * [RecyclerView.Adapter] of [DeviceHolder]
 */
class DeviceAdapter(
    private val inflater: LayoutInflater,
    private var cells: List<DeviceCell>,
    private val onClickListener: OnClickListener
) : RecyclerView.Adapter<DeviceHolder>() {
    /**
     * @param newCells
     */
    fun update(newCells: List<DeviceCell>) {
        cells = newCells
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceHolder = DeviceHolder(inflater, parent, onClickListener)

    override fun onBindViewHolder(deviceHolder: DeviceHolder, position: Int) {
        deviceHolder.bind(cells[position])
        deviceHolder.device = cells[position].device
        // BluetoothDevice device = _devices.get(position);
        // holder._textView.setText(device.getName());
        // holder._device = device;
    }

    /**
     * @return number of [DeviceCell]s present
     */
    override fun getItemCount(): Int = cells.size

    /**
     * [DeviceHolder]
     */
    class DeviceHolder(
        inflater: LayoutInflater,
        parent: ViewGroup?,
        onClickListener: OnClickListener
    ) : RecyclerView.ViewHolder(inflater.inflate(R.layout.row_item, parent, false)) {
        var textView: TextView = itemView.findViewById(R.id.row_item)
        var imageView: ImageView = itemView.findViewById<ImageView?>(R.id.pict).apply {
            // setImageResource(R.drawable.hexoskin)
        }
        private var cell: DeviceCell? = null
        var device: BluetoothDevice? = null

        /**
         * @param abstractCell
         */
        fun bind(abstractCell: AbstractCell) {
            val deviceCell = abstractCell as DeviceCell
            cell = deviceCell
            textView.text = deviceCell.device.name
        }

        init {
            itemView.setOnClickListener { view: View? ->
                cell ?: return@setOnClickListener
                onClickListener.onClickItem(cell, device)
            }
        }
    }

    interface OnClickListener {
        /**
         * @param deviceCell
         * @param device
         */
        fun onClickItem(deviceCell: DeviceCell?, device: BluetoothDevice?)
    }
}

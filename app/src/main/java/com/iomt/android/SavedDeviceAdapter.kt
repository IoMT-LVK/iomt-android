@file:Suppress("MISSING_KDOC_CLASS_ELEMENTS")

package com.iomt.android

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.recyclerview.widget.RecyclerView
import com.iomt.android.SavedDeviceAdapter.SavedDeviceHolder

/**
 * [RecyclerView.Adapter] of [SavedDeviceHolder]
 */
class SavedDeviceAdapter(
    private val inflater: LayoutInflater,
    private var cells: List<DeviceCell>,
    private val onClickListener: OnClickListener
) : RecyclerView.Adapter<SavedDeviceHolder>() {
    /**
     * @param newCells
     */
    @SuppressLint("NotifyDataSetChanged")
    fun update(newCells: List<DeviceCell>) {
        cells = newCells
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SavedDeviceHolder = SavedDeviceHolder(inflater, parent, onClickListener)

    @RequiresApi(Build.VERSION_CODES.S)
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun onBindViewHolder(deviceHolder: SavedDeviceHolder, position: Int) {
        deviceHolder.bind(cells[position])
        deviceHolder.device = cells[position].device
    }

    override fun getItemCount(): Int = cells.size

    class SavedDeviceHolder(
        inflater: LayoutInflater,
        parent: ViewGroup?,
        onClickListener: OnClickListener
    ) : RecyclerView.ViewHolder(inflater.inflate(R.layout.device_item, parent, false)) {
        val name: TextView = itemView.findViewById(R.id.device_name)
        val mac: TextView = itemView.findViewById(R.id.mac_address)
        var imageView: ImageView = itemView.findViewById(R.id.device_pict)
        private var cell: DeviceCell? = null
        var device: BluetoothDevice? = null

        /**
         * @param abstractCell
         */
        @RequiresApi(Build.VERSION_CODES.S)
        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        fun bind(abstractCell: AbstractCell) {
            val deviceCell = abstractCell as DeviceCell
            cell = deviceCell
            name.text = deviceCell.device.name
            mac.text = deviceCell.device.address
            if (deviceCell.device.name.startsWith("HX")) {
                imageView.setImageResource(R.drawable.hexoskin)
            }
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

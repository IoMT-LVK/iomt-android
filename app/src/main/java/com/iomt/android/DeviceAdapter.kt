package com.iomt.android

import android.bluetooth.BluetoothDevice
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.iomt.android.DeviceAdapter.DeviceHolder

class DeviceAdapter(
    private val inflater: LayoutInflater,
    private var cells: List<DeviceCell>,
    private val onClickListener: OnClickListener
) : RecyclerView.Adapter<DeviceHolder>() {
    fun Update(new_cells: List<DeviceCell>) {
        cells = new_cells
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceHolder {
        return DeviceHolder(inflater, parent, onClickListener)
    }

    override fun onBindViewHolder(deviceHolder: DeviceHolder, position: Int) {
        deviceHolder.bind(cells[position])
        deviceHolder.device = cells[position].device
        //        BluetoothDevice device = _devices.get(position);
//        holder._textView.setText(device.getName());
//        holder._device = device;
    }

    override fun getItemCount(): Int {
        return cells.size
        //return _devices.size();
    }

    class DeviceHolder(
        inflater: LayoutInflater,
        parent: ViewGroup?,
        onClickListener: OnClickListener
    ) : RecyclerView.ViewHolder(inflater.inflate(R.layout.row_item, parent, false)) {
        var textView: TextView
        var imageView: ImageView
        private var cell: DeviceCell? = null
        var device: BluetoothDevice? = null
        fun bind(absCell: AbsCell) {
            val deviceCell = absCell as DeviceCell
            cell = deviceCell
            textView.text = deviceCell.device.name
        }

        init {
            textView = itemView.findViewById(R.id.row_item)
            imageView = itemView.findViewById(R.id.pict)
            imageView.setImageResource(R.drawable.hexoskin)
            itemView.setOnClickListener { view: View? ->
                if (cell == null) {
                    return@setOnClickListener
                }
                onClickListener.onClickItem(cell, device)
            }
        }
    }

    interface OnClickListener {
        fun onClickItem(deviceCell: DeviceCell?, device: BluetoothDevice?)
    }
}
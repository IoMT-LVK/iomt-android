package com.iomt.android

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.iomt.android.DeviceInfoAdapter.DeviceInfoHolder

class DeviceInfoAdapter(
    private val inflater: LayoutInflater,
    private val cells: MutableList<DeviceInfoCell>,
    private val onClickListener: OnClickListener
) : RecyclerView.Adapter<DeviceInfoHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceInfoHolder {
        return DeviceInfoHolder(inflater, parent, onClickListener)
    }

    override fun onBindViewHolder(deviceInfoHolder: DeviceInfoHolder, position: Int) {
        deviceInfoHolder.bind(cells[position])
        deviceInfoHolder.deviceInfo = cells[position].deviceInfo
    }

    override fun getItemCount(): Int {
        return cells.size
    }

    inner class DeviceInfoHolder(
        inflater: LayoutInflater,
        parent: ViewGroup?,
        onClickListener: OnClickListener
    ) : RecyclerView.ViewHolder(inflater.inflate(R.layout.device_item, parent, false)) {
        var name: TextView
        var mac: TextView
        var imageView: ImageView
        var delImage: ImageView
        private var cell: DeviceInfoCell? = null
        var deviceInfo: DeviceInfo? = null
        fun bind(absCell: AbsCell) {
            val deviceInfoCell = absCell as DeviceInfoCell
            cell = deviceInfoCell
            name.text = deviceInfoCell.deviceInfo.name
            mac.text = deviceInfoCell.deviceInfo.address
            delImage.setImageResource(R.drawable.delete)
            if (deviceInfoCell.deviceInfo.name.startsWith("HX")) {
                imageView.setImageResource(R.drawable.hexoskin)
            }
            val prefs = itemView.context.getSharedPreferences(
                itemView.context.getString(R.string.ACC_DATA),
                Context.MODE_PRIVATE
            )
            val httpRequests = HTTPRequests(
                itemView.context,
                prefs.getString("JWT", ""),
                prefs.getString("UserId", "")
            )
            delImage.setOnClickListener {
                val device = deviceInfoCell.deviceInfo
                httpRequests.deleteDevice(device)
                cells.remove(deviceInfoCell)
                notifyDataSetChanged()
            }
        }

        init {
            name = itemView.findViewById(R.id.device_name)
            mac = itemView.findViewById(R.id.mac_address)
            imageView = itemView.findViewById(R.id.device_pict)
            delImage = itemView.findViewById(R.id.action)
            itemView.setOnClickListener { view: View? ->
                if (cell == null) {
                    return@setOnClickListener
                }
                onClickListener.onClickItem(cell, deviceInfo)
            }
        }
    }

    interface OnClickListener {
        fun onClickItem(deviceInfoCell: DeviceInfoCell?, deviceInfo: DeviceInfo?)
    }
}
@file:Suppress("MISSING_KDOC_CLASS_ELEMENTS", "MISSING_KDOC_TOP_LEVEL")

package com.iomt.android

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CharAdapter(private val inflater: LayoutInflater, private var cells: List<CharCell>) :
    RecyclerView.Adapter<CharAdapter.CharHolder>() {
    /**
     * @param newCells sells to set
     */
    fun update(newCells: List<CharCell>) {
        cells = newCells
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CharHolder = CharHolder(inflater, parent)

    override fun onBindViewHolder(charHolder: CharHolder, position: Int) {
        charHolder.bind(cells[position])
    }

    override fun getItemCount(): Int = cells.size

    class CharHolder(inflater: LayoutInflater, parent: ViewGroup?) :
        RecyclerView.ViewHolder(inflater.inflate(R.layout.row_item, parent, false)) {
        var textView: TextView = itemView.findViewById(R.id.row_item)
        var imageView: ImageView = itemView.findViewById(R.id.pict)
        var device: BluetoothDevice? = null

        /**
         * @param abstractCell sell to process
         */
        @SuppressLint("SetTextI18n")
        fun bind(abstractCell: AbstractCell) {
            val charCell = abstractCell as CharCell
            val name = charCell.name
            textView.text = name
            when (name) {
                "HEART RATE: " -> imageView.setImageResource(R.drawable.heart)
                "RESP. RATE: " -> imageView.setImageResource(R.drawable.lungs1)
                "INSP: " -> imageView.setImageResource(R.drawable.insp)
                "EXP: " -> imageView.setImageResource(R.drawable.exp)
                "STEP COUNT: " -> imageView.setImageResource(R.drawable.steps)
                "CADENCE: " -> imageView.setImageResource(R.drawable.cadence)
                "ACTIVITY: " -> imageView.setImageResource(R.drawable.act)
                "Connected", "Connecting ..." -> imageView.setImageResource(R.drawable.blt)
                "Disconnected" -> imageView.setImageResource(R.drawable.nosig)
                else -> {
                    // this is a generated else block
                }
            }
            textView.text = charCell.name + charCell.data
        }
    }
}

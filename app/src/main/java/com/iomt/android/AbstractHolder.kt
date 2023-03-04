package com.iomt.android

import android.view.View
import androidx.recyclerview.widget.RecyclerView

/**
 * Holder of abstract cell
 */
abstract class AbstractHolder(itemView: View?) : RecyclerView.ViewHolder(itemView!!) {
    /**
     * @param abstractCell
     */
    abstract fun bind(abstractCell: AbstractCell?)
}

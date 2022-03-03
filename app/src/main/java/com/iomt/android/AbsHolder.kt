package com.iomt.android

import android.view.View
import androidx.recyclerview.widget.RecyclerView

abstract class AbsHolder(itemView: View?) : RecyclerView.ViewHolder(itemView!!) {
    abstract fun bind(absCell: AbsCell?)
}
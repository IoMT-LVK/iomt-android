package com.iomt.android;

import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

public abstract class AbsHolder extends RecyclerView.ViewHolder {
    public AbsHolder(final View itemView) {
        super(itemView);
    }

    public abstract void bind(final AbsCell absCell);
}

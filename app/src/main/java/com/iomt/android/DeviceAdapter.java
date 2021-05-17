package com.iomt.android;

import android.bluetooth.BluetoothDevice;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.DeviceHolder> {
    private final LayoutInflater inflater;
    private List<DeviceCell> cells;
    private final OnClickListener onClickListener;

    public DeviceAdapter(final LayoutInflater inflater, final List<DeviceCell> cells, final OnClickListener onClickListener) {
        this.inflater = inflater;
        this.cells = cells;
        this.onClickListener = onClickListener;
    }

    public void Update(final List<DeviceCell> new_cells) {
        cells = new_cells;
        notifyDataSetChanged();
    }

    @Override
    public DeviceHolder onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {
        return new DeviceHolder(inflater, parent, onClickListener);
    }

    @Override
    public void onBindViewHolder(final DeviceHolder deviceHolder, int position) {
        deviceHolder.bind(cells.get(position));
        deviceHolder.device = cells.get(position).getDevice();
//        BluetoothDevice device = _devices.get(position);
//        holder._textView.setText(device.getName());
//        holder._device = device;
    }

    @Override
    public int getItemCount() {
        return cells.size();
        //return _devices.size();
    }

    static final class DeviceHolder extends RecyclerView.ViewHolder {
        public TextView textView;
        public ImageView imageView;
        private DeviceCell cell = null;
        public BluetoothDevice device;

        public DeviceHolder(LayoutInflater inflater, ViewGroup parent, DeviceAdapter.OnClickListener onClickListener) {
            super(inflater.inflate(R.layout.row_item, parent, false));
            textView = itemView.findViewById(R.id.row_item);
            imageView = itemView.findViewById(R.id.pict);
            imageView.setImageResource(R.drawable.hexoskin);
            itemView.setOnClickListener(view -> {
                if (cell == null) {
                    return;
                }
                onClickListener.onClickItem(cell, device);
            });
        }

        public void bind(final AbsCell absCell) {
            final DeviceCell deviceCell = (DeviceCell) absCell;
            this.cell = deviceCell;
            textView.setText(deviceCell.getDevice().getName());
        }
    }

    public interface OnClickListener {
        void onClickItem(DeviceCell deviceCell, BluetoothDevice device);
    }
}


package com.iomt.android;

import android.bluetooth.BluetoothDevice;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SavedDeviceAdapter extends RecyclerView.Adapter<SavedDeviceAdapter.SavedDeviceHolder> {
    private final LayoutInflater inflater;
    private List<DeviceCell> cells;
    private final OnClickListener onClickListener;

    public SavedDeviceAdapter(final LayoutInflater inflater, final List<DeviceCell> cells, final OnClickListener onClickListener) {
        this.inflater = inflater;
        this.cells = cells;
        this.onClickListener = onClickListener;
    }

    public void Update(final List<DeviceCell> new_cells) {
        cells = new_cells;
        notifyDataSetChanged();
    }

    @Override
    public SavedDeviceHolder onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {
        return new SavedDeviceHolder(inflater, parent, onClickListener);
    }

    @Override
    public void onBindViewHolder(final SavedDeviceHolder deviceHolder, int position) {
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

    static final class SavedDeviceHolder extends RecyclerView.ViewHolder {
        public TextView name, mac;
        public ImageView imageView;
        private DeviceCell cell = null;
        public BluetoothDevice device;

        public SavedDeviceHolder(LayoutInflater inflater, ViewGroup parent, SavedDeviceAdapter.OnClickListener onClickListener) {
            super(inflater.inflate(R.layout.device_item, parent, false));//????
            name = itemView.findViewById(R.id.device_name);
            mac = itemView.findViewById(R.id.mac_address);
            //textView = itemView.findViewById(R.id.row_item);
            imageView = itemView.findViewById(R.id.device_pict);
            //imageView.setImageResource(R.drawable.hexoskin);

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
            name.setText(deviceCell.getDevice().getName());
            mac.setText(deviceCell.getDevice().getAddress());
            if (deviceCell.getDevice().getName().startsWith("HX")) {
                imageView.setImageResource(R.drawable.hexoskin);
            }
        }
    }

    public interface OnClickListener {
        void onClickItem(DeviceCell deviceCell, BluetoothDevice device);
    }
}

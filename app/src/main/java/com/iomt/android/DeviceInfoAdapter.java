package com.iomt.android;

import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class DeviceInfoAdapter extends RecyclerView.Adapter<DeviceInfoAdapter.DeviceInfoHolder> {
    private final LayoutInflater inflater;
    private List<DeviceInfoCell> cells;
    private final DeviceInfoAdapter.OnClickListener onClickListener;

    public DeviceInfoAdapter(final LayoutInflater inflater, final List<DeviceInfoCell> cells, final DeviceInfoAdapter.OnClickListener onClickListener) {
        this.inflater = inflater;
        this.cells = cells;
        this.onClickListener = onClickListener;
    }

    @NonNull
    @Override
    public DeviceInfoAdapter.DeviceInfoHolder onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {
        return new DeviceInfoAdapter.DeviceInfoHolder(inflater, parent, onClickListener);
    }

    @Override
    public void onBindViewHolder(final DeviceInfoAdapter.DeviceInfoHolder deviceInfoHolder, int position) {
        deviceInfoHolder.bind(cells.get(position));
        deviceInfoHolder.deviceInfo = cells.get(position).getDeviceInfo();
    }

    @Override
    public int getItemCount() {
        return cells.size();
    }


    final class DeviceInfoHolder extends RecyclerView.ViewHolder {
        public TextView name, mac;
        public ImageView imageView, delImage;
        private DeviceInfoCell cell = null;
        public DeviceInfo deviceInfo;

        public DeviceInfoHolder(LayoutInflater inflater, ViewGroup parent, DeviceInfoAdapter.OnClickListener onClickListener) {
            super(inflater.inflate(R.layout.device_item, parent, false));
            name = itemView.findViewById(R.id.device_name);
            mac = itemView.findViewById(R.id.mac_address);
            imageView = itemView.findViewById(R.id.device_pict);
            delImage = itemView.findViewById(R.id.action);

            itemView.setOnClickListener(view -> {
                if (cell == null) {
                    return;
                }
                onClickListener.onClickItem(cell, deviceInfo);
            });
        }

        public void bind(final AbsCell absCell) {
            final DeviceInfoCell deviceInfoCell = (DeviceInfoCell) absCell;
            this.cell = deviceInfoCell;
            name.setText(deviceInfoCell.getDeviceInfo().getName());
            mac.setText(deviceInfoCell.getDeviceInfo().getAddress());
            delImage.setImageResource(R.drawable.delete);
            if (deviceInfoCell.getDeviceInfo().getName().startsWith("HX")) {
                imageView.setImageResource(R.drawable.hexoskin);
            }
            SharedPreferences prefs = itemView.getContext().getSharedPreferences(itemView.getContext().getString(R.string.ACC_DATA), MODE_PRIVATE);
            HTTPRequests httpRequests = new HTTPRequests(itemView.getContext(), prefs.getString("JWT", ""), prefs.getString("UserId", ""));

            delImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DeviceInfo device = deviceInfoCell.getDeviceInfo();

                    httpRequests.del_dev(device);

                    cells.remove(deviceInfoCell);
                    notifyDataSetChanged();
                }
            });
        }
    }

    public interface OnClickListener {
        void onClickItem(DeviceInfoCell deviceInfoCell, DeviceInfo deviceInfo);
    }
}

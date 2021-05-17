package com.iomt.android;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CharAdapter extends RecyclerView.Adapter<CharAdapter.CharHolder> {
    private final LayoutInflater inflater;
    private List<CharCell> cells;

    public CharAdapter(final LayoutInflater inflater, final List<CharCell> cells) {
        this.inflater = inflater;
        this.cells = cells;
    }

    public void Update(final List<CharCell> new_cells) {
        cells = new_cells;
        notifyDataSetChanged();
    }

    @Override
    public CharHolder onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {
        return new CharHolder(inflater, parent);
    }

    @Override
    public void onBindViewHolder(final CharHolder charHolder, int position) {
        charHolder.bind(cells.get(position));
    }

    @Override
    public int getItemCount() {
        return cells.size();
    }

    static final class CharHolder extends RecyclerView.ViewHolder {
        public TextView textView;
        public ImageView imageView;
        public BluetoothDevice device;

        public CharHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.row_item, parent, false));
            textView = itemView.findViewById(R.id.row_item);
            imageView = itemView.findViewById(R.id.pict);
        }

        @SuppressLint("SetTextI18n")
        public void bind(final AbsCell absCell) {
            final CharCell charCell = (CharCell) absCell;
            String name = charCell.getName();
            textView.setText(name);
            switch (name) {
                case "HEART RATE: ":
                    imageView.setImageResource(R.drawable.heart);
                    break;
                case "RESP. RATE: ":
                    imageView.setImageResource(R.drawable.lungs1);
                    break;
                case "INSP: ":
                    imageView.setImageResource(R.drawable.insp);
                    break;
                case "EXP: ":
                    imageView.setImageResource(R.drawable.exp);
                    break;
                case "STEP COUNT: ":
                    imageView.setImageResource(R.drawable.steps);
                    break;
                case "CADENCE: ":
                    imageView.setImageResource(R.drawable.cadence);
                    break;
                case "ACTIVITY: ":
                    imageView.setImageResource(R.drawable.act);
                    break;
                case "Connected":
                case "Connecting ...":
                    imageView.setImageResource(R.drawable.blt);
                    break;
                case "Disconnected":
                    imageView.setImageResource(R.drawable.nosig);
                    break;
            }
            textView.setText(charCell.getName() + charCell.getData());
        }
    }
}


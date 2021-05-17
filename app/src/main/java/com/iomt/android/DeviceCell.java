package com.iomt.android;

import android.bluetooth.BluetoothDevice;

public class DeviceCell extends AbsCell {
    public BluetoothDevice device;

    public DeviceCell(BluetoothDevice device) {
        this.device = device;
    }

    public BluetoothDevice getDevice() {
        return device;
    }
}

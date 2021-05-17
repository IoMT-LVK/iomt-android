package com.iomt.android;

import android.bluetooth.BluetoothDevice;

public class DeviceInfo {
    private String device_name;
    private String device_id;
    private String device_type;

    public DeviceInfo(String _name, String _addr, String _type) {
        device_name = _name;
        device_id = _addr;
        device_type = _type;
    }

    public DeviceInfo(BluetoothDevice d, String _type) {
        device_name = d.getName();
        device_id = d.getAddress();
        device_type = _type;
    }

    public String getName() { return device_name; }
    public String getAddress() { return device_id; }
    public String getDevice_type() { return device_type; }

    public boolean equals(DeviceInfo obj) {
        return (this.device_name.equals(obj.device_name) && this.device_id.equals(obj.device_id) && this.device_type.equals(obj.device_type));
    }
}

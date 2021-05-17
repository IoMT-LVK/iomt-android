package com.iomt.android;

public class DeviceInfoCell extends AbsCell {
    public DeviceInfo deviceInfo;

    public DeviceInfoCell(DeviceInfo device) {
        this.deviceInfo = device;
    }

    public DeviceInfo getDeviceInfo() {
        return deviceInfo;
    }
}

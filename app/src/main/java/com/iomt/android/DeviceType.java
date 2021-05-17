package com.iomt.android;

public class DeviceType {
    private String device_type;
    private String prefix;

    public DeviceType(String dt, String pr) {
        device_type = dt;
        prefix = pr;
    }

    public String getDevice_type() {
        return device_type;
    }

    public String getPrefix() {
        return prefix;
    }
}

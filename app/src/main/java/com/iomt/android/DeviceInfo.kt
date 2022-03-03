package com.iomt.android

import android.bluetooth.BluetoothDevice

class DeviceInfo {
    var name: String
        private set
    var address: String
        private set
    var device_type: String
        private set

    constructor(_name: String, _addr: String, _type: String) {
        name = _name
        address = _addr
        device_type = _type
    }

    constructor(d: BluetoothDevice, _type: String) {
        name = d.name
        address = d.address
        device_type = _type
    }

    fun equals(obj: DeviceInfo): Boolean {
        return name == obj.name && address == obj.address && device_type == obj.device_type
    }
}
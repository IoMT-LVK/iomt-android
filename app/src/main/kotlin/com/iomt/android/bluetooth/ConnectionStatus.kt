package com.iomt.android.bluetooth

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt

/**
 * Enum class that represents current ble connection state
 */
enum class ConnectionStatus {
    /**
     * Device is connected
     */
    CONNECTED,

    /**
     * Device is connecting/disconnected
     */
    CONNECTING,

    /**
     * Device is disconnected
     */
    DISCONNECTED,
    ;
    companion object {
        /**
         * @param bondState
         */
        fun fromBondState(bondState: Int) = when (bondState) {
            BluetoothDevice.BOND_BONDED -> CONNECTED
            BluetoothDevice.BOND_BONDING -> CONNECTING
            else -> DISCONNECTED
        }

        /**
         * @param connectionState
         */
        fun fromConnectionState(connectionState: Int) = when (connectionState) {
            BluetoothGatt.STATE_DISCONNECTED -> DISCONNECTED
            BluetoothGatt.STATE_CONNECTED -> CONNECTED
            else -> CONNECTING
        }
    }
}

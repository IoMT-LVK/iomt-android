package com.iomt.android.bluetooth

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
}

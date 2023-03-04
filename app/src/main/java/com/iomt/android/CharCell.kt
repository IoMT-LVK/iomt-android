package com.iomt.android

/**
 * @property name name of characteristic cell
 */
data class CharCell(var name: String) : AbstractCell() {
    /**
     * Some data
     */
    var data = "--"

    /**
     * Reset cell data
     */
    fun reset() {
        data = "--"
    }
}

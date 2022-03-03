package com.iomt.android

class CharCell(var name: String) : AbsCell() {
    var data = "--"
    fun reset() {
        data = "--"
    }
}
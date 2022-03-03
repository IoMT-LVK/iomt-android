package com.iomt.android

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.util.*

class DeviceViewModel : ViewModel() {
    private val cellsLiveData = MutableLiveData<List<AbsCell>>()
    fun addCell(deviceCell: AbsCell) {
        val oldList = cellsLiveData.value ?: return
        val tmp: MutableList<AbsCell> = ArrayList(oldList)
        tmp.add(deviceCell)
        cellsLiveData.value = tmp
    }

    fun resetCells() {
        val tmp: List<AbsCell> = ArrayList()
        cellsLiveData.value = tmp
    }

    fun getCellsLiveData(): LiveData<List<AbsCell>> {
        return cellsLiveData
    }

    init {
        resetCells()
    }
}
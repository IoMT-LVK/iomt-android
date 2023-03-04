@file:Suppress("MISSING_KDOC_CLASS_ELEMENTS", "MISSING_KDOC_ON_FUNCTION")

package com.iomt.android

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.util.*

/**
 * Device View Model
 */
class DeviceViewModel : ViewModel() {
    @Suppress("TYPE_ALIAS")
    private val cellsLiveData: MutableLiveData<List<AbstractCell>> = MutableLiveData()

    /**
     * @param deviceCell
     */
    fun addCell(deviceCell: AbstractCell) {
        val oldList = cellsLiveData.value ?: return
        val tmp: MutableList<AbstractCell> = ArrayList(oldList)
        tmp.add(deviceCell)
        cellsLiveData.value = tmp
    }

    fun resetCells() {
        val tmp: List<AbstractCell> = ArrayList()
        cellsLiveData.value = tmp
    }

    @Suppress("TYPE_ALIAS")
    fun getCellsLiveData(): LiveData<List<AbstractCell>> = cellsLiveData

    init {
        resetCells()
    }
}

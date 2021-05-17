package com.iomt.android;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

public class DeviceViewModel extends ViewModel {
    private final MutableLiveData<List<AbsCell>> cellsLiveData = new MutableLiveData<>();

    public DeviceViewModel () {resetCells();}

    public void addCell(final AbsCell deviceCell) {
        final List<AbsCell> oldList = cellsLiveData.getValue();
        if (oldList == null) {
            return;
        }
        final List<AbsCell> tmp = new ArrayList<>(oldList);
        tmp.add(deviceCell);
        cellsLiveData.setValue(tmp);
    }

    public void resetCells() {
        final List<AbsCell> tmp = new ArrayList<>();
        cellsLiveData.setValue(tmp);
    }

    public LiveData<List<AbsCell>> getCellsLiveData() {
        return cellsLiveData;
    }
}

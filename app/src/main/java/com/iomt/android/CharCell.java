package com.iomt.android;

public class CharCell extends AbsCell {
    private String name;
    private String data;

    public CharCell(String name) {
        this.name = name;
        data = "--";
    }

    public String getName() {
        return name;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void reset() {
        data = "--";
    }
}

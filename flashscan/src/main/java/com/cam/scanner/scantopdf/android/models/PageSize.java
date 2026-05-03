package com.cam.scanner.scantopdf.android.models;

public class PageSize {

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    private boolean checked;

    public PageSize(String sizeKey, String sizeValue) {
        this.sizeKey = sizeKey;
        this.sizeValue = sizeValue;
    }

    public String getSizeKey() {
        return sizeKey;
    }

    public void setSizeKey(String sizeKey) {
        this.sizeKey = sizeKey;
    }

    public String getSizeValue() {
        return sizeValue;
    }

    public void setSizeValue(String sizeValue) {
        this.sizeValue = sizeValue;
    }

    private String sizeKey;
    private String sizeValue;
}

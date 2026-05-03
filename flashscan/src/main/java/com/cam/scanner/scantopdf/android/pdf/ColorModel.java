package com.cam.scanner.scantopdf.android.pdf;

public class ColorModel {
    private String colorCode;
    private boolean checked;

    public ColorModel(String colorCode, boolean checked) {
        this.colorCode = colorCode;
        this.checked = checked;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public String getColorCode() {
        return colorCode;
    }

    public void setColorCode(String colorCode) {
        this.colorCode = colorCode;
    }
}

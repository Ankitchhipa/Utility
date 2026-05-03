package com.cam.scanner.scantopdf.android.models;

public class ImageToPdfOptions extends PDFOptions {

    public int getPdfQuality() {
        return pdfQuality;
    }

    public void setPdfQuality(int pdfQuality) {
        this.pdfQuality = pdfQuality;
    }

    private int pdfQuality;

    public int getBorderWidth() {
        return borderWidth;
    }

    public void setBorderWidth(int borderWidth) {
        this.borderWidth = borderWidth;
    }

    private int borderWidth;

    public boolean isWaterMarkAdded() {
        return waterMarkAdded;
    }

    public void setWaterMarkAdded(boolean waterMarkAdded) {
        this.waterMarkAdded = waterMarkAdded;
    }

    public WaterMark getWaterMark() {
        return waterMark;
    }

    public void setWaterMark(WaterMark waterMark) {
        this.waterMark = waterMark;
    }

    private boolean waterMarkAdded;
    private WaterMark waterMark;
}

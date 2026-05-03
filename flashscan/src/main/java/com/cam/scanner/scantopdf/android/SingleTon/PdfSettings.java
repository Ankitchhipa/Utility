package com.cam.scanner.scantopdf.android.SingleTon;

public class PdfSettings {

    private static volatile PdfSettings instance = null;

    public String getSelectedPdfPageSize() {
        return selectedPdfPageSize;
    }

    public void setSelectedPdfPageSize(String selectedPdfPageSize) {
        this.selectedPdfPageSize = selectedPdfPageSize;
    }

    private String selectedPdfPageSize;

    private PdfSettings() {

    }

    public static PdfSettings getInstance() {
        if (instance == null) {
            synchronized (PdfSettings.class) {
                if (instance == null) {
                    instance = new PdfSettings();
                }
            }
        }
        return instance;
    }


}

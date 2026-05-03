package com.cam.scanner.scantopdf.android.interfaces;

public interface PdfToImageCallback {

    void onConversionStart();

    void onConversionCompleted(String savedDirPath, boolean isSuccess);

}

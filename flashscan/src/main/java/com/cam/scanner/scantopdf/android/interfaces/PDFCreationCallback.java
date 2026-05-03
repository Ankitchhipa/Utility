package com.cam.scanner.scantopdf.android.interfaces;

public interface PDFCreationCallback {
    void onPdfCreationStarted();

    void onPdfCreated(String savedPdfPath);
}

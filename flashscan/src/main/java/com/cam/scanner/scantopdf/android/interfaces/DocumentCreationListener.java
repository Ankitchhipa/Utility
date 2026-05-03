package com.cam.scanner.scantopdf.android.interfaces;

public interface DocumentCreationListener {
    void onDocumentCreationStart();

    void onDocumentCreated(String folderPath);
}

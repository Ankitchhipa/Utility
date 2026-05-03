package com.cam.scanner.scantopdf.android.interfaces;

import java.io.File;

public interface CreateTempBitmapListener {
    void onCompressingStart();

    void onCompressingComplete(File compressedFile);
}

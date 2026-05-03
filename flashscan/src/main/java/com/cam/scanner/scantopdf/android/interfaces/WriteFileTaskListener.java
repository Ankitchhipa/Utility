package com.cam.scanner.scantopdf.android.interfaces;

public interface WriteFileTaskListener {
    void onWriteStart();

    void onWriteCompleted(String savedFilePath, boolean shouldDlgShow);

}

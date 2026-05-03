package com.cam.scanner.scantopdf.android.interfaces;

public interface CopyOperationListener {

    void onCopyStart();

    void onCopyComplete(int fileOperation);
}

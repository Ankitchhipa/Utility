package com.cam.scanner.scantopdf.android.interfaces;

public interface ReadFileListener {

    void onReadingStart();

    void onReadingCompleted(String readedText);
}

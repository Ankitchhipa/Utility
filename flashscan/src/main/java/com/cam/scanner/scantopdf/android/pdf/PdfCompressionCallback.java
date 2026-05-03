package com.cam.scanner.scantopdf.android.pdf;

public interface PdfCompressionCallback {

    void onCompressionStart();

    void onCompressionCompleted(boolean isSuccess,String path);
}

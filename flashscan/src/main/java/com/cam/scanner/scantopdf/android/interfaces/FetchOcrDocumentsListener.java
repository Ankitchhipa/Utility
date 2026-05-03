package com.cam.scanner.scantopdf.android.interfaces;

import com.cam.scanner.scantopdf.android.models.FileModel;

import java.util.List;

public interface FetchOcrDocumentsListener {

    void onFetchingStartOcr();

    void onFetchingCompleted(List<FileModel> fileModelList);
}

package com.cam.scanner.scantopdf.android.interfaces;

import com.cam.scanner.scantopdf.android.models.FileModel;

import java.util.List;

public interface OnFetchingCompleted {

    void onFetchingComplete(List<FileModel> fileModelList);

    void onFetchingStart();
}

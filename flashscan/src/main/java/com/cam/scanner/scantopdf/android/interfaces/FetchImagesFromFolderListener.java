package com.cam.scanner.scantopdf.android.interfaces;

import com.cam.scanner.scantopdf.android.models.ImageModel;

import java.util.List;

public interface FetchImagesFromFolderListener {

    void onFetchingStart();

    void onFetchingComplete(List<ImageModel> imagesList);
}

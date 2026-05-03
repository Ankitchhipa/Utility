package com.cam.scanner.scantopdf.android.interfaces;

import com.cam.scanner.scantopdf.android.models.ImageFolder;

import java.util.List;

public interface FetchGalleryImagesListener {

    void onFetchingStart();

    void onFetchingComplete(List<ImageFolder> imageFolders);
}

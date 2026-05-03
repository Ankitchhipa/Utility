package com.cam.scanner.scantopdf.android.models;

import android.graphics.Bitmap;
import android.graphics.Rect;

import com.cam.scanner.scantopdf.android.models.enums.FilterType;

public class ImageCropping {

    public String processedPath;
    public int id;
    public Bitmap originalBmp;
    public Bitmap processBmp;
    public int filterType = 1;
    public String fileName;
    public int rotation;
    public String x;
    public String y;
    public FilterType mLastFilterType = FilterType.Original;
    public int filterApplyCount = 0;
    public boolean isRotationApply = false;
    public Rect cropRect;
}
